/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
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
package gov.vha.isaac.ochre.model.observable.coordinate;

import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.observable.coordinate.ObservableLogicCoordinate;
import gov.vha.isaac.ochre.model.coordinate.LogicCoordinateImpl;
import gov.vha.isaac.ochre.model.observable.ObservableFields;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author kec
 */
@XmlRootElement(name = "observableLogicCoordinate")
@XmlAccessorType(XmlAccessType.FIELD)
public class ObservableLogicCoordinateImpl extends ObservableCoordinateImpl implements ObservableLogicCoordinate {
    LogicCoordinateImpl logicCoordinate;

    @XmlTransient
    IntegerProperty statedAssemblageSequenceProperty;
    @XmlTransient
    IntegerProperty inferredAssemblageSequenceProperty;
    @XmlTransient
    IntegerProperty descriptionLogicProfileSequenceProperty;
    @XmlTransient
    IntegerProperty classifierSequenceProperty;
    
    private ObservableLogicCoordinateImpl() {
        //for jaxb
    }

    public ObservableLogicCoordinateImpl(LogicCoordinate logicCoordinate) {        
        if (logicCoordinate instanceof ObservableLogicCoordinateImpl) {
            this.logicCoordinate = ((ObservableLogicCoordinateImpl)logicCoordinate).logicCoordinate;
        } else {
            this.logicCoordinate = (LogicCoordinateImpl) logicCoordinate;
        }
    }

    @Override
    public IntegerProperty statedAssemblageSequenceProperty() {
        if (statedAssemblageSequenceProperty == null) {
            statedAssemblageSequenceProperty = new SimpleIntegerProperty(this, 
                    ObservableFields.STATED_ASSEMBLAGE_SEQUENCE_FOR_LOGIC_COORDINATE.toExternalString(), 
                    getStatedAssemblageSequence());
            addListenerReference(logicCoordinate.setStatedAssemblageSequenceProperty(statedAssemblageSequenceProperty));
        }
        return statedAssemblageSequenceProperty;
    }

    @Override
    public IntegerProperty inferredAssemblageSequenceProperty() {
        if (inferredAssemblageSequenceProperty == null) {
            inferredAssemblageSequenceProperty = new SimpleIntegerProperty(this, 
                    ObservableFields.INFERRED_ASSEMBLAGE_SEQUENCE_FOR_LOGIC_COORDINATE.toExternalString(), 
                    getInferredAssemblageSequence());
        }
        addListenerReference(logicCoordinate.setInferredAssemblageSequenceProperty(inferredAssemblageSequenceProperty));
        return inferredAssemblageSequenceProperty;
    }

    @Override
    public IntegerProperty descriptionLogicProfileSequenceProperty() {
        if (descriptionLogicProfileSequenceProperty == null) {
            descriptionLogicProfileSequenceProperty = new SimpleIntegerProperty(this, 
                    ObservableFields.DESCRIPTION_LOGIC_PROFILE_SEQUENCE_FOR_LOGIC_COORDINATE.toExternalString(), 
                    getDescriptionLogicProfileSequence());
            addListenerReference(logicCoordinate.setDescriptionLogicProfileSequenceProperty(descriptionLogicProfileSequenceProperty));
        }
        return descriptionLogicProfileSequenceProperty;
    }

    @Override
    public IntegerProperty classifierSequenceProperty() {
        if (classifierSequenceProperty == null) {
            classifierSequenceProperty = new SimpleIntegerProperty(this, 
                    ObservableFields.CLASSIFIER_SEQUENCE_FOR_LOGIC_COORDINATE.toExternalString(), 
                    getClassifierSequence());
            addListenerReference(logicCoordinate.setClassifierSequenceProperty(classifierSequenceProperty));
        }
        return classifierSequenceProperty;
    }
    
    @Override
    public int getStatedAssemblageSequence() {
        if (statedAssemblageSequenceProperty != null) {
            return statedAssemblageSequenceProperty.get();
        }
        return logicCoordinate.getStatedAssemblageSequence();
    }

    @Override
    public int getInferredAssemblageSequence() {
        if (inferredAssemblageSequenceProperty != null) {
            return inferredAssemblageSequenceProperty.get();
        }
        return logicCoordinate.getInferredAssemblageSequence();
    }

    @Override
    public int getDescriptionLogicProfileSequence() {
        if (descriptionLogicProfileSequenceProperty != null) {
            return descriptionLogicProfileSequenceProperty.get();
        }
        return logicCoordinate.getDescriptionLogicProfileSequence();
    }

    @Override
    public int getClassifierSequence() {
        if (classifierSequenceProperty != null) {
            return classifierSequenceProperty.get();
        }
        return logicCoordinate.getClassifierSequence();
    }
    
    @Override
    public int hashCode() {
        return logicCoordinate.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return logicCoordinate.equals(obj);
    }

    @Override
    public String toString() {
        return "ObservableLogicCoordinateImpl{" + logicCoordinate.toString() + '}';
    }
}
