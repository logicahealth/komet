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
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.controlsfx.control.PropertySheet;
import org.eclipse.collections.api.block.function.primitive.IntToObjectFunction;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.primitive.ImmutableLongList;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.SingleAssemblageSnapshot;
import sh.isaac.api.StaticIsaacCache;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
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
import sh.isaac.api.util.time.DateTimeUtil;
import sh.isaac.model.collections.IntObjectMap;
import sh.isaac.model.observable.coordinate.ObservableLanguageCoordinateImpl;
import sh.isaac.model.observable.coordinate.ObservableLogicCoordinateImpl;
import sh.isaac.model.observable.coordinate.ObservableManifoldCoordinateImpl;
import sh.komet.gui.contract.*;
import sh.komet.gui.contract.preferences.KometPreferences;
import sh.komet.gui.control.concept.PropertySheetItemConceptConstraintWrapper;
import sh.komet.gui.control.concept.PropertySheetItemConceptWrapper;
import sh.komet.gui.control.property.PropertySheetItem;
import sh.komet.gui.control.property.SessionProperty;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.interfaces.ComponentList;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.provider.StatusMessageProvider;

import javax.inject.Singleton;
import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalField;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

import static sh.komet.gui.contract.preferences.GraphConfigurationItem.PREMISE_DIGRAPH;

/**
 *
 * @author kec
 */
@Service
@Singleton
public class FxGet implements StaticIsaacCache {


    private static ObservableMap<UuidStringKey, StampPathImmutable> PATHS;
    private static ObservableMap<UuidStringKey, ObservableLanguageCoordinate> LANGUAGE_COORDINATES;
    private static ObservableMap<UuidStringKey, ObservableLogicCoordinate>    LOGIC_COORDINATES;
    private static ObservableMap<UuidStringKey, ObservableManifoldCoordinate> MANIFOLD_COORDINATES;
    private static ObservableList<UuidStringKey> PATH_COORDINATE_KEY_LIST;
    private static ObservableList<UuidStringKey> LANGUAGE_COORDINATE_KEY_LIST;
    private static ObservableList<UuidStringKey> LOGIC_COORDINATE_KEY_LIST;
    private static ObservableList<UuidStringKey> MANIFOLD_COORDINATE_KEY_LIST;
    private static ObservableList<ConceptSpecification> NAVIGATION_OPTIONS;

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
        DIALOG_SERVICE = null;
        RULES_DRIVEN_KOMET_SERVICE = null;
        STATUS_MESSAGE_PROVIDER = null;
        FX_CONFIGURATION = null;

        PATHS = null;
        PATH_COORDINATE_KEY_LIST = null;

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

    private enum Keys {
        CONFIGURATION_NAME,
        PATH_COORDINATE_KEY_LIST,
        LANGUAGE_COORDINATE_KEY_LIST,
        LOGIC_COORDINATE_KEY_LIST,
        MANIFOLD_COORDINATE_KEY_LIST,

    }
    public static void load() {

        PATHS = FXCollections.observableMap(new TreeMap<>());
        PATH_COORDINATE_KEY_LIST = FXCollections.observableArrayList();

        LANGUAGE_COORDINATES = FXCollections.observableMap(new TreeMap<>());
        LANGUAGE_COORDINATE_KEY_LIST = FXCollections.observableArrayList();

        LOGIC_COORDINATES = FXCollections.observableMap(new TreeMap<>());
        LOGIC_COORDINATE_KEY_LIST = FXCollections.observableArrayList();

        MANIFOLD_COORDINATES = FXCollections.observableMap(new TreeMap<>());
        MANIFOLD_COORDINATE_KEY_LIST = FXCollections.observableArrayList();

        NAVIGATION_OPTIONS = FXCollections.observableArrayList();

        PATHS.addListener(FxGet::pathChangeListener);
        LANGUAGE_COORDINATES.addListener(FxGet::languageChangeListener);
        LOGIC_COORDINATES.addListener(FxGet::logicChangeListener);
        MANIFOLD_COORDINATES.addListener(FxGet::manifoldChangeListener);

        IsaacPreferences fxGetPreferences = preferenceService().getConfigurationPreferences().node(FxGet.class);
        CONFIGURATION_NAME_PROPERTY.setValue(fxGetPreferences.get(Keys.CONFIGURATION_NAME, VIEWER));
        List<UuidStringKey> pathCoordinateKeys = fxGetPreferences.getUuidStringKeyList(Keys.PATH_COORDINATE_KEY_LIST);
        for (UuidStringKey key: pathCoordinateKeys) {
            PATHS.put(key, fxGetPreferences.getObject(key.getUuid()));
        }

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

        NAVIGATION_OPTIONS.addAll(TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE,
                TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE,
                TermAux.PATH_ORIGIN_ASSEMBLAGE,
                TermAux.DEPENDENCY_MANAGEMENT);
    }

