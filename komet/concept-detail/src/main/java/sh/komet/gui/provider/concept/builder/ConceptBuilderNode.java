    /*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.komet.gui.provider.concept.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.*;
import javafx.util.Duration;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.alert.AlertObject;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.commit.CommitTask;
import sh.isaac.api.identity.IdentifiedObject;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.model.observable.ObservableDescriptionDialect;
import sh.isaac.model.observable.version.ObservableConceptVersionImpl;
import sh.isaac.model.observable.version.ObservableLogicGraphVersionImpl;
import sh.komet.gui.contract.GuiConceptBuilder;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.interfaces.DetailNodeAbstract;
import sh.komet.gui.interfaces.ExplorationNodeAbstract;
import sh.komet.gui.style.PseudoClasses;
import static sh.komet.gui.style.PseudoClasses.UNCOMMITTED_PSEUDO_CLASS;
import sh.komet.gui.style.StyleClasses;
import static sh.komet.gui.style.StyleClasses.ADD_DESCRIPTION_BUTTON;
import sh.komet.gui.util.FxGet;
import static sh.komet.gui.util.FxUtils.setupHeaderPanel;

/**
 *
 * @author kec
 */
public class ConceptBuilderNode extends ExplorationNodeAbstract implements GuiConceptBuilder {

    private static final int TRANSITION_OFF_TIME = 250;
    private static final int TRANSITION_ON_TIME = 350;

    {
        titleProperty.setValue("Concept builder");
        toolTipProperty.setValue("Concept builder");
        menuIconProperty.setValue(Iconography.NEW_CONCEPT.getIconographic());
    }
    private final VBox componentPanelBox = new VBox(8);
    private final ScrollPane scrollPane;
    private final Button addDescriptionButton = new Button("+ Add");
    private final BorderPane detailPane = new BorderPane();

    private final Button newConceptButton = new Button("New concept");
    private final Button commitButton = new Button("Commit");
    private final Button cancelButton = new Button("Cancel");
    private final TextField textField = new TextField("New concept");
    private UUID conceptUuid;

    private final ToolBar builderToolbar = new ToolBar(newConceptButton);

    private ObservableConceptVersionImpl conceptVersion;
    private final ObservableList<ObservableDescriptionDialect> descriptions = FXCollections.observableArrayList();

    private ObservableLogicGraphVersionImpl statedDefinition;
    protected ConceptBuilderComponentPanel conceptPanel;

    public ConceptBuilderNode(ViewProperties viewProperties, IsaacPreferences preferences) {
        super(viewProperties, viewProperties.getUnlinkedActivityFeed());
        this.detailPane.setCenter(componentPanelBox);
        this.detailPane.setTop(builderToolbar);
        this.scrollPane = new ScrollPane(detailPane);
        this.newConceptButton.setOnAction(this::newConcept);
        this.addDescriptionButton.setOnAction(this::newDescription);
        this.commitButton.setOnAction(this::commit);
        this.cancelButton.setOnAction(this::cancel);
        this.cancelButton.getStyleClass()
                .add(StyleClasses.CANCEL_BUTTON.toString());
        this.cancelButton.setOnAction(this::cancel);
        this.commitButton.getStyleClass()
                .add(StyleClasses.COMMIT_BUTTON.toString());
        this.componentPanelBox.getStyleClass()
                .add(StyleClasses.COMPONENT_DETAIL_BACKGROUND.toString());
        this.componentPanelBox.setFillWidth(true);
        this.addDescriptionButton.getStyleClass()
                .setAll(ADD_DESCRIPTION_BUTTON.toString());
        this.scrollPane.setFitToWidth(true);
        this.scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        FxGet.builders().add(this);
    }

    @Override
    public Node getMenuIconGraphic() {
        return Iconography.NEW_CONCEPT.getIconographic();
    }

    @Override
    public void savePreferences() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void initializeBuilder(String conceptName) {
        newConcept(null);
        textField.setText(conceptName);
    }

