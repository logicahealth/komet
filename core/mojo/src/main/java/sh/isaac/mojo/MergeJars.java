/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
package sh.isaac.mojo;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 *
 * @author kec
 */
@Mojo(
        name = "merge-jars",
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES
)

public class MergeJars
        extends AbstractMojo {

   @Parameter(required = true)
   private File inputJar1;

   @Parameter(required = true)
   private File inputJar2;

   @Parameter(required = true)
   private File mergedJar;

   private final HashMap<String, ManifestEntry> entryMap = new HashMap<>();
   private ManifestEntry currentEntry;

   @Override
   public void execute() throws MojoExecutionException, MojoFailureException {
      File tempZip = new File(mergedJar.getParentFile(), "temp.zip");

      try (ZipFile jar1 = new ZipFile(inputJar1);
              ZipFile jar2 = new ZipFile(inputJar2);
              ZipOutputStream writer
              = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(tempZip)));) {
         currentEntry = new ManifestEntry(jar1.getName());
         copyEntries(jar1, writer);
         currentEntry = new ManifestEntry(jar1.getName());
         copyEntries(jar2, writer);
         ZipEntry manifestEntry = new ZipEntry("META-INF/MANIFEST.MF");
         byte[] manifestData = createManifestData();
         
         manifestEntry.setSize(manifestData.length);
         writer.putNextEntry(manifestEntry);
         writer.write(manifestData);
         writer.flush();
         writer.close();
      } catch (IOException ex) {
         getLog().error(null, ex);
      }
      tempZip.renameTo(mergedJar);
   }

   private byte[] createManifestData() {
      StringWriter manifest = new StringWriter();
      manifest.append("Manifest-Version: 1.0\n");
      manifest.append("Archiver-Version: Plexus Archiver\n");
      manifest.append("Built-By: kec\n");
      manifest.append("Created-By: yGuard Bytecode Obfuscator 2.6\n\n");
      ArrayList<String> sortedKeys = new ArrayList<>(entryMap.keySet());
      Collections.sort(sortedKeys);
      for (String key : sortedKeys) {
         manifest.append(entryMap.get(key).toString());
      }
      return manifest.toString().getBytes(Charset.forName("US-ASCII"));
   }

   private void copyEntries(final ZipFile jar1, final ZipOutputStream writer) throws IOException {
      // Enumerate each entry
      for (Enumeration<? extends ZipEntry> entries
              = jar1.entries(); entries.hasMoreElements();) {
         // Get the entry name and write it to the output file
         ZipEntry zipEntry = entries.nextElement();
         int entryLength = (int) zipEntry.getSize();
         byte[] entryData = new byte[entryLength];
         int dataRead = 0;
         InputStream inputStream = jar1.getInputStream(zipEntry);
         while (dataRead < entryLength) {
            dataRead = dataRead + inputStream.read(entryData, dataRead, entryLength - dataRead);
         }

         if (zipEntry.getName().endsWith("MANIFEST.MF")) {
                 getLog().info("Found a manifest: " + zipEntry.getName());

            BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(entryData)));
            int nameCount = 0;
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
               if (line.startsWith("Name")) {
                  if (entryMap.containsKey(line)) {
                     nameCount++;
                     currentEntry = entryMap.get(line);
                  } else {
                     currentEntry = new ManifestEntry(line);
                     entryMap.put(line, currentEntry);
                  }
               } else {
                  if (line.length() > 2) {
                     currentEntry.addEntry(line);
                  }
               }
            }
            getLog().debug("NameCount: " + nameCount);
         } else {
            zipEntry.setCompressedSize(-1);
            writer.putNextEntry(zipEntry);
            writer.write(entryData);
         }
      }

   }

   private static class ManifestEntry {

      String name;
      ArrayList<String> entries = new ArrayList<>();

      public ManifestEntry(String name) {
         this.name = name;
      }

      @SuppressWarnings("unused")
      public String getName() {
         return name;
      }

      public void addEntry(String entry) {
         entries.add(entry);
      }

      @Override
      public String toString() {
         StringBuilder builder = new StringBuilder();
         builder.append(name).append("\n");
         entries.forEach((entry) -> {
            builder.append(entry).append("\n");
         });
         builder.append("\n");
         return builder.toString();
      }

   }
}
