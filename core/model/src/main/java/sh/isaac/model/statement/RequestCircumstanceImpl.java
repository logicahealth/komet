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

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.statement.*;

import java.util.List;
import sh.isaac.model.observable.ObservableFields;

/**
 *
 * @author kec
 */
public class RequestCircumstanceImpl extends CircumstanceImpl implements RequestCircumstance {
    private final SimpleListProperty<ClinicalStatement> conditionalTriggers = 
            new SimpleListProperty(this, ObservableFields.REQUEST_CIRCUMSTANCE_CONDITIONAL_TRIGGERS.toExternalString());
    private final SimpleListProperty<Participant> requestedParticipants = 
            new SimpleListProperty(this, ObservableFields.REQUEST_CIRCUMSTANCE_REQUESTED_PARTICIPANTS.toExternalString());
    private final SimpleObjectProperty<LogicalExpression> priority = 
            new SimpleObjectProperty<>(this, ObservableFields.REQUEST_CIRCUMSTANCE_PRIORITY.toExternalString());
    private final SimpleListProperty<Repetition> repetitions = 
            new SimpleListProperty(this, ObservableFields.REQUEST_CIRCUMSTANCE_REPETITIONS.toExternalString());
    private final SimpleObjectProperty<Result> requestedResult = 
            new SimpleObjectProperty<>(this, ObservableFields.REQUEST_CIRCUMSTANCE_REQUESTED_RESULT.toExternalString());

    @Override
    public ObservableList<ClinicalStatement> getConditionalTriggers() {
        return conditionalTriggers.get();
    }

    public SimpleListProperty<ClinicalStatement> conditionalTriggersProperty() {
        return conditionalTriggers;
    }

    public void setConditionalTriggers(List<ClinicalStatement> conditionalTriggers) {
        this.conditionalTriggers.get().setAll(conditionalTriggers);
    }

    @Override
    public ObservableList<Participant> getRequestedParticipants() {
        return requestedParticipants.get();
    }

    public SimpleListProperty<? extends Participant> requestedParticipantsProperty() {
        return requestedParticipants;
    }

    public void setRequestedParticipants(List<? extends Participant> requestedParticipants) {
        this.requestedParticipants.get().setAll(requestedParticipants);
    }

    @Override
    public LogicalExpression getPriority() {
        return priority.get();
    }

    public SimpleObjectProperty<LogicalExpression> priorityProperty() {
        return priority;
    }

    public void setPriority(LogicalExpression priority) {
        this.priority.set(priority);
    }

    @Override
    public ObservableList<Repetition> getRepetitions() {
        return repetitions.get();
    }

    public SimpleListProperty<Repetition> repetitionsProperty() {
        return repetitions;
    }

    public void setRepetitions(ObservableList<Repetition> repetitions) {
        this.repetitions.set(repetitions);
    }

    @Override
    public Result getRequestedResult() {
        return requestedResult.get();
    }

    public SimpleObjectProperty<Result> requestedResultProperty() {
        return requestedResult;
    }

    public void setRequestedResult(Result requestedResult) {
        this.requestedResult.set(requestedResult);
    }
}