    public static void sync() {
        IsaacPreferences fxGetPreferences = preferenceService().getConfigurationPreferences().node(FxGet.class);

        fxGetPreferences.put(Keys.CONFIGURATION_NAME, CONFIGURATION_NAME_PROPERTY.getValue());
        fxGetPreferences.putUuidStringKeyList(Keys.PATH_COORDINATE_KEY_LIST, PATH_COORDINATE_KEY_LIST);
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

    public static List<PropertySheet.Item> constraintPropertyItemsForAssemblageSemantic(ConceptSpecification assemblageConcept, ViewProperties viewProperties) {
        return propertyItemsForAssemblageSemantic(assemblageConcept, viewProperties, true);
    }

    public static List<PropertySheet.Item> propertyItemsForAssemblageSemantic(ConceptSpecification assemblageConcept, ViewProperties viewProperties) {
        return propertyItemsForAssemblageSemantic(assemblageConcept, viewProperties, false);
    }

    private static List<PropertySheet.Item> propertyItemsForAssemblageSemantic(ConceptSpecification assemblageConcept, ViewProperties viewProperties, boolean forConstraints) {
        TreeMap<Integer, ConceptSpecification> fieldIndexToFieldConcept = new TreeMap<>();
        TreeMap<Integer, ConceptSpecification> fieldIndexToFieldDataType = new TreeMap<>();
        List<PropertySheet.Item> items = new ArrayList();
        OptionalInt optionalSemanticConceptNid = Get.assemblageService().getSemanticTypeConceptForAssemblage(assemblageConcept, viewProperties.getManifoldCoordinate().getVertexStampFilter());

        if (optionalSemanticConceptNid.isPresent()) {
            int semanticConceptNid = optionalSemanticConceptNid.getAsInt();
            ImmutableIntSet semanticTypeOfFields = Get.assemblageService().getSemanticNidsForComponentFromAssemblage(semanticConceptNid, TermAux.SEMANTIC_FIELD_DATA_TYPES_ASSEMBLAGE.getNid());
            for (int nid : semanticTypeOfFields.toArray()) { // one member, "Concept field": 1
                SemanticChronology semanticTypeField = Get.assemblageService().getSemanticChronology(nid);
                LatestVersion<Version> latestSemanticTypeField = semanticTypeField.getLatestVersion(viewProperties.getManifoldCoordinate().getVertexStampFilter());
                Nid1_Int2_Version latestSemanticTypeFieldVersion = (Nid1_Int2_Version) latestSemanticTypeField.get();
                fieldIndexToFieldDataType.put(latestSemanticTypeFieldVersion.getInt2(), Get.concept(latestSemanticTypeFieldVersion.getNid1()));
            }

            ImmutableIntSet assemblageSemanticFields = Get.assemblageService().getSemanticNidsForComponentFromAssemblage(assemblageConcept.getNid(), MetaData.SEMANTIC_FIELDS_ASSEMBLAGE____SOLOR.getNid());
            for (int nid : assemblageSemanticFields.toArray()) {
                SemanticChronology semanticField = Get.assemblageService().getSemanticChronology(nid);
                LatestVersion<Version> latestSemanticField = semanticField.getLatestVersion(viewProperties.getManifoldCoordinate().getVertexStampFilter());
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
                            new PropertySheetItemConceptWrapper(viewProperties, property, TermAux.UNINITIALIZED_COMPONENT_ID.getNid()), viewProperties, viewProperties.getPreferredDescriptionText(fieldConcept)));
                } else {
                    items.add(new PropertySheetItemConceptWrapper(viewProperties, property, TermAux.UNINITIALIZED_COMPONENT_ID.getNid()));
                }

            } else if (fieldDataType.getNid() == MetaData.CONCEPT_FIELD____SOLOR.getNid()) {
                SimpleObjectProperty property = new SimpleObjectProperty(null, fieldConcept.toExternalString());
                if (forConstraints) {
                    items.add(new PropertySheetItemConceptConstraintWrapper(
                            new PropertySheetItemConceptWrapper(viewProperties, property, TermAux.UNINITIALIZED_COMPONENT_ID.getNid()), viewProperties, viewProperties.getPreferredDescriptionText(fieldConcept)));
                } else {
                    items.add(new PropertySheetItemConceptWrapper(viewProperties, property, TermAux.UNINITIALIZED_COMPONENT_ID.getNid()));
                }
            } else if (fieldDataType.getNid() == MetaData.BOOLEAN_FIELD____SOLOR.getNid()) {
                SimpleBooleanProperty property = new SimpleBooleanProperty(null, fieldConcept.toExternalString());
                items.add(new PropertySheetItem(property, viewProperties));
            } else if (fieldDataType.getNid() == MetaData.ARRAY_FIELD____SOLOR.getNid()) {
                SimpleObjectProperty property = new SimpleObjectProperty(null, fieldConcept.toExternalString());
                items.add(new PropertySheetItem(property, viewProperties));
            } else if (fieldDataType.getNid() == MetaData.BYTE_ARRAY_FIELD____SOLOR.getNid()) {
                SimpleObjectProperty property = new SimpleObjectProperty(null, fieldConcept.toExternalString());
                items.add(new PropertySheetItem(property, viewProperties));
            } else if (fieldDataType.getNid() == MetaData.DOUBLE_FIELD____SOLOR.getNid()) {
                SimpleDoubleProperty property = new SimpleDoubleProperty(null, fieldConcept.toExternalString());
            } else if (fieldDataType.getNid() == MetaData.FLOAT_FIELD____SOLOR.getNid()) {
                SimpleFloatProperty property = new SimpleFloatProperty(null, fieldConcept.toExternalString());
                items.add(new PropertySheetItem(property, viewProperties));
            } else if (fieldDataType.getNid() == MetaData.INTEGER_FIELD____SOLOR.getNid()) {
                SimpleIntegerProperty property = new SimpleIntegerProperty(null, fieldConcept.toExternalString());
                items.add(new PropertySheetItem(property, viewProperties));
            } else if (fieldDataType.getNid() == MetaData.LOGICAL_EXPRESSION_FIELD____SOLOR.getNid()) {
                SimpleObjectProperty property = new SimpleObjectProperty(null, fieldConcept.toExternalString());
                items.add(new PropertySheetItem(property, viewProperties));
            } else if (fieldDataType.getNid() == MetaData.LONG_FIELD____SOLOR.getNid()) {
                SimpleLongProperty property = new SimpleLongProperty(null, fieldConcept.toExternalString());
                items.add(new PropertySheetItem(property, viewProperties));
            } else if (fieldDataType.getNid() == MetaData.STRING_FIELD____SOLOR.getNid()) {
                SimpleStringProperty property = new SimpleStringProperty(null, fieldConcept.toExternalString());
                items.add(new PropertySheetItem(property, viewProperties));
            } else if (fieldDataType.getNid() == MetaData.POLYMORPHIC_FIELD____SOLOR.getNid()) {
                SimpleObjectProperty property = new SimpleObjectProperty(null, fieldConcept.toExternalString());
                items.add(new PropertySheetItem(property, viewProperties));
            } else if (fieldDataType.getNid() == MetaData.UUID_FIELD____SOLOR.getNid()) {
                SimpleObjectProperty property = new SimpleObjectProperty(null, fieldConcept.toExternalString());
                items.add(new PropertySheetItem(property, viewProperties));
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

    public static ObservableList<ConceptSpecification> navigationOptions() {
        return NAVIGATION_OPTIONS;
    }

    public static ObservableEditCoordinate editCoordinate() {
        return EditCoordinate.get();
    }

    private static void pathChangeListener(MapChangeListener.Change<? extends UuidStringKey, ? extends StampPathImmutable> change) {
        if (change.wasAdded()) {
            PATH_COORDINATE_KEY_LIST.add(change.getKey());
        }
        if (change.wasRemoved()) {
            PATH_COORDINATE_KEY_LIST.remove(change.getKey());
        }
    }

    private static void makeRecursiveOverrideMenu(ManifoldCoordinate manifoldCoordinate, ObservableList<MenuItem> menuItems,
                                           ObservableCoordinate observableCoordinate) {

        if (observableCoordinate.hasOverrides()) {
            Menu overridesMenu = new Menu(manifoldCoordinate.toPreferredConceptString(observableCoordinate.getName()) + " has overrides");
            menuItems.add(overridesMenu);
            menuItems.add(new SeparatorMenuItem());
            for (Property property: observableCoordinate.getBaseProperties()) {
                if (property instanceof PropertyWithOverride) {
                    PropertyWithOverride propertyWithOverride = (PropertyWithOverride) property;
                    if (propertyWithOverride.isOverridden()) {
                        overridesMenu.getItems().add(new MenuItem(getNameAndValueString(manifoldCoordinate, propertyWithOverride)));
                    }
                }
            }
            for (ObservableCoordinate compositeCoordinate: observableCoordinate.getCompositeCoordinates()) {
                if (compositeCoordinate.hasOverrides()) {
                    makeRecursiveOverrideMenu(manifoldCoordinate, overridesMenu.getItems(),
                            compositeCoordinate);
                }
            }
        }
    }

    /**
     * The
     * @param manifoldCoordinate Used to get preferred concept names
     * @param menuItems Menu item list add the menu item to
     * @param observableCoordinate The coordinate to make an display menu for.
     */
    public static void makeCoordinateDisplayMenu(ManifoldCoordinate manifoldCoordinate, ObservableList<MenuItem> menuItems,
                                                 ObservableCoordinate observableCoordinate) {

        makeRecursiveOverrideMenu(manifoldCoordinate, menuItems,
                observableCoordinate);

        for (Property<?> baseProperty: observableCoordinate.getBaseProperties()) {
            menuItems.add(new MenuItem(getNameAndValueString(manifoldCoordinate, baseProperty)));
        }
        for (ObservableCoordinate<?> compositeCoordinate: observableCoordinate.getCompositeCoordinates()) {
            String propertyName = getPropertyNameWithOverride(manifoldCoordinate, compositeCoordinate);
            Menu compositeMenu = new Menu(propertyName);
            menuItems.add(compositeMenu);
            makeCoordinateDisplayMenu(manifoldCoordinate, compositeMenu.getItems(), compositeCoordinate);
        }


        if (observableCoordinate instanceof ManifoldCoordinate) {
            menuItems.add(new SeparatorMenuItem());
            addRemoveOverrides(menuItems, observableCoordinate);
            addChangeItemsForManifold(manifoldCoordinate, menuItems, (ObservableManifoldCoordinate) observableCoordinate);
        } else if (observableCoordinate instanceof LanguageCoordinate) {
            menuItems.add(new SeparatorMenuItem());
            addChangeItemsForLanguage(manifoldCoordinate, menuItems, (ObservableLanguageCoordinate) observableCoordinate);
        } else if (observableCoordinate instanceof LogicCoordinate) {
            //menuItems.add(new SeparatorMenuItem());
            addChangeItemsForLogic(manifoldCoordinate, menuItems, (ObservableLogicCoordinate) observableCoordinate);
        } else if (observableCoordinate instanceof NavigationCoordinate) {
            menuItems.add(new SeparatorMenuItem());
            addChangeItemsForNavigation(manifoldCoordinate, menuItems, (ObservableNavigationCoordinate) observableCoordinate);
        } else if (observableCoordinate instanceof EditCoordinate) {
            menuItems.add(new SeparatorMenuItem());
            addChangeItemsForEdit(manifoldCoordinate, menuItems, (ObservableEditCoordinate) observableCoordinate);
        } else if (observableCoordinate instanceof StampFilter) {
            menuItems.add(new SeparatorMenuItem());
            addChangeItemsForFilter(manifoldCoordinate, menuItems, (ObservableStampFilter) observableCoordinate);
        }
    }

    private static void addRemoveOverrides(ObservableList<MenuItem> menuItems, ObservableCoordinate observableCoordinate) {
        if (observableCoordinate.hasOverrides()) {
            MenuItem removeOverrides = new MenuItem("Remove overrides");
            menuItems.add(removeOverrides);
            removeOverrides.setOnAction(event -> {
                Platform.runLater(() -> {
                    observableCoordinate.removeOverrides();
                 });
                event.consume();
            });
        }
    }

    private static String getNameAndValueString(ManifoldCoordinate manifoldCoordinate, Property<?> baseProperty) {
        String propertyName = getPropertyNameWithOverride(manifoldCoordinate, baseProperty);
        StringBuilder sb = new StringBuilder(propertyName + ": ");
        Object value = baseProperty.getValue();
        if (value instanceof Collection) {
            Collection collection = (Collection) value;
            if (collection.isEmpty()) {
                if (propertyName.toLowerCase().startsWith("module set")) {
                    StringBuilder collectionBuilder = new StringBuilder("\u2004\u2004\u2004\u2004\u2004");
                    manifoldCoordinate.toConceptString(Get.stampService().getModuleConceptsInUse(),
                            manifoldCoordinate::getPreferredDescriptionText,
                            collectionBuilder);
                    sb.append(" (*)\n").append(collectionBuilder);
                } else {
                    manifoldCoordinate.toConceptString(value, manifoldCoordinate::getPreferredDescriptionText, sb);
                }

            } else {
                Object obj = collection.iterator().next();
                if (obj instanceof ConceptSpecification) {
                    StringBuilder collectionBuilder = new StringBuilder("\u2004\u2004\u2004\u2004\u2004");
                    manifoldCoordinate.toConceptString(value, manifoldCoordinate::getPreferredDescriptionText, collectionBuilder);
                    sb.append("\n").append(collectionBuilder);
                } else {
                    if (collection instanceof Set) {
                        Object[] objects = collection.toArray();
                        Arrays.sort(objects, (o1, o2) ->
                                NaturalOrder.compareStrings(o1.toString(), o2.toString()));
                        sb.append(Arrays.toString(objects));
                    } else {
                        sb.append(collection.toString());
                    }

                }
            }
        } else {
            manifoldCoordinate.toConceptString(value, manifoldCoordinate::getPreferredDescriptionText, sb);
        }
        return sb.toString();
    }

    private static String getPropertyNameWithOverride(ManifoldCoordinate manifoldCoordinate, Property<?> baseProperty) {
        String propertyName;
        if (baseProperty instanceof PropertyWithOverride) {
            PropertyWithOverride propertyWithOverride = (PropertyWithOverride) baseProperty;
            propertyName = propertyWithOverride.getOverrideName(manifoldCoordinate);
        } else {
            propertyName = manifoldCoordinate.toPreferredConceptString(baseProperty.getName());
        }
        return propertyName;
    }

    private static void addChangeItemsForFilter(ManifoldCoordinate manifoldCoordinate, ObservableList<MenuItem> menuItems, ObservableStampFilter observableCoordinate) {


        Menu changePathMenu = new Menu("Change path");
        menuItems.add(changePathMenu);
        for (UuidStringKey key: FxGet.pathCoordinates().keySet()) {
            CheckMenuItem item = new CheckMenuItem(key.getString());
            StampPathImmutable pathForMenu = FxGet.pathCoordinates().get(key);
            item.setSelected(pathForMenu.getPathConceptNid() == observableCoordinate.getPathNidForFilter());
            item.setUserData(FxGet.pathCoordinates().get(key));
            item.setOnAction(event -> {
                StampPathImmutable path = (StampPathImmutable) item.getUserData();
                Platform.runLater(() -> observableCoordinate.pathConceptProperty().setValue(Get.concept(path.getPathConceptNid())));
                event.consume();
            });
            changePathMenu.getItems().add(item);
        }

        addChangePositionForFilter(menuItems, observableCoordinate);


        Menu changeAllowedStatusMenu = new Menu("Change allowed states");
        menuItems.add(changeAllowedStatusMenu);

        for (StatusSet statusSet: new StatusSet[] { StatusSet.ACTIVE_ONLY, StatusSet.ACTIVE_AND_INACTIVE}) {
            CheckMenuItem item = new CheckMenuItem(statusSet.toUserString());
            item.setSelected(statusSet.equals(observableCoordinate.getAllowedStates()));
            item.setOnAction(event -> {
                Platform.runLater(() -> {
                    ObservableSet<Status> set = FXCollections.observableSet(statusSet.toArray());
                    observableCoordinate.allowedStatusProperty().setValue(set);
                });
                event.consume();
            });
            changeAllowedStatusMenu.getItems().add(item);
        }

        addIncludedModulesMenu(menuItems, observableCoordinate, manifoldCoordinate);

        addExcludedModulesMenu(menuItems, observableCoordinate, manifoldCoordinate);

    }

    private static void addChangePositionForManifold(ObservableList<MenuItem> menuItems, ObservableManifoldCoordinate observableCoordinate) {
        addChangePositionMenu(menuItems, time -> {
            Platform.runLater(() -> {
                observableCoordinate.getEdgeStampFilter().timeProperty().setValue(time);
                observableCoordinate.getLanguageStampFilter().timeProperty().setValue(time);
                observableCoordinate.getVertexStampFilter().timeProperty().setValue(time);
            });
        });
    }

    private static void addChangePositionForFilter(ObservableList<MenuItem> menuItems, ObservableStampFilter observableCoordinate) {
        addChangePositionMenu(menuItems, time -> {
            Platform.runLater(() -> observableCoordinate.timeProperty().setValue(time));
        });
    }

    private static void addChangePositionMenu(ObservableList<MenuItem> menuItems, LongConsumer setPosition) {
        Menu changePositionMenu = new Menu("Change position");

        menuItems.add(changePositionMenu);
        MenuItem latestItem = new MenuItem("latest");
        changePositionMenu.getItems().add(latestItem);
        latestItem.setOnAction(event -> {
            Platform.runLater(() -> {
                setPosition.accept(Long.MAX_VALUE);
            });
            event.consume();
        });

        ImmutableLongList times = Get.stampService().getTimesInUse().toReversed();

        MutableIntObjectMap<Menu> yearMenuMap = IntObjectMaps.mutable.empty();
        for (long time: times.toArray()) {
            LocalDateTime localTime = DateTimeUtil.epochToZonedDateTime(time).toLocalDateTime();
            Menu aYearMenu = yearMenuMap.getIfAbsentPutWithKey(localTime.getYear(), (int year) -> {
                Menu yearMenu = new Menu(Integer.toString(year));
                changePositionMenu.getItems().add(yearMenu);
                yearMenu.getItems().add(new Menu("Jan"));
                yearMenu.getItems().add(new Menu("Feb"));
                yearMenu.getItems().add(new Menu("Mar"));
                yearMenu.getItems().add(new Menu("Apr"));
                yearMenu.getItems().add(new Menu("May"));
                yearMenu.getItems().add(new Menu("Jun"));
                yearMenu.getItems().add(new Menu("Jul"));
                yearMenu.getItems().add(new Menu("Aug"));
                yearMenu.getItems().add(new Menu("Sep"));
                yearMenu.getItems().add(new Menu("Oct"));
                yearMenu.getItems().add(new Menu("Nov"));
                yearMenu.getItems().add(new Menu("Dec"));
                return yearMenu;
            });
            Menu monthMenu = (Menu) aYearMenu.getItems().get(localTime.getMonthValue() - 1);
            MenuItem positionMenu = new MenuItem(
                    localTime.getDayOfMonth() + DateTimeUtil.getDayOfMonthSuffix(localTime.getDayOfMonth()) +
                    " " + DateTimeUtil.EASY_TO_READ_TIME_FORMAT.format(DateTimeUtil.epochToZonedDateTime(time)));
            monthMenu.getItems().add(positionMenu);
            positionMenu.setOnAction(event -> {
                Platform.runLater(() -> setPosition.accept(time));
                event.consume();
            });
        }

        yearMenuMap.values().forEach(yearMenu -> {
            ArrayList<MenuItem> toRemove = new ArrayList<>();
            for (MenuItem monthMenu: yearMenu.getItems()) {
                if (((Menu) monthMenu).getItems().isEmpty()) {
                    toRemove.add(monthMenu);
                }
            }
            yearMenu.getItems().removeAll(toRemove);
        });
    }

    private static void addIncludedModulesMenu(ObservableList<MenuItem> menuItems,
                                               ObservableStampFilter observableCoordinate,
                                               ManifoldCoordinate manifoldCoordinate) {
        Menu addIncludedModulesMenu = new Menu("Change included modules");
        menuItems.add(addIncludedModulesMenu);
        CheckMenuItem allModulesItem = new CheckMenuItem("all module wildcard");
        allModulesItem.setSelected(observableCoordinate.moduleSpecificationsProperty().isEmpty());
        addIncludedModulesMenu.getItems().add(allModulesItem);
        allModulesItem.setOnAction(event -> {
            Platform.runLater(() -> {
                observableCoordinate.moduleSpecificationsProperty().clear();
            });
            event.consume();
        });

        CheckMenuItem allIndividualModulesItem = new CheckMenuItem("all individual modules");

        allIndividualModulesItem.setSelected(observableCoordinate.moduleSpecificationsProperty().containsAll(
                Get.stampService().getModuleConceptsInUse().castToSet()));
        addIncludedModulesMenu.getItems().add(allIndividualModulesItem);
        allIndividualModulesItem.setOnAction(event -> {
            Platform.runLater(() -> {
                ObservableSet<ConceptSpecification> newSet = FXCollections.observableSet();
                newSet.addAll(Get.stampService().getModuleConceptsInUse().castToSet());
                observableCoordinate.moduleSpecificationsProperty().setValue(newSet);
            });
            event.consume();
        });

        Get.stampService().getModuleConceptsInUse().forEach(moduleConcept -> {
            CheckMenuItem item = new CheckMenuItem(manifoldCoordinate.getPreferredDescriptionText(moduleConcept));
            item.setSelected(observableCoordinate.moduleSpecificationsProperty().contains(moduleConcept));
            if (item.isSelected()) {
                item.setOnAction(event -> {
                    Platform.runLater(() -> {
                             observableCoordinate.moduleSpecificationsProperty().remove(moduleConcept);
                    });
                    event.consume();
                });
            } else {
                item.setOnAction(event -> {
                    Platform.runLater(() -> {
                        observableCoordinate.moduleSpecificationsProperty().add(moduleConcept);
                    });
                    event.consume();
                });
            }
            addIncludedModulesMenu.getItems().add(item);
        });
    }


    private static void addExcludedModulesMenu(ObservableList<MenuItem> menuItems,
                                               ObservableStampFilter observableCoordinate,
                                               ManifoldCoordinate manifoldCoordinate) {
        Menu excludedModulesMenu = new Menu("Change excluded modules");
        menuItems.add(excludedModulesMenu);
        CheckMenuItem noExclusionsWildcard = new CheckMenuItem("no exclusions wildcard");
        noExclusionsWildcard.setSelected(observableCoordinate.excludedModuleSpecificationsProperty().isEmpty());
        excludedModulesMenu.getItems().add(noExclusionsWildcard);
        noExclusionsWildcard.setOnAction(event -> {
            Platform.runLater(() -> {
                observableCoordinate.excludedModuleSpecificationsProperty().clear();
            });
            event.consume();
        });

        CheckMenuItem excludeAllIndividualModulesItem = new CheckMenuItem("exclude all individual modules");

        excludeAllIndividualModulesItem.setSelected(observableCoordinate.excludedModuleSpecificationsProperty().containsAll(
                Get.stampService().getModuleConceptsInUse().castToSet()));
        excludedModulesMenu.getItems().add(excludeAllIndividualModulesItem);
        if (excludeAllIndividualModulesItem.isSelected()) {
            excludeAllIndividualModulesItem.setOnAction(event -> {
                Platform.runLater(() -> {
                    observableCoordinate.excludedModuleSpecificationsProperty().clear();
                });
                event.consume();
            });
        } else {
            excludeAllIndividualModulesItem.setOnAction(event -> {
                Platform.runLater(() -> {
                    ObservableSet<ConceptSpecification> newSet = FXCollections.observableSet();
                    newSet.addAll(Get.stampService().getModuleConceptsInUse().castToSet());
                    observableCoordinate.excludedModuleSpecificationsProperty().setValue(newSet);
                });
                event.consume();
            });
        }
        Get.stampService().getModuleConceptsInUse().forEach(moduleConcept -> {
            CheckMenuItem item = new CheckMenuItem(manifoldCoordinate.getPreferredDescriptionText(moduleConcept));
            item.setSelected(observableCoordinate.excludedModuleSpecificationsProperty().contains(moduleConcept));
            if (item.isSelected()) {
                item.setOnAction(event -> {
                    Platform.runLater(() -> {
                             observableCoordinate.excludedModuleSpecificationsProperty().remove(moduleConcept);
                     });
                    event.consume();
                });
            } else {
                item.setOnAction(event -> {
                    Platform.runLater(() -> {
                        observableCoordinate.excludedModuleSpecificationsProperty().add(moduleConcept);
                    });
                    event.consume();
                });
            }
            excludedModulesMenu.getItems().add(item);
        });
    }

    private static void addChangeItemsForEdit(ManifoldCoordinate manifoldCoordinate, ObservableList<MenuItem> menuItems, ObservableEditCoordinate observableCoordinate) {

    }

    private static void addChangeItemsForNavigation(ManifoldCoordinate manifoldCoordinate, ObservableList<MenuItem> menuItems, ObservableNavigationCoordinate observableCoordinate) {
        Menu changeNavigationMenu = new Menu("Change navigation");
        menuItems.add(changeNavigationMenu);
        for (ConceptSpecification navOption: FxGet.navigationOptions()) {
            CheckMenuItem item = new CheckMenuItem(manifoldCoordinate.getPreferredDescriptionText(navOption));
            item.setSelected(observableCoordinate.getNavigationConceptNids().contains(navOption.getNid()));
            if (!item.isSelected()) {
                item.setOnAction(event -> {
                    Platform.runLater(() -> {
                        ObservableSet<ConceptSpecification> newSet = FXCollections.observableSet(navOption);
                        observableCoordinate.navigatorIdentifierConceptsProperty().setValue(newSet);
                    });
                    event.consume();
                });
            }
            changeNavigationMenu.getItems().add(item);
        }
    }

    private static void addChangeItemsForLogic(ManifoldCoordinate manifoldCoordinate, ObservableList<MenuItem> menuItems,
                                               ObservableLogicCoordinate observableCoordinate) {
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


    private static void addChangeItemsForLanguage(ManifoldCoordinate manifoldCoordinate, ObservableList<MenuItem> menuItems,
                                                  ObservableLanguageCoordinate observableCoordinate) {
        Menu changeLanguageMenu = new Menu("Change language");
        menuItems.add(changeLanguageMenu);
        for (ConceptSpecification language: FxGet.allowedLanguages()) {
            CheckMenuItem languageItem = new CheckMenuItem(manifoldCoordinate.getPreferredDescriptionText(language));
            changeLanguageMenu.getItems().add(languageItem);
            languageItem.setSelected(language.getNid() == observableCoordinate.languageConceptProperty().get().getNid());
            languageItem.setOnAction(event -> {
                Platform.runLater(() -> observableCoordinate.languageConceptProperty().setValue(language));
                event.consume();
            });
        }

        Menu changeTypeOrder = new Menu("Change description type preference order");
        menuItems.add(changeTypeOrder);
        for (ImmutableList<? extends ConceptSpecification> typePreferenceList: FxGet.allowedDescriptionTypeOrder()) {
            CheckMenuItem typeOrderItem = new CheckMenuItem(manifoldCoordinate.toConceptString(typePreferenceList.castToList(), manifoldCoordinate::getPreferredDescriptionText));
            changeTypeOrder.getItems().add(typeOrderItem);
            typeOrderItem.setSelected(observableCoordinate.descriptionTypePreferenceListProperty().getValue().equals(typePreferenceList.castToList()));
            typeOrderItem.setOnAction(event -> {
                ObservableList<ConceptSpecification> prefList = FXCollections.observableArrayList(typePreferenceList.toArray(new ConceptSpecification[0]));
                Platform.runLater(() ->
                        observableCoordinate.descriptionTypePreferenceListProperty().setValue(prefList)
                );
                event.consume();
            });
        }

        Menu changeDialectOrder = new Menu("Change dialect preference order");
        menuItems.add(changeDialectOrder);
        for (ImmutableList<? extends ConceptSpecification> dialectPreferenceList: FxGet.allowedDialectTypeOrder()) {
            CheckMenuItem dialectOrderItem = new CheckMenuItem(manifoldCoordinate.toConceptString(dialectPreferenceList.castToList(), manifoldCoordinate::getPreferredDescriptionText));
            changeDialectOrder.getItems().add(dialectOrderItem);
            dialectOrderItem.setSelected(observableCoordinate.dialectAssemblagePreferenceListProperty().getValue().equals(dialectPreferenceList.castToList()));
            dialectOrderItem.setOnAction(event -> {
                ObservableList<ConceptSpecification> prefList = FXCollections.observableArrayList(dialectPreferenceList.toArray(new ConceptSpecification[0]));
                Platform.runLater(() -> observableCoordinate.dialectAssemblagePreferenceListProperty().setValue(prefList));
                event.consume();
            });
        }
    }

    private static void addChangeItemsForManifold(ManifoldCoordinate manifoldCoordinate, ObservableList<MenuItem> menuItems,
                                                  ObservableManifoldCoordinate observableCoordinate) {
        Menu changeVertexSortMenu = new Menu("Change sort");
        menuItems.add(changeVertexSortMenu);
        VertexSort[] sorts = new VertexSort[] {VertexSortNaturalOrder.SINGLETON, VertexSortNone.SINGLETON};
        for (VertexSort vertexSort: sorts) {
            CheckMenuItem item = new CheckMenuItem(vertexSort.getVertexSortName());
            item.setSelected(observableCoordinate.getVertexSort().equals(vertexSort));
           item.setOnAction(event -> {
                Platform.runLater(() -> observableCoordinate.vertexSortProperty().setValue(vertexSort));
                event.consume();
            });
            changeVertexSortMenu.getItems().add(item);
        }

        Menu changePathMenu = new Menu("Change path");
        menuItems.add(changePathMenu);
        for (UuidStringKey key: FxGet.pathCoordinates().keySet()) {
            CheckMenuItem item = new CheckMenuItem(key.getString());
            StampPathImmutable pathCoordinate = FxGet.pathCoordinates().get(key);
            int pathNid = pathCoordinate.getPathConceptNid();
            item.setSelected(pathNid == observableCoordinate.getEdgeStampFilter().getPathNidForFilter() &&
                    pathNid == observableCoordinate.getLanguageStampFilter().getPathNidForFilter() &&
                    pathNid == observableCoordinate.getVertexStampFilter().getPathNidForFilter());
            item.setUserData(FxGet.pathCoordinates().get(key));
            item.setOnAction(event -> {
                StampPathImmutable path = (StampPathImmutable) item.getUserData();
                Platform.runLater(() -> observableCoordinate.changeManifoldPath(path.getPathConceptNid()));
                event.consume();
            });
            changePathMenu.getItems().add(item);
        }

        addChangePositionForManifold(menuItems, observableCoordinate);


        Menu changeAllowedStatusMenu = new Menu("Change allowed states");
        menuItems.add(changeAllowedStatusMenu);

        for (StatusSet statusSet: new StatusSet[] { StatusSet.ACTIVE_ONLY, StatusSet.ACTIVE_AND_INACTIVE}) {
            CheckMenuItem item = new CheckMenuItem(statusSet.toUserString());
            item.setSelected(statusSet.equals(observableCoordinate.getEdgeStampFilter().getAllowedStates()) &&
                    statusSet.equals(observableCoordinate.getLanguageStampFilter().getAllowedStates()) &&
                    statusSet.equals(observableCoordinate.getVertexStampFilter().getAllowedStates()) );
            item.setOnAction(event -> {
                Platform.runLater(() -> {
                    ObservableSet<Status> set = FXCollections.observableSet(statusSet.toArray());
                    observableCoordinate.getEdgeStampFilter().allowedStatusProperty().setValue(set);
                    observableCoordinate.getLanguageStampFilter().allowedStatusProperty().setValue(set);
                    observableCoordinate.getVertexStampFilter().allowedStatusProperty().setValue(set);
                });
                event.consume();
            });
            changeAllowedStatusMenu.getItems().add(item);
        }


        Menu changeNavigationMenu = new Menu("Change navigation");
        menuItems.add(changeNavigationMenu);
        for (ConceptSpecification navOption: FxGet.navigationOptions()) {
            ObservableNavigationCoordinate navigationCoordinate = observableCoordinate.getNavigationCoordinate();
            CheckMenuItem item = new CheckMenuItem(manifoldCoordinate.getPreferredDescriptionText(navOption));
            item.setSelected(navigationCoordinate.getNavigationConceptNids().contains(navOption.getNid()));
            if (!item.isSelected()) {
                item.setOnAction(event -> {
                    Platform.runLater(() -> {
                        ObservableSet<ConceptSpecification> newSet = FXCollections.observableSet(navOption);
                        navigationCoordinate.navigatorIdentifierConceptsProperty().setValue(newSet);
                    });
                    event.consume();
                });
            }
            changeNavigationMenu.getItems().add(item);
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



    public static <T extends ExplorationNode> Optional<NodeFactory<T>> nodeFactory(ConceptSpecification nodeSpecConcept, ManifoldCoordinate manifoldCoordinate) {
        ImmutableIntSet semanticNids = Get.assemblageService().getSemanticNidsForComponentFromAssemblage(nodeSpecConcept.getNid(), TermAux.PROVIDER_CLASS_ASSEMBLAGE.getNid());
        for (int nid: semanticNids.toArray()) {
            SemanticChronology chronology = Get.assemblageService().getSemanticChronology(nid);
            LatestVersion<StringVersion> optionalProviderClassStr = chronology.getLatestVersion(manifoldCoordinate.getVertexStampFilter());
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
                                                                       ViewProperties manifoldCoordinate) {
        return activeConceptMembers(assemblage.getNid(), manifoldCoordinate);
    }

    public static ObservableList<ConceptSnapshot> activeConceptMembers(int assemblageNid,
                                                                       ViewProperties viewProperties) {
        if (viewProperties == null) {
            throw new NullPointerException("manifoldCoordinate cannot be null");
        }
        ObservableList<ConceptSnapshot> activeConceptMemberList = FXCollections.observableArrayList();
        SingleAssemblageSnapshot<SemanticVersion> snapshot =
                Get.assemblageService().getSingleAssemblageSnapshot(assemblageNid, SemanticVersion.class, viewProperties.getManifoldCoordinate().getVertexStampFilter());

        snapshot.getLatestSemanticVersionsFromAssemblage().forEach(new Consumer<LatestVersion<SemanticVersion>>() {
            @Override
            public void accept(LatestVersion<SemanticVersion> semanticVersionLatestVersion) {
                if (semanticVersionLatestVersion.isPresent() && semanticVersionLatestVersion.get().isActive()) {
                    activeConceptMemberList.add(Get.conceptSnapshot(semanticVersionLatestVersion.get().getReferencedComponentNid(),
                            viewProperties.getManifoldCoordinate()));
                }
            }
        });
        activeConceptMemberList.sort((o1, o2) -> NaturalOrder.compareStrings(o1.getPreferredDescriptionText().get(),
                o2.getPreferredDescriptionText().get()));
        return activeConceptMemberList;
    }


    public static ViewProperties preferenceViewProperties() {
        if (preferenceViewProperties == null) {
            preferenceViewProperties = ViewProperties.make(UUID.fromString("1db21f81-c884-4dd7-8bf5-2befc955c887"), "Preferences view",
                    new ObservableManifoldCoordinateImpl(Coordinates.Manifold.DevelopmentInferredRegularNameSort()),
                    editCoordinate());
        }
        return preferenceViewProperties;
    }

    public static ViewProperties newDefaultViewProperties() {
        return ViewProperties.make(UUID.randomUUID(), "Default view",
                new ObservableManifoldCoordinateImpl(Coordinates.Manifold.DevelopmentInferredRegularNameSort()),
                editCoordinate());
    }

}
