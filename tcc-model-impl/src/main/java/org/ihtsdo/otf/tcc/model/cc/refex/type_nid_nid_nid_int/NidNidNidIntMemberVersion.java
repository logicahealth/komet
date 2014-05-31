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

package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid_int;

import java.io.IOException;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_int.RefexNidNidNidIntAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_int.RefexNidNidNidIntVersionBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_int.TtkRefexUuidUuidUuidIntMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_int.TtkRefexUuidUuidUuidIntRevision;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;

/**
 *
 * @author aimeefurber
 */
public class NidNidNidIntMemberVersion extends RefexMemberVersion<NidNidNidIntRevision, NidNidNidIntMember> implements RefexNidNidNidIntVersionBI<NidNidNidIntRevision> {
    private final NidNidNidIntMember rm;

    NidNidNidIntMemberVersion(RefexNidNidNidIntAnalogBI cv, final NidNidNidIntMember rm) {
        super(cv,rm);
        this.rm = rm;
    }

    RefexNidNidNidIntAnalogBI getCv() {
        return (RefexNidNidNidIntAnalogBI) cv;
    }

    @Override
    public TtkRefexUuidUuidUuidIntMemberChronicle getERefsetMember() throws IOException {
        return new TtkRefexUuidUuidUuidIntMemberChronicle(this);
    }

    @Override
    public TtkRefexUuidUuidUuidIntRevision getERefsetRevision() throws IOException {
        return new TtkRefexUuidUuidUuidIntRevision(this);
    }

    @Override
    public int getInt1() {
        return getCv().getInt1();
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
