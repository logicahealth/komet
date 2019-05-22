package sh.komet.gui.table.version;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.ObservableVersion;
import sh.komet.gui.cell.table.*;
import sh.komet.gui.manifold.Manifold;

import java.net.URL;
import java.util.ResourceBundle;

public class VersionTableController {

     @FXML // fx:id="assemblageDetailRootPane"
    private BorderPane assemblageDetailRootPane;
    @FXML  // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;
    @FXML  // URL location of the FXML file that was given to the FXMLLoader
    private URL location;
    @FXML  // fx:id="listTable"
    private TableView<ObservableChronology> listTable;
    @FXML  // fx:id="whatColumn"
    private TableColumn<ObservableChronology, ObservableVersion> whatColumn;
    @FXML  // fx:id="generalColumn"
    private TableColumn<ObservableChronology, ObservableVersion> generalColumn;
    @FXML  // fx:id="statusColumn"
    private TableColumn<ObservableChronology, ObservableVersion> statusColumn;
    @FXML  // fx:id="authorTimeColumn"
    private TableColumn<ObservableChronology, ObservableVersion> authorTimeColumn;
    @FXML  // fx:id="timeColumn"
    private TableColumn<ObservableChronology, ObservableVersion> timeColumn;
    @FXML  // fx:id="authorColumn"
    private TableColumn<ObservableChronology, ObservableVersion> authorColumn;
    @FXML  // fx:id="modulePathColumn"
    private TableColumn<ObservableChronology, ObservableVersion> modulePathColumn;
    @FXML  // fx:id="moduleColumn"
    private TableColumn<ObservableChronology, ObservableVersion> moduleColumn;
    @FXML  // fx:id="pathColumn"
    private TableColumn<ObservableChronology, ObservableVersion> pathColumn;
    private Manifold manifold;
    private KometTableCellValueFactory valueFactory;

    //~--- methods -------------------------------------------------------------
    @FXML  // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert listTable != null :
                "fx:id=\"listTable\" was not injected: check your FXML file 'VersionTable.fxml'.";
        assert whatColumn != null :
                "fx:id=\"assemblageExtensionWhat\" was not injected: check your FXML file 'VersionTable.fxml'.";
        assert generalColumn != null :
                "fx:id=\"assemblageExtensionGeneral\" was not injected: check your FXML file 'VersionTable.fxml'.";
        assert statusColumn != null :
                "fx:id=\"assemblageExtensionStatus\" was not injected: check your FXML file 'VersionTable.fxml'.";
        assert timeColumn != null :
                "fx:id=\"assemblageExtensionTime\" was not injected: check your FXML file 'VersionTable.fxml'.";
        assert authorColumn != null :
                "fx:id=\"assemblageExtensionAuthor\" was not injected: check your FXML file 'VersionTable.fxml'.";
        assert modulePathColumn != null : "fx:id=\"modulePathColumn\" was not injected: check your FXML file 'VersionTable.fxml'.";
        assert authorTimeColumn != null : "fx:id=\"authorTimeColumn\" was not injected: check your FXML file 'VersionTable.fxml'.";
        assert moduleColumn != null :
                "fx:id=\"assemblageExtensionModule\" was not injected: check your FXML file 'VersionTable.fxml'.";
        assert pathColumn != null :
                "fx:id=\"assemblageExtensionPath\" was not injected: check your FXML file 'VersionTable.fxml'.";

        authorTimeColumn.setText("author\ntime");
        modulePathColumn.setText("module\npath");
        listTable.setTableMenuButtonVisible(true);
    }

    //~--- get methods ---------------------------------------------------------
    public BorderPane getAssemblageDetailRootPane() {
        return assemblageDetailRootPane;
    }

    public Manifold getManifold() {
        return manifold;
    }

    //~--- set methods ---------------------------------------------------------
    public void setManifold(Manifold manifold) {
        if ((this.manifold != null) && (this.manifold != manifold)) {
            throw new UnsupportedOperationException("Manifold previously set... " + manifold);
        }

        this.manifold = manifold;
        this.valueFactory = new KometTableCellValueFactory(this.manifold);

        whatColumn.setCellValueFactory(this.valueFactory::getCellValue);
        whatColumn.setCellFactory(tableColumn -> new TableWhatCell(this.manifold));

        generalColumn.setCellValueFactory(this.valueFactory::getCellValue);
        generalColumn.setCellFactory(tableColumn -> new TableGeneralCell(this.manifold));

        statusColumn.setCellValueFactory(this.valueFactory::getCellValue);
        statusColumn.setCellFactory(tableColumn -> new TableStatusCell());

        timeColumn.setVisible(false);
        timeColumn.setCellValueFactory(this.valueFactory::getCellValue);
        timeColumn.setCellFactory(tableColumn -> new TableTimeCell());

        modulePathColumn.setCellValueFactory(this.valueFactory::getCellValue);
        modulePathColumn.setCellFactory(tableColumn -> new TableModulePathCell(this.manifold));

        authorTimeColumn.setCellValueFactory(this.valueFactory::getCellValue);
        authorTimeColumn.setCellFactory(tableColumn -> new TableAuthorTimeCell(this.manifold));

        authorColumn.setVisible(false);
        authorColumn.setCellValueFactory(this.valueFactory::getCellValue);
        authorColumn.setCellFactory(tableColumn -> new TableConceptCell(this.manifold, ObservableVersion::getAuthorNid));


        moduleColumn.setVisible(false);
        moduleColumn.setCellValueFactory(this.valueFactory::getCellValue);
        moduleColumn.setCellFactory(tableColumn -> new TableConceptCell(this.manifold, ObservableVersion::getModuleNid));

        pathColumn.setVisible(false);
        pathColumn.setCellValueFactory(this.valueFactory::getCellValue);
        pathColumn.setCellFactory(tableColumn -> new TableConceptCell(this.manifold, ObservableVersion::getPathNid));

    }
}
