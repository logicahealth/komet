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
package sh.komet.gui.control.concept;

import java.util.HashMap;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class AssemblageListEditor implements PropertyEditor<ObservableList<ConceptSpecification>> {
    
    private ObservableList<ConceptSpecification> value;
   
    BorderPane editorPane = new BorderPane();
    AnchorPane anchorPane = new AnchorPane();
    Manifold manifold;
    ListView<ConceptSpecification> conceptListView = new ListView<>();
    {
        conceptListView.setPrefHeight(152);
        conceptListView.setMinHeight(152);
        conceptListView.setMaxHeight(152);
        conceptListView.setCellFactory(c-> new ListCell<ConceptSpecification>() {
            @Override
            protected void updateItem(ConceptSpecification item, boolean empty) {
                super.updateItem(item, empty); 
                if (!empty) {
                    this.setText(manifold.getPreferredDescriptionText(item));
                } else {
                    this.setText("");
                }
            }
            
        });
        
        editorPane.setTop(anchorPane);
        editorPane.setCenter(conceptListView);
    }
    
    MenuButton forMenuButton = new MenuButton("");
    Button upButton = new Button("", Iconography.ARROW_UP.getIconographic());
    Button downButton = new Button("", Iconography.ARROW_DOWN.getIconographic());
    {
        upButton.setContentDisplay(ContentDisplay.RIGHT);
        downButton.setContentDisplay(ContentDisplay.RIGHT);
        upButton.setOnAction(this::moveUpSelection);
        downButton.setOnAction(this::moveDownSelection);
    }
    Button deleteButton = new Button("", Iconography.DELETE_TRASHCAN.getIconographic());
    private final ToolBar listToolbar = new ToolBar(forMenuButton, upButton, downButton, deleteButton);
    {
        listToolbar.setOrientation(Orientation.VERTICAL);
        editorPane.setRight(listToolbar);
        deleteButton.setOnAction(this::deleteSelection);
    }

    public AssemblageListEditor(Manifold manifold) {
        this.manifold = manifold;
        setupForMenu();
    }

    @Override
    public Node getEditor() {
        return editorPane;
    }

    @Override
    public ObservableList<ConceptSpecification> getValue() {
        return value;
    }

    @Override
    public void setValue(ObservableList<ConceptSpecification> value) {
        this.value = value;
        conceptListView.setItems(value);
    }
    
    private void deleteSelection(ActionEvent event) {
        int selectedIndex = this.conceptListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex > -1) {
            this.conceptListView.getItems().remove(selectedIndex);
        }
    }
    private void moveUpSelection(ActionEvent event) {
        int selectedIndex = this.conceptListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex > 0) {
            ConceptSpecification specToMove = this.conceptListView.getItems().remove(selectedIndex);
            this.conceptListView.getItems().add(selectedIndex-1, specToMove);
            this.conceptListView.getSelectionModel().select(selectedIndex-1);
        }
    }
    private void moveDownSelection(ActionEvent event) {
        int selectedIndex = this.conceptListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex > -1 && selectedIndex < this.conceptListView.getItems().size() - 1) {
            ConceptSpecification specToMove = this.conceptListView.getItems().remove(selectedIndex);
            this.conceptListView.getItems().add(selectedIndex+1, specToMove);
            this.conceptListView.getSelectionModel().select(selectedIndex+1);
        }
    }
    
    protected final void setupForMenu() {
        forMenuButton.getItems().clear();
        Menu favorites = new Menu("favorites");
        forMenuButton.getItems().add(favorites);
        favorites.getItems().add(makeMenuFromAssemblageNid(MetaData.SOLOR_CONCEPT____SOLOR.getNid()));
        favorites.getItems().add(makeMenuFromAssemblageNid(MetaData.ENGLISH_LANGUAGE____SOLOR.getNid()));
        
        
        Menu byType = new Menu("by type");
        forMenuButton.getItems().add(byType);
        
        HashMap<VersionType, Menu> versionTypeMenuMap = new HashMap();
        for (VersionType versionType : VersionType.values()) {
            Menu versionTypeMenu = new Menu(versionType.toString());
            versionTypeMenuMap.put(versionType, versionTypeMenu);
            byType.getItems().add(versionTypeMenu);
        }
        int[] assembalgeNids = Get.assemblageService().getAssemblageConceptNids();
        
        Menu byName = new Menu("by name");
        forMenuButton.getItems().add(byName);
        
        for (int assemblageNid : Get.assemblageService().getAssemblageConceptNids()) {
            MenuItem menu = makeMenuFromAssemblageNid(assemblageNid);
            byName.getItems().add(menu);
            
            MenuItem menu2 = makeMenuFromAssemblageNid(assemblageNid);
            VersionType versionType = Get.assemblageService().getVersionTypeForAssemblage(assemblageNid);
            versionTypeMenuMap.get(versionType).getItems().add(menu2);
        }
        byName.getItems().sort((o1, o2) -> {
            return o1.getText().compareTo(o2.getText());
        });
        for (Menu menu: versionTypeMenuMap.values()) {
            menu.getItems().sort((o1, o2) -> {
                return o1.getText().compareTo(o2.getText()); 
            });
        }
    }

    protected MenuItem makeMenuFromAssemblageNid(int assemblageNid) {
        MenuItem menu = new MenuItem(manifold.getPreferredDescriptionText(assemblageNid));
        menu.setOnAction((event) -> {
            this.conceptListView.getItems().add(Get.conceptSpecification(assemblageNid));
        });
        return menu;
    }    
}