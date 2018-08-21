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
import javafx.scene.control.MenuItem;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class ConceptMenuItem extends MenuItem implements ConceptSpecification {

    private final Manifold manifold;
    private final ConceptSpecification spec;

    //~--- constructors --------------------------------------------------------
    public ConceptMenuItem(ConceptSpecification spec, Manifold manifold) {
        super(manifold.getPreferredDescriptionText(spec));
        this.manifold = manifold;
        this.spec = spec;
    }

    public ConceptMenuItem(ConceptSpecificationForControlWrapper spec) {
        super(spec.getManifold().getPreferredDescriptionText(spec));
        this.manifold = spec.getManifold();
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

    //~--- get methods ---------------------------------------------------------
    @Override
    public int getNid() {
        return this.spec.getNid();
    }

    @Override
    public String getFullyQualifiedName() {
        return this.manifold.getFullySpecifiedDescriptionText(this.spec);
    }

    @Override
    public Optional<String> getRegularName() {
        return Optional.of(this.manifold.getPreferredDescriptionText(this.spec));
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

    public ConceptSpecification getSpec() {
        return spec;
    }

    public Manifold getManifold() {
        return manifold;
    }
    
    
}
