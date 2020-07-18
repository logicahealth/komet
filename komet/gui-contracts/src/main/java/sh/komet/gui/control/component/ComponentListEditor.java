package sh.komet.gui.control.component;

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
import sh.isaac.api.ComponentProxy;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.komet.iconography.IconographyHelper;
import sh.komet.gui.contract.ConceptSearchNodeFactory;
import sh.komet.gui.contract.preferences.WindowPreferences;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.interfaces.ConceptExplorationNode;
import sh.komet.gui.util.FxGet;

public class ComponentListEditor implements PropertyEditor<ObservableList<ComponentProxy>> {

    private PopOver popOver;
    private ObservableList<ComponentProxy> value;
    private ReadOnlyObjectProperty<ConceptSpecification> findSelectedConceptSpecification;

    BorderPane editorPane = new BorderPane();
    AnchorPane anchorPane = new AnchorPane();
    ManifoldCoordinate manifoldCoordinate;
    ListView<ComponentProxy> componentListView = new ListView<>();
    {
        componentListView.setPrefHeight(152);
        componentListView.setMinHeight(152);
        componentListView.setMaxHeight(152);
        componentListView.setCellFactory(c-> new ListCell<ComponentProxy>() {
            @Override
            protected void updateItem(ComponentProxy item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    this.setText(item.getComponentString());
                } else {
                    this.setText("");
                }
            }

        });

        editorPane.setTop(anchorPane);
        editorPane.setCenter(componentListView);
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

    public ComponentListEditor(ManifoldCoordinate manifoldCoordinate) {
        this.manifoldCoordinate = manifoldCoordinate;
    }

    @Override
    public Node getEditor() {
        return editorPane;
    }

    @Override
    public ObservableList<ComponentProxy> getValue() {
        return value;
    }

    @Override
    public void setValue(ObservableList<ComponentProxy> value) {
        this.value = value;
        componentListView.setItems(value);
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
        WindowPreferences windowPreferences = FxGet.windowPreferences(editorPane);
        ConceptExplorationNode searchExplorationNode = searchNodeFactory.createNode(windowPreferences.getViewPropertiesForWindow(),
                windowPreferences.getViewPropertiesForWindow().getActivityFeed(ViewProperties.LIST), null);
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
            this.componentListView.getItems().add(new ComponentProxy(this.findSelectedConceptSpecification.get()));
        }
    }
    private void deleteSelection(ActionEvent event) {
        int selectedIndex = this.componentListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex > -1) {
            this.componentListView.getItems().remove(selectedIndex);
        }
    }
    private void moveUpSelection(ActionEvent event) {
        int selectedIndex = this.componentListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex > 0) {
            ComponentProxy specToMove = this.componentListView.getItems().remove(selectedIndex);
            this.componentListView.getItems().add(selectedIndex-1, specToMove);
            this.componentListView.getSelectionModel().select(selectedIndex-1);
        }
    }
    private void moveDownSelection(ActionEvent event) {
        int selectedIndex = this.componentListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex > -1 && selectedIndex < this.componentListView.getItems().size() - 1) {
            ComponentProxy specToMove = this.componentListView.getItems().remove(selectedIndex);
            this.componentListView.getItems().add(selectedIndex+1, specToMove);
            this.componentListView.getSelectionModel().select(selectedIndex+1);
        }
    }
}