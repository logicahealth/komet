/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.komet.gui.importation;

import java.io.File;
import java.util.zip.ZipEntry;
import sh.isaac.solor.ContentProvider;

/**
 *
 * @author kec
 */
public class ImportItemZipEntry extends ImportItemAbstract {
    public static final String FILE_PARENT_KEY = "file";
    
    final File zipFile;
    final ZipEntry nestedZipFile;
    final ZipEntry entry;
    final String parentKey;
    SelectedImportType importType;
    final byte[] itemBytes;

    public ImportItemZipEntry(File zipFile, ZipEntry entry) {
       this(zipFile, null, entry, null);
    }
    
    public ImportItemZipEntry(File zipFile, ZipEntry nestedZipFile, ZipEntry entry, byte[] itemBytes) {
        this.zipFile = zipFile;
        this.entry = entry;
        this.nestedZipFile = nestedZipFile;
        this.itemBytes = itemBytes;
        String[] nameParts = entry.getName().split("/");
        for (String namePart: nameParts) {
            if (namePart.toLowerCase().startsWith("readme")) {
                importType = SelectedImportType.IGNORE;
                break;
            }
            switch (namePart.toLowerCase()) {
                case "full":
                    importType = SelectedImportType.FULL;
                    break;
                case "snapshot":
                    importType = SelectedImportType.SNAPSHOT;
                    break;
                case "delta":
                    importType = SelectedImportType.DELTA;
                    break;
                case "documentation":
                    importType = SelectedImportType.IGNORE;
                    break;
            }
            if (importType != null) {
                break;
            }
        }
        
        if (nameParts.length == 1) {
           if (nestedZipFile == null) {
              parentKey = FILE_PARENT_KEY;
           }
           else {
              parentKey = nestedZipFile.getName();
           }
        } else {
            StringBuilder parentKeyBuilder = new StringBuilder();
            if (nestedZipFile != null) {
               parentKeyBuilder.append(nestedZipFile.getName()).append("/");
            }
            for (int i = 0; i < nameParts.length - 1; i++) {
                parentKeyBuilder.append(nameParts[i]).append("/");
            }
            parentKey = parentKeyBuilder.toString();
        }
        setName(nameParts[nameParts.length - 1]);
    }

    public String getParentKey() {
        return parentKey;
    }

    public SelectedImportType getImportType() {
        return importType;
    }

    @Override
    public String recordCount() {
        return "";
    }

    @Override
    public String toString() {
        return "ImportItemZipEntry{" + "entry=" + entry + ", parentKey=" + parentKey + 
                ", importType: " + importType + ", nestedZip:" + nestedZipFile + '}';
    }
    
    public ContentProvider getContent() {
        return new ContentProvider(zipFile, nestedZipFile, entry, itemBytes);
    }
}
