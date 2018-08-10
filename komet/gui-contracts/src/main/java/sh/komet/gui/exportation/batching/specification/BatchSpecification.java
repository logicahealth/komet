package sh.komet.gui.exportation.batching.specification;

import java.util.List;

public interface BatchSpecification<T, U> {

    List<U> performProcessOnItem(T item);
    String getReaderUIText();
    List<T> createItemListToBatch();
    String getFileName(String rootDirName);
}
