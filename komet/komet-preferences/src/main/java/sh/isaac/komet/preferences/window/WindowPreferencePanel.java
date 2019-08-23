package sh.isaac.komet.preferences.window;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.preferences.KometPreferencesController;
import sh.isaac.komet.preferences.ParentPanel;
import sh.komet.gui.contract.preferences.PersonaItem;
import sh.komet.gui.contract.preferences.WindowPreferencesItem;
import sh.komet.gui.control.PropertySheetTextWrapper;
import sh.komet.gui.control.concept.PropertySheetConceptListWrapper;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.prefs.BackingStoreException;

/**
 * The window preferences must be able to reconstruct, and save state for an arbitrary number of
 * exploration and detail nodes that are discovered by service lookup and added by the user.
 * Since the exploration and detail nodes are dynamic, and are not necessarily known at compile time,
 * we have to specify an idiom by which state is provided to, and saved by these nodes.
 * <br>
 * Must handle addition of exploration and detail nodes to a window, and deletion of
 * exploration and detail nodes from a window.
 * <br>
 *     1. Modify the factory for exploration and detail nodes to accept the preferences node for use by the
 *     exploration or detail node. When created from the factory, the node should use a default configuration,
 *     and write that to the node.
 *
 *     2. Always pass in a deep clone of the parent's manifold. If the child overrides the manifold, it will
 *     persist that info.
 *
 *     3. The exploration and detail nodes store location and order info where to locate them in the window...
 *     The window constructs the node, then asks the node where to put it, and then puts it in that location.
 *
 *     4. The exploration and detail nodes store the factory class to construct to be used to reconstruct
 *     them.
 */
public class WindowPreferencePanel extends ParentPanel implements WindowPreferencesItem {
    public enum Keys {
        ITEM_NAME,
        LEFT_TAB_NODES,
        CENTER_TAB_NODES,
        RIGHT_TAB_NODES,
        X_LOC,
        Y_LOC,
        HEIGHT,
        WIDTH,
        PERSONA_UUID,
        MANIFOLD
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

    private final SimpleDoubleProperty xLocationProperty =
            new SimpleDoubleProperty(this, MetaData.WINDOW_X_POSITION____SOLOR.toExternalString());

    private final SimpleDoubleProperty yLocationProperty =
            new SimpleDoubleProperty(this, MetaData.WINDOW_Y_POSITION____SOLOR.toExternalString());

    private final SimpleDoubleProperty heightProperty =
            new SimpleDoubleProperty(this, MetaData.WINDOW_HEIGHT____SOLOR.toExternalString());

    private final SimpleDoubleProperty widthProperty =
            new SimpleDoubleProperty(this, MetaData.WINDOW_WIDTH____SOLOR.toExternalString());

    private PersonaItem personaItem;

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


