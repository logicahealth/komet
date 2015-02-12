/*
 * Copyright 2014 International Health Terminology Standards Development Organisation.
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

package org.ihtsdo.otf.tcc.model.cc.refex.type_array_of_bytearray;

import java.beans.PropertyVetoException;
import java.io.IOException;
import org.ihtsdo.otf.tcc.api.refex.type_array_of_bytearray.RefexArrayOfBytearrayAnalogBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_array_of_bytearray.TtkRefexArrayOfByteArrayMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_array_of_bytearray.TtkRefexArrayOfByteArrayRevision;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;

//~--- set methods ---------------------------------------------------------

//~--- inner classes -------------------------------------------------------
public class ArrayOfByteArrayMemberVersion extends RefexMemberVersion<ArrayOfByteArrayRevision, ArrayOfByteArrayMember> implements RefexArrayOfBytearrayAnalogBI<ArrayOfByteArrayRevision> {

    ArrayOfByteArrayMemberVersion(RefexArrayOfBytearrayAnalogBI<ArrayOfByteArrayRevision> cv, final ArrayOfByteArrayMember rm, int stamp) {
        super(cv,rm, stamp);
    }

    //~--- methods ----------------------------------------------------------
    //~--- get methods ------------------------------------------------------
    @Override
    public byte[][] getArrayOfByteArray() {
        return getCv().getArrayOfByteArray();
    }

    RefexArrayOfBytearrayAnalogBI<ArrayOfByteArrayRevision> getCv() {
        return (RefexArrayOfBytearrayAnalogBI<ArrayOfByteArrayRevision>) cv;
    }

    @Override
    public TtkRefexArrayOfByteArrayMemberChronicle getERefsetMember() throws IOException {
        return new TtkRefexArrayOfByteArrayMemberChronicle(this);
    }

    @Override
    public TtkRefexArrayOfByteArrayRevision getERefsetRevision() throws IOException {
        return new TtkRefexArrayOfByteArrayRevision(this);
    }

    //~--- set methods ------------------------------------------------------
    @Override
    public void setArrayOfByteArray(byte[][] arrayOfByteArray) throws PropertyVetoException {
        getCv().setArrayOfByteArray(arrayOfByteArray);
    }
    
}
