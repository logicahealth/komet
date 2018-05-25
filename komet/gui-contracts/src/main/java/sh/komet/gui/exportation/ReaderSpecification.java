package sh.komet.gui.exportation;

import sh.isaac.api.chronicle.Chronology;

import java.util.List;

public interface ReaderSpecification {

    String createExportString(Chronology chronology);
    void addColumnHeaders(List<String> stringList);
    String getReaderUIText();
    List<Chronology> createChronologyList();
    String getFileName(String rootDirName);
}
