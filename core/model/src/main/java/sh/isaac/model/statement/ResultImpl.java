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
import sh.isaac.api.statement.Measure;
import sh.isaac.api.statement.Result;
import sh.isaac.model.observable.ObservableFields;

import java.util.Optional;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.ManifoldCoordinate;

/**
 *
 * @author kec
 */
public class ResultImpl implements Result {

    private final ManifoldCoordinate manifold;

    private final SimpleObjectProperty<Measure> measure =
            new SimpleObjectProperty<>(this, ObservableFields.MEASURE_NORMAL_RANGE.toExternalString());

    private final SimpleObjectProperty<Measure> normalRange =
            new SimpleObjectProperty<>(this, ObservableFields.MEASURE_NORMAL_RANGE.toExternalString());

    public ResultImpl(ResultImpl result, ManifoldCoordinate manifold) {
        this.manifold = manifold;
        this.measure.set(result.getMeasure());
        this.normalRange.set(result.getNormalRange().get());
    }

    public ResultImpl(MeasureImpl measure, ManifoldCoordinate manifold) {
        this.manifold = manifold;
        this.measure.set(measure);
    }

    public ResultImpl(ManifoldCoordinate manifold) {
        this.manifold = manifold;
        this.measure.set(new MeasureImpl(manifold));
    }

    public Measure getMeasure() {
        return measure.get();
    }
    
    public void setMeasure(Measure measure) {
        this.measure.set(measure);
    }
    public SimpleObjectProperty<Measure> measureProperty() {
        return measure;
    }
    
    @Override
    public Optional<Measure> getNormalRange() {
        return Optional.ofNullable(normalRange.get());
    }

    public SimpleObjectProperty<Measure> normalRangeProperty() {
        return normalRange;
    }

    public void setNormalRange(Measure normalRange) {
        this.normalRange.set(normalRange);
    }

    
    @Override
    public Optional<Double> getResolution() {
        return measure.get().getResolution();
    }

    @Override
    public double getLowerBound() {
        return measure.get().getLowerBound();
    }

    @Override
    public double getUpperBound() {
        return measure.get().getUpperBound();
    }

    @Override
    public boolean includeLowerBound() {
        return measure.get().includeLowerBound();
    }

    @Override
    public boolean includeUpperBound() {
        return measure.get().includeUpperBound();
    }

    @Override
    public ConceptSpecification getMeasureSemantic() {
        return measure.get().getMeasureSemantic();
    }
}
