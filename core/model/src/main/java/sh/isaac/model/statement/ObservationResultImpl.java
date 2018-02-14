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
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.statement.Interval;
import sh.isaac.api.statement.ObservationResult;

import java.util.Optional;

/**
 *
 * @author kec
 */
public class ObservationResultImpl extends ResultImpl implements ObservationResult {
    private final SimpleObjectProperty<LogicalExpression> healthRisk = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Interval> normalRange = new SimpleObjectProperty<>();

    @Override
    public Optional<LogicalExpression> getHealthRisk() {
        return Optional.ofNullable(healthRisk.get());
    }

    public SimpleObjectProperty<LogicalExpression> healthRiskProperty() {
        return healthRisk;
    }

    public void setHealthRisk(LogicalExpression healthRisk) {
        this.healthRisk.set(healthRisk);
    }

    @Override
    public Optional<Interval> getNormalRange() {
        return Optional.ofNullable(normalRange.get());
    }

    public SimpleObjectProperty<Interval> normalRangeProperty() {
        return normalRange;
    }

    public void setNormalRange(Interval normalRange) {
        this.normalRange.set(normalRange);
    }
}
