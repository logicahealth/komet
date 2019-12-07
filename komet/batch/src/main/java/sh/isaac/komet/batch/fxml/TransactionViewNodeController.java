package sh.isaac.komet.batch.fxml;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
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
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.identity.IdentifiedObject;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.transaction.Transaction;
import sh.komet.gui.drag.drop.DropHelper;
import sh.komet.gui.interfaces.ComponentList;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.row.DragAndDropRowFactory;
import sh.komet.gui.table.version.VersionTable;
import sh.komet.gui.util.FxGet;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

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

    private Manifold manifold;

    private VersionTable versionTable;

    private DropHelper dropHelper;

    private final UUID listId = UUID.randomUUID();

    private Manifold listManifold;

    private StringProperty nameProperty = new SimpleStringProperty(this, "Transaction label", "");

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert batchAnchor != null : "fx:id=\"batchAnchor\" was not injected: check your FXML file 'TransactionViewNode.fxml'.";
        assert batchBorderPane != null : "fx:id=\"batchBorderPane\" was not injected: check your FXML file 'TransactionViewNode.fxml'.";
        assert transactionChoice != null : "fx:id=\"transactionChoice\" was not injected: check your FXML file 'TransactionViewNode.fxml'.";
        this.listManifold = Manifold.get(Manifold.ManifoldGroup.LIST);


        Get.commitService().getPendingTransactionList().addListener(new SetChangeListener<Transaction>() {
            @Override
            public void onChanged(Change<? extends Transaction> change) {
                transactionChoice.getItems().setAll(change.getSet());
            }
        });
    }


    @FXML
    void refreshList(ActionEvent event) {
        // TODO
        Transaction t = transactionChoice.getSelectionModel().getSelectedItem();
        if (t != null) {
            TableView<ObservableChronology> rootNode = versionTable.getRootNode();
            rootNode.getItems().clear();

            for (Integer nid: t.getComponentNidsForTransaction()) {
                rootNode.getItems().add(Get.observableChronology(nid));
            }
        }

    }

    @Override
    public ObservableList<ObservableChronology> getComponents() {
        return versionTable.getRootNode().getItems();
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
                    manifold.manifoldSelectionProperty().remove(new ComponentProxy(remitem.getNid(), remitem.toUserString()));
                }
                for (ObservableChronology additem : c.getAddedSubList()) {
                    manifold.manifoldSelectionProperty().add(new ComponentProxy(additem.getNid(), additem.toUserString()));
                }
            }
        }
    }


    public void setManifold(Manifold manifold) {
        this.manifold = manifold;
        this.versionTable = new VersionTable(manifold);
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
    public UUID getListId() {
        return listId;
    }

}