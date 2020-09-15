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

import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Edge;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.TaxonomySnapshot;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.api.util.NaturalOrder;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.api.util.time.DateTimeUtil;

import java.time.Instant;
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
        uuidList.add(manifoldCoordinate.getEditCoordinate().getEditCoordinateUuid());
        uuidList.add(manifoldCoordinate.getNavigationCoordinate().getNavigationCoordinateUuid());
        uuidList.add(manifoldCoordinate.getVertexSort().getVertexSortUUID());
        uuidList.add(manifoldCoordinate.getVertexStatusSet().getStatusSetUuid());
        uuidList.add(manifoldCoordinate.getViewStampFilter().getStampFilterUuid());
        uuidList.add(manifoldCoordinate.getLanguageCoordinate().getLanguageCoordinateUuid());
        uuidList.add(UuidT5Generator.get(manifoldCoordinate.getCurrentActivity().name()));
        StringBuilder sb = new StringBuilder(uuidList.toString());
        return UUID.nameUUIDFromBytes(sb.toString().getBytes());
    }

    default String toUserString() {
        StringBuilder sb = new StringBuilder("Manifold coordinate: ");
        sb.append("\nActivity: ").append(getCurrentActivity().toUserString());
        sb.append("\n").append(getNavigationCoordinate().toUserString());
        sb.append("\n\nView filter:\n").append(getViewStampFilter().toUserString());
        sb.append("\n\nLanguage coordinate:\n").append(getLanguageCoordinate().toUserString());
        sb.append("\n\nVertex filter:\n").append(getVertexStatusSet().toUserString());
        sb.append("\n\nSort:\n").append(getVertexSort().getVertexSortName());
        sb.append("\n\nLogic:\n").append(getLogicCoordinate().toUserString());
        sb.append("\n\nEdit:\n").append(getEditCoordinate().toUserString());
        return sb.toString();
    }

    EditCoordinate getEditCoordinate();

    TaxonomySnapshot getNavigationSnapshot();

    ManifoldCoordinateImmutable toManifoldCoordinateImmutable();

    default UUID getManifoldCoordinateUuid() {
        return getManifoldCoordinateUuid(this);
    }

    VertexSort getVertexSort();

    default int getAuthorNidForChanges() {
        return getEditCoordinate().getAuthorNidForChanges();
    }

    default int getPathNidForFilter() {
        return getViewStampFilter().getPathNidForFilter();
    }

    default int getPathNidForChanges() {
        return getPathNidForFilter();
    }

    default int[] sortVertexes(int[] vertexConceptNids) {
        return getVertexSort().sortVertexes(vertexConceptNids, toManifoldCoordinateImmutable());
    }

    /**
     * The coordinate that controls most aspects of the view. In some cases, the language stamp filter may provide
     * different status values, for example to allow display of retired descriptions or of retired concepts when pointed
     * to by active relationships in the view.
     *
     * This filter is used on the edges (relationships) in navigation operations, while {@link #getVertexStatusSet()}
     * is used on the vertexes (concepts) themselves.
     *
     * @return The view stamp filter,
     */
    StampFilter getViewStampFilter();

    /**
     * In most cases, this coordinate will be the equal to the coordinate returned by {@link #getViewStampFilter()},
     * But, it may be a different, depending on the construction - for example, a use case like returning inactive
     * vertexes (concepts) linked by active edges (relationships).
     *
     * This status set vertexes (source and destination concepts)
     * in navigation operations, while {@link #getViewStampFilter()} is used
     * on the edges (relationships) themselves.
     *
     * @return The vertex stamp filter,
     */
    StatusSet getVertexStatusSet();

    /**
     * All fields the same as the view stamp filter, except for the status set.
     * Having a vertex and view stamp filter allows for active relationships to point
     * to inactive concepts, as might be the case when you want to navigate retired concepts,
     * or concepts considered equivalent to retired concepts.
     * @return the filter to use for retrieving vertexes
     */
    StampFilter getVertexStampFilter();

    default LatestVersion<DescriptionVersion> getDescription(
            ConceptSpecification concept) {
        return this.getLanguageCoordinate().getDescription(concept.getNid(), this.getViewStampFilter());
    }

    default Optional<String> getDescriptionText(int conceptNid) {
        getLanguageCoordinate().getDescriptionText(conceptNid, this.getViewStampFilter());
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
        return this.getLanguageCoordinate().getDescription(conceptNid, this.getViewStampFilter());
    }


    default LatestVersion<DescriptionVersion> getDescription(
            List<SemanticChronology> descriptionList) {
        return this.getLanguageCoordinate().getDescription(descriptionList, this.getViewStampFilter());
    }

    PremiseSet getPremiseTypes();

    default NavigationCoordinateImmutable toNavigationCoordinateImmutable() {
        return getNavigationCoordinate().toNavigationCoordinateImmutable();
    }

    NavigationCoordinate getNavigationCoordinate();

    LogicCoordinate getLogicCoordinate();

    default LanguageCoordinate getLanguageCoordinate() {
        return getLanguageCoordinate();
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
        return this.getLogicCoordinate().getLogicGraphVersion(conceptNid, premiseType, this.getViewStampFilter());
    }


    default Optional<String> getFullyQualifiedName(int nid) {
        return this.getLanguageCoordinate().getFullyQualifiedNameText(nid, this.getViewStampFilter());
    }

    default String getVertexLabel(int vertexConceptNid) {
        return getVertexSort().getVertexLabel(vertexConceptNid,
                getLanguageCoordinate().toLanguageCoordinateImmutable(),
                getViewStampFilter().toStampFilterImmutable());
    }

    default String getVertexLabel(ConceptSpecification vertexConcept) {
        return getVertexLabel(vertexConcept.getNid());
    }

    default ImmutableList<String> getPreferredDescriptionTextList(int[] nidArray) {
        MutableList<String> results = Lists.mutable.empty();
        for (int nid: nidArray) {
            results.add(getPreferredDescriptionText(nid));
        }
        return results.toImmutable();
    }

    default ImmutableList<String> getPreferredDescriptionTextList(Collection<ConceptSpecification> conceptCollection) {
        MutableList<String> results = Lists.mutable.empty();
        for (ConceptSpecification conceptSpecification: conceptCollection) {
            results.add(getPreferredDescriptionText(conceptSpecification));
        }
        return results.toImmutable();
    }

    default ImmutableList<String> getFullyQualifiedNameTextList(int[] nidArray) {
        MutableList<String> results = Lists.mutable.empty();
        for (int nid: nidArray) {
            results.add(getFullyQualifiedDescriptionText(nid));
        }
        return results.toImmutable();
    }

    default ImmutableList<String> getFullyQualifiedNameTextList(Collection<ConceptSpecification> conceptCollection) {
        MutableList<String> results = Lists.mutable.empty();
        for (ConceptSpecification conceptSpecification: conceptCollection) {
            results.add(getFullyQualifiedDescriptionText(conceptSpecification));
        }
        return results.toImmutable();
    }


    default String getPreferredDescriptionText(int conceptNid) {
        try {
            return getLanguageCoordinate().getPreferredDescriptionText(conceptNid, getViewStampFilter())
                    .orElse("No desc for: " + Get.conceptDescriptionText(conceptNid));
        } catch (NoSuchElementException ex) {
            return ex.getLocalizedMessage();
        }
    }

    default String getPreferredDescriptionText(ConceptSpecification concept) {
        return getPreferredDescriptionText(concept.getNid());
    }

    default String getFullyQualifiedDescriptionText(int conceptNid) {
        return getLanguageCoordinate().getFullyQualifiedNameText(conceptNid, getViewStampFilter())
                .orElse("No desc for: " + Get.conceptDescriptionText(conceptNid));
    }

    default String getFullyQualifiedDescriptionText(ConceptSpecification concept) {
        return getFullyQualifiedDescriptionText(concept.getNid());
    }

    default LatestVersion<DescriptionVersion> getFullyQualifiedDescription(int conceptNid) {
        return getLanguageCoordinate().getFullyQualifiedDescription(conceptNid, getViewStampFilter());
    }

    default LatestVersion<DescriptionVersion> getFullyQualifiedDescription(ConceptSpecification concept) {
        return getFullyQualifiedDescription(concept.getNid());
    }


    default LatestVersion<DescriptionVersion> getPreferredDescription(int conceptNid) {
        return getLanguageCoordinate().getPreferredDescription(conceptNid, getViewStampFilter());
    }

    default LatestVersion<DescriptionVersion> getPreferredDescription(ConceptSpecification concept) {
        return getPreferredDescription(concept.getNid());
    }


    default OptionalInt getAcceptabilityNid(int descriptionNid, int dialectAssemblageNid) {
        ImmutableIntSet acceptabilityChronologyNids = Get.assemblageService().getSemanticNidsForComponentFromAssemblage(descriptionNid, dialectAssemblageNid);

        for (int acceptabilityChronologyNid: acceptabilityChronologyNids.toArray()) {
            SemanticChronology acceptabilityChronology = Get.assemblageService().getSemanticChronology(acceptabilityChronologyNid);
            LatestVersion<ComponentNidVersion> latestAcceptability = acceptabilityChronology.getLatestVersion(getViewStampFilter());
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
        return concept.getLogicalDefinition(getViewStampFilter(), premiseType, this.getLogicCoordinate());
    }

    default Optional<LogicalExpression> getInferredLogicalExpression(ConceptSpecification spec) {
        return getLogicCoordinate().getInferredLogicalExpression(spec.getNid(), this.getViewStampFilter());
    }

    default Optional<LogicalExpression> getInferredLogicalExpression(int conceptNid) {
        return getLogicCoordinate().getLogicalExpression(conceptNid, PremiseType.INFERRED, this.getViewStampFilter());
    }

    default String toFqnConceptString(Object object) {
        return toConceptString(object, this::getFullyQualifiedDescriptionText);
    }

    default String toPreferredConceptString(Object object) {
        return toConceptString(object, this::getPreferredDescriptionText);
    }

    default Optional<LogicalExpression> getLogicalExpression(int conceptNid, PremiseType premiseType) {
        ConceptChronology concept = Get.concept(conceptNid);
        LatestVersion<LogicGraphVersion> logicalDef = concept.getLogicalDefinition(getViewStampFilter(), premiseType, getLogicCoordinate());
        if (logicalDef.isPresent()) {
            return Optional.of(logicalDef.get().getLogicalExpression());
        }
        return Optional.empty();
    }

    default String toConceptString(Object object, Function<ConceptSpecification,String> toString) {
        StringBuilder sb = new StringBuilder();
        toConceptString(object, toString, sb);
        return sb.toString();
    }

    default void toConceptString(Object object, Function<ConceptSpecification,String> toString, StringBuilder sb) {
        if (object == null) {
            return;
        }
        if (object instanceof ConceptSpecification) {
            ConceptSpecification conceptSpecification = (ConceptSpecification) object;
            sb.append(toString.apply(conceptSpecification));
        } else if (object instanceof Collection) {

            if (object instanceof Set) {
                // a set, so order does not matter. Alphabetic order desirable.
                Set set = (Set) object;
                if (set.isEmpty()) {
                    toConceptString(set.toArray(), toString, sb);
                } else {
                    Object[] conceptSpecs = set.toArray();
                    Arrays.sort(conceptSpecs, (o1, o2) ->
                            NaturalOrder.compareStrings(toString.apply((ConceptSpecification) o1), toString.apply((ConceptSpecification) o2)));
                    toConceptString(conceptSpecs, toString, sb);
                }
            } else {
                // not a set, so order matters
                Collection collection = (Collection) object;
                toConceptString(collection.toArray(), toString, sb);
            }
        } else if (object.getClass().isArray()) {
            Object[] a = (Object[]) object;
            final int iMax = a.length - 1;
            if (iMax == -1) {
                sb.append("[]");
            } else {
                sb.append('[');
                int indent = sb.length();
                for (int i = 0; ; i++) {
                    if (i > 0) {
                        sb.append('\u200A');
                    }
                    sb.append(toConceptString(a[i], toString));
                    if (i == iMax) {
                        sb.append(']').toString();
                        return;
                    }
                    if (iMax > 0) {
                        sb.append(",\n");
                        for (int indentIndex = 0; indentIndex < indent; indentIndex++) {
                            sb.append('\u2004'); //
                        }
                    }
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
    }

    Activity getCurrentActivity();

    default int[] getRootNids() {
        return this.getNavigationSnapshot().getRootNids();
    }

    default int[] getChildNids(ConceptSpecification parent) {
        return getChildNids(parent.getNid());
    }
    default int[] getChildNids(int parentNid) {
        return this.getVertexSort().sortVertexes(this.getNavigationSnapshot().getTaxonomyChildConceptNids(parentNid),
                this.toManifoldCoordinateImmutable());
    }

    default boolean isChildOf(ConceptSpecification child, ConceptSpecification parent) {
        return isChildOf(child.getNid(), parent.getNid());
    }
    default boolean isChildOf(int childNid, int parentNid) {
        return this.getNavigationSnapshot().isChildOf(childNid, parentNid);
    }

    default boolean isLeaf(ConceptSpecification concept) {
        return isLeaf(concept.getNid());
    }
    default boolean isLeaf(int nid) {
        return this.getNavigationSnapshot().isLeaf(nid);
    }

    default boolean isKindOf(ConceptSpecification child, ConceptSpecification parent) {
        return isKindOf(child.getNid(), parent.getNid());
    }
    default boolean isKindOf(int childNid, int parentNid) {
        return this.getNavigationSnapshot().isKindOf(childNid, parentNid);
    }

    default  ImmutableIntSet getKindOfNidSet(ConceptSpecification kind) {
        return getKindOfNidSet(kind.getNid());
    }
    default ImmutableIntSet getKindOfNidSet(int kindNid) {
        return this.getNavigationSnapshot().getKindOfConcept(kindNid);
    }

    default boolean isDescendentOf(ConceptSpecification descendant, ConceptSpecification ancestor) {
        return isDescendentOf(descendant.getNid(), ancestor.getNid());
    }
    default boolean isDescendentOf(int descendantNid, int ancestorNid) {
        return this.getNavigationSnapshot().isDescendentOf(descendantNid, ancestorNid);
    }

    default ImmutableCollection<Edge> getParentEdges(int parentNid) {
        return this.getNavigationSnapshot().getTaxonomyParentLinks(parentNid);
    }
    default ImmutableCollection<Edge> getParentEdges(ConceptSpecification parent) {
        return getParentEdges(parent.getNid());
    }

    default ImmutableCollection<Edge> getChildEdges(ConceptSpecification child) {
        return getChildEdges(child.getNid());
    }
    default ImmutableCollection<Edge> getChildEdges(int childNid) {
        return this.getNavigationSnapshot().getTaxonomyChildLinks(childNid);
    }

    default ImmutableCollection<ConceptSpecification> getRoots() {
        return IntLists.immutable.of(getRootNids()).collect(nid -> Get.conceptSpecification(nid));
    }

    default String getPathString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getPreferredDescriptionText(getViewStampFilter().getPathNidForFilter()));
        return sb.toString();
    }

    /**
     * Gets the module nid.
     *
     * @return the module nid
     */
    default int getModuleNidForAnalog(Version version) {
        switch (getCurrentActivity()) {
            case DEVELOPING:
            case PROMOTING:
                if (version == null || version.getModuleNid() == 0 || version.getModuleNid() == Integer.MIN_VALUE ||
                        version.getModuleNid() == Integer.MAX_VALUE || version.getModuleNid() == TermAux.UNINITIALIZED_COMPONENT_ID.getNid()) {
                    return getEditCoordinate().getDefaultModuleNid();
                }
                return version.getModuleNid();
            case MODULARIZING:
                return getEditCoordinate().getDestinationModuleNid();
            case VIEWING:
                throw new IllegalStateException("Cannot make analog when viewing [1]. ");
            default:
                throw new UnsupportedOperationException(getCurrentActivity().name());
        }
    }

    default ConceptSpecification getModuleForAnalog(Version version) {
        return Get.conceptSpecification(getModuleNidForAnalog(version));
    }

    /**
     * Gets the path nid.
     *
     * @return the path nid
     */
    default int getPathNidForAnalog() {
        switch (getCurrentActivity()) {
            case DEVELOPING:
            case MODULARIZING:
                return getViewStampFilter().getPathNidForFilter();
            case PROMOTING:
                return getEditCoordinate().getPromotionPathNid();
            case VIEWING:
                throw new IllegalStateException("Cannot make analog when viewing [2]. ");
            default:
                throw new UnsupportedOperationException(getCurrentActivity().name());
        }
    }

    default ConceptSpecification getPathForAnalog() {
        return Get.conceptSpecification(getPathNidForAnalog());
    }

    public ManifoldCoordinate makeCoordinateAnalog(long classifyTimeInEpochMillis);
    
    public ManifoldCoordinate makeCoordinateAnalog(PremiseType premiseType);
    
	/**
	 * @param stampFilter - new stampFilter to use to in the new ManifoldCoordinate, for both the {@link ManifoldCoordinate#getViewStampFilter()} and
	 * {@link ManifoldCoordinate#getVertexStampFilter()} 
	 * @return a new manifold coordinate
	 */
	default ManifoldCoordinate makeCoordinateAnalog(StampFilter stampFilter) {
		return ManifoldCoordinateImmutable.make(stampFilter, this.getLanguageCoordinate(), this.getVertexSort(), stampFilter.getAllowedStates(), this.getNavigationCoordinate(),
				this.getLogicCoordinate(), this.getCurrentActivity(), this.getEditCoordinate());
	}

    default ManifoldCoordinate makeCoordinateAnalog(Instant classifyInstant) {
        return makeCoordinateAnalog(classifyInstant.toEpochMilli());
    }

    /**
     * @see #getWriteCoordinate(Transaction, Version)
     * @param transaction
     * @return
     */
    default WriteCoordinate getWriteCoordinate() {
        return getWriteCoordinate(null, null);
    }

    /**
     * @see #getWriteCoordinate(Transaction, Version)
     * @param transaction
     * @return
     */
    default WriteCoordinate getWriteCoordinate(Transaction transaction) {
        return getWriteCoordinate(transaction, null);
    }
    
    /**
     * @see #getWriteCoordinate(Transaction, Version, Status)
     * @param transaction
     * @param version
     * @return
     */
    default WriteCoordinate getWriteCoordinate(Transaction transaction, Version version) {
        return getWriteCoordinate(transaction, version, null);
    }

    /**
     * Return a WriteCoordinate based on {@link #getPathNidForAnalog()}, {@link #getModuleNidForAnalog(Version)}, {@link #getAuthorNidForChanges()}
     * @param transaction - optional - used if supplied
     * @param version - optional - used if supplied in {@link #getModuleForAnalog(Version)}
     * @param status - optional - used if supplied
     * @return the equivalent WriteCoordinate
     */
    default WriteCoordinate getWriteCoordinate(Transaction transaction, Version version, Status status) {
        return new WriteCoordinate() {
            @Override
            public Optional<Transaction> getTransaction() {
                return Optional.ofNullable(transaction);
            }
            
            @Override
            public int getPathNid() {
                return ManifoldCoordinate.this.getPathNidForAnalog();
            }
            
            @Override
            public int getModuleNid() {
                return ManifoldCoordinate.this.getModuleNidForAnalog(version);
            }
            
            @Override
            public int getAuthorNid() {
                return ManifoldCoordinate.this.getAuthorNidForChanges();
            }

            @Override
            public Status getStatus() {
                return status == null ? WriteCoordinate.super.getStatus() : status;
            }
        };
    }
}
