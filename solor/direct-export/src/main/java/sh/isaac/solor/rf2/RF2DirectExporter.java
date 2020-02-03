package sh.isaac.solor.rf2;

import javafx.beans.InvalidationListener;
import javafx.collections.SetChangeListener;
import javafx.concurrent.Task;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.TaxonomySnapshot;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.ChangeSetListener;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.UuidT3Generator;
import sh.isaac.solor.rf2.config.RF2FileType;
import sh.isaac.solor.rf2.config.RF2Configuration;
import sh.isaac.solor.rf2.config.RF2ReleaseType;
import sh.isaac.solor.rf2.exporters.core.*;
import sh.isaac.solor.rf2.exporters.refsets.RF2LanguageRefsetExporter;
import sh.isaac.solor.rf2.exporters.refsets.RF2RefsetExporter;
import sh.isaac.solor.rf2.utility.PreExportUtility;
import sh.isaac.solor.rf2.utility.RF2ExportHelper;
import sh.isaac.solor.rf2.utility.ZipExportDirectory;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class RF2DirectExporter extends TimedTaskWithProgressTracker<Void> implements PersistTaskResult {

    private final File exportDirectory;
    private final ManifoldCoordinate manifold;
    private final String exportMessage;
    private final LocalDateTime localDateTimeNow;
    private List<RF2Configuration> exportConfigurations;
    // TODO consider replacing readSemaphore with TaskCountManager
    private static final int READ_PERMITS = Runtime.getRuntime().availableProcessors() * 2;
    private final Semaphore readSemaphore = new Semaphore(READ_PERMITS);
    private final RF2ExportHelper rf2ExportHelper;
    private final PreExportUtility preExportUtility;
    private boolean isDescriptorAssemblagePresent;

    public RF2DirectExporter(ManifoldCoordinate manifold, File exportDirectory, String exportMessage){
        this.manifold = manifold;
        this.exportDirectory = exportDirectory;
        this.exportMessage = exportMessage;
        this.localDateTimeNow = LocalDateTime.now();
        this.rf2ExportHelper = new RF2ExportHelper(this.manifold);
        this.exportConfigurations = new ArrayList<>();
        this.preExportUtility = new PreExportUtility(this.manifold);

        isDescriptorAssemblagePresent = Get.identifierService().hasUuid(UuidT3Generator.fromSNOMED("900000000000456007"));

        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() {

        TaxonomySnapshot noTreeTaxonomy = Get.taxonomyService().getSnapshotNoTree(this.manifold);
        List<Integer> currentAssemblageNids = Arrays.stream(Get.assemblageService().getAssemblageConceptNids()).boxed().collect(Collectors.toList());
        int descriptorAssemblageNid = isDescriptorAssemblagePresent? Get.concept(UuidT3Generator.fromSNOMED("900000000000456007")).getNid() : 0;
        final ArrayList<VersionType> versionTypeIgnoreList = new ArrayList<>();
        versionTypeIgnoreList.add(VersionType.CONCEPT);
        versionTypeIgnoreList.add(VersionType.DESCRIPTION);
        versionTypeIgnoreList.add(VersionType.DYNAMIC);
        versionTypeIgnoreList.add(VersionType.LOGIC_GRAPH);
        versionTypeIgnoreList.add(VersionType.RF2_RELATIONSHIP);

        for(RF2ReleaseType rf2ReleaseType : RF2ReleaseType.values()){

            this.exportConfigurations.add(new RF2Configuration(RF2FileType.CONCEPT, rf2ReleaseType, this.localDateTimeNow, this.exportDirectory, noTreeTaxonomy, this.rf2ExportHelper));
            this.exportConfigurations.add(new RF2Configuration(RF2FileType.DESCRIPTION, rf2ReleaseType, this.localDateTimeNow, this.exportDirectory, noTreeTaxonomy, this.rf2ExportHelper));
            this.exportConfigurations.add(new RF2Configuration(RF2FileType.RELATIONSHIP, rf2ReleaseType, this.localDateTimeNow, this.exportDirectory, noTreeTaxonomy, this.rf2ExportHelper));
            this.exportConfigurations.add(new RF2Configuration(RF2FileType.STATED_RELATIONSHIP, rf2ReleaseType, this.localDateTimeNow, this.exportDirectory, noTreeTaxonomy, this.rf2ExportHelper));
            this.exportConfigurations.add(new RF2Configuration(RF2FileType.IDENTIFIER, rf2ReleaseType, this.localDateTimeNow, this.exportDirectory, noTreeTaxonomy, this.rf2ExportHelper));

            Arrays.stream(
                    noTreeTaxonomy
                            .getTaxonomyChildConceptNids(MetaData.LANGUAGE____SOLOR.getNid()))
                    .filter(langAssemblageNid -> currentAssemblageNids.contains(langAssemblageNid))
                    .forEach(languageNid ->{
                        exportConfigurations.add(
                                new RF2Configuration(RF2FileType.LANGUAGE_REFSET, rf2ReleaseType,
                                        this.localDateTimeNow, languageNid, this.exportDirectory,
                                        this.preExportUtility, this.isDescriptorAssemblagePresent, noTreeTaxonomy, this.rf2ExportHelper));
                    });

            Arrays.stream(
                    Get.assemblageService().getAssemblageConceptNids())
                    .filter(nid -> !versionTypeIgnoreList.contains(Get.assemblageService().getVersionTypeForAssemblage(nid)))
                    .filter(nid -> descriptorAssemblageNid != nid)
                    .forEach(assemblageNid -> {
                        exportConfigurations.add(
                                new RF2Configuration(RF2FileType.REFSET, rf2ReleaseType, this.localDateTimeNow, assemblageNid,
                                        Get.concept(assemblageNid).getFullyQualifiedName(),
                                        Get.assemblageService().getVersionTypeForAssemblage(assemblageNid),
                                        this.exportDirectory, this.preExportUtility, this.isDescriptorAssemblagePresent, noTreeTaxonomy, this.rf2ExportHelper));
                    });
        }

        updateTitle("Export " + this.exportMessage);
        addToTotalWork(exportConfigurations.size() + 4);

        Get.activeTasks().addListener((SetChangeListener<? super Task<?>>) change -> {
            if(change.wasRemoved()) {
                this.completedUnitOfWork();
            }
        });

        try {

            RF2Configuration fullDescriptorAssemblageConfiguration = null;
            RF2Configuration snapshotDescriptorAssemblageConfiguration = null;

            if (isDescriptorAssemblagePresent) {
                fullDescriptorAssemblageConfiguration = new RF2Configuration(RF2FileType.REFSET, RF2ReleaseType.FULL, this.localDateTimeNow,
                        descriptorAssemblageNid, Get.concept(descriptorAssemblageNid).getFullyQualifiedName(),
                        Get.assemblageService().getVersionTypeForAssemblage(descriptorAssemblageNid),
                        this.exportDirectory, this.preExportUtility, this.isDescriptorAssemblagePresent, noTreeTaxonomy, this.rf2ExportHelper);

                snapshotDescriptorAssemblageConfiguration = new RF2Configuration(RF2FileType.REFSET, RF2ReleaseType.SNAPSHOT, this.localDateTimeNow,
                        descriptorAssemblageNid, Get.concept(descriptorAssemblageNid).getFullyQualifiedName(),
                        Get.assemblageService().getVersionTypeForAssemblage(descriptorAssemblageNid),
                        this.exportDirectory, this.preExportUtility, this.isDescriptorAssemblagePresent, noTreeTaxonomy, this.rf2ExportHelper);

            }

            for (RF2Configuration rf2Configuration : this.exportConfigurations) {

                switch (rf2Configuration.getRf2FileType()){
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
                    case LANGUAGE_REFSET:

                        Get.executor().submit(
                                new RF2LanguageRefsetExporter(rf2Configuration, rf2ExportHelper, rf2Configuration.getIntStream(), readSemaphore));
                        break;
                    case REFSET:

                        Get.executor().submit(
                                new RF2RefsetExporter(rf2Configuration, rf2ExportHelper, rf2Configuration.getIntStream(), readSemaphore));
                        break;
                }
                if(isDescriptorAssemblagePresent && rf2Configuration.getRefsetDescriptorDefinitions().size() > 0) {
                    fullDescriptorAssemblageConfiguration.getRefsetDescriptorDefinitions().addAll(rf2Configuration.getRefsetDescriptorDefinitions());
                    snapshotDescriptorAssemblageConfiguration.getRefsetDescriptorDefinitions().addAll(rf2Configuration.getRefsetDescriptorDefinitions());

                }
            }

            if(isDescriptorAssemblagePresent) {
                Get.executor().submit(
                        new RF2RefsetExporter(fullDescriptorAssemblageConfiguration, rf2ExportHelper,
                                fullDescriptorAssemblageConfiguration.getIntStream(), readSemaphore));
                Get.executor().submit(
                        new RF2RefsetExporter(snapshotDescriptorAssemblageConfiguration, rf2ExportHelper,
                                fullDescriptorAssemblageConfiguration.getIntStream(), readSemaphore));
            }

            completedUnitOfWork();

            readSemaphore.acquireUninterruptibly(READ_PERMITS);
            readSemaphore.release(READ_PERMITS);

            Get.executor().submit(new ZipExportDirectory(Paths.get(this.exportConfigurations.get(0).getZipDirectory()), this.readSemaphore, READ_PERMITS, this.exportConfigurations.size()));
            completedUnitOfWork();

            readSemaphore.acquireUninterruptibly(READ_PERMITS);

        }finally {
            Get.activeTasks().remove(this);
            readSemaphore.release(READ_PERMITS);

        }

        return null;
    }
}
