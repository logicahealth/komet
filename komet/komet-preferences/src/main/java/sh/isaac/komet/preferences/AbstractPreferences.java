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

import java.util.Optional;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import sh.isaac.api.preferences.IsaacPreferences;

/**
 *
 * @author kec
 */
public abstract class AbstractPreferences implements Preferences {
    public enum CommonProperties { SHEET_NAME };
    
    SimpleStringProperty sheetNameProperty = new SimpleStringProperty(this, "Preference sheet name");
    SimpleObjectProperty<Preferences> parentSheetProperty = new SimpleObjectProperty<>(this, "Preference sheet parent");
    SimpleObjectProperty<ObservableList<Preferences>> 
            childSheetsProperty = new SimpleObjectProperty<>(this, "Preference sheet children", 
                    FXCollections.observableArrayList());
    SimpleObjectProperty<IsaacPreferences>  preferencesNodeProperty = new SimpleObjectProperty<>(this, "Preference node");

    public AbstractPreferences(IsaacPreferences  preferencesNode, String preferencesName) {
        preferencesNodeProperty.set(preferencesNode);
        this.setSheetName(preferencesName);
    }
    
    @Override
    public final String getSheetName() {
        return sheetNameProperty.get();
    }
    
    @Override
    public final void setSheetName(String sheetName) {
        this.sheetNameProperty.set(sheetName);
    }
    @Override
    public final SimpleStringProperty sheetNameProperty() {
        return sheetNameProperty;
    }

    @Override
    public final Optional<Preferences> getParentSheet() {
        return Optional.of(parentSheetProperty.get());
    }

    @Override
    public final void setParentSheet(Preferences parentSheet) {
        parentSheetProperty.set(parentSheet);
    }

    @Override
    public final SimpleObjectProperty<Preferences>  parentSheetProperty() {
        return parentSheetProperty;
    }
    
    @Override
    public final ObservableList<Preferences> getChildSheets() {
        return childSheetsProperty.get();
    }
    
    @Override
    public final SimpleObjectProperty<ObservableList<Preferences>>  childSheetsProperty() {
        return childSheetsProperty;
    }
    
    
}
