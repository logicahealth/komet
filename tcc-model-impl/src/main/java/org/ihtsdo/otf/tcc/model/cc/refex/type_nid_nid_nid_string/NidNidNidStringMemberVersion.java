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

package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid_string;

import java.io.IOException;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_string.RefexNidNidNidStringAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_string.RefexNidNidNidStringVersionBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_string.TtkRefexUuidUuidUuidStringMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_string.TtkRefexUuidUuidUuidStringRevision;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;

/**
 *
 * @author aimeefurber
 */
public class NidNidNidStringMemberVersion extends RefexMemberVersion<NidNidNidStringRevision, NidNidNidStringMember> implements RefexNidNidNidStringVersionBI<NidNidNidStringRevision> {

    NidNidNidStringMemberVersion(RefexNidNidNidStringAnalogBI cv, 
            final NidNidNidStringMember rm, int stamp) {
        super(cv,rm, stamp);
    }

    RefexNidNidNidStringAnalogBI getCv() {
        return (RefexNidNidNidStringAnalogBI) cv;
    }

    @Override
    public TtkRefexUuidUuidUuidStringMemberChronicle getERefsetMember() throws IOException {
        return new TtkRefexUuidUuidUuidStringMemberChronicle(this);
    }

    @Override
    public TtkRefexUuidUuidUuidStringRevision getERefsetRevision() throws IOException {
        return new TtkRefexUuidUuidUuidStringRevision(this);
    }

    @Override
    public String getString1() {
        return getCv().getString1();
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
