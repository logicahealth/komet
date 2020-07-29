/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */
package sh.isaac.model.observable.version;

//~--- non-JDK imports --------------------------------------------------------
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javafx.beans.property.Property;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.observable.concept.ObservableConceptChronology;
import sh.isaac.api.observable.concept.ObservableConceptVersion;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;
import sh.isaac.api.observable.semantic.version.ObservableSemanticVersion;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.model.VersionImpl;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.model.concept.ConceptVersionImpl;
import sh.isaac.model.observable.ObservableChronologyImpl;

//~--- classes ----------------------------------------------------------------
/**
 * The Class ObservableConceptVersionImpl.
 *
 * @author kec
 */
public class ObservableConceptVersionImpl
        extends ObservableVersionImpl
        implements ObservableConceptVersion {

    public ObservableConceptVersionImpl(UUID primordialUuid, int assemblageNid) {
        super(VersionType.CONCEPT, primordialUuid, assemblageNid);
        this.setAuthorNid(TermAux.UNINITIALIZED_COMPONENT_ID.getNid());
        this.setModuleNid(TermAux.UNINITIALIZED_COMPONENT_ID.getNid());
        this.setPathNid(TermAux.UNINITIALIZED_COMPONENT_ID.getNid());
        this.setStatus(Status.ACTIVE);
    }

    /**
     * Instantiates a new observable concept version impl.
     *
     * @param stampedVersion the stamped version
     * @param chronology the chronology
     */
    public ObservableConceptVersionImpl(ConceptVersion stampedVersion,
            ObservableConceptChronology chronology) {
        super(stampedVersion,
                chronology);
    }

    public ObservableConceptVersionImpl(ObservableSemanticVersion versionToClone, ObservableSemanticChronology chronology) {
        super(chronology);
        this.setStatus(versionToClone.getStatus());
    }

    @Override
    public <V extends ObservableVersion> V makeAutonomousAnalog(ManifoldCoordinate mc) {
        ObservableConceptVersionImpl analog = new ObservableConceptVersionImpl(this, getChronology());
        analog.setModuleNid(mc.getModuleNidForAnalog(this));
        analog.setAuthorNid(mc.getAuthorNidForChanges());
        analog.setPathNid(mc.getPathNidForAnalog(this));
        analog.setStatus(this.getStatus());
        return (V) analog;
    }

    @Override
    public <V extends Version> V setupAnalog(int stampSequence) {
        ConceptVersionImpl newVersion = new ConceptVersionImpl((ConceptChronologyImpl) this.getStampedVersion().getChronology(), stampSequence);
        ObservableConceptVersionImpl newObservableVersion
                = new ObservableConceptVersionImpl(newVersion, (ObservableConceptChronology) chronology);
        ((ObservableChronologyImpl) chronology).getVersionList().add(newObservableVersion);
        return (V) newObservableVersion;
    }

    @Override
    protected void updateVersion() {

        // nothing to update. 
    }

    @Override
    protected List<Property<?>> getEditableProperties2() {
        return new ArrayList<>();
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the chronology.
     *
     * @return the chronology
     */
    @Override
    public ObservableConceptChronology getChronology() {
        return (ObservableConceptChronology) this.chronology;
    }

    @Override
    public Chronology createIndependentChronicle() {
        ConceptChronologyImpl independentChronology
                = new ConceptChronologyImpl(this.getPrimordialUuid(), this.getAssemblageNid());
        boolean added = false;
        if (this.getChronology() != null) {
            for (Version v : this.getChronology().getVersionList()) {
                if (v == this) {
                    added = true;
                }
                independentChronology.addVersion(v);
            }
        }

        if (!added) {
            independentChronology.addVersion(this);
        }
        return independentChronology;
    }

    @Override
    public Chronology createChronologyForCommit(int stampSequence) {
        ConceptChronologyImpl independentChronology
                = new ConceptChronologyImpl(this.getPrimordialUuid(), this.getAssemblageNid());
        ConceptVersionImpl newVersion = new ConceptVersionImpl(independentChronology, stampSequence);
        independentChronology.addVersion(newVersion);
        return independentChronology;
    }

}
