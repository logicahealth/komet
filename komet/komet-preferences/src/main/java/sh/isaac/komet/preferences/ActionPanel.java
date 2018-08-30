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
import sh.komet.gui.control.PropertyEditorType;
import sh.komet.gui.control.PropertySheetTextWrapper;
import sh.komet.gui.control.concept.PropertySheetItemConceptConstraintWrapper;
import sh.komet.gui.control.concept.PropertySheetItemConceptWrapper;
import sh.komet.gui.control.versiontype.PropertySheetItemVersionTypeWrapper;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;

/**
 *
 * @author kec
 */
public class ActionPanel extends AbstractPreferences {

    public enum Keys {
        ACTION_NAME,
        VERSION_TYPE,
        ASSEMBLAGE,
        SEMANTIC_FIELD_CONCEPTS,
        VERSION_TYPE_FOR_ACTION
    };

    private final HashMap<ConceptSpecification, PropertySheet.Item> propertySheetItemMap = new HashMap<>();

    private final HashSet<ConceptSpecification> fieldConcepts = new HashSet<>();
    private final SimpleStringProperty actionNameProperty
            = new SimpleStringProperty(this, MetaData.ACTION_NAME____SOLOR.toExternalString());

    private final SimpleObjectProperty<ConceptSpecification> assemblageForActionProperty
            = new SimpleObjectProperty(this, MetaData.ASSEMBLAGE_FOR_ACTION____SOLOR.toExternalString());

    private final SimpleObjectProperty<VersionType> versionTypeForActionProperty
            = new SimpleObjectProperty(this, ObservableFields.VERSION_TYPE_FOR_ACTION.toExternalString());

    private final SimpleObjectProperty<List<PropertySheet.Item>> itemListProperty = new SimpleObjectProperty<>(this, "item list property");

    public ActionPanel(IsaacPreferences preferencesNode, Manifold manifold,
            KometPreferencesController kpc) {
        super(preferencesNode,
                preferencesNode.get(Keys.ACTION_NAME, "action " + preferencesNode.name()),
                manifold, kpc);
        revertFields();
        save();
        getItemList().add(new PropertySheetTextWrapper(manifold, actionNameProperty));
        getItemList().add(new PropertySheetItemVersionTypeWrapper("Version type", VersionType.CONCEPT));
        PropertySheetItemConceptWrapper conceptWrapper = new PropertySheetItemConceptWrapper(manifold, assemblageForActionProperty);
        getItemList().add(conceptWrapper);

        handleAssemblageChange(null, null, assemblageForActionProperty.get());
        assemblageForActionProperty.addListener(this::handleAssemblageChange);
    }

    private void handleAssemblageChange(ObservableValue<? extends ConceptSpecification> observable, ConceptSpecification oldValue, ConceptSpecification newValue) {
        if (this.itemListProperty.get() != null) {
            getItemList().removeAll(itemListProperty.get());
        }
        this.itemListProperty.set(FxGet.constraintPropertyItemsForAssemblageSemantic(newValue, getManifold()));
        getItemList().addAll(itemListProperty.get());
        for (PropertySheet.Item item : itemListProperty.get()) {
            if (item instanceof PropertySheetItemConceptConstraintWrapper) {
                readPropertySheetItemConceptContstraintWrapper(item);
            }
        }
        kpc.updateDetail();
    }

