package sh.isaac.solor.rf2;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.solor.ExportComponentType;
import sh.isaac.solor.ExportConfiguration;
import sh.isaac.solor.ZipExportFilesTask;
import sh.komet.gui.manifold.Manifold;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.stream.Stream;

public class RF2DirectExporter extends TimedTaskWithProgressTracker<Void> implements PersistTaskResult {

    private final File exportDirectory;
    private final Manifold manifold;
    private final String exportMessage;
    private static final int READ_PERMITS = Runtime.getRuntime()
            .availableProcessors() * 2;
    private final Semaphore readSemaphore = new Semaphore(READ_PERMITS);
    private final int READ_BATCH_SIZE = 102400;
    private final Map<ExportConfiguration, List<String>> mapOfArtifactsToExport = new HashMap<>();
    private final String zipFileName;
    private final LocalDateTime localDateTimeNow;

    public RF2DirectExporter(Manifold manifold, File exportDirectory, String exportMessage){
        this.manifold = manifold;
        this.exportDirectory = exportDirectory;
        this.exportMessage = exportMessage;
        this.localDateTimeNow = LocalDateTime.now();
        this.zipFileName = "/" + "SnomedCT_SolorRF2_PRODUCTION_"
                + DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").format(this.localDateTimeNow) + ".zip";

        this.readSemaphore.acquireUninterruptibly();
        updateTitle("Export " + this.exportMessage);
        addToTotalWork(5);
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() throws Exception {

        try {

            ExportConfiguration conceptConfig = new ExportConfiguration(
                    "id\teffectiveTime\tactive\tmoduleId\tdefinitionStatusId\r",
                    "SnomedCT_SolorRF2_PRODUCTION_"
                            + DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").format(this.localDateTimeNow)
                            + "/Snapshot/Terminology/sct2_Concept_Snapshot_"
                            + DateTimeFormatter.ofPattern("uuuuMMdd").format(this.localDateTimeNow) + ".txt",
                    ExportComponentType.RF2CONCEPT,
                    "Concept");

            ExportConfiguration descriptionConfig = new ExportConfiguration(
                    "id\teffectiveTime\tactive\tmoduleId\tconceptId\tlanguageCode" +
                            "\ttypeId\tterm\tcaseSignificanceId\r",
                    "SnomedCT_SolorRF2_PRODUCTION_"
                            + DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").format(this.localDateTimeNow)
                            + "/Snapshot/Terminology/sct2_Description_Snapshot_"
                            + DateTimeFormatter.ofPattern("uuuuMMdd").format(this.localDateTimeNow) + ".txt",
                    ExportComponentType.RF2DESCRIPTION,
                    "Description");

            ExportConfiguration relationshipConfig = new ExportConfiguration(
                    "id\teffectiveTime\tactive\tmoduleId\tsourceId\tdestinationId" +
                            "\trelationshipGroup\ttypeId\tcharacteristicTypeId\tmodifierId\r",
                    "SnomedCT_SolorRF2_PRODUCTION_"
                            + DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").format(this.localDateTimeNow)
                            + "/Snapshot/Terminology/sct2_Relationship_Snapshot_"
                            + DateTimeFormatter.ofPattern("uuuuMMdd").format(this.localDateTimeNow) + ".txt",
                    ExportComponentType.RF2RELATIONSHIP,
                    "Relationship");

            completedUnitOfWork();

            batchStreamAndRunReaders(Get.conceptService().getConceptChronologyStream(), conceptConfig);

            completedUnitOfWork();

            batchStreamAndRunReaders(Get.conceptService().getConceptChronologyStream()
                            .flatMap(conceptChronology -> conceptChronology.getConceptDescriptionList().stream()),
                    descriptionConfig);

            completedUnitOfWork();

            batchStreamAndRunReaders(Get.conceptService().getConceptChronologyStream()
                            .flatMap(conceptChronology -> conceptChronology.getSemanticChronologyList().stream())
                            .filter(semanticChronology -> semanticChronology.getVersionType() == VersionType.LOGIC_GRAPH),
                    relationshipConfig);

            completedUnitOfWork();

            for(int assemblageNID : Get.assemblageService().getAssemblageConceptNids()){

                IsaacObjectType isaacObjectTypeForAssemblage = Get.assemblageService().getObjectTypeForAssemblage(assemblageNID);

                if(isaacObjectTypeForAssemblage == IsaacObjectType.SEMANTIC) {

                    ExportConfiguration semanticAssemblageConfig = generateAssemblageExportConfiguration(
                            Get.assemblageService().getSemanticChronologyStream(assemblageNID)
                                    .findFirst().get().getVersionType(),
                            Get.concept(assemblageNID).getFullyQualifiedName());

                    batchStreamAndRunReaders(Get.assemblageService().getSemanticChronologyStream(assemblageNID),
                            semanticAssemblageConfig);
                } else if(isaacObjectTypeForAssemblage == IsaacObjectType.CONCEPT){
//                    ExportConfiguration conceptAssemblageConfig = generateAssemblageExportConfiguration(
//                            Get.assemblageService().getChronologyStream(assemblageNID)
//                            .findFirst().get().getVersionType(),
//                            Get.concept(assemblageNID).getFullyQualifiedName());
//                    batchStreamAndRunReaders(Get.assemblageService().getChronologyStream(assemblageNID),
//                            conceptAssemblageConfig);
                }
            }

            runZipTask();

            completedUnitOfWork();
        }finally {
            this.readSemaphore.release();
            Get.activeTasks().remove(this);
        }


        return null;
    }

    private void batchStreamAndRunReaders(Stream<? extends Chronology> streamToBatch, ExportConfiguration exportConfiguration){
        final ArrayList<Future<List<String>>> futures = new ArrayList<>();
        final ArrayList<Chronology> batch = new ArrayList<>(this.READ_BATCH_SIZE);

        streamToBatch
                .forEach(chronology -> {
                    batch.add(chronology);
                    if(batch.size() % this.READ_BATCH_SIZE == 0){
                        futures.add(runBatchReader(new ArrayList<>(batch), exportConfiguration));
                        batch.clear();
                    }
                });

        if(!batch.isEmpty()){
            futures.add(runBatchReader(batch, exportConfiguration));
        }

        this.readSemaphore.acquireUninterruptibly(READ_PERMITS - 1);

        final ArrayList<String> readerResults = new ArrayList<>();
        for(Future<List<String>> future : futures){
            try {
                readerResults.addAll(future.get());
            }catch (InterruptedException | ExecutionException ieE){
                ieE.printStackTrace();
            }
        }

        exportConfiguration.addHeaderToExport(readerResults);
        this.mapOfArtifactsToExport.put(exportConfiguration, readerResults);

        this.readSemaphore.release(READ_PERMITS - 1); //this task takes up a thread
    }

    private Future<List<String>> runBatchReader(List<Chronology> batch, ExportConfiguration exportConfiguration){
        switch (exportConfiguration.getExportComponentType()){
            case RF2CONCEPT:
                return Get.executor().submit(new RF2ExportConceptReader(batch, this.readSemaphore, this.manifold, exportConfiguration), new ArrayList<>());
            case RF2DESCRIPTION:
                return Get.executor().submit(new RF2ExportDescriptionReader(batch, this.readSemaphore, this.manifold, exportConfiguration), new ArrayList<>());
            case RF2RELATIONSHIP:
                return Get.executor().submit(new RF2ExportRelationshipReader(batch, this.readSemaphore, this.manifold, exportConfiguration), new ArrayList<>());
            case RF2Refset:
                return Get.executor().submit(new RF2ExportRefsetReader(batch, this.readSemaphore, this.manifold, exportConfiguration), new ArrayList<>());
            default:
                return null;
        }
    }

    private void runZipTask() throws InterruptedException, ExecutionException{
        updateMessage("Zipping SOLOR" + this.exportMessage + " Export...");
        ZipExportFilesTask zipExportFilesTask =
                new ZipExportFilesTask(this.exportDirectory, this.mapOfArtifactsToExport, this.readSemaphore, this.zipFileName);
        Get.executor().submit(zipExportFilesTask).get();
    }

    private ExportConfiguration generateAssemblageExportConfiguration(VersionType versionType, String assemblageFQN){

        String fileHeader = "id\teffectiveTime\tactive\tmoduleId\trefsetId\treferencedComponentId\t\r";
        String baseRefsetFilePath = "SnomedCT_SolorRF2_PRODUCTION_"
                + DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").format(this.localDateTimeNow)
                + "/Snapshot/Refset/der2_%1$sRefset_%2$sSnapshot_"
                + DateTimeFormatter.ofPattern("uuuuMMdd").format(this.localDateTimeNow) + ".txt";
        String completeRefsetFilePath = "";

        switch (versionType){
            case COMPONENT_NID:
                completeRefsetFilePath = String.format(baseRefsetFilePath, "c", assemblageFQN.replaceAll(" ", ""));
                break;
            case RF2_RELATIONSHIP:
                completeRefsetFilePath = String.format(baseRefsetFilePath, "ciccc", assemblageFQN.replaceAll(" ", ""));
                break;
            case LOINC_RECORD:
                completeRefsetFilePath = String.format(baseRefsetFilePath, "ssssssssss", assemblageFQN.replaceAll(" ", ""));
                break;
            case LOGIC_GRAPH:
                completeRefsetFilePath = String.format(baseRefsetFilePath, "s", assemblageFQN.replaceAll(" ", ""));
                break;
            case DESCRIPTION:
                completeRefsetFilePath = String.format(baseRefsetFilePath, "s", assemblageFQN.replaceAll(" ", ""));
                break;
            case STRING:
                completeRefsetFilePath = String.format(baseRefsetFilePath, "s", assemblageFQN.replaceAll(" ", ""));
                break;
            case LONG:
                completeRefsetFilePath = String.format(baseRefsetFilePath, "s", assemblageFQN.replaceAll(" ", ""));
                break;
            case Str1_Str2_Str3_Str4_Str5_Str6_Str7:
                completeRefsetFilePath = String.format(baseRefsetFilePath, "sssssss", assemblageFQN.replaceAll(" ", ""));
                break;
            case Int1_Int2_Str3_Str4_Str5_Nid6_Nid7:
                completeRefsetFilePath = String.format(baseRefsetFilePath, "iissscc", assemblageFQN.replaceAll(" ", ""));
                break;
            case Nid1_Int2_Str3_Str4_Nid5_Nid6:
                completeRefsetFilePath = String.format(baseRefsetFilePath, "sssssss", assemblageFQN.replaceAll(" ", ""));
                break;
            case Str1_Str2_Nid3_Nid4_Nid5:
                completeRefsetFilePath = String.format(baseRefsetFilePath, "ssccc", assemblageFQN.replaceAll(" ", ""));
                break;
            case Str1_Str2_Nid3_Nid4:
                completeRefsetFilePath = String.format(baseRefsetFilePath, "sscc", assemblageFQN.replaceAll(" ", ""));
                break;
            case Str1_Nid2_Nid3_Nid4:
                completeRefsetFilePath = String.format(baseRefsetFilePath, "scccc", assemblageFQN.replaceAll(" ", ""));
                break;
            case Nid1_Nid2_Int3:
                completeRefsetFilePath = String.format(baseRefsetFilePath, "cci", assemblageFQN.replaceAll(" ", ""));
                break;
            case Nid1_Nid2_Str3:
                completeRefsetFilePath = String.format(baseRefsetFilePath, "ccs", assemblageFQN.replaceAll(" ", ""));
                break;
            case Str1_Str2:
                completeRefsetFilePath = String.format(baseRefsetFilePath, "ss", assemblageFQN.replaceAll(" ", ""));
                break;
            case Nid1_Str2:
                completeRefsetFilePath = String.format(baseRefsetFilePath, "cs", assemblageFQN.replaceAll(" ", ""));
                break;
            case Nid1_Nid2:
                completeRefsetFilePath = String.format(baseRefsetFilePath, "cc", assemblageFQN.replaceAll(" ", ""));
                break;
            case Nid1_Int2:
                completeRefsetFilePath = String.format(baseRefsetFilePath, "ci", assemblageFQN.replaceAll(" ", ""));
                break;
            case CONCEPT:
            case MEMBER:
                completeRefsetFilePath = String.format(baseRefsetFilePath, "", assemblageFQN.replaceAll(" ", ""));
                break;
            case UNKNOWN:
                completeRefsetFilePath = String.format(baseRefsetFilePath, "unk", assemblageFQN.replaceAll(" ", ""));
                break;
            case DYNAMIC:
                completeRefsetFilePath = String.format(baseRefsetFilePath, "dyn", assemblageFQN.replaceAll(" ", ""));
                break;
            case MEASURE_CONSTRAINTS:
                completeRefsetFilePath = String.format(baseRefsetFilePath, "mes", assemblageFQN.replaceAll(" ", ""));
                break;
            default:
        }

        return new ExportConfiguration(fileHeader,completeRefsetFilePath,ExportComponentType.RF2Refset, assemblageFQN);

    }
}
