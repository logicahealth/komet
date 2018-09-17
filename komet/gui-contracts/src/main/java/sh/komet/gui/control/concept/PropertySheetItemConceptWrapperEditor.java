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
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.PopOver;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.contract.ConceptSearchNodeFactory;
import sh.komet.gui.interfaces.ConceptExplorationNode;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;

/**
 *
 * @author kec
 */
public class PropertySheetItemConceptWrapperEditor implements PropertyEditor<PropertySheetItemConceptWrapper> {
    
    private PropertySheetItemConceptWrapper constraintWrapper;
    private PopOver popOver;
    private ReadOnlyObjectProperty<ConceptSpecification> findSelectedConceptSpecification;
   
    BorderPane editorPane = new BorderPane();
    AnchorPane anchorPane = new AnchorPane();
    Manifold manifold;
    ComboBox defaultConcept = new ComboBox();
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
        
        defaultConcept.setButtonCell(new ListCell<ConceptSpecification>() {
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
        
        defaultConcept.setCellFactory(c-> new ListCell<ConceptSpecification>() {
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
        
        AnchorPane.setTopAnchor(defaultConcept, 0.);
        AnchorPane.setRightAnchor(defaultConcept, 0.);
        AnchorPane.setBottomAnchor(defaultConcept, 0.);
        AnchorPane.setLeftAnchor(defaultConcept, 0.);
        anchorPane.getChildren().add(defaultConcept);
        editorPane.setTop(anchorPane);
        editorPane.setCenter(conceptListView);
        
        defaultConcept.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                getValue().setValue(newValue);
            } else {
                getValue().setValue(TermAux.UNINITIALIZED_COMPONENT_ID);
            }
        });
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

    public PropertySheetItemConceptWrapperEditor(Manifold manifold) {
        this.manifold = manifold;
    }

    @Override
    public Node getEditor() {
        return editorPane;
    }

    @Override
    public PropertySheetItemConceptWrapper getValue() {
        return constraintWrapper;
    }

    @Override
    public void setValue(PropertySheetItemConceptWrapper value) {
        this.constraintWrapper = value;
        conceptListView.setItems(this.constraintWrapper.getAllowedValues());
        defaultConcept.setItems(this.constraintWrapper.getAllowedValues());
        this.constraintWrapper.getObservableValue().get().addListener((observable, oldValue, newValue) -> {
            defaultConcept.getSelectionModel().select(this.constraintWrapper.getValue());
        });
        defaultConcept.getSelectionModel().select(this.constraintWrapper.getValue());
    }
    private void showFindPopup(ActionEvent event) {
        this.popOver = new PopOver();
        this.popOver.getRoot().getStylesheets().add(FxGet.fxConfiguration().getUserCSSURL().toString());
        this.popOver.getRoot().getStylesheets().add(Iconography.getStyleSheetStringUrl());
        this.popOver.setCloseButtonEnabled(true);
        this.popOver.setHeaderAlwaysVisible(false);
        this.popOver.setTitle("");
        this.popOver.setArrowLocation(PopOver.ArrowLocation.LEFT_TOP);
        ConceptSearchNodeFactory searchNodeFactory = Get.service(ConceptSearchNodeFactory.class);
        ConceptExplorationNode searchExplorationNode = searchNodeFactory.createNode(manifold);
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
        Object selectedItem = defaultConcept.getSelectionModel().getSelectedItem();
        if (selectedIndex > 0) {
            ConceptSpecification specToMove = this.conceptListView.getItems().remove(selectedIndex);
            this.conceptListView.getItems().add(selectedIndex-1, specToMove);
            this.conceptListView.getSelectionModel().select(selectedIndex-1);
        }
        defaultConcept.getSelectionModel().select(selectedItem);
    }
    private void moveDownSelection(ActionEvent event) {
        int selectedIndex = this.conceptListView.getSelectionModel().getSelectedIndex();
        Object selectedItem = defaultConcept.getSelectionModel().getSelectedItem();
        if (selectedIndex > -1 && selectedIndex < this.conceptListView.getItems().size() - 1) {
            ConceptSpecification specToMove = this.conceptListView.getItems().remove(selectedIndex);
            this.conceptListView.getItems().add(selectedIndex+1, specToMove);
            this.conceptListView.getSelectionModel().select(selectedIndex+1);
        }
        defaultConcept.getSelectionModel().select(selectedItem);
    }
}
