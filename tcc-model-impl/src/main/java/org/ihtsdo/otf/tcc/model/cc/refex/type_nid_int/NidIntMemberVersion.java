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

package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_int;

import java.beans.PropertyVetoException;
import java.io.IOException;
import org.ihtsdo.otf.tcc.api.refex.type_nid_int.RefexNidIntAnalogBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_int.TtkRefexUuidIntMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_int.TtkRefexUuidIntRevision;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;

//~--- inner classes -------------------------------------------------------

public class NidIntMemberVersion extends RefexMemberVersion<NidIntRevision, NidIntMember> implements RefexNidIntAnalogBI<NidIntRevision> {
    private NidIntMember rm;

    public NidIntMemberVersion(RefexNidIntAnalogBI cv, final NidIntMember rm) {
        super(cv,rm);
        this.rm = rm;
    }
    
    public NidIntMemberVersion(){
    }
    
    //~--- methods ----------------------------------------------------------
    //~--- get methods ------------------------------------------------------

    @Override
    public int getNid1() {
        return getCv().getNid1();
    }

    RefexNidIntAnalogBI getCv() {
        return (RefexNidIntAnalogBI) cv;
    }

    @Override
    public TtkRefexUuidIntMemberChronicle getERefsetMember() throws IOException {
        return new TtkRefexUuidIntMemberChronicle(this);
    }

    @Override
    public TtkRefexUuidIntRevision getERefsetRevision() throws IOException {
        return new TtkRefexUuidIntRevision(this);
    }

    @Override
    public int getInt1() {
        return getCv().getInt1();
    }

    //~--- set methods ------------------------------------------------------
    @Override
    public void setNid1(int cnid1) throws PropertyVetoException {
        getCv().setNid1(cnid1);
    }

    @Override
    public void setInt1(int i) throws PropertyVetoException {
        getCv().setInt1(i);
    }
    
}
