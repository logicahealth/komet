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
import sh.isaac.api.statement.Circumstance;
import sh.isaac.api.statement.Measure;


/**
 *
 * @author kec
 */
public class CircumstanceImpl implements Circumstance {
    private final SimpleListProperty<LogicalExpression> purposeList = new SimpleListProperty();
    private final SimpleObjectProperty<Measure> timing = new SimpleObjectProperty<>();

    @Override
    public ObservableList<LogicalExpression> getPurposeList() {
        return purposeList.get();
    }

    public SimpleListProperty<LogicalExpression> purposeListProperty() {
        return purposeList;
    }

    public void setPurposeList(ObservableList<LogicalExpression> purposeList) {
        this.purposeList.set(purposeList);
    }

    @Override
    public Measure getTiming() {
        return timing.get();
    }

    public SimpleObjectProperty<Measure> timingProperty() {
        return timing;
    }

    public void setTiming(Measure timing) {
        this.timing.set(timing);
    }
}
