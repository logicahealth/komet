
package sh.isaac.komet.batch.fxml;

import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.komet.batch.ActionCell;
import sh.isaac.komet.batch.action.ActionItem;
import sh.komet.gui.control.concept.PropertySheetItemConceptWrapper;
import sh.komet.gui.manifold.Manifold;

import java.net.URL;
import java.util.ResourceBundle;

public class ActionNodeController {


    ActionItem actionItem;
    ActionCell actionCell;


    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private BorderPane actionBorderPane;

    private Manifold manifold;

    private PropertySheetItemConceptWrapper conceptWrapper;

    private SimpleObjectProperty<ConceptSpecification> conceptProperty = new SimpleObjectProperty<>(this, MetaData.CONCEPT_FIELD____SOLOR.toExternalString());


    @FXML
    void initialize() {
        assert anchorPane != null : "fx:id=\"anchorPane\" was not injected: check your FXML file 'ActionNode.fxml'.";
        assert actionBorderPane != null : "fx:id=\"actionBorderPane\" was not injected: check your FXML file 'ActionNode.fxml'.";
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

    public void setAction(Manifold manifold, ActionItem actionItem) {
        this.actionItem = actionItem;
        this.manifold = manifold;
        this.actionBorderPane.setCenter(actionItem.getPropertySheet());
    }
}
