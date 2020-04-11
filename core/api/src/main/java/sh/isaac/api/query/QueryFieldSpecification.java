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
package sh.isaac.api.query;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import sh.isaac.api.component.concept.ConceptSpecification;

/**
 *
 * @author kec
 */
public interface QueryFieldSpecification {

    SimpleIntegerProperty assemblageNidProperty();

    SimpleObjectProperty<AttributeFunction> attributeFunctionProperty();

    SimpleStringProperty columnNameProperty();

    ConceptSpecification getAssemblage();

    int getAssemblageNid();

    AttributeFunction getAttributeFunction();

    String getColumnName();

    Integer getPropertyIndex();

    ConceptSpecification getPropertySpecification();

    LetItemKey getStampFilterKey();

    SimpleIntegerProperty propertyIndexProperty();

    SimpleObjectProperty<ConceptSpecification> propertySpecificationProperty();

    void setAssemblage(ConceptSpecification specification);

    void setAssemblageNid(int assemblageNid);

    void setAssemblageUuid(ConceptSpecification assemblageConceptSpecification);

    void setAttributeFunction(AttributeFunction attributeFunction);

    void setColumnName(String columnName);

    void setPropertyIndex(Integer propertyIndex);

    void setPropertySpecification(ConceptSpecification propertySpecification);

    void setStampCoordinateKey(LetItemKey stampCoordinateKey);

    SimpleObjectProperty<LetItemKey> stampCoordinateKeyProperty();
    
}
