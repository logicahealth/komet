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
package sh.komet.gui.search.extended;

import java.util.Optional;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.provider.query.search.CompositeSearchResult;
import sh.komet.gui.interfaces.ConceptExplorationNode;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class ExtendedSearchConceptExplorationNode implements ConceptExplorationNode {

    private final ExtendedSearchViewController controller;
    private final Manifold manifold;
    private final SimpleObjectProperty<ConceptSpecification> conceptSpecification = new SimpleObjectProperty<>();

    public ExtendedSearchConceptExplorationNode(ExtendedSearchViewController controller, Manifold manifold) {
        this.controller = controller;
        this.manifold = manifold;
        controller.getSearchResults().getSelectionModel().selectedItemProperty().addListener(this::selectedSearchResultChanged);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getMenuIcon() {
        return Iconography.TARGET.getIconographic();
    }

    @Override
    public ReadOnlyProperty<String> getToolTip() {
        return new SimpleStringProperty("Shows all of the Dynamic Semantics in the system");
    }

    @Override
    public Optional<Node> getTitleNode() {
        return Optional.empty();
    }

    @Override
    public ReadOnlyProperty<String> getTitle() {
        return new SimpleStringProperty("Extended Search");
    }

    @Override
    public Node getNode() {
        return controller.getRoot();
    }

    @Override
    public Manifold getManifold() {
        return manifold;
    }
    
    private void selectedSearchResultChanged(ObservableValue<? extends CompositeSearchResult> observable, CompositeSearchResult oldValue, CompositeSearchResult newValue) {
        if (newValue != null) {
            conceptSpecification.setValue(newValue.getContainingConcept());
        } else {
            conceptSpecification.set(null);
        }
    }

    @Override
    public ReadOnlyObjectProperty<ConceptSpecification> selectedConceptSpecification() {
        return conceptSpecification;
    }

    @Override
    public void focusOnInput() {
        controller.getSearchText().requestFocus();
        controller.getSearchText().selectAll();
    }

    @Override
    public void focusOnResults() {
        controller.getSearchResults().requestFocus();
        controller.getSearchResults().getSelectionModel().selectFirst();
    }
    
    
}

