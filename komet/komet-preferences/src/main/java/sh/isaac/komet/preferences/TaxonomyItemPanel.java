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
import java.util.prefs.BackingStoreException;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.SingleAssemblageSnapshot;
import sh.isaac.api.TaxonomySnapshot;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.api.tree.TaxonomyAmalgam;
import sh.isaac.api.tree.TaxonomySnapshotFromComponentNidAssemblage;
import static sh.isaac.komet.preferences.TaxonomyItemPanel.Keys.INCLUDE_DEFINING_TAXONOMY;
import static sh.isaac.komet.preferences.TaxonomyItemPanel.Keys.INVERSE_TREES;
import static sh.isaac.komet.preferences.TaxonomyItemPanel.Keys.ROOTS;
import static sh.isaac.komet.preferences.TaxonomyItemPanel.Keys.TREES;

import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.komet.gui.contract.preferences.TaxonomyItem;
import sh.komet.gui.control.PropertySheetBooleanWrapper;
import sh.komet.gui.control.PropertySheetItemObjectListWrapper;
import sh.komet.gui.control.PropertySheetTextWrapper;
import sh.komet.gui.control.concept.PropertySheetConceptListWrapper;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;
import sh.komet.gui.util.UuidStringKey;

/**
 *
 * @author kec
 */
public class TaxonomyItemPanel extends AbstractPreferences implements TaxonomyItem {
    // TreeAmalgam
    public enum Keys {
        ITEM_NAME,
        TREES,
        INVERSE_TREES,
        INCLUDE_DEFINING_TAXONOMY,
        ROOTS,
        MANIFOLD_COORDINATE_KEY
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

    private final SimpleObjectProperty<UuidStringKey> manifoldCoordinateKeyProperty = new SimpleObjectProperty<>(this, TermAux.MANIFOLD_COORDINATE_KEY.toExternalString());
    private final PropertySheetItemObjectListWrapper<UuidStringKey> manifoldCoordinateKeyWrapper;


    public TaxonomyItemPanel(IsaacPreferences preferencesNode, Manifold manifold,
            KometPreferencesController kpc) {
        super(preferencesNode,
                getGroupName(preferencesNode, "Taxonomy configuration"),
                manifold, kpc);
        nameProperty.set(groupNameProperty().get());
        nameProperty.addListener((observable, oldValue, newValue) -> {
            groupNameProperty().set(newValue);
        });
        getItemList().add(new PropertySheetTextWrapper(manifold, nameProperty));
        getItemList().add(new PropertySheetBooleanWrapper(manifold, includeDefiningTaxonomyProperty));

        this.manifoldCoordinateKeyWrapper = new PropertySheetItemObjectListWrapper("Manifold coordinate",
                manifoldCoordinateKeyProperty, FxGet.manifoldCoordinateKeys());
        getItemList().add(manifoldCoordinateKeyWrapper);

        getItemList().add(new PropertySheetConceptListWrapper(manifold, taxonomyRootListProperty));
        getItemList().add(new PropertySheetConceptListWrapper(manifold, treeListProperty));
        getItemList().add(new PropertySheetConceptListWrapper(manifold, inverseTreeListProperty));
        revertFields();
        save();

    }

    @Override
    final protected void saveFields() throws BackingStoreException {
        // Delete old amalgam
        Optional<String> oldItemName = getPreferencesNode().get(Keys.ITEM_NAME);
        if (oldItemName.isPresent()) {
            FxGet.removeTaxonomyConfiguration(oldItemName.get());
        }
        
        getPreferencesNode().put(Keys.ITEM_NAME, nameProperty.get());

        getPreferencesNode().putConceptList(ROOTS, taxonomyRootListProperty);
        getPreferencesNode().putConceptList(TREES, treeListProperty);
        getPreferencesNode().putConceptList(INVERSE_TREES, inverseTreeListProperty);

        getPreferencesNode().putBoolean(INCLUDE_DEFINING_TAXONOMY, this.includeDefiningTaxonomyProperty.get());

        if (this.manifoldCoordinateKeyProperty.getValue() != null) {
            TaxonomyAmalgam amalgam = new TaxonomyAmalgam(FxGet.manifoldCoordinates().get(this.manifoldCoordinateKeyProperty.getValue()),
                    this.includeDefiningTaxonomyProperty.get());
            // TODO add support for other types of assemblage...
            for (ConceptSpecification proxy: treeListProperty.get()) {
                SingleAssemblageSnapshot<ComponentNidVersion> treeAssemblage = Get.assemblageService().getSingleAssemblageSnapshot(proxy.getNid(), ComponentNidVersion.class, getManifold());
                TaxonomySnapshot taxonomySnapshot = new TaxonomySnapshotFromComponentNidAssemblage(treeAssemblage, getManifold());
                amalgam.getTaxonomies().add(taxonomySnapshot);
            }
            for (ConceptSpecification proxy: inverseTreeListProperty.get()) {
                SingleAssemblageSnapshot<ComponentNidVersion> treeAssemblage = Get.assemblageService().getSingleAssemblageSnapshot(proxy.getNid(), ComponentNidVersion.class, getManifold());
                TaxonomySnapshot taxonomySnapshot = new TaxonomySnapshotFromComponentNidAssemblage(treeAssemblage, getManifold());
                amalgam.getInverseTaxonomies().add(taxonomySnapshot);
            }
            for (ConceptSpecification proxy: taxonomyRootListProperty) {
                amalgam.getTaxonomyRoots().add(proxy);
            }
            FxGet.addTaxonomyConfiguration(nameProperty.get(), amalgam);
        }
    }

    @Override 
    final protected void revertFields() {
        this.nameProperty.set(getPreferencesNode().get(Keys.ITEM_NAME, getGroupName()));

        taxonomyRootListProperty.setAll(getPreferencesNode().getConceptList(ROOTS));
        inverseTreeListProperty.setAll(getPreferencesNode().getConceptList(INVERSE_TREES));
        treeListProperty.setAll(getPreferencesNode().getConceptList(TREES));

        this.includeDefiningTaxonomyProperty.set(getPreferencesNode().getBoolean(INCLUDE_DEFINING_TAXONOMY, true));
    }
    
    @Override
    public boolean showDelete() {
        return true;
    }
    
}
