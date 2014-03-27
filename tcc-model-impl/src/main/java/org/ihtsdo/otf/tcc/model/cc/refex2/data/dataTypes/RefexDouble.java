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
import java.nio.ByteBuffer;

import org.ihtsdo.otf.tcc.api.refex2.data.RefexDataType;
import org.ihtsdo.otf.tcc.api.refex2.data.dataTypes.RefexDoubleBI;
import org.ihtsdo.otf.tcc.model.cc.refex2.data.RefexData;

/**
 * 
 * {@link RefexDouble}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexDouble extends RefexData implements RefexDoubleBI {
    public RefexDouble(double d) throws PropertyVetoException {
        super(RefexDataType.DOUBLE);
        setDataDouble(d);
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refex2.data.dataTypes.RefexDoubleBI#setDataDouble(double)
     */
    @Override
    public void setDataDouble(double d) throws PropertyVetoException {
        data_ = ByteBuffer.allocate(8).putDouble(d).array();
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refex2.data.dataTypes.RefexDoubleBI#getDataDouble()
     */
    @Override
    public double getDataDouble() {
        return ByteBuffer.wrap(data_).getDouble();
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refex2.data.RefexDataBI#getDataObject()
     */
    @Override
    public Object getDataObject() {
        return getDataDouble();
    }
}
