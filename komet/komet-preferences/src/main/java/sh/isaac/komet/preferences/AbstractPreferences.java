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

import java.util.List;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.preferences.IsaacPreferences;
import static sh.isaac.komet.preferences.PreferenceGroup.Keys.INITIALIZED;
import static sh.isaac.komet.preferences.PreferencesTreeItem.Properties.CHILDREN_NODES;
import static sh.isaac.komet.preferences.PreferencesTreeItem.Properties.PROPERTY_SHEET_CLASS;
import sh.komet.gui.control.property.PropertyEditorFactory;
import sh.komet.gui.control.property.PropertySheetItem;
import sh.komet.gui.control.property.PropertySheetPurpose;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public abstract class AbstractPreferences implements PreferenceGroup  {
    protected static final Logger LOG = LogManager.getLogger();
   
    protected final IsaacPreferences preferencesNode;
    private final BooleanProperty initialized = new SimpleBooleanProperty(this, INITIALIZED.toString());
    private final BooleanProperty changed = new SimpleBooleanProperty(this, "changed", false);
    private final SimpleStringProperty groupNameProperty = new SimpleStringProperty(this, "group name");
    private final ObservableList<PropertySheet.Item> itemList = FXCollections.observableArrayList(); 
    {
        itemList.addListener((ListChangeListener.Change<? extends PropertySheet.Item> c) -> {
            makePropertySheet();
        });
    }
    private final Manifold manifold;
    protected final KometPreferencesController kpc;
    protected PreferencesTreeItem treeItem;
    private final Button revertButton = new Button("Revert");
    private final Button saveButton = new Button("Save");
    private final BorderPane propertySheetBorderPane = new BorderPane();
    private PropertySheet propertySheet;
    {
        revertButton.setOnAction((event) -> {
            revert();
            changed.setValue(Boolean.FALSE);
        });
        revertButton.setDisable(changed.get());
        saveButton.setOnAction((event) -> {
            save();
            changed.setValue(Boolean.FALSE);
        });
        saveButton.setDisable(changed.get());
        changed.addListener((observable, oldValue, newValue) -> {
            if (newValue == true) {
                revertButton.setDisable(false);
                saveButton.setDisable(false);
            } else {
                revertButton.setDisable(true);
                saveButton.setDisable(true);
            }
        });
    } 
    ToolBar bottomBar = new ToolBar(revertButton, saveButton);

    public AbstractPreferences(IsaacPreferences preferencesNode, String groupName, Manifold manifold, 
            KometPreferencesController kpc) {
        this.preferencesNode = preferencesNode;
        this.initialized.setValue(preferencesNode.getBoolean(INITIALIZED, false));
        this.groupNameProperty.set(groupName);
        this.manifold = manifold;
        this.kpc = kpc;
    }

    @Override
    public PreferencesTreeItem getTreeItem() {
        return treeItem;
    }

    @Override
    public void setTreeItem(PreferencesTreeItem treeItem) {
        this.treeItem = treeItem;
        addChildren();
    }
    
    protected void addChildren() {
        // Override if node adds children to tree. 
    }

    public IsaacPreferences getPreferencesNode() {
        return preferencesNode;
    }
    
    @Override
    public final void save() {
        try {
            initialized.set(true);
            preferencesNode.putBoolean(INITIALIZED, initialized.get());
            saveFields();
            preferencesNode.sync();
        } catch (BackingStoreException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    protected final void addChild(String childName, Class<? extends AbstractPreferences> childPreferencesClass) {
        IsaacPreferences childNode = this.preferencesNode.node(childName);
        childNode.put(PROPERTY_SHEET_CLASS, childPreferencesClass.getName());
        List<String> childPreferences = this.preferencesNode.getList(CHILDREN_NODES);
        if (!childPreferences.contains(childName)) {
            childPreferences.add(childName);
        }
        
        this.preferencesNode.putList(CHILDREN_NODES, childPreferences);
    }


    @Override
    public final String getGroupName() {
        return groupNameProperty.get();
    }
    
    @Override
    public final SimpleStringProperty groupNameProperty() {
        return groupNameProperty;
    }
    

    public final void setGroupName(String groupName) {
        this.groupNameProperty.set(groupName);
    }

    @Override
    public final boolean initialized() {
        return initialized.get();
    }
    

    @Override
    public final void revert() {
        initialized.setValue(preferencesNode.getBoolean(INITIALIZED, false));
        revertFields();
    }    
    abstract void saveFields() throws BackingStoreException;
    abstract void revertFields();

    @Override
    public String toString() {
        return getGroupName();
    }
    
    public final List<PropertySheet.Item> getItemList() {
        return itemList;
    }
    
    protected void makePropertySheet() {
        PropertySheet sheet = new PropertySheet();
        sheet.setMode(PropertySheet.Mode.NAME);
        sheet.setSearchBoxVisible(false);
        sheet.setModeSwitcherVisible(false);
        sheet.setPropertyEditorFactory(new PropertyEditorFactory(manifold));
        sheet.getItems().addAll(itemList);
        for (PropertySheet.Item item: itemList) {
            Optional<ObservableValue<? extends Object>> observable = item.getObservableValue();
            if (observable.isPresent()) {
                observable.get().addListener((obs, oldValue, newValue) -> {
                    changed.set(true);
                });
            }
        }
        this.propertySheetBorderPane.setCenter(sheet);
    }
    
    
    @Override
    public final Node getCenterPanel(Manifold manifold) {
        if (this.propertySheet == null) {
            makePropertySheet();
        }
        return this.propertySheetBorderPane;
    }
    
    protected final void addProperty(ObservableValue<?> observableValue) {
        changed.set(true);
        observableValue.addListener(new WeakChangeListener<>((observable, oldValue, newValue) -> {
            changed.set(true);
        }));      
    }
    protected final void addProperty(ObservableList<? extends Object> observableList) {
        changed.set(true);
        observableList.addListener(new WeakListChangeListener<Object>((ListChangeListener.Change<? extends Object> c) -> {
            changed.set(true);
        }));
    }
    
    
    protected PropertySheetItem createPropertyItem(Property<?> property) {
        PropertySheetItem wrappedProperty = new PropertySheetItem(property.getValue(), property, manifold, PropertySheetPurpose.DESCRIPTION_DIALECT);
        return wrappedProperty;
    }

    @Override
    public Node getTopPanel(Manifold manifold) {
        return null;
    }

    @Override
    public Node getLeftPanel(Manifold manifold) {
        return null;
    }

   
    @Override
    public final Node getBottomPanel(Manifold manifold) {
        return this.bottomBar;
    }

    @Override
    public Node getRightPanel(Manifold manifold) {
        return null;
    }

    public Manifold getManifold() {
        return manifold;
    }
 
}
