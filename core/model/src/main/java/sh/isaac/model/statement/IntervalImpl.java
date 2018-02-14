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
import javafx.beans.property.SimpleFloatProperty;
import sh.isaac.api.statement.Interval;

/**
 *
 * @author kec
 */
public class IntervalImpl implements Interval {
    private final SimpleFloatProperty lowerBound = new SimpleFloatProperty();
    private final SimpleBooleanProperty includeLowerBound = new SimpleBooleanProperty();
    private final SimpleFloatProperty upperBound = new SimpleFloatProperty();
    private final SimpleBooleanProperty includeUpperBound = new SimpleBooleanProperty();

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

    public boolean includeLowerBound() {
        return includeLowerBound.get();
    }

    public SimpleBooleanProperty includeLowerBoundProperty() {
        return includeLowerBound;
    }

    public void setIncludeLowerBound(boolean includeLowerBound) {
        this.includeLowerBound.set(includeLowerBound);
    }

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
