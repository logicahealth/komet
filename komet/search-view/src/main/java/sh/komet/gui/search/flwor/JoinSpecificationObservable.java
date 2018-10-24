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
import sh.isaac.api.query.JoinSpecification;

/**
 *
 * @author kec
 */
public class JoinSpecificationObservable implements JoinSpecification {
    SimpleObjectProperty<ConceptSpecification> assemblageToJoin 
            = new SimpleObjectProperty<ConceptSpecification>(this, MetaData.ASSEMBLAGE_TO_JOIN____SOLOR.toExternalString());
    
    SimpleObjectProperty<ConceptSpecification> joinField 
            = new SimpleObjectProperty<ConceptSpecification>(this, MetaData.FIELD_TO_JOIN____SOLOR.toExternalString());
    
    SimpleObjectProperty<ConceptSpecification> sourceField 
            = new SimpleObjectProperty<ConceptSpecification>(this, MetaData.FOR_ASSEMBLAGE_FIELD_TO_JOIN____SOLOR.toExternalString());
    
    @Override
    public ConceptSpecification getAssemblageToJoin() {
        return assemblageToJoin.get();
    }

    @Override
    public ConceptSpecification getJoinField() {
        return joinField.get();
    }

    @Override
    public ConceptSpecification getSourceField() {
        return sourceField.get();
    }

    @Override
    public void setAssemblageToJoin(ConceptSpecification assemblageToJoin) {
        this.assemblageToJoin.set(assemblageToJoin);
    }

    @Override
    public void setJoinField(ConceptSpecification joinField) {
        this.joinField.set(joinField);
    }

    @Override
    public void setSourceField(ConceptSpecification sourceField) {
        this.sourceField.set(sourceField);
    }
    
    public SimpleObjectProperty<ConceptSpecification> getAssemblageToJoinProperty() {
        return assemblageToJoin;
    }

    public SimpleObjectProperty<ConceptSpecification> getJoinFieldProperty() {
        return joinField;
    }

    public SimpleObjectProperty<ConceptSpecification> getSourceFieldProperty() {
        return sourceField;
    }

}
