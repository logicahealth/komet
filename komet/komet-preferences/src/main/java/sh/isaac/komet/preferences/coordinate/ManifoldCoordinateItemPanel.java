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
import sh.komet.gui.control.concept.PropertySheetConceptListWrapper;
import sh.komet.gui.control.list.PropertySheetListWrapper;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;
import sh.komet.gui.util.UuidStringKey;

import java.util.UUID;

public class ManifoldCoordinateItemPanel extends AbstractPreferences implements Runnable {
    public enum Keys {
        MANIFOLD_NAME,
        MANIFOLD_GROUP_UUID,
        PREMISE_TYPE_KEY,
        STAMP_FOR_ORIGIN_KEY,
        STAMP_FOR_DESTINATION_KEY,
        LANGUAGE_COORDINATE_KEY,
        LOGIC_COORDINATE_KEY,
        SELECTED_COMPONENTS,
        HISTORY_RECORDS,
    }


    private final SimpleStringProperty nameProperty
            = new SimpleStringProperty(this, MetaData.MANIFOLD_NAME____SOLOR.toExternalString());

    private final SimpleObjectProperty<UuidStringKey> originStampCoordinateKeyProperty = new SimpleObjectProperty<>(this, TermAux.ORIGIN_STAMP_COORDINATE_KEY_FOR_MANIFOLD.toExternalString());
    private final PropertySheetItemObjectListWrapper<UuidStringKey> originStampCoordinateKeyWrapper;
    private final SimpleObjectProperty<UuidStringKey> destinationStampCoordinateKeyProperty = new SimpleObjectProperty<>(this, TermAux.DESTINATION_STAMP_COORDINATE_KEY_FOR_MANIFOLD.toExternalString());
    private final PropertySheetItemObjectListWrapper<UuidStringKey> destinationStampCoordinateKeyWrapper;

    private final SimpleObjectProperty<UuidStringKey> languageCoordinateKeyProperty = new SimpleObjectProperty<>(this, TermAux.LANGUAGE_COORDINATE_KEY_FOR_MANIFOLD.toExternalString());
    private final PropertySheetItemObjectListWrapper<UuidStringKey> languageCoordinateKeyWrapper;

    private final SimpleObjectProperty<UuidStringKey> logicCoordinateKeyProperty = new SimpleObjectProperty<>(this, TermAux.LOGIC_COORDINATE_KEY_FOR_MANIFOLD.toExternalString());
    private final PropertySheetItemObjectListWrapper<UuidStringKey> logicCoordinateKeyWrapper;

    private final PropertySheetListWrapper<ComponentProxy> selectionWrapper;
    private final PropertySheetListWrapper<ComponentProxy> historyWrapper;

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

        getItemList().add(new PropertySheetItemObjectListWrapper("Premise type", manifold.getManifoldCoordinate().taxonomyPremiseTypeProperty(),
                premiseTypes));

        this.originStampCoordinateKeyWrapper = new PropertySheetItemObjectListWrapper("STAMP for origin", originStampCoordinateKeyProperty, FxGet.stampCoordinateKeys());
        getItemList().add(this.originStampCoordinateKeyWrapper);

        this.destinationStampCoordinateKeyWrapper = new PropertySheetItemObjectListWrapper("STAMP for destination", destinationStampCoordinateKeyProperty, FxGet.stampCoordinateKeys());
        getItemList().add(destinationStampCoordinateKeyWrapper);

        this.languageCoordinateKeyWrapper = new PropertySheetItemObjectListWrapper("Language coordinate", languageCoordinateKeyProperty, FxGet.languageCoordinateKeys());
        getItemList().add(this.languageCoordinateKeyWrapper);

        this.logicCoordinateKeyWrapper = new PropertySheetItemObjectListWrapper("Logic coordinate", logicCoordinateKeyProperty, FxGet.logicCoordinateKeys());
        getItemList().add(this.logicCoordinateKeyWrapper);

