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

package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid;

import java.beans.PropertyVetoException;
import java.io.IOException;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid.RefexNidNidAnalogBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid.TtkRefexUuidUuidMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid.TtkRefexUuidUuidRevision;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;

//~--- inner classes -------------------------------------------------------

public class NidNidMemberVersion extends RefexMemberVersion<NidNidRevision, NidNidMember> implements RefexNidNidAnalogBI<NidNidRevision> {
    private final NidNidMember rm;

    NidNidMemberVersion(RefexNidNidAnalogBI cv, final NidNidMember rm) {
        super(cv,rm);
        this.rm = rm;
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

    RefexNidNidAnalogBI getCv() {
        return (RefexNidNidAnalogBI) cv;
    }

    @Override
    public TtkRefexUuidUuidMemberChronicle getERefsetMember() throws IOException {
        return new TtkRefexUuidUuidMemberChronicle(this);
    }

    @Override
    public TtkRefexUuidUuidRevision getERefsetRevision() throws IOException {
        return new TtkRefexUuidUuidRevision(this);
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
    
}
