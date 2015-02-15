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

package org.ihtsdo.otf.tcc.model.cc.refex.type_nid;

import java.beans.PropertyVetoException;
import java.io.IOException;
import org.ihtsdo.otf.tcc.api.refex.type_nid.RefexNidAnalogBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid.TtkRefexUuidMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid.TtkRefexUuidRevision;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;

//~--- inner classes -------------------------------------------------------

public class NidMemberVersion extends RefexMemberVersion<NidRevision, NidMember> implements RefexNidAnalogBI<NidRevision> {

    public NidMemberVersion(RefexNidAnalogBI cv, final NidMember rm, int stamp) {
        super(cv,rm, stamp);
    }
    
    public NidMemberVersion(){
        super();
    }
    //~--- methods ----------------------------------------------------------

    @Override
    public int getNid1() {
        return getCv().getNid1();
    }

    RefexNidAnalogBI getCv() {
        return (RefexNidAnalogBI) cv;
    }

    @Override
    public TtkRefexUuidMemberChronicle getERefsetMember() throws IOException {
        return new TtkRefexUuidMemberChronicle(this);
    }

    @Override
    public TtkRefexUuidRevision getERefsetRevision() throws IOException {
        return new TtkRefexUuidRevision(this);
    }

    //~--- set methods ------------------------------------------------------
    @Override
    public void setNid1(int c1id) throws PropertyVetoException {
        getCv().setNid1(c1id);
    }
    
}
