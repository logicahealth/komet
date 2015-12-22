/*
 * Copyright 2015 kec.
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
package gov.vha.isaac.ochre.ibdf.provider;

import gov.vha.isaac.ochre.api.externalizable.BinaryDataWriterService;
import gov.vha.isaac.ochre.api.externalizable.ByteArrayDataBuffer;
import gov.vha.isaac.ochre.api.externalizable.OchreExternalizable;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

/**
 *
 * @author kec
 */
public class BinaryDataWriterProvider implements BinaryDataWriterService {

    private static final int BUFFER_SIZE = 1024;
    Path dataPath;
    ByteArrayDataBuffer buffer = new ByteArrayDataBuffer(BUFFER_SIZE);
    DataOutputStream output;

    public BinaryDataWriterProvider(Path dataPath) throws FileNotFoundException {
        this.dataPath = dataPath;
        this.output = new DataOutputStream(new FileOutputStream(dataPath.toFile()));
    }

    @Override
    public void put(OchreExternalizable ochreObject) {
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

    @Override
    public void close()  {
        try {
            this.output.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
}
