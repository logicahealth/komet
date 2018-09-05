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
import java.util.List;
import java.util.prefs.BackingStoreException;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.controlsfx.control.PropertySheet;
import sh.isaac.MetaData;
import sh.isaac.api.BusinessRulesResource;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.model.observable.ObservableFields;
import sh.komet.gui.control.PropertyEditorType;
import sh.komet.gui.control.PropertySheetTextWrapper;
import sh.komet.gui.control.concept.PropertySheetItemConceptConstraintWrapper;
import sh.komet.gui.control.concept.PropertySheetItemConceptWrapper;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;

/**
 *
 * @author kec
 */
public class LogicActionPanel extends AbstractPreferences {

    public enum Keys {
        ACTION_NAME,
        ROLE_TYPE_TO_ADD,
        ASSEMBLAGE_FOR_CONSTRAINT
    };

    private final SimpleStringProperty actionNameProperty
            = new SimpleStringProperty(this, MetaData.ACTION_NAME____SOLOR.toExternalString());

    private final SimpleObjectProperty<ConceptSpecification> assemblageForConstraintProperty
            = new SimpleObjectProperty(this, ObservableFields.ASSEMBLAGE_FOR_CONSTRAINT.toExternalString());

    private final SimpleObjectProperty<ConceptSpecification> roleTypeProperty
            = new SimpleObjectProperty(this, ObservableFields.ROLE_TYPE_TO_ADD.toExternalString());



    public LogicActionPanel(IsaacPreferences preferencesNode, Manifold manifold,
            KometPreferencesController kpc) {
        super(preferencesNode,
                preferencesNode.get(Keys.ACTION_NAME, "logic action " + preferencesNode.name()),
                manifold, kpc);
        revertFields();
        save();
        getItemList().add(new PropertySheetTextWrapper(manifold, actionNameProperty));

        PropertySheetItemConceptWrapper roleTypeWrapper = new PropertySheetItemConceptWrapper(manifold, roleTypeProperty);
        getItemList().add(roleTypeWrapper);
        
        PropertySheetItemConceptWrapper assemblageForConstraintWrapper = new PropertySheetItemConceptWrapper(manifold, assemblageForConstraintProperty);
        getItemList().add(assemblageForConstraintWrapper);
    }

    @Override
    final void saveFields() throws BackingStoreException {
        getPreferencesNode().put(Keys.ACTION_NAME, actionNameProperty.get());
        getPreferencesNode().putConceptSpecification(Keys.ROLE_TYPE_TO_ADD, roleTypeProperty.get());
        getPreferencesNode().putConceptSpecification(Keys.ASSEMBLAGE_FOR_CONSTRAINT, assemblageForConstraintProperty.get());

        // For each semantic field, read/write constraints 
        FxGet.rulesDrivenKometService().addResourcesAndUpdate(getBusinessRulesResources());
    }

    @Override
    final void revertFields() {
        this.actionNameProperty.set(getPreferencesNode().get(Keys.ACTION_NAME, getGroupName()));
        this.roleTypeProperty.set(getPreferencesNode().getConceptSpecification(Keys.ROLE_TYPE_TO_ADD, MetaData.ROLE____SOLOR));
        this.assemblageForConstraintProperty.set(getPreferencesNode().getConceptSpecification(Keys.ASSEMBLAGE_FOR_CONSTRAINT, TermAux.ASSEMBLAGE));
    }

    protected void readPropertySheetItemConceptContstraintWrapper(PropertySheet.Item item) {
        PropertySheetItemConceptConstraintWrapper constraintsItem = (PropertySheetItemConceptConstraintWrapper) item;
        ConceptSpecification fieldConcept = constraintsItem.getFieldConcept();
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
        b.append("   $addEditLogicalNode : AddEditLogicalExpressionNodeMenuItems(getNodeSemantic() == NodeSemantic.NECESSARY_SET || "
                + "getNodeSemantic() == NodeSemantic.SUFFICIENT_SET);\n");
        b.append("then\n");
        b.append("   $addEditLogicalNode.addRoleWithRestrictionsAction(new ")
                .append(new ConceptProxy(roleTypeProperty.get()).toString())
                .append(", new ")
                .append(new ConceptProxy(this.assemblageForConstraintProperty.get()).toString())
                .append(");\n");

        b.append("end\n\n");

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
