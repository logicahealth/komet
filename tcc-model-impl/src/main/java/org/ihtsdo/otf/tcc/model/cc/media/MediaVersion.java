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

import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.MediaCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.media.MediaVersionBI;
import org.ihtsdo.otf.tcc.model.cc.component.Version;

//~--- inner classes -------------------------------------------------------

public class MediaVersion extends Version<MediaRevision, Media> implements MediaVersionFacade {
   
    public MediaVersion(MediaVersionFacade cv, final Media m, int stamp) {
        super(cv,m, stamp);
    }

    //~--- methods ----------------------------------------------------------
    public MediaRevision makeAnalog() {
        if (getCv() != cc) {
            return new MediaRevision((MediaRevision) getCv(), (Media) cc);
        }
        return new MediaRevision((Media) cc);
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
        return cc.enclosingConceptNid;
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
        return ((Media) cc).format;
    }

    @Override
    public byte[] getMedia() {
        return ((Media) cc).image;
    }

    @Override
    public Media getPrimordialVersion() {
        return ((Media) cc);
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
    public Optional<MediaVersion> getVersion(ViewCoordinate c) throws ContradictionException {
        return ((Media) cc).getVersion(c);
    }

    @Override
    public List<? extends MediaVersion> getVersions() {
        return ((Media) cc).getVersions();
    }
    @Override
    public List<? extends MediaVersion> getVersionList() {
        return ((Media) cc).getVersions();
    }

    @Override
    public Collection<MediaVersion> getVersions(ViewCoordinate c) {
        return ((Media) cc).getVersions(c);
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

    @Override
    public Optional<LatestVersion<MediaVersionBI>> getLatestVersion(Class<MediaVersionBI> type, StampCoordinate<?> coordinate) {
        return this.getCv().getLatestVersion(type, coordinate);
    }
    
}
