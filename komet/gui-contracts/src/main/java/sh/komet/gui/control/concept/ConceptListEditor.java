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
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.PopOver;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;
import sh.komet.gui.contract.ConceptSearchNodeFactory;
import sh.komet.gui.interfaces.ConceptExplorationNode;

/**
 *
 * @author kec
 */
public class ConceptListEditor implements PropertyEditor<ObservableList<ConceptSpecification>> {

    Button addButton = new Button("", Iconography.ADD.getIconographic()); {
        addButton.setOnAction(this::addToList);
    }
    ToolBar toolbar = new ToolBar(addButton);
    ListView<ConceptSpecification> conceptListView = new ListView<>();
    {
        conceptListView.setCellFactory(c-> new ListCell<ConceptSpecification>() {
            @Override
            protected void updateItem(ConceptSpecification item, boolean empty) {
                super.updateItem(item, empty); 
                if (!empty) {
                    this.setText(manifold.getPreferredDescriptionText(item));
                }
            }
            
        });
    }
    BorderPane editorPane = new BorderPane(conceptListView);
    {
        editorPane.setTop(toolbar);
    }
    private final Manifold manifold;
    private ObservableList<ConceptSpecification> conceptList;
    private ReadOnlyObjectProperty<ConceptSpecification> selectedConceptSpecification;

    public ConceptListEditor(Manifold manifold, ObservableList<ConceptSpecification>  conceptListProperty) {
        this.manifold = manifold;
        this.conceptList = conceptListProperty;
        this.conceptListView.setItems(conceptListProperty);
    }
    
    @Override
    public Node getEditor() {
        return editorPane;
    }

    @Override
    public ObservableList<ConceptSpecification> getValue() {
        return this.conceptList;
    }

    @Override
    public void setValue(ObservableList<ConceptSpecification> value) {
        this.conceptList = value;
    }
    
    private void addToList(ActionEvent event) {
        PopOver popOver = new PopOver();
        popOver.getRoot().getStylesheets().add(FxGet.fxConfiguration().getUserCSSURL().toString());
        popOver.getRoot().getStylesheets().add(Iconography.getStyleSheetStringUrl());
        popOver.setCloseButtonEnabled(true);
        popOver.setHeaderAlwaysVisible(false);
        popOver.setTitle("");
        popOver.setArrowLocation(PopOver.ArrowLocation.LEFT_TOP);
        ConceptSearchNodeFactory searchNodeFactory = Get.service(ConceptSearchNodeFactory.class);
        ConceptExplorationNode searchExplorationNode = searchNodeFactory.createNode(manifold);
        Node searchNode = searchExplorationNode.getNode();
        this.selectedConceptSpecification = searchExplorationNode.selectedConceptSpecification();
        BorderPane searchBorder = new BorderPane(searchNode);
        Button addSelection = new Button("add");
        addSelection.setOnAction(this::addSelectionToList);
        ToolBar popOverToolbar = new ToolBar(addSelection);
        searchBorder.setTop(popOverToolbar);
        searchBorder.setPrefSize(500, 400);
        searchBorder.setMinSize(500, 400);
        popOver.setContentNode(searchBorder);
        popOver.show(addButton);
    }
    
    private void addSelectionToList(ActionEvent event) {
        conceptListView.getItems().add(this.selectedConceptSpecification.get());
    }
    
}
