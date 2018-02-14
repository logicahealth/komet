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
import sh.isaac.api.statement.IdentifiedParticipant;
import sh.isaac.api.statement.PerformanceCircumstance;
import sh.isaac.api.statement.Result;

import java.util.Collection;
import java.util.List;

/**
 *
 * @author kec
 */
public class PerformanceCircumstanceImpl extends CircumstanceImpl implements PerformanceCircumstance {
    private final SimpleObjectProperty<Result> result = new SimpleObjectProperty<>();
    private final SimpleListProperty<IdentifiedParticipant> performanceParticipants = new SimpleListProperty();

    @Override
    public Result getResult() {
        return result.get();
    }

    public SimpleObjectProperty<Result> resultProperty() {
        return result;
    }

    public void setResult(Result result) {
        this.result.set(result);
    }

    @Override
    public ObservableList<IdentifiedParticipant> getPerformanceParticipants() {
        return performanceParticipants.get();
    }

    public SimpleListProperty<IdentifiedParticipant> performanceParticipantsProperty() {
        return performanceParticipants;
    }

    public void setPerformanceParticipants(List<IdentifiedParticipant> performanceParticipants) {
        this.performanceParticipants.get().setAll(performanceParticipants);
    }

}
