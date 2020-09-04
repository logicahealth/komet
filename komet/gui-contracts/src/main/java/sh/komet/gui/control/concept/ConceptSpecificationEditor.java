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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.PopOver;
import org.controlsfx.property.editor.PropertyEditor;
import com.sun.javafx.scene.control.ControlAcceleratorSupport;
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
import sh.isaac.api.ComponentProxy;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.komet.iconography.IconographyHelper;
import sh.komet.gui.contract.ConceptSearchNodeFactory;
import sh.komet.gui.contract.preferences.WindowPreferences;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.interfaces.ConceptExplorationNode;
import sh.komet.gui.menu.MenuItemWithText;
import sh.komet.gui.util.FxGet;

/**
 *
 * @author kec
 */
public class ConceptSpecificationEditor implements PropertyEditor<ConceptSpecification> {
    private static final Logger LOG = LogManager.getLogger();

    private final SimpleObjectProperty<ConceptSpecification> conceptSpecificationValue;
    private final MenuButton menuButton = new MenuButton();
    private final ManifoldCoordinate manifoldCoordinate;
    private ReadOnlyObjectProperty<ConceptSpecification> findSelectedConceptSpecification;
    private PopOver popOver;
    private final MenuItem findItem = new MenuItemWithText("Find");
    FixedWidthFindSeparator fixedWidthFindSeparator = new FixedWidthFindSeparator();
    FixedWidthFindSeparator fixedWidthManifoldSeparator = new FixedWidthFindSeparator();

    public ConceptSpecificationEditor(PropertySheetItemConceptWrapper wrapper, ManifoldCoordinate manifoldCoordinate) {
        this.manifoldCoordinate = manifoldCoordinate;
        this.conceptSpecificationValue = (SimpleObjectProperty<ConceptSpecification>) wrapper.getObservableValue().get();
        this.conceptSpecificationValue.addListener(this::setButtonText);
        if (wrapper.getValue() != null) {
            this.menuButton.setText(manifoldCoordinate.getPreferredDescriptionText(wrapper.getValue()));
        } else {
            this.menuButton.setText("Empty");
        }
        
        
        this.menuButton.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                LOG.debug("Adding meta f to " + menuButton.getText());
                findItem.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.META_DOWN));
                ControlAcceleratorSupport.addAcceleratorsIntoScene(menuButton.getItems(), menuButton);


                menuButton.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.F, KeyCombination.META_DOWN), () -> {
                    showFindPopup(null);
                });
            } else {
                ControlAcceleratorSupport.removeAcceleratorsFromScene(menuButton.getItems(), menuButton);
                findItem.setAccelerator(null);
            }
        });
        this.menuButton.widthProperty().addListener((observable, oldValue, newValue) -> {
            fixedWidthFindSeparator.setWidth(newValue.doubleValue() - 16);
            fixedWidthManifoldSeparator.setWidth(newValue.doubleValue() - 16);
        });
        this.menuButton.sceneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                addMenuItems(wrapper, manifoldCoordinate);
            }
        });
    }

    protected final void addMenuItems(PropertySheetItemConceptWrapper wrapper, ManifoldCoordinate manifoldCoordinate) {
        this.menuButton.getItems().clear();
        for (ConceptSpecification allowedValue : wrapper.getAllowedValues()) {
            ConceptMenuItem menuItem = new ConceptMenuItem(allowedValue, manifoldCoordinate);
            menuItem.setOnAction(this::handleAction);
            this.menuButton.getItems().add(menuItem);
        }
        this.menuButton.getItems().add(fixedWidthFindSeparator);
        if (wrapper.allowSearch()) {
            findItem.setOnAction(this::showFindPopup);
            this.menuButton.getItems().add(findItem);
        }
        if (wrapper.allowHistory()) {
            this.menuButton.getItems().add(fixedWidthManifoldSeparator);
            WindowPreferences windowPreferences = FxGet.windowPreferences(this.menuButton);

            for (ActivityFeed activityFeed: windowPreferences.getViewPropertiesForWindow().getActivityFeeds()) {
                if (!activityFeed.feedHistoryProperty().isEmpty()) {
                    Menu activityFeedHistory = new Menu(activityFeed.getFeedName());
                    this.menuButton.getItems().add(activityFeedHistory);
                    for (ComponentProxy record : activityFeed.feedHistoryProperty()) {

                        MenuItem historyMenuItem = new MenuItem(record.getComponentString());
                        historyMenuItem.setUserData(record);
                        historyMenuItem.setOnAction(this::handleAction);
                        activityFeedHistory.getItems().add(historyMenuItem);
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
        if (event.getSource() instanceof MenuItem) {
            MenuItem menuItem = (MenuItem) event.getSource();
            ComponentProxy record = (ComponentProxy) menuItem.getUserData();
            ConceptSpecification spec = Get.conceptSpecification(record.getNid());
            this.conceptSpecificationValue.set(spec);
            this.menuButton.setText(manifoldCoordinate.getPreferredDescriptionText(spec));
        }
    }
    
    private void setButtonText(ObservableValue<? extends ConceptSpecification> observable, ConceptSpecification oldValue, ConceptSpecification newValue) {
        menuButton.setText(manifoldCoordinate.getPreferredDescriptionText(conceptSpecificationValue.get()));
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
        WindowPreferences windowPreferences = FxGet.windowPreferences(this.menuButton);

        ConceptExplorationNode searchExplorationNode = searchNodeFactory.createNode(windowPreferences.getViewPropertiesForWindow(),
                windowPreferences.getViewPropertiesForWindow().getUnlinkedActivityFeed() , null);
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
            ConceptSpecification selectedConcept = this.findSelectedConceptSpecification.get();
            selectedConcept = new ConceptProxy(selectedConcept);
            ConceptSpecificationForControlWrapper newConceptSpec = new ConceptSpecificationForControlWrapper(selectedConcept, manifoldCoordinate);
            this.conceptSpecificationValue.set(newConceptSpec);
        }
    }
}
