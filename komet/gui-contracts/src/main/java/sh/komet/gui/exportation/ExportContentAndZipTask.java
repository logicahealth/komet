package sh.komet.gui.exportation;

import sh.isaac.api.Get;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.komet.gui.manifold.Manifold;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/*
 * aks8m - 5/15/18
 */
public class ExportContentAndZipTask extends TimedTaskWithProgressTracker<Void> implements PersistTaskResult {

    private final File exportDirectory;
    private final ExportFormatType exportFormatType;
    private final Manifold manifold;

    public ExportContentAndZipTask(Manifold manifold, File exportDirectory, ExportFormatType exportFormatType){
        this.manifold = manifold;
        this.exportDirectory = exportDirectory;
        this.exportFormatType = exportFormatType;
        updateTitle("Export " + this.exportFormatType.toString());

        addToTotalWork(4);
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() throws Exception {

        try {
            Map<ReaderSpecification, List<String>> linesToExportMap = new HashMap<>();

            completedUnitOfWork();

            final ExportLookUpCache exportLookUpCache = new ExportLookUpCache();
            exportLookUpCache.generateCache();

            completedUnitOfWork();

            if (false) { //TODO Remove this when done debugging
                System.out.println("Start Concept================" + LocalDateTime.now());
                //Concepts
                completedUnitOfWork();
                updateMessage("Reading Concepts...");

                ReaderSpecification conceptReaderSpec = new RF2ConceptSpec(this.manifold, exportLookUpCache);
                BatchReader conceptBatchReader = new BatchReader(conceptReaderSpec, 102400);

                Future<List<String>> conceptReadTask = Get.executor().submit(conceptBatchReader, new ArrayList<>());
                linesToExportMap.put(conceptReaderSpec, conceptReadTask.get());
                completedUnitOfWork();
                System.out.println("Finish Concept================" + LocalDateTime.now());
            }

            completedUnitOfWork();

            if (false) { //TODO Remove this when done debugging
                System.out.println("Start Descriptions================" + LocalDateTime.now());
                //Descriptions
                completedUnitOfWork();
                updateMessage("Reading Descriptions...");

                ReaderSpecification descriptionReaderSpec = new RF2DescriptionSpec(this.manifold, exportLookUpCache);
                BatchReader descriptionBatchReader = new BatchReader(descriptionReaderSpec, 102400);

                Future<List<String>> descriptionReadTask = Get.executor().submit(descriptionBatchReader, new ArrayList<>());
                linesToExportMap.put(descriptionReaderSpec, descriptionReadTask.get());
                System.out.println("Finish Descriptions================" + LocalDateTime.now());
            }

            if(true){
                System.out.println("Start Descriptions================" + LocalDateTime.now());
                //Relationship
                completedUnitOfWork();
                updateMessage("Reading Relationships...");

                ReaderSpecification relationshipReaderSpec = new RF2RelationshipSpec(this.manifold, exportLookUpCache);
                BatchReader relationshipBatchReader = new BatchReader(relationshipReaderSpec, 102400);

                Future<List<String>> relationshipReadTask = Get.executor().submit(relationshipBatchReader, new ArrayList<>());
                linesToExportMap.put(relationshipReaderSpec, relationshipReadTask.get());
                System.out.println("Finish Descriptions================" + LocalDateTime.now());
            }

            if(false){
                System.out.println("Start ZippingExport================" + LocalDateTime.now());
                //Zipping
                completedUnitOfWork();
                updateMessage("Zipping SOLOR Export...");

                ZipExportFiles zipExportFiles = new ZipExportFiles(this.exportFormatType, this.exportDirectory, linesToExportMap);
                Future zipExportFilesTask = Get.executor().submit(zipExportFiles);
                zipExportFilesTask.get();
                System.out.println("Finish ZippingExport================" + LocalDateTime.now());
            }



            completedUnitOfWork();

        }finally {
            Get.activeTasks().remove(this);
        }

        return null;
    }

}
