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

import java.util.UUID;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;

/**
 *
 * @author kec
 */
public class UuidIntRecord implements Comparable<UuidIntRecord>{
    private long msb;
    private long lsb;
    private int nid;

    public UuidIntRecord(long msb, long lsb, int nid) {
        this.msb = msb;
        this.lsb = lsb;
        this.nid = nid;
    }

    public long getLsb() {
        return lsb;
    }

    public long getMsb() {
        return msb;
    }

    public UuidIntRecord(UUID uuid, int nid) {
        this.msb = uuid.getMostSignificantBits();
        this.lsb = uuid.getLeastSignificantBits();
        this.nid = nid;
    }
    
    public int getNid() {
        return nid;
    }
    
    public UUID getUuid() {
       return new UUID(msb, lsb);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UuidIntRecord other = (UuidIntRecord) obj;
        if (this.msb != other.msb) {
            return false;
        }
        if (this.lsb != other.lsb) {
            return false;
        }
        if (this.nid != other.nid) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + (int) (this.msb ^ (this.msb >>> 32));
        hash = 89 * hash + (int) (this.lsb ^ (this.lsb >>> 32));
        return hash;
    }
    
    public short getShortUuidHash() {
        return getShortUuidHash(getUuid());
    }
    public static short getShortUuidHash(UUID uuid) {
        short hash = Hashcode.intHashToShortHash(uuid.hashCode());
        if (hash >= 0) {
            return hash;
        }
        return (short) (hash - Short.MIN_VALUE);
    }

    @Override
    public int compareTo(UuidIntRecord t) {
        if (nid != t.nid) {
            return nid - t.nid;
        }
        return getUuid().compareTo(t.getUuid());
    }
}
