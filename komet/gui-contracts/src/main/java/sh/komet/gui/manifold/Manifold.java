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


package sh.komet.gui.manifold;

//~--- JDK imports ------------------------------------------------------------

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

import javafx.scene.Node;
import javafx.scene.control.Label;

import sh.isaac.MetaData;
import sh.isaac.api.ComponentProxy;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSnapshotService;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.coordinate.*;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.observable.coordinate.ObservableEditCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLogicCoordinate;
import sh.isaac.api.observable.coordinate.ObservableManifoldCoordinate;
import sh.isaac.api.observable.coordinate.ObservableStampCoordinate;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.interfaces.EditInFlight;

//~--- classes ----------------------------------------------------------------

/**
 * Manifold: Uniting various features, in this case an object that contains a set of coordinates and selections that
 * enable coordinated activities across a set of processes or graphical panels. In geometry, a coordinate system is a
 * system which uses one or more numbers, or coordinates, to uniquely determine the position of a point or other
 * geometric element on a manifold such as Euclidean space. ISAAC uses a number of coordinates, language coordinates,
 * stamp coordinates, edit coordinates to specify how a function should behave, in addition to current state information
 * such as a current position (selection). We use Manifold here to prevent confusion with other types of context,
 * context menus, etc.
 * <p>
 * Standard groups are singletons, the unlinked group may be multiple instances, with UUIDs to keep them unlinked
 * with other unlinked groups.
 * <p>
 * <p>
 * <p>
 * TODO: Move to general API when better refined.
 * TODO: Handle clearing the concept snapshot if any dependent fields are invalidated.
 *
 * @author kec
 */
