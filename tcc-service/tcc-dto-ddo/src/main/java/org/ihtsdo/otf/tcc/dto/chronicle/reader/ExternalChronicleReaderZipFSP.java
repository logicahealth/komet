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

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;
import org.ihtsdo.otf.tcc.dto.chronicle.FileChronicleZipFSP;

/**
 *
 * @author kec
 */
public class ExternalChronicleReaderZipFSP extends FileChronicleZipFSP {

    public ExternalChronicleReaderZipFSP(Path chroniclePath) throws IOException {
        super(chroniclePath, AccessType.OPEN);
    }
    
   public void process() throws IOException {
       AtomicInteger count = new AtomicInteger(0);
        for (Path p: chronicleFileSystem.getRootDirectories()) {
            process(p, count);
        }
        
        System.out.println("Processed: " + count);
    }
    public void process(Path path, AtomicInteger count) throws IOException {
        if (Files.notExists(path))
            return;
        count.incrementAndGet();
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
                for (Path child : ds)
                    process(child, count);
            }
        }
    }    

    
    public void list() throws IOException {
        for (Path p: chronicleFileSystem.getRootDirectories()) {
            list(p, true);
        }
    }
    
    public void list(Path path, boolean verbose ) throws IOException {
        if (!"/".equals(path.toString())) {
           System.out.printf("  %s%n", path.toString());
           if (verbose)
                System.out.println(Files.readAttributes(path, BasicFileAttributes.class).toString());
        }
        if (Files.notExists(path))
            return;
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
                for (Path child : ds)
                    list(child, verbose);
            }
        }
    }    
    
}
