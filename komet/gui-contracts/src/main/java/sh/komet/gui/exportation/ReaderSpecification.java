package sh.komet.gui.exportation;

import sh.isaac.api.chronicle.Chronology;

import java.io.UnsupportedEncodingException;
import java.util.List;

public interface ReaderSpecification {

    List<byte[]> readExportData(Chronology chronology) throws UnsupportedEncodingException;
    String getReaderUIText();
    List<Chronology> createChronologyList();
    String getFileName(String rootDirName);
    void addColumnHeaders(List<byte[]> list) throws UnsupportedEncodingException;
}
