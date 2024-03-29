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

import java.util.Optional;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;

import static sh.isaac.komet.preferences.GraphConfigurationItemPanel.Keys.INCLUDE_DEFINING_TAXONOMY;
import static sh.isaac.komet.preferences.GraphConfigurationItemPanel.Keys.INVERSE_TREES;
import static sh.isaac.komet.preferences.GraphConfigurationItemPanel.Keys.ROOTS;
import static sh.isaac.komet.preferences.GraphConfigurationItemPanel.Keys.TREES;

import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.komet.gui.contract.preferences.GraphConfigurationItem;
import sh.komet.gui.control.property.wrapper.PropertySheetBooleanWrapper;
import sh.komet.gui.control.property.wrapper.PropertySheetTextWrapper;
import sh.komet.gui.control.concept.PropertySheetConceptListWrapper;
import sh.komet.gui.control.property.ViewProperties;
import sh.isaac.api.util.UuidStringKey;

/**
 *
 * @author kec
 */
public class GraphConfigurationItemPanel extends AbstractPreferences implements GraphConfigurationItem {
    // TreeAmalgam
    public enum Keys {
        ITEM_NAME,
        TREES,
        INVERSE_TREES,
        INCLUDE_DEFINING_TAXONOMY,
        ROOTS
    };

    private final SimpleStringProperty nameProperty
            = new SimpleStringProperty(this, MetaData.TAXONOMY_CONFIGURATION_NAME____SOLOR.toExternalString());
    private final SimpleListProperty<ConceptSpecification> taxonomyRootListProperty = 
            new SimpleListProperty<>(this, MetaData.TAXONOMY_CONFIGURATION_ROOTS____SOLOR.toExternalString(), FXCollections.observableArrayList());
    
    private final SimpleListProperty<ConceptSpecification> treeListProperty = 
            new SimpleListProperty<>(this, MetaData.TREE_LIST____SOLOR.toExternalString(), FXCollections.observableArrayList());
    private final SimpleListProperty<ConceptSpecification> inverseTreeListProperty = 
            new SimpleListProperty<>(this, MetaData.INVERSE_TREE_LIST____SOLOR.toExternalString(), FXCollections.observableArrayList());
    private final SimpleBooleanProperty includeDefiningTaxonomyProperty = 
            new SimpleBooleanProperty(this, MetaData.INCLUDE_DEFINING_TAXONOMY____SOLOR.toExternalString());

    private final UuidStringKey graphConfigurationKey;


    public GraphConfigurationItemPanel(IsaacPreferences preferencesNode, ViewProperties viewProperties,
                                       KometPreferencesController kpc) {
        super(preferencesNode,
                getGroupName(preferencesNode, "View configuration"),
                viewProperties, kpc);
        nameProperty.set(groupNameProperty().get());
        this.graphConfigurationKey = new UuidStringKey(UUID.fromString(preferencesNode.name()), nameProperty.getValue());
        nameProperty.addListener((observable, oldValue, newValue) -> {
            groupNameProperty().set(newValue);
            this.graphConfigurationKey.updateString(newValue);
        });

        getItemList().add(new PropertySheetTextWrapper(viewProperties.getManifoldCoordinate(), nameProperty));
        getItemList().add(new PropertySheetBooleanWrapper(viewProperties.getManifoldCoordinate(), includeDefiningTaxonomyProperty));

        getItemList().add(new PropertySheetConceptListWrapper(viewProperties.getManifoldCoordinate(), taxonomyRootListProperty));
        getItemList().add(new PropertySheetConceptListWrapper(viewProperties.getManifoldCoordinate(), treeListProperty));
        getItemList().add(new PropertySheetConceptListWrapper(viewProperties.getManifoldCoordinate(), inverseTreeListProperty));
        revertFields();
        save();

    }

    @Override
    final protected void saveFields() throws BackingStoreException {
        // Delete old amalgam
        Optional<String> oldItemName = getPreferencesNode().get(Keys.ITEM_NAME);

        getPreferencesNode().put(Keys.ITEM_NAME, nameProperty.get());

        getPreferencesNode().putConceptList(ROOTS, taxonomyRootListProperty);
        getPreferencesNode().putConceptList(TREES, treeListProperty);
        getPreferencesNode().putConceptList(INVERSE_TREES, inverseTreeListProperty);

        getPreferencesNode().putBoolean(INCLUDE_DEFINING_TAXONOMY, this.includeDefiningTaxonomyProperty.get());

    }

    @Override 
    final protected void revertFields() {
        this.nameProperty.set(getPreferencesNode().get(Keys.ITEM_NAME, getGroupName()));

        this.taxonomyRootListProperty.setAll(getPreferencesNode().getConceptList(ROOTS));
        this.inverseTreeListProperty.setAll(getPreferencesNode().getConceptList(INVERSE_TREES));
        this.treeListProperty.setAll(getPreferencesNode().getConceptList(TREES));

        this.includeDefiningTaxonomyProperty.set(getPreferencesNode().getBoolean(INCLUDE_DEFINING_TAXONOMY, true));
    }
    
    @Override
    public boolean showDelete() {
        return true;
    }
    
}
