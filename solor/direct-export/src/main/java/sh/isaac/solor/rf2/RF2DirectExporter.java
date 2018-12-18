package sh.isaac.solor.rf2;

import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.solor.ZipExportFilesTask;
import sh.isaac.solor.rf2.config.RF2ConfigType;
import sh.isaac.solor.rf2.config.RF2Configuration;
import sh.isaac.solor.rf2.readers.core.*;
import sh.isaac.solor.rf2.readers.refsets.RF2LanguageRefsetReader;
import sh.komet.gui.manifold.Manifold;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RF2DirectExporter extends TimedTaskWithProgressTracker<Void> implements PersistTaskResult {

    private final File exportDirectory;
    private final Manifold manifold;
    private final String exportMessage;
    private final LocalDateTime localDateTimeNow;
    private final List<RF2Configuration> exportConfigurations;
    private final Map<RF2Configuration, List<String>> artifactsToZip = new HashMap<>();
    private static final int READ_PERMITS = Runtime.getRuntime()
            .availableProcessors() * 2;
    private final Semaphore readSemaphore = new Semaphore(READ_PERMITS);
    private final static int BATCH_SIZE = 102400;


    public RF2DirectExporter(Manifold manifold, File exportDirectory, String exportMessage){
        this.manifold = manifold;
        this.exportDirectory = exportDirectory;
        this.exportMessage = exportMessage;
        this.localDateTimeNow = LocalDateTime.now();

        this.exportConfigurations = new ArrayList<>();
        this.exportConfigurations.add(new RF2Configuration(RF2ConfigType.CONCEPT, this.localDateTimeNow));
        this.exportConfigurations.add(new RF2Configuration(RF2ConfigType.DESCRIPTION, this.localDateTimeNow));
        this.exportConfigurations.add(new RF2Configuration(RF2ConfigType.RELATIONSHIP, this.localDateTimeNow));
        this.exportConfigurations.add(new RF2Configuration(RF2ConfigType.STATED_RELATIONSHIP, this.localDateTimeNow));
        this.exportConfigurations.add(new RF2Configuration(RF2ConfigType.IDENTIFIER, this.localDateTimeNow));
        this.exportConfigurations.add(new RF2Configuration(RF2ConfigType.TRANSITIVE_CLOSURE, this.localDateTimeNow));
        this.exportConfigurations.add(new RF2Configuration(RF2ConfigType.VERSIONED_TRANSITIVE_CLOSURE, this.localDateTimeNow));
        configureAllLanguageRF2Configurations();

        updateTitle("Export " + this.exportMessage);
        addToTotalWork(this.exportConfigurations.size() + 2);
        Get.activeTasks().add(this);
    }

    private void configureAllLanguageRF2Configurations(){
        final List<Integer> currentAssemblages = Arrays.stream(Get.assemblageService().getAssemblageConceptNids())
                .boxed().collect(Collectors.toList());
        final RF2Configuration languageConfigTemplate = new RF2Configuration(RF2ConfigType.LANGUAGE_REFSET, this.localDateTimeNow);

        Arrays.stream(Get.taxonomyService().getSnapshot(this.manifold)
                .getTaxonomyChildConceptNids(MetaData.LANGUAGE____SOLOR.getNid()))
                .filter(currentAssemblages::contains)
                .forEach(activeLanguageNid -> this.exportConfigurations.add(new RF2Configuration(RF2ConfigType.LANGUAGE_REFSET,
                        this.localDateTimeNow,
                        languageConfigTemplate.getChronologyStream()
                                .filter(chronology -> chronology.getAssemblageNid() == activeLanguageNid),
                        activeLanguageNid)));
    }

    @Override
    protected Void call() throws Exception {

        try {
            completedUnitOfWork();

            for (RF2Configuration rf2Configuration : this.exportConfigurations) {
                final List<Future<List<String>>> futures = new ArrayList<>();
                final List<String> readerResults = new ArrayList<>();

                List<List<Chronology>> batches = batchReaderStream(rf2Configuration.getChronologyStream());

                for (List<Chronology> batch : batches) {
                    switch (rf2Configuration.getRf2ConfigType()) {
                        case CONCEPT:
                            futures.add(Get.executor().submit(
                                    new RF2ConceptReader(batch, readSemaphore, manifold, rf2Configuration.getMessage()),
                                    new ArrayList<>()));
                            break;
                        case DESCRIPTION:
                            futures.add(Get.executor().submit(
                                    new RF2DescriptionReader(batch, readSemaphore, manifold, rf2Configuration.getMessage()),
                                    new ArrayList<>()));
                            break;
                        case RELATIONSHIP:
                        case STATED_RELATIONSHIP:
                            futures.add(Get.executor().submit(
                                    new RF2RelationshipReader(batch, readSemaphore, manifold, rf2Configuration.getMessage()),
                                    new ArrayList<>()));
                            break;
                        case IDENTIFIER:
                            futures.add(Get.executor().submit(
                                    new RF2IdentifierReader(batch, readSemaphore, manifold, rf2Configuration.getMessage()),
                                    new ArrayList<>()));
                            break;
                        case TRANSITIVE_CLOSURE:
                            futures.add(Get.executor().submit(
                                    new RF2TransitiveClosureReader(batch, readSemaphore, manifold, rf2Configuration.getMessage()),
                                    new ArrayList<>()));
                            break;
                        case VERSIONED_TRANSITIVE_CLOSURE:
                            futures.add(Get.executor().submit(
                                    new RF2VersionedTransitiveClosureReader(batch, readSemaphore, manifold, rf2Configuration.getMessage()),
                                    new ArrayList<>()));
                            break;
                        case LANGUAGE_REFSET:
                            futures.add(Get.executor().submit(
                                    new RF2LanguageRefsetReader(batch, readSemaphore, manifold, rf2Configuration.getMessage()),
                                    new ArrayList<>()));
                    }
                }

                readSemaphore.acquireUninterruptibly(READ_PERMITS);

                for (Future<List<String>> future : futures) {
                    readerResults.addAll(future.get());
                }
                rf2Configuration.addHeaderToExport(readerResults);
                this.artifactsToZip.put(rf2Configuration, readerResults);

                readSemaphore.release(READ_PERMITS);

                completedUnitOfWork();
            }

            completedUnitOfWork();

            updateMessage("Zipping SOLOR" + this.exportMessage + " Export...");
            String zipFilePath = "/SnomedCT_SolorRF2_PRODUCTION_TIME1.zip"
                    .replace("TIME1", DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'")
                            .format(this.localDateTimeNow));
            ZipExportFilesTask zipExportFilesTask =
                    new ZipExportFilesTask(this.exportDirectory, this.artifactsToZip, this.readSemaphore, zipFilePath);
            Get.executor().submit(zipExportFilesTask).get();

            completedUnitOfWork();

        }catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
        }finally {
            Get.activeTasks().remove(this);
        }

        return null;
    }

    private List<List<Chronology>> batchReaderStream(Stream<? extends Chronology> totalChronologyStream){
        final List<Chronology> batch = new ArrayList<>(BATCH_SIZE);
        final List<List<Chronology>> batches = new ArrayList<>();

        totalChronologyStream
                .forEach(chronology -> {
                    batch.add(chronology);
                    if(batch.size() % BATCH_SIZE == 0){
                        batches.add(new ArrayList<>(batch));
                        batch.clear();
                    }
                });

        if(!batch.isEmpty()){
            batches.add(new ArrayList<>(batch));
        }

        return batches;
    }
}
