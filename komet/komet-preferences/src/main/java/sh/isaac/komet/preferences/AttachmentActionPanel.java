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
package sh.isaac.komet.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.prefs.BackingStoreException;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import org.controlsfx.control.PropertySheet;
import sh.isaac.MetaData;
import sh.isaac.api.BusinessRulesResource;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.model.observable.ObservableFields;
import sh.komet.gui.contract.preferences.AttachmentItem;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.komet.gui.control.PropertyEditorType;
import sh.komet.gui.control.PropertySheetBooleanWrapper;
import sh.komet.gui.control.PropertySheetTextWrapper;
import sh.komet.gui.control.concept.PropertySheetItemConceptConstraintWrapper;
import sh.komet.gui.control.concept.PropertySheetItemConceptWrapper;
import sh.komet.gui.control.property.PropertySheetItem;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.control.versiontype.PropertySheetItemVersionTypeWrapper;
import sh.komet.gui.util.FxGet;

/**
 *
 * @author kec
 */
public class AttachmentActionPanel extends AbstractPreferences implements AttachmentItem {

    public enum Keys {
        ITEM_NAME,
        VERSION_TYPE,
        ASSEMBLAGE,
        SEMANTIC_FIELD_CONCEPTS,
        VERSION_TYPE_FOR_ACTION,
        SHOW_STATUS,
        SHOW_MODULE,
        SHOW_PATH,
        SHOW_SEARCH,
        SHOW_HISTORY
    };

    private final HashMap<ConceptSpecification, PropertySheet.Item> propertySheetItemMap = new HashMap<>();

    private final HashSet<ConceptSpecification> fieldConcepts = new HashSet<>();
    private final SimpleStringProperty nameProperty
            = new SimpleStringProperty(this, MetaData.ACTION_NAME____SOLOR.toExternalString());

    private final SimpleObjectProperty<ConceptSpecification> assemblageForActionProperty
            = new SimpleObjectProperty(this, MetaData.ASSEMBLAGE_FOR_ACTION____SOLOR.toExternalString());

    private final SimpleObjectProperty<VersionType> versionTypeForActionProperty
            = new SimpleObjectProperty(this, ObservableFields.VERSION_TYPE_FOR_ACTION.toExternalString());

    private final SimpleObjectProperty<List<PropertySheet.Item>> itemListProperty = new SimpleObjectProperty<>(this, "item list property");

    private final SimpleBooleanProperty showStatusProperty = new SimpleBooleanProperty(this, "edit status", true);

    private final SimpleBooleanProperty showModuleProperty = new SimpleBooleanProperty(this, "edit module", true);

    private final SimpleBooleanProperty showPathProperty = new SimpleBooleanProperty(this, "edit path", true);

    private final SimpleBooleanProperty showSearchProperty = new SimpleBooleanProperty(this, "allow search", true);

    private final SimpleBooleanProperty showHistoryProperty = new SimpleBooleanProperty(this, "allow history", true);

    public AttachmentActionPanel(IsaacPreferences preferencesNode, ViewProperties viewProperties,
                                 KometPreferencesController kpc) {
        super(preferencesNode,
                getGroupNameForAction(preferencesNode),
                viewProperties, kpc);
        nameProperty.set(groupNameProperty().get());
        nameProperty.addListener((observable, oldValue, newValue) -> {
            groupNameProperty().set(newValue);
        });
        revertFields();
        save();
        getItemList().add(new PropertySheetTextWrapper(viewProperties.getManifoldCoordinate(), nameProperty));
        getItemList().add(new PropertySheetBooleanWrapper("edit status", showStatusProperty));
        getItemList().add(new PropertySheetBooleanWrapper("edit module", showModuleProperty));
        getItemList().add(new PropertySheetBooleanWrapper("edit path", showPathProperty));
        getItemList().add(new PropertySheetBooleanWrapper("allow search", showSearchProperty));
        getItemList().add(new PropertySheetBooleanWrapper("allow history", showHistoryProperty));

        getItemList().add(new PropertySheetItemVersionTypeWrapper("Version type", versionTypeForActionProperty));
        PropertySheetItemConceptWrapper conceptWrapper = new PropertySheetItemConceptWrapper(viewProperties.getManifoldCoordinate(), assemblageForActionProperty);
        getItemList().add(conceptWrapper);

        handleAssemblageChange(null, null, assemblageForActionProperty.get());
        assemblageForActionProperty.addListener(this::handleAssemblageChange);
    }

