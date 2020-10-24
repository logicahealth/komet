
package sh.isaac.komet.batch.fxml;

import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.observable.coordinate.ObservableManifoldCoordinate;
import sh.isaac.komet.batch.ActionCell;
import sh.isaac.komet.batch.action.ActionItem;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.control.concept.PropertySheetItemConceptWrapper;
import sh.komet.gui.control.property.ViewProperties;

import java.net.URL;
import java.util.ResourceBundle;

public class ActionNodeController {


    ActionItem actionItem;
    ActionCell actionCell;

    @FXML
    private Button upButton;

    @FXML
    private Button downButton;

    @FXML
    private Button deleteButton;


    @FXML
    private Label actionTitle;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private BorderPane actionBorderPane;

    private ManifoldCoordinate manifold;

    private PropertySheetItemConceptWrapper conceptWrapper;

    private SimpleObjectProperty<ConceptSpecification> conceptProperty = new SimpleObjectProperty<>(this, MetaData.CONCEPT_FIELD____SOLOR.toExternalString());


    @FXML
    void initialize() {
        assert anchorPane != null : "fx:id=\"anchorPane\" was not injected: check your FXML file 'ActionNode.fxml'.";
        assert actionBorderPane != null : "fx:id=\"actionBorderPane\" was not injected: check your FXML file 'ActionNode.fxml'.";

        this.upButton.setGraphic(Iconography.ARROW_UP.getStyledIconographic());
        this.upButton.setText("");
        this.upButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        this.downButton.setGraphic(Iconography.ARROW_DOWN.getStyledIconographic());
        this.downButton.setText("");
        this.downButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        this.deleteButton.setGraphic(Iconography.DELETE_TRASHCAN.getStyledIconographic());
        this.deleteButton.setText("");
        this.deleteButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

    }

    @FXML
    void delete(ActionEvent event) {
        this.actionCell.delete(event);
    }

    @FXML
    void moveDown(ActionEvent event) {
        this.actionCell.moveDown(event);
    }

    @FXML
    void moveUp(ActionEvent event) {
        this.actionCell.moveUp(event);
    }

    public AnchorPane getAnchorPane() {
        return anchorPane;
    }

    public void setActionCell(ActionCell actionCell) {
        this.actionCell = actionCell;
    }

    public ActionCell getActionCell() {
        return actionCell;
    }

    public ActionItem getActionItem() {
        return actionItem;
    }

    public void setAction(ObservableManifoldCoordinate manifold, ActionItem actionItem) {
        actionItem.setupForGui(manifold);
        this.actionTitle.setText(actionItem.getTitle());
        this.actionItem = actionItem;
        this.manifold = manifold;
        this.actionBorderPane.setCenter(actionItem.getPropertySheet());
    }
}
