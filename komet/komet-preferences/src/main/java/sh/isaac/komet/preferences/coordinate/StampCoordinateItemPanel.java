package sh.isaac.komet.preferences.coordinate;

import javafx.beans.property.SimpleStringProperty;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.observable.coordinate.ObservableStampCoordinate;
import sh.isaac.api.observable.coordinate.ObservableStampPosition;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.preferences.AbstractPreferences;
import sh.isaac.model.coordinate.StampCoordinateImpl;
import sh.isaac.model.observable.coordinate.ObservableStampCoordinateImpl;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.komet.gui.contract.preferences.PreferenceGroup;
import sh.komet.gui.control.PropertySheetItemDateTimeWrapper;
import sh.komet.gui.control.PropertySheetStampPrecedenceWrapper;
import sh.komet.gui.control.PropertySheetStatusSetWrapper;
import sh.komet.gui.control.PropertySheetTextWrapper;
import sh.komet.gui.control.concept.PropertySheetConceptListWrapper;
import sh.komet.gui.control.concept.PropertySheetConceptSetWrapper;
import sh.komet.gui.control.concept.PropertySheetItemConceptWrapper;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;
import sh.komet.gui.util.UuidStringKey;

import java.util.Optional;
import java.util.UUID;
import java.util.prefs.BackingStoreException;

public class StampCoordinateItemPanel extends AbstractPreferences {
    public enum Keys {
        STAMP_COORDINATE_DATA
    }


    private final SimpleStringProperty nameProperty
            = new SimpleStringProperty(this, MetaData.STAMP_COORDINATE_NAME____SOLOR.toExternalString());

    private final ObservableStampCoordinate stampCoordinateItem;

    private final UuidStringKey itemKey;

    public StampCoordinateItemPanel(StampCoordinate stampCoordinate, String coordinateName, IsaacPreferences preferencesNode, Manifold manifold, KometPreferencesController kpc) {
        super(preferencesNode, coordinateName, manifold, kpc);
        if (stampCoordinate instanceof ManifoldCoordinate) {
            stampCoordinate = ((ManifoldCoordinate) stampCoordinate).getStampCoordinate();
        }
        if (stampCoordinate instanceof ObservableStampCoordinate) {
            stampCoordinate = ((ObservableStampCoordinate) stampCoordinate).getStampCoordinate();
        }
        this.stampCoordinateItem = new ObservableStampCoordinateImpl(stampCoordinate.deepClone());
        setup(manifold);
        this.itemKey = new UuidStringKey(UUID.fromString(preferencesNode.name()), nameProperty.get());
        FxGet.stampCoordinates().put(itemKey, stampCoordinateItem);
    }

