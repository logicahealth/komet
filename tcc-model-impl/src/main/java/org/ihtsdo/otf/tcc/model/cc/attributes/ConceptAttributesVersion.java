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

package org.ihtsdo.otf.tcc.model.cc.attributes;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptAttributeAB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeAnalogBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.component.Version;

//~--- inner classes -------------------------------------------------------

public class ConceptAttributesVersion extends Version<ConceptAttributesRevision, ConceptAttributes> implements ConceptAttributeAnalogBI<ConceptAttributesRevision> {
    private ConceptAttributes ca = null; //TODO-AKF: this can't be final if using a no arg constructor, will have to set it agian later
    
//    TODO-AKF: Is this needed?
//    public ConceptAttributesVersion(final ConceptAttributes<ConceptAttributesRevision, ConceptAttributes> ca) {
//        this.ca = ca;
//    }
    public ConceptAttributesVersion(){}
    
    public ConceptAttributesVersion(ConceptAttributeAnalogBI<ConceptAttributesRevision> cv, final ConceptAttributes ca) {
        super(cv, ca); //TODO-AKF: this is correct?
        this.ca = ca;
    }

    //~--- methods ----------------------------------------------------------
    public ConceptAttributesRevision makeAnalog() {
        if (cv == ca) {
            return new ConceptAttributesRevision(ca, ca);
        }
        return new ConceptAttributesRevision((ConceptAttributesRevision) getCv(), ca);
    }

    @Override
    public ConceptAttributesRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
        return getCv().makeAnalog(status, time, authorNid, moduleNid, pathNid);
    }

    @Override
    public boolean fieldsEqual(Version<ConceptAttributesRevision, ConceptAttributes> another) {
        ConceptAttributesVersion anotherVersion = (ConceptAttributesVersion) another;
        if (this.isDefined() == anotherVersion.isDefined()) {
            return true;
        }
        return false;
    }

    public ConceptAttributeAnalogBI<ConceptAttributesRevision> getCv() {
        return (ConceptAttributeAnalogBI<ConceptAttributesRevision>) cv;
    }

    @Override
    public ConceptAttributeAB makeBlueprint(ViewCoordinate vc, IdDirective idDirective, RefexDirective refexDirective) throws IOException, ContradictionException, InvalidCAB {
        return getCv().makeBlueprint(vc, idDirective, refexDirective);
    }

    @Override
    public ConceptAttributes getPrimordialVersion() {
        return ca;
    }

    @Override
    public Collection<? extends RefexChronicleBI<?>> getRefexMembers(int refsetNid) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IntArrayList getVariableVersionNids() {
        return new IntArrayList(2);
    }

    @Override
    public ConceptAttributesVersion getVersion(ViewCoordinate c) throws ContradictionException {
        return ca.getVersion(c);
    }

    @Override
    public List<? extends ConceptAttributesVersion> getVersions() {
        return ca.getVersions();
    }

    @Override
    public Collection<ConceptAttributesVersion> getVersions(ViewCoordinate c) {
        return ca.getVersions(c);
    }

    @Override
    public boolean hasCurrentRefexMember(ViewCoordinate xyz, int refsetNid) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isDefined() {
        return getCv().isDefined();
    }

    //~--- set methods ------------------------------------------------------
    @Override
    public void setDefined(boolean defined) throws PropertyVetoException {
        getCv().setDefined(defined);
    }
    
        public String toSimpleString(){
        StringBuilder buf = new StringBuilder();
        buf.append(" -nid: ").append(getCv().getNid());
        buf.append(" -enclosing concept nid: ").append(getCv().getConceptNid());
        buf.append(" -stamp: ").append(getCv().getStamp());
        if(ca == cv){
            buf.append(" -revision count: ").append(ca.revisions.size());
        }
        buf.append(" -defined: ").append(getCv().isDefined());
        return buf.toString();
    }
    
}
