/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
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
package gov.vha.isaac.ochre.model.log;

import gov.vha.isaac.ochre.model.ByteArrayDataBuffer;
import gov.vha.isaac.ochre.model.OchreExternalizable;
import gov.vha.isaac.ochre.model.OchreExternalizableObjectType;
import gov.vha.isaac.ochre.model.concept.ConceptChronologyImpl;
import gov.vha.isaac.ochre.model.sememe.SememeChronologyImpl;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.util.Optional;

/**
 *
 * @author kec
 */
public class OchreLogHandler {

    public static class OchreLogWriter {

        ByteArrayDataBuffer buffer;
        DataOutput output;

        public void write(OchreExternalizable ochreObject) {
            try {
                buffer.reset();
                ochreObject.putExternal(buffer);
                
                output.writeByte(ochreObject.getOchreObjectType().getToken());
                output.writeByte(ochreObject.getDataFormatVersion());
                output.writeInt(buffer.getLimit());
                output.write(buffer.getData(), 0, buffer.getLimit());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    
    public static class OchreLogReader {

        DataInput input;

        public Optional<OchreExternalizable> read() {
            try {
                OchreExternalizableObjectType type = OchreExternalizableObjectType.fromDataStream(input);
                byte dataFormatVersion = input.readByte();
                int recordSize = input.readInt();
                byte[] objectData = new byte[recordSize];
                input.readFully(objectData);
                ByteArrayDataBuffer buffer = new ByteArrayDataBuffer(objectData);
                buffer.setExternalData(true);
                buffer.setObjectDataFormatVersion(dataFormatVersion);
                switch (type) {
                    case COMMIT_RECORD:
                        return Optional.of(new CommitRecordImpl(dataFormatVersion, buffer));
                    case CONCEPT:
                        return Optional.of(new ConceptChronologyImpl(buffer));
                    case SEMEME:
                        return Optional.of(new SememeChronologyImpl(dataFormatVersion, buffer));
                        default: 
                            throw new UnsupportedOperationException("Can't handle: " + type);
                }
            } catch (EOFException ex) {
                return Optional.empty();
             } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    
}
