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
package sh.isaac.api.query;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.component.concept.ConceptSpecification;


/**
 * The AttributeSpecification provides the data necessary to convert a
 component nid into format for inclusion in a results table. The query 
 returns an array of 1 or more nids for each row, which needs to be processed into 
 the result set. Each nid is a member of an assemblage
 that was specified in FOR clause of the query. A list of AttributeSpecification 
 elements will expand the array of nids into an array of Strings to populate a row in the
 result set. 
 * 
 * @author kec
 */
public class AttributeSpecification implements QueryFieldSpecification {
     /**
     * The index of the property on the version of the chronology to 
     * process for this function. The property index is based on the ordered 
     * list of properties provided by the ObservableVersion.getProperties() method. 
     */
    private final SimpleIntegerProperty propertyIndexProperty;

    /**
     * The name of the column to include in the results. 
     */
    private final SimpleStringProperty columnNameProperty;
    
    /**
     * The key for the stamp coordinate from which to determine the version of the 
     * chronology to process. The nid is obtained by finding the nid in the nid array 
     * which is a member of the specified assemblage. 
     */
    private final SimpleObjectProperty<LetItemKey> stampCoordinateKeyProperty;

    /**
     * The assemblage from which the nid to process must be an element of. 
     */
    private final SimpleIntegerProperty assemblageNidProperty;
    /**
     * Possibly null cell function to apply to the property value to generate the 
     * result value. 
     */
    
    private final SimpleObjectProperty<AttributeFunction> attributeFunctionProperty;
    
    /**
     * The concept that specifies the property on the version of the chronology to 
     * process for this function. Not used in result set generation. 
     */
    private final SimpleObjectProperty<ConceptSpecification> propertySpecificationProperty;

    /**
     * No arg constructor for Jaxb. 
     */
    public AttributeSpecification() {
        this.attributeFunctionProperty = new SimpleObjectProperty();
        this.columnNameProperty = new SimpleStringProperty();
        this.assemblageNidProperty = new SimpleIntegerProperty();
        this.propertySpecificationProperty = new SimpleObjectProperty();
        this.propertyIndexProperty = new SimpleIntegerProperty();
        this.stampCoordinateKeyProperty  = new SimpleObjectProperty();
    }
    
    public AttributeSpecification(AttributeSpecification another) {
        this.attributeFunctionProperty = new SimpleObjectProperty(another.attributeFunctionProperty.get());
        this.columnNameProperty = new SimpleStringProperty(another.columnNameProperty.get());
        
        /**
         * The assemblage from which to select the nid to process. 
         */
        this.assemblageNidProperty = new SimpleIntegerProperty(another.assemblageNidProperty.get());
        this.propertySpecificationProperty = new SimpleObjectProperty(another.propertySpecificationProperty.get());
        this.propertyIndexProperty = new SimpleIntegerProperty(another.propertyIndexProperty.get());
        this.stampCoordinateKeyProperty  = new SimpleObjectProperty(another.getStampFilterKey());
    }
    
    public AttributeSpecification(
            AttributeFunction attributeFunction, String columnName, int assemblageNid,
            ConceptSpecification propertySpecification, 
            LetItemKey stampCoordinateKey, int propertyIndex) {
        this.attributeFunctionProperty = new SimpleObjectProperty(attributeFunction);
        this.columnNameProperty = new SimpleStringProperty(columnName);
        this.assemblageNidProperty = new SimpleIntegerProperty(assemblageNid);
        this.propertySpecificationProperty = new SimpleObjectProperty(propertySpecification);
        this.propertyIndexProperty = new SimpleIntegerProperty(propertyIndex);
        this.stampCoordinateKeyProperty = new SimpleObjectProperty(stampCoordinateKey);
    }
    
    @Override
    public LetItemKey getStampFilterKey() {
        return stampCoordinateKeyProperty.get();
    }

    @Override
    public void setStampCoordinateKey(LetItemKey stampCoordinateKey) {
        this.stampCoordinateKeyProperty.set(stampCoordinateKey);
    }

    @Override
    public SimpleObjectProperty<LetItemKey> stampCoordinateKeyProperty() {
        return stampCoordinateKeyProperty;
    }

    @Override
    public ConceptSpecification getPropertySpecification() {
        return this.propertySpecificationProperty.get();
    }
    @Override
    public void setPropertySpecification(ConceptSpecification propertySpecification) {
        this.propertySpecificationProperty.set(propertySpecification);
    }
    
    @Override
    public SimpleObjectProperty<ConceptSpecification> propertySpecificationProperty() {
        return this.propertySpecificationProperty;
    }

    @Override
    public Integer getPropertyIndex() {
        return this.propertyIndexProperty.get();
    }
    @Override
    public void setPropertyIndex(Integer propertyIndex) {
        this.propertyIndexProperty.set(propertyIndex);
    }
    
    @Override
    public SimpleIntegerProperty propertyIndexProperty() {
        return this.propertyIndexProperty;
    }

    @Override
    public int getAssemblageNid() {
        return this.assemblageNidProperty.get();
    }
    @Override
    public void setAssemblageNid(int assemblageNid) {
        this.assemblageNidProperty.set(assemblageNid);
    }
    
    @Override
    public SimpleIntegerProperty assemblageNidProperty() {
        return this.assemblageNidProperty;
    }
    
     @Override
     public ConceptSpecification getAssemblage() {
         if (this.assemblageNidProperty.get() == 0) {
             return null;
         }
        return new ConceptProxy(this.assemblageNidProperty.get());
    }
    @Override
     public void setAssemblage(ConceptSpecification specification) {
         this.assemblageNidProperty.set(specification.getNid());
        
    }

    @Override
    public void setAssemblageUuid(ConceptSpecification assemblageConceptSpecification) {
        setAssemblageNid(assemblageConceptSpecification.getNid());
    }

    @Override
    public AttributeFunction getAttributeFunction() {
        return attributeFunctionProperty.get();
    }

    @Override
    public SimpleObjectProperty<AttributeFunction> attributeFunctionProperty() {
        return attributeFunctionProperty;
    }

    @Override
    public void setAttributeFunction(AttributeFunction attributeFunction) {
        this.attributeFunctionProperty.set(attributeFunction);
    }

    @Override
    public String getColumnName() {
        return columnNameProperty.get();
    }

    @Override
    public SimpleStringProperty columnNameProperty() {
        return columnNameProperty;
    }

    @Override
    public void setColumnName(String columnName) {
        this.columnNameProperty.set(columnName);
    }
}
