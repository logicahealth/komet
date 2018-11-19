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

import java.util.Map;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.util.time.DateTimeUtil;

/**
 *
 * @author kec
 */
@XmlRootElement(name = "AttributeFunction")
@XmlAccessorType(value = XmlAccessType.NONE)
public class AttributeFunction {

    String functionName;
    AttributeTriFunction<String, StampCoordinate, Query, String> function;

    /**
     * No arg constructor for Jaxb
     */
    public AttributeFunction() {
    }

    public AttributeFunction(String functionName) {
        this.functionName = functionName;
        setFunctionName(functionName);
    }

    public String apply(String dataIn, StampCoordinate stampCoordinate, Query query) {
        return function.apply(dataIn, stampCoordinate, query);
    }

    @Override
    public String toString() {
        return functionName;
    }

    @XmlAttribute
    public String getFunctionName() {
        return functionName;
    }

    public static AttributeTriFunction<String, StampCoordinate, Query, String> getFunctionFromName(String functionName) {
        switch (functionName) {
            case "":
                return (string, stampCoordinate, query) -> {
                    return string;
                };
            case "Identity":
                return (string, stampCoordinate, query) -> {
                    return string;
                };
            case "Primoridal uuid":
                return (string, stampCoordinate, query) -> {
                    int nid = Integer.parseInt(string);
                    return Get.identifierService().getUuidPrimordialForNid(nid).toString();
                };
            case "All uuids":
                return (string, stampCoordinate, query) -> {
                    int nid = Integer.parseInt(string);
                    return Get.identifierService().getUuidsForNid(nid).toString();
                };
            case "Epoch to 8601 date/time":
                return (string, stampCoordinate, query) -> {
                    long epochTime = Long.parseLong(string);
                    return DateTimeUtil.format(epochTime);
                };
            default:
                if (functionName.endsWith(" preferred name")) {
                    return (string, stampCoordinate, query) -> {
                        LanguageCoordinate lc = null;
                        for (Map.Entry<LetItemKey, Object> entry : query.getLetDeclarations().entrySet()) {
                            if (functionName.startsWith(entry.getKey().getItemName())) {
                                lc = (LanguageCoordinate) entry.getValue();
                                break;
                            }
                        }
                        if (lc != null) {
                            int nid = Integer.parseInt(string);
                            LatestVersion<DescriptionVersion> description = lc.getPreferredDescription(nid, stampCoordinate);
                            if (description.isPresent()) {
                                return description.get().getText();
                            }
                            return "No current preferred name";
                        }
                        throw new IllegalStateException("Cannot find LetItemKey for " + functionName);
                    };
                } else if (functionName.endsWith(" FQN")) {
                    return (string, stampCoordinate, query) -> {
                        LanguageCoordinate lc = null;
                        for (Map.Entry<LetItemKey, Object> entry : query.getLetDeclarations().entrySet()) {
                            if (functionName.startsWith(entry.getKey().getItemName())) {
                                lc = (LanguageCoordinate) entry.getValue();
                                break;
                            }
                        }
                        if (lc != null) {
                            int nid = Integer.parseInt(string);
                            LatestVersion<DescriptionVersion> description = lc.getFullySpecifiedDescription(nid, stampCoordinate);
                            if (description.isPresent()) {
                                return description.get().getText();
                            }
                            return "No current FQN";
                        }
                        throw new IllegalStateException("Cannot find LetItemKey for " + functionName + " " + query.getLetDeclarations().entrySet());
                    };
                } else if (functionName.endsWith(" definition")) {
                    return (string, stampCoordinate, query) -> {
                        LanguageCoordinate lc = null;
                        for (Map.Entry<LetItemKey, Object> entry : query.getLetDeclarations().entrySet()) {
                            if (functionName.startsWith(entry.getKey().getItemName())) {
                                lc = (LanguageCoordinate) entry.getValue();
                                break;
                            }
                        }
                        if (lc != null) {
                            int nid = Integer.parseInt(string);
                            LatestVersion<DescriptionVersion> description = lc.getDefinitionDescription(Get.concept(nid).getConceptDescriptionList(), stampCoordinate);
                            if (description.isPresent()) {
                                return description.get().getText();
                            }
                            return "No current definition";
                        }
                        throw new IllegalStateException("Cannot find LetItemKey for " + functionName);
                    };
                }
        }    
        throw new IllegalStateException("No function for " + functionName);
    }
    
    public final void setFunctionName(String functionName) {
        this.functionName = functionName;
        if (this.functionName.isEmpty()) {
            this.functionName = "Identity";
        }
        this.function = getFunctionFromName(this.functionName);

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
        return Objects.equals(this.functionName, other.functionName);
    }

}
