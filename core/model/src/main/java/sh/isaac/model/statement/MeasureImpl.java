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

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import sh.isaac.api.statement.Measure;

import java.util.Optional;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.model.observable.ObservableFields;

/**
 *
 * @author kec
 */
public class MeasureImpl implements Measure {
    
    private final ManifoldCoordinate manifold;

    private final SimpleStringProperty narrative = 
            new SimpleStringProperty(this, ObservableFields.MEASURE_NARRATIVE.toExternalString());

    private final SimpleFloatProperty resolution = 
            new SimpleFloatProperty(this, ObservableFields.MEASURE_RESOLUTION.toExternalString());
    private final SimpleObjectProperty<ConceptChronology> measureSemantic = 
            new SimpleObjectProperty<>(this, ObservableFields.MEASURE_SEMANTIC.toExternalString());
    private final SimpleFloatProperty lowerBound =
            new SimpleFloatProperty(this, ObservableFields.INTERVAL_LOWER_BOUND.toExternalString());
    private final SimpleBooleanProperty includeLowerBound =
            new SimpleBooleanProperty(this, ObservableFields.INTERVAL_INCLUDE_UPPER_BOUND.toExternalString());
    private final SimpleFloatProperty upperBound =
            new SimpleFloatProperty(this, ObservableFields.INTERVAL_UPPER_BOUND.toExternalString());
    private final SimpleBooleanProperty includeUpperBound =
            new SimpleBooleanProperty(this, ObservableFields.INTERVAL_INCLUDE_LOWER_BOUND.toExternalString());

    public MeasureImpl(ManifoldCoordinate manifold) {
        this.manifold = manifold;
        measureSemantic.set(Get.concept(TermAux.ISO_8601));
        measureSemantic.addListener(this::measureChanged);
        includeLowerBound.setValue(true);
        includeLowerBound.addListener(this::measureChanged);
        lowerBound.setValue(System.currentTimeMillis());
        lowerBound.addListener(this::measureChanged);
        upperBound.setValue(System.currentTimeMillis() + 1000 * 60 * 60);
        upperBound.addListener(this::measureChanged);
        includeUpperBound.setValue(true);
        includeUpperBound.addListener(this::measureChanged);
        resolution.setValue(1000 * 60);
        resolution.addListener(this::measureChanged);
        updateNarrative();
        
    }
    
    private void measureChanged(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {
        updateNarrative();
    }


    /**
         *
         * @return the resolution of this measurement.
         */
    @Override
    public Optional<Float> getResolution() {
        return Optional.ofNullable(resolution.get());
    }

    public SimpleFloatProperty resolutionProperty() {
        return resolution;
    }

    public void setResolution(Float resolution) {
        this.resolution.set(resolution);
    }
    
    public String getNarrative() {
        return narrative.get();
    }

    public ReadOnlyStringProperty narrativeProperty() {
        return narrative;
    }
    
    private void updateNarrative() {
        StringBuilder builder = new StringBuilder();
        if (includeLowerBound()) {
            builder.append("[");
        } else {
            builder.append("(");
        }
        
        builder.append(getLowerBound());
        builder.append(", ");
        builder.append(getUpperBound());

        if (includeUpperBound()) {
            builder.append("]");
        } else {
            builder.append(")");
        }

        if (getResolution().isPresent()) {
            builder.append(" ± ");
            builder.append(getResolution().get());
        }
 
        builder.append(" ");
        builder.append(manifold.getPreferredDescriptionText(getMeasureSemantic()));
        narrative.setValue(builder.toString());

    }

    /**
         * In most cases, the semantics of the measurement are the units of measure.
         * @return the semantics for this measurement.
         */
    @Override
    public ConceptChronology getMeasureSemantic() {
        return measureSemantic.get();
    }

    public SimpleObjectProperty<ConceptChronology> measureSemanticProperty() {
        return measureSemantic;
    }

    public void setMeasureSemantic(ConceptChronology measureSemantic) {
        this.measureSemantic.set(measureSemantic);
    }

    /**
         *
         * @return the lower bound for this measurement
         */
    @Override
    public float getLowerBound() {
        return lowerBound.get();
    }

    public SimpleFloatProperty lowerBoundProperty() {
        return lowerBound;
    }

    public void setLowerBound(float lowerBound) {
        this.lowerBound.set(lowerBound);
    }

    /**
         *
         * @return true if the lower bound is part of the interval.
         */
    @Override
    public boolean includeLowerBound() {
        return includeLowerBound.get();
    }

    public SimpleBooleanProperty includeLowerBoundProperty() {
        return includeLowerBound;
    }

    public void setIncludeLowerBound(boolean includeLowerBound) {
        this.includeLowerBound.set(includeLowerBound);
    }

    /**
         *
         * @return the upper bound for this measurement
         */
    @Override
    public float getUpperBound() {
        return upperBound.get();
    }

    public SimpleFloatProperty upperBoundProperty() {
        return upperBound;
    }

    public void setUpperBound(float upperBound) {
        this.upperBound.set(upperBound);
    }

    /**
         *
         * @return true if the upper bound is part of the interval.
         */
    @Override
    public boolean includeUpperBound() {
        return includeUpperBound.get();
    }

    public SimpleBooleanProperty includeUpperBoundProperty() {
        return includeUpperBound;
    }

    public void setIncludeUpperBound(boolean includeUpperBound) {
        this.includeUpperBound.set(includeUpperBound);
    }
}
