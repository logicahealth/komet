/*
 * Copyright 2019 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
package sh.isaac.api.query;

import java.util.Objects;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.ManifoldCoordinate;

/**
 *
 * @author kec
 */
public class JoinProperty {
    final ConceptSpecification assemblageSpec;
    final ConceptSpecification fieldSpec;
    final ManifoldCoordinate manifold;

    public JoinProperty(ConceptSpecification assemblageSpec, ConceptSpecification fieldSpec, ManifoldCoordinate manifold) {
        this.assemblageSpec = assemblageSpec;
        this.fieldSpec = fieldSpec;
        this.manifold = manifold;
    }

    public ConceptSpecification getAssemblageSpec() {
        return assemblageSpec;
    }

    public ConceptSpecification getFieldSpec() {
        return fieldSpec;
    }

    @Override
    public String toString() {
        return manifold.getPreferredDescriptionText(assemblageSpec) + ": " + manifold.getPreferredDescriptionText(fieldSpec);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.assemblageSpec.getPrimordialUuid().hashCode();
        hash = 97 * hash + this.fieldSpec.getPrimordialUuid().hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final JoinProperty other = (JoinProperty) obj;
        if (!Objects.equals(this.assemblageSpec, other.assemblageSpec)) {
            return false;
        }
        return Objects.equals(this.fieldSpec, other.fieldSpec);
    }
    
}
