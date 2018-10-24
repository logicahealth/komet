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
package sh.isaac.api.component.concept;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 *
 * @author kec
 */
public class ConceptSpecificationWithLabel implements ConceptSpecification {
    final ConceptSpecification wrappedSpec;

    @Override
    public String toExternalString() {
        return wrappedSpec.toExternalString();
    }

    @Override
    public String getFullyQualifiedName() {
        return wrappedSpec.getFullyQualifiedName();
    }

    @Override
    public Optional<String> getRegularName() {
        return wrappedSpec.getRegularName();
    }

    @Override
    public void clearCache() {
        wrappedSpec.clearCache();
    }

    @Override
    public String toUserString() {
        return wrappedSpec.toUserString();
    }

    @Override
    public int getNid() throws NoSuchElementException {
        return wrappedSpec.getNid();
    }

    @Override
    public int getAssemblageNid() {
        return wrappedSpec.getAssemblageNid();
    }

    @Override
    public UUID getPrimordialUuid() {
        return wrappedSpec.getPrimordialUuid();
    }

    @Override
    public List<UUID> getUuidList() {
        return wrappedSpec.getUuidList();
    }

    @Override
    public UUID[] getUuids() {
        return wrappedSpec.getUuids();
    }

    @Override
    public boolean isIdentifiedBy(UUID uuid) {
        return wrappedSpec.isIdentifiedBy(uuid);
    }
    final String label;

    public ConceptSpecificationWithLabel(ConceptSpecification wrappedSpec, String label) {
        this.wrappedSpec = wrappedSpec;
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.wrappedSpec);
        hash = 59 * hash + Objects.hashCode(this.label);
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
        final ConceptSpecificationWithLabel other = (ConceptSpecificationWithLabel) obj;
        if (!Objects.equals(this.label, other.label)) {
            return false;
        }
        if (!Objects.equals(this.wrappedSpec, other.wrappedSpec)) {
            return false;
        }
        return true;
    }
    
    
}
