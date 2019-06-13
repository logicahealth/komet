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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.TreeMap;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javax.inject.Singleton;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.controlsfx.control.PropertySheet;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.StaticIsaacCache;
import sh.isaac.api.TaxonomySnapshot;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.component.semantic.version.brittle.Nid1_Int2_Version;
import sh.isaac.api.observable.coordinate.ObservableEditCoordinate;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.api.preferences.PreferencesService;
import sh.isaac.api.tree.TaxonomyAmalgam;
import sh.komet.gui.contract.DialogService;
import sh.komet.gui.contract.GuiConceptBuilder;
import sh.komet.gui.contract.GuiSearcher;
import sh.komet.gui.contract.NodeFactory;
import sh.komet.gui.contract.RulesDrivenKometService;
import sh.komet.gui.contract.StatusMessageService;
import sh.komet.gui.contract.preferences.KometPreferences;
import sh.komet.gui.control.concept.PropertySheetItemConceptConstraintWrapper;
import sh.komet.gui.control.concept.PropertySheetItemConceptWrapper;
import sh.komet.gui.control.property.PropertySheetItem;
import sh.komet.gui.control.property.SessionProperty;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.provider.StatusMessageProvider;

/**
 *
 * @author kec
 */
@Service
@Singleton
public class FxGet implements StaticIsaacCache {
    private static final HashMap<Manifold.ManifoldGroup, Manifold> MANIFOLDS = new HashMap<>();


    private static DialogService DIALOG_SERVICE = null;
    private static RulesDrivenKometService RULES_DRIVEN_KOMET_SERVICE = null;
    private static StatusMessageProvider STATUS_MESSAGE_PROVIDER = null;
    private static FxConfiguration FX_CONFIGURATION = null;
    // TODO make SEARCHER_LIST behave like a normal lookup service. 
    private static final List<GuiSearcher> SEARCHER_LIST = new ArrayList<>();
    // TODO make SEARCHER_LIST behave like a normal lookup service. 
    private static final List<GuiConceptBuilder> BUILDER_LIST = new ArrayList<>();
    
    private static final SimpleStringProperty CONFIGURATION_NAME_PROPERTY = new SimpleStringProperty(null, MetaData.CONFIGURATION_NAME____SOLOR.toExternalString(), "viewer");
    private static final ObservableMap<String, TaxonomyAmalgam> TAXONOMY_CONFIGURATIONS = FXCollections.observableHashMap();
    private static final ObservableList<String> TAXONOMY_CONFIGURATION_KEY_LIST = FXCollections.observableArrayList();
    private static final String DEFAULT_TAXONOMY_CONFIGURATION = "Defining";
    static {
        TAXONOMY_CONFIGURATIONS.addListener((MapChangeListener.Change<? extends String, ? extends TaxonomySnapshot> change) -> {
            if (change.wasAdded()) {
                TAXONOMY_CONFIGURATION_KEY_LIST.add(change.getKey());
            }
            if (change.wasRemoved()) {            
                TAXONOMY_CONFIGURATION_KEY_LIST.remove(change.getKey());
            }
        });
        TAXONOMY_CONFIGURATIONS.put(DEFAULT_TAXONOMY_CONFIGURATION, null);
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
    
    public static String getConfigurationName() {
        return FxGet.CONFIGURATION_NAME_PROPERTY.get();
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
            TAXONOMY_CONFIGURATIONS.clear();
            TAXONOMY_CONFIGURATION_KEY_LIST.clear();
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
        OptionalInt optionalSemanticConceptNid = Get.assemblageService().getSemanticTypeConceptForAssemblage(assemblageConcept, manifold);

        if (optionalSemanticConceptNid.isPresent()) {
            int semanticConceptNid = optionalSemanticConceptNid.getAsInt();
            NidSet semanticTypeOfFields = Get.assemblageService().getSemanticNidsForComponentFromAssemblage(semanticConceptNid, TermAux.SEMANTIC_FIELD_DATA_TYPES_ASSEMBLAGE.getNid());
            for (int nid : semanticTypeOfFields.asArray()) { // one member, "Concept field": 1
                SemanticChronology semanticTypeField = Get.assemblageService().getSemanticChronology(nid);
                LatestVersion<Version> latestSemanticTypeField = semanticTypeField.getLatestVersion(manifold);
                Nid1_Int2_Version latestSemanticTypeFieldVersion = (Nid1_Int2_Version) latestSemanticTypeField.get();
                fieldIndexToFieldDataType.put(latestSemanticTypeFieldVersion.getInt2(), Get.concept(latestSemanticTypeFieldVersion.getNid1()));
            }

            NidSet assemblageSemanticFields = Get.assemblageService().getSemanticNidsForComponentFromAssemblage(assemblageConcept.getNid(), MetaData.SEMANTIC_FIELDS_ASSEMBLAGE____SOLOR.getNid());
            for (int nid : assemblageSemanticFields.asArray()) {
                SemanticChronology semanticField = Get.assemblageService().getSemanticChronology(nid);
                LatestVersion<Version> latestSemanticField = semanticField.getLatestVersion(manifold);
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
        return (ObservableEditCoordinate) SecurityUtils.getSubject().getSession().getAttribute(SessionProperty.EDIT_COORDINATE);
    }
    
    public static ObservableList<String> taxonomyConfigurationNames() {
        return TAXONOMY_CONFIGURATION_KEY_LIST;
    }
    
    public static TaxonomyAmalgam taxonomyConfiguration(String configurationName) {
        return TAXONOMY_CONFIGURATIONS.get(configurationName);
    }

    public static void addTaxonomyConfiguration(String configurationName, TaxonomyAmalgam taxonomyConfiguration) {
        TAXONOMY_CONFIGURATIONS.put(configurationName, taxonomyConfiguration);
    }

    public static void removeTaxonomyConfiguration(String configurationName) {
        TAXONOMY_CONFIGURATIONS.remove(configurationName);
    }

    public static String defaultTaxonomyConfiguration() {
        return DEFAULT_TAXONOMY_CONFIGURATION;
    }
    
    public static TaxonomySnapshot taxonomySnapshot(Manifold manifold, String configurationName) {
        if (configurationName.equals(DEFAULT_TAXONOMY_CONFIGURATION)) {
            return Get.taxonomyService().getSnapshot(manifold);
        }
        TaxonomyAmalgam amalgam = taxonomyConfiguration(configurationName);
        return amalgam.makeAnalog(manifold);
    }


    public static <T extends ExplorationNode> Optional<NodeFactory<T>> nodeFactory(ConceptSpecification nodeSpecConcept) {
        NidSet semanticNids = Get.assemblageService().getSemanticNidsForComponentFromAssemblage(nodeSpecConcept.getNid(), TermAux.PROVIDER_CLASS_ASSEMBLAGE.getNid());
        for (int nid: semanticNids.asArray()) {
            SemanticChronology chronology = Get.assemblageService().getSemanticChronology(nid);
            LatestVersion<StringVersion> optionalProviderClassStr = chronology.getLatestVersion(FxGet.getManifold(Manifold.ManifoldGroup.KOMET));
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

    public static Manifold getManifold(Manifold.ManifoldGroup manifoldGroup) {
        if (MANIFOLDS.isEmpty()) {
            for (Manifold.ManifoldGroup mg : Manifold.ManifoldGroup.values()) {
                MANIFOLDS.put(mg, Manifold.make(mg));
            }

        }
        return MANIFOLDS.get(manifoldGroup);
    }
}
