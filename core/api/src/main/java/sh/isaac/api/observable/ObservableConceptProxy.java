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
package sh.isaac.api.observable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javafx.beans.property.SimpleObjectProperty;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.component.concept.ConceptSpecification;

/**
 *
 * @author kec
 */

public class ObservableConceptProxy extends SimpleObjectProperty<ConceptProxy> implements ConceptSpecification {

    public ObservableConceptProxy(Object bean, ConceptProxy conceptProxy) {
        super(bean, conceptProxy.toExternalString(), conceptProxy);
    }

    public ObservableConceptProxy(Object bean, String name, ConceptProxy conceptProxy) {
        super(bean, name, conceptProxy);
    }

    @Override
    public String getFullyQualifiedName() {
        return get().getFullyQualifiedName();
    }

    @Override
    public Optional<String> getRegularName() {
        return get().getRegularName();
    }

    @Override
    public List<UUID> getUuidList() {
        return get().getUuidList();
    }
    
    @Override
    public UUID[] getUuids() {
        return getUuidList().toArray(new UUID[0]);
    }

 }
