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

package org.ihtsdo.otf.tcc.model.cc.refex.type_boolean;

import java.beans.PropertyVetoException;
import java.io.IOException;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.api.refex.type_boolean.RefexBooleanAnalogBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_boolean.TtkRefexBooleanMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_boolean.TtkRefexBooleanRevision;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;

//~--- inner classes -------------------------------------------------------

public class BooleanMemberVersion extends RefexMemberVersion<BooleanRevision, BooleanMember> implements RefexBooleanAnalogBI<BooleanRevision> {
    private final BooleanMember rm;

    BooleanMemberVersion(RefexBooleanAnalogBI<BooleanRevision> cv, final BooleanMember rm) {
        super(cv,rm);
        this.rm = rm;
    }

    //~--- methods ----------------------------------------------------------
    //~--- get methods ------------------------------------------------------
    @Override
    public boolean getBoolean1() {
        return getCv().getBoolean1();
    }

    RefexBooleanAnalogBI<BooleanRevision> getCv() {
        return (RefexBooleanAnalogBI<BooleanRevision>) cv;
    }

    @Override
    public TtkRefexBooleanMemberChronicle getERefsetMember() throws IOException {
        return new TtkRefexBooleanMemberChronicle(this);
    }

    @Override
    public TtkRefexBooleanRevision getERefsetRevision() throws IOException {
        return new TtkRefexBooleanRevision(this);
    }
    
    //~--- set methods ------------------------------------------------------
    @Override
    public void setBoolean1(boolean value) throws PropertyVetoException {
        getCv().setBoolean1(value);
    }
    
}
