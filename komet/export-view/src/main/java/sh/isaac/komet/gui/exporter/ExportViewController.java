package sh.isaac.komet.gui.exporter;

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
import org.controlsfx.control.CheckListView;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.solor.rf2.RF2DirectExporter;
import sh.komet.gui.exportation.ExportFormatType;
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
    private ChoiceBox<ExportFormatType> exportTypeChoiceBox;
    @FXML
    private TextField directoryTextField;
    @FXML
    private CheckListView<String> manufacturerCheckListView;
    @FXML
    private CheckListView<String> modelCheckedListView;
    @FXML
    private CheckListView<String> analyteCheckedListView;
    @FXML
    private CheckListView<String> specimenCheckListView;

    private final ObservableList<ExportFormatType> exportFormatTypes = FXCollections.observableArrayList();
    private File selectedDirectory;

    @FXML
    void initialize(){
        assert directoryBrowseButton != null : "fx:id=\"directoryBrowseButton\" was not injected: check your FXML file 'ExportView.fxml'.";
        assert exportButton != null: "fx:id=\"exportButton\" was not injected: check your FXML file 'ExportView.fxml'.";
        assert exportTypeChoiceBox != null : "fx:id=\"exportTypeChoiceBox\" was not injected: check your FXML file 'ExportView.fxml'.";
        assert directoryTextField != null : "fx:id=\"directoryTextField\" was not injected: check your FXML file 'ExportView.fxml'.";


        this.exportFormatTypes.addAll(ExportFormatType.values());
        this.exportTypeChoiceBox.setItems(this.exportFormatTypes);
        this.exportButton.setDisable(true);
        this.exportTypeChoiceBox.getSelectionModel().selectFirst();

        Get.assemblageService().getSemanticNidStream(MetaData.LIVD_ASSEMBLAGE____SOLOR.getNid())
                .forEach(nid -> {

//                    System.out.println("break");


        });

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
        switch(this.exportTypeChoiceBox.getSelectionModel().getSelectedItem()){
            case RF2:
                Get.executor().execute(new RF2DirectExporter(
                        this.manifold,
                        this.selectedDirectory,
                        this.exportTypeChoiceBox.getSelectionModel().getSelectedItem().toString()
                ));
                break;
            case SRF:
            case SOF:
            default :
                throw new RuntimeException("Unsupported type");
        }
        this.exportStage.close();
    }

    public void setExportStage(Stage exportStage) {
        this.exportStage = exportStage;
    }

    public void setManifold(Manifold manifold) {
        this.manifold = manifold;
    }

}
