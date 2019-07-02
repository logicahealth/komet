package sh.isaac.komet.batch.fxml;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import sh.isaac.api.Get;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.komet.batch.ActionCell;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.search.flwor.LetPropertySheet;
import sh.komet.gui.util.FxGet;

import java.io.IOException;

public class CompositeActionNodeController {


    @FXML
    private ListView<ActionNodeController> actionListView;

    @FXML
    private ChoiceBox<FxGet.ComponentListKey> listChoiceBox;


    private Manifold manifold;

    private Transaction transaction;

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert actionListView != null : "fx:id=\"actionListView\" was not injected: check your FXML file 'CompositeActionNode.fxml'.";
        actionListView.setCellFactory(param -> new ActionCell(actionListView, manifold));
        listChoiceBox.setItems(FxGet.componentListKeys());
        //LetPropertySheet letPropertySheet = new LetPropertySheet();
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


    @FXML
    void applyActions(ActionEvent event) {
        FxGet.ComponentListKey listKey = listChoiceBox.getValue();
        if (transaction == null) {
            this.transaction = Get.commitService().newTransaction(ChangeCheckerMode.ACTIVE);
        }
        StampCoordinate stampCoordinate = null;
        EditCoordinate editCoordinate = FxGet.editCoordinate();
        if (listKey != null) {
            for (ObservableChronology chronology: listKey.getComponentList().getComponents()) {
                for (ActionNodeController actionNode: actionListView.getItems()) {
                    actionNode.getAction().apply(chronology, this.transaction, stampCoordinate, editCoordinate);
                    throw new UnsupportedOperationException("Need to implement stamp coordinate selection");
                }
            }
        }
    }

    @FXML
    void commitActions(ActionEvent event) {
        this.transaction.commit();
        this.transaction = null;
    }

    @FXML
    void cancelActions(ActionEvent event) {
        this.transaction.cancel();
        this.transaction = null;
    }


    public void setManifold(Manifold manifold) {
        this.manifold = manifold;
    }
}
