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

package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_float;

import java.beans.PropertyVetoException;
import java.io.IOException;
import org.ihtsdo.otf.tcc.api.refex.type_nid_float.RefexNidFloatAnalogBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_float.TtkRefexUuidFloatMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_float.TtkRefexUuidFloatRevision;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;

//~--- inner classes -------------------------------------------------------

public class NidFloatMemberVersion extends RefexMemberVersion<NidFloatRevision, NidFloatMember> implements RefexNidFloatAnalogBI<NidFloatRevision> {
    private final NidFloatMember rm;

    NidFloatMemberVersion(RefexNidFloatAnalogBI cv, final NidFloatMember rm) {
        super(cv, rm);
        this.rm = rm;
    }

    //~--- methods ----------------------------------------------------------
    //~--- get methods ------------------------------------------------------
    @Override
    public int getNid1() {
        return getCv().getNid1();
    }

    RefexNidFloatAnalogBI getCv() {
        return (RefexNidFloatAnalogBI) cv;
    }

    @Override
    public TtkRefexUuidFloatMemberChronicle getERefsetMember() throws IOException {
        return new TtkRefexUuidFloatMemberChronicle(this);
    }

    @Override
    public TtkRefexUuidFloatRevision getERefsetRevision() throws IOException {
        return new TtkRefexUuidFloatRevision(this);
    }

    @Override
    public float getFloat1() {
        return getCv().getFloat1();
    }

    @Override
    public void setNid1(int cnid1) throws PropertyVetoException {
        getCv().setNid1(cnid1);
    }

    @Override
    public void setFloat1(float f) throws PropertyVetoException {
        getCv().setFloat1(f);
    }
    
}
