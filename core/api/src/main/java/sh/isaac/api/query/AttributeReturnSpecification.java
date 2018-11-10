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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.xml.ConceptSpecificationAdaptor;

/**
 * The AttributeReturnSpecification provides the data necessary to convert a
 component nid into format for inclusion in a results table. The query 
 returns an array of 1 or more nids for each row, which needs to be processed into 
 * the result set. Each nid is a member of an assemblage
 that was specified in FOR clause of the query. A list of AttributeReturnSpecification 
 elements will expand the array of nids into an array of Strings to populate a row in the
 result set. 
 * 
 * @author kec
 */
@XmlRootElement(name = "AttributeReturnSpecification")
@XmlAccessorType(value = XmlAccessType.NONE)
public class AttributeReturnSpecification {
     /**
     * The index of the property on the version of the chronology to 
     * process for this function. The property index is based on the ordered 
     * list of properties provided by the ObservableVersion.getProperties() method. 
     */
    private final SimpleIntegerProperty propertyIndex;

    /**
     * The name of the column to include in the results. 
     */
    private final SimpleStringProperty columnName;
    
    /**
     * The key for the stamp coordinate from which to determine the version of the 
     * chronology to process. The nid is obtained by finding the nid in the nid array 
     * which is a member of the specified assemblage. 
     */
    private final SimpleObjectProperty<LetItemKey> stampCoordinateKey  = new SimpleObjectProperty();

    /**
     * The assemblage from which the nid to process must be an element of. 
     */
    private final SimpleIntegerProperty assemblageNid;
    /**
     * Possibly null cell function to apply to the property value to generate the 
     * result value. 
     */
    
    private final SimpleObjectProperty<AttributeFunction> attributeFunction;
    
    /**
     * The concept that specifies the property on the version of the chronology to 
     * process for this function. Not used in result set generation. 
     */
    private final SimpleObjectProperty<ConceptSpecification> propertySpecification;

    /**
     * No arg constructor for Jaxb. 
     */
    public AttributeReturnSpecification() {
        this.attributeFunction = new SimpleObjectProperty();
        this.columnName = new SimpleStringProperty();
        this.assemblageNid = new SimpleIntegerProperty();
        this.propertySpecification = new SimpleObjectProperty();
        this.propertyIndex = new SimpleIntegerProperty();
    }
    
    public AttributeReturnSpecification(AttributeReturnSpecification another) {
        this.attributeFunction = new SimpleObjectProperty(another.attributeFunction.get());
        this.columnName = new SimpleStringProperty(another.columnName.get());
        
        /**
         * The assemblage from which to select the nid to process. 
         */
        this.assemblageNid = new SimpleIntegerProperty(another.assemblageNid.get());
        this.propertySpecification = new SimpleObjectProperty(another.propertySpecification.get());
        this.propertyIndex = new SimpleIntegerProperty(another.propertyIndex.get());
    }
    
    public AttributeReturnSpecification(
            AttributeFunction attributeFunction, String columnName, int assemblageNid,
            ConceptSpecification propertySpecification, int propertyIndex) {
        this.attributeFunction = new SimpleObjectProperty(attributeFunction);
        this.columnName = new SimpleStringProperty(columnName);
        this.assemblageNid = new SimpleIntegerProperty(assemblageNid);
        this.propertySpecification = new SimpleObjectProperty(propertySpecification);
        this.propertyIndex = new SimpleIntegerProperty(propertyIndex);
    }
    
    @XmlElement(name = "stampCoordinateKey")
    public LetItemKey getStampCoordinateKey() {
        return stampCoordinateKey.get();
    }

    public SimpleObjectProperty<LetItemKey> stampCoordinateKeyProperty() {
        return stampCoordinateKey;
    }

    public void setStampCoordinateKey(LetItemKey stampCoordinateKey) {
        this.stampCoordinateKey.set(stampCoordinateKey);
    }

    @XmlElement(name = "propertySpecification")
    public ConceptSpecification getPropertySpecification() {
        return this.propertySpecification.get();
    }
    public void setPropertySpecification(ConceptSpecification propertySpecification) {
        this.propertySpecification.set(propertySpecification);
    }
    
    public SimpleObjectProperty<ConceptSpecification> propertySpecificationProperty() {
        return this.propertySpecification;
    }

    @XmlAttribute(name = "propertyIndex")
    public Integer getPropertyIndex() {
        return this.propertyIndex.get();
    }
    public void setPropertyIndex(Integer propertyIndex) {
        this.propertyIndex.set(propertyIndex);
    }
    
    public SimpleIntegerProperty propertyIndexProperty() {
        return this.propertyIndex;
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
    
    @XmlElement(name = "assemblage")
    @XmlJavaTypeAdapter(ConceptSpecificationAdaptor.class)
     public ConceptSpecification getAssemblage() {
        return new ConceptProxy(this.assemblageNid.get());
    }

    public void setAssemblageUuid(ConceptSpecification assemblageConceptSpecification) {
        setAssemblageNid(assemblageConceptSpecification.getNid());
    }

    @XmlElement(name = "attributeFunction")
    public AttributeFunction getAttributeFunction() {
        return attributeFunction.get();
    }

    public SimpleObjectProperty<AttributeFunction> attributeFunctionProperty() {
        return attributeFunction;
    }

    public void setAttributeFunction(AttributeFunction attributeFunction) {
        this.attributeFunction.set(attributeFunction);
    }

    @XmlAttribute(name = "columnName")
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
