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
package sh.isaac.solor.direct;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * @author kec
 */
public class RxNormExtractor {

    public static void main(String[] args) {
        HashSet<String> watchTokens = new HashSet<>();
        watchTokens.add("852877");
        try (ZipFile zipFile = new ZipFile("/Users/kec/isaac/solor-source-artifact-transformer/solor-terminology-sources/RxNorm_full_03052018.zip", Charset.forName("UTF-8"))) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName().toLowerCase();
                if (entryName.endsWith(".rrf") && !entryName.startsWith("prescribe")) {
                    System.out.println(entryName);
                    String rowString;
                    BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry), Charset.forName("UTF-8")));
                    while ((rowString = reader.readLine()) != null) {
                        String[] columns = rowString.split("\\|");
                        for (String column : columns) {
                            if (watchTokens.contains(column)) {
                                
                                System.out.println(entry.getName() + ": " + rowString);
                                break;
                            }

                        }
                    }

                }

            }
        } catch (IOException ex) {
            Logger.getLogger(RxNormExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
