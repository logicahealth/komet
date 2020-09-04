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
package sh.isaac.model.observable.commitaware;

import javafx.beans.property.SimpleObjectProperty;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;

/**
 *
 * @author kec
 */
public class CommitAwareConceptSpecificationProperty extends SimpleObjectProperty<ConceptSpecification>{
    private final CommitAwareIntegerProperty commitAwareIntegerProperty;

    public CommitAwareConceptSpecificationProperty(CommitAwareIntegerProperty commitAwareIntegerProperty) {
        super(commitAwareIntegerProperty.getBean(), commitAwareIntegerProperty.getName());
        this.commitAwareIntegerProperty = commitAwareIntegerProperty;
        if (commitAwareIntegerProperty.intValue() == 0) {
            commitAwareIntegerProperty.setValue(TermAux.UNINITIALIZED_COMPONENT_ID.getNid());
            this.setValue(TermAux.UNINITIALIZED_COMPONENT_ID);
        } else {
            this.setValue(Get.conceptSpecification(commitAwareIntegerProperty.intValue()));
        }
        
        this.commitAwareIntegerProperty.addListener((observable, oldValue, newValue) -> {
            this.set(Get.conceptSpecification(newValue.intValue()));
        });
        this.addListener((observable, oldValue, newValue) -> {
            commitAwareIntegerProperty.set(newValue.getNid());
        });
    }
 
}
