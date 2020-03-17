package sh.isaac.komet.batch.fxml;

import javafx.collections.MapChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import sh.isaac.api.Get;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.marshal.MarshalUtil;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.coordinate.ObservableStampCoordinate;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.komet.batch.ActionCell;
import sh.isaac.komet.batch.action.ActionFactory;
import sh.isaac.komet.batch.action.ActionItem;
import sh.isaac.komet.batch.action.CompositeAction;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;
import sh.komet.gui.util.UuidStringKey;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class CompositeActionNodeController {


    public static final String SOLOR_ACTION_FILE_EXT = ".saf";
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
    private MenuButton addActionMenuButton;

    @FXML
    private ChoiceBox<UuidStringKey> listChoiceBox;

    @FXML
    private ChoiceBox<UuidStringKey> viewKeyChoiceBox;

    @FXML
    private Button newButton;

    @FXML
    private Button openButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button saveAsButton;

    @FXML
    private TextField actionNameField;

    private Manifold manifold;

    private Transaction transaction;

    private File currentFile;

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert actionListView != null : "fx:id=\"actionListView\" was not injected: check your FXML file 'CompositeActionNode.fxml'.";
        assert applyButton != null : "fx:id=\"applyButton\" was not injected: check your FXML file 'CompositeActionNode.fxml'.";
        assert commitButton != null : "fx:id=\"commitButton\" was not injected: check your FXML file 'CompositeActionNode.fxml'.";
        assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'CompositeActionNode.fxml'.";
        assert listChoiceBox != null : "fx:id=\"listChoiceBox\" was not injected: check your FXML file 'CompositeActionNode.fxml'.";
        assert viewKeyChoiceBox != null : "fx:id=\"stampChoiceBox\" was not injected: check your FXML file 'CompositeActionNode.fxml'.";
        assert actionNameField != null : "fx:id=\"actionNameField\" was not injected: check your FXML file 'CompositeActionNode.fxml'.";
        assert newButton != null : "fx:id=\"newButton\" was not injected: check your FXML file 'CompositeActionNode.fxml'.";
        assert openButton != null : "fx:id=\"openButton\" was not injected: check your FXML file 'CompositeActionNode.fxml'.";
        assert saveButton != null : "fx:id=\"saveButton\" was not injected: check your FXML file 'CompositeActionNode.fxml'.";

        listChoiceBox.setItems(FxGet.componentListKeys());
        viewKeyChoiceBox.getItems().setAll(FxGet.stampCoordinates().keySet());
        FxGet.stampCoordinates().addListener((MapChangeListener<UuidStringKey, ObservableStampCoordinate>) change -> {
            viewKeyChoiceBox.getItems().setAll(FxGet.stampCoordinates().keySet());
        });

        //LetPropertySheet letPropertySheet = new LetPropertySheet();

        actionNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                applyButton.setDisable(true);
                commitButton.setDisable(true);
                cancelButton.setDisable(true);
                saveButton.setDisable(true);
                saveAsButton.setDisable(true);
            } else {
                if (commitButton.isDisabled()) {
                    applyButton.setDisable(false);
                }
                if (currentFile != null && currentFile.getName().equals(newValue + SOLOR_ACTION_FILE_EXT)) {
                    saveButton.setDisable(false);
                } else {
                    saveButton.setDisable(true);
                }

                saveAsButton.setDisable(false);
            }
        });

        actionListView.setCellFactory(param -> new ActionCell(actionListView, manifold));

        List<ActionFactory> factories = Get.services(ActionFactory.class);
        factories.sort((o1, o2) -> o1.getActionName().compareTo(o2.getActionName()));
        for (ActionFactory factory: factories) {
            MenuItem factoryItem = new MenuItem(factory.getActionName(), factory.getActionIcon());
            factoryItem.setOnAction(event -> {
                addAction(factory.makeActionItem(manifold));
            });
            addActionMenuButton.getItems().add(factoryItem);
        }
    }

    void addAction(ActionItem actionItem) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sh/isaac/komet/batch/fxml/ActionNode.fxml"));
            Object root = loader.load();
            ActionNodeController actionNodeController = loader.getController();
            actionNodeController.setAction(manifold, actionItem);
            actionListView.getItems().add(actionNodeController);
        } catch (IOException e) {
            FxGet.dialogs().showErrorDialog(e);
        }
    }

    @FXML
    void newCompositeAction(ActionEvent event) {
        currentFile = null;
        actionNameField.setText("New composite action");
        actionListView.getItems().clear();
    }

    @FXML
    void openCompositeAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open composite action");
        // saf = solor action file
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Solor action file (*.saf)", "*.saf"));
        fileChooser.setInitialDirectory(FxGet.actionFileDirectory());
        final File importFile = fileChooser.showOpenDialog(null);
        if (importFile != null) {
            try {
                newCompositeAction(event);
                currentFile = importFile;
                CompositeAction compositeAction = MarshalUtil.fromFile(importFile);
                actionNameField.setText(compositeAction.getActionTitle());
                listChoiceBox.setValue(compositeAction.getListKey());
                viewKeyChoiceBox.setValue(compositeAction.getViewKey());
                for (ActionItem actionItem: compositeAction.getActionItemList()) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/sh/isaac/komet/batch/fxml/ActionNode.fxml"));
                    Object root = loader.load();
                    ActionNodeController actionNodeController = loader.getController();
                    actionNodeController.setAction(manifold, actionItem);
                    actionListView.getItems().add(actionNodeController);
                }
            } catch (IOException | ReflectiveOperationException e) {
                FxGet.dialogs().showErrorDialog(e);
            }
        }
    }

    @FXML
    void saveCompositeAction(ActionEvent event) {
        if (currentFile != null) {
            try {
                CompositeAction compositeAction = getCompositeAction();
                currentFile.createNewFile();
                MarshalUtil.toFile(compositeAction, currentFile);
            } catch (IOException | ReflectiveOperationException e) {
                FxGet.dialogs().showErrorDialog(e);
            }
        } else {
            FxGet.dialogs().showErrorDialog(new NullPointerException("currentFile is null..."));
        }
    }

    @FXML
    void saveCompositeActionAs(ActionEvent event) {
        CompositeAction compositeAction = getCompositeAction();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save composite action");
        // saf = solor action file
        fileChooser.setInitialFileName(compositeAction.getActionTitle() + SOLOR_ACTION_FILE_EXT);
        fileChooser.setInitialDirectory(FxGet.actionFileDirectory());
        final File exportFile = fileChooser.showSaveDialog(null);
        if (exportFile != null) {
            try {
                exportFile.createNewFile();
                MarshalUtil.toFile(compositeAction, exportFile);
                String exportName = exportFile.getName();
                exportName = exportName.substring(0,exportName.length() - SOLOR_ACTION_FILE_EXT.length());
                this.actionNameField.setText(exportName);
                this.currentFile = exportFile;
                this.saveButton.setDisable(false);
            } catch (IOException | ReflectiveOperationException e) {
                FxGet.dialogs().showErrorDialog(e);
            }
        }

    }


    @FXML
    void applyActions(ActionEvent event) {
        // TODO turn this into a timed task with progress tracker...
        UuidStringKey listKey = listChoiceBox.getValue();
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

        UuidStringKey stampKey = viewKeyChoiceBox.getSelectionModel().selectedItemProperty().getValue();
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
            CompositeAction compositeAction = getCompositeAction();
            compositeAction.apply(FxGet.componentList(listKey).getComponents(), this.transaction,
                    stampCoordinate, editCoordinate);
        } else {
            FxGet.dialogs().showErrorDialog("No list selected", "You must select a list to apply the actions to",
                    "The actions are applied to a list of components. You must select a list to apply the actions to.");
        }
    }

    private CompositeAction getCompositeAction() {
        List<ActionItem> actions = new ArrayList<>(actionListView.getItems().size());
        for (ActionNodeController actionNodeController: actionListView.getItems()) {
            actions.add(actionNodeController.actionItem);
        }
        return new CompositeAction(actionNameField.getText(),
                listChoiceBox.getValue(),
                viewKeyChoiceBox.getValue(),
                actions);
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
