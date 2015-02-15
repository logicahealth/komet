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

package org.ihtsdo.otf.tcc.model.cc.refex.type_nid_boolean;

import java.beans.PropertyVetoException;
import java.io.IOException;
import org.ihtsdo.otf.tcc.api.refex.type_nid_boolean.RefexNidBooleanAnalogBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_boolean.TtkRefexUuidBooleanRevision;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_float.TtkRefexUuidFloatMemberChronicle;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;

/**
 * Class description
 *
 *
 * @version        Enter version here..., 13/03/27
 * @author         Enter your name here...
 */
public class NidBooleanMemberVersion extends RefexMemberVersion<NidBooleanRevision, NidBooleanMember> implements RefexNidBooleanAnalogBI<NidBooleanRevision> {

    /**
     * Constructs ...
     *
     *
     * @param cv
     */
    NidBooleanMemberVersion(RefexNidBooleanAnalogBI cv, final NidBooleanMember rm,
            int stamp) {
        super(cv, rm, stamp);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    RefexNidBooleanAnalogBI getCv() {
        return (RefexNidBooleanAnalogBI) cv;
    }

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public TtkRefexUuidFloatMemberChronicle getERefsetMember() throws IOException {
        return new TtkRefexUuidFloatMemberChronicle(this);
    }

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public TtkRefexUuidBooleanRevision getERefsetRevision() throws IOException {
        return new TtkRefexUuidBooleanRevision(this);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public boolean getBoolean1() {
        return getCv().getBoolean1();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public int getNid1() {
        return getCv().getNid1();
    }

    /**
     * Method description
     *
     *
     * @param f
     *
     * @throws PropertyVetoException
     */
    @Override
    public void setBoolean1(boolean b) throws PropertyVetoException {
        getCv().setBoolean1(b);
    }

    /**
     * Method description
     *
     *
     * @param cnid1
     *
     * @throws PropertyVetoException
     */
    @Override
    public void setNid1(int cnid1) throws PropertyVetoException {
        getCv().setNid1(cnid1);
    }
    
}