    @Override
    final void saveFields() throws BackingStoreException {
        getPreferencesNode().put(Keys.ACTION_NAME, actionNameProperty.get());
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
            } else {
                throw new UnsupportedOperationException("Can't handle : " + item);
            }
        }
        ArrayList<String> semanticFieldConcepts = new ArrayList<>();
        for (ConceptSpecification spec : fieldConcepts) {
            semanticFieldConcepts.add(spec.toExternalString());
        }
        getPreferencesNode().putList(Keys.SEMANTIC_FIELD_CONCEPTS, semanticFieldConcepts);
        // For each semantic field, read/write constraints 
        FxGet.rulesDrivenKometService().addResourcesAndUpdate(getBusinessRulesResources());
    }

    @Override
    final void revertFields() {
        this.actionNameProperty.set(getPreferencesNode().get(Keys.ACTION_NAME, getGroupName()));
        this.versionTypeForActionProperty.set(VersionType.valueOf(getPreferencesNode().get(Keys.VERSION_TYPE_FOR_ACTION, VersionType.CONCEPT.name())));
        this.assemblageForActionProperty.set(getPreferencesNode().getConceptSpecification(Keys.ASSEMBLAGE, TermAux.ASSEMBLAGE));
        this.fieldConcepts.clear();
        for (String externalString : getPreferencesNode().getList(Keys.SEMANTIC_FIELD_CONCEPTS)) {
            this.fieldConcepts.add(new ConceptProxy(externalString));
        }
        this.propertySheetItemMap.clear();
        this.itemListProperty.set(FxGet.constraintPropertyItemsForAssemblageSemantic(assemblageForActionProperty.get(), getManifold()));

        for (PropertySheet.Item item : itemListProperty.get()) {
            if (item instanceof PropertySheetItemConceptConstraintWrapper) {
                readPropertySheetItemConceptContstraintWrapper(item);

            } else {
                throw new UnsupportedOperationException("Can't handle : " + item);
            }
        }
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
                "src/main/resources/rules/sh/isaac/provider/drools/" + preferencesNode.name() + ".drl",
                getRuleBytes()));

        return resources.toArray(new BusinessRulesResource[resources.size()]);
    }

    private byte[] getRuleBytes() {
        StringBuilder b = new StringBuilder();
        b.append("package sh.isaac.provider.drools;\n");
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
        b.append("import sh.komet.gui.control.concept.PropertySheetItemConceptWrapper;\n");
        b.append("import sh.komet.gui.control.property.PropertySheetItem;\n");
        b.append("import sh.komet.gui.control.property.PropertySheetPurpose;\n");
        b.append("import sh.komet.gui.control.property.EditorType;\n");
        b.append("import sh.isaac.api.logic.NodeSemantic;\n");
        b.append("\n");
        b.append("rule \"").append(actionNameProperty.get()).append("\"\n");
        b.append("when\n");
        b.append("   $addAttachmentToVersion : AddAttachmentMenuItems(getVersionType() == VersionType.").append(versionTypeForActionProperty.get().name()).append(")\n");
        b.append("then\n");
        b.append("   System.out.println(\"AddAttachmentMenuItems: \" + $addAttachmentToVersion);\n");
        b.append("   PropertySheetMenuItem propertySheetMenuItem = $addAttachmentToVersion.makePropertySheetMenuItem(\"")
                .append(actionNameProperty.get())
                .append("\", new ").append(new ConceptProxy(this.assemblageForActionProperty.get().toExternalString()).toString())
                .append(");\n");

        for (ConceptSpecification fieldConcept : fieldConcepts) {
            PropertySheet.Item item = propertySheetItemMap.get(fieldConcept);
            PropertyEditorType type = getPropertyEditorType(item);
            b.append("   propertySheetMenuItem.addPropertyToEdit(\"")
                    .append(getManifold().getPreferredDescriptionText(fieldConcept)).append("\", new ")
                    .append(new ConceptProxy(fieldConcept.toExternalString()).toString()).append(", PropertyEditorType.")
                    .append(type.name())
                    .append(");\n");
        }

        b.append("end\n\n");

        for (ConceptSpecification fieldConcept : fieldConcepts) {
            PropertySheet.Item item = propertySheetItemMap.get(fieldConcept);
            PropertyEditorType type = getPropertyEditorType(item);
            if (type == PropertyEditorType.CONCEPT) {
                PropertySheetItemConceptConstraintWrapper constraintsItem = (PropertySheetItemConceptConstraintWrapper) item;
                PropertySheetItemConceptWrapper conceptItem = constraintsItem.getValue();
                b.append("rule \"Setup constraints for ").append(getManifold().getFullySpecifiedDescriptionText(fieldConcept)).append("\"\n");
                b.append("when\n");
                b.append("   $propertySheetItem : PropertySheetItemConceptWrapper(getSpecification() == new ")
                        .append(new ConceptProxy(fieldConcept.toExternalString()).toString()).append(");\n");
                b.append("then\n");
                b.append("   $propertySheetItem.setDefaultValue(new ").append(new ConceptProxy(conceptItem.getValue()).toString()).append(");\n");
                for (ConceptSpecification conceptSpec: conceptItem.getAllowedValues()) {
                    b.append("   $propertySheetItem.getAllowedValues().add(new ").append(new ConceptProxy(conceptSpec).toString()).append(");\n");
                }
                b.append("end\n\n");
            }

        }
        return b.toString().getBytes();
    }

    protected PropertyEditorType getPropertyEditorType(PropertySheet.Item item) throws UnsupportedOperationException {
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
}
