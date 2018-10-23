package sh.isaac.solor;

import java.util.List;

public class ExportConfiguration {

    private String fileHeader;
    private String filePath;
    private ExportComponentType exportComponentType;
    private String message;


    public ExportConfiguration(String fileHeader, String filePath, ExportComponentType exportComponentType, String message) {
        this.fileHeader = fileHeader;
        this.filePath = filePath;
        this.exportComponentType = exportComponentType;
        this.message = message;
    }

    public void addHeaderToExport(List<String> listToExport){
        listToExport.add(0, this.fileHeader);
    }

    public void setFileHeader(String fileHeader) {
        this.fileHeader = fileHeader;
    }

    public String getFileHeader() {
        return fileHeader;
    }

    public String getFilePath() {
        return filePath;
    }

    public ExportComponentType getExportComponentType() {
        return exportComponentType;
    }

    public String getMessage() {
        return message;
    }
}
