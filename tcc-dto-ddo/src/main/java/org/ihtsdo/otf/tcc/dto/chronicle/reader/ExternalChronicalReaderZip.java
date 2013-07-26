/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.otf.tcc.dto.chronicle.reader;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;

/**
 *
 * @author kec
 */
public class ExternalChronicalReaderZip {
    ZipFile zipFile;

    public ExternalChronicalReaderZip(Path path) throws ZipException, IOException {
        zipFile = new ZipFile(path.toFile());
        System.out.println(path + " contains " + zipFile.size() + " entries.");
    }
    
    public SortedSet<ZipEntry> getSortedEntries() {
        
        SortedSet<ZipEntry> sortedEntries = new TreeSet<>(new Comparator<ZipEntry>() {

            @Override
            public int compare(ZipEntry ze1, ZipEntry ze2) {
                return ze1.getName().compareTo(ze2.getName());
            }
        });
        Enumeration<? extends ZipEntry> zipEntryItr = zipFile.entries();
        while (zipEntryItr.hasMoreElements()) {
            sortedEntries.add(zipEntryItr.nextElement());
        }
        return sortedEntries;
    }
    
    public TtkConceptChronicle readEntry(ZipEntry entry) throws IOException, ClassNotFoundException {
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(zipFile.getInputStream(entry)))) {
            return new TtkConceptChronicle(dis);
        }
        
        
    }

    public void close() throws IOException {
        zipFile.close();
    }
    
}
