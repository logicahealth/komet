/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.otf.tcc.api.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

public class JarExtractor {

    private enum READ_TYPE {
        JAR_FILE, JAR_INPUT_STREAM
    };

    private static READ_TYPE readType = READ_TYPE.JAR_INPUT_STREAM;

    /**
     * Note the following about opening large jar files:
     * <p>
     * http://confluence.atlassian.com/display/DOC/java.util.zip.ZipFile.open+
     * causes+OutOfMemoryError+for+large+zip+files
     * <p>
     * http://bugs.sun.com/bugdatabase/view_bug.do;:YfiG?bug_id=4705373
     * <p>
     * The method java.util.zip.ZipFile.open does not actually use the allocated
     * memory of the heap, it maps the entire zip file into virtual memory
     * outside the heap. If you run into this problem, you should try to reduce
     * your heap size to about 600MB and try the restore again. This problem is
     * allegedly fixed in java 6.
     * 
     * For now, using JarInputStream bypasses the memory mapped issues with
     * JarFile, but may be worse performance.
     * 
     */

    public static void execute(File source, File destDir) throws IOException {
        switch (readType) {
        case JAR_FILE:
            executeJarFile(source, destDir);
            break;
        case JAR_INPUT_STREAM:
            executeJarInputStream(source, destDir);
            break;
        default:
            throw new RuntimeException("Can't handle readtype: " + readType);
        }

    }

    private static void executeJarFile(File source, File destDir) throws IOException {
        destDir.mkdirs();
        JarFile jf = new JarFile(source);
        for (Enumeration<JarEntry> e = jf.entries(); e.hasMoreElements();) {
            JarEntry je = e.nextElement();
            System.out.println("Jar entry (a): " + je.getName() + " compressed: " + je.getCompressedSize() + " size: "
                + je.getSize() + " time: " + new Date(je.getTime()) + " comment: " + je.getComment());
            java.io.File f = new java.io.File(destDir + java.io.File.separator + je.getName());

            if (je.isDirectory()) { // if its a directory, create it
                f.mkdir();
                continue;
            }
            try (java.io.InputStream is = new BufferedInputStream(jf.getInputStream(je))) {
                f.getParentFile().mkdirs();
                java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
                byte[] buffer = new byte[102400];
                int bytesRead;
                while ((bytesRead = is.read(buffer, 0, 102400)) != -1) {
                    // write contents of 'is' to 'fos'
                    fos.write(buffer, 0, bytesRead);
                }
                fos.close();
            }
            f.setLastModified(je.getTime());
        }
    }

    private static void executeJarInputStream(File source, File destDir) throws IOException {
        destDir.mkdirs();
        FileInputStream fis = new FileInputStream(source);
        BufferedInputStream bis = new BufferedInputStream(fis);
        try (JarInputStream jis = new JarInputStream(bis)) {
            JarEntry je = jis.getNextJarEntry();
            while (je != null) {
                System.out.println("Jar entry (b): " + je.getName() + " compressed: " + je.getCompressedSize() + " size: "
                    + je.getSize() + " time: " + new Date(je.getTime()) + " comment: " + je.getComment());
                java.io.File f = new java.io.File(destDir + java.io.File.separator + je.getName());
                if (je.isDirectory()) {
                    f.mkdir();
                } else {
                    f.getParentFile().mkdirs();
                    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(f)) {
                        byte[] buffer = new byte[102400];
                        int bytesRead;
                        while ((bytesRead = jis.read(buffer, 0, 102400)) != -1) {
                            // write contents of 'is' to 'fos'
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                    f.setLastModified(je.getTime());
                }
                je = jis.getNextJarEntry();
            }
        }
    }

}
