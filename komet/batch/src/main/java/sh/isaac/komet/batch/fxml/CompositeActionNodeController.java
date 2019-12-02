package sh.isaac.komet.batch.fxml;

import javafx.collections.MapChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import sh.isaac.api.Get;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.coordinate.ObservableStampCoordinate;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.komet.batch.ActionCell;
import sh.isaac.komet.batch.action.PromoteComponentAction;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;
import sh.komet.gui.util.UuidStringKey;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;

public class CompositeActionNodeController {


    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ListView<ActionNodeController> actionListView;

    @FXML
    private Button applyButton;

    @FXML
    private Button commitButton;

    @FXML
    private Button cancelButton;

    @FXML
    private ChoiceBox<FxGet.ComponentListKey> listChoiceBox;

    @FXML
    private ChoiceBox<UuidStringKey> stampChoiceBox;

    @FXML
    private Button newButton;

    @FXML
    private Button openButton;

    @FXML
    private Button saveButton;

    @FXML
    private TextField actionNameField;

    private Manifold manifold;

    private Transaction transaction;

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert actionListView != null : "fx:id=\"actionListView\" was not injected: check your FXML file 'CompositeActionNode.fxml'.";
        assert applyButton != null : "fx:id=\"applyButton\" was not injected: check your FXML file 'CompositeActionNode.fxml'.";
        assert commitButton != null : "fx:id=\"commitButton\" was not injected: check your FXML file 'CompositeActionNode.fxml'.";
        assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'CompositeActionNode.fxml'.";
        assert listChoiceBox != null : "fx:id=\"listChoiceBox\" was not injected: check your FXML file 'CompositeActionNode.fxml'.";
        assert stampChoiceBox != null : "fx:id=\"stampChoiceBox\" was not injected: check your FXML file 'CompositeActionNode.fxml'.";
        assert actionNameField != null : "fx:id=\"actionNameField\" was not injected: check your FXML file 'CompositeActionNode.fxml'.";
        assert newButton != null : "fx:id=\"newButton\" was not injected: check your FXML file 'CompositeActionNode.fxml'.";
        assert openButton != null : "fx:id=\"openButton\" was not injected: check your FXML file 'CompositeActionNode.fxml'.";
        assert saveButton != null : "fx:id=\"saveButton\" was not injected: check your FXML file 'CompositeActionNode.fxml'.";

        listChoiceBox.setItems(FxGet.componentListKeys());
        stampChoiceBox.getItems().setAll(FxGet.stampCoordinates().keySet());
        FxGet.stampCoordinates().addListener((MapChangeListener<UuidStringKey, ObservableStampCoordinate>) change -> {
            stampChoiceBox.getItems().setAll(FxGet.stampCoordinates().keySet());
        });

        //LetPropertySheet letPropertySheet = new LetPropertySheet();

        actionNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                applyButton.setDisable(true);
                commitButton.setDisable(true);
                cancelButton.setDisable(true);
                saveButton.setDisable(true);
            } else {
                if (commitButton.isDisabled()) {
                    applyButton.setDisable(false);
                }
                saveButton.setDisable(false);
            }
        });

        actionListView.setCellFactory(param -> new ActionCell(actionListView, manifold));

    }

    @FXML
    void addAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sh/isaac/komet/batch/fxml/ActionNode.fxml"));
            Object root = loader.load();
            ActionNodeController actionNodeController = loader.getController();
            actionNodeController.setAction(manifold, new PromoteComponentAction(manifold));
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
        // TODO turn this into a timed task with progress tracker...
        FxGet.ComponentListKey listKey = listChoiceBox.getValue();
        applyButton.setDisable(true);
        commitButton.setDisable(false);
        cancelButton.setDisable(false);

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");

        String timeString = formatter.format(date);

        if (transaction == null) {
            this.transaction = Get.commitService().newTransaction(Optional.of(actionNameField.getText()
                    + " " + timeString), ChangeCheckerMode.ACTIVE);
        }

        UuidStringKey stampKey = stampChoiceBox.getSelectionModel().selectedItemProperty().getValue();
        if (stampKey == null) {
            FxGet.dialogs().showErrorDialog("No Stamp selected",
                    "You must select a stamp coordinate to define the version eligible for promotion",
                    "The stamp coordinate is applied to a chronology to compute the latest version. " +
                            "Without the stamp coordinate, a promotion determination cannot be made. ");
            return;
        }
        StampCoordinate stampCoordinate = FxGet.stampCoordinates().get(stampKey);
        EditCoordinate editCoordinate = FxGet.editCoordinate();
        if (listKey != null) {
            for (ObservableChronology chronology: listKey.getComponentList().getComponents()) {
                for (ActionNodeController actionNode: actionListView.getItems()) {
                    actionNode.getActionItem().apply(chronology, this.transaction, stampCoordinate, editCoordinate);
                }
            }
        }
    }

    @FXML
    void commitActions(ActionEvent event) {
        this.transaction.commit();
        this.transaction = null;
        applyButton.setDisable(false);
        commitButton.setDisable(true);
        cancelButton.setDisable(true);
    }

    @FXML
    void cancelActions(ActionEvent event) {
        this.transaction.cancel();
        this.transaction = null;
        applyButton.setDisable(false);
        commitButton.setDisable(true);
        cancelButton.setDisable(true);
    }

    public void setManifold(Manifold manifold) {
        this.manifold = manifold;
    }
}
