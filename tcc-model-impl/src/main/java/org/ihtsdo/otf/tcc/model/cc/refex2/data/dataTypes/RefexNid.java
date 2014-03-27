/*
 * Copyright 2010 International Health Terminology Standards Development Organisation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ihtsdo.otf.tcc.model.cc.refex2.data.dataTypes;

import java.beans.PropertyVetoException;

import org.ihtsdo.otf.tcc.api.refex2.data.RefexDataType;
import org.ihtsdo.otf.tcc.api.refex2.data.dataTypes.RefexNidBI;
import org.ihtsdo.otf.tcc.model.cc.refex2.data.RefexData;

/**
 * 
 * {@link RefexNid}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexNid extends RefexData implements RefexNidBI {
    public RefexNid(int nid) throws PropertyVetoException {
        super(RefexDataType.NID);
        setDataNid(nid);
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refex2.data.dataTypes.RefexNidBI#setDataNid(int)
     */
    @Override
    public void setDataNid(int integer) throws PropertyVetoException {
        data_ = RefexInteger.intToByteArray(integer);
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refex2.data.dataTypes.RefexNidBI#getDataNid()
     */
    @Override
    public int getDataNid() {
        return RefexInteger.getIntFromByteArray(data_);
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refex2.data.RefexDataBI#getDataObject()
     */
    @Override
    public Object getDataObject() {
        return getDataNid();
    }
}
