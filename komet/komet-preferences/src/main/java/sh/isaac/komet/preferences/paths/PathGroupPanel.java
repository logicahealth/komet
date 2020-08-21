package sh.isaac.komet.preferences.paths;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifiedComponentBuilder;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.commit.*;
import sh.isaac.api.component.concept.ConceptBuilder;
import sh.isaac.api.component.concept.ConceptSnapshot;
import sh.isaac.api.component.semantic.SemanticBuilder;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.coordinate.*;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.komet.preferences.ParentPanel;
import sh.isaac.model.observable.coordinate.ObservableManifoldCoordinateImpl;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.komet.gui.contract.preferences.PreferenceGroup;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.util.FxGet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.prefs.BackingStoreException;

import static sh.isaac.api.logic.LogicalExpressionBuilder.*;
import static sh.komet.gui.contract.preferences.PreferenceGroup.Keys.GROUP_NAME;

public class PathGroupPanel extends ParentPanel implements CommitListener {

    ObservableList<ConceptSnapshot> pathConcepts;
    private transient UUID listenerUuid = UUID.randomUUID();
    private transient BorderPane centerPane = new BorderPane();
    private transient NewPathPanel newPathPanel;
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

    public PathGroupPanel(IsaacPreferences preferencesNode, ViewProperties viewProperties, KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "Paths"), viewProperties, kpc);
        this.pathConcepts = FxGet.activeConceptMembers(TermAux.PATH_ASSEMBLAGE, viewProperties.getManifoldCoordinate());
        Get.commitService().addCommitListener(this);
        Platform.runLater(() -> refreshChildren());
    }

    @Override
    public void handleCommit(CommitRecord commitRecord) {
        Platform.runLater(() -> {
            this.pathConcepts = FxGet.activeConceptMembers(TermAux.PATH_ASSEMBLAGE, getViewProperties().getManifoldCoordinate());
            refreshChildren();
        });
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

    public Node getCenterPanel(ViewProperties viewProperties) {
         return this.centerPane;
    }

    private void createPath(ActionEvent action) {
        // Paths should always be created in sandbox module on foundation path?
        // Maybe can also be retired in sandbox module on foundation path?

        List<IdentifiedComponentBuilder> builders = new ArrayList<>();
        // int authorNid, int defaultModuleNid, int promotionPathNid, int destinationModuleNid

        ObservableManifoldCoordinateImpl manifoldForPathCreation =
                new ObservableManifoldCoordinateImpl(this.getManifoldCoordinate().toManifoldCoordinateImmutable());
        manifoldForPathCreation.getEditCoordinate().defaultModuleProperty().setValue(TermAux.SANDBOX_MODULE);
        manifoldForPathCreation.getViewStampFilter().pathConceptProperty().setValue(TermAux.SANDBOX_PATH);

        // 1. Create new concept/description, in sandbox module
        LogicalExpressionBuilder expressionBuilder = Get.logicalExpressionBuilderService().getLogicalExpressionBuilder();

        NecessarySet(And(ConceptAssertion(TermAux.SANDBOX_PATH.getNid(), expressionBuilder)));
        final LogicalExpression logicalExpression = expressionBuilder.build();

        ConceptBuilder conceptBuilder = Get.conceptBuilderService()
                .getDefaultConceptBuilder(newPathPanel.getNewPathName(),
                        "Path", logicalExpression,
                        TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getAssemblageNid());
        builders.add(conceptBuilder);
        int conceptNid = conceptBuilder.getNid();

        // 2. Add to path refset in sandbox module
        SemanticBuilder<? extends SemanticChronology> pathMemberBuilder = Get.semanticBuilderService()
                .getMembershipSemanticBuilder(conceptNid,
                        TermAux.PATH_ASSEMBLAGE.getNid());
        builders.add(pathMemberBuilder);
        // 3. Add origins in sandbox module
        for (StampPositionImmutable origin: newPathPanel.getNewPathCoordinate().getPathOrigins()) {
            SemanticBuilder<? extends SemanticChronology> originElementBuilder =  Get.semanticBuilderService()
                    .getComponentLongSemanticBuilder(origin.getPathForPositionNid(), origin.getTime(),
                            conceptNid, TermAux.PATH_ORIGIN_ASSEMBLAGE.getNid());
            builders.add(originElementBuilder);
        }
        Transaction transaction = Get.commitService().newTransaction(Optional.of("Path from preference panel"), ChangeCheckerMode.INACTIVE);
        builders.forEach(identifiedComponentBuilder -> {
            final List<Chronology> builtObjects = new ArrayList<>();
            identifiedComponentBuilder.build(transaction, manifoldForPathCreation, builtObjects);
        });
        transaction.commit();
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
       newPathPanel = new NewPathPanel(FxGet.preferenceViewProperties());
       centerPane.setCenter(newPathPanel.getEditor());
       addButton.setVisible(false);
       cancelButton.setVisible(true);
       createButton.setVisible(true);

    }

    @Override
    public Node getTopPanel(ViewProperties viewProperties) {
        return topToolbar;
    }
    public Node getBottomPanel(ViewProperties viewProperties) {
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