public class Manifold
        implements StampCoordinateProxy, LanguageCoordinateProxy, LogicCoordinateProxy, ManifoldCoordinateProxy {

    private static final ConcurrentHashMap<ManifoldGroup, Manifold> MANIFOLD_MAP = new ConcurrentHashMap<>();

    private static final HashMap<String, Supplier<Node>> ICONOGRAPHIC_SUPPLIER = new HashMap<>();

    private static final ObservableSet<EditInFlight> EDITS_IN_PROCESS = FXCollections.observableSet();

    private static final SimpleListProperty<ComponentProxy> UNLINKED_HISTORY = new SimpleListProperty<>(null, MetaData.MANIFOLD_HISTORY____SOLOR.toExternalString(), FXCollections.observableList(new LinkedList<>()));

    private static int historySize = 50;

    public enum ManifoldGroup {
        UNLINKED("unlinked"), SEARCH("search"),
        INFERRED_GRAPH_NAVIGATION_ANY_NODE("inferred navigation-any node status"), FLWOR("flwor"), CLINICAL_STATEMENT("statement"),
        CORRELATION("correlation"), KOMET("KOMET"), CLASSIFICATON("classification"),
        LIST("list"), INFERRED_GRAPH_NAVIGATION_ACTIVE_NODES("inferred navigation-active nodes"),
        INFERRED_GRAPH_NAVIGATION_ACTIVE_FQN_NODES("inferred navigation-active fqn nodes"),
        STATED_GRAPH_NAVIGATION_ANY_NODE("stated navigation-any node status");

        private String groupName;
        private UUID groupUuid;

        private ManifoldGroup(String groupName) {
            this.groupName = groupName;
            this.groupUuid = UuidT5Generator.get(UUID.fromString("2e2c07eb-ecdb-5e90-812d-488b1c743272"), this.name());
        }

        public UUID getGroupUuid() {
            return groupUuid;
        }

        public String getGroupName() {
            return groupName;
        }

        public static Optional<ManifoldGroup> getFromGroupUuid(UUID groupUuid) {
            for (ManifoldGroup manifoldGroup : ManifoldGroup.values()) {
                if (manifoldGroup.groupUuid.equals(groupUuid)) {
                    return Optional.of(manifoldGroup);
                }
            }
            return Optional.empty();
        }

        public static Optional<ManifoldGroup> getFromGroupName(String groupName) {
            for (ManifoldGroup manifoldGroup : ManifoldGroup.values()) {
                if (manifoldGroup.groupName.equals(groupName)) {
                    return Optional.of(manifoldGroup);
                }
            }
            return Optional.empty();
        }
    }

    //~--- static initializers -------------------------------------------------

    static {
        ICONOGRAPHIC_SUPPLIER.put(ManifoldGroup.UNLINKED.getGroupName(), () -> new Label());
        ICONOGRAPHIC_SUPPLIER.put(ManifoldGroup.SEARCH.getGroupName(), () -> Iconography.SIMPLE_SEARCH.getIconographic());
        ICONOGRAPHIC_SUPPLIER.put(ManifoldGroup.INFERRED_GRAPH_NAVIGATION_ACTIVE_NODES.getGroupName(), () -> Iconography.TAXONOMY_ICON.getIconographic());
        ICONOGRAPHIC_SUPPLIER.put(ManifoldGroup.INFERRED_GRAPH_NAVIGATION_ACTIVE_FQN_NODES.getGroupName(), () -> Iconography.TAXONOMY_ICON.getIconographic());
        ICONOGRAPHIC_SUPPLIER.put(ManifoldGroup.INFERRED_GRAPH_NAVIGATION_ANY_NODE.getGroupName(), () -> Iconography.TAXONOMY_ICON.getIconographic());
        ICONOGRAPHIC_SUPPLIER.put(ManifoldGroup.STATED_GRAPH_NAVIGATION_ANY_NODE.getGroupName(), () -> Iconography.TAXONOMY_ICON.getIconographic());
        ICONOGRAPHIC_SUPPLIER.put(ManifoldGroup.FLWOR.getGroupName(), () -> Iconography.FLWOR_SEARCH.getIconographic());
        ICONOGRAPHIC_SUPPLIER.put(ManifoldGroup.CORRELATION.getGroupName(), () -> new Label("C"));
        ICONOGRAPHIC_SUPPLIER.put(ManifoldGroup.KOMET.getGroupName(), () -> new Label("K"));
        ICONOGRAPHIC_SUPPLIER.put(ManifoldGroup.CLASSIFICATON.getGroupName(), () -> Iconography.INFERRED.getIconographic());
        ICONOGRAPHIC_SUPPLIER.put(ManifoldGroup.LIST.getGroupName(), () -> Iconography.LIST.getIconographic());


        for (ManifoldGroup group: ManifoldGroup.values()) {
            if (group != ManifoldGroup.UNLINKED) {
                MANIFOLD_MAP.put(group, new Manifold(group.groupName, group.groupUuid, Get.configurationService()
                        .getUserConfiguration(Optional.empty()).getManifoldCoordinate().deepClone(),
                        Get.configurationService()
                                .getUserConfiguration(Optional.empty()).getEditCoordinate()));
            }
        }
    }

    //~--- fields --------------------------------------------------------------

    private final SimpleObjectProperty<ConceptSnapshotService> conceptSnapshotProperty = new SimpleObjectProperty<>();
    final SimpleListProperty<ComponentProxy> manifoldSelection;
    final SimpleListProperty<ComponentProxy> manifoldHistory;
    final String groupName;
    final UUID manifoldUuid;
    final ObservableManifoldCoordinate observableManifoldCoordinate;
    final ObservableEditCoordinate observableEditCoordinate;
    private Runnable selectionPreferenceUpdater;

    //~--- constructors --------------------------------------------------------


    private Manifold(String groupName,
                     UUID manifoldUuid,
                     ObservableManifoldCoordinate observableManifoldCoordinate,
                     ObservableEditCoordinate editCoordinate) {
        if (observableManifoldCoordinate.getStampCoordinate() == null) {
            throw new NullPointerException("Manifold.getStampCoordinate() cannot be null.");
        }
        if (observableManifoldCoordinate.getLanguageCoordinate() == null) {
            throw new NullPointerException("Manifold.getLanguageCoordinate() cannot be null.");
        }
        if (observableManifoldCoordinate.getLogicCoordinate() == null) {
            throw new NullPointerException("Manifold.getLogicCoordinate() cannot be null.");
        }
        this.groupName = groupName;
        this.manifoldUuid = manifoldUuid;
        this.observableManifoldCoordinate = observableManifoldCoordinate;
        this.observableEditCoordinate = editCoordinate;
        manifoldSelection = new SimpleListProperty(this,
                MetaData.MANIFOLD_SELECTION____SOLOR.toExternalString(),
                FXCollections.observableList(new LinkedList<>()));
        this.manifoldSelection.addListener(this::selectionListChanged);
        if (groupName.equals(ManifoldGroup.UNLINKED.getGroupName())) {
            manifoldHistory = UNLINKED_HISTORY;
        } else {
            manifoldHistory  = new SimpleListProperty<>(this, MetaData.MANIFOLD_HISTORY____SOLOR.toExternalString(), FXCollections.observableList(new LinkedList<>()));
        }
    }

    //~--- methods -------------------------------------------------------------

    public void setSelectionPreferenceUpdater(Runnable selectionPreferenceUpdater) {
        this.selectionPreferenceUpdater = selectionPreferenceUpdater;
    }
    public SimpleListProperty<ComponentProxy> manifoldSelectionProperty() {
        return manifoldSelection;
    }


    public void selectionListChanged(ListChangeListener.Change<? extends ComponentProxy> c) {
        // need to listen to update history records...
        while (c.next()) {
            if (c.wasPermutated()) {
                for (int i = c.getFrom(); i < c.getTo(); ++i) {
                    //permutate
                    //If only permutation, then no new history record is added,
                    //only the order could change, but we will ignore the order change for now.
                }
            } else if (c.wasUpdated()) {
                //update item
                //We don't do updates within an item...
            } else {
                for (ComponentProxy remitem : c.getRemoved()) {
                    // We don't remove items from history
                    // remitem.remove(Outer.this);
                }
                for (ComponentProxy additem : c.getAddedSubList()) {
                    ComponentProxy historyRecord = new ComponentProxy(
                            additem.getNid(),
                            getFullySpecifiedDescriptionText(additem.getNid()));
                    addHistory(historyRecord, manifoldHistory);
                }
            }
        }
        if (this.selectionPreferenceUpdater != null) {
            this.selectionPreferenceUpdater.run();
        }
    }

    @Override
    public Manifold deepClone() {
        throw new UnsupportedOperationException("Manifolds are singletons... Cloning would violate the singleton aspect...");
    }

    public String groupNameProperty() {
        return groupName;
    }

    /**
     * Get the manifold for a control group.
     *
     * @param group
     * @return a new manifold on each call.
     */
    public static final Manifold get(ManifoldGroup group) {
        if (group == ManifoldGroup.UNLINKED) {
            // create a new one, with a new UUID...
            return create(group.groupName, UUID.randomUUID());
        }
        return MANIFOLD_MAP.get(group);
    }

    public static final Manifold get(String groupName) {
        if (groupName.equals(ManifoldGroup.UNLINKED.getGroupName())) {
            // create a new one, with a new UUID...
            return create(groupName, UUID.randomUUID());
        }
        Optional<ManifoldGroup> optionalGroup = ManifoldGroup.getFromGroupName(groupName);
        if (optionalGroup.isPresent()) {
            return MANIFOLD_MAP.get(optionalGroup.get());
        }
        return create(groupName, UUID.randomUUID());
    }

    /**
     * Get a manifold for local use within a control group that is not linked to the selection of other concept
     * presentations.
     *
     * @param groupName
     * @return a new manifold on each call.
     */
    public static final Manifold create(String groupName, UUID manifoldUuid) {
        Manifold manifold = newManifold(
                groupName,
                manifoldUuid,
                Get.configurationService()
                        .getUserConfiguration(Optional.empty()).getManifoldCoordinate().deepClone(),
                Get.configurationService()
                        .getUserConfiguration(Optional.empty()).getEditCoordinate());
        return manifold;
    }


    private static Manifold newManifold(String name,
                                       UUID manifoldUuid,
                                       ObservableManifoldCoordinate observableManifoldCoordinate,
                                       ObservableEditCoordinate editCoordinate) {
        Manifold manifold = new Manifold(name, manifoldUuid, observableManifoldCoordinate, editCoordinate);
        if (manifold.getStampCoordinate() == null) {
            throw new NullPointerException("Manifold.getStampCoordinate() cannot be null.");
        }
        if (manifold.getLanguageCoordinate() == null) {
            throw new NullPointerException("Manifold.getLanguageCoordinate() cannot be null.");
        }
        if (manifold.getLogicCoordinate() == null) {
            throw new NullPointerException("Manifold.getLogicCoordinate() cannot be null.");
        }
        return manifold;
    }

    public LatestVersion<String> getDescriptionText(int conceptNid) {
        LatestVersion<DescriptionVersion> latestVersion = getDescription(conceptNid, this);
        if (latestVersion.isPresent()) {
            LatestVersion<String> latestText = new LatestVersion<>(latestVersion.get().getText());
            for (DescriptionVersion contradition : latestVersion.contradictions()) {
                latestText.addLatest(contradition.getText());
            }
            return latestText;
        }
        return new LatestVersion<>();
    }

    @Override
    public String toString() {
        return "Manifold{groupName=" + groupName + ", manifoldUuid=" +
                manifoldUuid + ", observableManifoldCoordinate=" + observableManifoldCoordinate +
                ", editCoordinate=" + observableEditCoordinate + ", manifoldSelection=" +
                manifoldSelection.get() + '}';
    }


    private static void addHistory(ComponentProxy history, ObservableList<ComponentProxy> historyDequeue) {
        if (history.getNid() == MetaData.UNINITIALIZED_COMPONENT____SOLOR.getNid()) {
            return;
        }
        if (historyDequeue.isEmpty() || !historyDequeue.get(0).equals(history)) {
            historyDequeue.add(0, history);

            while (historyDequeue.size() > historySize) {
                historyDequeue.remove(historySize, historyDequeue.size());
            }
        }
    }

    //~--- get methods ---------------------------------------------------------
    public ConceptSnapshotService getConceptSnapshotService() {
        if (conceptSnapshotProperty.getValue() == null) {
            conceptSnapshotProperty.set(Get.conceptService()
                    .getSnapshot(observableManifoldCoordinate));
        }
        return conceptSnapshotProperty.get();
    }

    /**
     * @return
     * @deprecated we need to associate the edit coordinate with an authenticated
     * session. For the FX gui, it is associated with the FxGet as a means to access the
     * session.
     */
    @Deprecated
    public ObservableEditCoordinate getEditCoordinate() {
        return observableEditCoordinate;
    }

    public Optional<ConceptChronology> getOptionalFocusedConcept(int indexInSelection) {

        if (indexInSelection < 0) {
           return Optional.empty();
        }
        if (indexInSelection >= manifoldSelection.size()) {
            return Optional.empty();
        }
        ComponentProxy component = manifoldSelection.get(indexInSelection);
        if (Get.identifierService().getObjectTypeForComponent(component.getNid()) == IsaacObjectType.CONCEPT) {
            return Optional.of(Get.concept(component.getNid()));
        }

        return Optional.empty();
    }

    public Optional<ComponentProxy> getOptionalFocusedComponent(int indexInSelection) {

        if (indexInSelection < 0) {
            return Optional.empty();
        }
        if (indexInSelection >= manifoldSelection.size()) {
            return Optional.empty();
        }
        return Optional.of(manifoldSelection.get(indexInSelection));
    }


    //~--- get methods ---------------------------------------------------------


    public String getGroupName() {
        return groupName;
    }

    public static Set<String> getGroupNames() {
        return ICONOGRAPHIC_SUPPLIER.keySet();
    }

    public SimpleListProperty<ComponentProxy> getHistoryRecords() {
        return manifoldHistory;
    }

    public Optional<Node> getOptionalIconographic() {
        return getOptionalIconographic(getGroupName());
    }

    public static Optional<Node> getOptionalIconographic(String groupName) {
        if (ICONOGRAPHIC_SUPPLIER.get(groupName) != null) {
            return Optional.of(ICONOGRAPHIC_SUPPLIER.get(groupName)
                    .get());
        }
        return Optional.empty();
    }

    @Override
    public ObservableLanguageCoordinate getLanguageCoordinate() {
        return this.observableManifoldCoordinate.getLanguageCoordinate();
    }

    @Override
    public ObservableLogicCoordinate getLogicCoordinate() {
        return this.observableManifoldCoordinate.getLogicCoordinate();
    }

    @Override
    public ObservableManifoldCoordinate getManifoldCoordinate() {
        return observableManifoldCoordinate;
    }

    public UUID getManifoldUuid() {
        return manifoldUuid;
    }

    @Override
    public ObservableStampCoordinate getStampCoordinate() {
        return this.observableManifoldCoordinate.getStampCoordinate();
    }

    @Override
    public Optional<? extends StampCoordinate> optionalDestinationStampCoordinate() {
        return this.observableManifoldCoordinate.optionalDestinationStampCoordinate();
    }

    public void addEditInFlight(EditInFlight editInFlight) {
        EDITS_IN_PROCESS.add(editInFlight);
        editInFlight.addCompletionListener((observable, oldValue, newValue) -> {
            EDITS_IN_PROCESS.remove(editInFlight);
        });
    }

    @Override
    public Optional<LanguageCoordinate> getNextPriorityLanguageCoordinate() {
        return this.observableManifoldCoordinate.getNextPriorityLanguageCoordinate();
    }

    @Override
    public LatestVersion<DescriptionVersion> getDefinitionDescription(List<SemanticChronology> descriptionList, StampCoordinate stampCoordinate) {
        return this.observableManifoldCoordinate.getDefinitionDescription(descriptionList, stampCoordinate);
    }

    @Override
    public int[] getModulePreferenceListForLanguage() {
        return this.observableManifoldCoordinate.getModulePreferenceListForLanguage();
    }

    @Override
    public List<ConceptSpecification> getModulePreferenceOrderForVersions() {
        return this.observableManifoldCoordinate.getModulePreferenceOrderForVersions();
    }

    @Override
    public Set<ConceptSpecification> getModuleSpecifications() {
        return this.observableManifoldCoordinate.getModuleSpecifications();
    }

    @Override
    public ConceptSpecification getLanguageConcept() {
        return this.observableManifoldCoordinate.getLanguageConcept();
    }

    @Override
    public ConceptSpecification[] getDescriptionTypeSpecPreferenceList() {
        return this.observableManifoldCoordinate.getDescriptionTypeSpecPreferenceList();
    }

    @Override
    public ConceptSpecification[] getDialectAssemblageSpecPreferenceList() {
        return this.observableManifoldCoordinate.getDialectAssemblageSpecPreferenceList();
    }

    @Override
    public ConceptSpecification[] getModuleSpecPreferenceListForLanguage() {
        return this.observableManifoldCoordinate.getModuleSpecPreferenceListForLanguage();
    }

    @Override
    public Set<ConceptSpecification> getAuthorSpecifications() {
        return this.observableManifoldCoordinate.getAuthorSpecifications();
    }

    @Override
    public NidSet getAuthorNids() {
        return this.observableManifoldCoordinate.getAuthorNids();
    }

    @Override
    public void putExternal(ByteArrayDataBuffer out) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StampCoordinateReadOnly getStampCoordinateReadOnly() {
        return getStampCoordinate().getStampCoordinateReadOnly();
    }
}

