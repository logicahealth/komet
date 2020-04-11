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
package sh.komet.gui.provider.concept.comparison;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import sh.isaac.api.Status;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.control.axiom.AxiomView;
import sh.komet.gui.control.concept.ConceptLabelToolbar;
import sh.komet.gui.control.concept.ManifoldLinkedConceptLabel;
import sh.komet.gui.interfaces.DetailNode;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.style.StyleClasses;

/**
 *
 * @author kec
 */
public class ExpressionView implements DetailNode, Supplier<List<MenuItem>> {

    private final BorderPane conceptDetailPane = new BorderPane();
    private final SimpleStringProperty titleProperty = new SimpleStringProperty("empty");
    private final SimpleStringProperty toolTipProperty = new SimpleStringProperty("empty");
    private final SimpleObjectProperty<LogicalExpression> expressionProperty = new SimpleObjectProperty<>();
    private final SimpleObjectProperty menuIconProperty = new SimpleObjectProperty(Iconography.LAMBDA.getIconographic());

    private final SimpleObjectProperty<Manifold> manifoldProperty = new SimpleObjectProperty<>();
    private final SimpleIntegerProperty selectionIndexProperty = new SimpleIntegerProperty(0);
    private ManifoldLinkedConceptLabel titleLabel = null;
    private final ConceptLabelToolbar conceptLabelToolbar;

    //~--- constructors --------------------------------------------------------
    public ExpressionView(Manifold conceptDetailManifold) {
        this.manifoldProperty.set(conceptDetailManifold);
        this.conceptLabelToolbar = ConceptLabelToolbar.make(this.manifoldProperty, this.selectionIndexProperty,
                this, Optional.of(false));
        conceptDetailPane.setTop(this.conceptLabelToolbar.getToolbarNode());
        conceptDetailPane.getStyleClass().add(StyleClasses.CONCEPT_DETAIL_PANE.toString());
        expressionProperty().addListener((observable, oldValue, newValue) -> {
            getLogicDetail();
        });
    }

    @Override
    public void savePreferences() {
        throw new UnsupportedOperationException();
    }

    public final SimpleObjectProperty<LogicalExpression> expressionProperty() {
        return expressionProperty;
    }
    
    public LogicalExpression getExpression() {
        return expressionProperty.get();
    }
    
    public void setExpression(LogicalExpression expression) {
        expressionProperty.set(expression);
    }
    
    

    @Override
    public SimpleObjectProperty getMenuIconProperty() {
       return menuIconProperty;
    }


    private void getLogicDetail() {
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        conceptDetailPane.setCenter(splitPane);
        if (expressionProperty.get() != null) {
            splitPane.getItems().add(AxiomView.createWithCommitPanel(expressionProperty.get(), PremiseType.STATED, getManifold()));
        } else {
            conceptDetailPane.setCenter(new Label("No stated form"));
        }
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public ReadOnlyProperty<String> getTitle() {
        return this.titleProperty;
    }

    @Override
    public Optional<Node> getTitleNode() {
        // MaterialDesignIcon.LAMBDA
        if (titleLabel == null) {
            this.titleLabel = new ManifoldLinkedConceptLabel(this.manifoldProperty, this.selectionIndexProperty, ManifoldLinkedConceptLabel::setPreferredText, this);
            this.titleLabel.setGraphic(Iconography.LAMBDA.getIconographic());
            this.titleProperty.set("");
        }
        return Optional.of(titleLabel);
    }

    @Override
    public ReadOnlyProperty<String> getToolTip() {
        return this.toolTipProperty;
    }

    @Override
    public List<MenuItem> get() {
        List<MenuItem> assemblageMenuList = new ArrayList<>();
        // No extra menu items added yet. 
        return assemblageMenuList;
    }

    @Override
    public Manifold getManifold() {
        return this.manifoldProperty.get();
    }

    @Override
    public boolean selectInTabOnChange() {
        return conceptLabelToolbar.getFocusTabOnConceptChange().get();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public Node getNode() {
        return conceptDetailPane;
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
