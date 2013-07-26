/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.otf.tcc.datastore.nidmaps;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 *
 * @author kec
 */
public class UuidNidBinder extends TupleBinding<Set<UuidIntRecord>> {

    @Override
    public Set<UuidIntRecord> entryToObject(TupleInput input) {
        int size = input.readInt();
        Set<UuidIntRecord> uuidNidRecSet = 
                new ConcurrentSkipListSet<>();
        for (int i = 0; i < size; i++) {
            uuidNidRecSet.add(new UuidIntRecord(input.readLong(), 
                    input.readLong(), 
                    input.readInt()));
        }
         return uuidNidRecSet;
    }

    @Override
    public void objectToEntry(Set<UuidIntRecord> uuidNidRecSet, TupleOutput output) {
        output.writeInt(uuidNidRecSet.size());
        for (UuidIntRecord uuidNedRec : uuidNidRecSet) {
            output.writeLong(uuidNedRec.getMsb());
            output.writeLong(uuidNedRec.getLsb());
            output.writeInt(uuidNedRec.getNid());
        }
    }
}
