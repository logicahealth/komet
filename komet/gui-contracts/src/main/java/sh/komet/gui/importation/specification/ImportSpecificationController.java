package sh.komet.gui.importation.specification;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import sh.komet.gui.manifold.Manifold;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 2019-05-01
 * aks8m - https://github.com/aks8m
 */
public class ImportSpecificationController {

    private Manifold manifold;
    private SimpleBooleanProperty closeExplorationNode;

    private SimpleListProperty<ImportSpecificationWizardWrapper> wizardPanes = new SimpleListProperty<>(FXCollections.observableArrayList());
    private SimpleIntegerProperty indexProperty = new SimpleIntegerProperty(-1);
    private final SimpleListProperty<File> filesProperty = new SimpleListProperty<>(FXCollections.observableArrayList());

    @FXML
    private BorderPane importSpecificationBorderPane;
    @FXML
    private HBox wizardHBox;
    @FXML
    private Pane importSpecificationLeftPane;
    @FXML
    private Button previousButton;
    @FXML
    private Button nextButton;
    @FXML
    private ComboBox standaloneImporterComboBox;
    @FXML
    private Label positionLabel;
    @FXML
    private Label curentFileNameLabel;

    @FXML
    private void initialize() {

        this.indexProperty.addListener((observableValue, originalNumber, newNumber) -> {

            this.wizardHBox.getChildren().clear();
            this.wizardHBox.getChildren().add(this.wizardPanes.get(newNumber.intValue()).getNode());
            this.curentFileNameLabel.setText(this.wizardPanes.get(newNumber.intValue()).getImportController().getFileName());
            this.positionLabel.setText((newNumber.intValue() + 1) + " of " + this.wizardPanes.size());
        });

        this.filesProperty
                .addListener((ListChangeListener<File>) change -> {

                    while(change.next()) {

                        change.getAddedSubList().stream()
                                .forEach(file -> {

                                    try {

                                        FXMLLoader importConfigurationLoader = new FXMLLoader(getClass().getResource("/fxml/ImportConfigurationWizard.fxml"));

                                        Node configNode = importConfigurationLoader.load();
                                        ImportConfigurationController importConfigurationController = importConfigurationLoader.getController();
                                        importConfigurationController.setFileToImport(file);

                                        this.wizardPanes.add(new ImportSpecificationWizardWrapper(configNode, importConfigurationController));

                                        if(this.indexProperty.get() == -1){
                                            this.indexProperty.setValue(0);
                                        }

                                        this.positionLabel.setText((this.indexProperty.intValue() + 1) + " of " + this.wizardPanes.size());

                                    } catch (IOException ioE) {
                                        ioE.printStackTrace();
                                    }

                                });
                    }
        });

        this.positionLabel.setText("0 of " + this.wizardPanes.size());

    }

    @FXML
    private void previousWizardPane(ActionEvent actionEvent){
        if(this.indexProperty.get() > 0) {
            this.indexProperty.setValue(this.indexProperty.get() - 1);
        }
    }

    @FXML
    private void nextWizardPane(ActionEvent actionEvent){
        if(this.indexProperty.get() < this.wizardPanes.size() - 1) {
            this.indexProperty.setValue(this.indexProperty.get() + 1);
        }
    }

    @FXML
    private void browseForFiles(MouseEvent actionEvent){
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Specify file(s) for import...");

        List<File> importFiles = chooser.showOpenMultipleDialog(importSpecificationBorderPane.getScene().getWindow());
        if(importFiles != null && importFiles.size() > 0) {
            this.filesProperty.get().addAll(importFiles);
        }
    }

    @FXML
    private void dragOverFiles(DragEvent dragEvent){
        dragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        dragEvent.consume();
    }

    @FXML
    private void dragAndDropFiles(DragEvent dragEvent){
        Dragboard dragboard = dragEvent.getDragboard();
        if(dragboard.hasFiles()){
            this.filesProperty.get().addAll(dragboard.getFiles());
        }
    }

    @FXML
    private void runImportSpecification(ActionEvent actionEvent){

    }

    public void setManifold(Manifold manifold){
        this.manifold = manifold;
    }

    public void setCloseExplorationNodeProperty(SimpleBooleanProperty closeExplorationNode) {
        this.closeExplorationNode = closeExplorationNode;
    }
}
