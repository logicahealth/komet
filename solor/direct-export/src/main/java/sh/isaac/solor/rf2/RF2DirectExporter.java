package sh.isaac.solor.rf2;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.solor.DirectExporter;
import sh.isaac.solor.ExportComponentType;
import sh.isaac.solor.ZipExportFilesTask;
import sh.komet.gui.manifold.Manifold;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.stream.Stream;

public class RF2DirectExporter extends TimedTaskWithProgressTracker<Void> implements PersistTaskResult, DirectExporter {

    private final File exportDirectory;
    private final Manifold manifold;
    private final String exportMessage;
    private static final int READ_PERMITS = Runtime.getRuntime()
            .availableProcessors() * 2;
    private final Semaphore readSemaphore = new Semaphore(READ_PERMITS);
    private final int READ_BATCH_SIZE = 10240;
    private final Map<ExportComponentType, List<String>> mapOfArtifactsToExport = new HashMap<>();
    private final String zipFileName;

    public RF2DirectExporter(Manifold manifold, File exportDirectory, String exportMessage){
        this.manifold = manifold;
        this.exportDirectory = exportDirectory;
        this.exportMessage = exportMessage;
        this.zipFileName = "/" + "SnomedCT_SolorRF2_PRODUCTION_"
                + DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").format(LocalDateTime.now()) + ".zip";

        this.readSemaphore.acquireUninterruptibly();
        updateTitle("Export " + this.exportMessage);
        addToTotalWork(5);
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() throws Exception {

        try {
            completedUnitOfWork();

            batchStreamAndRun(Get.conceptService().getConceptChronologyStream(),
                    ExportComponentType.RF2CONCEPT);

            completedUnitOfWork();

            batchStreamAndRun(Get.conceptService().getConceptChronologyStream()
                            .flatMap(conceptChronology -> conceptChronology.getConceptDescriptionList().stream()),
                    ExportComponentType.RF2DESCRIPTION);

            completedUnitOfWork();

            batchStreamAndRun(Get.conceptService().getConceptChronologyStream()
                            .flatMap(conceptChronology -> conceptChronology.getSemanticChronologyList().stream())
                            .filter(semanticChronology -> semanticChronology.getVersionType() == VersionType.LOGIC_GRAPH),
                    ExportComponentType.RF2RELATIONSHIP);

            completedUnitOfWork();

            runZipTask();

            completedUnitOfWork();
        }finally {
            this.readSemaphore.release();
            Get.activeTasks().remove(this);
        }


        return null;
    }

    private void batchStreamAndRun(Stream<? extends Chronology> streamToBatch, ExportComponentType exportComponentType){
        final ArrayList<Future<List<String>>> futures = new ArrayList<>();
        final ArrayList<Chronology> batch = new ArrayList<>(this.READ_BATCH_SIZE);

        streamToBatch
                .forEach(chronology -> {
                    batch.add(chronology);
                    if(batch.size() % this.READ_BATCH_SIZE == 0){
                        futures.add(runBatchReader(new ArrayList<>(batch), exportComponentType));
                        batch.clear();
                    }
                });

        if(!batch.isEmpty()){
            futures.add(runBatchReader(batch, exportComponentType));
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

        switch (exportComponentType){
            case RF2CONCEPT:
                readerResults.add(0, "id\teffectiveTime\tactive\tmoduleId\tdefinitionStatusId\r");
                break;
            case RF2DESCRIPTION:
                readerResults.add(0,
                        "id\teffectiveTime\tactive\tmoduleId\tconceptId\tlanguageCode\ttypeId\tterm\tcaseSignificanceId\r");
                break;
            case RF2RELATIONSHIP:
                readerResults.add(0, ("id\teffectiveTime\tactive\tmoduleId\tsourceId\tdestinationId" +
                        "\trelationshipGroup\ttypeId\tcharacteristicTypeId\tmodifierId\r"));
                break;
        }
        this.mapOfArtifactsToExport.put(exportComponentType, readerResults);

        this.readSemaphore.release(READ_PERMITS - 1); //this task takes up a thread
    }

    private Future<List<String>> runBatchReader(List<Chronology> batch, ExportComponentType exportComponentType ){
        switch (exportComponentType){
            case RF2CONCEPT:
                return Get.executor().submit(new RF2ExportConceptReader(batch, this.readSemaphore, this.manifold), new ArrayList<>());
            case RF2DESCRIPTION:
                return Get.executor().submit(new RF2ExportDescriptionReader(batch, this.readSemaphore, this.manifold), new ArrayList<>());
            case RF2RELATIONSHIP:
                return Get.executor().submit(new RF2ExportRelationshipReader(batch, this.readSemaphore, this.manifold), new ArrayList<>());
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
}
