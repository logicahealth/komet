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

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.PopOver;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.komet.iconography.IconographyHelper;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;
import sh.komet.gui.contract.ConceptSearchNodeFactory;
import sh.komet.gui.interfaces.ConceptExplorationNode;

/**
 *
 * @author kec
 */
public class ConceptListEditor implements PropertyEditor<ObservableList<ConceptSpecification>> {
    
    private PopOver popOver;
    private ObservableList<ConceptSpecification> value;
    private ReadOnlyObjectProperty<ConceptSpecification> findSelectedConceptSpecification;
   
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
    
    Button findButton = new Button("", Iconography.SIMPLE_SEARCH.getIconographic());
    Button upButton = new Button("", Iconography.ARROW_UP.getIconographic());
    Button downButton = new Button("", Iconography.ARROW_DOWN.getIconographic());
    {
        findButton.setOnAction(this::showFindPopup);
        upButton.setContentDisplay(ContentDisplay.RIGHT);
        downButton.setContentDisplay(ContentDisplay.RIGHT);
        upButton.setOnAction(this::moveUpSelection);
        downButton.setOnAction(this::moveDownSelection);
    }
    Button deleteButton = new Button("", Iconography.DELETE_TRASHCAN.getIconographic());
    private final ToolBar listToolbar = new ToolBar(findButton, upButton, downButton, deleteButton);
    {
        listToolbar.setOrientation(Orientation.VERTICAL);
        editorPane.setRight(listToolbar);
        deleteButton.setOnAction(this::deleteSelection);
    }

    public ConceptListEditor(Manifold manifold) {
        this.manifold = manifold;
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
    private void showFindPopup(ActionEvent event) {
        this.popOver = new PopOver();
        this.popOver.getRoot().getStylesheets().add(FxGet.fxConfiguration().getUserCSSURL().toString());
        this.popOver.getRoot().getStylesheets().add(IconographyHelper.getStyleSheetStringUrl());
        this.popOver.setCloseButtonEnabled(true);
        this.popOver.setHeaderAlwaysVisible(false);
        this.popOver.setTitle("");
        this.popOver.setArrowLocation(PopOver.ArrowLocation.LEFT_TOP);
        ConceptSearchNodeFactory searchNodeFactory = Get.service(ConceptSearchNodeFactory.class);
        ConceptExplorationNode searchExplorationNode = searchNodeFactory.createNode(manifold, null);
        Node searchNode = searchExplorationNode.getNode();
        this.findSelectedConceptSpecification = searchExplorationNode.selectedConceptSpecification();
        BorderPane searchBorder = new BorderPane(searchNode);
        Button addSelection = new Button("add");
        addSelection.setOnAction(this::setToFindSelection);
        ToolBar popOverToolbar = new ToolBar(addSelection);
        searchBorder.setTop(popOverToolbar);
        searchBorder.setPrefSize(500, 400);
        searchBorder.setMinSize(500, 400);
        this.popOver.setContentNode(searchBorder);
        this.popOver.show(findButton);
        searchExplorationNode.focusOnInput();
    }
    
    private void setToFindSelection(ActionEvent event) {
        if (this.findSelectedConceptSpecification.get() != null) {
            this.conceptListView.getItems().add(this.findSelectedConceptSpecification.get());
        }
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
}