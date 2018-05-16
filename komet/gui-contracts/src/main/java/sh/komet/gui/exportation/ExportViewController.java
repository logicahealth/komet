package sh.komet.gui.exportation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.Get;
import sh.komet.gui.manifold.Manifold;

import java.io.File;


/*
 * aks8m - 5/15/18
 */
public class ExportViewController {

    protected static final Logger LOG = LogManager.getLogger();
    private Stage exportStage;
    private Manifold manifold;

    @FXML
    private Button directoryBrowseButton;
    @FXML
    private Button exportButton;
    @FXML
    private ChoiceBox<ExportTypes> exportTypeChoiceBox;
    @FXML
    private TextField directoryTextField;

    private final ObservableList<ExportTypes> exportTypes = FXCollections.observableArrayList();
    private File selectedDirectory;


    @FXML
    void initialize(){
        assert directoryBrowseButton != null : "fx:id=\"directoryBrowseButton\" was not injected: check your FXML file 'ExportView.fxml'.";
        assert exportButton != null: "fx:id=\"exportButton\" was not injected: check your FXML file 'ExportView.fxml'.";
        assert exportTypeChoiceBox != null : "fx:id=\"exportTypeChoiceBox\" was not injected: check your FXML file 'ExportView.fxml'.";
        assert directoryTextField != null : "fx:id=\"directoryTextField\" was not injected: check your FXML file 'ExportView.fxml'.";


        this.exportTypes.addAll(ExportTypes.values());
        this.exportTypeChoiceBox.setItems(this.exportTypes);
        this.exportButton.setDisable(true);
        this.exportTypeChoiceBox.getSelectionModel().selectFirst();
    }

    @FXML
    public void browseForDirectory(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Browse to Directory to Export");
        this.selectedDirectory = directoryChooser.showDialog(this.exportStage);

        if(this.selectedDirectory != null && this.selectedDirectory.isDirectory()) {
            this.directoryTextField.setText(selectedDirectory.getAbsolutePath());
            this.exportButton.setDisable(false);
        }
    }

    @FXML
    public void exportData(){
        Get.executor().execute(new ExportContentAndZipTask(
                this.manifold,
                this.selectedDirectory,
                this.exportTypeChoiceBox.getSelectionModel().getSelectedItem()));
    }

    public void setExportStage(Stage exportStage) {
        this.exportStage = exportStage;
    }

    public void setManifold(Manifold manifold) {
        this.manifold = manifold;
    }

}
