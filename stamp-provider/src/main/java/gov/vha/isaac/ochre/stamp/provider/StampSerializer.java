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
package gov.vha.isaac.ochre.stamp.provider;

import gov.vha.isaac.ochre.api.DataSerializer;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.commit.Stamp;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

/**
 *
 * @author kec
 */
public class StampSerializer implements DataSerializer<Stamp>, Serializable {
    @Override
    public void serialize(DataOutput out, Stamp stamp) {
        try {
            out.writeBoolean(stamp.getStatus().getBoolean());
            out.writeLong(stamp.getTime());
            out.writeInt(stamp.getAuthorSequence());
            out.writeInt(stamp.getModuleSequence());
            out.writeInt(stamp.getPathSequence());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Stamp deserialize(DataInput in) {
        try {
            return new Stamp(State.getFromBoolean(in.readBoolean()),
                    in.readLong(), in.readInt(), in.readInt(), in.readInt());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
