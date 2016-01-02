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
package gov.vha.isaac.ochre.sememe.provider;


import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.externalizable.ByteArrayDataBuffer;
import gov.vha.isaac.ochre.model.sememe.SememeChronologyImpl;
import gov.vha.isaac.ochre.model.waitfree.WaitFreeMergeSerializer;

/**
 *
 * @author kec
 */
public class SememeSerializer implements WaitFreeMergeSerializer<SememeChronologyImpl<? extends SememeVersion<?>>>{

    @Override
    public void serialize(ByteArrayDataBuffer d, SememeChronologyImpl<? extends SememeVersion<?>> a) {
        byte[] data = a.getDataToWrite();
        d.put(data, 0, data.length);
    }

    @Override
    public SememeChronologyImpl<?> merge(SememeChronologyImpl<? extends SememeVersion<?>> a,
                                         SememeChronologyImpl<? extends SememeVersion<?>> b, int writeSequence) {
        byte[] dataBytes = a.mergeData(writeSequence, b.getDataToWrite(writeSequence));
        ByteArrayDataBuffer db = new ByteArrayDataBuffer(dataBytes);
        return SememeChronologyImpl.make(db);
    }

    @Override
    public SememeChronologyImpl<?> deserialize(ByteArrayDataBuffer db) {
       return SememeChronologyImpl.make(db);
    }
    
}
