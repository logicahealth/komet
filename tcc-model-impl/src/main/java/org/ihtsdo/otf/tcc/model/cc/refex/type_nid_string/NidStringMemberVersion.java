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

package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_string;

import java.beans.PropertyVetoException;
import java.io.IOException;
import org.ihtsdo.otf.tcc.api.refex.type_nid_string.RefexNidStringAnalogBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_string.TtkRefexUuidStringMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_string.TtkRefexUuidStringRevision;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;

//~--- inner classes -------------------------------------------------------

public class NidStringMemberVersion extends RefexMemberVersion<NidStringRevision, NidStringMember> implements RefexNidStringAnalogBI<NidStringRevision> {
    private final NidStringMember rm;

    NidStringMemberVersion(RefexNidStringAnalogBI cv, final NidStringMember rm) {
        super(cv, rm);
        this.rm = rm;
    }

    //~--- methods ----------------------------------------------------------
    //~--- get methods ------------------------------------------------------
    @Override
    public int getNid1() {
        return getCv().getNid1();
    }

    RefexNidStringAnalogBI getCv() {
        return (RefexNidStringAnalogBI) cv;
    }

    @Override
    public TtkRefexUuidStringMemberChronicle getERefsetMember() throws IOException {
        return new TtkRefexUuidStringMemberChronicle(this);
    }

    @Override
    public TtkRefexUuidStringRevision getERefsetRevision() throws IOException {
        return new TtkRefexUuidStringRevision(this);
    }

    @Override
    public String getString1() {
        return getCv().getString1();
    }
    //~--- set methods ------------------------------------------------------

    @Override
    public void setNid1(int c1id) throws PropertyVetoException {
        getCv().setNid1(c1id);
    }

    @Override
    public void setString1(String value) throws PropertyVetoException {
        getCv().setString1(value);
    }
    
}
