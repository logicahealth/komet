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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import sh.isaac.api.component.concept.ConceptSpecification;

/**
 *
 * @author kec
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class JoinSpec implements JoinSpecification {
    ConceptSpecification firstAssemblage;
    ConceptSpecification firstField;
    ConceptSpecification secondAssemblage;
    ConceptSpecification secondField;
    LetItemKey stampCoordinateKey;
    
    public JoinSpec() {
    }

    public JoinSpec(ConceptSpecification firstAssemblage,
            ConceptSpecification firstField, 
            ConceptSpecification secondAssemblage, 
            ConceptSpecification secondField,
            LetItemKey stampCoordinateKey) {
        this.firstAssemblage = firstAssemblage;
        this.firstField = firstField;
        this.secondAssemblage = secondAssemblage;
        this.secondField = secondField;
        this.stampCoordinateKey = stampCoordinateKey;
    }

    public JoinSpec(JoinSpecification js) {
        this.firstAssemblage = js.getFirstAssemblage();
        this.firstField = js.getFirstField();
        this.secondAssemblage = js.getSecondAssemblage();
        this.secondField = js.getSecondField();
        this.stampCoordinateKey = js.getStampCoordinateKey();
    }

    @XmlElement
    @Override
    public ConceptSpecification getFirstAssemblage() {
        return firstAssemblage;
    }

    @Override
    public void setFirstAssemblage(ConceptSpecification firstAssemblage) {
        this.firstAssemblage = firstAssemblage;
    }

    @XmlElement
    @Override
    public ConceptSpecification getFirstField() {
        return firstField;
    }

    @Override
    public void setFirstField(ConceptSpecification firstField) {
        this.firstField = firstField;
    }

    @XmlElement
    @Override
    public ConceptSpecification getSecondAssemblage() {
        return secondAssemblage;
    }

    @Override
    public void setSecondAssemblage(ConceptSpecification secondAssemblage) {
        this.secondAssemblage = secondAssemblage;
    }

    @XmlElement
    @Override
    public ConceptSpecification getSecondField() {
        return secondField;
    }

    @Override
    public void setSecondField(ConceptSpecification secondField) {
        this.secondField = secondField;
    }

    @XmlElement
    @Override
    public LetItemKey getStampCoordinateKey() {
        return stampCoordinateKey;
    }

    @Override
    public void setStampCoordinateKey(LetItemKey stampCoordinateKey) {
        this.stampCoordinateKey = stampCoordinateKey;
    }
    
    
}
