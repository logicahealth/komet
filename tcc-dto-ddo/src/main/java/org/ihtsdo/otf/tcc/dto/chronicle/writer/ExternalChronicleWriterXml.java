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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;

/**
 *
 * @author kec
 */
public class ExternalChronicleWriterXml implements ExternalChronicleWriterBI {
    BufferedWriter writer;
    public ExternalChronicleWriterXml(Path path) throws IOException {
        Files.deleteIfExists(path);
        Files.createDirectories(path.getParent());
        Files.createFile(path);
        writer = Files.newBufferedWriter(path, Charset.forName("UTF-8"));
        writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
        writer.append("<concepts>\n");
    }

    @Override
    public void write(TtkConceptChronicle ttkConceptChronicle, long time) throws IOException {
        writer.append(ttkConceptChronicle.toXml());
    }

    @Override
    public void write(ConceptChronicleBI chronicleToWrite, long time) throws IOException {
        write(new TtkConceptChronicle(chronicleToWrite), time);
    }
    
    @Override
    public void close() throws IOException {
        writer.append("</concepts>");
        writer.close();
    }
        
}
