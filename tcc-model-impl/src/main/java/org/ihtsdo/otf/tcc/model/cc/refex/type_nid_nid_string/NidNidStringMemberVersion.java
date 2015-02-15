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

package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_string;

import java.beans.PropertyVetoException;
import java.io.IOException;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_string.RefexNidNidStringAnalogBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_string.TtkRefexUuidUuidStringMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_string.TtkRefexUuidUuidStringRevision;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;

//~--- inner classes -------------------------------------------------------

public class NidNidStringMemberVersion extends RefexMemberVersion<NidNidStringRevision, NidNidStringMember> implements RefexNidNidStringAnalogBI<NidNidStringRevision> {

    NidNidStringMemberVersion(RefexNidNidStringAnalogBI cv, 
            final NidNidStringMember rm, int stamp) {
        super(cv, rm, stamp);
    }
    //~--- methods ----------------------------------------------------------
    //~--- get methods ------------------------------------------------------

    @Override
    public int getNid1() {
        return getCv().getNid1();
    }

    @Override
    public int getNid2() {
        return getCv().getNid2();
    }

    RefexNidNidStringAnalogBI getCv() {
        return (RefexNidNidStringAnalogBI) cv;
    }

    @Override
    public TtkRefexUuidUuidStringMemberChronicle getERefsetMember() throws IOException {
        return new TtkRefexUuidUuidStringMemberChronicle(this);
    }

    @Override
    public TtkRefexUuidUuidStringRevision getERefsetRevision() throws IOException {
        return new TtkRefexUuidUuidStringRevision(this);
    }

    @Override
    public String getString1() {
        return getCv().getString1();
    }

    //~--- set methods ------------------------------------------------------
    @Override
    public void setNid1(int cnid1) throws PropertyVetoException {
        getCv().setNid1(cnid1);
    }

    @Override
    public void setNid2(int cnid2) throws PropertyVetoException {
        getCv().setNid2(cnid2);
    }

    @Override
    public void setString1(String str) throws PropertyVetoException {
        getCv().setString1(str);
    }
    
}
