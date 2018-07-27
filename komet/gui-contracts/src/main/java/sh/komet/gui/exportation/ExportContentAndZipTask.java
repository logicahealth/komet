package sh.komet.gui.exportation;

import sh.isaac.api.Get;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.komet.gui.manifold.Manifold;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

/*
 * aks8m - 5/15/18
 */
public class ExportContentAndZipTask extends TimedTaskWithProgressTracker<Void> implements PersistTaskResult {

    private final File exportDirectory;
    private final ExportFormatType exportFormatType;
    private final Manifold manifold;
    private static final int WRITE_PERMITS = Runtime.getRuntime()
            .availableProcessors() * 2;
    private final Semaphore readSemaphore = new Semaphore(WRITE_PERMITS);
    private final int BATCH_SIZE = 10240;
    private final Map<ReaderSpecification, List<String>> exportMap = new HashMap<>();

    private final ExportComponentType[] exportComponentTypes = new ExportComponentType[]{
            ExportComponentType.CONCEPT,
            ExportComponentType.DESCRIPTION,
            ExportComponentType.RELATIONSHIP};



    public ExportContentAndZipTask(Manifold manifold, File exportDirectory, ExportFormatType exportFormatType){
        this.manifold = manifold;
        this.exportDirectory = exportDirectory;
        this.exportFormatType = exportFormatType;
        updateTitle("Export " + this.exportFormatType.toString());

        this.readSemaphore.acquireUninterruptibly();
        addToTotalWork(exportComponentTypes.length + 3);
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() throws Exception {

        try {

            completedUnitOfWork();

            LocalDateTime totalStart = LocalDateTime.now();

            for(ExportComponentType exportComponentType : exportComponentTypes) {

                runBatchReader(exportComponentType);
                completedUnitOfWork();
            }

            completedUnitOfWork();

            runZipTask();

            completedUnitOfWork();

            System.out.println("¯\\_(ツ)_/¯ : Total Time: " + Duration.between(totalStart, LocalDateTime.now()));

        }finally {
            this.readSemaphore.release();
            Get.activeTasks().remove(this);

        }

        return null;
    }

    private void runBatchReader(ExportComponentType exportComponentType) throws InterruptedException, ExecutionException {

        ReaderSpecification readerSpecification;
        LocalDateTime runStart = LocalDateTime.now();

        switch (exportComponentType){
            case CONCEPT:
                readerSpecification = new RF2ConceptSpec(this.manifold);
                break;
            case DESCRIPTION:
                readerSpecification = new RF2DescriptionSpec(this.manifold);
                break;
            case RELATIONSHIP:
                readerSpecification = new RF2RelationshipSpec(this.manifold);
                break;
            default:
                readerSpecification = null;
        }

        updateMessage("Reading"+ readerSpecification.getReaderUIText() + "...");

        List<String> conceptList = Get.executor().submit(
                new BatchReader(readerSpecification, BATCH_SIZE, readSemaphore), new ArrayList<String>()).get();

        readerSpecification.addColumnHeaders(conceptList);
        exportMap.put(readerSpecification,conceptList );

        System.out.println("¯\\_(ツ)_/¯ : Total " + readerSpecification.getReaderUIText() + "Reading Time: " + Duration.between(runStart, LocalDateTime.now()));
    }

    private void runZipTask() throws InterruptedException, ExecutionException{

        LocalDateTime zipStart = LocalDateTime.now();

        updateMessage("Zipping SOLOR Export...");

        ZipExportFiles zipExportFiles =
                new ZipExportFiles(this.exportFormatType, this.exportDirectory, this.exportMap, this.readSemaphore);
        Get.executor().submit(zipExportFiles).get();

        System.out.println("¯\\_(ツ)_/¯ : Total Zipping Time: " + Duration.between(zipStart, LocalDateTime.now()));

    }

}
