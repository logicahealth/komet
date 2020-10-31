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

import javafx.beans.property.*;
import javafx.collections.*;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.robot.Robot;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.controlsfx.control.PropertySheet;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.SingleAssemblageSnapshot;
import sh.isaac.api.StaticIsaacCache;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.commit.CommitListener;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.commit.CommitService;
import sh.isaac.api.component.concept.ConceptSnapshot;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.component.semantic.version.brittle.Nid1_Int2_Version;
import sh.isaac.api.coordinate.*;
import sh.isaac.api.observable.coordinate.*;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.api.preferences.PreferencesService;
import sh.isaac.api.util.NaturalOrder;
import sh.isaac.api.util.UuidStringKey;
import sh.isaac.model.observable.coordinate.ObservableEditCoordinateImpl;
import sh.isaac.model.observable.coordinate.ObservableLanguageCoordinateImpl;
import sh.isaac.model.observable.coordinate.ObservableLogicCoordinateImpl;
import sh.isaac.model.observable.coordinate.ObservableManifoldCoordinateImpl;
import sh.komet.gui.contract.*;
import sh.komet.gui.contract.preferences.KometPreferences;
import sh.komet.gui.contract.preferences.WindowPreferences;
import sh.komet.gui.control.concept.PropertySheetItemConceptConstraintWrapper;
import sh.komet.gui.control.concept.PropertySheetItemConceptWrapper;
import sh.komet.gui.control.property.PropertySheetItem;
import sh.komet.gui.control.property.SessionProperty;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.interfaces.ComponentList;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.lists.ComponentListFromAssemblage;
import sh.komet.gui.lists.ComponentListSelectorForMenuButton;
import sh.komet.gui.provider.StatusMessageProvider;

import jakarta.inject.Singleton;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static sh.komet.gui.contract.preferences.GraphConfigurationItem.PREMISE_DIGRAPH;

/**
 *
 * @author kec
 */
@Service
@Singleton
public class FxGet implements StaticIsaacCache {

    public static final String KOMET_PREFERENCES_ROOT = "sh/komet/preferences";

    public enum PROPERTY_KEYS {
        WINDOW_PREFERENCES
    }

    private enum Keys {
        CONFIGURATION_NAME,
        LANGUAGE_COORDINATE_KEY_LIST,
        LOGIC_COORDINATE_KEY_LIST,
        MANIFOLD_COORDINATE_KEY_LIST,

    }

    private static CommitListener COMMIT_LISTENER;


    private static ObservableMap<UuidStringKey, StampPathImmutable> PATHS;
    private static ObservableMap<UuidStringKey, ObservableLanguageCoordinate> LANGUAGE_COORDINATES;
    private static ObservableMap<UuidStringKey, ObservableLogicCoordinate>    LOGIC_COORDINATES;
    private static ObservableMap<UuidStringKey, ObservableManifoldCoordinate> MANIFOLD_COORDINATES;
    private static ObservableList<UuidStringKey> LANGUAGE_COORDINATE_KEY_LIST;
    private static ObservableList<UuidStringKey> LOGIC_COORDINATE_KEY_LIST;
    private static ObservableList<UuidStringKey> MANIFOLD_COORDINATE_KEY_LIST;
    private static ObservableList<ImmutableList<ConceptSpecification>> NAVIGATION_OPTIONS;

    private static final ConcurrentHashMap<UuidStringKey, ComponentList> componentListMap = new ConcurrentHashMap();

    private static final ObservableList<UuidStringKey> componentListKeys = FXCollections.observableArrayList(new ArrayList<>());
    public static final String VIEWER = "viewer";

    private static DialogService DIALOG_SERVICE = null;
    private static RulesDrivenKometService RULES_DRIVEN_KOMET_SERVICE = null;
    private static StatusMessageProvider STATUS_MESSAGE_PROVIDER = null;
    private static FxConfiguration FX_CONFIGURATION = null;
    // TODO make SEARCHER_LIST behave like a normal lookup service. 
    private static final List<GuiSearcher> SEARCHER_LIST = new ArrayList<>();
    // TODO make SEARCHER_LIST behave like a normal lookup service. 
    private static final List<GuiConceptBuilder> BUILDER_LIST = new ArrayList<>();