    private static String getGroupNameForAction(IsaacPreferences preferencesNode) {
        if (preferencesNode.hasKey("8c6a76da-206e-314c-b1e2-eda9037d431e.Keys.ACTION_NAME")) {
            String actionName = preferencesNode.get("8c6a76da-206e-314c-b1e2-eda9037d431e.Keys.ACTION_NAME", "");
            preferencesNode.remove("8c6a76da-206e-314c-b1e2-eda9037d431e.Keys.ACTION_NAME");
            preferencesNode.put(Keys.ITEM_NAME, actionName);
        }
        return preferencesNode.get(Keys.ITEM_NAME, "error");
    }

    private void handleAssemblageChange(ObservableValue<? extends ConceptSpecification> observable, ConceptSpecification oldValue, ConceptSpecification newValue) {
        if (this.itemListProperty.get() != null) {
            getItemList().removeAll(itemListProperty.get());
        }
        this.itemListProperty.set(FxGet.constraintPropertyItemsForAssemblageSemantic(newValue, getViewProperties().getManifoldCoordinate()));
        getItemList().addAll(itemListProperty.get());
        for (PropertySheet.Item item : itemListProperty.get()) {
            if (item instanceof PropertySheetItemConceptConstraintWrapper) {
                readPropertySheetItemConceptContstraintWrapper(item);
            }
        }
        kpc.updateDetail();
    }

    @Override
    final protected void saveFields() throws BackingStoreException {
        getPreferencesNode().put(Keys.ITEM_NAME, nameProperty.get());
        getPreferencesNode().put(Keys.VERSION_TYPE_FOR_ACTION, versionTypeForActionProperty.get().name());
        getPreferencesNode().putConceptSpecification(Keys.ASSEMBLAGE, assemblageForActionProperty.get());
        // For each field, save a list: default value first, then allowed values. 
        for (PropertySheet.Item item : itemListProperty.get()) {
            if (item instanceof PropertySheetItemConceptConstraintWrapper) {
                PropertySheetItemConceptConstraintWrapper constraintsItem = (PropertySheetItemConceptConstraintWrapper) item;
                ConceptSpecification fieldConcept = constraintsItem.getFieldConcept();
                fieldConcepts.add(fieldConcept);
                propertySheetItemMap.put(fieldConcept, item);
                constraintsItem.writeToPreferences(getPreferencesNode().node(fieldConcept.getPrimordialUuid().toString()));
            } else if (item instanceof PropertySheetItem) {
                PropertySheetItem propertySheetItem = (PropertySheetItem) item;
                switch (propertySheetItem.getEditorType()) {
                    case TEXT:
                        ConceptSpecification fieldConcept = propertySheetItem.getSpecification();
                        fieldConcepts.add(fieldConcept);
                        propertySheetItemMap.put(fieldConcept, item);
                        break;
                    default:
                        throw new UnsupportedOperationException("Can't handle: " + propertySheetItem.getEditorType());

                }

            } else {
                throw new UnsupportedOperationException("Can't handle : " + item);
            }
        }
        ArrayList<String> semanticFieldConcepts = new ArrayList<>();
        for (ConceptSpecification spec : fieldConcepts) {
            semanticFieldConcepts.add(spec.toExternalString());
        }
        getPreferencesNode().putList(Keys.SEMANTIC_FIELD_CONCEPTS, semanticFieldConcepts);
        getPreferencesNode().putBoolean(Keys.SHOW_HISTORY, this.showHistoryProperty.get());
        getPreferencesNode().putBoolean(Keys.SHOW_MODULE, this.showModuleProperty.get());
        getPreferencesNode().putBoolean(Keys.SHOW_PATH, this.showPathProperty.get());
        getPreferencesNode().putBoolean(Keys.SHOW_SEARCH, this.showSearchProperty.get());
        getPreferencesNode().putBoolean(Keys.SHOW_STATUS, this.showStatusProperty.get());

        // For each semantic field, read/write constraints 
        FxGet.rulesDrivenKometService().addResourcesAndUpdate(getBusinessRulesResources());
    }

