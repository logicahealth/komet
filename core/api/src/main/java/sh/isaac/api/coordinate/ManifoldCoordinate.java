/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



package sh.isaac.api.coordinate;

//~--- JDK imports ------------------------------------------------------------

import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.util.time.DateTimeUtil;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface ManifoldCoordinate.
 *
 * @author kec
 * TODO consider deprecation/deletion and switch to diagraph coordinate.
 */
public interface ManifoldCoordinate {

    static UUID getManifoldCoordinateUuid(ManifoldCoordinate manifoldCoordinate) {
        ArrayList<UUID> uuidList = new ArrayList<>();
        uuidList.add(manifoldCoordinate.getDigraph().getDigraphCoordinateUuid());
        uuidList.add(manifoldCoordinate.getVertexSort().getVertexSortUUID());
        StringBuilder sb = new StringBuilder(uuidList.toString());
        return UUID.nameUUIDFromBytes(sb.toString().getBytes());
    }

    default String toUserString() {
        StringBuilder sb = new StringBuilder("Manifold coordinate: ");
        sb.append("\nDigraph coordinate: ").append(getDigraph().toUserString());
        return sb.toString();
    }

    ManifoldCoordinateImmutable toManifoldCoordinateImmutable();

    default UUID getManifoldCoordinateUuid() {
        return getManifoldCoordinateUuid(this);
    }

    /**
     * In most cases all stamp filters will be the same.
     * @return the vertex stamp filter services as the default stamp filter.
     */
    default StampFilter getLanguageStampFilter() {
        return getVertexStampFilter();
    }

    default StampFilter getVertexStampFilter() {
        return getDigraph().getVertexStampFilter();
    }

    default StampFilter getEdgeStampFilter() {
        return getDigraph().getEdgeStampFilter();
    }

    default LatestVersion<DescriptionVersion> getDescription(
            ConceptSpecification concept) {
        return this.getLanguageCoordinate().getDescription(concept.getNid(), this.getLanguageStampFilter());
    }
    default Optional<String> getDescriptionText(int conceptNid) {
        getLanguageCoordinate().getDescriptionText(conceptNid, this.getLanguageStampFilter());
        LatestVersion<DescriptionVersion> latestVersion = getDescription(conceptNid);
        if (latestVersion.isPresent()) {
            return Optional.of(latestVersion.get().getText());
        }
        return Optional.empty();
    }


    default Optional<String> getDescriptionText(ConceptSpecification concept) {
        return getDescriptionText(concept.getNid());
    }

    default LatestVersion<DescriptionVersion> getDescription(
            int conceptNid) {
        return this.getLanguageCoordinate().getDescription(conceptNid, this.getLanguageStampFilter());
    }


    default LatestVersion<DescriptionVersion> getDescription(
            List<SemanticChronology> descriptionList) {
        return this.getLanguageCoordinate().getDescription(descriptionList, this.getLanguageStampFilter());
    }

    default PremiseType getPremiseType() {
        return getDigraph().getPremiseType();
    }

    default DigraphCoordinateImmutable toDigraphImmutable() {
        return getDigraph().toDigraphImmutable();
    }

    DigraphCoordinate getDigraph();

    default VertexSort getVertexSort() {
        return getDigraph().getVertexSort();
    }

    default LogicCoordinate getLogicCoordinate() {
        return getDigraph().getLogicCoordinate();
    }

    default LanguageCoordinate getLanguageCoordinate() {
        return getDigraph().getLanguageCoordinate();
    }

    default Optional<String> getFullyQualifiedName(int nid, StampFilter filter) {
        return this.getLanguageCoordinate().getFullyQualifiedNameText(nid, filter);
    }

    default Optional<LogicalExpression> getStatedLogicalExpression(int conceptNid) {
        return getLogicalExpression(conceptNid, PremiseType.STATED);
    }

    default Optional<LogicalExpression> getStatedLogicalExpression(ConceptSpecification concept) {
        return getLogicalExpression(concept.getNid(), PremiseType.STATED);
    }

