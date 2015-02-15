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

package org.ihtsdo.otf.tcc.model.cc.refex.type_membership;

import java.io.IOException;
import org.ihtsdo.otf.tcc.api.refex.RefexAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_long.RefexLongAnalogBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_member.TtkRefexMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_member.TtkRefexRevision;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;

//~--- inner classes -------------------------------------------------------

public class MembershipMemberVersion extends RefexMemberVersion<MembershipRevision, MembershipMember> implements RefexAnalogBI<MembershipRevision> {

    MembershipMemberVersion(RefexAnalogBI cv, final MembershipMember rm, int stamp) {
        super(cv, rm, stamp);
    }

    //~--- methods ----------------------------------------------------------
    //~--- get methods ------------------------------------------------------
    RefexLongAnalogBI getCv() {
        return (RefexLongAnalogBI) cv;
    }

    @Override
    public TtkRefexMemberChronicle getERefsetMember() throws IOException {
        return new TtkRefexMemberChronicle(this);
    }

    @Override
    public TtkRefexRevision getERefsetRevision() throws IOException {
        return new TtkRefexRevision(this);
    }
    
}
