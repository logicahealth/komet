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

import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptAttributeAB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeAnalogBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.model.cc.component.Version;

//~--- inner classes -------------------------------------------------------

public class ConceptAttributesVersion extends Version<ConceptAttributesRevision, ConceptAttributes> 
    implements ConceptAttributeAnalogBI<ConceptAttributesRevision> {

    
    public ConceptAttributesVersion(){}
    
    public ConceptAttributesVersion(ConceptAttributeAnalogBI<ConceptAttributesRevision> cv, ConceptAttributes cc, int stamp) {
        super(cv, cc, stamp);
    }

    //~--- methods ----------------------------------------------------------
    public ConceptAttributesRevision makeAnalog() {
        if (cv == cc) {
            return new ConceptAttributesRevision(((ConceptAttributes) cc), ((ConceptAttributes)cc));
        }
        return new ConceptAttributesRevision((ConceptAttributesRevision) getCv(), (ConceptAttributes) cc);
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
        return (ConceptAttributes) cc;
    }

    @Override
    public Collection<? extends RefexChronicleBI<?>> getRefexMembers(int refsetNid) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public ConceptAttributesVersion getVersion(ViewCoordinate c) throws ContradictionException {
        return ((ConceptAttributes)cc).getVersion(c);
    }

    @Override
    public List<? extends ConceptAttributesVersion> getVersions() {
        return ((ConceptAttributes)cc).getVersions();
    }

    @Override
    public List<? extends ConceptAttributesVersion> getVersionList() {
        return ((ConceptAttributes)cc).getVersionList();
    }

    @Override
    public Collection<ConceptAttributesVersion> getVersions(ViewCoordinate c) {
        return ((ConceptAttributes)cc).getVersions(c);
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
        if(cc == cv){
            buf.append(" -revision count: ").append(cc.revisions.size());
        }
        buf.append(" -defined: ").append(getCv().isDefined());
        return buf.toString();
    }
    @Override
    public int getEnclosingConceptNid() {
       return getCv().getEnclosingConceptNid();
    }    
    
        @Override
    public int getAssociatedConceptNid() {
       return getEnclosingConceptNid();
    }
    
    @Override
    public Optional<LatestVersion<ConceptAttributeVersionBI>> getLatestVersion(Class<ConceptAttributeVersionBI> type, StampCoordinate coordinate) {
       return getCv().getLatestVersion(type, coordinate);
    }

}
