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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;
import org.ihtsdo.otf.tcc.dto.chronicle.FileChronicleZipFSP;
import org.ihtsdo.otf.tcc.dto.chronicle.reader.ExternalChronicleReaderZipFSP;

/**
 *
 * @author kec
 */
public class ExternalChronicleWriterZipFSP extends FileChronicleZipFSP implements ExternalChronicleWriterBI {

    private static void printMemoryInfo() {
        int mb = 1024*1024;
         
        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();
         
        System.out.println("##### Heap utilization statistics [MB] #####");
         
        //Print used memory
        System.out.println("Used Memory:"
            + (runtime.totalMemory() - runtime.freeMemory()) / mb);
 
        //Print free memory
        System.out.println("Free Memory:"
            + runtime.freeMemory() / mb);
         
        //Print total available memory
        System.out.println("Total Memory:" + runtime.totalMemory() / mb);
 
        //Print Maximum available memory
        System.out.println("Max Memory:" + runtime.maxMemory() / mb);
    }

    public ExternalChronicleWriterZipFSP(Path chroniclePath) throws IOException {
        super(chroniclePath, AccessType.CREATE);
    }

    @Override
    public void write(TtkConceptChronicle ttkConceptChronicle, long time) throws IOException {
        Path chroniclePath =
                chronicleFileSystem.getPath("c|"
                + ttkConceptChronicle.getPrimordialUuid().toString() + "|" + Long.toString(time));
        
        
        try (OutputStream out = getZipFSProvider().newOutputStream(chroniclePath, StandardOpenOption.CREATE_NEW)) {
            ttkConceptChronicle.writeExternal(new DataOutputStream(out));
        }
        Files.setAttribute(chroniclePath, "creationTime",
                                       FileTime.fromMillis(time));
        Files.setAttribute(chroniclePath, "lastModifiedTime",
                                       FileTime.fromMillis(time));
    }

    @Override
    public void write(ConceptChronicleBI chronicleToWrite, long time) throws IOException {
        write(new TtkConceptChronicle(chronicleToWrite), time);
    }
    public static void  main(String[] args) {
        printMemoryInfo();
        
        try {
            File f = new File("/Users/kec/NetBeansProjects/econ/eConcept.econ");
            DataInputStream dataStream = new DataInputStream(
                 new BufferedInputStream(new FileInputStream(f)));
            
            int count = 0;
            long startTime = System.currentTimeMillis();
            while (dataStream.available() > 0) {
                TtkConceptChronicle eConcept = new TtkConceptChronicle(dataStream);
                count++;
            }
            dataStream.close();
            long elapsed = System.currentTimeMillis() - startTime;
            System.out.println("Read " + count + " concepts in: " + elapsed + " ms");
        printMemoryInfo();
            
            ExternalChronicleWriterZipFSP writer = new ExternalChronicleWriterZipFSP(Paths.get("target", "test.ecf"));
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
        printMemoryInfo();
            
            startTime = System.currentTimeMillis();
            ExternalChronicleReaderZipFSP reader = new ExternalChronicleReaderZipFSP(Paths.get("target", "test.ecf"));
            reader.process();
            elapsed = System.currentTimeMillis() - startTime;
            System.out.println("Process " + count + " concepts in: " + elapsed + " ms");
        printMemoryInfo();
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ExternalChronicleWriterZipFSP.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(ExternalChronicleWriterZipFSP.class.getName()).log(Level.SEVERE, null, ex);
        }
        printMemoryInfo();
    }
}
