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
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.komet.gui.control.PropertySheetTextWrapper;
import sh.komet.gui.control.concept.PropertySheetItemConceptConstraintWrapper;
import sh.komet.gui.control.concept.PropertySheetItemConceptWrapper;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;

/**
 *
 * @author kec
 */
public class ActionPanel extends AbstractPreferences {

    public enum Keys {
        ACTION_NAME,
        ASSEMBLAGE,
        SEMANTIC_FIELD_CONCEPTS
    };

    private final HashMap<ConceptSpecification, PropertySheet.Item> propertySheetItemMap = new HashMap<>();

    private final HashSet<ConceptSpecification> fieldConcepts = new HashSet<>();
    private final SimpleStringProperty actionName
            = new SimpleStringProperty(this, MetaData.ACTION_NAME____SOLOR.toExternalString());

    private final SimpleObjectProperty<ConceptSpecification> assemblageForAction
            = new SimpleObjectProperty(this, MetaData.ASSEMBLAGE_FOR_ACTION____SOLOR.toExternalString());

    private final SimpleObjectProperty<List<PropertySheet.Item>> itemListProperty = new SimpleObjectProperty<>(this, "item list property");
    {
        itemListProperty.addListener((observable, oldValue, newValue) -> {
            System.out.println("Item list changed: " + newValue);
        });
    }
    public ActionPanel(IsaacPreferences preferencesNode, Manifold manifold,
            KometPreferencesController kpc) {
        super(preferencesNode,
                preferencesNode.get(Keys.ACTION_NAME, "action " + preferencesNode.name()),
                manifold, kpc);
        revertFields();
        save();
        getItemList().add(new PropertySheetTextWrapper(manifold, actionName));
        PropertySheetItemConceptWrapper conceptWrapper = new PropertySheetItemConceptWrapper(manifold, assemblageForAction);
        getItemList().add(conceptWrapper);

        handleAssemblageChange(null, null, assemblageForAction.get());
        assemblageForAction.addListener(this::handleAssemblageChange);
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
        getPreferencesNode().put(Keys.ACTION_NAME, actionName.get());
        getPreferencesNode().putConceptSpecification(Keys.ASSEMBLAGE, assemblageForAction.get());
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

    }

    @Override
    final void revertFields() {
        this.actionName.set(getPreferencesNode().get(Keys.ACTION_NAME, getGroupName()));
        this.assemblageForAction.set(getPreferencesNode().getConceptSpecification(Keys.ASSEMBLAGE, TermAux.ASSEMBLAGE));
        this.fieldConcepts.clear();
        for (String externalString : getPreferencesNode().getList(Keys.SEMANTIC_FIELD_CONCEPTS)) {
            this.fieldConcepts.add(new ConceptProxy(externalString));
        }
        this.propertySheetItemMap.clear();
        this.itemListProperty.set(FxGet.constraintPropertyItemsForAssemblageSemantic(assemblageForAction.get(), getManifold()));

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

}
