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

package org.ihtsdo.otf.tcc.model.cc.media;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.MediaCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.component.Version;

//~--- inner classes -------------------------------------------------------

public class MediaVersion extends Version<MediaRevision, Media> implements MediaVersionFacade {
    private final Media m;

    public MediaVersion(MediaVersionFacade cv, final Media m) {
        super(cv,m);
        this.m = m;
    }

    //~--- methods ----------------------------------------------------------
    public MediaRevision makeAnalog() {
        if (getCv() != m) {
            return new MediaRevision((MediaRevision) getCv(), m);
        }
        return new MediaRevision(m);
    }

    @Override
    public MediaRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
        return (MediaRevision) getCv().makeAnalog(status, time, authorNid, moduleNid, pathNid);
    }

    @Override
    public boolean fieldsEqual(Version<MediaRevision,Media> another) {
        MediaVersion anotherVersion = (MediaVersion) another;
        if (!this.getFormat().equals(anotherVersion.getFormat())) {
            return false;
        }
        if (!Arrays.equals(this.getMedia(), anotherVersion.getMedia())) {
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
        return m.enclosingConceptNid;
    }

    MediaVersionFacade getCv() {
        return (MediaVersionFacade) cv;
    }

    @Override
    public MediaCAB makeBlueprint(ViewCoordinate vc, IdDirective idDirective, RefexDirective refexDirective) throws IOException, ContradictionException, InvalidCAB {
        return getCv().makeBlueprint(vc, idDirective, refexDirective);
    }

    @Override
    public String getFormat() {
        return m.format;
    }

    @Override
    public byte[] getMedia() {
        return m.image;
    }

    @Override
    public Media getPrimordialVersion() {
        return m;
    }

    @Override
    public String getTextDescription() {
        return getCv().getTextDescription();
    }

    @Override
    public int getTypeNid() {
        return getCv().getTypeNid();
    }

    @Override
    public MediaVersion getVersion(ViewCoordinate c) throws ContradictionException {
        return m.getVersion(c);
    }

    @Override
    public List<? extends MediaVersion> getVersions() {
        return m.getVersions();
    }

    @Override
    public Collection<MediaVersion> getVersions(ViewCoordinate c) {
        return m.getVersions(c);
    }

    //~--- set methods ------------------------------------------------------
    @Override
    public void setTextDescription(String name) throws PropertyVetoException {
        getCv().setTextDescription(name);
    }

    @Override
    public void setTypeNid(int type) throws PropertyVetoException {
        getCv().setTypeNid(type);
    }
    
}
