package sh.isaac.solor.rf2;

import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.solor.rf2.config.RF2ConfigType;
import sh.isaac.solor.rf2.config.RF2Configuration;
import sh.isaac.solor.rf2.exporters.core.*;
import sh.isaac.solor.rf2.exporters.refsets.RF2LanguageRefsetExporter;
import sh.isaac.solor.rf2.exporters.refsets.RF2RefsetExporter;
import sh.isaac.solor.rf2.utility.PreExportUtility;
import sh.isaac.solor.rf2.utility.RF2ExportHelper;
import sh.komet.gui.manifold.Manifold;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class RF2DirectExporter extends TimedTaskWithProgressTracker<Void> implements PersistTaskResult {

    private final File exportDirectory;
    private final Manifold manifold;
    private final String exportMessage;
    private final LocalDateTime localDateTimeNow;
    private List<RF2Configuration> exportConfigurations;
    private static final int READ_PERMITS = Runtime.getRuntime()
            .availableProcessors() * 2;
    private final Semaphore readSemaphore = new Semaphore(READ_PERMITS);
    private final RF2ExportHelper rf2ExportHelper;
    private final PreExportUtility preExportUtility;

    public RF2DirectExporter(Manifold manifold, File exportDirectory, String exportMessage){
        this.manifold = manifold;
        this.exportDirectory = exportDirectory;
        this.exportMessage = exportMessage;
        this.localDateTimeNow = LocalDateTime.now();
        this.rf2ExportHelper = new RF2ExportHelper(this.manifold);
        this.exportConfigurations = new ArrayList<>();
        this.preExportUtility = new PreExportUtility(this.manifold);

        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() {

        this.exportConfigurations.add(new RF2Configuration(RF2ConfigType.CONCEPT, this.localDateTimeNow, this.exportDirectory, this.manifold));
        this.exportConfigurations.add(new RF2Configuration(RF2ConfigType.DESCRIPTION, this.localDateTimeNow, this.exportDirectory, this.manifold));
        this.exportConfigurations.add(new RF2Configuration(RF2ConfigType.RELATIONSHIP, this.localDateTimeNow, this.exportDirectory, this.manifold));
        this.exportConfigurations.add(new RF2Configuration(RF2ConfigType.STATED_RELATIONSHIP, this.localDateTimeNow, this.exportDirectory, this.manifold));
        this.exportConfigurations.add(new RF2Configuration(RF2ConfigType.IDENTIFIER, this.localDateTimeNow, this.exportDirectory, this.manifold));
        this.exportConfigurations.add(new RF2Configuration(RF2ConfigType.TRANSITIVE_CLOSURE, this.localDateTimeNow, this.exportDirectory, this.manifold));
        this.exportConfigurations.add(new RF2Configuration(RF2ConfigType.VERSIONED_TRANSITIVE_CLOSURE, this.localDateTimeNow, this.exportDirectory, this.manifold));

        List<Integer> currentAssemblageNids = Arrays.stream(Get.assemblageService().getAssemblageConceptNids()).boxed().collect(Collectors.toList());

        //Add all languages TODO- Create a Factory and account for user specific selections
        Arrays.stream(
                Get.taxonomyService().getSnapshot(manifold)
                        .getTaxonomyChildConceptNids(MetaData.LANGUAGE____SOLOR.getNid()))
                .filter(langAssemblageNid -> currentAssemblageNids.contains(langAssemblageNid))
                .forEach(languageNid ->
                        exportConfigurations.add(
                                new RF2Configuration(RF2ConfigType.LANGUAGE_REFSET,
                                        this.localDateTimeNow, languageNid, this.exportDirectory, this.manifold, this.preExportUtility)));
//
//        //Add all refsets TODO- Create a Factory and account for user specific selections
        Arrays.stream(Get.assemblageService().getAssemblageConceptNids())
                .forEach(assemblageNid -> exportConfigurations.add(
                        new RF2Configuration(RF2ConfigType.REFSET, this.localDateTimeNow, assemblageNid,
                                Get.concept(assemblageNid).getFullyQualifiedName(),
                                Get.assemblageService().getVersionTypeForAssemblage(assemblageNid),
                                this.exportDirectory, this.manifold, this.preExportUtility)));

        updateTitle("Export " + this.exportMessage);
        addToTotalWork(this.exportConfigurations.size() + 2);

        try {
            completedUnitOfWork();

            for (RF2Configuration rf2Configuration : this.exportConfigurations) {

                switch (rf2Configuration.getRf2ConfigType()){
                    case CONCEPT:
                        Get.executor().submit(
                                new RF2ConceptExporter(rf2Configuration, rf2ExportHelper, rf2Configuration.getIntStream(), readSemaphore));
                        break;
                    case DESCRIPTION:
                        Get.executor().submit(
                                new RF2DescriptionExporter(rf2Configuration, rf2ExportHelper, rf2Configuration.getIntStream(), readSemaphore));
                        break;
                    case RELATIONSHIP:
                    case STATED_RELATIONSHIP:
                        Get.executor().submit(
                                new RF2RelationshipExporter(rf2Configuration, rf2ExportHelper, rf2Configuration.getIntStream(), readSemaphore));
                        break;
                    case IDENTIFIER:
                        Get.executor().submit(
                                new RF2IdentifierExporter(rf2Configuration, rf2ExportHelper, rf2Configuration.getIntStream(), readSemaphore));
                        break;
                    case TRANSITIVE_CLOSURE:
                        Get.executor().submit(
                                new RF2TransitiveClosureExporter(rf2Configuration, rf2ExportHelper, rf2Configuration.getIntStream(), readSemaphore));
                        break;
                    case VERSIONED_TRANSITIVE_CLOSURE:
                        Get.executor().submit(
                                new RF2VersionedTransitiveClosureExporter(rf2Configuration, rf2ExportHelper, rf2Configuration.getIntStream(), readSemaphore));
                        break;
                    case LANGUAGE_REFSET:
                        Get.executor().submit(
                                new RF2LanguageRefsetExporter(rf2Configuration, rf2ExportHelper, rf2Configuration.getIntStream(), readSemaphore));
                        break;
                    case REFSET:
                        Get.executor().submit(
                                new RF2RefsetExporter(rf2Configuration, rf2ExportHelper, rf2Configuration.getIntStream(), readSemaphore));
                        break;
                }

                completedUnitOfWork();
            }

            readSemaphore.acquireUninterruptibly(READ_PERMITS);

        }finally {
            readSemaphore.release(READ_PERMITS);
            Get.activeTasks().remove(this);
        }

        return null;
    }
}