    public WindowPreferencePanel(IsaacPreferences preferencesNode,
                                 Manifold manifold,
                                 KometPreferencesController kpc,
                                 PersonaItem personaItem) {
        super(preferencesNode, getGroupName(preferencesNode),
                manifold, kpc);
        this.personaItem = personaItem;
        nameProperty.set(groupNameProperty().get());
        nameProperty.addListener((observable, oldValue, newValue) -> {
            groupNameProperty().set(newValue);
        });
        for (ConceptSpecification defaultItem: personaItem.leftPaneDefaultsProperty()) {
            this.leftTabNodesProperty.add(defaultItem);
        }

        for (ConceptSpecification defaultItem: personaItem.centerPaneDefaultsProperty()) {
            this.centerTabNodesProperty.add(defaultItem);
        }

        for (ConceptSpecification defaultItem: personaItem.rightPaneDefaultsProperty()) {
            this.rightTabNodesProperty.add(defaultItem);
        }

        this.leftTabsWrapper = new PropertySheetConceptListWrapper(manifold, leftTabNodesProperty);
        this.centerTabsWrapper = new PropertySheetConceptListWrapper(manifold, centerTabNodesProperty);
        this.rightTabsWrapper = new PropertySheetConceptListWrapper(manifold, rightTabNodesProperty);
        setDefaultLocationAndSize();
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
    protected void saveFields() throws BackingStoreException {
        Optional<String> oldItemName = getPreferencesNode().get(Keys.ITEM_NAME);
        if (oldItemName.isPresent()) {
            FxGet.removeTaxonomyConfiguration(oldItemName.get());
        }

        getPreferenceNode().put(Keys.ITEM_NAME, this.nameProperty.get());
        getPreferencesNode().putConceptList(Keys.LEFT_TAB_NODES, leftTabNodesProperty);
        getPreferencesNode().putConceptList(Keys.CENTER_TAB_NODES, centerTabNodesProperty);
        getPreferencesNode().putConceptList(Keys.RIGHT_TAB_NODES, rightTabNodesProperty);
        getPreferenceNode().putDouble(Keys.X_LOC, this.xLocationProperty.doubleValue());
        getPreferenceNode().putDouble(Keys.Y_LOC, this.yLocationProperty.doubleValue());
        getPreferenceNode().putDouble(Keys.HEIGHT, this.heightProperty.doubleValue());
        getPreferenceNode().putDouble(Keys.WIDTH, this.widthProperty.doubleValue());
        if (personaItem != null) {
            getPreferenceNode().putUuid(Keys.PERSONA_UUID, personaItem.getPersonaUuid());
        }

    }

    @Override
    protected void revertFields() {

        Optional<UUID> optionalPersonaUuid = getPreferenceNode().getUuid(Keys.PERSONA_UUID);
        if (optionalPersonaUuid.isPresent()) {
           this.personaItem = null;
           UUID personaUuid = optionalPersonaUuid.get();
           for (PersonaItem personaItem: FxGet.kometPreferences().getPersonaPreferences()) {
                if (personaItem.getPersonaUuid().equals(personaUuid)) {
                    this.personaItem = personaItem;
                    break;
                }
           }
        } else {
            this.personaItem = null;
        }

        this.nameProperty.set(getPreferencesNode().get(Keys.ITEM_NAME, "KOMET window"));

        this.leftTabNodesProperty.setAll(getPreferencesNode().getConceptList(Keys.LEFT_TAB_NODES,
                List.of(MetaData.TAXONOMY_PANEL____SOLOR)));

        this.centerTabNodesProperty.setAll(getPreferencesNode().getConceptList(Keys.CENTER_TAB_NODES,
                List.of(MetaData.CONCEPT_DETAILS_PANEL____SOLOR,
                        MetaData.CONCEPT_DETAILS_PANEL____SOLOR)));

        this.rightTabNodesProperty.setAll(getPreferencesNode().getConceptList(Keys.RIGHT_TAB_NODES,
                List.of(MetaData.ACTIVITIES_PANEL____SOLOR,
                        MetaData.SIMPLE_SEARCH_PANEL____SOLOR,
                        MetaData.EXTENDED_SEARCH_PANEL____SOLOR)));

        setDefaultLocationAndSize();
    }

    private void setDefaultLocationAndSize() {
        this.xLocationProperty.setValue(getPreferencesNode().getDouble(Keys.X_LOC, 40.0));
        this.yLocationProperty.setValue(getPreferencesNode().getDouble(Keys.Y_LOC, 40.0));
        this.heightProperty.setValue(getPreferencesNode().getDouble(Keys.HEIGHT, 897));
        this.widthProperty.setValue(getPreferencesNode().getDouble(Keys.WIDTH, 1400));
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
    protected Class getChildClass() {
        return WindowTabPanePreferencesPanel.class;
    }

    @Override
    public IsaacPreferences getPreferenceNode() {
        return getPreferencesNode();
    }

    @Override
    public boolean showRevertAndSave() {
        return true;
    }

    @Override
    public SimpleDoubleProperty xLocationProperty() {
        return xLocationProperty;
    }

    @Override
    public SimpleDoubleProperty yLocationProperty() {
        return yLocationProperty;
    }

    @Override
    public SimpleDoubleProperty heightProperty() {
        return heightProperty;
    }

    @Override
    public SimpleDoubleProperty widthProperty() {
        return widthProperty;
    }

    @Override
    public PersonaItem getPersonaItem() {
        return this.personaItem;
    }

    @Override
    public void setPersonaItem(PersonaItem personaItem) {
        if (personaItem == null ||
                (this.personaItem == null && personaItem != null) ||
                (!this.personaItem.getPersonaUuid().equals(personaItem.getPersonaUuid()))) {
            // things have changed.
            this.personaItem = personaItem;

            this.leftTabNodesProperty.clear();
            this.centerTabNodesProperty.clear();
            this.rightTabNodesProperty.clear();

            if (this.personaItem != null) {
                this.leftTabNodesProperty.addAll(this.personaItem.leftPaneDefaultsProperty().get());
                this.centerTabNodesProperty.addAll(this.personaItem.centerPaneDefaultsProperty().get());
                this.rightTabNodesProperty.addAll(this.personaItem.rightPaneDefaultsProperty().get());
            }
        }
    }
}
