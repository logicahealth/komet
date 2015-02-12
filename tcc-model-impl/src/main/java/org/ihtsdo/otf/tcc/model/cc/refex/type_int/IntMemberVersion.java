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

package org.ihtsdo.otf.tcc.model.cc.refex.type_int;

import java.beans.PropertyVetoException;
import java.io.IOException;
import org.ihtsdo.otf.tcc.api.refex.type_int.RefexIntAnalogBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_int.TtkRefexIntMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_int.TtkRefexIntRevision;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;

//~--- inner classes -------------------------------------------------------

public class IntMemberVersion extends RefexMemberVersion<IntRevision, IntMember> implements RefexIntAnalogBI<IntRevision> {

    IntMemberVersion(RefexIntAnalogBI cv, final IntMember rm, int stamp) {
        super(cv,rm, stamp);
    }
    //~--- methods ----------------------------------------------------------

    //~--- get methods ------------------------------------------------------
    RefexIntAnalogBI getCv() {
        return (RefexIntAnalogBI) cv;
    }

    @Override
    public TtkRefexIntMemberChronicle getERefsetMember() throws IOException {
        return new TtkRefexIntMemberChronicle(this);
    }

    @Override
    public TtkRefexIntRevision getERefsetRevision() throws IOException {
        return new TtkRefexIntRevision(this);
    }

    @Override
    public int getInt1() {
        return getCv().getInt1();
    }

    //~--- set methods ------------------------------------------------------
    @Override
    public void setInt1(int value) throws PropertyVetoException {
        getCv().setInt1(value);
    }
    
}