    private void newDescription(Event event) {
        ObservableDescriptionDialect newDescriptionDialect = new ObservableDescriptionDialect(conceptUuid, MetaData.ENGLISH_LANGUAGE____SOLOR.getNid());
        descriptions.add(newDescriptionDialect);
        newDescriptionDialect.getDescription().setDescriptionTypeConceptNid(MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getNid());
        newDescriptionDialect.getDescription().setStatus(Status.ACTIVE, null);
        newDescriptionDialect.getDialect().setStatus(Status.ACTIVE, null);
        layoutBuilderComponents();
    }

    private void newConcept(Event event) {
        builderToolbar.getItems().clear();
        descriptions.clear();
        conceptUuid = Get.newUuidWithAssignment();
        //Region spacer = new Region();
        HBox.setHgrow(textField, Priority.ALWAYS);
        textField.setMinWidth(Region.USE_PREF_SIZE);
        builderToolbar.getItems().addAll(textField, cancelButton, commitButton);
        builderToolbar.getStyleClass().add(StyleClasses.COMPONENT_PANEL.toString());
        builderToolbar.pseudoClassStateChanged(UNCOMMITTED_PSEUDO_CLASS, true);
        detailPane.getStyleClass().add(StyleClasses.COMPONENT_PANEL.toString());
        detailPane.pseudoClassStateChanged(UNCOMMITTED_PSEUDO_CLASS, true);
 
        textField.setText("New concept");
        textField.requestFocus();
        textField.selectAll();

        this.conceptVersion = new ObservableConceptVersionImpl(conceptUuid, MetaData.SOLOR_CONCEPT_ASSEMBLAGE____SOLOR.getNid());
        this.conceptVersion.setStatus(Status.ACTIVE, null);

        ObservableDescriptionDialect fqnDescriptionDialect;
        ObservableDescriptionDialect namDescriptionDialect;
        ObservableDescriptionDialect defDescriptionDialect;

        fqnDescriptionDialect = new ObservableDescriptionDialect(conceptUuid, MetaData.ENGLISH_LANGUAGE____SOLOR.getNid());
        descriptions.add(fqnDescriptionDialect);

        fqnDescriptionDialect.getDescription().setDescriptionTypeConceptNid(MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getNid());
        fqnDescriptionDialect.getDescription().setStatus(Status.ACTIVE, null);
        fqnDescriptionDialect.getDescription().setText(textField.getText());
        fqnDescriptionDialect.getDescription().textProperty().bindBidirectional(textField.textProperty());
        fqnDescriptionDialect.getDialect().setStatus(Status.ACTIVE, null);

        namDescriptionDialect = new ObservableDescriptionDialect(conceptUuid, MetaData.ENGLISH_LANGUAGE____SOLOR.getNid());
        descriptions.add(namDescriptionDialect);
        namDescriptionDialect.getDescription().setDescriptionTypeConceptNid(MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getNid());
        namDescriptionDialect.getDescription().setStatus(Status.ACTIVE, null);
        namDescriptionDialect.getDialect().setStatus(Status.ACTIVE, null);

        defDescriptionDialect = new ObservableDescriptionDialect(conceptUuid, MetaData.ENGLISH_LANGUAGE____SOLOR.getNid());
        descriptions.add(defDescriptionDialect);
        defDescriptionDialect.getDescription().setStatus(Status.ACTIVE, null);
        defDescriptionDialect.getDialect().setStatus(Status.ACTIVE, null);
        defDescriptionDialect.getDescription().setDescriptionTypeConceptNid(MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR.getNid());

        this.statedDefinition = new ObservableLogicGraphVersionImpl(conceptUuid, viewProperties.getManifoldCoordinate().getLogicCoordinate().getStatedAssemblageNid());
        this.statedDefinition.setStatus(Status.ACTIVE, null);
        this.statedDefinition.assemblageNidProperty().set(viewProperties.getManifoldCoordinate().getLogicCoordinate().getStatedAssemblageNid());

        layoutBuilderComponents();
    }

