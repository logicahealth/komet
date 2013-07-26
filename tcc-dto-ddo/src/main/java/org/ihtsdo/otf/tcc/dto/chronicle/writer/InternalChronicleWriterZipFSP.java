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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.dto.ChronicleConverter;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;
import org.ihtsdo.otf.tcc.dto.chronicle.FileChronicleZipFSP;

/**
 *
 * @author kec
 */
public class InternalChronicleWriterZipFSP extends FileChronicleZipFSP implements InternalChronicleWriterBI {

    public InternalChronicleWriterZipFSP(Path chroniclePath) throws IOException {
        super(chroniclePath, AccessType.CREATE);
    }

    @Override
    public void write(TtkConceptChronicle ttkConceptChronicle, long time) throws IOException {

        ConceptChronicleBI chronicleToWrite = ChronicleConverter.convert(ttkConceptChronicle);
        write(chronicleToWrite, time);
    }

    @Override
    public void write(ConceptChronicleBI chronicleToWrite, long time) throws IOException {
        Path chroniclePath =
                chronicleFileSystem.getPath("c|"
                + Integer.toString(chronicleToWrite.getNid()) + "|" + Long.toString(time));
        try (OutputStream out = getZipFSProvider().newOutputStream(chroniclePath, StandardOpenOption.CREATE_NEW)) {
            chronicleToWrite.writeExternal(new DataOutputStream(out));
        }
        Files.setAttribute(chroniclePath, "creationTime",
                                       FileTime.fromMillis(time));
        Files.setAttribute(chroniclePath, "lastModifiedTime",
                                       FileTime.fromMillis(time));
    }
}
