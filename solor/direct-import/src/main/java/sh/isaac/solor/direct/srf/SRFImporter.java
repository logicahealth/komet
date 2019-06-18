package sh.isaac.solor.direct.srf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.solor.direct.ImportSpecification;
import sh.isaac.solor.direct.ImportStreamType;
import sh.isaac.solor.direct.ImportType;
import sh.isaac.solor.direct.srf.writers.SRFBrittleAssemblageWriter;
import sh.isaac.solor.direct.srf.writers.SRFConceptWriter;
import sh.isaac.solor.direct.srf.writers.SRFDescriptionWriter;
import sh.isaac.solor.direct.srf.writers.SRFRelationshipWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 * 2019-04-23
 * aks8m - https://github.com/aks8m
 */
public class SRFImporter {

    private static final Logger LOG = LogManager.getLogger();

    public static void RunImport(BufferedReader bufferedReader, ImportSpecification importSpecification, Semaphore writeSemaphore, int WRITE_PERMITS, ImportType importType){

        final int writeSize = 102400;
        ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
        String rowString;
        boolean empty = true;

        try {
            bufferedReader.readLine();  // discard header row

            while ((rowString = bufferedReader.readLine()) != null) {
                empty = false;
                String[] columns = rowString.split("\t");

                columnsToWrite.add(columns);

                if (columnsToWrite.size() == writeSize) {

                    columnsToWrite = new ArrayList<>(writeSize);
                    Get.executor().submit(createWriter(columnsToWrite, importSpecification, writeSemaphore, importType));
                }
            }
        }catch (IOException e){
            LOG.error(e.getMessage());
        }

        if (empty) {
            LOG.warn("No data in file: " + importSpecification.getContentProvider().getStreamSourceName());
        }
        if (!columnsToWrite.isEmpty()) {

            Get.executor().submit(createWriter(columnsToWrite, importSpecification, writeSemaphore, importType));
        }

        writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);

        if(importSpecification.getStreamType() == ImportStreamType.SRF_CONCEPT){

            for (IndexBuilderService indexer : LookupService.get().getAllServices(IndexBuilderService.class)) {
                try {
                    indexer.sync().get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Get.conceptService().sync();
            Get.assemblageService().sync();

        } else {

            for (IndexBuilderService indexer : LookupService.get().getAllServices(IndexBuilderService.class)) {
                try {
                    indexer.sync().get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Get.assemblageService().sync();

        }
        writeSemaphore.release(WRITE_PERMITS);

    }

    private static TimedTaskWithProgressTracker<Void> createWriter(
            ArrayList<String[]> linesToWrite, ImportSpecification importSpecification, Semaphore writeSemaphore, ImportType importType){

        String sourcePath = trimZipName(importSpecification.getContentProvider().getStreamSourceName());

        if(importSpecification.getStreamType() == ImportStreamType.SRF_CONCEPT){

            return new SRFConceptWriter(linesToWrite, writeSemaphore, "Processing SRF Concepts from "
                    + sourcePath , importType);

        } else if(importSpecification.getStreamType() == ImportStreamType.SRF_DESCRIPTION){

            return new SRFDescriptionWriter(linesToWrite, writeSemaphore, "Processing SRF Descriptions from "
                    + sourcePath, importType);

        } else if(importSpecification.getStreamType() == ImportStreamType.SRF_INFERRED_RELATIONSHIP
                || importSpecification.getStreamType() == ImportStreamType.SRF_STATED_RELATIONSHIP){

            return new SRFRelationshipWriter(linesToWrite, writeSemaphore, "Processing SRF Relationships from "
                    + sourcePath, importType, importSpecification.getStreamType());

        } else{

            return new SRFBrittleAssemblageWriter(linesToWrite, writeSemaphore, importSpecification.getStreamType(),
                    "Processing SRF Assemblages from " + sourcePath, importType);

        }
    }

    private static String trimZipName(String zipName) {
        int index = zipName.lastIndexOf("/");
        return zipName.substring(index + 1);
    }
}
