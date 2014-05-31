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

package org.ihtsdo.otf.tcc.model.cc.refex.type_long;

import java.beans.PropertyVetoException;
import java.io.IOException;
import org.ihtsdo.otf.tcc.api.refex.type_long.RefexLongAnalogBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_long.TtkRefexLongMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_long.TtkRefexLongRevision;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;

//~--- inner classes -------------------------------------------------------

public class LongMemberVersion extends RefexMemberVersion<LongRevision, LongMember> implements RefexLongAnalogBI<LongRevision> {
    private final LongMember rm;

    LongMemberVersion(RefexLongAnalogBI cv, final LongMember rm) {
        super(cv, rm);
        this.rm = rm;
    }

    //~--- methods ----------------------------------------------------------
    //~--- get methods ------------------------------------------------------
    RefexLongAnalogBI getCv() {
        return (RefexLongAnalogBI) cv;
    }

    @Override
    public TtkRefexLongMemberChronicle getERefsetMember() throws IOException {
        return new TtkRefexLongMemberChronicle(this);
    }

    @Override
    public TtkRefexLongRevision getERefsetRevision() throws IOException {
        return new TtkRefexLongRevision(this);
    }

    @Override
    public long getLong1() {
        return getCv().getLong1();
    }

    //~--- set methods ------------------------------------------------------
    @Override
    public void setLong1(long l) throws PropertyVetoException {
        getCv().setLong1(l);
    }
    
}
