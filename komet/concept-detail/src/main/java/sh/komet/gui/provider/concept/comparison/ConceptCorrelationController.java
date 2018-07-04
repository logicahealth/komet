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

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.model.observable.ObservableFields;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;

/**
 * FXML Controller class
 *
 * @author kec
 */
public class ConceptCorrelationController {
    
    ObjectProperty<LogicalExpression> referenceExpressionProperty = new SimpleObjectProperty<>(this, 
                  ObservableFields.CORELATION_REFERENCE_EXPRESSION.toExternalString(), null);
    
    ObjectProperty<LogicalExpression> comparisonExpressionProperty = new SimpleObjectProperty<>(this, 
                  ObservableFields.CORELATION_COMPARISON_EXPRESSION.toExternalString(), null);

    ObjectProperty<LogicalExpression> corelationExpressionProperty = new SimpleObjectProperty<>(this, 
                  ObservableFields.CORELATION_EXPRESSION.toExternalString(), null);

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="referenceBorderPane"
    private BorderPane referenceBorderPane; // Value injected by FXMLLoader

    @FXML // fx:id="correlationBorderPane"
    private BorderPane correlationBorderPane; // Value injected by FXMLLoader

    @FXML // fx:id="comparisonBorderPane"
    private BorderPane comparisonBorderPane; // Value injected by FXMLLoader

    private Manifold manifold;
    
    private ExpressionView referenceExpressionView;
    private ExpressionView correlationExpressionView;
    private ExpressionView comparisonExpressionView;

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert referenceBorderPane != null : "fx:id=\"referenceBorderPane\" was not injected: check your FXML file 'ConceptComparison.fxml'.";
        assert correlationBorderPane != null : "fx:id=\"correlationBorderPane\" was not injected: check your FXML file 'ConceptComparison.fxml'.";
        assert comparisonBorderPane != null : "fx:id=\"comparisonBorderPane\" was not injected: check your FXML file 'ConceptComparison.fxml'.";
        referenceBorderPane.sceneProperty().addListener((observable, oldValue, newValue) -> {
            handleRefreshUserCss(null);
            this.referenceExpressionView.setExpression(CorrelationTestGenerator.makeReferenceExpression());
            this.comparisonExpressionView.setExpression(CorrelationTestGenerator.makeComparisonExpression());
        });

    }
    public void setManifold(Manifold manifold) {
        this.manifold = manifold;
        this.referenceExpressionView = new ExpressionView(manifold);
        this.referenceBorderPane.setCenter(this.referenceExpressionView.getNode());
        
        this.correlationExpressionView = new ExpressionView(manifold);
        this.correlationBorderPane.setCenter(this.correlationExpressionView.getNode());
        
        this.comparisonExpressionView = new ExpressionView(manifold);
        this.comparisonBorderPane.setCenter(this.comparisonExpressionView.getNode());
        
    }
    
    public void handleRefreshUserCss(ActionEvent event) {
        // "Feature" to make css editing/testing easy in the dev environment. 
        referenceBorderPane.getScene()
                .getStylesheets()
                .remove(FxGet.fxConfiguration().getUserCSSURL().toString());
        referenceBorderPane.getScene()
                .getStylesheets()
                .add(FxGet.fxConfiguration().getUserCSSURL().toString());
        System.out.println("Updated css: " + FxGet.fxConfiguration().getUserCSSURL().toString());

    }

    public final LogicalExpression getReferenceExpression() {
        return referenceExpressionProperty.get();
    }

    public final void setReferenceExpression(LogicalExpression value) {
        referenceExpressionProperty.set(value);
    }

    public ObjectProperty<LogicalExpression> referenceExpressionProperty() {
        return referenceExpressionProperty;
    }

    public final LogicalExpression getComparisonExpression() {
        return comparisonExpressionProperty.get();
    }

    public final void setComparisonExpression(LogicalExpression value) {
        comparisonExpressionProperty.set(value);
    }

    public ObjectProperty<LogicalExpression> comparisonExpressionProperty() {
        return comparisonExpressionProperty;
    }

    public final LogicalExpression getCorelationExpression() {
        return corelationExpressionProperty.get();
    }

    public final void setCorelationExpression(LogicalExpression value) {
        corelationExpressionProperty.set(value);
    }

    public ObjectProperty<LogicalExpression> corelationExpressionProperty() {
        return corelationExpressionProperty;
    }
    
    
}