    private static final SimpleStringProperty CONFIGURATION_NAME_PROPERTY = new SimpleStringProperty(null, MetaData.CONFIGURATION_NAME____SOLOR.toExternalString(), VIEWER);

    private static ViewProperties preferenceViewProperties;

    private static Robot robot = null;

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

    public static Point2D getMouseLocation() {
        if (robot == null) {
            robot = new Robot();
        }
        return robot.getMousePosition();
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
            PREMISE_DIGRAPH);

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
        CommitListener local = COMMIT_LISTENER;
        if (local != null) {
            //During shutdown, reset it called after a runlevel change.  Do NOT restart the commit service while only trying to clean up.
            //In reality, this static class probably shouldn't be static, if it wasnt to keep state with things that have run levels.... 
            List<CommitService> cs = LookupService.getActiveServices(CommitService.class);
            if (cs.size() > 0) {
                cs.get(0).removeCommitListener(local);
            }
        }
        COMMIT_LISTENER = null;
        DIALOG_SERVICE = null;
        RULES_DRIVEN_KOMET_SERVICE = null;
        STATUS_MESSAGE_PROVIDER = null;
        FX_CONFIGURATION = null;

        PATHS = null;

        LANGUAGE_COORDINATES = null;
        LANGUAGE_COORDINATE_KEY_LIST = null;

        LOGIC_COORDINATES = null;
        LOGIC_COORDINATE_KEY_LIST = null;

        MANIFOLD_COORDINATES = null;
        MANIFOLD_COORDINATE_KEY_LIST = null;

        NAVIGATION_OPTIONS = null;
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

    public static IsaacPreferences kometConfigurationRootNode() {
        return preferenceService().getConfigurationPreferences().node(KOMET_PREFERENCES_ROOT);
    }
    public static IsaacPreferences kometUserRootNode() {
        return preferenceService().getUserPreferences().node(KOMET_PREFERENCES_ROOT);
    }
    public static void load() {

        COMMIT_LISTENER = new CommitListener() {
            UUID uuid = UUID.randomUUID();
            @Override
            public UUID getListenerUuid() {
                return uuid;
            }

            @Override
            public void handleCommit(CommitRecord commitRecord) {
                addPaths();
            }
        };

        Get.commitService().addCommitListener(COMMIT_LISTENER);

        PATHS = FXCollections.observableMap(new TreeMap<>());

        LANGUAGE_COORDINATES = FXCollections.observableMap(new TreeMap<>());
        LANGUAGE_COORDINATE_KEY_LIST = FXCollections.observableArrayList();

        LOGIC_COORDINATES = FXCollections.observableMap(new TreeMap<>());
        LOGIC_COORDINATE_KEY_LIST = FXCollections.observableArrayList();

        MANIFOLD_COORDINATES = FXCollections.observableMap(new TreeMap<>());
        MANIFOLD_COORDINATE_KEY_LIST = FXCollections.observableArrayList();

        NAVIGATION_OPTIONS = FXCollections.observableArrayList();

        LANGUAGE_COORDINATES.addListener(FxGet::languageChangeListener);
        LOGIC_COORDINATES.addListener(FxGet::logicChangeListener);
        MANIFOLD_COORDINATES.addListener(FxGet::manifoldChangeListener);

        IsaacPreferences fxGetPreferences = preferenceService().getConfigurationPreferences().node(FxGet.class);
        CONFIGURATION_NAME_PROPERTY.setValue(fxGetPreferences.get(Keys.CONFIGURATION_NAME, VIEWER));

        addPaths();

        List<UuidStringKey> languageCoordinateKeys = fxGetPreferences.getUuidStringKeyList(Keys.LANGUAGE_COORDINATE_KEY_LIST);
        for (UuidStringKey key: languageCoordinateKeys) {
            LANGUAGE_COORDINATES.put(key, new ObservableLanguageCoordinateImpl(fxGetPreferences.getObject(key.getUuid())));
        }

        List<UuidStringKey> logicCoordinateKeys = fxGetPreferences.getUuidStringKeyList(Keys.LOGIC_COORDINATE_KEY_LIST);
        for (UuidStringKey key: logicCoordinateKeys) {
            LOGIC_COORDINATES.put(key, new ObservableLogicCoordinateImpl(fxGetPreferences.getObject(key.getUuid())));
        }

        List<UuidStringKey> manifoldCoordinateKeys = fxGetPreferences.getUuidStringKeyList(Keys.MANIFOLD_COORDINATE_KEY_LIST);
        for (UuidStringKey key: manifoldCoordinateKeys) {
            MANIFOLD_COORDINATES.put(key, new ObservableManifoldCoordinateImpl(fxGetPreferences.getObject(key.getUuid())));
        }

        NAVIGATION_OPTIONS.addAll(
                Lists.immutable.of(TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE),
                Lists.immutable.of(TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE),
                Lists.immutable.of(TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE, TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE),
                Lists.immutable.of(TermAux.PATH_ORIGIN_ASSEMBLAGE),
                Lists.immutable.of(TermAux.DEPENDENCY_MANAGEMENT));
    }