    public StampCoordinateItemPanel(IsaacPreferences preferencesNode, Manifold manifold, KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(PreferenceGroup.Keys.GROUP_NAME).get(), manifold, kpc);
        Optional<byte[]> optionalBytes = preferencesNode.getByteArray(Keys.STAMP_COORDINATE_DATA);
        if (optionalBytes.isPresent()) {
            ByteArrayDataBuffer buffer = new ByteArrayDataBuffer(optionalBytes.get());
            StampCoordinateImpl stampCoordinate = StampCoordinateImpl.make(buffer);
            this.stampCoordinateItem = new ObservableStampCoordinateImpl(stampCoordinate);
        } else {
            setGroupName("Development latest");
            StampCoordinate stampCoordinate = Get.coordinateFactory().createDevelopmentLatestStampCoordinate();
            this.stampCoordinateItem = new ObservableStampCoordinateImpl(stampCoordinate.deepClone());
        }
        setup(manifold);
        this.itemKey = new UuidStringKey(UUID.fromString(preferencesNode.name()), nameProperty.get());
        FxGet.stampCoordinates().put(itemKey, stampCoordinateItem);
     }

    private void setup(Manifold manifold) {

        nameProperty.set(groupNameProperty().get());
        getItemList().add(new PropertySheetTextWrapper(manifold, nameProperty));
        nameProperty.addListener((observable, oldValue, newValue) -> {
            groupNameProperty().set(newValue);
            FxGet.stampCoordinates().remove(itemKey);
            itemKey.updateString(newValue);
            FxGet.stampCoordinates().put(itemKey, stampCoordinateItem);
        });

        getItemList().add(new PropertySheetStatusSetWrapper(manifold, stampCoordinateItem.allowedStatesProperty()));
        ObservableStampPosition stampPosition = stampCoordinateItem.stampPositionProperty().get();
        getItemList().add(new PropertySheetItemConceptWrapper(manifold, "Path",
                stampPosition.stampPathConceptSpecificationProperty(), TermAux.DEVELOPMENT_PATH, TermAux.MASTER_PATH));
        getItemList().add(new PropertySheetItemDateTimeWrapper("Time", stampPosition.timeProperty()));
        getItemList().add(new PropertySheetStampPrecedenceWrapper("Precedence", stampCoordinateItem.stampPrecedenceProperty()));
        getItemList().add(new PropertySheetConceptSetWrapper(manifold, stampCoordinateItem.moduleSpecificationsProperty()));
        getItemList().add(new PropertySheetConceptListWrapper(manifold, stampCoordinateItem.modulePreferenceListForVersionsProperty()));
        getItemList().add(new PropertySheetConceptSetWrapper(manifold, stampCoordinateItem.authorSpecificationsProperty()));
        revert();
        save();
    }

    @Override
    protected void saveFields() throws BackingStoreException {
        getPreferencesNode().put(PreferenceGroup.Keys.GROUP_NAME, this.nameProperty.get());
        ByteArrayDataBuffer buff = new ByteArrayDataBuffer();
        this.stampCoordinateItem.putExternal(buff);
        buff.trimToSize();
        getPreferencesNode().putByteArray(Keys.STAMP_COORDINATE_DATA, buff.getData());
    }

    @Override
    protected void revertFields() throws BackingStoreException {
        this.nameProperty.set(getPreferencesNode().get(PreferenceGroup.Keys.GROUP_NAME, getGroupName()));
        ByteArrayDataBuffer revertbuffer = new ByteArrayDataBuffer();
        this.stampCoordinateItem.getStampCoordinate().putExternal(revertbuffer);
        revertbuffer.trimToSize();
        byte[] data = getPreferencesNode().getByteArray(Keys.STAMP_COORDINATE_DATA, revertbuffer.getData());
        ByteArrayDataBuffer buffer = new ByteArrayDataBuffer(data);
        StampCoordinateImpl stampCoordinate = StampCoordinateImpl.make(buffer);
        if (!stampCoordinate.getStampCoordinateUuid().equals(this.stampCoordinateItem.getStampCoordinateUuid())) {
            this.stampCoordinateItem.allowedStatesProperty().getValue().clear();
            this.stampCoordinateItem.allowedStatesProperty().getValue().addAll(stampCoordinate.getAllowedStates());

            this.stampCoordinateItem.authorSpecificationsProperty().getValue().clear();
            this.stampCoordinateItem.authorSpecificationsProperty().getValue().addAll(stampCoordinate.getAuthorSpecifications());

            this.stampCoordinateItem.modulePreferenceListForVersionsProperty().clear();
            this.stampCoordinateItem.modulePreferenceListForVersionsProperty().addAll(stampCoordinate.getModulePreferenceOrderForVersions());

            this.stampCoordinateItem.moduleSpecificationsProperty().clear();
            this.stampCoordinateItem.moduleSpecificationsProperty().addAll(stampCoordinate.getModuleSpecifications());

            this.stampCoordinateItem.stampPositionProperty().get().stampPathConceptSpecificationProperty().setValue(stampCoordinate.getStampPosition().getStampPathSpecification());
            this.stampCoordinateItem.stampPositionProperty().get().timeProperty().setValue(stampCoordinate.getStampPosition().getTime());

            this.stampCoordinateItem.stampPrecedenceProperty().setValue(stampCoordinate.getStampPrecedence());
        }
    }
}