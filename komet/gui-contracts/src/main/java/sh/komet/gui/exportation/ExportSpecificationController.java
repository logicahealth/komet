/**
 * Sample Skeleton for 'ExportSpecification.fxml' Controller Class
 */

package sh.komet.gui.exportation;

import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.solor.sof.SofExporter;
import sh.komet.gui.control.concept.PropertySheetConceptSetWrapper;
import sh.komet.gui.control.property.PropertyEditorFactory;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;

public class ExportSpecificationController {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="topBorderPane"
    private BorderPane topBorderPane; // Value injected by FXMLLoader

    @FXML // fx:id="exportFormat"
    private ChoiceBox<ExportFormatType> exportFormat; // Value injected by FXMLLoader

    @FXML // fx:id="selectedFileLabel"
    private Label selectedFileLabel; // Value injected by FXMLLoader

    private final ObservableList<PropertySheet.Item> itemList = FXCollections.observableArrayList();

    final ObservableSet<ConceptSpecification> moduleConcepts = FXCollections.observableSet(new HashSet<>());
    final SimpleSetProperty<ConceptSpecification> moduleConceptProperty = new SimpleSetProperty(this,
            ObservableFields.MODULE_FOR_USER.toExternalString(),
            moduleConcepts);
    private PropertySheetConceptSetWrapper moduleConceptsWrapper;

    final ObservableSet<ConceptSpecification> pathConcepts = FXCollections.observableSet(new HashSet<>());
    final SimpleSetProperty<ConceptSpecification> pathConceptProperty = new SimpleSetProperty(this,
            ObservableFields.PATH_FOR_USER.toExternalString(), pathConcepts);
    private PropertySheetConceptSetWrapper pathConceptsWrapper;

    public ExportSpecificationController() {
        this.moduleConcepts.add(TermAux.SOLOR_OVERLAY_MODULE);

        this.pathConcepts.add(TermAux.MASTER_PATH);
        this.pathConcepts.add(TermAux.DEVELOPMENT_PATH);
    }

    @FXML
    void doExport(MouseEvent event) {
        SofExporter exporter = new SofExporter(moduleConcepts, pathConcepts, new File(selectedFileLabel.getText()));
        Get.executor().execute(exporter);
    }
    @FXML
    void selectFile(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(FxGet.solorDirectory());
        chooser.setInitialFileName("export.sof");
        chooser.setTitle("Specify file for export...");
        File chosen = chooser.showSaveDialog(topBorderPane.getScene().getWindow());
        if (chosen != null) {
            selectedFileLabel.setText(chosen.getAbsolutePath());
        }
    }


    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert topBorderPane != null : "fx:id=\"topBorderPane\" was not injected: check your FXML file 'ExportSpecification.fxml'.";
        this.exportFormat.getItems().addAll(ExportFormatType.values());
        this.exportFormat.getSelectionModel().select(ExportFormatType.SOF);
    }

    public void setManifold(Manifold manifold) {
        this.moduleConceptsWrapper = new PropertySheetConceptSetWrapper(manifold, moduleConceptProperty);
        this.pathConceptsWrapper = new PropertySheetConceptSetWrapper(manifold, pathConceptProperty);
        this.itemList.add(this.moduleConceptsWrapper);
        this.itemList.add(this.pathConceptsWrapper);
        PropertySheet sheet = new PropertySheet();
        sheet.setMode(PropertySheet.Mode.NAME);
        sheet.setSearchBoxVisible(false);
        sheet.setModeSwitcherVisible(false);
        sheet.setPropertyEditorFactory(new PropertyEditorFactory(manifold));
        sheet.getItems().addAll(itemList);
        topBorderPane.setCenter(sheet);
    }
}