    private void layoutBuilderComponents() {
        componentPanelBox.getChildren().clear();
        final ParallelTransition parallelTransition = new ParallelTransition();
        this.conceptPanel = new ConceptBuilderComponentPanel(viewProperties, conceptVersion, false, textField.textProperty());
        parallelTransition.getChildren().add(addComponent(conceptPanel, new Insets(10, 5, 1, 5)));
        AnchorPane descriptionHeader = setupHeaderPanel("DESCRIPTIONS", addDescriptionButton, null);
        descriptionHeader.pseudoClassStateChanged(PseudoClasses.DESCRIPTION_PSEUDO_CLASS, true);
        parallelTransition.getChildren()
                .add(addNode(descriptionHeader));

        for (ObservableDescriptionDialect descDialect : descriptions) {
            ConceptBuilderComponentPanel descPanel = new ConceptBuilderComponentPanel(viewProperties, descDialect, false, textField.textProperty());
            parallelTransition.getChildren().add(addComponent(descPanel));
        }
        AnchorPane definitionHeader = setupHeaderPanel("AXIOMS", null);
        definitionHeader.pseudoClassStateChanged(PseudoClasses.LOGICAL_DEFINITION_PSEUDO_CLASS, true);
        parallelTransition.getChildren()
                .add(addNode(definitionHeader));
        ConceptBuilderComponentPanel logicPanel = new ConceptBuilderComponentPanel(viewProperties, statedDefinition, false, textField.textProperty());
        parallelTransition.getChildren()
                .add(addComponent(logicPanel));

        parallelTransition.play();
    }

    private Animation addNode(AnchorPane header) {
        header.setOpacity(0);
        VBox.setMargin(header, new Insets(1, 5, 1, 5));
        componentPanelBox.getChildren()
                .add(header);

        FadeTransition ft = new FadeTransition(Duration.millis(TRANSITION_ON_TIME), header);

        ft.setFromValue(0);
        ft.setToValue(1);
        return ft;
    }

    private Animation addComponent(ConceptBuilderComponentPanel panel) {
        return this.addComponent(panel, new Insets(1, 5, 1, 5));
    }

    private Animation addComponent(ConceptBuilderComponentPanel panel, Insets insets) {

        panel.setOpacity(0);
        VBox.setMargin(panel, insets);
        componentPanelBox.getChildren()
                .add(panel);

        FadeTransition ft = new FadeTransition(Duration.millis(TRANSITION_ON_TIME), panel);

        ft.setFromValue(0);
        ft.setToValue(1);
        return ft;
    }

    private void commit(Event event) {
        ObservableVersion[] versionsToCommit;
        try {
            versionsToCommit = getVersionsToCommit();
            Transaction transaction = Get.commitService().newTransaction(Optional.of("Concept builder commit"), ChangeCheckerMode.ACTIVE);
            CommitTask commitTask = transaction.commitObservableVersions("Lambda graph edit", versionsToCommit);
            Get.executor().execute(() -> {
                try {
                    Optional<CommitRecord> commitRecord = commitTask.get();
                    completeCommit(commitTask, commitRecord);
                    Platform.runLater(() -> getActivityFeed().feedSelectionProperty().setAll(Get.concept(conceptUuid)));
                } catch (InterruptedException | ExecutionException ex) {
                    FxGet.dialogs().showErrorDialog("Error during commit", ex);
                }
            });
        } catch (IllegalStateException ex) {
            // TODO alert user that content is not sufficiently formed to submit for commit. 
            FxGet.dialogs().showErrorDialog("Error during commit", ex);
            return;
        }

    }


