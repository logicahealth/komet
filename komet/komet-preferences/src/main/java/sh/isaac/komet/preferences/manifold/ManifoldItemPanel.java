package sh.isaac.komet.preferences.manifold;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.MetaData;
import sh.isaac.api.ComponentProxy;
import sh.isaac.api.Get;
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

import java.util.Optional;
import java.util.UUID;

public class ManifoldItemPanel extends AbstractPreferences {
    public enum Keys {
        MANIFOLD_NAME,
        MANIFOLD_GROUP_UUID,
        FOCUSED_CONCEPT,
        HISTORY_RECORDS,
    }


    private final SimpleStringProperty nameProperty
            = new SimpleStringProperty(this, MetaData.MANIFOLD_NAME____SOLOR.toExternalString());

    private final PropertySheetItemConceptWrapper focusWrapper;
    private final PropertySheetListWrapper<ComponentProxy> historyWrapper;

    private final Manifold manifoldItem;

    public ManifoldItemPanel(IsaacPreferences preferencesNode, Manifold manifold, KometPreferencesController kpc) {
        super(preferencesNode, getGroupName(preferencesNode, "Manifold"), manifold, kpc);
        nameProperty.set(groupNameProperty().get());
        nameProperty.addListener((observable, oldValue, newValue) -> {
            groupNameProperty().set(newValue);
        });

        this.manifoldItem = FxGet.manifold(
                Manifold.ManifoldGroup.getFromGroupUuid(
                        UUID.fromString(preferencesNode.name())
                ).get());

        revertFields();
        save();
        getItemList().add(new PropertySheetTextWrapper(manifold, nameProperty));
        this.focusWrapper = new PropertySheetItemConceptWrapper(manifold, "Manifold focus", manifoldItem.focusedConceptProperty());
        this.historyWrapper = new PropertySheetListWrapper(manifold, Manifold.getGroupHistory(nameProperty.get()),
                () -> new ComponentProxy(TermAux.UNINITIALIZED_COMPONENT_ID.getNid(), TermAux.UNINITIALIZED_COMPONENT_ID.getFullyQualifiedName()), manifold1 -> new ComponentProxyEditorStub(manifold1));
        getItemList().add(this.focusWrapper);
        getItemList().add(this.historyWrapper);
        this.manifoldItem.focusedConceptProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                save();
                this.focusWrapper.setValue(newValue);
            }
        });
    }

    @Override
    protected void saveFields() {
        getPreferencesNode().put(PreferenceGroup.Keys.GROUP_NAME, this.nameProperty.get());
        Optional<ConceptSpecification> focusedConcept = this.manifoldItem.getFocusedConcept();
        if (focusedConcept.isPresent()) {
            getPreferencesNode().putConceptSpecification(Keys.FOCUSED_CONCEPT, focusedConcept.get());
        }
        getPreferencesNode().putComponentList(Keys.HISTORY_RECORDS, manifoldItem.getHistoryRecords());
    }

    @Override
    protected void revertFields() {
        this.nameProperty.set(getPreferencesNode().get(PreferenceGroup.Keys.GROUP_NAME, getGroupName()));
        Optional<ConceptSpecification> optionalFocus = getPreferencesNode().getConceptSpecification(Keys.FOCUSED_CONCEPT);
        if (optionalFocus.isPresent()) {
            Manifold.getGroupFocusProperty(this.nameProperty.get()).set(Get.concept(optionalFocus.get()));
        }
        manifoldItem.getHistoryRecords().clear();
        manifoldItem.getHistoryRecords().addAll(getPreferencesNode().getComponentList(Keys.HISTORY_RECORDS));
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
