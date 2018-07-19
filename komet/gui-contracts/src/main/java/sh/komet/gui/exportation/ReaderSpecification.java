package sh.komet.gui.exportation;

import sh.isaac.api.chronicle.Chronology;

import java.io.UnsupportedEncodingException;
import java.util.List;

public interface ReaderSpecification {

    List<String> readExportData(Chronology chronology);
    String getReaderUIText();
    List<Chronology> createChronologyList();
    String getFileName(String rootDirName);
    void addColumnHeaders(List<String> list);
}
