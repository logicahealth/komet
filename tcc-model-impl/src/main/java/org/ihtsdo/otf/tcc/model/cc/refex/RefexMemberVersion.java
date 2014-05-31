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

package org.ihtsdo.otf.tcc.model.cc.refex;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.dto.component.TtkRevision;
import org.ihtsdo.otf.tcc.dto.component.refex.TtkRefexAbstractMemberChronicle;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.component.Version;

//~--- inner classes -------------------------------------------------------
public class RefexMemberVersion<R extends RefexRevision<R, C>, C extends RefexMember<R, C>> 
    extends Version<R, C> implements RefexAnalogBI<R> {
    private final RefexMember rm;

    public RefexMemberVersion(RefexAnalogBI<R> cv, final RefexMember<R,C> rm) {
        super(cv,rm); //TODO_AKF: is this correct?
        this.rm = rm;
    }

    //~--- methods ----------------------------------------------------------
    @Override
    public RefexType getRefexType() {
        return rm.getRefexType();
    }

    public R makeAnalog() {
        if (rm != cv) {
        }
        return (R) rm.makeAnalog();
    }

    @Override
    public R makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
        return getCv().makeAnalog(status, time, authorNid, moduleNid, pathNid);
    }

    @Override
    public boolean fieldsEqual(Version<R,C> another) { //TODO-AKF: is this okay?
        RefexMemberVersion anotherVersion = (RefexMemberVersion) another;
        if (this.getTypeNid() != anotherVersion.getTypeNid()) {
            return false;
        }
        if (this.getAssemblageNid() != anotherVersion.getAssemblageNid()) {
            return false;
        }
        if (this.getReferencedComponentNid() != anotherVersion.getReferencedComponentNid()) {
            return false;
        }
        if (this.refexFieldsEqual(anotherVersion)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean refexFieldsEqual(RefexVersionBI another) {
        return getCv().refexFieldsEqual(another);
    }

    //~--- get methods ------------------------------------------------------
    @Override
    public int getAssemblageNid() {
        return rm.assemblageNid;
    }

    @Override
    @Deprecated
    public int getRefexExtensionNid() {
        return getAssemblageNid();
    }

    RefexAnalogBI<R> getCv() {
        return (RefexAnalogBI<R>) cv;
    }

    public TtkRefexAbstractMemberChronicle<?> getERefsetMember() throws IOException {
        throw new UnsupportedOperationException("subclass must override");
    }

    public TtkRevision getERefsetRevision() throws IOException {
        throw new UnsupportedOperationException("subclass must override");
    }

    @Override
    public RefexMember getPrimordialVersion() {
        return rm;
    }

    @Override
    public int getReferencedComponentNid() {
        return rm.getReferencedComponentNid();
    }

    @Override
    public RefexCAB makeBlueprint(ViewCoordinate vc, IdDirective idDirective, RefexDirective refexDirective) throws IOException, InvalidCAB, ContradictionException {
        return getCv().makeBlueprint(vc, idDirective, refexDirective);
    }

    public int getTypeNid() {
        return rm.getTypeNid();
    }

    @Override
    public IntArrayList getVariableVersionNids() {
        if (rm != getCv()) {
            return ((RefexRevision) getCv()).getVariableVersionNids();
        } else {
            return rm.getVariableVersionNids();
        }
    }

    @Override
    public RefexMemberVersion<R,C> getVersion(ViewCoordinate c) throws ContradictionException {
        return rm.getVersion(c);
    }

    @Override
    public List<? extends RefexMemberVersion<R,C>> getVersions() {
        return rm.getVersions();
    }

    @Override
    public Collection<RefexMemberVersion<R, C>> getVersions(ViewCoordinate c) {
        return rm.getVersions(c);
    }

    //~--- set methods ------------------------------------------------------
    @Override
    public void setAssemblageNid(int collectionNid) throws PropertyVetoException, IOException {
        rm.setAssemblageNid(collectionNid);
    }

    @Override
    @Deprecated
    public void setRefexExtensionNid(int collectionNid) throws PropertyVetoException, IOException {
        setAssemblageNid(collectionNid);
    }

    @Override
    public void setReferencedComponentNid(int componentNid) throws PropertyVetoException, IOException {
        rm.setReferencedComponentNid(componentNid);
    }
    
}
