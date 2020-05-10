package sh.isaac.komet.preferences.paths;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.PropertySheet;
import org.eclipse.collections.api.factory.Sets;
import sh.isaac.MetaData;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.coordinate.StampPathImmutable;
import sh.isaac.model.observable.coordinate.ObservableStampPathImpl;
import sh.komet.gui.control.PropertySheetPositionListWrapper;
import sh.komet.gui.control.PropertySheetTextWrapper;
import sh.komet.gui.control.property.PropertyEditorFactory;
import sh.komet.gui.manifold.Manifold;

public class NewPathPanel {
    private final ObservableList<PropertySheet.Item> itemList = FXCollections.observableArrayList();
    {
        itemList.addListener((ListChangeListener.Change<? extends PropertySheet.Item> c) -> {
            makePropertySheet();
        });
    }
    private final Manifold manifold;

    private final BorderPane propertySheetBorderPane = new BorderPane();

    private final SimpleStringProperty nameProperty
            = new SimpleStringProperty(this, MetaData.PATH_COORDINATE_NAME____SOLOR.toExternalString());

    private final ObservableStampPathImpl pathCoordinateItem;

    public NewPathPanel(Manifold manifold) {
        this.manifold = manifold;
        this.pathCoordinateItem = ObservableStampPathImpl.make(StampPathImmutable.make(TermAux.UNINITIALIZED_COMPONENT_ID, Sets.immutable.empty()));
        this.nameProperty.set("");
        this.itemList.add(new PropertySheetTextWrapper("Path name", nameProperty));
        this.itemList.add(new PropertySheetPositionListWrapper("Origins", pathCoordinateItem.pathOriginsAsListProperty()));
    }

    protected void makePropertySheet() {
        PropertySheet sheet = new PropertySheet();
        sheet.setMode(PropertySheet.Mode.NAME);
        sheet.setSearchBoxVisible(false);
        sheet.setModeSwitcherVisible(false);
        sheet.setPropertyEditorFactory(new PropertyEditorFactory(manifold));
        sheet.getItems().addAll(this.itemList);
        this.propertySheetBorderPane.setCenter(sheet);
    }

    public Node getEditor() {
        return this.propertySheetBorderPane;
    }
}
