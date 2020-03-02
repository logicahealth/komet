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
package sh.komet.gui.search.flwor;

import javafx.beans.property.SimpleObjectProperty;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.query.JoinProperty;
import sh.isaac.api.query.JoinSpecification;
import sh.isaac.api.query.LetItemKey;

/**
 *
 * @author kec
 */
public class JoinSpecificationObservable implements JoinSpecification {
    SimpleObjectProperty<ConceptSpecification> firstAssemblageProperty 
            = new SimpleObjectProperty<>(this, MetaData.ASSEMBLAGE_1_TO_JOIN____SOLOR.toExternalString());
    
    SimpleObjectProperty<ConceptSpecification> secondAssemblageProperty 
            = new SimpleObjectProperty<>(this, MetaData.ASSEMBLAGE_2_TO_JOIN____SOLOR.toExternalString());
    
    SimpleObjectProperty<JoinProperty> firstFieldProperty 
            = new SimpleObjectProperty<>(this, MetaData.FIELD_1_TO_JOIN____SOLOR.toExternalString());
    
    SimpleObjectProperty<JoinProperty> secondFieldProperty 
            = new SimpleObjectProperty<>(this, MetaData.FIELD_2_TO_JOIN____SOLOR.toExternalString());

    SimpleObjectProperty<LetItemKey> stampCoordinateKeyProperty 
            = new SimpleObjectProperty<>(this, MetaData.ORIGIN_STAMP_COORDINATE_KEY_FOR_MANIFOLD____SOLOR.toExternalString());

    public JoinSpecificationObservable() {
    }

    public JoinSpecificationObservable(ConceptSpecification firstAssemblage, 
            JoinProperty firstField, 
            ConceptSpecification secondAssemblage, 
            JoinProperty secondField, 
            LetItemKey stampCoordinateKey) {
        firstAssemblageProperty.set(firstAssemblage);
        secondAssemblageProperty.set(secondAssemblage);
        firstFieldProperty.set(firstField);
        secondFieldProperty.set(secondField);
        stampCoordinateKeyProperty.set(stampCoordinateKey);
    }

    public JoinSpecificationObservable(JoinSpecification joinSpec) {
        firstAssemblageProperty.set(joinSpec.getFirstAssemblage());
        firstAssemblageProperty.addListener((observable, oldValue, newValue) -> {
            joinSpec.setFirstAssemblage(newValue);
        });
        secondAssemblageProperty.set(joinSpec.getSecondAssemblage());
        secondAssemblageProperty.addListener((observable, oldValue, newValue) -> {
            joinSpec.setSecondAssemblage(newValue);
        });
        firstFieldProperty.set(joinSpec.getFirstField());
        firstFieldProperty.addListener((observable, oldValue, newValue) -> {
            joinSpec.setFirstField(newValue);
        });
        secondFieldProperty.set(joinSpec.getSecondField());
        secondFieldProperty.addListener((observable, oldValue, newValue) -> {
            joinSpec.setSecondField(newValue);
        });
        
        stampCoordinateKeyProperty.set(joinSpec.getStampCoordinateKey());
        stampCoordinateKeyProperty.addListener((observable, oldValue, newValue) -> {
            joinSpec.setStampCoordinateKey(newValue);
        });
    }

    @Override
    public ConceptSpecification getFirstAssemblage() {
        return firstAssemblageProperty.get();
    }

    @Override
    public void setFirstAssemblage(ConceptSpecification firstAssemblage) {
        firstAssemblageProperty.set(firstAssemblage);
    }


    @Override
    public ConceptSpecification getSecondAssemblage() {
        return secondAssemblageProperty.get();
    }

    @Override
    public JoinProperty getSecondField() {
        return secondFieldProperty.get();
    }

    @Override
    public JoinProperty getFirstField() {
        return firstFieldProperty.get();
    }

    @Override
    public void setSecondAssemblage(ConceptSpecification assemblageToJoin) {
        this.secondAssemblageProperty.set(assemblageToJoin);
    }

    @Override
    public void setSecondField(JoinProperty joinField) {
        this.secondFieldProperty.set(joinField);
    }

    @Override
    public void setFirstField(JoinProperty sourceField) {
        this.firstFieldProperty.set(sourceField);
    }

    public SimpleObjectProperty<ConceptSpecification> firstAssemblageProperty() {
        return firstAssemblageProperty;
    }

    public SimpleObjectProperty<ConceptSpecification> secondAssemblageProperty() {
        return secondAssemblageProperty;
    }

    public SimpleObjectProperty<JoinProperty> firstFieldProperty() {
        return firstFieldProperty;
    }

    public SimpleObjectProperty<JoinProperty> secondFieldProperty() {
        return secondFieldProperty;
    }

    public SimpleObjectProperty<LetItemKey> stampCoordinateKeyProperty() {
        return stampCoordinateKeyProperty;
    }
   
    @Override
    public LetItemKey getStampCoordinateKey() {
        return stampCoordinateKeyProperty.get();
    }

    @Override
    public void setStampCoordinateKey(LetItemKey stampCoordinateKey) {
        this.stampCoordinateKeyProperty.set(stampCoordinateKey);
    }
    
}
