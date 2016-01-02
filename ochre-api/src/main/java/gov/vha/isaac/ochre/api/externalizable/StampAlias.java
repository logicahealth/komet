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
package gov.vha.isaac.ochre.api.externalizable;

import gov.vha.isaac.ochre.api.externalizable.ByteArrayDataBuffer;
import gov.vha.isaac.ochre.api.externalizable.OchreExternalizable;
import gov.vha.isaac.ochre.api.externalizable.OchreExternalizableObjectType;

/**
 *
 * @author kec
 */
public class StampAlias implements OchreExternalizable {

    int stampSequence;
    int stampAlias;

    public StampAlias(int stampSequence, int stampAlias) {
        this.stampSequence = stampSequence;
        this.stampAlias = stampAlias;
    }
    
   public StampAlias(ByteArrayDataBuffer in) {
        byte version = in.getByte();
        if (version == getDataFormatVersion()) {
            stampSequence = StampUniversal.get(in).getStampSequence();
            stampAlias = StampUniversal.get(in).getStampSequence();
        } else {
            throw new UnsupportedOperationException("Can't handle version: " + version);
        }
    }
    
    @Override
    public void putExternal(ByteArrayDataBuffer out) {
        out.putByte(getDataFormatVersion());
        StampUniversal.get(stampSequence).writeExternal(out);
        StampUniversal.get(stampAlias).writeExternal(out);
    }

    @Override
    public byte getDataFormatVersion() {
        return 0;
    }

    @Override
    public OchreExternalizableObjectType getOchreObjectType() {
        return OchreExternalizableObjectType.STAMP_ALIAS;
    }
    
    
}