    default Optional<LogicalExpression> getLogicalExpression(ConceptSpecification concept, PremiseType premiseType) {
        return this.getLogicalExpression(concept.getNid(), premiseType);
    }

    default Optional<LogicalExpression> getLogicalExpression(int conceptNid, PremiseType premiseType) {
        return this.getLogicCoordinate().getLogicalExpression(conceptNid, premiseType, this.getDigraph().getVertexStampFilter());
    }

    default LatestVersion<LogicGraphVersion> getStatedLogicalDefinition(int conceptNid) {
        return this.getLogicalDefinition(conceptNid, PremiseType.STATED);
    }

    default LatestVersion<LogicGraphVersion> getStatedLogicalDefinition(ConceptSpecification concept) {
        return this.getLogicalDefinition(concept.getNid(), PremiseType.STATED);
    }

    default LatestVersion<LogicGraphVersion> getLogicalDefinition(ConceptSpecification concept, PremiseType premiseType) {
        return this.getLogicalDefinition(concept.getNid(), premiseType);
    }

    default LatestVersion<LogicGraphVersion> getLogicalDefinition(int conceptNid, PremiseType premiseType) {
        return this.getLogicCoordinate().getLogicGraphVersion(conceptNid, premiseType, this.getVertexStampFilter());
    }


    default Optional<String> getFullyQualifiedName(int nid) {
        return this.getLanguageCoordinate().getFullyQualifiedNameText(nid, this.getLanguageStampFilter());
    }
    /**
     * Sort the vertex concept nids with respect to settings from the
     * digraphCoordinate where appropriate.
     * @param vertexConceptNids
     * @return sorted vertexConceptNids
     */
    default int[] sortVertexes(int[] vertexConceptNids) {
        return getVertexSort().sortVertexes(vertexConceptNids, getDigraph().toDigraphImmutable());
    }

    default String getVertexLabel(int vertexConceptNid) {
        return getVertexSort().getVertexLabel(vertexConceptNid,
                getDigraph().getLanguageCoordinate().toLanguageCoordinateImmutable(),
                getDigraph().getLanguageStampFilter().toStampFilterImmutable());
    }

    default String getVertexLabel(ConceptSpecification vertexConcept) {
        return getVertexLabel(vertexConcept.getNid());
    }

    default String getPreferredDescriptionText(int conceptNid) {
        try {
            return VertexSortPreferredName.getRegularName(conceptNid, getLanguageCoordinate(), getDigraph().getLanguageStampFilter());
        } catch (NoSuchElementException ex) {
            return ex.getLocalizedMessage();
        }
    }

    default String getPreferredDescriptionText(ConceptSpecification concept) {
        return getPreferredDescriptionText(concept.getNid());
    }

    default String getFullyQualifiedDescriptionText(int conceptNid) {
        return VertexSortFullyQualifiedName.getFullyQualifiedName(conceptNid, getLanguageCoordinate(), getDigraph().getLanguageStampFilter());
    }

    default String getFullyQualifiedDescriptionText(ConceptSpecification concept) {
        return getFullyQualifiedDescriptionText(concept.getNid());
    }

    default LatestVersion<DescriptionVersion> getFullyQualifiedDescription(int conceptNid) {
        return getLanguageCoordinate().getFullyQualifiedDescription(conceptNid, getDigraph().getLanguageStampFilter());
    }

    default LatestVersion<DescriptionVersion> getFullyQualifiedDescription(ConceptSpecification concept) {
        return getFullyQualifiedDescription(concept.getNid());
    }


    default LatestVersion<DescriptionVersion> getPreferredDescription(int conceptNid) {
        return getLanguageCoordinate().getPreferredDescription(conceptNid, getDigraph().getLanguageStampFilter());
    }

    default LatestVersion<DescriptionVersion> getPreferredDescription(ConceptSpecification concept) {
        return getPreferredDescription(concept.getNid());
    }


