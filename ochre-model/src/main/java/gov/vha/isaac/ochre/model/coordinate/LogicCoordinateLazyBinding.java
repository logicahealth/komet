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
package gov.vha.isaac.ochre.model.coordinate;

import gov.vha.isaac.ochre.api.ConceptProxy;
import gov.vha.isaac.ochre.api.component.concept.ConceptSpecification;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import java.util.UUID;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author kec
 */
@XmlRootElement(name = "logicCoordinateLazyBinding")
@XmlAccessorType(XmlAccessType.FIELD)
public class LogicCoordinateLazyBinding extends LogicCoordinateImpl {
    
    @XmlJavaTypeAdapter(ConceptProxyAdapter.class)
    private ConceptSpecification statedAssemblageProxy = null;
    @XmlJavaTypeAdapter(ConceptProxyAdapter.class)
    private ConceptSpecification inferredAssemblageProxy = null;
    @XmlJavaTypeAdapter(ConceptProxyAdapter.class)
    private ConceptSpecification descriptionLogicProfileProxy = null;
    @XmlJavaTypeAdapter(ConceptProxyAdapter.class)
    private ConceptSpecification classifierProxy = null;
    
    private LogicCoordinateLazyBinding() {
        //for jaxb
    }

    public LogicCoordinateLazyBinding(ConceptSpecification statedAssemblageProxy, 
            ConceptSpecification inferredAssemblageProxy, 
            ConceptSpecification descriptionLogicProfileProxy, 
            ConceptSpecification classifierProxy) {
        super(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        this.statedAssemblageProxy = statedAssemblageProxy;
        this.inferredAssemblageProxy = inferredAssemblageProxy;
        this.descriptionLogicProfileProxy = descriptionLogicProfileProxy;
        this.classifierProxy = classifierProxy;
    }
    
    

    @Override
    public int getStatedAssemblageSequence() {
        if (statedAssemblageSequence == Integer.MAX_VALUE) {
            statedAssemblageSequence = statedAssemblageProxy.getConceptSequence();
        }
        return statedAssemblageSequence;
    }

    @Override
    public int getInferredAssemblageSequence() {
        if (inferredAssemblageSequence == Integer.MAX_VALUE) {
            inferredAssemblageSequence = inferredAssemblageProxy.getConceptSequence();
        }
        return inferredAssemblageSequence;
    }

    @Override
    public int getDescriptionLogicProfileSequence() {
        if (descriptionLogicProfileSequence == Integer.MAX_VALUE) {
            descriptionLogicProfileSequence = descriptionLogicProfileProxy.getConceptSequence();
        }
        return descriptionLogicProfileSequence;
    }

    @Override
    public int getClassifierSequence() {
        if (classifierSequence == Integer.MAX_VALUE) {
            classifierSequence = classifierProxy.getConceptSequence();
        }
        return classifierSequence;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + this.getStatedAssemblageSequence();
        hash = 83 * hash + this.getInferredAssemblageSequence();
        hash = 83 * hash + this.getDescriptionLogicProfileSequence();
        hash = 83 * hash + this.getClassifierSequence();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        //Do not compare object classes, a LogicCoordinateImpl from one impl should be able to be equal to another impl...
        final LogicCoordinate other = (LogicCoordinate) obj;
        if (this.getStatedAssemblageSequence() != other.getStatedAssemblageSequence()) {
            return false;
        }
        if (this.getInferredAssemblageSequence() != other.getInferredAssemblageSequence()) {
            return false;
        }
        if (this.getDescriptionLogicProfileSequence() != other.getDescriptionLogicProfileSequence()) {
            return false;
        }
        return this.getClassifierSequence() == other.getClassifierSequence();
    }

    @Override
    public String toString() {
        return "LogicCoordinateLazyBinding{" + "statedAssemblageProxy=" + statedAssemblageProxy + ", inferredAssemblageProxy=" + inferredAssemblageProxy + ", descriptionLogicProfileProxy=" + descriptionLogicProfileProxy + ", classifierProxy=" + classifierProxy + '}';
    }
    
    private static class ConceptProxyAdapter extends XmlAdapter<UUID[], ConceptProxy> {
        public UUID[] marshal(ConceptProxy c) {
            return c.getUuidList().toArray(new UUID[c.getUuidList().size()]);
        }

        @Override
        public ConceptProxy unmarshal(UUID[] v) throws Exception {
            return new ConceptProxy("", v);
        }
    }
}
