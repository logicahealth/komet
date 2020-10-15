/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
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
package sh.isaac.model.observable.version;

import java.util.UUID;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;
import sh.isaac.api.observable.semantic.version.ObservableSemanticVersion;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.SemanticVersionImpl;

/**
 *
 * @author kec
 */
public class ObservableSemanticVersionImpl extends ObservableAbstractSemanticVersionImpl {

    public ObservableSemanticVersionImpl(SemanticVersion stampedVersion, ObservableSemanticChronology chronology) {
        super(stampedVersion, chronology);
    }

    public ObservableSemanticVersionImpl(ObservableSemanticVersion versionToClone, ObservableSemanticChronology chronology) {
        super(versionToClone, chronology);
    }
    
    public ObservableSemanticVersionImpl(UUID primordialUuid, UUID referencedComponentUuid, int assemblageNid) {
        super(VersionType.MEMBER, primordialUuid, referencedComponentUuid, assemblageNid);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends ObservableVersion> V makeAutonomousAnalog(ManifoldCoordinate mc) {
        ObservableSemanticVersionImpl analog = new ObservableSemanticVersionImpl(this, getChronology());
        copyLocalFields(analog);
        analog.setModuleNid(mc.getModuleNidForAnalog(this));
        analog.setAuthorNid(mc.getAuthorNidForChanges());
        analog.setPathNid(mc.getPathNidForAnalog());
        return (V) analog;
    }

    @Override
    protected void updateVersion() {
        // nothing to update
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends Version> V makeAnalog(int stampSequence) {
        SemanticVersion newVersion = getStampedVersion().makeAnalog(stampSequence);
        ObservableAbstractSemanticVersionImpl newObservableVersion = new ObservableSemanticVersionImpl(newVersion, getChronology());
        getChronology().getVersionList().add(newObservableVersion);
        return (V) newObservableVersion;
    }

    @Override
    public Chronology createChronologyForCommit(int stampSequence) {
        SemanticChronologyImpl sc = new SemanticChronologyImpl(versionType, getPrimordialUuid(), getAssemblageNid(), getReferencedComponentNid());
        SemanticVersion newVersion = new SemanticVersionImpl(sc, stampSequence);
        copyLocalFields(newVersion);
        sc.addVersion(newVersion);
        return sc;
    }

   @Override
    protected void copyLocalFields(SemanticVersion analog) {
        // Nothing to copy. 
    }    
}
