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
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.PopOver;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.komet.iconography.IconographyHelper;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedListProperty;
import sh.komet.gui.contract.preferences.WindowPreferences;
import sh.komet.gui.style.StyleClasses;
import sh.komet.gui.util.FxGet;
import sh.komet.gui.contract.ConceptSearchNodeFactory;
import sh.komet.gui.interfaces.ConceptExplorationNode;

/**
 *
 * @author kec
 */
public class ConceptListEditor implements PropertyEditor<ObservableList<ConceptSpecification>> {
    // TODO add ability to optionally prevent duplicates in the list.
    private PopOver popOver;
    private ObservableList<ConceptSpecification> listOfValues;
    private ReadOnlyObjectProperty<ConceptSpecification> findSelectedConceptSpecification;
    private SimpleEqualityBasedListProperty<ConceptSpecification> allowedValuesListProperty;
    private boolean allowDuplicates = false;


    BorderPane editorPane = new BorderPane();
    AnchorPane anchorPane = new AnchorPane();
    ManifoldCoordinate manifoldCoordinate;
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
                    this.setText(manifoldCoordinate.getPreferredDescriptionText(item));
                } else {
                    this.setText("");
                }
            }
            
        });
        
        editorPane.setTop(anchorPane);
        editorPane.setCenter(conceptListView);
    }

    private Button findButton = new Button("", Iconography.SIMPLE_SEARCH.getIconographic());
    private Button upButton = new Button("", Iconography.ARROW_UP.getIconographic());
    private Button downButton = new Button("", Iconography.ARROW_DOWN.getIconographic());
    private Button allowedValuesChoice = new Button("", Iconography.ADD.getIconographic());
    private Button deleteButton = new Button("", Iconography.DELETE_TRASHCAN.getIconographic());
    private ContextMenu contextMenu = new ContextMenu();
    {
        findButton.setOnAction(this::showFindPopup);
        upButton.setContentDisplay(ContentDisplay.RIGHT);
        downButton.setContentDisplay(ContentDisplay.RIGHT);
        allowedValuesChoice.setContentDisplay(ContentDisplay.RIGHT);
        upButton.setOnAction(this::moveUpSelection);
        downButton.setOnAction(this::moveDownSelection);
        allowedValuesChoice.setOnAction(this::showAllowedValues);

    }
    private final ToolBar listToolbar = new ToolBar(findButton, upButton, downButton, deleteButton);
    {
        listToolbar.getStyleClass().add(StyleClasses.CONCEPT_LIST_EDITOR_TOOLBAR.toString());
        listToolbar.setOrientation(Orientation.VERTICAL);
        editorPane.setRight(listToolbar);
        deleteButton.setOnAction(this::deleteSelection);
    }

    public ConceptListEditor(ManifoldCoordinate manifoldCoordinate) {
        this.manifoldCoordinate = manifoldCoordinate;
    }

    @Override
    public Node getEditor() {
        return editorPane;
    }

    @Override
    public ObservableList<ConceptSpecification> getValue() {
        return listOfValues;
    }

    @Override
    public void setValue(ObservableList<ConceptSpecification> value) {
        this.listOfValues = value;
        conceptListView.setItems(value);
    }

    private void showAllowedValues(ActionEvent event) {
        contextMenu.getItems().forEach(menuItem -> {
            ConceptSpecification possibleValue = (ConceptSpecification) menuItem.getUserData();
            if (allowDuplicates) {
                menuItem.setDisable(false);
            } else {
                menuItem.setDisable(this.listOfValues.contains(possibleValue));
            }
        });
        contextMenu.show(this.allowedValuesChoice, FxGet.getMouseLocation().getX(), FxGet.getMouseLocation().getY());
        event.consume();
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
            WindowPreferences windowPreferences = FxGet.windowPreferences(this.editorPane);


            ConceptExplorationNode searchExplorationNode = searchNodeFactory.createNode(windowPreferences.getViewPropertiesForWindow(),
                    windowPreferences.getViewPropertiesForWindow().getUnlinkedActivityFeed(), null);
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
            event.consume();
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

    public void setAllowedValues(SimpleEqualityBasedListProperty<ConceptSpecification> allowedValuesListProperty) {
        this.allowedValuesListProperty = allowedValuesListProperty;
        for (ConceptSpecification value: allowedValuesListProperty) {
            MenuItem menuItem = new MenuItem(this.manifoldCoordinate.getPreferredDescriptionText(value));
            menuItem.setUserData(value);
            menuItem.setOnAction(event -> {
                this.listOfValues.add(value);
            });
            this.contextMenu.getItems().add(menuItem);
        }
        allowedValuesChoice.setContextMenu(this.contextMenu);
        allowedValuesChoice.setOnAction(this::showAllowedValues);
        listToolbar.getItems().clear();
        listToolbar.getItems().setAll(allowedValuesChoice, upButton, downButton, deleteButton);
    }

    public boolean allowDuplicates() {
        return allowDuplicates;
    }

    public void setAllowDuplicates(boolean allowDuplicates) {
        this.allowDuplicates = allowDuplicates;
    }
}