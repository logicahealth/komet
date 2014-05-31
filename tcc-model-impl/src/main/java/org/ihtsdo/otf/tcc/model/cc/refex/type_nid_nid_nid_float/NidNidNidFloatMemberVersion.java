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

package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid_float;

import java.io.IOException;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_float.RefexNidNidNidFloatAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_float.RefexNidNidNidFloatVersionBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_float.TtkRefexUuidUuidUuidFloatMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_float.TtkRefexUuidUuidUuidFloatRevision;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;

/**
 *
 * @author aimeefurber
 */
public class NidNidNidFloatMemberVersion extends RefexMemberVersion<NidNidNidFloatRevision, NidNidNidFloatMember> implements RefexNidNidNidFloatVersionBI<NidNidNidFloatRevision> {
    private final NidNidNidFloatMember rm;

    NidNidNidFloatMemberVersion(RefexNidNidNidFloatAnalogBI cv, final NidNidNidFloatMember rm) {
        super(cv,rm);
        this.rm = rm;
    }

    RefexNidNidNidFloatAnalogBI getCv() {
        return (RefexNidNidNidFloatAnalogBI) cv;
    }

    @Override
    public TtkRefexUuidUuidUuidFloatMemberChronicle getERefsetMember() throws IOException {
        return new TtkRefexUuidUuidUuidFloatMemberChronicle(this);
    }

    @Override
    public TtkRefexUuidUuidUuidFloatRevision getERefsetRevision() throws IOException {
        return new TtkRefexUuidUuidUuidFloatRevision(this);
    }

    @Override
    public float getFloat1() {
        return getCv().getFloat1();
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
