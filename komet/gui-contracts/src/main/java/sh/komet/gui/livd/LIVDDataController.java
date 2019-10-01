/**
 * Sample Skeleton for 'ExportSpecification.fxml' Controller Class
 */

package sh.komet.gui.livd;

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
import javafx.stage.Window;
import org.controlsfx.control.PropertySheet;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.solor.sof.SofExporter;
import sh.komet.gui.control.concept.PropertySheetConceptSetWrapper;
import sh.komet.gui.control.property.PropertyEditorFactory;
import sh.komet.gui.manifold.Manifold;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;

public class LIVDDataController {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="topBorderPane"
    private BorderPane topBorderPane; // Value injected by FXMLLoader

    @FXML // fx:id="exportFormat"
    private ChoiceBox<sh.komet.gui.exportation.ExportFormatType> exportFormat; // Value injected by FXMLLoader

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

    public LIVDDataController() {

    }




    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {

    }

    public void setManifold(Manifold manifold) {

    }
}

