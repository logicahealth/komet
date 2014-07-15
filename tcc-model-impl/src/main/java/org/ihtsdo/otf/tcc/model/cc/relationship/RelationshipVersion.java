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

package org.ihtsdo.otf.tcc.model.cc.relationship;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RelationshipCAB;
import org.ihtsdo.otf.tcc.api.chronicle.TypedComponentVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipAnalogBI;
import org.ihtsdo.otf.tcc.model.cc.PersistentStore;
import org.ihtsdo.otf.tcc.model.cc.component.Version;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;

//~--- inner classes -------------------------------------------------------

public class RelationshipVersion extends Version<RelationshipRevision, Relationship> implements RelationshipAnalogBI<RelationshipRevision>, TypedComponentVersionBI {
    private Relationship r = null;
    
    public RelationshipVersion (){}

    public RelationshipVersion(RelationshipAnalogBI cv, Relationship r) {
        super(cv,r);
        this.r = r;
    }

    //~--- methods ----------------------------------------------------------
    @Override
    public boolean fieldsEqual(Version<RelationshipRevision, Relationship> another) {
        RelationshipVersion anotherVersion = (RelationshipVersion) another;
        if (this.getC2Nid() != anotherVersion.getC2Nid()) {
            return false;
        }
        if (this.getCharacteristicNid() != anotherVersion.getCharacteristicNid()) {
            return false;
        }
        if (this.getGroup() != anotherVersion.getGroup()) {
            return false;
        }
        if (this.getRefinabilityNid() != anotherVersion.getRefinabilityNid()) {
            return false;
        }
        if (this.getTypeNid() != anotherVersion.getTypeNid()) {
            return false;
        }
        return true;
    }

    public RelationshipRevision makeAnalog() {
        if (r != getCv()) {
            RelationshipRevision rev = (RelationshipRevision) getCv();
            return new RelationshipRevision(rev, r);
        }
        return new RelationshipRevision(r);
    }

    @Override
    public RelationshipRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
        return (RelationshipRevision) getCv().makeAnalog(status, time, authorNid, moduleNid, pathNid);
    }

    @Override
    public RelationshipCAB makeBlueprint(ViewCoordinate vc, IdDirective idDirective, RefexDirective refexDirective) throws IOException, ContradictionException, InvalidCAB {
        return getCv().makeBlueprint(vc, idDirective, refexDirective);
    }

    //~--- get methods ------------------------------------------------------
    public int getC1Nid() {
        return getEnclosingConcept().getNid();
    }

    public int getC2Nid() {
        return getCv().getDestinationNid();//TODO-AKF: Does this method need to be here? Why C2Nid and DestinationNid?
    }

    @Override
    public int getCharacteristicNid() {
        return getCv().getCharacteristicNid();
    }

    RelationshipAnalogBI getCv() {
        return (RelationshipAnalogBI) cv;
    }

    @Override
    public int getDestinationNid() {
        return getCv().getDestinationNid();
    }

    @Override
    public int getGroup() {
        return getCv().getGroup();
    }

    @Override
    public int getOriginNid() {
        return r.enclosingConceptNid;
    }

    @Override
    public Relationship getPrimordialVersion() {
        return r;
    }

    @Override
    public int getRefinabilityNid() {
        return getCv().getRefinabilityNid();
    }

    public ConceptChronicle getType() throws IOException {
        return (ConceptChronicle) PersistentStore.get().getConcept(getTypeNid());
    }

    @Override
    public int getTypeNid() {
        return getCv().getTypeNid();
    }

    @Override
    public RelationshipVersion getVersion(ViewCoordinate c) throws ContradictionException {
        return r.getVersion(c);
    }

    @Override
    public List<? extends RelationshipVersion> getVersions() {
        return r.getVersions();
    }

    @Override
    public Collection<RelationshipVersion> getVersions(ViewCoordinate c) {
        return r.getVersions(c);
    }

    @Override
    public boolean isInferred() {
        return getCv().isInferred();
    }

    @Override
    public boolean isStated() {
        return getCv().isStated();
    }

    //~--- set methods ------------------------------------------------------
    @Override
    public void setCharacteristicNid(int characteristicNid) throws PropertyVetoException {
        getCv().setCharacteristicNid(characteristicNid);
    }

    @Override
    public void setDestinationNid(int destNid) throws PropertyVetoException {
        getCv().setDestinationNid(destNid);
    }

    @Override
    public void setGroup(int group) throws PropertyVetoException {
        getCv().setGroup(group);
    }

    @Override
    public void setRefinabilityNid(int refinabilityNid) throws PropertyVetoException {
        getCv().setRefinabilityNid(refinabilityNid);
    }

    @Override
    public void setTypeNid(int typeNid) throws PropertyVetoException {
        getCv().setTypeNid(typeNid);
    }
    
}
