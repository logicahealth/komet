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
import org.ihtsdo.otf.tcc.model.cc.refex2.data.RefexData;

/**
 * 
 * {@link RefexInteger}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexInteger extends RefexData {
    public RefexInteger(int integer) throws PropertyVetoException {
        super(RefexDataType.INTEGER);
        setDataInteger(integer);
    }

    public void setDataInteger(int integer) throws PropertyVetoException {
        data_ = intToByteArray(integer);
    }

    public int getDataInteger() {
       return getIntFromByteArray(data_);
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refex2.data.RefexDataBI#getDataObject()
     */
    @Override
    public Object getDataObject() {
        return getDataInteger();
    }
    
    protected static byte[] intToByteArray(int integer)
    {
        byte[] bytes = new byte[4];
        bytes = new byte[4];
        bytes[0] = (byte) (integer >> 24);
        bytes[1] = (byte) (integer >> 16);
        bytes[2] = (byte) (integer >> 8);
        bytes[3] = (byte) (integer >> 0);
        return bytes;
    }
    
    protected static int getIntFromByteArray(byte[] bytes)
    {
        return ((bytes[0] << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8) | ((bytes[3] & 0xFF) << 0));
    }
    
    public static void main(String[] args) throws PropertyVetoException
    {
        //TODO turn this into a JUNit test
        RefexInteger i = new RefexInteger(5);
        
        System.out.println(i.getDataInteger());
        System.out.println(i.getDataObject());
        System.out.println(Integer.MAX_VALUE);
        i.setDataInteger(Integer.MAX_VALUE);
        System.out.println(i.getDataInteger());
        System.out.println(i.getDataObject());
        i.setDataInteger(Integer.MIN_VALUE);
        System.out.println(Integer.MIN_VALUE);
        System.out.println(i.getDataInteger());
        System.out.println(i.getDataObject());
    }
}
