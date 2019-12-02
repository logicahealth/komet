package sh.isaac.komet.preferences.coordinate;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.MetaData;
import sh.isaac.api.ComponentProxy;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.preferences.AbstractPreferences;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.komet.gui.contract.preferences.PreferenceGroup;
import sh.komet.gui.control.PropertySheetTextWrapper;
import sh.komet.gui.control.concept.PropertySheetItemConceptWrapper;
import sh.komet.gui.control.list.PropertySheetListWrapper;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;
import sh.komet.gui.util.UuidStringKey;

import java.util.Optional;
import java.util.UUID;

public class ManifoldCoordinateItemPanel extends AbstractPreferences {
    public enum Keys {
        MANIFOLD_NAME,
        MANIFOLD_GROUP_UUID,
        SELECTED_COMPONENTS,
        HISTORY_RECORDS,
    }


    private final SimpleStringProperty nameProperty
            = new SimpleStringProperty(this, MetaData.MANIFOLD_NAME____SOLOR.toExternalString());

    private final PropertySheetListWrapper<ComponentProxy> selectionWrapper;
    private final PropertySheetListWrapper<ComponentProxy> historyWrapper;

    private final Manifold manifold;

    private final UuidStringKey itemKey;


    public ManifoldCoordinateItemPanel(IsaacPreferences preferencesNode, Manifold manifold, KometPreferencesController kpc) {
        super(preferencesNode, getGroupName(preferencesNode, "Manifold"), manifold, kpc);
        nameProperty.set(groupNameProperty().get());

        this.manifold = manifold;

        revertFields();
        save();
        getItemList().add(new PropertySheetTextWrapper(manifold, nameProperty));

        this.selectionWrapper = new PropertySheetListWrapper(manifold, manifold.manifoldSelectionProperty(),
                () -> new ComponentProxy(TermAux.UNINITIALIZED_COMPONENT_ID.getNid(), TermAux.UNINITIALIZED_COMPONENT_ID.getFullyQualifiedName()), manifold1 -> new ComponentProxyEditorStub(manifold1));


        this.historyWrapper = new PropertySheetListWrapper(manifold, manifold.getHistoryRecords(),
                () -> new ComponentProxy(TermAux.UNINITIALIZED_COMPONENT_ID.getNid(), TermAux.UNINITIALIZED_COMPONENT_ID.getFullyQualifiedName()), manifold1 -> new ComponentProxyEditorStub(manifold1));
        getItemList().add(this.selectionWrapper);
        getItemList().add(this.historyWrapper);


        this.itemKey = new UuidStringKey(UUID.fromString(preferencesNode.name()), nameProperty.get());
        FxGet.manifoldCoordinates().put(itemKey, this.manifold.getManifoldCoordinate());
        nameProperty.addListener((observable, oldValue, newValue) -> {
            groupNameProperty().set(newValue);
            FxGet.languageCoordinates().remove(itemKey);
            itemKey.updateString(newValue);
            FxGet.manifoldCoordinates().put(itemKey, this.manifold.getManifoldCoordinate());
        });
    }

    @Override
    protected void saveFields() {
        getPreferencesNode().put(PreferenceGroup.Keys.GROUP_NAME, this.nameProperty.get());
        getPreferencesNode().putComponentList(Keys.SELECTED_COMPONENTS, manifold.manifoldSelectionProperty());
        getPreferencesNode().putComponentList(Keys.HISTORY_RECORDS, manifold.getHistoryRecords());
    }

    @Override
    protected void revertFields() {
        this.nameProperty.set(getPreferencesNode().get(PreferenceGroup.Keys.GROUP_NAME, getGroupName()));
        if (!manifold.manifoldSelectionProperty().get().equals(getPreferencesNode().getComponentList(Keys.SELECTED_COMPONENTS))) {
            manifold.manifoldSelectionProperty().clear();
            manifold.manifoldSelectionProperty().addAll(getPreferencesNode().getComponentList(Keys.SELECTED_COMPONENTS));
        }
        if (!manifold.getHistoryRecords().get().equals(getPreferencesNode().getComponentList(Keys.HISTORY_RECORDS))) {
            manifold.getHistoryRecords().clear();
            manifold.getHistoryRecords().addAll(getPreferencesNode().getComponentList(Keys.HISTORY_RECORDS));
        }
    }

    private static class ComponentProxyEditorStub implements PropertyEditor<ComponentProxy> {

        private final Manifold manifold;

        ComponentProxy value;

        public ComponentProxyEditorStub(Object manifold) {
            this.manifold = (Manifold) manifold;
        }

        @Override
        public Node getEditor() {
            return new Label("ComponentProxyEditorStub");
        }

        @Override
        public ComponentProxy getValue() {
            return value;
        }

        @Override
        public void setValue(ComponentProxy value) {
            this.value = value;
        }
    }
}
