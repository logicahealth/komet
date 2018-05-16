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
package sh.isaac.komet.preferences;

import java.util.UUID;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.preferencesfx.model.Category;
import sh.isaac.komet.preferencesfx.model.Setting;

/**
 *
 * @author kec
 */
public class ManifoldGroupPreferences extends AbstractPreferences {

StringProperty stringProperty = new SimpleStringProperty("String");
BooleanProperty booleanProperty = new SimpleBooleanProperty(true);
IntegerProperty integerProperty = new SimpleIntegerProperty(12);
DoubleProperty doubleProperty = new SimpleDoubleProperty(6.5);

    // HashMap for manifolds?
    // HashMap for Windows?
    public ManifoldGroupPreferences(IsaacPreferences preferencesNode) {
        super(preferencesNode, preferencesNode.get(AbstractPreferences.CommonProperties.SHEET_NAME, "Manifold " + UUID.randomUUID()));
    }

    @Override
    public Property<?>[] getProperties() {
        return new Property<?>[]{stringProperty, booleanProperty, integerProperty, doubleProperty};
    }

    @Override
    public Category[] getCategories() {
        return new Category[]{
            Category.of(getSheetName(),
            Setting.of("Setting 1", stringProperty), // creates a group automatically
            Setting.of("Setting 2", booleanProperty), // which contains both settings
            Setting.of("Setting 3", integerProperty),
            Setting.of("Setting 4", doubleProperty)            
            )
        };
    }

}