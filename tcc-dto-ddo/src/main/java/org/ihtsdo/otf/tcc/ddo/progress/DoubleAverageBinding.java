/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.otf.tcc.ddo.progress;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * TODO Examine JavaFX properties to see if we need to do anything special to prevent memory leaks. 
 * 
 * @author kec
 */
public class DoubleAverageBinding extends DoubleBinding {

    ReadOnlyDoubleProperty[] items;

    public DoubleAverageBinding(ReadOnlyDoubleProperty[] items) {
        this.items = items;
        super.bind(items);
    }
    
    @Override
    protected double computeValue() {
        double sum = 0;
        for (ReadOnlyDoubleProperty item: items) {
            sum += item.doubleValue();
        }
        return sum / items.length;
    }

    @Override
    public ObservableList<?> getDependencies() {
        return FXCollections.observableArrayList(items);
    }
    
}
