/*
 * Copyright 2014 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.otf.tcc.model.cc.concept;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author aimeefurber
 */
public interface I_ManageSimpleConceptData extends I_ManageConceptData{

    byte[] getReadOnlyBytes() throws IOException;

    byte[] getReadWriteBytes() throws IOException;

    DataInputStream getMutableDataStream() throws IOException;

    void resetNidData();

    long getLastChange();

    long getLastWrite();

    void setLastWrite(long version);
    
    int getReadWriteDataVersion() throws InterruptedException, ExecutionException, IOException;

}
