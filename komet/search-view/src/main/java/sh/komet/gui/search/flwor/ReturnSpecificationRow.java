/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development includeProperty the
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
package sh.komet.gui.search.flwor;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import sh.isaac.api.component.concept.ConceptSpecification;

/**
 *
 * @author kec
 */
public class ReturnSpecificationRow {
    private final SimpleBooleanProperty includeInResults;
    private final SimpleStringProperty assemblageName;
    private final SimpleStringProperty propertyName;
    private final SimpleStringProperty functionName;
    private final SimpleStringProperty columnName;
    private final SimpleIntegerProperty assemblageNid;
    private final SimpleObjectProperty<ConceptSpecification> propertySpecification;

    public ReturnSpecificationRow(String assemblageName, String propertyName, 
            String functionName, String columnName, int assemblageNid,
            ConceptSpecification propertySpecification) {
        this.includeInResults = new SimpleBooleanProperty(false);
        this.assemblageName = new SimpleStringProperty(assemblageName);
        this.propertyName = new SimpleStringProperty(propertyName);
        this.functionName = new SimpleStringProperty(functionName);
        this.columnName = new SimpleStringProperty(columnName);
        this.assemblageNid = new SimpleIntegerProperty(assemblageNid);
        this.propertySpecification = new SimpleObjectProperty(propertySpecification);
    }
    public ConceptSpecification getPropertySpecification() {
        return this.propertySpecification.get();
    }
    public void setPropertySpecification(ConceptSpecification propertySpecification) {
        this.propertySpecification.set(propertySpecification);
    }
    
    public SimpleObjectProperty<ConceptSpecification> propertySpecificationProperty() {
        return this.propertySpecification;
    }

    public int getAssemblageNid() {
        return this.assemblageNid.get();
    }
    public void setAssemblageNid(int assemblageNid) {
        this.assemblageNid.set(assemblageNid);
    }
    
    public SimpleIntegerProperty assemblageNidProperty() {
        return this.assemblageNid;
    }
    
    public boolean includeInResults() {
        return includeInResults.get();
    }

    public SimpleBooleanProperty includeInResultsProperty() {
        return includeInResults;
    }

    public void setIncludeInResults(boolean includeInResults) {
        this.includeInResults.set(includeInResults);
    }

    public String getAssemblageName() {
        return assemblageName.get();
    }

    public SimpleStringProperty assemblageNameProperty() {
        return assemblageName;
    }

    public void setAssemblageName(String assemblageName) {
        this.assemblageName.set(assemblageName);
    }

    public String getPropertyName() {
        return propertyName.get();
    }

    public SimpleStringProperty propertyNameProperty() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName.set(propertyName);
    }

    public String getFunctionName() {
        return functionName.get();
    }

    public SimpleStringProperty functionNameProperty() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName.set(functionName);
    }

    public String getColumnName() {
        return columnName.get();
    }

    public SimpleStringProperty columnNameProperty() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName.set(columnName);
    }
}
