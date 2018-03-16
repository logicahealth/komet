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
import java.util.function.Consumer;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import sh.isaac.model.observable.version.ObservableDescriptionVersionImpl;
import sh.isaac.model.statement.MeasureImpl;
import sh.komet.gui.control.list.ListEditor;
import sh.komet.gui.control.measure.MeasureEditor;
import sh.komet.gui.control.version.VersionEditor;
import sh.komet.gui.interfaces.DetailNode;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class ConceptBuilderNode implements DetailNode {
    private final Manifold manifold;
    private final SimpleStringProperty titleProperty = new SimpleStringProperty("Concept builder");
    private final SimpleStringProperty toolTipProperty = new SimpleStringProperty("Concept builder");
    private final BorderPane builderBorderPane = new BorderPane();
    private final Button newConceptButton = new Button("New concept");
    private final Button commitButton = new Button("Commit");
    private final Button cancelButton = new Button("Cancel");
    
    private final ToolBar builderToolbar = new ToolBar(newConceptButton);
    

    public ConceptBuilderNode(Manifold manifold, Consumer<Node> nodeConsumer) {
        this.manifold = manifold;
        builderBorderPane.setTop(builderToolbar);
        nodeConsumer.accept(builderBorderPane);
        newConceptButton.setOnAction(this::newConcept);
        commitButton.setOnAction(this::commit);
        cancelButton.setOnAction(this::cancel);
    }

    private void newConcept(Event event) {
        builderToolbar.getItems().clear();
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.setMinWidth(Region.USE_PREF_SIZE);
        builderToolbar.getItems().addAll(spacer, cancelButton, commitButton);
        ListEditor<MeasureImpl> conceptEditor = new ListEditor<>(manifold, 
                () -> new MeasureImpl(manifold), 
                (Manifold m) -> new MeasureEditor(m));
        builderBorderPane.setCenter(conceptEditor.getEditor());
        ObservableDescriptionVersionImpl desc = new ObservableDescriptionVersionImpl();
        VersionEditor descEditor = new VersionEditor(desc, manifold);
        builderBorderPane.setBottom(descEditor.getEditor());
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