    public static void sync() {
        IsaacPreferences fxGetPreferences = preferenceService().getConfigurationPreferences().node(FxGet.class);

        fxGetPreferences.put(Keys.CONFIGURATION_NAME, CONFIGURATION_NAME_PROPERTY.getValue());

        for (Map.Entry<UuidStringKey, StampPathImmutable> entry: PATHS.entrySet()) {
            fxGetPreferences.putObject(entry.getKey().getUuid(), entry.getValue());
        }

        fxGetPreferences.putUuidStringKeyList(Keys.LANGUAGE_COORDINATE_KEY_LIST, LANGUAGE_COORDINATE_KEY_LIST);
        for (Map.Entry<UuidStringKey, ObservableLanguageCoordinate> entry: LANGUAGE_COORDINATES.entrySet()) {
            ObservableLanguageCoordinate value = entry.getValue();
            fxGetPreferences.putObject(entry.getKey().getUuid(), value.getValue());
        }

        fxGetPreferences.putUuidStringKeyList(Keys.LOGIC_COORDINATE_KEY_LIST, LOGIC_COORDINATE_KEY_LIST);
        for (Map.Entry<UuidStringKey, ObservableLogicCoordinate> entry: LOGIC_COORDINATES.entrySet()) {
            ObservableLogicCoordinate value = entry.getValue();
            fxGetPreferences.putObject(entry.getKey().getUuid(), value.getValue());
        }

        fxGetPreferences.putUuidStringKeyList(Keys.MANIFOLD_COORDINATE_KEY_LIST, MANIFOLD_COORDINATE_KEY_LIST);
        for (Map.Entry<UuidStringKey, ObservableManifoldCoordinate> entry: MANIFOLD_COORDINATES.entrySet()) {
            ObservableManifoldCoordinate value = entry.getValue();
            fxGetPreferences.putObject(entry.getKey().getUuid(), value.getValue());
        }
    }

    public static List<PropertySheet.Item> constraintPropertyItemsForAssemblageSemantic(ConceptSpecification assemblageConcept, ManifoldCoordinate manifoldCoordinate) {
        return propertyItemsForAssemblageSemantic(assemblageConcept, manifoldCoordinate, true);
    }

    public static List<PropertySheet.Item> propertyItemsForAssemblageSemantic(ConceptSpecification assemblageConcept, ManifoldCoordinate manifoldCoordinate) {
        return propertyItemsForAssemblageSemantic(assemblageConcept, manifoldCoordinate, false);
    }

