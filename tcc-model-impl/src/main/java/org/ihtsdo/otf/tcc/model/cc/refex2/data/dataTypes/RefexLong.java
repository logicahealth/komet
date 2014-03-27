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
import org.ihtsdo.otf.tcc.api.refex2.data.dataTypes.RefexLongBI;
import org.ihtsdo.otf.tcc.model.cc.refex2.data.RefexData;

/**
 * 
 * {@link RefexLong}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexLong extends RefexData implements RefexLongBI {
    public RefexLong(long l) throws PropertyVetoException {
        super(RefexDataType.LONG);
        setDataLong(l);
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refex2.data.dataTypes.RefexLongBI#setDataLong(long)
     */
    @Override
    public void setDataLong(long l) throws PropertyVetoException {
        data_ = ByteBuffer.allocate(8).putLong(l).array();
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refex2.data.dataTypes.RefexLongBI#getDataLong()
     */
    @Override
    public long getDataLong() {
        return ByteBuffer.wrap(data_).getLong();
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refex2.data.RefexDataBI#getDataObject()
     */
    @Override
    public Object getDataObject() {
        return getDataLong();
    }
    
    public static void main(String[] args) throws PropertyVetoException
    {
        //TODO turn this into a JUNit test
        RefexLong l = new RefexLong(5);
        
        System.out.println(l.getDataLong());
        System.out.println(l.getDataObject());
        System.out.println(Long.MAX_VALUE);
        l.setDataLong(Long.MAX_VALUE);
        System.out.println(l.getDataLong());
        System.out.println(l.getDataObject());
        l.setDataLong(Long.MIN_VALUE);
        System.out.println(Long.MIN_VALUE);
        System.out.println(l.getDataLong());
        System.out.println(l.getDataObject());
    }
}
