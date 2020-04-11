package sh.isaac.komet.preferences.coordinate;

import javafx.beans.property.SimpleStringProperty;
import sh.isaac.MetaData;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.coordinate.Coordinates;
import sh.isaac.api.coordinate.PathCoordinate;
import sh.isaac.api.coordinate.PathCoordinateImmutable;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.preferences.AbstractPreferences;
import sh.isaac.model.observable.coordinate.ObservablePathCoordinateImpl;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.komet.gui.contract.preferences.PreferenceGroup;
import sh.komet.gui.control.PropertySheetTextWrapper;
import sh.komet.gui.control.concept.PropertySheetConceptSetWrapper;
import sh.komet.gui.control.concept.PropertySheetItemConceptWrapper;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;
import sh.komet.gui.util.UuidStringKey;

import java.util.Optional;
import java.util.UUID;
import java.util.prefs.BackingStoreException;

public class PathItemPanel extends AbstractPreferences {
    public enum Keys {
        PATH_COORDINATE_DATA
    }


    private final SimpleStringProperty nameProperty
            = new SimpleStringProperty(this, MetaData.PATH_COORDINATE_NAME____SOLOR.toExternalString());

    private final ObservablePathCoordinateImpl pathCoordinateItem;

    private final UuidStringKey itemKey;

    public PathItemPanel(PathCoordinate pathCoordinate, String coordinateName, IsaacPreferences preferencesNode, Manifold manifold, KometPreferencesController kpc) {
        super(preferencesNode, coordinateName, manifold, kpc);
        this.pathCoordinateItem = ObservablePathCoordinateImpl.make(pathCoordinate.toPathCoordinateImmutable());
        setup(manifold);
        this.itemKey = new UuidStringKey(UUID.fromString(preferencesNode.name()), nameProperty.get());
        FxGet.pathCoordinates().put(itemKey, pathCoordinateItem);
    }

    public PathItemPanel(IsaacPreferences preferencesNode, Manifold manifold, KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(PreferenceGroup.Keys.GROUP_NAME).get(), manifold, kpc);
        Optional<byte[]> optionalBytes = preferencesNode.getByteArray(Keys.PATH_COORDINATE_DATA);
        if (optionalBytes.isPresent()) {
            ByteArrayDataBuffer buffer = new ByteArrayDataBuffer(optionalBytes.get());
            PathCoordinateImmutable pathCoordinate = PathCoordinateImmutable.make(buffer);
            this.pathCoordinateItem = ObservablePathCoordinateImpl.make(pathCoordinate);
        } else {
            setGroupName("Development latest");
            PathCoordinate pathCoordinate = Coordinates.Path.Development();
            this.pathCoordinateItem = ObservablePathCoordinateImpl.make(pathCoordinate.toPathCoordinateImmutable());
        }
        setup(manifold);
        this.itemKey = new UuidStringKey(UUID.fromString(preferencesNode.name()), nameProperty.get());
        FxGet.pathCoordinates().put(itemKey, pathCoordinateItem);
     }

    private void setup(Manifold manifold) {

        nameProperty.set(groupNameProperty().get());
        getItemList().add(new PropertySheetTextWrapper(manifold, nameProperty));
        nameProperty.addListener((observable, oldValue, newValue) -> {
            groupNameProperty().set(newValue);
            FxGet.pathCoordinates().remove(itemKey);
            itemKey.updateString(newValue);
            FxGet.pathCoordinates().put(itemKey, pathCoordinateItem);
        });
        getItemList().add(new PropertySheetItemConceptWrapper(manifold, "Path",
                pathCoordinateItem.pathConceptProperty(), TermAux.DEVELOPMENT_PATH, TermAux.MASTER_PATH));
        getItemList().add(new PropertySheetConceptSetWrapper(manifold, pathCoordinateItem.moduleSpecificationsProperty()));
        revert();
        save();
    }

    @Override
    protected void saveFields() throws BackingStoreException {
        getPreferencesNode().put(PreferenceGroup.Keys.GROUP_NAME, this.nameProperty.get());
        ByteArrayDataBuffer buff = new ByteArrayDataBuffer();
        this.pathCoordinateItem.getValue().marshal(buff);
        buff.trimToSize();
        getPreferencesNode().putByteArray(Keys.PATH_COORDINATE_DATA, buff.getData());
    }

    @Override
    protected void revertFields() throws BackingStoreException {
        this.nameProperty.set(getPreferencesNode().get(PreferenceGroup.Keys.GROUP_NAME, getGroupName()));
        ByteArrayDataBuffer revertbuffer = new ByteArrayDataBuffer();
        this.pathCoordinateItem.getPathCoordinate().toPathCoordinateImmutable().marshal(revertbuffer);
        revertbuffer.trimToSize();
        byte[] data = getPreferencesNode().getByteArray(Keys.PATH_COORDINATE_DATA, revertbuffer.getData());
        ByteArrayDataBuffer buffer = new ByteArrayDataBuffer(data);
        PathCoordinateImmutable pathCoordinate = PathCoordinateImmutable.make(buffer);
        this.pathCoordinateItem.setValue(pathCoordinate);
    }
}