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
import sh.isaac.api.coordinate.StampPositionImmutable;
import sh.isaac.model.observable.coordinate.ObservableStampPathImpl;
import sh.komet.gui.control.property.wrapper.PropertySheetPositionListWrapper;
import sh.komet.gui.control.property.wrapper.PropertySheetTextWrapper;
import sh.komet.gui.control.property.PropertyEditorFactory;
import sh.komet.gui.control.property.ViewProperties;

import java.time.ZonedDateTime;

import static sh.isaac.api.util.time.DateTimeUtil.EASY_TO_READ_DATE_TIME_FORMAT;

public class NewPathPanel {
    private final ObservableList<PropertySheet.Item> itemList = FXCollections.observableArrayList();
    {
        itemList.addListener((ListChangeListener.Change<? extends PropertySheet.Item> c) -> {
            makePropertySheet();
        });
    }
    private final ViewProperties viewProperties;

    private final BorderPane propertySheetBorderPane = new BorderPane();

    private final SimpleStringProperty nameProperty
            = new SimpleStringProperty(this, MetaData.PATH_COORDINATE_NAME____SOLOR.toExternalString());

    private final ObservableStampPathImpl pathCoordinateItem;

    public NewPathPanel(ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
        ZonedDateTime nowInMinutes = ZonedDateTime.parse(ZonedDateTime.now().format(EASY_TO_READ_DATE_TIME_FORMAT),
                EASY_TO_READ_DATE_TIME_FORMAT);

        StampPositionImmutable originOnDevelop =
                StampPositionImmutable.make(nowInMinutes.toInstant().toEpochMilli(), TermAux.DEVELOPMENT_PATH);
        this.pathCoordinateItem = ObservableStampPathImpl.make(
                StampPathImmutable.make(TermAux.UNINITIALIZED_COMPONENT_ID, Sets.immutable.of(originOnDevelop)));
        this.nameProperty.set("Feature path");
        this.itemList.add(new PropertySheetTextWrapper("Path name", nameProperty));
        this.itemList.add(new PropertySheetPositionListWrapper("Origins", pathCoordinateItem.pathOriginsAsListPropertyProperty()));
    }

    protected void makePropertySheet() {
        PropertySheet sheet = new PropertySheet();
        sheet.setMode(PropertySheet.Mode.NAME);
        sheet.setSearchBoxVisible(false);
        sheet.setModeSwitcherVisible(false);
        sheet.setPropertyEditorFactory(new PropertyEditorFactory(viewProperties.getManifoldCoordinate()));
        sheet.getItems().addAll(this.itemList);
        this.propertySheetBorderPane.setCenter(sheet);
    }

    public ObservableStampPathImpl getNewPathCoordinate() {
        return pathCoordinateItem;
    }

    public String getNewPathName() {
        return nameProperty.getValue();
    }

    public Node getEditor() {
        return this.propertySheetBorderPane;
    }
}
