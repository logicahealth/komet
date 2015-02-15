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

package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid;

import java.io.IOException;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid.RefexNidNidNidAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid.RefexNidNidNidVersionBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid.TtkRefexUuidUuidUuidMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid.TtkRefexUuidUuidUuidRevision;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;

//~--- inner classes -------------------------------------------------------

public class NidNidNidMemberVersion extends RefexMemberVersion<NidNidNidRevision, NidNidNidMember> implements RefexNidNidNidVersionBI<NidNidNidRevision> {

    NidNidNidMemberVersion(RefexNidNidNidAnalogBI cv, final NidNidNidMember rm,
            int stamp) {
        super(cv,rm, stamp);
    }

    RefexNidNidNidAnalogBI getCv() {
        return (RefexNidNidNidAnalogBI) cv;
    }

    @Override
    public TtkRefexUuidUuidUuidMemberChronicle getERefsetMember() throws IOException {
        return new TtkRefexUuidUuidUuidMemberChronicle(this);
    }

    @Override
    public TtkRefexUuidUuidUuidRevision getERefsetRevision() throws IOException {
        return new TtkRefexUuidUuidUuidRevision(this);
    }

    //~--- set methods ------------------------------------------------------
    @Override
    public int getNid3() {
        return getCv().getNid3();
    }

    @Override
    public int getNid2() {
        return getCv().getNid2();
    }

    @Override
    public int getNid1() {
        return getCv().getNid1();
    }
    
}
