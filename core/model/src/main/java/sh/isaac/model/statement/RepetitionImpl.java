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
import sh.isaac.api.statement.Interval;
import sh.isaac.api.statement.Measure;
import sh.isaac.api.statement.Repetition;

/**
 *
 * @author kec
 */
public class RepetitionImpl implements Repetition {
    private final SimpleObjectProperty<Measure> periodStart = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Measure> periodDuration = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Interval> eventFrequency = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Measure> eventSeparation = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Measure> eventDuration = new SimpleObjectProperty<>();

    @Override
    public Measure getPeriodStart() {
        return periodStart.get();
    }

    public SimpleObjectProperty<Measure> periodStartProperty() {
        return periodStart;
    }

    public void setPeriodStart(Measure periodStart) {
        this.periodStart.set(periodStart);
    }

    @Override
    public Measure getPeriodDuration() {
        return periodDuration.get();
    }

    public SimpleObjectProperty<Measure> periodDurationProperty() {
        return periodDuration;
    }

    public void setPeriodDuration(Measure periodDuration) {
        this.periodDuration.set(periodDuration);
    }

    @Override
    public Interval getEventFrequency() {
        return eventFrequency.get();
    }

    public SimpleObjectProperty<Interval> eventFrequencyProperty() {
        return eventFrequency;
    }

    public void setEventFrequency(Interval eventFrequency) {
        this.eventFrequency.set(eventFrequency);
    }

    @Override
    public Measure getEventSeparation() {
        return eventSeparation.get();
    }

    public SimpleObjectProperty<Measure> eventSeperationProperty() {
        return eventSeparation;
    }

    public void setEventSeperation(Measure eventSeperation) {
        this.eventSeparation.set(eventSeperation);
    }

    @Override
    public Optional<Measure> getEventDuration() {
        return Optional.ofNullable(eventDuration.get());
    }

    public SimpleObjectProperty<Measure> eventDurationProperty() {
        return eventDuration;
    }

    public void setEventDuration(Measure eventDuration) {
        this.eventDuration.set(eventDuration);
    }
}
