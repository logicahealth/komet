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
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.util.UUIDUtil;
import sh.isaac.api.util.time.DateTimeUtil;

/**
 *
 * @author kec
 */
@XmlRootElement(name = "AttributeFunction")
@XmlAccessorType(value = XmlAccessType.NONE)
public class AttributeFunction {

    public static final String KIND_OF_PREFIX = "Kind of ";
    public static final String CHILD_OF_PREFIX = "Child of ";
    public static final String DESCENDENT_OF_PREFIX = "Descendent of ";
    public static final String MANIFOLD_PREFIX = " per ";
    public static final String DEFINITION_UUID = " definition UUID";
    public static final String DEFINITION = " definition";
    public static final String FQN_UUID = " FQN UUID";
    public static final String FQN = " FQN";
    public static final String PREFERRED_NAME_UUID = " preferred name UUID";
    public static final String PREFERRED_NAME = " preferred name";
    public static final String IS_PREFERRED = " is preferred";
    public static final String EPOCH_TO_8601_DATETIME = "Epoch to 8601 date/time";
    public static final String ALL_UUIDS = "All uuids";
    public static final String PRIMORDIAL_UUID = "Primordial uuid";
    public static final String IDENTITY = "Identity";
    public static final String EMPTY = "";
    public static final String SCT_ID = "SCT id";
    public static final String COORDINATE_UUID = " Coordinate UUID";


