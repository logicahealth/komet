package sh.komet.gui.exportation;

import sh.isaac.api.Get;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.komet.gui.manifold.Manifold;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
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
    private final int BATCH_SIZE = 102400;

    private final boolean EXPORT_CONCEPTS_FEATURE_ENABLED = true;
    private final boolean EXPORT_DESCRIPTIONS_FEATURE_ENABLED = false;
    private final boolean EXPORT_RELATIONSHIPS_ENABLED = false;
    private final boolean EXPORT_ZIP_ENABLED = true;



    public ExportContentAndZipTask(Manifold manifold, File exportDirectory, ExportFormatType exportFormatType){
        this.manifold = manifold;
        this.exportDirectory = exportDirectory;
        this.exportFormatType = exportFormatType;
        updateTitle("Export " + this.exportFormatType.toString());

        this.readSemaphore.acquireUninterruptibly();
        addToTotalWork(6);
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() throws Exception {

        try {

            LocalDateTime totalStart = LocalDateTime.now();

            completedUnitOfWork();

            Map<ReaderSpecification, List<String>> exportMap = new HashMap<>();
            final ExportLookUpCache exportLookUpCache = new ExportLookUpCache();

            ReaderSpecification conceptReaderSpec = new RF2ConceptSpec(this.manifold, exportLookUpCache);
            ReaderSpecification descriptionReaderSpec = new RF2DescriptionSpec(this.manifold, exportLookUpCache);
            ReaderSpecification relationshipReaderSpec = new RF2RelationshipSpec(this.manifold, exportLookUpCache);

            completedUnitOfWork();

            if (EXPORT_CONCEPTS_FEATURE_ENABLED) { //TODO Remove this when done debugging

                LocalDateTime conceptStart = LocalDateTime.now();

                updateMessage("Reading Concepts...");

                List<String> conceptList = Get.executor().submit(
                        new BatchReader(conceptReaderSpec, BATCH_SIZE, readSemaphore), new ArrayList<String>()).get();

                conceptReaderSpec.addColumnHeaders(conceptList);
                exportMap.put(conceptReaderSpec,conceptList );

                System.out.println("~~~Total Concept Reading Time: " + Duration.between(conceptStart, LocalDateTime.now()));
            }

            completedUnitOfWork();

            if (EXPORT_DESCRIPTIONS_FEATURE_ENABLED) { //TODO Remove this when done debugging

                LocalDateTime descriptionStart = LocalDateTime.now();

                updateMessage("Reading Descriptions...");

                List<String> descriptionList = Get.executor().submit(
                        new BatchReader(descriptionReaderSpec, BATCH_SIZE, readSemaphore), new ArrayList<String>()).get();

                descriptionReaderSpec.addColumnHeaders(descriptionList);
                exportMap.put(descriptionReaderSpec, descriptionList);

                System.out.println("~~~Total Description Reading Time: " + Duration.between(descriptionStart, LocalDateTime.now()));
            }

            completedUnitOfWork();

            if(EXPORT_RELATIONSHIPS_ENABLED){ //TODO Remove this when done debugging

                LocalDateTime relationshipStart = LocalDateTime.now();

                updateMessage("Reading Inferred & SNOMED Relationships...");

                List<String> relationshipList = Get.executor().submit(
                        new BatchReader(relationshipReaderSpec, BATCH_SIZE, readSemaphore), new ArrayList<String>()).get();

                relationshipReaderSpec.addColumnHeaders(relationshipList);
                exportMap.put(relationshipReaderSpec, relationshipList);

                System.out.println("~~~Total Relationship Reading Time: " + Duration.between(relationshipStart, LocalDateTime.now()));
            }

            completedUnitOfWork();


            if(EXPORT_ZIP_ENABLED){ //TODO Remove this when done debugging

                LocalDateTime zipStart = LocalDateTime.now();

                updateMessage("Zipping SOLOR Export...");

                ZipExportFiles zipExportFiles =
                        new ZipExportFiles(this.exportFormatType, this.exportDirectory, exportMap, readSemaphore);
                Get.executor().submit(zipExportFiles).get();

                System.out.println("~~~Total Zipping Time: " + Duration.between(zipStart, LocalDateTime.now()));
            }

            completedUnitOfWork();

            System.out.println("~~~Total Time: " + Duration.between(totalStart, LocalDateTime.now()));


        }finally {
            this.readSemaphore.release();
            Get.activeTasks().remove(this);

        }

        return null;
    }

}
