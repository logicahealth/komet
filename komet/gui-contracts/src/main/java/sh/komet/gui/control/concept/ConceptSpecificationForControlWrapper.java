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
package sh.komet.gui.control.concept;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.komet.gui.control.property.ViewProperties;

/**
 *
 * @author kec
 */
public class ConceptSpecificationForControlWrapper
        implements ConceptSpecification {

    private final ManifoldCoordinate manifoldCoordinate;
    private final ConceptSpecification spec;
    
    private static final Logger LOG = LogManager.getLogger();

    //~--- constructors --------------------------------------------------------
    public ConceptSpecificationForControlWrapper(ConceptSpecification spec, ManifoldCoordinate manifoldCoordinate) {
        if (spec == null) {
            spec = MetaData.UNINITIALIZED_COMPONENT____SOLOR;
        }
        this.manifoldCoordinate = manifoldCoordinate;
        this.spec = spec;
    }

    //~--- methods -------------------------------------------------------------
    @Override
    public String toString() {
        Optional<String> description = getRegularName();

        if (description.isPresent()) {
            return description.get();
        }

        return "unspecified";
    }
    public ManifoldCoordinate getManifoldCoordinate() {
        return manifoldCoordinate;
    }

    //~--- get methods ---------------------------------------------------------
    public ConceptSpecification getSpec() {
        return spec;
    }

    @Override
    public int getNid() {
        if (this.spec == null) {
            LOG.error("Opps. This.spec is null...");
        }
        return this.spec.getNid();
    }

    @Override
    public String getFullyQualifiedName() {
        return this.manifoldCoordinate.getFullyQualifiedDescriptionText(this.spec);
    }

    @Override
    public Optional<String> getRegularName() {
        return Optional.of(this.manifoldCoordinate.getPreferredDescriptionText(this.spec));
    }

    @Override
    public List<UUID> getUuidList() {
        return this.spec.getUuidList();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConceptSpecification) {
            return this.spec.equals(obj);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.spec.hashCode();
    }
}
