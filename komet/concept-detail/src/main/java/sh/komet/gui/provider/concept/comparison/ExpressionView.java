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

import javafx.beans.property.*;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.identity.IdentifiedObject;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.control.axiom.AxiomView;
import sh.komet.gui.control.concept.ConceptLabelToolbar;
import sh.komet.gui.control.concept.ConceptLabelWithDragAndDrop;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.interfaces.DetailNodeAbstract;
import sh.komet.gui.style.StyleClasses;

/**
 *
 * @author kec
 */
public class ExpressionView extends DetailNodeAbstract implements Supplier<List<MenuItem>> {

    {
        titleProperty.setValue("empty");
        toolTipProperty.setValue("empty");
        menuIconProperty.setValue(Iconography.LAMBDA.getIconographic());
    }
     private final SimpleObjectProperty<LogicalExpression> expressionProperty = new SimpleObjectProperty<>();

    private final ConceptLabelToolbar conceptLabelToolbar;

    private final SimpleObjectProperty<ActivityFeed> activityFeedProperty = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<IdentifiedObject> identifiedObjectFocusProperty = new SimpleObjectProperty<>();

    //~--- constructors --------------------------------------------------------
    public ExpressionView(ViewProperties viewProperties, String activityGroupName, IsaacPreferences preferences) {
        super(viewProperties, viewProperties.getActivityFeed(activityGroupName), preferences);
        this.conceptLabelToolbar = ConceptLabelToolbar.make(this.viewProperties,
                this.identifiedObjectFocusProperty,
                ConceptLabelWithDragAndDrop::setPreferredText,
                this.selectionIndexProperty,
                () -> this.unlinkFromActivityFeed(),
                this.activityFeedProperty,
                Optional.of(true));
        detailPane.setTop(this.conceptLabelToolbar.getToolbarNode());
        detailPane.getStyleClass().add(StyleClasses.CONCEPT_DETAIL_PANE.toString());
        expressionProperty().addListener((observable, oldValue, newValue) -> {
            getLogicDetail();
        });
    }

    @Override
    public Node getMenuIconGraphic() {
        return Iconography.LAMBDA.getIconographic();
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

    @Override
    public void updateFocusedObject(IdentifiedObject component) {
        setExpression((LogicalExpression) component);
    }

    public void setExpression(LogicalExpression expression) {
        expressionProperty.set(expression);
    }
    
    private void getLogicDetail() {
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        detailPane.setCenter(splitPane);
        if (expressionProperty.get() != null) {
            splitPane.getItems().add(AxiomView.createWithCommitPanel(expressionProperty.get(), PremiseType.STATED, this.viewProperties));
        } else {
            detailPane.setCenter(new Label("No stated form"));
        }
    }

    //~--- get methods ---------------------------------------------------------

    @Override
    public List<MenuItem> get() {
        List<MenuItem> assemblageMenuList = new ArrayList<>();
        // No extra menu items added yet. 
        return assemblageMenuList;
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
        return detailPane;
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
