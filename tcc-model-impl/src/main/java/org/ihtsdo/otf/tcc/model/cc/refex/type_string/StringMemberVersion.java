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

package org.ihtsdo.otf.tcc.model.cc.refex.type_string;

import java.beans.PropertyVetoException;
import java.io.IOException;
import org.ihtsdo.otf.tcc.api.refex.type_string.RefexStringAnalogBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_string.TtkRefexStringMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_string.TtkRefexStringRevision;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;

//~--- inner classes -------------------------------------------------------

public class StringMemberVersion extends RefexMemberVersion<StringRevision, StringMember> implements RefexStringAnalogBI<StringRevision> {
    public StringMemberVersion(RefexStringAnalogBI cv, final StringMember rm,
            int stamp) {
        super(cv, rm, stamp);
    }
    
    public StringMemberVersion(){
        super();
    }

    //~--- methods ----------------------------------------------------------
    //~--- get methods ------------------------------------------------------
    RefexStringAnalogBI getCv() {
        return (RefexStringAnalogBI) cv;
    }

    @Override
    public TtkRefexStringMemberChronicle getERefsetMember() throws IOException {
        return new TtkRefexStringMemberChronicle(this);
    }

    @Override
    public TtkRefexStringRevision getERefsetRevision() throws IOException {
        return new TtkRefexStringRevision(this);
    }

    @Override
    public String getString1() {
        return getCv().getString1();
    }

    //~--- set methods ------------------------------------------------------
    @Override
    public void setString1(String str) throws PropertyVetoException {
        getCv().setString1(str);
    }
    
}
