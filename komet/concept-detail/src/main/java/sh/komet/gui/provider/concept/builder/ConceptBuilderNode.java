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

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.alert.AlertObject;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.commit.CommitTask;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.model.observable.ObservableDescriptionDialect;
import sh.isaac.model.observable.version.ObservableConceptVersionImpl;
import sh.isaac.model.observable.version.ObservableLogicGraphVersionImpl;
import sh.komet.gui.interfaces.DetailNode;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.style.PseudoClasses;
import sh.komet.gui.style.StyleClasses;
import static sh.komet.gui.style.StyleClasses.ADD_DESCRIPTION_BUTTON;
import sh.komet.gui.util.FxGet;
import static sh.komet.gui.util.FxUtils.setupHeaderPanel;

/**
 *
 * @author kec
 */
public class ConceptBuilderNode implements DetailNode {

    private static final int TRANSITION_OFF_TIME = 250;
    private static final int TRANSITION_ON_TIME = 750;

    private final Manifold manifold;
    private final SimpleStringProperty titleProperty = new SimpleStringProperty("Concept builder");
    private final SimpleStringProperty toolTipProperty = new SimpleStringProperty("Concept builder");
    private final VBox componentPanelBox = new VBox(8);
    private final BorderPane builderBorderPane = new BorderPane(componentPanelBox);
    private final ScrollPane scrollPane = new ScrollPane(builderBorderPane);
    private final Button addDescriptionButton = new Button("+ Add");

    private final Button newConceptButton = new Button("New concept");
    private final Button commitButton = new Button("Commit");
    private final Button cancelButton = new Button("Cancel");
    private final TextField textField = new TextField("New concept");
    private UUID conceptUuid;

    private final ToolBar builderToolbar = new ToolBar(newConceptButton);

    private ObservableConceptVersionImpl conceptVersion;
    private ObservableDescriptionDialect fqnDescriptionDialect;
    private ObservableDescriptionDialect namDescriptionDialect;
    private ObservableDescriptionDialect defDescriptionDialect;
    private ObservableLogicGraphVersionImpl statedDefinition;

    public ConceptBuilderNode(Manifold manifold) {
        this.manifold = manifold;
        builderBorderPane.setTop(builderToolbar);
        newConceptButton.setOnAction(this::newConcept);
        commitButton.setOnAction(this::commit);
        cancelButton.setOnAction(this::cancel);
        cancelButton.getStyleClass()
                .add(StyleClasses.CANCEL_BUTTON.toString());
        cancelButton.setOnAction(this::cancel);
        commitButton.getStyleClass()
                .add(StyleClasses.COMMIT_BUTTON.toString());
        componentPanelBox.getStyleClass()
                .add(StyleClasses.COMPONENT_DETAIL_BACKGROUND.toString());
        componentPanelBox.setFillWidth(true);
        addDescriptionButton.getStyleClass()
                .setAll(ADD_DESCRIPTION_BUTTON.toString());
        this.scrollPane.setFitToWidth(true);
        this.scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    }

