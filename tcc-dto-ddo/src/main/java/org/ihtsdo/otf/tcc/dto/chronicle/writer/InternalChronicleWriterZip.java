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
package org.ihtsdo.otf.tcc.dto.chronicle.writer;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.dto.ChronicleConverter;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;

/**
 *
 * @author kec
 */
public class InternalChronicleWriterZip implements InternalChronicleWriterBI {

    ZipOutputStream zos;

    public InternalChronicleWriterZip(Path path) throws IOException {
        Files.deleteIfExists(path);
        Files.createDirectories(path.getParent());
        Files.createFile(path);
        zos =
                new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(path, StandardOpenOption.WRITE)));
    }

    @Override
    public void write(TtkConceptChronicle ttkConceptChronicle, long time) throws IOException {

        ConceptChronicleBI chronicleToWrite = ChronicleConverter.convert(ttkConceptChronicle);
        write(chronicleToWrite, time);
    }

    @Override
    public void write(ConceptChronicleBI chronicleToWrite, long time) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(10240);
        DataOutputStream daos = new DataOutputStream(baos);
        chronicleToWrite.writeExternal(daos);
        String entryName = "c|"
                + chronicleToWrite.getNid() + "|" + Long.toString(time);
        
        ZipEntry entry = new ZipEntry(entryName);
        entry.setTime(time);
        entry.setMethod(ZipEntry.DEFLATED);
        
        zos.putNextEntry(entry);
                
        zos.write(baos.toByteArray());
        zos.closeEntry();
        baos.reset();
    }
    
    @Override
    public void close() throws IOException {
        zos.close();
    }
    
}
