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

package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid_long;

import java.io.IOException;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_long.RefexNidNidNidLongAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_long.RefexNidNidNidLongVersionBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_long.TtkRefexUuidUuidUuidLongMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_long.TtkRefexUuidUuidUuidLongRevision;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;

/**
 *
 * @author aimeefurber
 */
public class NidNidNidLongMemberVersion extends RefexMemberVersion<NidNidNidLongRevision, NidNidNidLongMember> implements RefexNidNidNidLongVersionBI<NidNidNidLongRevision> {

    NidNidNidLongMemberVersion(RefexNidNidNidLongAnalogBI cv, 
            final NidNidNidLongMember rm, int stamp) {
        super(cv,rm, stamp);
    }

    RefexNidNidNidLongAnalogBI getCv() {
        return (RefexNidNidNidLongAnalogBI) cv;
    }

    @Override
    public TtkRefexUuidUuidUuidLongMemberChronicle getERefsetMember() throws IOException {
        return new TtkRefexUuidUuidUuidLongMemberChronicle(this);
    }

    @Override
    public TtkRefexUuidUuidUuidLongRevision getERefsetRevision() throws IOException {
        return new TtkRefexUuidUuidUuidLongRevision(this);
    }

    @Override
    public long getLong1() {
        return getCv().getLong1();
    }

    @Override
    public int getNid1() {
        return getCv().getNid1();
    }

    @Override
    public int getNid2() {
        return getCv().getNid2();
    }

    @Override
    public int getNid3() {
        return getCv().getNid3();
    }
    
}
