package sh.isaac.komet.preferences.coordinate;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.MetaData;
import sh.isaac.api.ComponentProxy;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.preferences.AbstractPreferences;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.komet.gui.contract.preferences.PreferenceGroup;
import sh.komet.gui.control.PropertySheetItemObjectListWrapper;
import sh.komet.gui.control.PropertySheetTextWrapper;
import sh.komet.gui.control.component.PropertySheetComponentListWrapper;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;
import sh.komet.gui.util.UuidStringKey;

import java.util.UUID;

public class ManifoldCoordinateItemPanel extends AbstractPreferences implements Runnable {
    public enum Keys {
        MANIFOLD_NAME,
        MANIFOLD_GROUP_UUID,
    }


    private final SimpleStringProperty nameProperty
            = new SimpleStringProperty(this, MetaData.MANIFOLD_NAME____SOLOR.toExternalString());

    private final Manifold manifold;

    private final UuidStringKey itemKey;


    public ManifoldCoordinateItemPanel(IsaacPreferences preferencesNode, Manifold manifold, KometPreferencesController kpc) {
        super(preferencesNode, getGroupName(preferencesNode, "Manifold"), manifold, kpc);

        // replaces the manifold with the manifold specified by the groupNameProperty...
        manifold = Manifold.get(groupNameProperty().get());
        nameProperty.set(groupNameProperty().get());

        this.manifold = manifold;

        getItemList().add(new PropertySheetTextWrapper(manifold, nameProperty));

        ObservableList<PremiseType> premiseTypes =
                FXCollections.observableArrayList(PremiseType.INFERRED, PremiseType.STATED);


        this.itemKey = new UuidStringKey(UUID.fromString(preferencesNode.name()), nameProperty.get());
        FxGet.manifoldCoordinates().put(itemKey, this.manifold);
        FxGet.setManifoldForManifoldCoordinate(itemKey, manifold);
        nameProperty.addListener((observable, oldValue, newValue) -> {
            groupNameProperty().set(newValue);
            FxGet.languageCoordinates().remove(itemKey);
            itemKey.updateString(newValue);
            FxGet.manifoldCoordinates().put(itemKey, this.manifold);
            FxGet.setManifoldForManifoldCoordinate(itemKey, this.manifold);
        });
        this.manifold.setSelectionPreferenceUpdater(this);
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


    private static class ComponentProxyEditorStub implements PropertyEditor<ComponentProxy> {

        private final Manifold manifold;

        ComponentProxy value;

        public ComponentProxyEditorStub(Object manifold) {
            this.manifold = (Manifold) manifold;
        }

        @Override
        public Node getEditor() {
            return new Label("ComponentProxyEditorStub: ");
        }

        @Override
        public ComponentProxy getValue() {
            return value;
        }

        @Override
        public void setValue(ComponentProxy value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value.toString();
        }

    }
}