    @Override
    final protected void revertFields() {
        this.nameProperty.set(getPreferencesNode().get(Keys.ITEM_NAME, getGroupName()));
        this.versionTypeForActionProperty.set(VersionType.valueOf(getPreferencesNode().get(Keys.VERSION_TYPE_FOR_ACTION, VersionType.CONCEPT.name())));
        this.assemblageForActionProperty.set(getPreferencesNode().getConceptSpecification(Keys.ASSEMBLAGE, TermAux.ASSEMBLAGE));
        this.fieldConcepts.clear();
        for (String externalString : getPreferencesNode().getList(Keys.SEMANTIC_FIELD_CONCEPTS)) {
            this.fieldConcepts.add(new ConceptProxy(externalString));
        }
        this.propertySheetItemMap.clear();
        this.itemListProperty.set(FxGet.constraintPropertyItemsForAssemblageSemantic(assemblageForActionProperty.get(), getViewProperties().getManifoldCoordinate()));

        for (PropertySheet.Item item : itemListProperty.get()) {
            if (item instanceof PropertySheetItemConceptConstraintWrapper) {
                readPropertySheetItemConceptContstraintWrapper(item);

            } else if (item instanceof PropertySheetItem) {
                PropertySheetItem propertySheetItem = (PropertySheetItem) item;
                switch (propertySheetItem.getEditorType()) {
                    case TEXT:
                        ConceptSpecification fieldConcept = propertySheetItem.getSpecification();
                        fieldConcepts.add(fieldConcept);
                        propertySheetItemMap.put(fieldConcept, item);
                        break;
                    default:
                        throw new UnsupportedOperationException("Can't handle: " + propertySheetItem.getEditorType());

                }

            } else {
                throw new UnsupportedOperationException("Can't handle : " + item);
            }
        }

        this.showHistoryProperty.set(getPreferencesNode().getBoolean(Keys.SHOW_HISTORY, true));
        this.showModuleProperty.set(getPreferencesNode().getBoolean(Keys.SHOW_MODULE, true));
        this.showPathProperty.set(getPreferencesNode().getBoolean(Keys.SHOW_PATH, true));
        this.showSearchProperty.set(getPreferencesNode().getBoolean(Keys.SHOW_SEARCH, true));
        this.showStatusProperty.set(getPreferencesNode().getBoolean(Keys.SHOW_STATUS, true));
    }

    protected void readPropertySheetItemConceptContstraintWrapper(PropertySheet.Item item) {
        PropertySheetItemConceptConstraintWrapper constraintsItem = (PropertySheetItemConceptConstraintWrapper) item;
        ConceptSpecification fieldConcept = constraintsItem.getFieldConcept();
        this.fieldConcepts.add(fieldConcept);
        this.propertySheetItemMap.put(fieldConcept, item);
        addProperty(constraintsItem.getValue().getObservableValue().get());
        addProperty(constraintsItem.getValue().getAllowedValues());
        addProperty(constraintsItem.getValue().allowHistoryProperty());
        addProperty(constraintsItem.getValue().allowSearchProperty());
        constraintsItem.readFromPreferences(getPreferencesNode().node(fieldConcept.getPrimordialUuid().toString()));
    }

    public BusinessRulesResource[] getBusinessRulesResources() {
        List<BusinessRulesResource> resources = new ArrayList<>();

        resources.add(new BusinessRulesResource(
                "sh/komet/rules/user" + preferencesNode.name() + ".drl",
                getRuleBytes()));

        return resources.toArray(new BusinessRulesResource[resources.size()]);
    }