    private static List<PropertySheet.Item> propertyItemsForAssemblageSemantic(ConceptSpecification assemblageConcept, ManifoldCoordinate manifoldCoordinate, boolean forConstraints) {
        TreeMap<Integer, ConceptSpecification> fieldIndexToFieldConcept = new TreeMap<>();
        TreeMap<Integer, ConceptSpecification> fieldIndexToFieldDataType = new TreeMap<>();
        List<PropertySheet.Item> items = new ArrayList();
        OptionalInt optionalSemanticConceptNid = Get.assemblageService().getSemanticTypeConceptForAssemblage(assemblageConcept, manifoldCoordinate.getViewStampFilter());

        if (optionalSemanticConceptNid.isPresent()) {
            int semanticConceptNid = optionalSemanticConceptNid.getAsInt();
            ImmutableIntSet semanticTypeOfFields = Get.assemblageService().getSemanticNidsForComponentFromAssemblage(semanticConceptNid, TermAux.SEMANTIC_FIELD_DATA_TYPES_ASSEMBLAGE.getNid());
            for (int nid : semanticTypeOfFields.toArray()) { // one member, "Concept field": 1
                SemanticChronology semanticTypeField = Get.assemblageService().getSemanticChronology(nid);
                LatestVersion<Version> latestSemanticTypeField = semanticTypeField.getLatestVersion(manifoldCoordinate.getViewStampFilter());
                Nid1_Int2_Version latestSemanticTypeFieldVersion = (Nid1_Int2_Version) latestSemanticTypeField.get();
                fieldIndexToFieldDataType.put(latestSemanticTypeFieldVersion.getInt2(), Get.concept(latestSemanticTypeFieldVersion.getNid1()));
            }

            ImmutableIntSet assemblageSemanticFields = Get.assemblageService().getSemanticNidsForComponentFromAssemblage(assemblageConcept.getNid(), MetaData.SEMANTIC_FIELDS_ASSEMBLAGE____SOLOR.getNid());
            for (int nid : assemblageSemanticFields.toArray()) {
                SemanticChronology semanticField = Get.assemblageService().getSemanticChronology(nid);
                LatestVersion<Version> latestSemanticField = semanticField.getLatestVersion(manifoldCoordinate.getViewStampFilter());
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
                            new PropertySheetItemConceptWrapper(manifoldCoordinate, property, TermAux.UNINITIALIZED_COMPONENT_ID.getNid()),
                            manifoldCoordinate, manifoldCoordinate.getPreferredDescriptionText(fieldConcept)));
                } else {
                    items.add(new PropertySheetItemConceptWrapper(manifoldCoordinate, property, TermAux.UNINITIALIZED_COMPONENT_ID.getNid()));
                }

            } else if (fieldDataType.getNid() == MetaData.CONCEPT_FIELD____SOLOR.getNid()) {
                SimpleObjectProperty property = new SimpleObjectProperty(null, fieldConcept.toExternalString());
                if (forConstraints) {
                    items.add(new PropertySheetItemConceptConstraintWrapper(
                            new PropertySheetItemConceptWrapper(manifoldCoordinate, property, TermAux.UNINITIALIZED_COMPONENT_ID.getNid()), manifoldCoordinate, manifoldCoordinate.getPreferredDescriptionText(fieldConcept)));
                } else {
                    items.add(new PropertySheetItemConceptWrapper(manifoldCoordinate, property, TermAux.UNINITIALIZED_COMPONENT_ID.getNid()));
                }
            } else if (fieldDataType.getNid() == MetaData.BOOLEAN_FIELD____SOLOR.getNid()) {
                SimpleBooleanProperty property = new SimpleBooleanProperty(null, fieldConcept.toExternalString());
                items.add(new PropertySheetItem(property, manifoldCoordinate));
            } else if (fieldDataType.getNid() == MetaData.ARRAY_FIELD____SOLOR.getNid()) {
                SimpleObjectProperty property = new SimpleObjectProperty(null, fieldConcept.toExternalString());
                items.add(new PropertySheetItem(property, manifoldCoordinate));
            } else if (fieldDataType.getNid() == MetaData.BYTE_ARRAY_FIELD____SOLOR.getNid()) {
                SimpleObjectProperty property = new SimpleObjectProperty(null, fieldConcept.toExternalString());
                items.add(new PropertySheetItem(property, manifoldCoordinate));
            } else if (fieldDataType.getNid() == MetaData.DOUBLE_FIELD____SOLOR.getNid()) {
                SimpleDoubleProperty property = new SimpleDoubleProperty(null, fieldConcept.toExternalString());
            } else if (fieldDataType.getNid() == MetaData.FLOAT_FIELD____SOLOR.getNid()) {
                SimpleFloatProperty property = new SimpleFloatProperty(null, fieldConcept.toExternalString());
                items.add(new PropertySheetItem(property, manifoldCoordinate));
            } else if (fieldDataType.getNid() == MetaData.INTEGER_FIELD____SOLOR.getNid()) {
                SimpleIntegerProperty property = new SimpleIntegerProperty(null, fieldConcept.toExternalString());
                items.add(new PropertySheetItem(property, manifoldCoordinate));
            } else if (fieldDataType.getNid() == MetaData.LOGICAL_EXPRESSION_FIELD____SOLOR.getNid()) {
                SimpleObjectProperty property = new SimpleObjectProperty(null, fieldConcept.toExternalString());
                items.add(new PropertySheetItem(property, manifoldCoordinate));
            } else if (fieldDataType.getNid() == MetaData.LONG_FIELD____SOLOR.getNid()) {
                SimpleLongProperty property = new SimpleLongProperty(null, fieldConcept.toExternalString());
                items.add(new PropertySheetItem(property, manifoldCoordinate));
            } else if (fieldDataType.getNid() == MetaData.STRING_FIELD____SOLOR.getNid()) {
                SimpleStringProperty property = new SimpleStringProperty(null, fieldConcept.toExternalString());
                items.add(new PropertySheetItem(property, manifoldCoordinate));
            } else if (fieldDataType.getNid() == MetaData.POLYMORPHIC_FIELD____SOLOR.getNid()) {
                SimpleObjectProperty property = new SimpleObjectProperty(null, fieldConcept.toExternalString());
                items.add(new PropertySheetItem(property, manifoldCoordinate));
            } else if (fieldDataType.getNid() == MetaData.UUID_FIELD____SOLOR.getNid()) {
                SimpleObjectProperty property = new SimpleObjectProperty(null, fieldConcept.toExternalString());
                items.add(new PropertySheetItem(property, manifoldCoordinate));
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

    public static ObservableList<ImmutableList<ConceptSpecification>> navigationOptions() {
        return NAVIGATION_OPTIONS;
    }
    public static Collection<? extends ConceptSpecification> allowedLanguages() {
        return Lists.immutable.of(TermAux.ENGLISH_LANGUAGE, TermAux.SPANISH_LANGUAGE).castToList();
    }

    public static ImmutableList<ImmutableList<? extends ConceptSpecification>> allowedDescriptionTypeOrder() {

        return Lists.immutable.of(
                Lists.immutable.of(TermAux.REGULAR_NAME_DESCRIPTION_TYPE, TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE),
                Lists.immutable.of(TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE, TermAux.REGULAR_NAME_DESCRIPTION_TYPE));
    }

    public static ImmutableList<ImmutableList<? extends ConceptSpecification>> allowedDialectTypeOrder() {
        return Lists.immutable.of(
                Lists.immutable.of(TermAux.US_DIALECT_ASSEMBLAGE, TermAux.GB_DIALECT_ASSEMBLAGE),
                Lists.immutable.of(TermAux.GB_DIALECT_ASSEMBLAGE, TermAux.US_DIALECT_ASSEMBLAGE));
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
    public static ObservableMap<UuidStringKey, StampPathImmutable> pathCoordinates() {
        if (PATHS.isEmpty()) {
            //TODO add commit listener, and update when new semantic or a commit.
            addPaths();
        }
        return PATHS;
    }

    private static void addPaths() {
        Get.identifierService().getNidsForAssemblage(TermAux.PATH_ASSEMBLAGE, false).forEach(semanticNid -> {
            SemanticChronology pathConceptSemantic = Get.assemblageService().getSemanticChronology(semanticNid);
            StampPathImmutable path = StampPathImmutable.make(pathConceptSemantic.getReferencedComponentNid());
            String pathDescription = Get.defaultCoordinate().getPreferredDescriptionText(path.getPathConceptNid());
            UuidStringKey pathKey = new UuidStringKey(path.getPathCoordinateUuid(), pathDescription);
            PATHS.put(pathKey, path);
        });
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

    public static ObservableList<UuidStringKey> languageCoordinateKeys() {
        return LANGUAGE_COORDINATE_KEY_LIST;
    }
    public static ObservableList<UuidStringKey> logicCoordinateKeys() {
        return LOGIC_COORDINATE_KEY_LIST;
    }
    public static ObservableList<UuidStringKey> manifoldCoordinateKeys() {
        return MANIFOLD_COORDINATE_KEY_LIST;
    }



    public static <T extends ExplorationNode> Optional<NodeFactory<T>> nodeFactory(ConceptSpecification nodeSpecConcept, ManifoldCoordinate manifoldCoordinate) {
        ImmutableIntSet semanticNids = Get.assemblageService().getSemanticNidsForComponentFromAssemblage(nodeSpecConcept.getNid(), TermAux.PROVIDER_CLASS_ASSEMBLAGE.getNid());
        for (int nid: semanticNids.toArray()) {
            SemanticChronology chronology = Get.assemblageService().getSemanticChronology(nid);
            LatestVersion<StringVersion> optionalProviderClassStr = chronology.getLatestVersion(manifoldCoordinate.getViewStampFilter());
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


    public static ObservableList<UuidStringKey> componentListKeys() {
        return componentListKeys;
    }
    public static ComponentList componentList(UuidStringKey componentListKey, ManifoldCoordinate manifoldCoordinate) {

        if (componentListKey.equals(ComponentListSelectorForMenuButton.EMPTY_LIST_KEY)) {
            return ComponentListSelectorForMenuButton.EMPTY_LIST;
        }
        if (Get.identifierService().hasUuid(componentListKey.getUuid())) {
            return new ComponentListFromAssemblage(manifoldCoordinate, Get.concept(componentListKey.getUuid()));
        }
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
        SingleAssemblageSnapshot<SemanticVersion> snapshot =
                Get.assemblageService().getSingleAssemblageSnapshot(assemblageNid, SemanticVersion.class, manifoldCoordinate.getViewStampFilter());

        snapshot.getLatestSemanticVersionsFromAssemblage().forEach(new Consumer<LatestVersion<SemanticVersion>>() {
            @Override
            public void accept(LatestVersion<SemanticVersion> semanticVersionLatestVersion) {
                if (semanticVersionLatestVersion.isPresent() && semanticVersionLatestVersion.get().isActive()) {
                    activeConceptMemberList.add(Get.conceptSnapshot(semanticVersionLatestVersion.get().getReferencedComponentNid(),
                            manifoldCoordinate));
                }
            }
        });
        activeConceptMemberList.sort((o1, o2) -> NaturalOrder.compareStrings(o1.getRegularDescriptionText().get(),
                o2.getRegularDescriptionText().get()));
        return activeConceptMemberList;
    }


    public static ViewProperties preferenceViewProperties() {
        IsaacPreferences preferences = FxGet.kometConfigurationRootNode();
        if (preferenceViewProperties == null) {
            preferenceViewProperties = ViewProperties.make(UUID.fromString("1db21f81-c884-4dd7-8bf5-2befc955c887"), ViewProperties.PREFERENCES,
                    new ObservableManifoldCoordinateImpl(Coordinates.Manifold.DevelopmentInferredRegularNameSort()),
                    new ObservableEditCoordinateImpl(Coordinates.Edit.Default()),
                    preferences);
        }
        return preferenceViewProperties;
    }

    public static ViewProperties newDefaultViewProperties(IsaacPreferences preferences) {
        return ViewProperties.make(UUID.randomUUID(), "Default view",
                new ObservableManifoldCoordinateImpl(Coordinates.Manifold.DevelopmentInferredRegularNameSort()),
                new ObservableEditCoordinateImpl(Coordinates.Edit.Default()), preferences);
    }

    public static WindowPreferences windowPreferences(Node node) {
        return (WindowPreferences) node.getScene().getWindow().getProperties().get(PROPERTY_KEYS.WINDOW_PREFERENCES);
    }
}
