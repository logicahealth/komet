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

package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_long;

import java.beans.PropertyVetoException;
import java.io.IOException;
import org.ihtsdo.otf.tcc.api.refex.type_nid_long.RefexNidLongAnalogBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_long.TtkRefexUuidLongMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_long.TtkRefexUuidLongRevision;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;

//~--- inner classes -------------------------------------------------------

public class NidLongMemberVersion extends RefexMemberVersion<NidLongRevision, NidLongMember> implements RefexNidLongAnalogBI<NidLongRevision> {

    public NidLongMemberVersion(RefexNidLongAnalogBI cv, final NidLongMember rm,
            int stamp) {
        super(cv,rm, stamp);
    }
    
    public NidLongMemberVersion(){
        super();
    }
    //~--- methods ----------------------------------------------------------

    //~--- get methods ------------------------------------------------------
    @Override
    public int getNid1() {
        return getCv().getNid1();
    }

    RefexNidLongAnalogBI getCv() {
        return (RefexNidLongAnalogBI) cv;
    }

    @Override
    public TtkRefexUuidLongMemberChronicle getERefsetMember() throws IOException {
        return new TtkRefexUuidLongMemberChronicle(this);
    }

    @Override
    public TtkRefexUuidLongRevision getERefsetRevision() throws IOException {
        return new TtkRefexUuidLongRevision(this);
    }

    @Override
    public long getLong1() {
        return getCv().getLong1();
    }

    //~--- set methods ------------------------------------------------------
    @Override
    public void setNid1(int cnid1) throws PropertyVetoException {
        getCv().setNid1(cnid1);
    }

    @Override
    public void setLong1(long l) throws PropertyVetoException {
        getCv().setLong1(l);
    }
    
}