        getItemList().add(new PropertySheetComponentListWrapper(manifold, manifold.manifoldSelectionProperty()));

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
        this.manifold.manifoldSelectionProperty().setAll(getPreferencesNode().getComponentList(Keys.SELECTED_COMPONENTS));
        this.manifold.setSelectionPreferenceUpdater(this);
        revertFields();
        save();
        if (originStampCoordinateKeyProperty.get() != null) {
            this.manifold.getManifoldCoordinate().stampCoordinateProperty().setValue(FxGet.stampCoordinates().get(originStampCoordinateKeyProperty.get()));
        } else {
            this.manifold.getManifoldCoordinate().stampCoordinateProperty().setValue(null);
        }
        if (destinationStampCoordinateKeyProperty.get() != null) {
            this.manifold.getManifoldCoordinate().destinationStampCoordinateProperty().setValue(FxGet.stampCoordinates().get(destinationStampCoordinateKeyProperty.get()));
        } else {
            this.manifold.getManifoldCoordinate().destinationStampCoordinateProperty().setValue(null);
        }
        if (languageCoordinateKeyProperty.get() != null) {
            this.manifold.getManifoldCoordinate().languageCoordinateProperty().setValue(FxGet.languageCoordinates().get(languageCoordinateKeyProperty.get()));
        } else {
            this.manifold.getManifoldCoordinate().languageCoordinateProperty().setValue(null);
        }
        if (logicCoordinateKeyProperty.get() != null) {
            this.manifold.getManifoldCoordinate().logicCoordinateProperty().setValue(FxGet.logicCoordinates().get(logicCoordinateKeyProperty.get()));
        } else {
            this.manifold.getManifoldCoordinate().logicCoordinateProperty().setValue(null);
        }
        originStampCoordinateKeyProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.manifold.getManifoldCoordinate().stampCoordinateProperty().setValue(FxGet.stampCoordinates().get(newValue));
            } else {
                this.manifold.getManifoldCoordinate().stampCoordinateProperty().setValue(null);
            }
        });
        destinationStampCoordinateKeyProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.manifold.getManifoldCoordinate().destinationStampCoordinateProperty().setValue(FxGet.stampCoordinates().get(newValue));
            } else {
                this.manifold.getManifoldCoordinate().destinationStampCoordinateProperty().setValue(null);
            }
        });
        languageCoordinateKeyProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.manifold.getManifoldCoordinate().languageCoordinateProperty().setValue(FxGet.languageCoordinates().get(newValue));
            } else {
                this.manifold.getManifoldCoordinate().languageCoordinateProperty().setValue(null);
            }
        });
        logicCoordinateKeyProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.manifold.getManifoldCoordinate().logicCoordinateProperty().setValue(FxGet.logicCoordinates().get(newValue));
            } else {
                this.manifold.getManifoldCoordinate().logicCoordinateProperty().setValue(null);
            }
        });

    }

    @Override
    public void run() {
        save();
    }

    @Override
    protected void saveFields() {
        getPreferencesNode().put(PreferenceGroup.Keys.GROUP_NAME, this.nameProperty.get());
        getPreferencesNode().put(Keys.PREMISE_TYPE_KEY, manifold.getManifoldCoordinate().getTaxonomyPremiseType().name());
        if (originStampCoordinateKeyProperty.get() != null) {
            getPreferencesNode().putArray(Keys.STAMP_FOR_ORIGIN_KEY, originStampCoordinateKeyProperty.get().toStringArray());
        }
        if (destinationStampCoordinateKeyProperty.get() != null) {
            getPreferencesNode().putArray(Keys.STAMP_FOR_DESTINATION_KEY, destinationStampCoordinateKeyProperty.get().toStringArray());
        }
        if (languageCoordinateKeyProperty.get() != null) {
            getPreferencesNode().putArray(Keys.LANGUAGE_COORDINATE_KEY, languageCoordinateKeyProperty.get().toStringArray());
        }
        if (logicCoordinateKeyProperty.get() != null) {
            getPreferencesNode().putArray(Keys.LOGIC_COORDINATE_KEY, logicCoordinateKeyProperty.get().toStringArray());
        }


        getPreferencesNode().putComponentList(Keys.SELECTED_COMPONENTS, manifold.manifoldSelectionProperty());
        getPreferencesNode().putComponentList(Keys.HISTORY_RECORDS, manifold.getHistoryRecords());
    }

    @Override
    protected void revertFields() {
        this.nameProperty.set(getPreferencesNode().get(PreferenceGroup.Keys.GROUP_NAME, getGroupName()));
        this.manifold.getManifoldCoordinate().taxonomyPremiseTypeProperty()
                .setValue(PremiseType.valueOf(getPreferencesNode().get(Keys.PREMISE_TYPE_KEY, PremiseType.INFERRED.name())));

        if (getPreferencesNode().hasKey(Keys.STAMP_FOR_ORIGIN_KEY)) {
            this.originStampCoordinateKeyProperty.setValue(new UuidStringKey(getPreferencesNode().getArray(Keys.STAMP_FOR_ORIGIN_KEY)));
            this.originStampCoordinateKeyWrapper.setValue(this.originStampCoordinateKeyProperty.get());
        }
        if (getPreferencesNode().hasKey(Keys.STAMP_FOR_DESTINATION_KEY)) {
            this.destinationStampCoordinateKeyProperty.setValue(new UuidStringKey(getPreferencesNode().getArray(Keys.STAMP_FOR_DESTINATION_KEY)));
            this.destinationStampCoordinateKeyWrapper.setValue(this.destinationStampCoordinateKeyProperty.get());
        }
        if (getPreferencesNode().hasKey(Keys.LANGUAGE_COORDINATE_KEY)) {
            this.languageCoordinateKeyProperty.setValue(new UuidStringKey(getPreferencesNode().getArray(Keys.LANGUAGE_COORDINATE_KEY)));
            this.languageCoordinateKeyWrapper.setValue(this.languageCoordinateKeyProperty.get());
        }
        if (getPreferencesNode().hasKey(Keys.LOGIC_COORDINATE_KEY)) {
            this.logicCoordinateKeyProperty.setValue(new UuidStringKey(getPreferencesNode().getArray(Keys.LOGIC_COORDINATE_KEY)));
            this.logicCoordinateKeyWrapper.setValue(this.logicCoordinateKeyProperty.get());
        }

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
