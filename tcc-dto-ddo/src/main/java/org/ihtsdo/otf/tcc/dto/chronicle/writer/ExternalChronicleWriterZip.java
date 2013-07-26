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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;
import org.ihtsdo.otf.tcc.dto.chronicle.reader.ExternalChronicalReaderZip;
import org.ihtsdo.otf.tcc.dto.component.description.TtkDescriptionChronicle;

/**
 *
 * @author kec
 */
public class ExternalChronicleWriterZip implements ExternalChronicleWriterBI {

    private static boolean testForMatch(TtkConceptChronicle eConcept, ConceptSpec match) {
        boolean found = false;
        if (eConcept.getPrimordialUuid().equals(match.getUuids()[0])) {
            System.out.println("[1] Found: " + eConcept);
            found = true;
        }
//        } else {
//            for (TtkDescriptionChronicle desc: eConcept.getDescriptions()) {
//                if (desc.getText().toLowerCase().equals(match.getDescription().toLowerCase())) {
//                    System.out.println("[2] Found: " + eConcept);
//                    found = true;
//                }
//            }
//        }
        return found;
    }

    ZipOutputStream zos;
    ByteArrayOutputStream baos = new ByteArrayOutputStream(10240);

    public ExternalChronicleWriterZip(Path path) throws IOException {
        Files.deleteIfExists(path);
        Files.createDirectories(path.getParent());
        Files.createFile(path);
        zos =
                new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(path, StandardOpenOption.WRITE)));
    }

    @Override
    public void write(TtkConceptChronicle ttkConceptChronicle, long time) throws IOException {
        DataOutputStream daos = new DataOutputStream(baos);
        ttkConceptChronicle.writeExternal(daos);
        String entryName = "c|"
                + ttkConceptChronicle.getPrimordialUuid().toString() + "|" + Long.toString(time);
        
        ZipEntry entry = new ZipEntry(entryName);
        entry.setTime(time);
        entry.setMethod(ZipEntry.DEFLATED);
        
        zos.putNextEntry(entry);
                
        zos.write(baos.toByteArray());
        zos.closeEntry();
        baos.reset();
    }

    @Override
    public void write(ConceptChronicleBI chronicleToWrite, long time) throws IOException {
        write(new TtkConceptChronicle(chronicleToWrite), time);
    }
    
    @Override
    public void close() throws IOException {
        zos.close();
    }
    
    public static void  main(String[] args) {
        try {
            File f = new File("/Users/kec/NetBeansProjects/econ/eConcept.econ");
            DataInputStream dataStream = new DataInputStream(
                 new BufferedInputStream(new FileInputStream(f)));
            
            int count = 0;
            long startTime = System.currentTimeMillis();
            boolean found = false;
            while (dataStream.available() > 0) {
                TtkConceptChronicle eConcept = new TtkConceptChronicle(dataStream);
                found = (testForMatch(eConcept, Snomed.RESPIRATORY_DISORDER) ||
                         testForMatch(eConcept, Snomed.ALLERGIC_ASTHMA));
                count++;
            }
            if (!found) {
                            System.out.println("[3] cannot find HYPERSENSITIVITY_CONDITION. ");
            }
            dataStream.close();
            long elapsed = System.currentTimeMillis() - startTime;
            System.out.println("Read " + count + " concepts in: " + elapsed + " ms");
            ExternalChronicleWriterZip writer = new ExternalChronicleWriterZip(Paths.get("target", "test.ecf"));
            dataStream = new DataInputStream(
                 new BufferedInputStream(new FileInputStream(f)));
            count = 0;
            startTime = System.currentTimeMillis();
            while (dataStream.available() > 0) {
                TtkConceptChronicle eConcept = new TtkConceptChronicle(dataStream);
                writer.write(eConcept, startTime);
                count++;
            }
            dataStream.close();
            writer.close();
            elapsed = System.currentTimeMillis() - startTime;
            System.out.println("Read and write " + count + " concepts in: " + elapsed + " ms");
            ExternalChronicalReaderZip reader = new ExternalChronicalReaderZip(Paths.get("target", "test.ecf"));
            startTime = System.currentTimeMillis();
            SortedSet<ZipEntry> entries = reader.getSortedEntries();
            elapsed = System.currentTimeMillis() - startTime;
            System.out.println("Sorted " + entries.size() + " entries in: " + elapsed + " ms");
            startTime = System.currentTimeMillis();
            for (ZipEntry entry: entries) {
                reader.readEntry(entry);
            }
            elapsed = System.currentTimeMillis() - startTime;
            System.out.println("Read " + entries.size() + " sorted entries in: " + elapsed + " ms");
            
            
            ExternalChronicleWriterXml xmlWriter = new ExternalChronicleWriterXml(Paths.get("target", "test.xml"));
            startTime = System.currentTimeMillis();
            elapsed = System.currentTimeMillis() - startTime;
            System.out.println("Sorted " + entries.size() + " entries in: " + elapsed + " ms");
            startTime = System.currentTimeMillis();
            for (ZipEntry entry: entries) {
                TtkConceptChronicle ttkConceptChronicle = reader.readEntry(entry);
                xmlWriter.write(ttkConceptChronicle, startTime);
            }
            xmlWriter.close();;
            elapsed = System.currentTimeMillis() - startTime;
            System.out.println("Read/wrote xml " + entries.size() + " sorted entries in: " + elapsed + " ms");
            
            
            startTime = System.currentTimeMillis();
            InternalChronicleWriterBI internalWriter = new InternalChronicleWriterZip(Paths.get("target", "test.icf"));
            for (ZipEntry entry: entries) {
                internalWriter.write(reader.readEntry(entry), startTime);
            }
            internalWriter.close();
            reader.close();
            elapsed = System.currentTimeMillis() - startTime;
            System.out.println("Read " + entries.size() + 
                    " sorted entries, converted to internal, and wrote in: " + 
                    elapsed + " ms");
            
            
            
            
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ExternalChronicleWriterZip.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(ExternalChronicleWriterZip.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
