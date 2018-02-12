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
package sh.komet.gui.importation;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

/**
 *
 * @author kec
 */
public abstract class ImportItemAbstract implements ImportItem {

    SimpleStringProperty nameProperty = new SimpleStringProperty();
    BooleanProperty importData = new SimpleBooleanProperty(true);


    @Override
    public final SimpleStringProperty nameProperty() {
        return nameProperty;
    }

    @Override
    public final String getName() {
        return nameProperty.get();
    }

    public final void setName(String name) {
        this.nameProperty.set(name);
    }

    @Override
    public String recordCount() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean importData() {
        return importData.get();
    }

    @Override
    public void setImportData(boolean value) {
        importData.set(value);
    }

    @Override
    public BooleanProperty importDataProperty() {
        return importData;
    }

    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        setImportData(newValue);
    }
    
}