    String functionName;
    AttributeQuadFunction<String, Long, StampCoordinate, Query, String> function;

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
        if (UUIDUtil.isUUID(dataIn)) {
            return function.apply(functionName, new Long(Get.nidForUuids(UUID.fromString(dataIn))), stampCoordinate, query);
        }
        if (functionName.equals(IDENTITY) || functionName.equals(EMPTY)) {
            return dataIn;
        }
        return function.apply(functionName, Long.parseLong(dataIn), stampCoordinate, query);
    }

    @Override
    public String toString() {
        return functionName;
    }

    @XmlAttribute
    public String getFunctionName() {
        return functionName;
    }

    public static AttributeQuadFunction<String, Long, StampCoordinate, Query, String> getFunctionFromName(String functionName) {
        switch (functionName) {
            case EMPTY:
                return (funcName, nid, stampCoordinate, query) -> {
                    return nid.toString();
                };
            case IDENTITY:
                return (funcName, nid, stampCoordinate, query) -> {
                    return nid.toString();
                };
            case PRIMORDIAL_UUID:
                return (funcName, nid, stampCoordinate, query) -> {
                    return Get.identifierService().getUuidPrimordialForNid(nid.intValue()).toString();
                };
            case ALL_UUIDS:
                return (funcName, nid, stampCoordinate, query) -> {
                    return Get.identifierService().getUuidsForNid(nid.intValue()).toString();
                };
            case EPOCH_TO_8601_DATETIME:
                return (funcName, time, stampCoordinate, query) -> {
                    return DateTimeUtil.format(time);
                };
            case SCT_ID:
                return (funcName, nid, stampCoordinate, query) -> {
                    Optional<SemanticChronology> optionalSctidChronology = 
                    Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblage(nid.intValue(), TermAux.SNOMED_IDENTIFIER.getNid()).findFirst();
                    if (optionalSctidChronology.isPresent()) {
                        LatestVersion<StringVersion> optionalSctidVersion = optionalSctidChronology.get().getLatestVersion(stampCoordinate);
                        if (optionalSctidVersion.isPresent()) {
                            return optionalSctidVersion.get().getString();
                        }
                    }
                    return EMPTY;
                };
            default:
                if (functionName.endsWith(COORDINATE_UUID)) {
                    return (funcName, nid, stampCoordinate, query) -> {
                        Object coordinate = null;
                        for (Map.Entry<LetItemKey, Object> entry : query.getLetDeclarations().entrySet()) {
                            if (functionName.startsWith(entry.getKey().getItemName())) {
                                coordinate = (LanguageCoordinate) entry.getValue();
                                break;
                            }
                        }
                        if (coordinate != null) {
                            if (coordinate instanceof ManifoldCoordinate) {
                                return ((ManifoldCoordinate)coordinate).getManifoldCoordinateUuid().toString();
                            } else if (coordinate instanceof StampCoordinate) {
                                return ((StampCoordinate)coordinate).getStampCoordinateUuid().toString();
                            } else if (coordinate instanceof LanguageCoordinate) {
                                return ((LanguageCoordinate)coordinate).getLanguageCoordinateUuid().toString();
                            } else if (coordinate instanceof LogicCoordinate) {
                                return ((LogicCoordinate)coordinate).getLogicCoordinateUuid().toString();
                            } 
                            return EMPTY;
                        }
                        throw new IllegalStateException("Cannot find LetItemKey for " + functionName);
                        
                    };
                } else if (functionName.endsWith(IS_PREFERRED)) {
                    return (funcName, nid, stampCoordinate, query) -> {
                        LanguageCoordinate lc = null;
                        for (Map.Entry<LetItemKey, Object> entry : query.getLetDeclarations().entrySet()) {
                            if (functionName.startsWith(entry.getKey().getItemName())) {
                                lc = (LanguageCoordinate) entry.getValue();
                                break;
                            }
                        }
                        if (lc != null) {
                            SemanticChronology descChronolgy = Get.assemblageService().getSemanticChronology(nid.intValue());
                            LatestVersion<DescriptionVersion> description = lc.getPreferredDescription(descChronolgy.getReferencedComponentNid(), stampCoordinate);
                            if (description.isPresent() && description.get().getNid() == nid.intValue()) {
                                return Boolean.TRUE.toString();
                            }
                            return Boolean.FALSE.toString();
                        }
                        throw new IllegalStateException("Cannot find LetItemKey for " + functionName);
                        
                    };
                } else if (functionName.endsWith(PREFERRED_NAME)) {
                    return (funcName, nid, stampCoordinate, query) -> {
                        LanguageCoordinate lc = null;
                        for (Map.Entry<LetItemKey, Object> entry : query.getLetDeclarations().entrySet()) {
                            if (functionName.startsWith(entry.getKey().getItemName())) {
                                lc = (LanguageCoordinate) entry.getValue();
                                break;
                            }
                        }
                        if (lc != null) {
                            LatestVersion<DescriptionVersion> description = lc.getPreferredDescription(nid.intValue(), stampCoordinate);
                            if (description.isPresent()) {
                                return description.get().getText();
                            }
                            return "No current preferred name";
                        }
                        throw new IllegalStateException("Cannot find LetItemKey for " + functionName);
                    };
                } else if (functionName.endsWith(PREFERRED_NAME_UUID)) {
                    return (funcName, nid, stampCoordinate, query) -> {
                        LanguageCoordinate lc = null;
                        for (Map.Entry<LetItemKey, Object> entry : query.getLetDeclarations().entrySet()) {
                            if (functionName.startsWith(entry.getKey().getItemName())) {
                                lc = (LanguageCoordinate) entry.getValue();
                                break;
                            }
                        }
                        if (lc != null) {
                            LatestVersion<DescriptionVersion> description = lc.getPreferredDescription(nid.intValue(), stampCoordinate);
                            if (description.isPresent()) {
                                return description.get().getPrimordialUuid().toString();
                            }
                            return "No current preferred name";
                        }
                        throw new IllegalStateException("Cannot find LetItemKey for " + functionName);
                    };
                } else if (functionName.endsWith(FQN)) {
                    return (funcName, nid, stampCoordinate, query) -> {
                        LanguageCoordinate lc = null;
                        for (Map.Entry<LetItemKey, Object> entry : query.getLetDeclarations().entrySet()) {
                            if (functionName.startsWith(entry.getKey().getItemName())) {
                                lc = (LanguageCoordinate) entry.getValue();
                                break;
                            }
                        }
                        if (lc != null) {
                            LatestVersion<DescriptionVersion> description = lc.getFullySpecifiedDescription(nid.intValue(), stampCoordinate);
                            if (description.isPresent()) {
                                return description.get().getText();
                            }
                            return "No current FQN";
                        }
                        throw new IllegalStateException("Cannot find LetItemKey for " + functionName + " " + query.getLetDeclarations().entrySet());
                    };
                } else if (functionName.endsWith(FQN_UUID)) {
                    return (funcName, nid, stampCoordinate, query) -> {
                        LanguageCoordinate lc = null;
                        for (Map.Entry<LetItemKey, Object> entry : query.getLetDeclarations().entrySet()) {
                            if (functionName.startsWith(entry.getKey().getItemName())) {
                                lc = (LanguageCoordinate) entry.getValue();
                                break;
                            }
                        }
                        if (lc != null) {
                            LatestVersion<DescriptionVersion> description = lc.getFullySpecifiedDescription(nid.intValue(), stampCoordinate);
                            if (description.isPresent()) {
                                return description.get().getPrimordialUuid().toString();
                            }
                            return "No current FQN";
                        }
                        throw new IllegalStateException("Cannot find LetItemKey for " + functionName + " " + query.getLetDeclarations().entrySet());
                    };
                } else if (functionName.endsWith(DEFINITION)) {
                    return (funcName, nid, stampCoordinate, query) -> {
                        LanguageCoordinate lc = null;
                        for (Map.Entry<LetItemKey, Object> entry : query.getLetDeclarations().entrySet()) {
                            if (functionName.startsWith(entry.getKey().getItemName())) {
                                lc = (LanguageCoordinate) entry.getValue();
                                break;
                            }
                        }
                        if (lc != null) {
                            LatestVersion<DescriptionVersion> description = lc.getDefinitionDescription(Get.concept(nid.intValue()).getConceptDescriptionList(), stampCoordinate);
                            if (description.isPresent()) {
                                return description.get().getText();
                            }
                            return "No current definition";
                        }
                        throw new IllegalStateException("Cannot find LetItemKey for " + functionName);
                    };
                } else if (functionName.endsWith(DEFINITION_UUID)) {
                    return (funcName, nid, stampCoordinate, query) -> {
                        LanguageCoordinate lc = null;
                        for (Map.Entry<LetItemKey, Object> entry : query.getLetDeclarations().entrySet()) {
                            if (functionName.startsWith(entry.getKey().getItemName())) {
                                lc = (LanguageCoordinate) entry.getValue();
                                break;
                            }
                        }
                        if (lc != null) {
                            LatestVersion<DescriptionVersion> description = lc.getDefinitionDescription(Get.concept(nid.intValue()).getConceptDescriptionList(), stampCoordinate);
                            if (description.isPresent()) {
                                return description.get().getPrimordialUuid().toString();
                            }
                            return "No current definition";
                        }
                        throw new IllegalStateException("Cannot find LetItemKey for " + functionName);
                    };
                } else if (functionName.startsWith(KIND_OF_PREFIX)) {
                    return (funcName, nid, stampCoordinate, query) -> {
                        return doTaxonomyQuery(functionName, KIND_OF_PREFIX, query, nid.intValue());
                    };
                } else if (functionName.startsWith(CHILD_OF_PREFIX)) {
                    return (funcName, nid, stampCoordinate, query) -> {
                        return doTaxonomyQuery(functionName, CHILD_OF_PREFIX, query, nid.intValue());
                    };
                } else if (functionName.startsWith(DESCENDENT_OF_PREFIX)) {
                    return (funcName, nid, stampCoordinate, query) -> {
                        return doTaxonomyQuery(functionName, DESCENDENT_OF_PREFIX, query, nid.intValue());
                    };
                }
        }
        throw new IllegalStateException("No function for " + functionName);
    }
    protected static String doTaxonomyQuery(String functionName1, String prefix, Query query, int nid) throws NoSuchElementException {
        String conceptKeyName = functionName1.substring(prefix.length(), functionName1.indexOf(MANIFOLD_PREFIX));
        String manifoldKeyName = functionName1.substring(functionName1.indexOf(MANIFOLD_PREFIX) + MANIFOLD_PREFIX.length());
        ManifoldCoordinateForQuery manifold = null;
        for (LetItemKey key : query.getLetDeclarations().keySet()) {
            if (key.getItemName().equals(manifoldKeyName)) {
                manifold = (ManifoldCoordinateForQuery) query.getLetDeclarations().get(key);
            }
        }
        ConceptSpecification kindOfSpec = null;
        for (LetItemKey key : query.getLetDeclarations().keySet()) {
            if (key.getItemName().equals(conceptKeyName)) {
                kindOfSpec = (ConceptSpecification) query.getLetDeclarations().get(key);
            }
        }
        if (manifold != null && kindOfSpec != null) {
            switch (prefix) {
                case KIND_OF_PREFIX:
                    if (Get.taxonomyService().getSnapshotNoTree(manifold).isKindOf(nid, kindOfSpec.getNid())) {
                        return Boolean.TRUE.toString();
                    }
                    break;
                case CHILD_OF_PREFIX:
                    if (Get.taxonomyService().getSnapshotNoTree(manifold).isChildOf(nid, kindOfSpec.getNid())) {
                        return Boolean.TRUE.toString();
                    }
                    break;
                case DESCENDENT_OF_PREFIX:
                    if (Get.taxonomyService().getSnapshotNoTree(manifold).isDescendentOf(nid, kindOfSpec.getNid())) {
                        return Boolean.TRUE.toString();
                    }
                    break;
            }
            return Boolean.FALSE.toString();
        }
        return "Error: kos: " + kindOfSpec + " m: " + manifold;
    }

    protected static int getNidFromString(String string) throws NumberFormatException {
        int nid;
        if (UUIDUtil.isUUID(string)) {
            nid = Get.nidForUuids(UUID.fromString(string));
        } else {
            nid = Integer.parseInt(string);
        }
        return nid;
    }

    public final void setFunctionName(String functionName) {
        this.functionName = functionName;
        if (this.functionName.isEmpty()) {
            this.functionName = IDENTITY;
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
