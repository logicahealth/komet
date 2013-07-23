/*
 *  Copyright 2011 kec.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.ihtsdo.otf.tcc.datastore.nidmaps;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author kec
 */
public class UuidListBinder extends TupleBinding<List<UUID>> {

    @Override
    public List<UUID> entryToObject(TupleInput ti) {
        int size = ti.readShort();
        List<UUID> uuidList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            uuidList.add(new UUID(ti.readLong(), ti.readLong()));
        }
        return uuidList;
    }

    @Override
    public void objectToEntry(List<UUID> e, TupleOutput to) {
        to.writeShort(e.size());
        for (UUID uuid: e) {
            to.writeLong(uuid.getMostSignificantBits());
            to.writeLong(uuid.getLeastSignificantBits());
        }
    }

}
