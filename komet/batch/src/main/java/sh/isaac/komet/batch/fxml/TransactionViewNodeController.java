package sh.isaac.komet.batch.fxml;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import sh.isaac.api.ComponentProxy;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.identity.IdentifiedObject;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.transaction.Transaction;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.drag.drop.DropHelper;
import sh.komet.gui.interfaces.ComponentList;
import sh.komet.gui.row.DragAndDropRowFactory;
import sh.komet.gui.table.version.VersionTable;
import sh.komet.gui.util.FxGet;
import sh.isaac.api.util.UuidStringKey;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.stream.Stream;

import static sh.isaac.komet.batch.TransactionViewFactory.TRANSACTION_VIEW;

public class TransactionViewNodeController implements ComponentList {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private AnchorPane batchAnchor;

    @FXML
    private BorderPane batchBorderPane;

    @FXML
    private ChoiceBox<Transaction> transactionChoice;

    private VersionTable versionTable;

    private DropHelper dropHelper;

    private final UUID listId = UUID.randomUUID();

    private ViewProperties viewProperties;
    private ActivityFeed activityFeed;

    private StringProperty nameProperty = new SimpleStringProperty(this, "Transaction label", "");

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert batchAnchor != null : "fx:id=\"batchAnchor\" was not injected: check your FXML file 'TransactionViewNode.fxml'.";
        assert batchBorderPane != null : "fx:id=\"batchBorderPane\" was not injected: check your FXML file 'TransactionViewNode.fxml'.";
        assert transactionChoice != null : "fx:id=\"transactionChoice\" was not injected: check your FXML file 'TransactionViewNode.fxml'.";

        Get.commitService().getPendingTransactionList().addListener(new SetChangeListener<Transaction>() {
            @Override
            public void onChanged(Change<? extends Transaction> change) {
                Transaction priorSelection = transactionChoice.getSelectionModel().getSelectedItem();
                transactionChoice.getSelectionModel().select(null);
                transactionChoice.getItems().setAll(change.getSet());
                if (priorSelection != null && change.getSet().contains(priorSelection)) {
                    transactionChoice.getSelectionModel().select(priorSelection);
                }
            }
        });

        transactionChoice.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            refreshList();
            if (newValue.getTransactionName().isEmpty()) {
                nameProperty.setValue(TRANSACTION_VIEW);
            } else {
                nameProperty.setValue(newValue.getTransactionName() + " transaction");
            }
        });
    }


    @FXML
    void refreshList(ActionEvent event) {
        refreshList();
    }

    void refreshList() {
        Transaction t = transactionChoice.getSelectionModel().getSelectedItem();
        TableView<ObservableChronology> rootNode = versionTable.getRootNode();
        rootNode.getItems().clear();
        if (t != null) {
            for (Integer nid: t.getComponentNidsForTransaction()) {
                rootNode.getItems().add(Get.observableChronology(nid));
            }
        }
    }

    @Override
    public Stream<Chronology> getComponentStream() {
        return versionTable.getRootNode().getItems().stream().map(observableChronology -> (Chronology) observableChronology);
    }

    @Override
    public Optional<ObservableList<ObservableChronology>>  getOptionalObservableComponentList() {
        return Optional.of(versionTable.getRootNode().getItems());
    }

    public void close() {
        System.out.println("Closing ListViewNodeController");
        FxGet.removeComponentList(this);
    }


    private void selectionChanged(ListChangeListener.Change<? extends ObservableChronology> c) {
        while (c.next()) {
            if (c.wasPermutated()) {
                for (int i = c.getFrom(); i < c.getTo(); ++i) {
                    //nothing to do...
                }
            } else if (c.wasUpdated()) {
                //nothing to do
            } else {
                for (ObservableChronology remitem : c.getRemoved()) {
                    this.activityFeed.feedSelectionProperty().remove(new ComponentProxy(remitem.getNid(), remitem.toUserString()));
                }
                for (ObservableChronology additem : c.getAddedSubList()) {
                    this.activityFeed.feedSelectionProperty().add(new ComponentProxy(additem.getNid(), additem.toUserString()));
                }
            }
        }
        // Check to make sure lists are equal in size/properly synchronized.
        if (this.activityFeed.feedSelectionProperty().size() != c.getList().size()) {
            // lists are out of sync, reset with fresh list.
            ComponentProxy[] selectedItems = new ComponentProxy[c.getList().size()];
            for (int i = 0; i < selectedItems.length; i++) {
                ObservableChronology component = c.getList().get(i);
                selectedItems[i] = new ComponentProxy(component.getNid(), component.toUserString());
            }
            this.activityFeed.feedSelectionProperty().setAll(selectedItems);
        }

    }


    public void setViewProperties(ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
        this.activityFeed = viewProperties.getActivityFeed(ViewProperties.LIST);
        this.versionTable = new VersionTable(viewProperties);
        this.versionTable.getRootNode().getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.versionTable.getRootNode().getSelectionModel().getSelectedItems().addListener(this::selectionChanged);

        DragAndDropRowFactory dragAndDropRowFactory = new DragAndDropRowFactory();
        this.versionTable.getRootNode().setRowFactory(dragAndDropRowFactory);

        this.dropHelper = new DropHelper(versionTable.getRootNode(),
                this::addIdentifiedObject, dragEvent -> true, dragAndDropRowFactory::isDragging);

        this.batchBorderPane.setCenter(this.versionTable.getRootNode());
    }

    private void addIdentifiedObject(IdentifiedObject object) {
        if (object == null) {
            return;
        }
        ObservableChronology chronology;
        if (object instanceof ObservableChronology) {
            chronology = (ObservableChronology) object;
        } else {
            chronology = Get.observableChronology(object.getNid());
        }
        TableView<ObservableChronology> table = versionTable.getRootNode();
        table.getItems().add(chronology);
        table.getSelectionModel().clearAndSelect(table.getItems().size() - 1);
        table.requestFocus();
    }

    @Override
    public StringProperty nameProperty() {
        return nameProperty;
    }

    @Override
    public UuidStringKey getUuidStringKey() {
        return new UuidStringKey(listId, nameProperty().getValue());
    }

    @Override
    public int listSize() {
        return versionTable.getRootNode().getItems().size();
    }


}
