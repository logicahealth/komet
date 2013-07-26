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
package org.ihtsdo.otf.tcc.dto.chronicle;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;

/**
 * Using NIO to access a zip file is a really cool idea, but my tests
 * show that the memory footprint is to large, and it is considerably slower
 * than using the Java7 zip utilities directly. So this implementation is left
 * as an example for future testing. 
 * 
 * See: http://cr.openjdk.java.net/~sherman/zipfs_src/Demo.java 
 * http://docs.oracle.com/javase/7/docs/technotes/guides/io/fsp/zipfilesystemprovider.html
 * @author kec
 */
public class FileChronicleZipFSP {
    
    public enum AccessType { CREATE, OPEN };

    private static FileSystemProvider zipFSProvider;

    protected static FileSystemProvider getZipFSProvider() {
        if (zipFSProvider == null) {
            for (FileSystemProvider provider : FileSystemProvider.installedProviders()) {
                if ("jar".equals(provider.getScheme())) {
                    zipFSProvider = provider;
                    return zipFSProvider;
                }
            }
            throw new UnsupportedOperationException("Can't find zip FileSystemProvider");
        }
        return zipFSProvider;
    }
    protected FileSystem chronicleFileSystem;

    public FileChronicleZipFSP(Path chroniclePath, AccessType access) throws IOException {
        Map<String, Object> env = new HashMap<>();
        switch (access) {
            case CREATE:
                env.put("create", "true");
        }
        
        chronicleFileSystem = getZipFSProvider().newFileSystem(chroniclePath, env);
    }
    
    public void close() throws IOException {
        chronicleFileSystem.close();
    }

}
