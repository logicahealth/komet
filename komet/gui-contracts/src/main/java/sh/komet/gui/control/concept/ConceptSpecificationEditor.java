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

import java.util.Collection;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;
import org.controlsfx.control.PopOver;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.contract.ConceptSearchNodeFactory;
import sh.komet.gui.interfaces.ConceptExplorationNode;
import sh.komet.gui.manifold.HistoryRecord;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;

/**
 *
 * @author kec
 */
public class ConceptSpecificationEditor implements PropertyEditor<ConceptSpecification> {

    private final SimpleObjectProperty<ConceptSpecification> conceptSpecificationValue;
    private final MenuButton menuButton = new MenuButton();
    private final Manifold manifold;
    private ReadOnlyObjectProperty<ConceptSpecification> findSelectedConceptSpecification;
    private PopOver popOver;
    private final MenuItem findItem = new MenuItem("Find");
    FixedWidthMenuSeperator fixedWidthFindSeperator = new FixedWidthMenuSeperator();
    FixedWidthMenuSeperator fixedWidthManifoldSeperator = new FixedWidthMenuSeperator();

    public ConceptSpecificationEditor(PropertySheetItemConceptWrapper wrapper, Manifold manifold) {
        this.manifold = manifold;
        this.conceptSpecificationValue = (SimpleObjectProperty<ConceptSpecification>) wrapper.getObservableValue().get();
        this.conceptSpecificationValue.addListener(this::setButtonText);
        if (wrapper.getValue() != null) {
            this.menuButton.setText(manifold.getPreferredDescriptionText(wrapper.getValue()));
        } else {
            this.menuButton.setText("Empty");
        }
        
        
        this.menuButton.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                System.out.println("Adding meta f to " + menuButton.getText());
                findItem.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.META_DOWN));
                menuButton.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.F, KeyCombination.META_DOWN), () -> {
                    showFindPopup(null);
                });
            } else {
                findItem.setAccelerator(null);
            }
        });
        this.menuButton.widthProperty().addListener((observable, oldValue, newValue) -> {
            fixedWidthFindSeperator.setWidth(newValue.doubleValue() - 16);
            fixedWidthManifoldSeperator.setWidth(newValue.doubleValue() - 16);
        });
        this.menuButton.armedProperty().addListener((observable, oldValue, armed) -> {
            if (armed) {
                addMenuItems(wrapper, manifold);
            }
        });
    }

    protected final void addMenuItems(PropertySheetItemConceptWrapper wrapper, Manifold manifold1) {
        this.menuButton.getItems().clear();
        for (ConceptSpecification allowedValue : wrapper.getAllowedValues()) {
            ConceptMenuItem menuItem = new ConceptMenuItem(allowedValue, manifold1);
            menuItem.setOnAction(this::handleAction);
            this.menuButton.getItems().add(menuItem);
        }
        this.menuButton.getItems().add(fixedWidthFindSeperator);
        if (wrapper.allowSearch()) {
            findItem.setOnAction(this::showFindPopup);
            this.menuButton.getItems().add(findItem);
        }
        if (wrapper.allowHistory()) {
            this.menuButton.getItems().add(fixedWidthManifoldSeperator);
            for (String manifoldGroup : Manifold.getGroupNames()) {
                Collection<HistoryRecord> groupHistory = Manifold.getGroupHistory(manifoldGroup);
                if (!groupHistory.isEmpty()) {
                    Menu manifoldHistory = new Menu(manifoldGroup);
                    this.menuButton.getItems().add(manifoldHistory);
                    for (HistoryRecord record : groupHistory) {
                        ConceptMenuItem conceptItem = new ConceptMenuItem(Get.conceptSpecification(record.getComponentId()),
                                this.manifold);
                        conceptItem.setOnAction(this::handleAction);
                        manifoldHistory.getItems().add(conceptItem);
                    }
                }
            }
        }
    }
    
    @Override
    public Node getEditor() {
        return menuButton;
    }

    @Override
    public ConceptSpecification getValue() {
        return this.conceptSpecificationValue.get();
    }

    @Override
    public void setValue(ConceptSpecification value) {
        this.conceptSpecificationValue.set(value);
    }

    private void handleAction(ActionEvent event) {
        if (event.getSource() instanceof ConceptMenuItem) {
            ConceptMenuItem menuItem = (ConceptMenuItem) event.getSource();
            this.conceptSpecificationValue.set(menuItem.getSpec());
            this.menuButton.setText(manifold.getPreferredDescriptionText(menuItem));
        }
    }
    
    private void setButtonText(ObservableValue<? extends ConceptSpecification> observable, ConceptSpecification oldValue, ConceptSpecification newValue) {
        menuButton.setText(manifold.getPreferredDescriptionText(conceptSpecificationValue.get()));
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
        Manifold manifoldClone = manifold.deepClone();
        manifoldClone.setGroupName(Manifold.ManifoldGroup.UNLINKED.getGroupName());
        ConceptExplorationNode searchExplorationNode = searchNodeFactory.createNode(manifoldClone);
        Node searchNode = searchExplorationNode.getNode();
        this.findSelectedConceptSpecification = searchExplorationNode.selectedConceptSpecification();
        BorderPane searchBorder = new BorderPane(searchNode);
        Button addSelection = new Button("set");
        addSelection.setOnAction(this::setToFindSelection);
        ToolBar popOverToolbar = new ToolBar(addSelection);
        searchBorder.setTop(popOverToolbar);
        searchBorder.setPrefSize(500, 400);
        searchBorder.setMinSize(500, 400);
        this.popOver.setContentNode(searchBorder);
        this.popOver.show(menuButton);
        searchExplorationNode.focusOnInput();
    }
    
    private void setToFindSelection(ActionEvent event) {
        if (this.popOver != null) {
            this.popOver.hide(Duration.ZERO);
        }
        if (this.findSelectedConceptSpecification.get() != null) {
            this.conceptSpecificationValue.set(this.findSelectedConceptSpecification.get());
        }
    }
}
