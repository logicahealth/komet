/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
package sh.komet.gui.util;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.controlsfx.control.PropertySheet;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.SingleAssemblageSnapshot;
import sh.isaac.api.StaticIsaacCache;
import sh.isaac.api.TaxonomySnapshot;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.concept.ConceptSnapshot;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.component.semantic.version.brittle.Nid1_Int2_Version;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.StampPathImmutable;
import sh.isaac.api.observable.coordinate.*;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.api.preferences.PreferencesService;
import sh.isaac.api.util.NaturalOrder;
import sh.komet.gui.contract.*;
import sh.komet.gui.contract.preferences.KometPreferences;
import sh.komet.gui.contract.preferences.PersonaChangeListener;
import sh.komet.gui.contract.preferences.PersonaItem;
import sh.komet.gui.control.concept.PropertySheetItemConceptConstraintWrapper;
import sh.komet.gui.control.concept.PropertySheetItemConceptWrapper;
import sh.komet.gui.control.property.PropertySheetItem;
import sh.komet.gui.control.property.SessionProperty;
import sh.komet.gui.interfaces.ComponentList;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.manifold.GraphAmalgamWithManifold;
import sh.komet.gui.provider.StatusMessageProvider;

import javax.inject.Singleton;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static sh.komet.gui.contract.preferences.GraphConfigurationItem.DEFINING_ACTIVE;

/**
 *
 * @author kec
 */
@Service
@Singleton
public class FxGet implements StaticIsaacCache {
    private static final HashMap<Manifold.ManifoldGroup, Manifold> MANIFOLDS = new HashMap<>();
    private static final HashMap<UuidStringKey, Manifold> MANIFOLD_FOR_MANIFOLD_COORDINATE = new HashMap<>();


    private static final ConcurrentHashMap<UuidStringKey, ComponentList> componentListMap = new ConcurrentHashMap();

    private static final ObservableList<UuidStringKey> componentListKeys = FXCollections.observableArrayList(new ArrayList<>());

    private static DialogService DIALOG_SERVICE = null;
    private static RulesDrivenKometService RULES_DRIVEN_KOMET_SERVICE = null;
    private static StatusMessageProvider STATUS_MESSAGE_PROVIDER = null;
    private static FxConfiguration FX_CONFIGURATION = null;
    // TODO make SEARCHER_LIST behave like a normal lookup service. 
    private static final List<GuiSearcher> SEARCHER_LIST = new ArrayList<>();
    // TODO make SEARCHER_LIST behave like a normal lookup service. 
    private static final List<GuiConceptBuilder> BUILDER_LIST = new ArrayList<>();

    private static final SimpleStringProperty CONFIGURATION_NAME_PROPERTY = new SimpleStringProperty(null, MetaData.CONFIGURATION_NAME____SOLOR.toExternalString(), "viewer");
    private static final ObservableMap<UuidStringKey, GraphAmalgamWithManifold> GRAPH_CONFIGURATIONS = FXCollections.observableHashMap();
    private static final ObservableList<UuidStringKey> GRAPH_CONFIGURATION_KEY_LIST = FXCollections.observableArrayList();

    static {



        // TODO: will this listener go away as part of garbage collection?
        GRAPH_CONFIGURATIONS.addListener((MapChangeListener.Change<? extends UuidStringKey, ? extends TaxonomySnapshot> change) -> {
            if (change.wasAdded()) {
                GRAPH_CONFIGURATION_KEY_LIST.add(change.getKey());
            }
            if (change.wasRemoved()) {            
                GRAPH_CONFIGURATION_KEY_LIST.remove(change.getKey());
            }
        });

    }

    public static List<GuiSearcher> searchers() {
        return SEARCHER_LIST;
    }

    public static List<GuiConceptBuilder> builders() {
        return BUILDER_LIST;
    }

    public static DialogService dialogs() {
        if (DIALOG_SERVICE == null) {
            DIALOG_SERVICE = Get.service(DialogService.class);
        }
        return DIALOG_SERVICE;
    }

    public static StatusMessageService statusMessageService() {
        if (STATUS_MESSAGE_PROVIDER == null) {
            STATUS_MESSAGE_PROVIDER = new StatusMessageProvider();
        }
        return STATUS_MESSAGE_PROVIDER;
    }

