package sh.isaac.komet.preferences.coordinate;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import sh.isaac.MetaData;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.api.util.UuidStringKey;
import sh.isaac.komet.preferences.AbstractPreferences;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.komet.gui.contract.preferences.PreferenceGroup;
import sh.komet.gui.control.property.wrapper.PropertySheetTextWrapper;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.util.FxGet;

import java.util.UUID;

public class ManifoldCoordinateItemPanel extends AbstractPreferences implements Runnable {
    public enum Keys {
        MANIFOLD_NAME,
        MANIFOLD_GROUP_UUID,
    }


    private final SimpleStringProperty nameProperty
            = new SimpleStringProperty(this, MetaData.MANIFOLD_NAME____SOLOR.toExternalString());

    private final UuidStringKey itemKey;


    public ManifoldCoordinateItemPanel(IsaacPreferences preferencesNode, ViewProperties viewProperties, KometPreferencesController kpc) {
        super(preferencesNode, getGroupName(preferencesNode, "Manifold"), viewProperties, kpc);

        // replaces the manifold with the manifold specified by the groupNameProperty...
        //viewProperties = Manifold.get(groupNameProperty().get());
        nameProperty.set(groupNameProperty().get());


        getItemList().add(new PropertySheetTextWrapper(viewProperties.getManifoldCoordinate(), nameProperty));

        ObservableList<PremiseType> premiseTypes =
                FXCollections.observableArrayList(PremiseType.INFERRED, PremiseType.STATED);


        this.itemKey = new UuidStringKey(UUID.fromString(preferencesNode.name()), nameProperty.get());
        //FxGet.manifoldCoordinates().put(itemKey, this.manifold);
        //FxGet.setManifoldForManifoldCoordinate(itemKey, viewProperties);
        nameProperty.addListener((observable, oldValue, newValue) -> {
            groupNameProperty().set(newValue);
            FxGet.languageCoordinates().remove(itemKey);
            itemKey.updateString(newValue);
            //FxGet.manifoldCoordinates().put(itemKey, this.manifold);
            //FxGet.setManifoldForManifoldCoordinate(itemKey, this.manifold);
        });
        //this.manifold.setSelectionPreferenceUpdater(this);
        revertFields();
        save();

    }

    @Override
    public void run() {
        save();
    }

    @Override
    protected void saveFields() {
        getPreferencesNode().put(PreferenceGroup.Keys.GROUP_NAME, this.nameProperty.get());
    }

    @Override
    protected void revertFields() {
        this.nameProperty.set(getPreferencesNode().get(PreferenceGroup.Keys.GROUP_NAME, getGroupName()));
    }

}