    default OptionalInt getAcceptabilityNid(int descriptionNid, int dialectAssemblageNid) {
        ImmutableIntSet acceptabilityChronologyNids = Get.assemblageService().getSemanticNidsForComponentFromAssemblage(descriptionNid, dialectAssemblageNid);

        for (int acceptabilityChronologyNid: acceptabilityChronologyNids.toArray()) {
            SemanticChronology acceptabilityChronology = Get.assemblageService().getSemanticChronology(acceptabilityChronologyNid);
            LatestVersion<ComponentNidVersion> latestAcceptability = acceptabilityChronology.getLatestVersion(getLanguageStampFilter());
            if (latestAcceptability.isPresent()) {
                return OptionalInt.of(latestAcceptability.get().getComponentNid());
            }
        }
        return OptionalInt.empty();
    }

    default LatestVersion<LogicGraphVersion> getStatedLogicGraphVersion(int conceptNid) {
        return getLogicGraphVersion(conceptNid, PremiseType.STATED);
    }

    default LatestVersion<LogicGraphVersion> getInferredLogicGraphVersion(ConceptSpecification conceptSpecification) {
        return getLogicGraphVersion(conceptSpecification.getNid(), PremiseType.INFERRED);
    }

    default LatestVersion<LogicGraphVersion> getStatedLogicGraphVersion(ConceptSpecification conceptSpecification) {
        return getLogicGraphVersion(conceptSpecification.getNid(), PremiseType.STATED);
    }

    default LatestVersion<LogicGraphVersion> getInferredLogicGraphVersion(int conceptNid) {
        return getLogicGraphVersion(conceptNid, PremiseType.INFERRED);
    }

    default LatestVersion<LogicGraphVersion> getLogicGraphVersion(int conceptNid, PremiseType premiseType) {
        ConceptChronology concept = Get.concept(conceptNid);
        return concept.getLogicalDefinition(getEdgeStampFilter(), premiseType, this.getLogicCoordinate());
    }

    default Optional<LogicalExpression> getInferredLogicalExpression(ConceptSpecification spec) {
        return getLogicCoordinate().getInferredLogicalExpression(spec.getNid(), this.getEdgeStampFilter());
    }

    default Optional<LogicalExpression> getInferredLogicalExpression(int conceptNid) {
        return getLogicCoordinate().getLogicalExpression(conceptNid, PremiseType.INFERRED, this.getEdgeStampFilter());
    }

    default String toFqnConceptString(Object object) {
        return toConceptString(object, this::getFullyQualifiedDescriptionText);
    }

    default String toPreferredConceptString(Object object) {
        return toConceptString(object, this::getPreferredDescriptionText);
    }

    default String toConceptString(Object object, Function<ConceptSpecification,String> toString) {
        if (object == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        if (object instanceof ConceptSpecification) {
            ConceptSpecification conceptSpecification = (ConceptSpecification) object;
            sb.append(toString.apply(conceptSpecification));
        } else if (object instanceof Collection) {
            Collection collection = (Collection) object;
            return toConceptString(collection.toArray(), toString);
        } else if (object.getClass().isArray()) {
            Object[] a = (Object[]) object;
            int iMax = a.length - 1;
            if (iMax == -1) {
                sb.append("[]");
            } else {
                sb.append('[');
                for (int i = 0; ; i++) {
                    sb.append(toConceptString(a[i], toString));
                    if (i == iMax)
                        return sb.append(']').toString();
                    sb.append(", ");
                }
            }
        } else if (object instanceof String) {
            String string = (String) object;
            if (string.indexOf(ConceptProxy.FIELD_SEPARATOR) > -1) {
                ConceptProxy conceptProxy = new ConceptProxy(string);
                sb.append(toConceptString(conceptProxy, toString));
            } else {
                sb.append(string);
            }
        } else if (object instanceof Long) {
            sb.append(DateTimeUtil.format((Long) object));
        } else {
            sb.append(object.toString());
        }
        return sb.toString();
    }

}
