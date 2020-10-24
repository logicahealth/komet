package sh.isaac.komet.batch.fxml;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.marshal.MarshalUtil;
import sh.isaac.api.observable.concept.ObservableConceptChronology;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.api.util.UuidStringKey;
import sh.isaac.api.util.time.DateTimeUtil;
import sh.isaac.komet.batch.ActionCell;
import sh.isaac.komet.batch.VersionChangeListener;
import sh.isaac.komet.batch.action.ActionFactory;
import sh.isaac.komet.batch.action.ActionItem;
import sh.isaac.komet.batch.action.CompositeAction;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.interfaces.ComponentList;
import sh.komet.gui.lists.ComponentListSelectorForMenuButton;
import sh.komet.gui.util.FxGet;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

public class CompositeActionNodeController implements VersionChangeListener {


    public static final String SOLOR_ACTION_FILE_EXT = ".saf";
    public static final String KOMET_ACTION_FILE_EXT = ".kaf";
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TitledPane specificationTitledPane;

    @FXML
    private AnchorPane affectedConceptsAnchorPane;

    @FXML
    private TitledPane affectedConceptsTitledPane;

    @FXML
    private TitledPane actionLogTitledPane;

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
    private MenuButton listMenuButton;


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

    private ViewProperties viewProperties;

    private Transaction transaction;

    private File currentFile;

    private ListViewNodeController listViewController;

    private ComponentListSelectorForMenuButton componentListSelectorForMenuButton;

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert actionListView != null : "fx:id=\"actionListView\" was not injected: check your FXML file 'CompositeActionNode.fxml'.";
        assert applyButton != null : "fx:id=\"applyButton\" was not injected: check your FXML file 'CompositeActionNode.fxml'.";
        assert commitButton != null : "fx:id=\"commitButton\" was not injected: check your FXML file 'CompositeActionNode.fxml'.";
        assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'CompositeActionNode.fxml'.";
        assert listMenuButton != null : "fx:id=\"listMenuButton\" was not injected: check your FXML file 'CompositeActionNode.fxml'.";
        assert actionNameField != null : "fx:id=\"actionNameField\" was not injected: check your FXML file 'CompositeActionNode.fxml'.";
        assert newButton != null : "fx:id=\"newButton\" was not injected: check your FXML file 'CompositeActionNode.fxml'.";
        assert openButton != null : "fx:id=\"openButton\" was not injected: check your FXML file 'CompositeActionNode.fxml'.";
        assert saveButton != null : "fx:id=\"saveButton\" was not injected: check your FXML file 'CompositeActionNode.fxml'.";


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

        actionListView.setCellFactory(param -> new ActionCell(actionListView, viewProperties));

        List<ActionFactory> factories = Get.services(ActionFactory.class);
        factories.sort((o1, o2) -> o1.getActionName().compareTo(o2.getActionName()));
        for (ActionFactory factory: factories) {
            MenuItem factoryItem = new MenuItem(factory.getActionName(), factory.getActionIcon());
            factoryItem.setOnAction(event -> {
                addAction(factory.makeActionItem(viewProperties.getManifoldCoordinate()));
            });
            addActionMenuButton.getItems().add(factoryItem);
        }

