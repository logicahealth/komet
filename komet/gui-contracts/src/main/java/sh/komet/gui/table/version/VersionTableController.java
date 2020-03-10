package sh.komet.gui.table.version;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import sh.isaac.api.Get;
import sh.isaac.api.identity.IdentifiedObject;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.util.UUIDUtil;
import sh.komet.gui.cell.CellHelper;
import sh.komet.gui.cell.table.*;
import sh.komet.gui.clipboard.ClipboardHelper;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;

import javax.sound.sampled.Clip;
import java.net.URL;
import java.util.*;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;
import static sh.komet.gui.clipboard.ClipboardHelper.getUuidsFromClipboard;

public class VersionTableController {
    private static final KeyCodeCombination keyCodePaste = new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN);
    private static final KeyCodeCombination keyCodeCopy = new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN);

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
    private final ContextMenu copyPasteMenu = new ContextMenu();
    private final MenuItem copySelectedItems = new MenuItem("Copy selected items");
    private final MenuItem pasteClipboard = new MenuItem("Paste clipboard");
    {
        // add menu items to menu
        copySelectedItems.setOnAction(this::copySelectionToClipboard);
        pasteClipboard.setOnAction(this::pasteClipboard);
        copyPasteMenu.getItems().add(pasteClipboard);
        copyPasteMenu.getItems().add(copySelectedItems);
    }

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

        listTable.setContextMenu(copyPasteMenu);

        listTable.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (listTable.getVisibleLeafColumns().size() == 2) {
                this.generalColumn.setMaxWidth(newValue.doubleValue() - 20 - whatColumn.getWidth());
                this.generalColumn.setMinWidth(newValue.doubleValue() - 20 - whatColumn.getWidth());
                this.generalColumn.setPrefWidth(newValue.doubleValue() - 20 - whatColumn.getWidth());
            } else {
                this.generalColumn.setMaxWidth(newValue.doubleValue() - 20 -  - whatColumn.getWidth());
                this.generalColumn.setMinWidth(100);
                this.generalColumn.setPrefWidth(USE_COMPUTED_SIZE);
            }
        });

        generalColumn.setComparator((o1, o2) -> {
            return CellHelper.getTextForComponent(this.getManifold(), o1)
                    .compareTo(CellHelper.getTextForComponent(this.getManifold(), o2));
        });

        listTable.setOnKeyPressed(event -> {
            if (keyCodePaste.match(event)) {
                pasteClipboard(event);
            }
            if (keyCodeCopy.match(event)) {
                copySelectionToClipboard(event);
            }
        });
    }

    public void pasteClipboard(Event event) {
        List<UUID> uuids = ClipboardHelper.getUuidsFromClipboard();
        for (UUID uuid: uuids) {
            listTable.getItems().add(Get.observableChronology(uuid));
        }
        event.consume();
    }

    public void deleteSelectedItems(ActionEvent event) {
        final TreeSet<Integer> rows = new TreeSet<>();
        for (final TablePosition tablePosition : listTable.getSelectionModel().getSelectedCells()) {
            rows.add(tablePosition.getRow());
        }
        Iterator<Integer> itr = rows.descendingIterator();
        while (itr.hasNext()) {
            int rowToDelete = itr.next();
            listTable.getItems().remove(rowToDelete);
        }
    }

    public void copySelectionToClipboard(Event event) {
        final Set<Integer> rows = new TreeSet<>();
        for (final TablePosition tablePosition : listTable.getSelectionModel().getSelectedCells()) {
            rows.add(tablePosition.getRow());
        }
        ArrayList<IdentifiedObject> objects = new ArrayList<>();
        for (final Integer row : rows) {
            objects.add((IdentifiedObject) listTable.getItems().get(row));
        }
        ClipboardHelper.copyToClipboard(objects);
        event.consume();
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

    public void setWhatColumnVisible(boolean value) {
        whatColumn.setVisible(value);
    }
    public void setStatusColumnVisible(boolean value) {
        statusColumn.setVisible(value);
    }
    public void setTimeColumnVisible(boolean value) {
        timeColumn.setVisible(value);
    }
    public void setModulePathColumnVisible(boolean value) {
        modulePathColumn.setVisible(value);
    }
    public void setAuthorTimeColumnVisible(boolean value) {
        authorTimeColumn.setVisible(value);
    }
    public void setAuthorColumnVisible(boolean value) {
        authorColumn.setVisible(value);
    }
    public void setModuleColumnVisible(boolean value) {
        moduleColumn.setVisible(value);
    }
    public void setPathColumnVisible(boolean value) {
        pathColumn.setVisible(value);
    }
}
