package sh.isaac.solor;

import sh.isaac.solor.direct.ImportType;

import java.io.File;
import java.util.zip.ZipEntry;

/**
 * 2019-05-15
 * aks8m - https://github.com/aks8m
 */
public class DetailedContentProvider extends ContentProvider {

    private final ImportType contentImportType;

    public DetailedContentProvider(File zipFile, ZipEntry entry, ImportType contentImportType) {
        super(zipFile, entry);
        this.contentImportType = contentImportType;
    }
}