    public static RulesDrivenKometService rulesDrivenKometService() {
        if (RULES_DRIVEN_KOMET_SERVICE == null) {
            RULES_DRIVEN_KOMET_SERVICE = Get.service(RulesDrivenKometService.class);
        }
        return RULES_DRIVEN_KOMET_SERVICE;
    }

    public static FxConfiguration fxConfiguration() {
        if (FX_CONFIGURATION == null) {
            FX_CONFIGURATION = new FxConfiguration();
        }
        return FX_CONFIGURATION;
    }

    public static KometPreferences kometPreferences() {
        return Get.service(KometPreferences.class);
    }

    public static SimpleStringProperty configurationNameProperty() {
        return CONFIGURATION_NAME_PROPERTY;
    }

    public static void setConfigurationName(String configurationName) {
        FxGet.CONFIGURATION_NAME_PROPERTY.set(configurationName);
    }
    
    public static String configurationName() {
        return FxGet.CONFIGURATION_NAME_PROPERTY.get();
    }

    private static final SimpleObjectProperty<UuidStringKey> defaultViewKeyProperty = new SimpleObjectProperty<>(null,
            TermAux.VIEW_COORDINATE_KEY.toExternalString(),
            DEFINING_ACTIVE);

    public static UuidStringKey defaultViewKey() {
        return defaultViewKeyProperty.get();
    }
    public static SimpleObjectProperty<UuidStringKey> defaultViewKeyProperty() {
        return defaultViewKeyProperty;
    }
    public static  void setDefaultViewKey(UuidStringKey defaultViewKey) {
        defaultViewKeyProperty.set(defaultViewKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        DIALOG_SERVICE = null;
        RULES_DRIVEN_KOMET_SERVICE = null;
        STATUS_MESSAGE_PROVIDER = null;
        FX_CONFIGURATION = null;
        Platform.runLater(() -> {
            GRAPH_CONFIGURATIONS.clear();
            GRAPH_CONFIGURATION_KEY_LIST.clear();
        });
        
    }

    public static PreferencesService preferenceService() {
        return Get.preferencesService();
    }

    public static IsaacPreferences systemNode(Class<?> c) {
        return preferenceService().getSystemPreferences().node(c);
    }

    public static IsaacPreferences userNode(Class<?> c) {
        return preferenceService().getUserPreferences().node(c);
    }

    public static IsaacPreferences configurationNode(Class<?> c) {
        return preferenceService().getConfigurationPreferences().node(c);
    }

    public static List<PropertySheet.Item> constraintPropertyItemsForAssemblageSemantic(ConceptSpecification assemblageConcept, Manifold manifold) {
        return propertyItemsForAssemblageSemantic(assemblageConcept, manifold, true);
    }

    public static List<PropertySheet.Item> propertyItemsForAssemblageSemantic(ConceptSpecification assemblageConcept, Manifold manifold) {
        return propertyItemsForAssemblageSemantic(assemblageConcept, manifold, false);
    }

    private static List<PropertySheet.Item> propertyItemsForAssemblageSemantic(ConceptSpecification assemblageConcept, Manifold manifold, boolean forConstraints) {
        TreeMap<Integer, ConceptSpecification> fieldIndexToFieldConcept = new TreeMap<>();
        TreeMap<Integer, ConceptSpecification> fieldIndexToFieldDataType = new TreeMap<>();
        List<PropertySheet.Item> items = new ArrayList();
        OptionalInt optionalSemanticConceptNid = Get.assemblageService().getSemanticTypeConceptForAssemblage(assemblageConcept, manifold.getStampFilter());

        if (optionalSemanticConceptNid.isPresent()) {
            int semanticConceptNid = optionalSemanticConceptNid.getAsInt();
            ImmutableIntSet semanticTypeOfFields = Get.assemblageService().getSemanticNidsForComponentFromAssemblage(semanticConceptNid, TermAux.SEMANTIC_FIELD_DATA_TYPES_ASSEMBLAGE.getNid());
            for (int nid : semanticTypeOfFields.toArray()) { // one member, "Concept field": 1
                SemanticChronology semanticTypeField = Get.assemblageService().getSemanticChronology(nid);
                LatestVersion<Version> latestSemanticTypeField = semanticTypeField.getLatestVersion(manifold.getStampFilter());
                Nid1_Int2_Version latestSemanticTypeFieldVersion = (Nid1_Int2_Version) latestSemanticTypeField.get();
                fieldIndexToFieldDataType.put(latestSemanticTypeFieldVersion.getInt2(), Get.concept(latestSemanticTypeFieldVersion.getNid1()));
            }

            ImmutableIntSet assemblageSemanticFields = Get.assemblageService().getSemanticNidsForComponentFromAssemblage(assemblageConcept.getNid(), MetaData.SEMANTIC_FIELDS_ASSEMBLAGE____SOLOR.getNid());
            for (int nid : assemblageSemanticFields.toArray()) {
                SemanticChronology semanticField = Get.assemblageService().getSemanticChronology(nid);
                LatestVersion<Version> latestSemanticField = semanticField.getLatestVersion(manifold.getStampFilter());
                Nid1_Int2_Version latestSemanticFieldVersion = (Nid1_Int2_Version) latestSemanticField.get();
                fieldIndexToFieldConcept.put(latestSemanticFieldVersion.getInt2(), Get.concept(latestSemanticFieldVersion.getNid1()));
            }
        } else {
            FxGet.statusMessageService().reportStatus("Cannot find semantic type for " + Get.conceptDescriptionText(assemblageConcept.getNid()));
        }

        for (int i = 0; i < fieldIndexToFieldConcept.size(); i++) {
            ConceptSpecification fieldConcept = fieldIndexToFieldConcept.get(i);
            ConceptSpecification fieldDataType = fieldIndexToFieldDataType.get(i);
            if (fieldDataType.getNid() == MetaData.COMPONENT_FIELD____SOLOR.getNid()) {
                SimpleObjectProperty property = new SimpleObjectProperty(null, fieldConcept.toExternalString());
                if (forConstraints) {
                    items.add(new PropertySheetItemConceptConstraintWrapper(
                            new PropertySheetItemConceptWrapper(manifold, property, TermAux.UNINITIALIZED_COMPONENT_ID.getNid()), manifold, manifold.getPreferredDescriptionText(fieldConcept)));
                } else {
                    items.add(new PropertySheetItemConceptWrapper(manifold, property, TermAux.UNINITIALIZED_COMPONENT_ID.getNid()));
                }

            } else if (fieldDataType.getNid() == MetaData.CONCEPT_FIELD____SOLOR.getNid()) {
                SimpleObjectProperty property = new SimpleObjectProperty(null, fieldConcept.toExternalString());
                if (forConstraints) {
                    items.add(new PropertySheetItemConceptConstraintWrapper(
                            new PropertySheetItemConceptWrapper(manifold, property, TermAux.UNINITIALIZED_COMPONENT_ID.getNid()), manifold, manifold.getPreferredDescriptionText(fieldConcept)));
                } else {
                    items.add(new PropertySheetItemConceptWrapper(manifold, property, TermAux.UNINITIALIZED_COMPONENT_ID.getNid()));
                }
            } else if (fieldDataType.getNid() == MetaData.BOOLEAN_FIELD____SOLOR.getNid()) {
                SimpleBooleanProperty property = new SimpleBooleanProperty(null, fieldConcept.toExternalString());
                items.add(new PropertySheetItem(property, manifold));
            } else if (fieldDataType.getNid() == MetaData.ARRAY_FIELD____SOLOR.getNid()) {
                SimpleObjectProperty property = new SimpleObjectProperty(null, fieldConcept.toExternalString());
                items.add(new PropertySheetItem(property, manifold));
            } else if (fieldDataType.getNid() == MetaData.BYTE_ARRAY_FIELD____SOLOR.getNid()) {
                SimpleObjectProperty property = new SimpleObjectProperty(null, fieldConcept.toExternalString());
                items.add(new PropertySheetItem(property, manifold));
            } else if (fieldDataType.getNid() == MetaData.DOUBLE_FIELD____SOLOR.getNid()) {
                SimpleDoubleProperty property = new SimpleDoubleProperty(null, fieldConcept.toExternalString());
            } else if (fieldDataType.getNid() == MetaData.FLOAT_FIELD____SOLOR.getNid()) {
                SimpleFloatProperty property = new SimpleFloatProperty(null, fieldConcept.toExternalString());
                items.add(new PropertySheetItem(property, manifold));
            } else if (fieldDataType.getNid() == MetaData.INTEGER_FIELD____SOLOR.getNid()) {
                SimpleIntegerProperty property = new SimpleIntegerProperty(null, fieldConcept.toExternalString());
                items.add(new PropertySheetItem(property, manifold));
            } else if (fieldDataType.getNid() == MetaData.LOGICAL_EXPRESSION_FIELD____SOLOR.getNid()) {
                SimpleObjectProperty property = new SimpleObjectProperty(null, fieldConcept.toExternalString());
                items.add(new PropertySheetItem(property, manifold));
            } else if (fieldDataType.getNid() == MetaData.LONG_FIELD____SOLOR.getNid()) {
                SimpleLongProperty property = new SimpleLongProperty(null, fieldConcept.toExternalString());
                items.add(new PropertySheetItem(property, manifold));
            } else if (fieldDataType.getNid() == MetaData.STRING_FIELD____SOLOR.getNid()) {
                SimpleStringProperty property = new SimpleStringProperty(null, fieldConcept.toExternalString());
                items.add(new PropertySheetItem(property, manifold));
            } else if (fieldDataType.getNid() == MetaData.POLYMORPHIC_FIELD____SOLOR.getNid()) {
                SimpleObjectProperty property = new SimpleObjectProperty(null, fieldConcept.toExternalString());
                items.add(new PropertySheetItem(property, manifold));
            } else if (fieldDataType.getNid() == MetaData.UUID_FIELD____SOLOR.getNid()) {
                SimpleObjectProperty property = new SimpleObjectProperty(null, fieldConcept.toExternalString());
                items.add(new PropertySheetItem(property, manifold));
            }
        }
        return items;
    }

    // GetProperties for assemblage... Add to general API?
    // Leave property sheet in gui api. 
    
    public static Subject subject() {
        return SecurityUtils.getSubject();
    }
    
    public static ConceptSpecification currentUser() {
        return (ConceptSpecification) SecurityUtils.getSubject().getSession().getAttribute(SessionProperty.USER_SESSION_CONCEPT);
    }



    public static ObservableEditCoordinate editCoordinate() {
        return EditCoordinate.get();
    }

    private static ObservableMap<UuidStringKey, StampPathImmutable> PATHS = FXCollections.observableMap(new TreeMap<>());
    private static ObservableMap<UuidStringKey, ObservableLanguageCoordinate> LANGUAGE_COORDINATES = FXCollections.observableMap(new TreeMap<>());
    private static ObservableMap<UuidStringKey, ObservableLogicCoordinate>    LOGIC_COORDINATES = FXCollections.observableMap(new TreeMap<>());
    private static ObservableMap<UuidStringKey, ObservableManifoldCoordinate> MANIFOLD_COORDINATES = FXCollections.observableMap(new TreeMap<>());
    private static final ObservableList<UuidStringKey> PATH_COORDINATE_KEY_LIST = FXCollections.observableArrayList();
    private static final ObservableList<UuidStringKey> LANGUAGE_COORDINATE_KEY_LIST = FXCollections.observableArrayList();
    private static final ObservableList<UuidStringKey> LOGIC_COORDINATE_KEY_LIST = FXCollections.observableArrayList();
    private static final ObservableList<UuidStringKey> MANIFOLD_COORDINATE_KEY_LIST = FXCollections.observableArrayList();

    private static void pathChangeListener(MapChangeListener.Change<? extends UuidStringKey, ? extends StampPathImmutable> change) {
        if (change.wasAdded()) {
            PATH_COORDINATE_KEY_LIST.add(change.getKey());
        }
        if (change.wasRemoved()) {
            PATH_COORDINATE_KEY_LIST.remove(change.getKey());
        }
    }

    private static void languageChangeListener(MapChangeListener.Change<? extends UuidStringKey, ? extends ObservableLanguageCoordinate> change) {
        if (change.wasAdded()) {
            LANGUAGE_COORDINATE_KEY_LIST.add(change.getKey());
        }
        if (change.wasRemoved()) {
            LANGUAGE_COORDINATE_KEY_LIST.remove(change.getKey());
        }
    }

    private static void logicChangeListener(MapChangeListener.Change<? extends UuidStringKey, ? extends ObservableLogicCoordinate> change) {
        if (change.wasAdded()) {
            LOGIC_COORDINATE_KEY_LIST.add(change.getKey());
        }
        if (change.wasRemoved()) {
            LOGIC_COORDINATE_KEY_LIST.remove(change.getKey());
        }
    }
    private static void manifoldChangeListener(MapChangeListener.Change<? extends UuidStringKey, ? extends ObservableManifoldCoordinate> change) {
        if (change.wasAdded()) {
            MANIFOLD_COORDINATE_KEY_LIST.add(change.getKey());
        }
        if (change.wasRemoved()) {
            MANIFOLD_COORDINATE_KEY_LIST.remove(change.getKey());
        }
    }
    static {
        PATHS.addListener(FxGet::pathChangeListener);
        LANGUAGE_COORDINATES.addListener(FxGet::languageChangeListener);
        LOGIC_COORDINATES.addListener(FxGet::logicChangeListener);
        MANIFOLD_COORDINATES.addListener(FxGet::manifoldChangeListener);
    }

    public static ObservableMap<UuidStringKey, StampPathImmutable> pathCoordinates() {
        if (PATHS.isEmpty()) {
            //TODO add commit listener, and update when new semantic or a commit.
            Get.identifierService().getNidsForAssemblage(TermAux.PATH_ASSEMBLAGE).forEach(semanticNid -> {
                SemanticChronology pathConceptSemantic = Get.assemblageService().getSemanticChronology(semanticNid);
                StampPathImmutable path = StampPathImmutable.make(pathConceptSemantic.getReferencedComponentNid());
                String pathDescription = Get.defaultCoordinate().getPreferredDescriptionText(path.getPathConceptNid());
                UuidStringKey pathKey = new UuidStringKey(path.getPathCoordinateUuid(), pathDescription);
                PATHS.put(pathKey, path);
            });
        }
        return PATHS;
    }
    public static ObservableMap<UuidStringKey, ObservableLanguageCoordinate> languageCoordinates() {
        return LANGUAGE_COORDINATES;
    }
    public static ObservableMap<UuidStringKey, ObservableLogicCoordinate> logicCoordinates() {
        return LOGIC_COORDINATES;
    }
    public static ObservableMap<UuidStringKey, ObservableManifoldCoordinate> manifoldCoordinates() {
        return MANIFOLD_COORDINATES;
    }
    public static ObservableList<UuidStringKey> pathCoordinateKeys() {
        return PATH_COORDINATE_KEY_LIST;
    }
    public static ObservableList<UuidStringKey> languageCoordinateKeys() {
        return LANGUAGE_COORDINATE_KEY_LIST;
    }
    public static ObservableList<UuidStringKey> logicCoordinateKeys() {
        return LOGIC_COORDINATE_KEY_LIST;
    }
    public static ObservableList<UuidStringKey> manifoldCoordinateKeys() {
        return MANIFOLD_COORDINATE_KEY_LIST;
    }


    
    public static ObservableList<UuidStringKey> graphConfigurationKeys() {
        return GRAPH_CONFIGURATION_KEY_LIST;
    }
    
    public static GraphAmalgamWithManifold graphConfiguration(UuidStringKey configurationName) {
        return GRAPH_CONFIGURATIONS.get(configurationName);
    }

    public static Manifold manifoldForManifoldCoordinate(UuidStringKey configurationName) {
        return MANIFOLD_FOR_MANIFOLD_COORDINATE.get(configurationName);
    }
    public static void setManifoldForManifoldCoordinate(UuidStringKey manifoldCoordinateKey, Manifold manifold) {
        MANIFOLD_FOR_MANIFOLD_COORDINATE.put(manifoldCoordinateKey, manifold);
    }

    public static void addGraphConfiguration(UuidStringKey configurationName, GraphAmalgamWithManifold taxonomyConfiguration) {
        GRAPH_CONFIGURATIONS.put(configurationName, taxonomyConfiguration);
    }

    public static void removeGraphConfiguration(String configurationName) {
        GRAPH_CONFIGURATIONS.remove(configurationName);
    }


    public static TaxonomySnapshot graphSnapshot(UuidStringKey configurationName) {
        return graphConfiguration(configurationName);
    }


    public static <T extends ExplorationNode> Optional<NodeFactory<T>> nodeFactory(ConceptSpecification nodeSpecConcept) {
        ImmutableIntSet semanticNids = Get.assemblageService().getSemanticNidsForComponentFromAssemblage(nodeSpecConcept.getNid(), TermAux.PROVIDER_CLASS_ASSEMBLAGE.getNid());
        for (int nid: semanticNids.toArray()) {
            SemanticChronology chronology = Get.assemblageService().getSemanticChronology(nid);
            LatestVersion<StringVersion> optionalProviderClassStr = chronology.getLatestVersion(FxGet.manifold(Manifold.ManifoldGroup.KOMET).getStampFilter());
            if (optionalProviderClassStr.isPresent()) {
                StringVersion providerClassString = optionalProviderClassStr.get();
                try {
                    return Optional.of((NodeFactory<T>) Get.service(Class.forName(providerClassString.getString())));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return Optional.empty();
    }

    public static Manifold manifold(Manifold.ManifoldGroup manifoldGroup) {
        if (MANIFOLDS.isEmpty()) {
            for (Manifold.ManifoldGroup mg : Manifold.ManifoldGroup.values()) {
                MANIFOLDS.put(mg, Manifold.get(mg));
            }

        }
        return MANIFOLDS.get(manifoldGroup);
    }
    public static ObservableList<UuidStringKey> componentListKeys() {
        return componentListKeys;
    }
    public static ComponentList componentList(UuidStringKey componentListKey) {
        return componentListMap.get(componentListKey);
    }

    public static void addComponentList(ComponentList list) {
        componentListMap.put(list.getUuidStringKey(), list);
        componentListKeys.add(list.getUuidStringKey());
        sortComponentList();
    }

    public static void removeComponentList(ComponentList list) {
        componentListMap.remove(list.getUuidStringKey());
        componentListKeys.remove(list.getUuidStringKey());
    }

    private static void sortComponentList() {
        componentListKeys.sort(
                (o1, o2) -> {
                    return NaturalOrder.compareStrings(o1.getString(), o2.getString());
                });
    }

    public static File solorDirectory() {
        File solorDirectory = new File(System.getProperty("user.home"), "Solor");
        solorDirectory.mkdirs();
        return solorDirectory;
    }

    public static File actionFileDirectory() {
        File actionDirectory = new File(FxGet.solorDirectory(), "action files");
        actionDirectory.mkdirs();
        return actionDirectory;
    }

    public static ObservableList<ConceptSnapshot> activeConceptMembers(ConceptSpecification assemblage,
                                                                       ManifoldCoordinate manifoldCoordinate) {
        return activeConceptMembers(assemblage.getNid(), manifoldCoordinate);
    }

    public static ObservableList<ConceptSnapshot> activeConceptMembers(int assemblageNid,
                                                                       ManifoldCoordinate manifoldCoordinate) {
        if (manifoldCoordinate == null) {
            throw new NullPointerException("manifoldCoordinate cannot be null");
        }
        ObservableList<ConceptSnapshot> activeConceptMemberList = FXCollections.observableArrayList();
        SingleAssemblageSnapshot<SemanticVersion> snapshot = Get.assemblageService().getSingleAssemblageSnapshot(assemblageNid, SemanticVersion.class, manifoldCoordinate.getStampFilter());

        snapshot.getLatestSemanticVersionsFromAssemblage().forEach(new Consumer<LatestVersion<SemanticVersion>>() {
            @Override
            public void accept(LatestVersion<SemanticVersion> semanticVersionLatestVersion) {
                if (semanticVersionLatestVersion.isPresent() && semanticVersionLatestVersion.get().isActive()) {
                    activeConceptMemberList.add(Get.conceptSnapshot(semanticVersionLatestVersion.get().getReferencedComponentNid(),
                            manifoldCoordinate));
                }
            }
        });
        activeConceptMemberList.sort((o1, o2) -> NaturalOrder.compareStrings(o1.getPreferredDescriptionText().get(),
                o2.getPreferredDescriptionText().get()));
        return activeConceptMemberList;
    }
}