    private void newConcept(Event event) {
        builderToolbar.getItems().clear();
        conceptUuid = UUID.randomUUID();
        //Region spacer = new Region();
        HBox.setHgrow(textField, Priority.ALWAYS);
        textField.setMinWidth(Region.USE_PREF_SIZE);
        builderToolbar.getItems().addAll(textField, cancelButton, commitButton);

//        ListEditor<ObservableDescriptionDialect> descriptionEditor = new ListEditor<>(manifold, 
//                () -> new ObservableDescriptionDialect(conceptUuid), 
//                (Manifold m) -> new DescriptionDialectEditor(m));
//        
        textField.setText("New concept");
        textField.requestFocus();
        textField.selectAll();

        final ParallelTransition parallelTransition = new ParallelTransition();

        this.conceptVersion = new ObservableConceptVersionImpl(conceptUuid, MetaData.SOLOR_CONCEPT____SOLOR.getNid());
        ConceptBuilderComponentPanel conceptPanel = new ConceptBuilderComponentPanel(manifold, conceptVersion);
        parallelTransition.getChildren().add(addComponent(conceptPanel, new Insets(10, 5, 1, 5)));

        AnchorPane descriptionHeader = setupHeaderPanel("DESCRIPTIONS", addDescriptionButton);
        descriptionHeader.pseudoClassStateChanged(PseudoClasses.DESCRIPTION_PSEUDO_CLASS, true);
        parallelTransition.getChildren()
                .add(addNode(descriptionHeader));
        this.fqnDescriptionDialect = new ObservableDescriptionDialect(conceptUuid, MetaData.ENGLISH_LANGUAGE____SOLOR.getNid());
        fqnDescriptionDialect.getDescription().setText(textField.getText());
        fqnDescriptionDialect.getDescription().textProperty().bindBidirectional(textField.textProperty());

        ConceptBuilderComponentPanel fqnPanel = new ConceptBuilderComponentPanel(manifold, fqnDescriptionDialect);
        parallelTransition.getChildren()
                .add(addComponent(fqnPanel));

        this.namDescriptionDialect = new ObservableDescriptionDialect(conceptUuid, MetaData.ENGLISH_LANGUAGE____SOLOR.getNid());
        namDescriptionDialect.getDescription().setDescriptionTypeConceptNid(MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getNid());
        ConceptBuilderComponentPanel synPanel = new ConceptBuilderComponentPanel(manifold, namDescriptionDialect);
        parallelTransition.getChildren()
                .add(addComponent(synPanel));

        this.defDescriptionDialect = new ObservableDescriptionDialect(conceptUuid, MetaData.ENGLISH_LANGUAGE____SOLOR.getNid());
        defDescriptionDialect.getDescription().setDescriptionTypeConceptNid(MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR.getNid());
        ConceptBuilderComponentPanel defPanel = new ConceptBuilderComponentPanel(manifold, defDescriptionDialect);
        parallelTransition.getChildren()
                .add(addComponent(defPanel));

        AnchorPane definitionHeader = setupHeaderPanel("AXIOMS", null);
        definitionHeader.pseudoClassStateChanged(PseudoClasses.LOGICAL_DEFINITION_PSEUDO_CLASS, true);
        parallelTransition.getChildren()
                .add(addNode(definitionHeader));

        this.statedDefinition = new ObservableLogicGraphVersionImpl(conceptUuid, manifold.getLogicCoordinate().getStatedAssemblageNid());
        statedDefinition.assemblageNidProperty().set(manifold.getStatedAssemblageNid());
        ConceptBuilderComponentPanel logicPanel = new ConceptBuilderComponentPanel(manifold, statedDefinition);
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

    private void cancel(Event event) {
        builderToolbar.getItems().clear();
        builderToolbar.getItems().addAll(newConceptButton);
        componentPanelBox.getChildren().clear();
    }

    private void commit(Event event) {
        ObservableVersion[] versionsToCommit;
        try {
            versionsToCommit = getVersionsToCommit();
        } catch (IllegalStateException ex) {
            // TODO alert user that content is not sufficiently formed to submit for commit. 
            FxGet.dialogs().showErrorDialog("Error during commit", ex);
            return;
        }
        CommitTask commitTask = Get.commitService().commit(manifold.getEditCoordinate(), "", versionsToCommit);
        Get.executor().execute(() -> {
            try {
                Optional<CommitRecord> commitRecord = commitTask.get();
                completeCommit(commitTask, commitRecord);
            } catch (InterruptedException | ExecutionException ex) {
                FxGet.dialogs().showErrorDialog("Error during commit", ex);
            }
        });
    }

    private void completeCommit(CommitTask commitTask, Optional<CommitRecord> commitRecord) {
        if (commitRecord.isPresent()) {
            Platform.runLater(() -> {
                builderToolbar.getItems().clear();
                builderToolbar.getItems().addAll(newConceptButton);
                componentPanelBox.getChildren().clear();
            });
        } else {
            // TODO show errors. 
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
        // FQN
        versionsToCommit.add(this.fqnDescriptionDialect.getDescription());
        versionsToCommit.add(this.fqnDescriptionDialect.getDialect());
        // NAM - optional
        if (this.namDescriptionDialect.getDescription().getText() != null
                && this.namDescriptionDialect.getDescription().getText().length() > 2) {
            versionsToCommit.add(this.namDescriptionDialect.getDescription());
            versionsToCommit.add(this.namDescriptionDialect.getDialect());
        }

        // DEF - optional
        if (this.defDescriptionDialect.getDescription().getText() != null
                && this.defDescriptionDialect.getDescription().getText().length() > 2) {
            versionsToCommit.add(this.defDescriptionDialect.getDescription());
            versionsToCommit.add(this.defDescriptionDialect.getDialect());
        }
        // Stated Axioms
        
        if (this.statedDefinition.getLogicalExpression().isMeaningful()) {
            versionsToCommit.add(this.statedDefinition);
        } else {
            throw new IllegalStateException("Logical expression is not meaningful");
        }
        return versionsToCommit.toArray(new ObservableVersion[versionsToCommit.size()]);
    }

    @Override
    public ReadOnlyProperty<String> getTitle() {
        return titleProperty;
    }

    @Override
    public Optional<Node> getTitleNode() {
        return Optional.empty();
    }

    @Override
    public ReadOnlyProperty<String> getToolTip() {
        return toolTipProperty;
    }

    @Override
    public boolean selectInTabOnChange() {
        return false;
    }

    @Override
    public Manifold getManifold() {
        return manifold;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public Node getNode() {
        return this.scrollPane;
    }
}
