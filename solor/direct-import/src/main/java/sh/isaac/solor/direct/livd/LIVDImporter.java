package sh.isaac.solor.direct.livd;

import com.monitorjbl.xlsx.StreamingReader;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.solor.direct.livd.writer.LIVDAssemblageWriter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * 2019-05-16
 * aks8m - https://github.com/aks8m
 */
public class LIVDImporter {

    private final Semaphore writeSemaphore;
    private final int WRITE_PERMITS;

    public LIVDImporter(Semaphore writeSemaphore, int WRITE_PERMITS) {
        this.writeSemaphore = writeSemaphore;
        this.WRITE_PERMITS = WRITE_PERMITS;
    }

    public void runImport(InputStream inputStream){

        List<String[]> valuesToWrite = new ArrayList<>();
        Workbook workbook = StreamingReader.builder()
                .rowCacheSize(100)
                .bufferSize(4096)
                .open(inputStream);

        int rowSkip = 1;
        final int startReadingRowNumber = 6;

        for(Row row : workbook.getSheetAt(workbook.getNumberOfSheets() - 1)){

            if(rowSkip >= startReadingRowNumber){
                String[] rowValues = new String[19];

                for(int i = 0; i < 19; i++){
                    rowValues[i] = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
                }

                valuesToWrite.add(rowValues);
            }
            rowSkip++;
        }

        Get.executor().submit(new LIVDAssemblageWriter(valuesToWrite, this.writeSemaphore));

        this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
        for (IndexBuilderService indexer : LookupService.get().getAllServices(IndexBuilderService.class)) {
            try {
                indexer.sync().get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Get.assemblageService().sync();
        this.writeSemaphore.release(WRITE_PERMITS);
    }
}