        Platform.runLater(() -> specificationTitledPane.setExpanded(true));
    }

    void addAction(ActionItem actionItem) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sh/isaac/komet/batch/fxml/ActionNode.fxml"));
            Object root = loader.load();
            ActionNodeController actionNodeController = loader.getController();
            actionNodeController.setAction(viewProperties.getManifoldCoordinate(), actionItem);
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
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Komet action file (*." +
                KOMET_ACTION_FILE_EXT + ", *." + SOLOR_ACTION_FILE_EXT +
                ")", "*"+KOMET_ACTION_FILE_EXT, "*"+SOLOR_ACTION_FILE_EXT));
        fileChooser.setInitialDirectory(FxGet.actionFileDirectory());
        final File importFile = fileChooser.showOpenDialog(null);
        if (importFile != null) {
            try {
                newCompositeAction(event);
                currentFile = importFile;
                CompositeAction compositeAction = MarshalUtil.fromFile(importFile);
                actionNameField.setText(compositeAction.getActionTitle());
                this.componentListSelectorForMenuButton.componentListProperty().setValue(compositeAction.getListKey());
                for (ActionItem actionItem: compositeAction.getActionItemList()) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/sh/isaac/komet/batch/fxml/ActionNode.fxml"));
                    Object root = loader.load();
                    ActionNodeController actionNodeController = loader.getController();
                    actionNodeController.setAction(viewProperties.getManifoldCoordinate(), actionItem);
                    actionListView.getItems().add(actionNodeController);
                }
            } catch (IOException e) {
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
            } catch (IOException e) {
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
        fileChooser.setInitialFileName(compositeAction.getActionTitle() + KOMET_ACTION_FILE_EXT);
        fileChooser.setInitialDirectory(FxGet.actionFileDirectory());
        final File exportFile = fileChooser.showSaveDialog(null);
        if (exportFile != null) {
            try {
                exportFile.createNewFile();
                MarshalUtil.toFile(compositeAction, exportFile);
                String exportName = exportFile.getName();
                exportName = exportName.substring(0,exportName.length() - KOMET_ACTION_FILE_EXT.length());
                this.actionNameField.setText(exportName);
                this.currentFile = exportFile;
                this.saveButton.setDisable(false);
            } catch (IOException e) {
                FxGet.dialogs().showErrorDialog(e);
            }
        }
    }


    @FXML
    void applyActions(ActionEvent event) {
        // TODO turn this into a timed task with progress tracker...
        applyButton.setDisable(true);
        commitButton.setDisable(false);
        cancelButton.setDisable(false);

        String timeString = DateTimeUtil.timeNowSimple();
        if (transaction == null) {
            this.transaction = Get.commitService().newTransaction(Optional.of(actionNameField.getText()
                    + " " + timeString), ChangeCheckerMode.ACTIVE);
        }
        UuidStringKey listKey = this.componentListSelectorForMenuButton.getComponentListKey();
          if (listKey != null) {
            try {
                this.affectedConceptsTitledPane.setText("affected concepts");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/sh/isaac/komet/batch/fxml/ListViewNode.fxml"));
                Node root = loader.load();
                this.listViewController = loader.getController();
                this.listViewController.setViewProperties(viewProperties, viewProperties.getActivityFeed(ViewProperties.LIST));
                this.listViewController.nameProperty().setValue(this.actionNameField.getText() + " " + DateTimeUtil.nowWithZone());

                AnchorPane.setTopAnchor(root, 1.0);
                AnchorPane.setRightAnchor(root, 1.0);
                AnchorPane.setBottomAnchor(root, 1.0);
                AnchorPane.setLeftAnchor(root, 1.0);
                this.affectedConceptsAnchorPane.getChildren().setAll(root);

                CompositeAction compositeAction = getCompositeAction();
                ComponentList componentList = FxGet.componentList(listKey, this.viewProperties.getManifoldCoordinate());
                Future<?> future = compositeAction.apply(componentList.listSize(), componentList.getComponentStream(), this.transaction,
                        this.viewProperties.getManifoldCoordinate(), this);
                Platform.runLater(() -> processFuture(future));
            } catch (IOException e) {
                FxGet.dialogs().showErrorDialog(e);
            }
        } else {
            FxGet.dialogs().showErrorDialog("No list selected", "You must select a list to apply the actions to",
                    "The actions are applied to a list of components. You must select a list to apply the actions to.");
        }
    }

    private void processFuture(Future<?> future) {
        try {
            future.get(1, TimeUnit.MILLISECONDS);
            if (transaction.getComponentNidsForTransaction().isEmpty()) {
                Platform.runLater(() -> cancelActions(null));
            }
        } catch (ExecutionException|InterruptedException e) {
            FxGet.dialogs().showErrorDialog(e);
        } catch (TimeoutException e) {
            Platform.runLater(() ->
                processFuture(future));
        }
    }

    @Override
    public void versionChanged(Version oldValue, Version newValue) {
        switch (newValue.getSemanticType()) {
            case CONCEPT:
                addAffectedConcept(newValue.getNid());
                break;
            default:
                SemanticChronology sc = ((SemanticVersion) newValue).getChronology();
                while (sc != null) {
                    Optional<? extends Chronology> optionalReferencedComponent = Get.identifiedObjectService().getChronology(sc.getReferencedComponentNid());
                    sc = null;
                    if (optionalReferencedComponent.isPresent()) {
                        Chronology referencedComponent = optionalReferencedComponent.get();
                        if (referencedComponent.getVersionType() == VersionType.CONCEPT) {
                            addAffectedConcept(referencedComponent.getNid());
                        } else {
                            sc = (SemanticChronology) referencedComponent;
                        }
                    }
                }
        }
    }

    private void addAffectedConcept(int conceptNid) {
        if (this.listViewController != null) {
            ObservableConceptChronology conceptChronology = Get.observableChronologyService().getObservableConceptChronology(conceptNid);
            Platform.runLater(() -> {
                this.listViewController.getItemList().add(conceptChronology);
                int count = this.listViewController.getItemList().size();
                this.affectedConceptsTitledPane.setText("affected concepts: " + count);
            });
        }
    }

    private CompositeAction getCompositeAction() {
        List<ActionItem> actions = new ArrayList<>(actionListView.getItems().size());
        for (ActionNodeController actionNodeController: actionListView.getItems()) {
            actions.add(actionNodeController.actionItem);
        }
        return new CompositeAction(actionNameField.getText(),
                this.componentListSelectorForMenuButton.getComponentListKey(),
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

    public StringProperty getActionNameProperty() {
        return actionNameField.textProperty();
    }

    public void setViewProperties(ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
        this.componentListSelectorForMenuButton = new ComponentListSelectorForMenuButton(this.listMenuButton,
                this.viewProperties.getManifoldCoordinate());
    }

}
