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

import java.util.Objects;
import java.util.function.BiFunction;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import sh.isaac.api.coordinate.StampCoordinate;

/**
 *
 * @author kec
 */
@XmlRootElement(name = "AttributeFunction")
@XmlAccessorType(value = XmlAccessType.NONE)
public class AttributeFunction {
    String functionName;
    BiFunction<String,StampCoordinate, String> function;

    /**
     * No arg constructor for Jaxb
     */
    public AttributeFunction() {
    }

    public AttributeFunction(String functionName, BiFunction<String, StampCoordinate, String> function) {
        this.functionName = functionName;
        this.function = function;
    }
    
    public String apply(String dataIn, StampCoordinate stampCoordinate) {
        return function.apply(dataIn, stampCoordinate);
    }

    @Override
    public String toString() {
        return functionName;
    }

    @XmlAttribute
     public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.functionName);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AttributeFunction other = (AttributeFunction) obj;
        if (!Objects.equals(this.functionName, other.functionName)) {
            return false;
        }
        return true;
    }
    
    
}
