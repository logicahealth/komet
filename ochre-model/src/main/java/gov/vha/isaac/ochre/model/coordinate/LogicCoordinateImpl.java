/*
 * Copyright 2015 kec.
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
package gov.vha.isaac.ochre.model.coordinate;

import gov.vha.isaac.ochre.api.Get;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;

/**
 *
 * @author kec
 */
@XmlRootElement(name = "logicCoordinate")
@XmlAccessorType(XmlAccessType.FIELD)
public class LogicCoordinateImpl implements LogicCoordinate {
    
    int statedAssemblageSequence;
    int inferredAssemblageSequence;
    int descriptionLogicProfileSequence;
    int classifierSequence;
    
    protected LogicCoordinateImpl() {
        //for jaxb and subclass
    }

    public LogicCoordinateImpl(int statedAssemblageSequence, int inferredAssemblageSequence, int descriptionLogicProfileSequence, int classifierSequence) {
        this.statedAssemblageSequence = statedAssemblageSequence;
        this.inferredAssemblageSequence = inferredAssemblageSequence;
        this.descriptionLogicProfileSequence = descriptionLogicProfileSequence;
        this.classifierSequence = classifierSequence;
    }
    public ChangeListener<Number> setStatedAssemblageSequenceProperty(IntegerProperty statedAssemblageSequenceProperty) {
        ChangeListener<Number> listener = (ObservableValue<? extends Number> observable,
                                           Number oldValue,
                                           Number newValue) -> {
            statedAssemblageSequence = newValue.intValue();
        };
        statedAssemblageSequenceProperty.addListener(new WeakChangeListener<>(listener));
        return listener;
    }
    public ChangeListener<Number> setInferredAssemblageSequenceProperty(IntegerProperty inferredAssemblageSequenceProperty) {
        ChangeListener<Number> listener = (ObservableValue<? extends Number> observable,
                                           Number oldValue,
                                           Number newValue) -> {
            inferredAssemblageSequence = newValue.intValue();
        };
        inferredAssemblageSequenceProperty.addListener(new WeakChangeListener<>(listener));
        return listener;
    }
    public ChangeListener<Number> setDescriptionLogicProfileSequenceProperty(IntegerProperty descriptionLogicProfileSequenceProperty) {
        ChangeListener<Number> listener = (ObservableValue<? extends Number> observable,
                                           Number oldValue,
                                           Number newValue) -> {
            descriptionLogicProfileSequence = newValue.intValue();
        };
        descriptionLogicProfileSequenceProperty.addListener(new WeakChangeListener<>(listener));
        return listener;
    }
    public ChangeListener<Number> setClassifierSequenceProperty(IntegerProperty classifierSequenceProperty) {
        ChangeListener<Number> listener = (ObservableValue<? extends Number> observable,
                                           Number oldValue,
                                           Number newValue) -> {
            classifierSequence = newValue.intValue();
        };
        classifierSequenceProperty.addListener(new WeakChangeListener<>(listener));
        return listener;
    }

    
    @Override
    public int getStatedAssemblageSequence() {
        return statedAssemblageSequence;
    }

    @Override
    public int getInferredAssemblageSequence() {
        return inferredAssemblageSequence;
    }

    @Override
    public int getDescriptionLogicProfileSequence() {
        return descriptionLogicProfileSequence;
    }

    @Override
    public int getClassifierSequence() {
        return classifierSequence;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + this.statedAssemblageSequence;
        hash = 29 * hash + this.inferredAssemblageSequence;
        hash = 29 * hash + this.descriptionLogicProfileSequence;
        hash = 29 * hash + this.classifierSequence;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        //Do not compare object classes, a LogicCoordinateImpl from one impl should be able to be equal to another impl...
        final LogicCoordinate other = (LogicCoordinate) obj;
        if (this.statedAssemblageSequence != other.getStatedAssemblageSequence()) {
            return false;
        }
        if (this.inferredAssemblageSequence != other.getInferredAssemblageSequence()) {
            return false;
        }
        if (this.descriptionLogicProfileSequence != other.getDescriptionLogicProfileSequence()) {
            return false;
        }
        return this.classifierSequence == other.getClassifierSequence();
    }
    
    @Override
    public String toString() {
        return "LogicCoordinateImpl{" + Get.conceptDescriptionText(statedAssemblageSequence)
					 + "<" + statedAssemblageSequence + ">,\n"
					 + Get.conceptDescriptionText(inferredAssemblageSequence) 
					 + "<" + inferredAssemblageSequence + ">, \n"
					 + Get.conceptDescriptionText(descriptionLogicProfileSequence) 
					 + "<" + descriptionLogicProfileSequence + ">, \n"
					 + Get.conceptDescriptionText(classifierSequence) 
					 + "<" + classifierSequence + ">}";
    }
}
