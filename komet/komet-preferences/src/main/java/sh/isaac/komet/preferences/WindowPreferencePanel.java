package sh.isaac.komet.preferences;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.komet.gui.contract.preferences.WindowPreferenceItems;
import sh.komet.gui.control.PropertySheetTextWrapper;
import sh.komet.gui.control.concept.PropertySheetConceptListWrapper;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;

import java.util.List;
import java.util.Optional;
import java.util.prefs.BackingStoreException;

public class WindowPreferencePanel extends ParentPanel implements WindowPreferenceItems {
    public enum Keys {
        ITEM_NAME,
        LEFT_TAB_NODES,
        CENTER_TAB_NODES,
        RIGHT_TAB_NODES
    };
    private final SimpleStringProperty nameProperty
            = new SimpleStringProperty(this, MetaData.WINDOW_CONFIGURATION_NAME____SOLOR.toExternalString());

    private final SimpleListProperty<ConceptSpecification> leftTabNodesProperty =
            new SimpleListProperty<>(this, MetaData.LEFT_TAB_NODES____SOLOR.toExternalString(), FXCollections.observableArrayList());
    private final PropertySheetConceptListWrapper leftTabsWrapper;


    private final SimpleListProperty<ConceptSpecification> centerTabNodesProperty =
            new SimpleListProperty<>(this, MetaData.CENTER_TAB_NODES____SOLOR.toExternalString(), FXCollections.observableArrayList());
    private final PropertySheetConceptListWrapper centerTabsWrapper;

    private final SimpleListProperty<ConceptSpecification> rightTabNodesProperty =
            new SimpleListProperty<>(this, MetaData.RIGHT_TAB_NODES____SOLOR.toExternalString(), FXCollections.observableArrayList());
    private final PropertySheetConceptListWrapper rightTabsWrapper;



    public WindowPreferencePanel(IsaacPreferences preferencesNode,
                                 Manifold manifold,
                                 KometPreferencesController kpc) {
        super(preferencesNode, getGroupName(preferencesNode),
                manifold, kpc);
        nameProperty.set(groupNameProperty().get());
        nameProperty.addListener((observable, oldValue, newValue) -> {
            groupNameProperty().set(newValue);
        });
        this.leftTabsWrapper = new PropertySheetConceptListWrapper(manifold, leftTabNodesProperty);
        this.centerTabsWrapper = new PropertySheetConceptListWrapper(manifold, centerTabNodesProperty);
        this.rightTabsWrapper = new PropertySheetConceptListWrapper(manifold, rightTabNodesProperty);
        revertFields();
        save();
        getItemList().add(new PropertySheetTextWrapper(manifold, nameProperty));
        getItemList().add(leftTabsWrapper);
        getItemList().add(centerTabsWrapper);
        getItemList().add(rightTabsWrapper);
    }

    private static String getGroupName(IsaacPreferences preferencesNode) {
        return preferencesNode.get(Keys.ITEM_NAME, "Window configurations");
    }

    @Override
    void saveFields() throws BackingStoreException {
        Optional<String> oldItemName = getPreferencesNode().get(Keys.ITEM_NAME);
        if (oldItemName.isPresent()) {
            FxGet.removeTaxonomyConfiguration(oldItemName.get());
        }

        getPreferencesNode().putConceptList(Keys.LEFT_TAB_NODES, leftTabNodesProperty);
        getPreferencesNode().putConceptList(Keys.CENTER_TAB_NODES, centerTabNodesProperty);
        getPreferencesNode().putConceptList(Keys.RIGHT_TAB_NODES, rightTabNodesProperty);

    }

    @Override
    void revertFields() {
        this.nameProperty.set(getPreferencesNode().get(TaxonomyItemPanel.Keys.ITEM_NAME, "KOMET window"));

        leftTabNodesProperty.setAll(getPreferencesNode().getConceptList(Keys.LEFT_TAB_NODES,
                List.of(MetaData.TAXONOMY_PANEL____SOLOR)));

        centerTabNodesProperty.setAll(getPreferencesNode().getConceptList(Keys.CENTER_TAB_NODES,
                List.of(MetaData.CONCEPT_DETAILS_PANEL____SOLOR,
                        MetaData.CONCEPT_DETAILS_PANEL____SOLOR)));

        rightTabNodesProperty.setAll(getPreferencesNode().getConceptList(Keys.RIGHT_TAB_NODES,
                List.of(MetaData.ACTIVITIES_PANEL____SOLOR,
                        MetaData.SIMPLE_SEARCH_PANEL____SOLOR,
                        MetaData.EXTENDED_SEARCH_PANEL____SOLOR)));
    }
    @Override
    public boolean showDelete() {
        return true;
    }

    @Override
    public StringProperty getWindowName() {
        return nameProperty;
    }

    @Override
    public ObservableList<ConceptSpecification> getLeftTabPanelSpecs() {
        return leftTabNodesProperty;
    }

    @Override
    public ObservableList<ConceptSpecification> getCenterTabPanelSpecs() {
        return centerTabNodesProperty;
    }

    @Override
    public ObservableList<ConceptSpecification> getRightTabPanelSpecs() {
        return rightTabNodesProperty;
    }

    @Override
    protected Class getChildClass() {
        return WindowTabPanePreferencesPanel.class;
    }
}
