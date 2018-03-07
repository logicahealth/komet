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

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.ZonedDateTime;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import sh.isaac.api.statement.Measure;

import java.util.Optional;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.util.time.DateTimeUtil;
import sh.isaac.api.util.time.DurationUtil;
import sh.isaac.model.observable.ObservableFields;

/**
 *
 * @author kec
 */
public class MeasureImpl implements Measure {

    private static final MathContext ONE_DIGIT_PRECISION = new MathContext(1);

    private final ManifoldCoordinate manifold;

    private final SimpleStringProperty narrative
            = new SimpleStringProperty(this, ObservableFields.MEASURE_NARRATIVE.toExternalString());

    private final SimpleDoubleProperty resolution
            = new SimpleDoubleProperty(this, ObservableFields.MEASURE_RESOLUTION.toExternalString());
    private final SimpleObjectProperty<ConceptSpecification> measureSemantic
            = new SimpleObjectProperty<>(this, ObservableFields.MEASURE_SEMANTIC.toExternalString());
    private final SimpleDoubleProperty lowerBound
            = new SimpleDoubleProperty(this, ObservableFields.INTERVAL_LOWER_BOUND.toExternalString());
    private final SimpleBooleanProperty includeLowerBound
            = new SimpleBooleanProperty(this, ObservableFields.INTERVAL_INCLUDE_UPPER_BOUND.toExternalString());
    private final SimpleDoubleProperty upperBound
            = new SimpleDoubleProperty(this, ObservableFields.INTERVAL_UPPER_BOUND.toExternalString());
    private final SimpleBooleanProperty includeUpperBound
            = new SimpleBooleanProperty(this, ObservableFields.INTERVAL_INCLUDE_LOWER_BOUND.toExternalString());

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
    public Optional<Double> getResolution() {
        return Optional.ofNullable(resolution.get());
    }

    public String getResolutionString() {
        if (measureSemantic.get() != null) {
            if (measureSemantic.get().getNid() == TermAux.ISO_8601.getNid()
                    || measureSemantic.get().getNid() == TermAux.ISO_8601_AFTER.getNid()
                    || measureSemantic.get().getNid() == TermAux.ISO_8601_PRIOR.getNid()) {
                long resolutionInMilliseconds = (long) resolution.get();
                return DurationUtil.msTo8601(resolutionInMilliseconds);
            }
        }
        BigDecimal roundedPrecision = new BigDecimal(resolution.get(), ONE_DIGIT_PRECISION);
        return roundedPrecision.toString();
    }

    public SimpleDoubleProperty resolutionProperty() {
        return resolution;
    }

    public void setResolution(Double resolution) {
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

        builder.append(getLowerBoundString());
        builder.append(", ");
        builder.append(getUpperBoundString());

        if (includeUpperBound()) {
            builder.append("]");
        } else {
            builder.append(")");
        }

        if (getResolution().isPresent()) {
            builder.append(" ± ");

            builder.append(getResolutionString());
        }

        builder.append(" ");
        if (getMeasureSemantic() != null) {
            builder.append(manifold.getPreferredDescriptionText(getMeasureSemantic()));
        } else {
            builder.append("unspecified measure semantic");
        }
        narrative.setValue(builder.toString());

    }

    /**
     * In most cases, the semantics of the measurement are the units of measure.
     *
     * @return the semantics for this measurement.
     */
    @Override
    public ConceptSpecification getMeasureSemantic() {
        return measureSemantic.get();
    }

    public SimpleObjectProperty<ConceptSpecification> measureSemanticProperty() {
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
    public double getLowerBound() {
        return lowerBound.get();
    }

    private String getLowerBoundString() {
        if (measureSemantic.get() != null) {

            if (measureSemantic.get().getNid() == TermAux.ISO_8601.getNid()) {
                ZonedDateTime boundryDateTime
                        = DateTimeUtil.epochToZonedDateTime(lowerBound.longValue());
                return DateTimeUtil.format(boundryDateTime, resolution.doubleValue());
            }
            if (measureSemantic.get().getNid() == TermAux.ISO_8601_AFTER.getNid()
                    || measureSemantic.get().getNid() == TermAux.ISO_8601_PRIOR.getNid()) {
                return DurationUtil.msTo8601(lowerBound.longValue());
            }
        }
        return Double.toString(getLowerBound());
    }

    private String getUpperBoundString() {
        if (measureSemantic.get() != null) {

            if (measureSemantic.get().getNid() == TermAux.ISO_8601.getNid()) {
                ZonedDateTime boundryDateTime
                        = DateTimeUtil.epochToZonedDateTime(upperBound.longValue());
                return DateTimeUtil.format(boundryDateTime, resolution.doubleValue());
            }
            if (measureSemantic.get().getNid() == TermAux.ISO_8601_AFTER.getNid()
                    || measureSemantic.get().getNid() == TermAux.ISO_8601_PRIOR.getNid()) {
                return DurationUtil.msTo8601(upperBound.longValue());
            }
        }
        return Double.toString(getUpperBound());
    }

    public SimpleDoubleProperty lowerBoundProperty() {
        return lowerBound;
    }

    public void setLowerBound(double lowerBound) {
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
    public double getUpperBound() {
        return upperBound.get();
    }

    public SimpleDoubleProperty upperBoundProperty() {
        return upperBound;
    }

    public void setUpperBound(double upperBound) {
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
