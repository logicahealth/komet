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

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import sh.isaac.MetaData;
import sh.isaac.model.observable.ObservableDescriptionDialect;
import sh.isaac.model.observable.version.ObservableConceptVersionImpl;
import sh.isaac.model.observable.version.ObservableLogicGraphVersionImpl;
import sh.komet.gui.interfaces.DetailNode;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.style.PseudoClasses;
import sh.komet.gui.style.StyleClasses;
import static sh.komet.gui.style.StyleClasses.ADD_DESCRIPTION_BUTTON;
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
    

    public ConceptBuilderNode(Manifold manifold, Consumer<Node> nodeConsumer) {
        this.manifold = manifold;
        builderBorderPane.setTop(builderToolbar);
        nodeConsumer.accept(builderBorderPane);
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
        nodeConsumer.accept(this.scrollPane);
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
        
        ObservableConceptVersionImpl conceptVersion = new ObservableConceptVersionImpl(conceptUuid);
        ConceptBuilderComponentPanel conceptPanel = new ConceptBuilderComponentPanel(manifold, conceptVersion);  
        parallelTransition.getChildren().add(addComponent(conceptPanel, new Insets(10, 5, 1, 5)));
        
        AnchorPane descriptionHeader = setupHeaderPanel("DESCRIPTIONS", addDescriptionButton);
        descriptionHeader.pseudoClassStateChanged(PseudoClasses.DESCRIPTION_PSEUDO_CLASS, true);
        parallelTransition.getChildren()
                    .add(addNode(descriptionHeader));       
        ObservableDescriptionDialect descriptionDialect = new ObservableDescriptionDialect(conceptUuid);
        descriptionDialect.getDescription().setText(textField.getText());
        descriptionDialect.getDescription().textProperty().bindBidirectional(textField.textProperty());
        
        ConceptBuilderComponentPanel fqnPanel = new ConceptBuilderComponentPanel(manifold, descriptionDialect);  
        parallelTransition.getChildren()
                    .add(addComponent(fqnPanel));
        
        
        ObservableDescriptionDialect synDialect = new ObservableDescriptionDialect(conceptUuid);
        synDialect.getDescription().setDescriptionTypeConceptNid(MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getNid());
        ConceptBuilderComponentPanel synPanel = new ConceptBuilderComponentPanel(manifold, synDialect);  
        parallelTransition.getChildren()
                    .add(addComponent(synPanel));
        
        ObservableDescriptionDialect defDialect = new ObservableDescriptionDialect(conceptUuid);
        defDialect.getDescription().setDescriptionTypeConceptNid(MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR.getNid());
        ConceptBuilderComponentPanel defPanel = new ConceptBuilderComponentPanel(manifold, defDialect);  
        parallelTransition.getChildren()
                    .add(addComponent(defPanel));
        
         
        AnchorPane definitionHeader = setupHeaderPanel("AXIOMS", null);
        descriptionHeader.pseudoClassStateChanged(PseudoClasses.STATED_PSEUDO_CLASS, true);
        parallelTransition.getChildren()
                    .add(addNode(definitionHeader));  
        
        ObservableLogicGraphVersionImpl emptyDefinition = new ObservableLogicGraphVersionImpl(conceptUuid);
        emptyDefinition.assemblageNidProperty().set(manifold.getStatedAssemblageNid());
        ConceptBuilderComponentPanel logicPanel = new ConceptBuilderComponentPanel(manifold, emptyDefinition);  
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
        builderBorderPane.setCenter(null);
    }
    
    private void commit(Event event) {
        builderToolbar.getItems().clear();
        builderToolbar.getItems().addAll(newConceptButton);
        builderBorderPane.setCenter(null);
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
    
}
