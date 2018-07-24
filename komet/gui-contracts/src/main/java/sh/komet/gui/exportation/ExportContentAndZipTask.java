package sh.komet.gui.exportation;

import sh.isaac.api.Get;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.komet.gui.manifold.Manifold;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        addToTotalWork(6);
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() throws Exception {

        try {

            LocalDateTime totalStart = LocalDateTime.now();

            completedUnitOfWork();

            Map<ReaderSpecification, List<String>> exportObjectsToZip = new HashMap<>();
            final ExportLookUpCache exportLookUpCache = new ExportLookUpCache();

            ReaderSpecification conceptReaderSpec = new RF2ConceptSpec(this.manifold, exportLookUpCache);
            ReaderSpecification descriptionReaderSpec = new RF2DescriptionSpec(this.manifold, exportLookUpCache);
            ReaderSpecification relationshipReaderSpec = new RF2RelationshipSpec(this.manifold, exportLookUpCache);

            completedUnitOfWork();

            if (true) { //TODO Remove this when done debugging

                LocalDateTime conceptStart = LocalDateTime.now();

                updateMessage("Reading Concepts...");

                List<String> conceptList = Get.executor().submit(
                        new BatchReader(conceptReaderSpec, 102400), new ArrayList<String>()).get();

                conceptReaderSpec.addColumnHeaders(conceptList);
                exportObjectsToZip.put(conceptReaderSpec,conceptList );

                System.out.println("~~~Total Concept Reading Time: " + Duration.between(conceptStart, LocalDateTime.now()));
            }

            completedUnitOfWork();

            if (true) { //TODO Remove this when done debugging

                LocalDateTime descriptionStart = LocalDateTime.now();

                updateMessage("Reading Descriptions...");

                List<String> descriptionList = Get.executor().submit(
                        new BatchReader(descriptionReaderSpec, 102400), new ArrayList<String>()).get();

                descriptionReaderSpec.addColumnHeaders(descriptionList);
                exportObjectsToZip.put(descriptionReaderSpec, descriptionList);

                System.out.println("~~~Total Description Reading Time: " + Duration.between(descriptionStart, LocalDateTime.now()));
            }

            completedUnitOfWork();

            if(true){ //TODO Remove this when done debugging

                LocalDateTime relationshipStart = LocalDateTime.now();

                updateMessage("Reading Relationships...");

                List<String> relationshipList = Get.executor().submit(
                        new BatchReader(relationshipReaderSpec, 102400), new ArrayList<String>()).get();

                relationshipReaderSpec.addColumnHeaders(relationshipList);
                exportObjectsToZip.put(relationshipReaderSpec, relationshipList);

                System.out.println("~~~Total Relationship Reading Time: " + Duration.between(relationshipStart, LocalDateTime.now()));
            }

            completedUnitOfWork();


            if(true){ //TODO Remove this when done debugging

                LocalDateTime zipStart = LocalDateTime.now();

                updateMessage("Zipping SOLOR Export...");

                ZipExportFiles zipExportFiles =
                        new ZipExportFiles(this.exportFormatType, this.exportDirectory, exportObjectsToZip);
                Get.executor().submit(zipExportFiles).get();

                System.out.println("~~~Total Zipping Time: " + Duration.between(zipStart, LocalDateTime.now()));
            }

            completedUnitOfWork();

            System.out.println("~~~Total Time: " + Duration.between(totalStart, LocalDateTime.now()));


        }finally {
            Get.activeTasks().remove(this);
        }

        return null;
    }

}
