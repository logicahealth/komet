package sh.isaac.komet.preferences.paths;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.commit.ChronologyChangeListener;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSnapshot;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.preferences.ParentPanel;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.komet.gui.contract.preferences.PreferenceGroup;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;

import java.util.UUID;
import java.util.prefs.BackingStoreException;

import static sh.komet.gui.contract.preferences.PreferenceGroup.Keys.GROUP_NAME;

public class PathGroupPanel extends ParentPanel implements ChronologyChangeListener {

    ObservableList<ConceptSnapshot> pathConcepts;
    private transient UUID listenerUuid = UUID.randomUUID();
    private transient BorderPane centerPane = new BorderPane();
    Button addButton = new Button("Add path...");
    {
        addButton.setOnAction(this::addPath);
    }
    ToolBar topToolbar = new ToolBar(addButton);

    Pane filler = new Pane();
    Button cancelButton = new Button("cancel");
    Button createButton = new Button("create");
    {
        HBox.setHgrow(filler, Priority.ALWAYS);
        cancelButton.setOnAction(this::cancelAddPath);
        cancelButton.setVisible(false);
        createButton.setOnAction(this::createPath);
        createButton.setVisible(false);
    }

    ToolBar bottomToolbar = new ToolBar(filler, cancelButton, createButton);

    public PathGroupPanel(IsaacPreferences preferencesNode, Manifold manifold, KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "Paths"), manifold, kpc);
        this.pathConcepts = FxGet.activeConceptMembers(TermAux.PATH_ASSEMBLAGE, manifold);
        Get.commitService().addChangeListener(this);
        Platform.runLater(() -> refreshChildren());
    }

    @Override
    public void handleChange(ConceptChronology cc) {
        // ignore
    }

    @Override
    public void handleChange(SemanticChronology sc) {
        // ignore
    }

    @Override
    public void handleCommit(CommitRecord commitRecord) {
        Platform.runLater(() -> {
            this.pathConcepts = FxGet.activeConceptMembers(TermAux.PATH_ASSEMBLAGE, getManifold());
        });
        refreshChildren();
    }

    private void refreshChildren() {
        getTreeItem().getChildren().clear();
        boolean expanded = getTreeItem().isExpanded();
        for (ConceptSnapshot pathConcept: pathConcepts) {
            PathItemPanel pathItemPanelPanel = new PathItemPanel(pathConcept);
            TreeItem<PreferenceGroup> treePathItem = new TreeItem(pathItemPanelPanel);
            getTreeItem().getChildren().add(treePathItem);
        }
        getTreeItem().setExpanded(expanded);
    }

    @Override
    public UUID getListenerUuid() {
        return this.listenerUuid;
    }

    public Node getCenterPanel(Manifold manifold) {
         return this.centerPane;
    }

    private void createPath(ActionEvent action) {
        // Paths should always be created in sandbox module on foundation path?
        // Maybe can also be retired in sandbox module on foundation path?
        setupButtonsForNew();
    }

    private void setupButtonsForNew() {
        this.centerPane.setCenter(null);
        this.addButton.setVisible(true);
        this.createButton.setVisible(false);
        this.cancelButton.setVisible(false);
    }

    private void cancelAddPath(ActionEvent action) {
        setupButtonsForNew();
    }
    private void addPath(ActionEvent action) {
       NewPathPanel newPathPanel = new NewPathPanel(this.getManifold());
       centerPane.setCenter(newPathPanel.getEditor());
       addButton.setVisible(false);
       cancelButton.setVisible(true);
       createButton.setVisible(true);

    }

    @Override
    public Node getTopPanel(Manifold manifold) {
        return topToolbar;
    }
    public Node getBottomPanel(Manifold manifold) {
        return bottomToolbar;
    }

    @Override
    protected Class getChildClass() {
        return PathItemPanel.class;
    }

    @Override
    protected void saveFields() throws BackingStoreException {

    }

    @Override
    protected void revertFields() throws BackingStoreException {

    }
}
