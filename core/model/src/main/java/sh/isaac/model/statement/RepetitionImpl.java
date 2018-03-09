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

import java.util.Optional;
import javafx.beans.property.SimpleObjectProperty;
import sh.isaac.api.statement.Measure;
import sh.isaac.api.statement.Repetition;
import sh.isaac.model.observable.ObservableFields;

/**
 *
 * @author kec
 */
public class RepetitionImpl implements Repetition {
    private final SimpleObjectProperty<MeasureImpl> periodStart = 
            new SimpleObjectProperty<>(this, ObservableFields.REPETITION_PERIOD_START.toExternalString());
    private final SimpleObjectProperty<MeasureImpl> periodDuration = 
            new SimpleObjectProperty<>(this, ObservableFields.REPETITION_PERIOD_DURATION.toExternalString());
    private final SimpleObjectProperty<MeasureImpl> eventFrequency =
            new SimpleObjectProperty<>(this, ObservableFields.REPETITION_EVENT_FREQUENCY.toExternalString());
    private final SimpleObjectProperty<MeasureImpl> eventDuration =
            new SimpleObjectProperty<>(this, ObservableFields.REPETITION_EVENT_DURATION.toExternalString());

    @Override
    public Measure getPeriodStart() {
        return periodStart.get();
    }

    public SimpleObjectProperty<MeasureImpl> periodStartProperty() {
        return periodStart;
    }

    public void setPeriodStart(MeasureImpl periodStart) {
        this.periodStart.set(periodStart);
    }

    @Override
    public MeasureImpl getPeriodDuration() {
        return periodDuration.get();
    }

    public SimpleObjectProperty<MeasureImpl> periodDurationProperty() {
        return periodDuration;
    }

    public void setPeriodDuration(MeasureImpl periodDuration) {
        this.periodDuration.set(periodDuration);
    }

    @Override
    public Measure getEventFrequency() {
        return eventFrequency.get();
    }

    public SimpleObjectProperty<MeasureImpl> eventFrequencyProperty() {
        return eventFrequency;
    }

    public void setEventFrequency(MeasureImpl eventFrequency) {
        this.eventFrequency.set(eventFrequency);
    }

    @Override
    public Optional<Measure> getEventDuration() {
        return Optional.ofNullable(eventDuration.get());
    }

    public SimpleObjectProperty<MeasureImpl> eventDurationProperty() {
        return eventDuration;
    }

    public void setEventDuration(MeasureImpl eventDuration) {
        this.eventDuration.set(eventDuration);
    }
}
