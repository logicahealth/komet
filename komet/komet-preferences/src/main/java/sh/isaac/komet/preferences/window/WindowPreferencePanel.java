package sh.isaac.komet.preferences.window;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.isaac.komet.preferences.ParentPanel;
import sh.komet.gui.contract.preferences.PreferenceGroup;
import sh.komet.gui.contract.preferences.PersonaItem;
import sh.komet.gui.contract.preferences.TabSpecification;
import sh.komet.gui.contract.preferences.WindowPreferencesItem;
import sh.komet.gui.control.PropertySheetBooleanWrapper;
import sh.komet.gui.control.PropertySheetTextWrapper;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;

import java.util.*;

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
        LEFT_TAB_NODES,
        CENTER_TAB_NODES,
        RIGHT_TAB_NODES,
        X_LOC,
        Y_LOC,
        HEIGHT,
        WIDTH,
        PERSONA_UUID,
        MANIFOLD,
        /**
         * The window uuid str is the name of the preference node associated with this window...
         */
        WINDOW_UUID_STR,
        ENABLE_LEFT_PANE,
        ENABLE_CENTER_PANE,
        ENABLE_RIGHT_PANE,
        LEFT_TAB_SELECTION,
        CENTER_TAB_SELECTION,
        RIGHT_TAB_SELECTION,
        DIVIDER_POSITIONS
    };

    private final SimpleBooleanProperty enableLeftPaneProperty = new SimpleBooleanProperty(this, MetaData.ENABLE_LEFT_PANE____SOLOR.toExternalString(), false);
    private final SimpleBooleanProperty enableCenterPaneProperty = new SimpleBooleanProperty(this, MetaData.ENABLE_CENTER_PANE____SOLOR.toExternalString(), false);
    private final SimpleBooleanProperty enableRightPaneProperty = new SimpleBooleanProperty(this, MetaData.ENABLE_RIGHT_PANE____SOLOR.toExternalString(), false);

    private final SimpleStringProperty windowNameProperty
            = new SimpleStringProperty(this, MetaData.WINDOW_CONFIGURATION_NAME____SOLOR.toExternalString());

    private final SimpleListProperty<TabSpecification> leftTabNodesProperty =
            new SimpleListProperty<>(this, MetaData.LEFT_TAB_NODES____SOLOR.toExternalString(), FXCollections.observableArrayList());

    private final SimpleListProperty<TabSpecification> centerTabNodesProperty =
            new SimpleListProperty<>(this, MetaData.CENTER_TAB_NODES____SOLOR.toExternalString(), FXCollections.observableArrayList());

    private final SimpleListProperty<TabSpecification> rightTabNodesProperty =
            new SimpleListProperty<>(this, MetaData.RIGHT_TAB_NODES____SOLOR.toExternalString(), FXCollections.observableArrayList());

    private final SimpleDoubleProperty xLocationProperty =
            new SimpleDoubleProperty(this, MetaData.WINDOW_X_POSITION____SOLOR.toExternalString());

    private final SimpleDoubleProperty yLocationProperty =
            new SimpleDoubleProperty(this, MetaData.WINDOW_Y_POSITION____SOLOR.toExternalString());

    private final SimpleDoubleProperty heightProperty =
            new SimpleDoubleProperty(this, MetaData.WINDOW_HEIGHT____SOLOR.toExternalString());

    private final SimpleDoubleProperty widthProperty =
            new SimpleDoubleProperty(this, MetaData.WINDOW_WIDTH____SOLOR.toExternalString());

    private final SimpleIntegerProperty leftTabSelectionProperty = new SimpleIntegerProperty(this, "left tab selection", 0);
    private final SimpleIntegerProperty centerTabSelectionProperty = new SimpleIntegerProperty(this, "center tab selection", 0);
    private final SimpleIntegerProperty rightTabSelectionProperty = new SimpleIntegerProperty(this, "right tab selection", 0);

    private final SimpleObjectProperty<double[]> dividerPositionsProperty = new SimpleObjectProperty<>(this, "divider positions", new double[] {0.2504, 0.7504});
    private PersonaItem personaItem;

    private static HashSet<String> windowIds = new HashSet<>();

    /**
     * This constructor is called by reflection when reconstituting a window from the preferences.
     * @param preferencesNode
     * @param manifold
     * @param kpc
     */
    public WindowPreferencePanel(IsaacPreferences preferencesNode,
                                 Manifold manifold,
                                 KometPreferencesController kpc) {
        super(preferencesNode, getGroupName(preferencesNode),
                manifold, kpc);

        windowIds.add(preferencesNode.name());
        // The GROUP_NAME is the name of this window that will show up in the preferences panel,
        // We want the name of the group to be the same as name of the window, and will link the
        // properties here.
        windowNameProperty.set(preferencesNode.get(PreferenceGroup.Keys.GROUP_NAME, "New window 1"));

        windowNameProperty.addListener((observable, oldValue, newValue) -> {
            for (Window window: Stage.getWindows()) {
                if (window instanceof Stage) {
                    Stage stage = (Stage) window;
                    String uuidStr = (String) stage.getScene().getProperties().get(Keys.WINDOW_UUID_STR);
                    if (preferencesNode.name().equals(uuidStr)) {
                        stage.setTitle(newValue);
                    }
                }
            }
            FxGet.kometPreferences().updatePreferencesTitle(UUID.fromString(preferencesNode.name()), newValue);
        });
        revertFields();
        save();
        getItemList().add(new PropertySheetTextWrapper(manifold, windowNameProperty));
        getItemList().add(new PropertySheetBooleanWrapper(manifold, enableLeftPaneProperty));
        getItemList().add(new PropertySheetBooleanWrapper(manifold, enableCenterPaneProperty));
        getItemList().add(new PropertySheetBooleanWrapper(manifold, enableRightPaneProperty));
        this.leftTabSelectionProperty.addListener((observable, oldValue, newValue) -> {
            save();
        });
        this.centerTabSelectionProperty.addListener((observable, oldValue, newValue) -> {
            save();
        });
        this.rightTabSelectionProperty.addListener((observable, oldValue, newValue) -> {
            save();
        });
    }

    @Override
    public UUID getWindowUuid() {
        return UUID.fromString(getPreferenceNode().name());
    }

    public static String getWindowName(String prefix, String nodeName) {
        windowIds.add(nodeName);
        if (windowIds.size() > 1) {
            int foundCount = 0;
            for (Window window: Window.getWindows()) {
                if (window instanceof Stage) {
                    Stage stage = (Stage) window;
                    if (stage.getTitle().startsWith(prefix)) {
                        foundCount++;
                    }
                }
            }
            if (foundCount > 0) {
                return prefix + " " + foundCount;
            }
        }
        return prefix;
    }

    /**
     * This constructor is called when creating a new WindowPreferencePanel from a persona, not
     * when reconstituting from a preferences node.
     * @param preferencesNode
     * @param manifold
     * @param kpc
     * @param personaItem
     */
    public WindowPreferencePanel(IsaacPreferences preferencesNode,
                                 Manifold manifold,
                                 KometPreferencesController kpc,
                                 PersonaItem personaItem) {
        super(preferencesNode, getRequiredGroupName(preferencesNode),
                manifold, kpc);
        this.personaItem = personaItem;
        windowIds.add(preferencesNode.name());
        String startingName = getWindowName(personaItem.instanceNameProperty().get(), preferencesNode.name());

        windowNameProperty.set(preferencesNode.get(PreferenceGroup.Keys.GROUP_NAME, startingName));
        windowNameProperty.addListener((observable, oldValue, newValue) -> {
            groupNameProperty().set(newValue);
        });
        this.enableLeftPaneProperty.set(personaItem.enableLeftPaneProperty().getValue());
        for (ConceptSpecification defaultItem: personaItem.leftPaneDefaultsProperty()) {
            this.leftTabNodesProperty.add(new TabSpecification(defaultItem, UUID.randomUUID()));
        }
        this.enableCenterPaneProperty.set(personaItem.enableCenterPaneProperty().getValue());
        for (ConceptSpecification defaultItem: personaItem.centerPaneDefaultsProperty()) {
            this.centerTabNodesProperty.add(new TabSpecification(defaultItem, UUID.randomUUID()));
        }
        this.enableRightPaneProperty.set(personaItem.enableRightPaneProperty().getValue());
        for (ConceptSpecification defaultItem: personaItem.rightPaneDefaultsProperty()) {
            this.rightTabNodesProperty.add(new TabSpecification(defaultItem, UUID.randomUUID()));
        }
        setDefaultLocationAndSize();
        save();
        getItemList().add(new PropertySheetTextWrapper(manifold, windowNameProperty));
        getItemList().add(new PropertySheetBooleanWrapper(manifold, enableLeftPaneProperty));
        getItemList().add(new PropertySheetBooleanWrapper(manifold, enableCenterPaneProperty));
        getItemList().add(new PropertySheetBooleanWrapper(manifold, enableRightPaneProperty));
    }

    @Override
    public ObservableList<TabSpecification> getNodesList(int paneIndex) {
        switch (paneIndex) {
            case 0:
                return this.leftTabNodesProperty;
            case 1:
                return this.centerTabNodesProperty;
            case 2:
                return this.rightTabNodesProperty;
            default:
                return FXCollections.emptyObservableList();
        }
    }

    private static String getRequiredGroupName(IsaacPreferences preferencesNode) {
        Optional<String> name = preferencesNode.get(PreferenceGroup.Keys.GROUP_NAME);
        if (name.isPresent()) {
            return name.get();
        }
        throw new IllegalStateException("Window name not set");
    }

    @Override
    protected void saveFields() {
        getPreferenceNode().put(PreferenceGroup.Keys.GROUP_NAME, this.windowNameProperty.get());
        getPreferencesNode().putList(Keys.LEFT_TAB_NODES, TabSpecification.toStringList(leftTabNodesProperty));
        getPreferencesNode().putList(Keys.CENTER_TAB_NODES, TabSpecification.toStringList(centerTabNodesProperty));
        getPreferencesNode().putList(Keys.RIGHT_TAB_NODES, TabSpecification.toStringList(rightTabNodesProperty));
        getPreferenceNode().putDouble(Keys.X_LOC, this.xLocationProperty.doubleValue());
        getPreferenceNode().putDouble(Keys.Y_LOC, this.yLocationProperty.doubleValue());
        getPreferenceNode().putDouble(Keys.HEIGHT, this.heightProperty.doubleValue());
        getPreferenceNode().putDouble(Keys.WIDTH, this.widthProperty.doubleValue());
        getPreferencesNode().putBoolean(Keys.ENABLE_LEFT_PANE, this.enableLeftPaneProperty.get());
        getPreferencesNode().putBoolean(Keys.ENABLE_CENTER_PANE, this.enableCenterPaneProperty.get());
        getPreferencesNode().putBoolean(Keys.ENABLE_RIGHT_PANE, this.enableRightPaneProperty.get());
        if (personaItem != null) {
            getPreferenceNode().putUuid(Keys.PERSONA_UUID, personaItem.getPersonaUuid());
        }
        getPreferenceNode().putInt(Keys.LEFT_TAB_SELECTION, leftTabSelectionProperty.get());
        getPreferenceNode().putInt(Keys.CENTER_TAB_SELECTION, centerTabSelectionProperty.get());
        getPreferenceNode().putInt(Keys.RIGHT_TAB_SELECTION, rightTabSelectionProperty.get());
        getPreferenceNode().putDoubleArray(Keys.DIVIDER_POSITIONS, dividerPositionsProperty.get());
    }

    @Override
    protected void revertFields() {
        setDefaultLocationAndSize();

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

        this.windowNameProperty.set(getPreferencesNode().get(PreferenceGroup.Keys.GROUP_NAME, windowNameProperty.getValue()));
        this.enableLeftPaneProperty.set(getPreferencesNode().getBoolean(Keys.ENABLE_LEFT_PANE, true));
        this.enableCenterPaneProperty.set(getPreferencesNode().getBoolean(Keys.ENABLE_CENTER_PANE, true));
        this.enableRightPaneProperty.set(getPreferencesNode().getBoolean(Keys.ENABLE_RIGHT_PANE, true));

        this.leftTabNodesProperty.setAll(TabSpecification.fromStringList(getPreferencesNode().getList(Keys.LEFT_TAB_NODES, new ArrayList<>())));

        this.centerTabNodesProperty.setAll(TabSpecification.fromStringList(getPreferencesNode().getList(Keys.CENTER_TAB_NODES,new ArrayList<>())));

        this.rightTabNodesProperty.setAll(TabSpecification.fromStringList(getPreferencesNode().getList(Keys.RIGHT_TAB_NODES,new ArrayList<>())));

        this.leftTabSelectionProperty.set(getPreferenceNode().getInt(Keys.LEFT_TAB_SELECTION, leftTabSelectionProperty.get()));
        this.centerTabSelectionProperty.set(getPreferenceNode().getInt(Keys.CENTER_TAB_SELECTION, centerTabSelectionProperty.get()));
        this.rightTabSelectionProperty.set(getPreferenceNode().getInt(Keys.RIGHT_TAB_SELECTION, rightTabSelectionProperty.get()));

        this.dividerPositionsProperty.set(getPreferenceNode().getDoubleArray(Keys.DIVIDER_POSITIONS, this.dividerPositionsProperty.get()));
    }

    private void setDefaultLocationAndSize() {
        this.xLocationProperty.setValue(getPreferencesNode().getDouble(Keys.X_LOC, 40.0));
        this.yLocationProperty.setValue(getPreferencesNode().getDouble(Keys.Y_LOC, 40.0));
        this.heightProperty.setValue(getPreferencesNode().getDouble(Keys.HEIGHT, 897));
        this.widthProperty.setValue(getPreferencesNode().getDouble(Keys.WIDTH, 1400));
        this.dividerPositionsProperty.setValue(new double[] {0.2504, 0.7504});
    }

    @Override
    public SimpleObjectProperty<double[]> dividerPositionsProperty() {
        return dividerPositionsProperty;
    }

    @Override
    public boolean showDelete() {
        return true;
    }

    @Override
    public StringProperty getWindowName() {
        return windowNameProperty;
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
    public SimpleBooleanProperty enableLeftPaneProperty() {
        return this.enableLeftPaneProperty;
    }

    @Override
    public SimpleBooleanProperty enableCenterPaneProperty() {
        return this.enableCenterPaneProperty;
    }

    @Override
    public SimpleBooleanProperty enableRightPaneProperty() {
        return this.enableRightPaneProperty;
    }

    @Override
    public SimpleIntegerProperty leftTabSelectionProperty() {
        return this.leftTabSelectionProperty;
    }

    @Override
    public SimpleIntegerProperty centerTabSelectionProperty() {
        return this.centerTabSelectionProperty;
    }

    @Override
    public SimpleIntegerProperty rightTabSelectionProperty() {
        return this.rightTabSelectionProperty;
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
                for (ConceptSpecification defaultItem: personaItem.leftPaneDefaultsProperty()) {
                    this.leftTabNodesProperty.add(new TabSpecification(defaultItem, UUID.randomUUID()));
                }
                for (ConceptSpecification defaultItem: personaItem.centerPaneDefaultsProperty()) {
                    this.centerTabNodesProperty.add(new TabSpecification(defaultItem, UUID.randomUUID()));
                }
                for (ConceptSpecification defaultItem: personaItem.rightPaneDefaultsProperty()) {
                    this.rightTabNodesProperty.add(new TabSpecification(defaultItem, UUID.randomUUID()));
                }
            }
        }
    }


    @Override
    public boolean isPaneEnabled(int paneIndex) {
        switch (paneIndex) {
            case 0:
                return enableLeftPaneProperty.get();
            case 1:
                return enableCenterPaneProperty.get();
            case 2:
                return enableRightPaneProperty.get();
            default:
                return false;
        }
    }

}
