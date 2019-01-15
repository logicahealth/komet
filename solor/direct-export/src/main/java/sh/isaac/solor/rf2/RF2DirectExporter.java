package sh.isaac.solor.rf2;

import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.solor.rf2.config.RF2ConfigType;
import sh.isaac.solor.rf2.config.RF2Configuration;
import sh.isaac.solor.rf2.readers.core.*;
import sh.isaac.solor.rf2.readers.refsets.RF2LanguageRefsetReader;
import sh.isaac.solor.rf2.readers.refsets.RF2RefsetReader;
import sh.komet.gui.manifold.Manifold;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RF2DirectExporter extends TimedTaskWithProgressTracker<Void> implements PersistTaskResult {

    private final File exportDirectory;
    private final Manifold manifold;
    private final String exportMessage;
    private final LocalDateTime localDateTimeNow;
    private final List<RF2Configuration> exportConfigurations;
    private static final int READ_PERMITS = Runtime.getRuntime()
            .availableProcessors() * 2;
    private final Semaphore readSemaphore = new Semaphore(READ_PERMITS);
    private final long pageSize = 102400;

    final List<Integer> currentAssemblages = Arrays.stream(Get.assemblageService().getAssemblageConceptNids())
            .boxed().collect(Collectors.toList());

    public RF2DirectExporter(Manifold manifold, File exportDirectory, String exportMessage){
        this.manifold = manifold;
        this.exportDirectory = exportDirectory;
        this.exportMessage = exportMessage;
        this.localDateTimeNow = LocalDateTime.now();

        this.exportConfigurations = new ArrayList<>();
        this.exportConfigurations.add(new RF2Configuration(RF2ConfigType.CONCEPT, this.localDateTimeNow, this.exportDirectory));
        this.exportConfigurations.add(new RF2Configuration(RF2ConfigType.DESCRIPTION, this.localDateTimeNow, this.exportDirectory));
        this.exportConfigurations.add(new RF2Configuration(RF2ConfigType.RELATIONSHIP, this.localDateTimeNow, this.exportDirectory));
        this.exportConfigurations.add(new RF2Configuration(RF2ConfigType.STATED_RELATIONSHIP, this.localDateTimeNow, this.exportDirectory));
        this.exportConfigurations.add(new RF2Configuration(RF2ConfigType.IDENTIFIER, this.localDateTimeNow, this.exportDirectory));
        this.exportConfigurations.add(new RF2Configuration(RF2ConfigType.TRANSITIVE_CLOSURE, this.localDateTimeNow, this.exportDirectory));
        this.exportConfigurations.add(new RF2Configuration(RF2ConfigType.VERSIONED_TRANSITIVE_CLOSURE, this.localDateTimeNow, this.exportDirectory));
        configureAllLanguageRF2Configurations();
        configureAllRefsetRF2Configurations();

        updateTitle("Export " + this.exportMessage);
        addToTotalWork(this.exportConfigurations.size() + 2);
        Get.activeTasks().add(this);
    }

    private void configureAllLanguageRF2Configurations(){
        Arrays.stream(Get.taxonomyService().getSnapshot(this.manifold)
                .getTaxonomyChildConceptNids(MetaData.LANGUAGE____SOLOR.getNid()))
                .filter(this.currentAssemblages::contains)
                .forEach(activeLanguageNid -> this.exportConfigurations.add(
                        new RF2Configuration(RF2ConfigType.LANGUAGE_REFSET,
                                this.localDateTimeNow,
                                () -> RF2Configuration.GetLanguageStreamSupplier().get()
                                        .filter(chronology -> chronology.getAssemblageNid() == activeLanguageNid),
                                activeLanguageNid,
                                this.exportDirectory.toString())
                        )
                );
    }

    private void configureAllRefsetRF2Configurations(){
        this.currentAssemblages.stream()
                .map(Get::concept)
                .filter(conceptChronology -> RF2Configuration.GetVersionTypesToExportAsRefsets()
                        .contains(Get.assemblageService().getVersionTypeForAssemblage(conceptChronology.getNid())))
                .forEach(conceptChronology -> this.exportConfigurations.add(
                        new RF2Configuration(RF2ConfigType.REFSET,
                                this.localDateTimeNow,
                                () -> RF2Configuration.GetRefsetStreamSupplier().get()
                                        .filter(semanticChronology ->
                                                semanticChronology.getAssemblageNid() == conceptChronology.getNid()
                                        ),
                                Get.assemblageService().getVersionTypeForAssemblage(conceptChronology.getNid()),
                                conceptChronology.getFullyQualifiedName(),
                                this.exportDirectory.toString()
                        )
                ));
    }

    @Override
    protected Void call() {

        try {
            completedUnitOfWork();

            for (RF2Configuration rf2Configuration : this.exportConfigurations) {

                long streamSize = rf2Configuration.getChronologyStream().count();
                long numberOfPages = streamSize / pageSize;

                System.out.println(rf2Configuration.getFilePath() + " - Size: " + streamSize);

                for (int i = 0; i < numberOfPages; i++) {
                    long skip = i * pageSize;
                    submitReaderTask(rf2Configuration.getChronologyStream().skip(skip).limit(pageSize),
                            rf2Configuration);
                }

                if(streamSize - (pageSize * numberOfPages) > 0) {
                    submitReaderTask(rf2Configuration.getChronologyStream().skip(pageSize * numberOfPages),
                            rf2Configuration);
                }

                completedUnitOfWork();
            }

            readSemaphore.acquire(READ_PERMITS);

        }catch (InterruptedException ieE){
            ieE.printStackTrace();
        }finally {
            Get.activeTasks().remove(this);
            readSemaphore.release(READ_PERMITS);

        }

        updateTitle("Zipping " + this.exportConfigurations.get(0).getRootDirectory());
        updateMessage("Zipping to " + this.exportConfigurations.get(0).getZipDirectory());

//        ZipExportDirectory.zip(this.exportConfigurations);

        return null;
    }

    private void submitReaderTask(Stream<? extends Chronology> streamPage, RF2Configuration rf2Configuration){
        switch (rf2Configuration.getRf2ConfigType()){
            case CONCEPT:
                Get.executor().submit(
                        new RF2ConceptReader(streamPage, readSemaphore, manifold, rf2Configuration, pageSize));
                break;
            case DESCRIPTION:
                Get.executor().submit(
                        new RF2DescriptionReader(streamPage, readSemaphore, manifold, rf2Configuration, pageSize));
                break;
            case RELATIONSHIP:
            case STATED_RELATIONSHIP:
                Get.executor().submit(
                        new RF2RelationshipReader(streamPage, readSemaphore, manifold, rf2Configuration, pageSize));
                break;
            case IDENTIFIER:
                Get.executor().submit(
                        new RF2IdentifierReader(streamPage, readSemaphore, manifold, rf2Configuration, pageSize));
                break;
            case TRANSITIVE_CLOSURE:
                Get.executor().submit(
                        new RF2TransitiveClosureReader(streamPage, readSemaphore, manifold, rf2Configuration, pageSize));
                break;
            case VERSIONED_TRANSITIVE_CLOSURE:
                Get.executor().submit(
                        new RF2VersionedTransitiveClosureReader(streamPage, readSemaphore, manifold, rf2Configuration, pageSize));
                break;
            case LANGUAGE_REFSET:
                Get.executor().submit(
                        new RF2LanguageRefsetReader(streamPage, readSemaphore, manifold, rf2Configuration, pageSize));
                break;
            case REFSET:
                Get.executor().submit(
                        new RF2RefsetReader(streamPage, readSemaphore, manifold, rf2Configuration, pageSize));
                break;
        }
    }
}
