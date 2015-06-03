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

package org.ihtsdo.otf.tcc.model.cc.description;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.ihtsdo.otf.tcc.api.blueprint.DescriptionCAB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.chronicle.TypedComponentVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionAnalogBI;
import org.ihtsdo.otf.tcc.model.cc.component.Version;

//~--- inner classes -------------------------------------------------------

public class DescriptionVersion extends Version<DescriptionRevision, Description> implements DescriptionAnalogBI<DescriptionRevision>, TypedComponentVersionBI {
 
    public DescriptionVersion(){}
    
    public DescriptionVersion(DescriptionAnalogBI<DescriptionRevision> cv, Description d, int stamp) {
        super(cv,d, stamp);
    }

    //~--- methods ----------------------------------------------------------
    public DescriptionRevision makeAnalog() {
        if (cc == cv) {
            return new DescriptionRevision((Description) cc);
        }
        return new DescriptionRevision((DescriptionRevision) cv, (Description) cc);
    }

    @Override
    public DescriptionRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
        return getCv().makeAnalog(status, time, authorNid, moduleNid, pathNid);
    }

    @Override
    public boolean fieldsEqual(Version<DescriptionRevision, Description> another) {
        DescriptionVersion anotherVersion = (DescriptionVersion) another;
        if (this.isInitialCaseSignificant() != anotherVersion.isInitialCaseSignificant()) {
            return false;
        }
        if (!this.getText().equals(anotherVersion.getText())) {
            return false;
        }
        if (!this.getLang().equals(anotherVersion.getLang())) {
            return false;
        }
        if (this.getTypeNid() != anotherVersion.getTypeNid()) {
            return false;
        }
        return true;
    }

    //~--- get methods ------------------------------------------------------
    @Override
    public int getConceptNid() {
        return cc.enclosingConceptNid;
    }

    public DescriptionAnalogBI<DescriptionRevision> getCv() {
        return (DescriptionAnalogBI<DescriptionRevision>) cv;
    }

    @Override
    public DescriptionCAB makeBlueprint(ViewCoordinate vc, IdDirective idDirective, RefexDirective refexDirective) throws IOException, ContradictionException, InvalidCAB {
        return getCv().makeBlueprint(vc, idDirective, refexDirective);
    }

    @Override
    public String getLang() {
        return getCv().getLang();
    }

    @Override
    public Description getPrimordialVersion() {
        return (Description) cc;
    }

    @Override
    public String getText() {
        return getCv().getText();
    }

    @Override
    public int getTypeNid() {
        return getCv().getTypeNid();
    }

    @Override
    public Optional<DescriptionVersion> getVersion(ViewCoordinate c) throws ContradictionException {
        return ((Description)cc).getVersion(c);
    }

    @Override
    public List<? extends DescriptionVersion> getVersions() {
        return ((Description)cc).getVersions();
    }

    @Override
    public List<? extends DescriptionVersion> getVersionList() {
        return ((Description)cc).getVersionList();
    }

    @Override
    public Collection<DescriptionVersion> getVersions(ViewCoordinate c) {
        return ((Description)cc).getVersions(c);
    }

    @Override
    public boolean isInitialCaseSignificant() {
        return getCv().isInitialCaseSignificant();
    }

    //~--- set methods ------------------------------------------------------
    @Override
    public void setInitialCaseSignificant(boolean capStatus) throws PropertyVetoException {
        getCv().setInitialCaseSignificant(capStatus);
    }

    @Override
    public void setLang(String lang) throws PropertyVetoException {
        getCv().setLang(lang);
    }

    @Override
    public void setText(String text) throws PropertyVetoException {
        getCv().setText(text);
    }

    @Override
    public void setTypeNid(int typeNid) throws PropertyVetoException {
        getCv().setTypeNid(typeNid);
    }

    @Override
    public boolean matches(Pattern p) {
        return getCv().matches(p);
    }
    
}