    private void cancel(Event event) {
        builderToolbar.getStyleClass().remove(StyleClasses.COMPONENT_PANEL.toString());
        builderToolbar.pseudoClassStateChanged(UNCOMMITTED_PSEUDO_CLASS, false);
        detailPane.getStyleClass().remove(StyleClasses.COMPONENT_PANEL.toString());
        detailPane.pseudoClassStateChanged(UNCOMMITTED_PSEUDO_CLASS, false);

        builderToolbar.getItems().clear();
        builderToolbar.getItems().addAll(newConceptButton);
        componentPanelBox.getChildren().clear();
    }
    private void completeCommit(CommitTask commitTask, Optional<CommitRecord> commitRecord) {
        if (commitRecord.isPresent()) {
            Platform.runLater(() -> {
                builderToolbar.getStyleClass().remove(StyleClasses.COMPONENT_PANEL.toString());
                builderToolbar.pseudoClassStateChanged(UNCOMMITTED_PSEUDO_CLASS, false);
                detailPane.getStyleClass().remove(StyleClasses.COMPONENT_PANEL.toString());
                detailPane.pseudoClassStateChanged(UNCOMMITTED_PSEUDO_CLASS, false);

                builderToolbar.getItems().clear();
                builderToolbar.getItems().addAll(newConceptButton);
                componentPanelBox.getChildren().clear();
            });
        } else {
            for (AlertObject alert : commitTask.getAlerts()) {
                switch (alert.getAlertType()) {
                    case ERROR:
                        FxGet.dialogs().showErrorDialog(alert.getAlertTitle(), alert.getAlertCategory().toString(),
                                alert.getAlertDescription(), componentPanelBox.getScene().getWindow());
                        break;
                    case INFORMATION:
                        FxGet.dialogs().showInformationDialog(alert.getAlertTitle(),
                                alert.getAlertDescription(), componentPanelBox.getScene().getWindow());
                        break;
                    case WARNING:
                        FxGet.dialogs().showInformationDialog(alert.getAlertTitle(),
                                alert.getAlertDescription(), componentPanelBox.getScene().getWindow());
                        break;
                }
            }
        }

    }

    private ObservableVersion[] getVersionsToCommit() throws IllegalStateException {
        List<ObservableVersion> versionsToCommit = new ArrayList<>();
        // Concept
        versionsToCommit.add(this.conceptVersion);
        
        // Assemblage
        versionsToCommit.addAll(Arrays.asList(this.conceptPanel.getVersionsToCommit()));

        // descriptions
        for (ObservableDescriptionDialect descDialect : descriptions) {
            if (descDialect.getDescription().getText() != null
                    && descDialect.getDescription().getText().length() > 2) {
                versionsToCommit.add(descDialect.getDescription());
                versionsToCommit.add(descDialect.getDialect());
            }
        }

        // Stated Axioms

        if (this.statedDefinition.getLogicalExpression().isMeaningful()) {
            versionsToCommit.add(this.statedDefinition);
        } else {
            throw new IllegalStateException("Logical expression is not meaningful");
        }
        for (ObservableVersion version: versionsToCommit) {
            version.setAuthorNid(FxGet.currentUser().getNid(), null);
            if (version.getModuleNid() == 0 || version.getModuleNid() == TermAux.UNINITIALIZED_COMPONENT_ID.getNid()) {
                version.setModuleNid(this.conceptVersion.getModuleNid(), null);
            }
            if (version.getPathNid() == 0 || version.getPathNid() == TermAux.UNINITIALIZED_COMPONENT_ID.getNid()) {
                version.setPathNid(this.conceptVersion.getPathNid(), null);
            }
        }
        return versionsToCommit.toArray(new ObservableVersion[versionsToCommit.size()]);
    }

    @Override
    public Optional<Node> getTitleNode() {
        return Optional.empty();
    }

    @Override
    public ActivityFeed getActivityFeed() {
        return this.viewProperties.getActivityFeed(ViewProperties.CONCEPT_BUILDER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getNode() {
        return this.scrollPane;
    }

    @Override
    public void close() {
        // nothing to do...
    }

    @Override
    public boolean canClose() {
        return true;
    }

}
