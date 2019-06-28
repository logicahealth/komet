package sh.isaac.komet.batch.fxml;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import sh.isaac.komet.batch.ActionCell;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;

import java.io.IOException;

public class CompositeActionNodeController {


    @FXML
    private ListView<ActionNodeController> actionListView;

    private Manifold manifold;

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert actionListView != null : "fx:id=\"actionListView\" was not injected: check your FXML file 'CompositeActionNode.fxml'.";
        actionListView.setCellFactory(param -> new ActionCell(actionListView, manifold));
    }

    @FXML
    void addAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sh/isaac/komet/batch/fxml/ActionNode.fxml"));
            Object root = loader.load();
            ActionNodeController actionNodeController = loader.getController();
            actionNodeController.setManifold(manifold);
            actionListView.getItems().add(actionNodeController);
        } catch (IOException e) {
            FxGet.dialogs().showErrorDialog(e);
        }
    }

    @FXML
    void newCompositeAction(ActionEvent event) {
        actionListView.getItems().clear();
    }

    @FXML
    void openCompositeAction(ActionEvent event) {

    }

    @FXML
    void saveCompositeAction(ActionEvent event) {

    }

    public void setManifold(Manifold manifold) {
        this.manifold = manifold;
    }
}