    private byte[] getRuleBytes() {
        StringBuilder b = new StringBuilder();
        b.append("package sh.komet.rules.user;\n");
        b.append("import java.util.ArrayList;\n");
        b.append("import java.util.List;\n");
        b.append("import java.util.Map;\n");
        b.append("import java.util.UUID;\n");
        b.append("import java.util.function.Consumer;\n");
        b.append("import javafx.scene.control.MenuItem;\n");
        b.append("import javafx.beans.property.Property;\n");
        b.append("import org.controlsfx.control.PropertySheet;\n");
        b.append("import org.controlsfx.control.PropertySheet.Item;\n");
        b.append("import sh.isaac.api.observable.ObservableCategorizedVersion;\n");
        b.append("import sh.isaac.api.ConceptProxy;\n");
        b.append("import sh.isaac.api.Get;\n");
        b.append("import sh.isaac.api.component.concept.ConceptSpecification;\n");
        b.append("import sh.isaac.api.Status;\n");
        b.append("import sh.isaac.provider.drools.AddEditLogicalExpressionNodeMenuItems;\n");
        b.append("import sh.komet.gui.control.PropertySheetMenuItem;\n");
        b.append("import sh.komet.gui.manifold.Manifold;\n");
        b.append("import sh.isaac.MetaData;\n");
        b.append("import sh.isaac.api.bootstrap.TermAux;\n");
        b.append("import sh.isaac.api.chronicle.VersionCategory;\n");
        b.append("import sh.isaac.api.chronicle.VersionType;\n");
        b.append("import sh.isaac.provider.drools.AddAttachmentMenuItems;\n");
        b.append("import sh.komet.gui.control.PropertyEditorType;\n");
        b.append("import sh.komet.gui.control.PropertySheetTextWrapper;\n");
        b.append("import sh.komet.gui.control.concept.PropertySheetItemConceptWrapper;\n");
        b.append("import sh.komet.gui.control.property.PropertySheetItem;\n");
        b.append("import sh.komet.gui.control.property.PropertySheetPurpose;\n");
        b.append("import sh.komet.gui.control.property.EditorType;\n");
        b.append("import sh.isaac.api.logic.NodeSemantic;\n");
        b.append("\n");
        b.append("rule \"").append(nameProperty.get())
                .append(" ").append(getPreferencesNode().name()).append("\"\n");
        b.append("when\n");
        b.append("   $addAttachmentToVersion : AddAttachmentMenuItems(getVersionType() == VersionType.").append(versionTypeForActionProperty.get().name()).append(")\n");
        b.append("then\n");
        b.append("   PropertySheetMenuItem propertySheetMenuItem = $addAttachmentToVersion.makePropertySheetMenuItem(\"")
                .append(nameProperty.get())
                .append("\", new ").append(new ConceptProxy(this.assemblageForActionProperty.get().toExternalString()).toString())
                .append(");\n");

        for (ConceptSpecification fieldConcept : fieldConcepts) {
            PropertySheet.Item item = propertySheetItemMap.get(fieldConcept);
            if (item == null) {
                LOG.error("Null item for: " + fieldConcept);
            } else {
                PropertyEditorType type = getPropertyEditorType(item);
                b.append("   propertySheetMenuItem.addPropertyToEdit(\"")
                        .append(getManifoldCoordinate().getPreferredDescriptionText(fieldConcept)).append("\", new ")
                        .append(new ConceptProxy(fieldConcept.toExternalString()).toString()).append(", PropertyEditorType.")
                        .append(type.name())
                        .append(");\n");
            }
        }

        if (showModuleProperty.get()) {
            b.append("   propertySheetMenuItem.addPropertyToEdit(\"module\", MetaData.MODULE_NID_FOR_VERSION____SOLOR, PropertyEditorType.CONCEPT);\n");
        }
        if (showStatusProperty.get()) {
            b.append("   propertySheetMenuItem.addPropertyToEdit(\"status\", MetaData.STATUS_FOR_VERSION____SOLOR, PropertyEditorType.STATUS);\n");
        }
        if (showPathProperty.get()) {
            b.append("   propertySheetMenuItem.addPropertyToEdit(\"path\", MetaData.PATH_NID_FOR_VERSION____SOLOR, PropertyEditorType.CONCEPT);\n");
        }
        b.append("end\n\n");

        for (ConceptSpecification fieldConcept : fieldConcepts) {
            PropertySheet.Item item = propertySheetItemMap.get(fieldConcept);
            if (item == null) {
                LOG.error("Null item for: " + fieldConcept);
            } else {
                PropertyEditorType type = getPropertyEditorType(item);
                switch (type) {
                    case CONCEPT: {
                        PropertySheetItemConceptConstraintWrapper constraintsItem = (PropertySheetItemConceptConstraintWrapper) item;
                        PropertySheetItemConceptWrapper conceptItem = constraintsItem.getValue();
                        b.append("rule \"Setup constraints for ").append(getManifoldCoordinate().getFullyQualifiedDescriptionText(fieldConcept))
                                .append(" ").append(getPreferencesNode().name()).append("\"\n");
                        b.append("when\n");
                        b.append("   $propertySheetItem : PropertySheetItemConceptWrapper(getSpecification() == new ")
                                .append(new ConceptProxy(fieldConcept.toExternalString()).toString()).append(");\n");
                        b.append("then\n");
                        b.append("   $propertySheetItem.setDefaultValue(new ").append(new ConceptProxy(conceptItem.getValue()).toString()).append(");\n");
                        for (ConceptSpecification conceptSpec : conceptItem.getAllowedValues()) {
                            b.append("   $propertySheetItem.getAllowedValues().add(new ").append(new ConceptProxy(conceptSpec).toString()).append(");\n");
                        }
                        b.append("   $propertySheetItem.setAllowSearch(").append(Boolean.toString(showSearchProperty.get())).append(");\n");
                        b.append("   $propertySheetItem.setAllowHistory(").append(Boolean.toString(showHistoryProperty.get())).append(");\n");

                        b.append("end\n\n");
                        break;
                    }
                    case TEXT: {
//                    b.append("rule \"Setup ").append(getManifold().getFullySpecifiedDescriptionText(fieldConcept)).append("\"\n");
//                    b.append("   lock-on-active true\n");
//                    b.append("when\n");
//                    b.append("   $propertySheetItem : PropertySheetTextWrapper(getSpecification() == new ")
//                            .append(new ConceptProxy(fieldConcept.toExternalString()).toString()).append(");\n");
//                    b.append("then\n");
//                    b.append("   $propertySheetItem.setEditorType(EditorType.TEXT);\n");
//                    b.append("end\n\n");
                        break;
                    }
                    case STATUS:
                }
            }
        }
        return b.toString().getBytes();
    }

    protected PropertyEditorType getPropertyEditorType(PropertySheet.Item item) throws UnsupportedOperationException {
        if (item instanceof PropertySheetItem) {
            PropertySheetItem propertySheetItem = (PropertySheetItem) item;
            // TODO consolidate PropertyEditorType and EditorType?
            switch (propertySheetItem.getEditorType()) {
                case TEXT:
                    return PropertyEditorType.TEXT;
                case CONCEPT_SPEC_CHOICE_BOX:
                    return PropertyEditorType.CONCEPT;
                case OBJECT_CHOICE_BOX:
                    break;
                case BOOLEAN:
                    throw new UnsupportedOperationException("Boolean not yet supported");
                case UNSPECIFIED:
            }
        }

        PropertyEditorType type;
        if (item.getValue() instanceof ConceptSpecification) {
            type = PropertyEditorType.CONCEPT;
        } else if (item.getValue() instanceof String) {
            type = PropertyEditorType.TEXT;
        } else if (item.getValue() instanceof Status) {
            type = PropertyEditorType.STATUS;
        } else {
            throw new UnsupportedOperationException("Can't handle: " + item.getValue() + " from: " + item);
        }
        return type;
    }

    @Override
    public boolean showDelete() {
        return true;
    }

}
