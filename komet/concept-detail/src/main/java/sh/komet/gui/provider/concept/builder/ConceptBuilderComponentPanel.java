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
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.model.observable.ObservableDescriptionDialect;
import sh.isaac.model.observable.version.ObservableConceptVersionImpl;
import sh.isaac.model.observable.version.ObservableLogicGraphVersionImpl;
import sh.komet.gui.control.concept.ConceptVersionEditor;
import sh.komet.gui.control.description.dialect.DescriptionDialectEditor;
import sh.komet.gui.control.axiom.AxiomView;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.style.PseudoClasses;
import sh.komet.gui.style.StyleClasses;

/**
 *
 * @author kec
 */
public class ConceptBuilderComponentPanel 
        extends Pane {

    public static final int FIRST_COLUMN_WIDTH = 32;

    //~--- fields --------------------------------------------------------------
    protected final int badgeWidth = 25;
    protected final ArrayList<Node> badges = new ArrayList<>();
    protected int columns = 10;
    protected Node  logicDetailTree = null;
    protected Node editorPane = null;
    protected final Text componentText = new Text();
    protected final Text componentType = new Text();
    protected final GridPane gridpane = new GridPane();
    protected final SimpleBooleanProperty isConcept = new SimpleBooleanProperty(false);
    protected final SimpleBooleanProperty isDescription = new SimpleBooleanProperty(false);
    protected final SimpleBooleanProperty isLogicalDefinition = new SimpleBooleanProperty(false);
    protected final int rowHeight = 25;
    protected int wrappingWidth = 300;
    protected final CheckBox revertCheckBox = new CheckBox();
    private final ObservableVersion observableVersion;
    private final Manifold manifold;
    protected int rows;

    //~--- initializers --------------------------------------------------------
    {
        isDescription.addListener(this::pseudoStateChanged);
        isLogicalDefinition.addListener(this::pseudoStateChanged);
        isConcept.addListener(this::pseudoStateChanged);
    }

    //~--- constructors --------------------------------------------------------
    public ConceptBuilderComponentPanel(Manifold manifold, ObservableVersion observableVersion) {
        this.manifold = manifold;
        this.observableVersion = observableVersion;
        this.getChildren()
                .add(gridpane);
        componentType.getStyleClass()
                .add(StyleClasses.COMPONENT_VERSION_WHAT_CELL.toString());
        componentText.getStyleClass()
                .add(StyleClasses.COMPONENT_TEXT.toString());
        componentText.setWrappingWidth(wrappingWidth);
        componentText.layoutBoundsProperty()
                .addListener(this::textLayoutChanged);
        this.widthProperty()
                .addListener(this::widthChanged);


        if (observableVersion instanceof ObservableDescriptionDialect) {
            isDescription.set(true);
            setupDescription((ObservableDescriptionDialect) observableVersion);
        }  else if (observableVersion instanceof LogicGraphVersion) {
            isLogicalDefinition.set(true);
            setupEl((ObservableLogicGraphVersionImpl) observableVersion);
        } else if (observableVersion instanceof ObservableConceptVersionImpl) {
            isConcept.set(true);
            setupConcept((ObservableConceptVersionImpl) observableVersion);
        } 

        // gridpane.gridLinesVisibleProperty().set(true);
        this.getStyleClass()
                .add(StyleClasses.COMPONENT_PANEL.toString());
    }
    //~--- methods -------------------------------------------------------------
    protected final void setupConcept(ObservableConceptVersionImpl conceptVersion) {
            componentType.setText(" CON");
            componentText.setText(
                    "\n" + conceptVersion.getStatus() + " in " + getManifold().getPreferredDescriptionText(
                    conceptVersion.getModuleNid()) + " on " + getManifold().getPreferredDescriptionText(
                    conceptVersion.getPathNid()));  
            ConceptVersionEditor editor = new ConceptVersionEditor(manifold);
            editor.setValue(conceptVersion);
            this.editorPane = editor.getEditor();
    }
    protected final void setupEl(ObservableLogicGraphVersionImpl logicGraphVersion) {
        PremiseType premiseType = PremiseType.STATED;
        badges.add(Iconography.STATED.getIconographic());
        componentType.setText(" EL++");
      
        this.logicDetailTree = AxiomView.create(logicGraphVersion, premiseType, manifold);
        this.editorPane = this.logicDetailTree;
    }

    protected final void setupDescription(ObservableDescriptionDialect descriptionDialect) {
        DescriptionDialectEditor editor = new DescriptionDialectEditor(this.manifold);
        editor.setValue(descriptionDialect);
        this.editorPane = editor.getEditor();
        componentText.setText(descriptionDialect.getDescription().getText());

            int descriptionType = descriptionDialect.getDescription().getDescriptionTypeConceptNid();

            if (descriptionType == TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid()) {
                componentType.setText(" FQN");
            } else if (descriptionType == TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid()) {
                componentType.setText(" NĀM");
            } else if (descriptionType == TermAux.DEFINITION_DESCRIPTION_TYPE.getNid()) {
                componentType.setText(" DEF");
            } else {
                componentType.setText(getManifold().getPreferredDescriptionText(descriptionType));
            }

    }

    protected void textLayoutChanged(ObservableValue<? extends Bounds> bounds, Bounds oldBounds, Bounds newBounds) {
        redoLayout();
    }

    protected void widthChanged(ObservableValue<? extends Number> observableWidth, Number oldWidth, Number newWidth) {
        redoLayout();
    }

    private void pseudoStateChanged(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (observable == isDescription) {
            this.pseudoClassStateChanged(PseudoClasses.DESCRIPTION_PSEUDO_CLASS, newValue);
        } else if (observable == isLogicalDefinition) {
            this.pseudoClassStateChanged(PseudoClasses.LOGICAL_DEFINITION_PSEUDO_CLASS, newValue);
        } else if (observable == isConcept) {
            this.pseudoClassStateChanged(PseudoClasses.CONCEPT_PSEUDO_CLASS, newValue);
        }
    }

    private void redoLayout() {
        if (getParent() != null) {
            getParent().applyCss();
            getParent().layout();
        }
        gridpane.getChildren().clear();
        double doubleRows = componentText.boundsInLocalProperty()
                .get()
                .getHeight() / rowHeight;
        int rowsOfText = (int) doubleRows + 1;
        gridpane.getRowConstraints()
                .clear();

        gridpane.setMinWidth(layoutBoundsProperty().get()
                .getWidth());
        gridpane.setPrefWidth(layoutBoundsProperty().get()
                .getWidth());
        gridpane.setMaxWidth(layoutBoundsProperty().get()
                .getWidth());
        setupColumns();
        wrappingWidth = (int) (layoutBoundsProperty().get()
                .getWidth() - (5 * badgeWidth));
        if (componentText.getWrappingWidth() != wrappingWidth) {
            componentText.setWrappingWidth(wrappingWidth);
            // will call redoLayout, so should not continue to layout...
        } else {

            gridpane.getChildren()
                    .remove(componentType);
            GridPane.setConstraints(componentType, 0, 0, 2, 1, HPos.LEFT, VPos.TOP, Priority.NEVER, Priority.NEVER);
            gridpane.getChildren()
                    .add(componentType);  // next is 3

            int gridRow = 0;

            if (editorPane != null) {
                gridpane.getChildren()
                        .remove(componentText);
                gridpane.getChildren()
                        .remove(editorPane);

                GridPane.setConstraints(editorPane,
                        3,
                        gridRow++,
                        columns - 4,
                        5,
                        HPos.LEFT,
                        VPos.TOP,
                        Priority.ALWAYS,
                        Priority.NEVER, 
                        new Insets(5,1,3,1));
                gridpane.getChildren()
                        .add(editorPane);
            } else {
                componentText.getLayoutBounds()
                        .getHeight();
                gridpane.getChildren()
                        .remove(componentText);
                GridPane.setConstraints(
                        componentText,
                        3,
                        gridRow++,
                        columns - 4,
                        (int) rowsOfText,
                        HPos.LEFT,
                        VPos.TOP,
                        Priority.ALWAYS,
                        Priority.NEVER);
                gridpane.getChildren()
                        .add(componentText);
                gridpane.getRowConstraints()
                        .add(new RowConstraints(rowHeight));
            }

            boolean firstBadgeAdded = false;

            for (int i = 0; i < badges.size();) {
                for (int row = gridRow; i < badges.size(); row++) {
                    this.rows = row;
                    gridpane.getRowConstraints()
                            .add(new RowConstraints(rowHeight));

                    if (row + 1 <= rowsOfText) {
                        for (int column = 0; (column < 3) && (i < badges.size()); column++) {
                            if (firstBadgeAdded && (column == 0)) {
                                column = 1;
                                firstBadgeAdded = true;
                            }

                            setupBadge(badges.get(i++), column, row);
                        }
                    } else {
                        for (int column = 0; (column < columns) && (i < badges.size()); column++) {
                            if (firstBadgeAdded && (column == 0)) {
                                column = 1;
                                firstBadgeAdded = true;
                            }

                            setupBadge(badges.get(i++), column, row);
                        }
                    }
                }
            }
        }

    }

    private void setupBadge(Node badge, int column, int row) {
        gridpane.getChildren()
                .remove(badge);
        GridPane.setConstraints(
                badge,
                column,
                row,
                1,
                1,
                HPos.CENTER,
                VPos.CENTER,
                Priority.NEVER,
                Priority.NEVER,
                new Insets(2));
        gridpane.getChildren()
                .add(badge);

        if (!badge.getStyleClass()
                .contains(StyleClasses.COMPONENT_BADGE.toString())) {
            badge.getStyleClass()
                    .add(StyleClasses.COMPONENT_BADGE.toString());
        }
    }

    private void setupColumns() {
        if (this.getParent() != null) {
            this.columns = (int) (getLayoutBounds().getWidth() / badgeWidth) - 1;

            if (this.columns < 6) {
                this.columns = 6;
            }

            gridpane.getColumnConstraints()
                    .clear();

            for (int i = 0; i < this.columns; i++) {
                if (i == 0) {
                    gridpane.getColumnConstraints()
                            .add(new ColumnConstraints(FIRST_COLUMN_WIDTH));
                } else {
                    gridpane.getColumnConstraints()
                            .add(new ColumnConstraints(badgeWidth));
                }
            }
        }
    }

    @Override
    public final ObservableList<Node> getChildren() {
        return super.getChildren();
    }

    public int getColumns() {
        return columns;
    }

    /**
     * @return the manifold
     */
    public Manifold getManifold() {
        return manifold;
    }

    public int getRows() {
        return rows;
    }
}
