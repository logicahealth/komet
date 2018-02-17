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
package sh.isaac.model.statement;

import javafx.beans.property.SimpleObjectProperty;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.statement.InterventionResult;
import sh.isaac.model.observable.ObservableFields;

/**
 *
 * @author kec
 */
public class InterventionResultImpl extends ResultImpl implements InterventionResult {
    private final SimpleObjectProperty<ConceptChronology> status = 
            new SimpleObjectProperty<>(this, ObservableFields.INTERVENTION_RESULT_STATUS.toExternalString());

    @Override
    public ConceptChronology getStatus() {
        return status.get();
    }

    public SimpleObjectProperty<ConceptChronology> statusProperty() {
        return status;
    }

    public void setStatus(ConceptChronology status) {
        this.status.set(status);
    }
}
