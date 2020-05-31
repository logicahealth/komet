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

import sh.isaac.komet.iconography.IconographyHelper;
import sh.isaac.model.logic.IsomorphicResultsFromPathHash;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.BorderPane;
import sh.isaac.api.Get;
import sh.isaac.api.logic.IsomorphicResults;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.model.logic.IsomorphicResultsBottomUp;
import sh.isaac.model.observable.ObservableFields;
import sh.komet.gui.control.property.ViewProperties;
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

    @FXML // fx:id="comparisonChoiceBox"
    private ChoiceBox<String> comparisonChoiceBox; // Value injected by FXMLLoader

    @FXML // fx:id="algorithmChoiceBox"
    private ChoiceBox<String> algorithmChoiceBox; // Value injected by FXMLLoader

    private final String[] comparisons = {"Problem 1", "Problem 2", "Problem 3", "Problem 4", "Problem 5", "Problem 6"};
    private final String[] algorithms = {"Bottom up", "Lineage Hash"};

    private ViewProperties viewProperties;

    private ExpressionView referenceExpressionView;
    private ExpressionView correlationExpressionView;
    private ExpressionView comparisonExpressionView;

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert referenceBorderPane != null : "fx:id=\"referenceBorderPane\" was not injected: check your FXML file 'ConceptComparison.fxml'.";
        assert correlationBorderPane != null : "fx:id=\"correlationBorderPane\" was not injected: check your FXML file 'ConceptComparison.fxml'.";
        assert comparisonBorderPane != null : "fx:id=\"comparisonBorderPane\" was not injected: check your FXML file 'ConceptComparison.fxml'.";
        assert comparisonChoiceBox != null : "fx:id=\"comparisonChoiceBox\" was not injected: check your FXML file 'ConceptComparison.fxml'.";
        assert algorithmChoiceBox != null : "fx:id=\"algorithmChoiceBox\" was not injected: check your FXML file 'ConceptComparison.fxml'.";

        referenceBorderPane.sceneProperty().addListener((observable, oldValue, newValue) -> {
            handleRefreshUserCss(null);
            this.referenceExpressionView.setExpression(CorrelationProblem5.getReferenceExpression());
            this.comparisonExpressionView.setExpression(CorrelationProblem5.getComparisonExpression());
        });

        comparisonChoiceBox.setItems(FXCollections.observableArrayList(comparisons));
        comparisonChoiceBox.getSelectionModel().select("Problem 5");
        comparisonChoiceBox.setOnAction(this::handleComparisonAction);
        algorithmChoiceBox.setItems(FXCollections.observableArrayList(algorithms));
        algorithmChoiceBox.setOnAction(this::handleAlgorithmAction);
        algorithmChoiceBox.getSelectionModel().select("Bottom up");
    }

    public void setViewProperties(ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
        this.referenceExpressionView = new ExpressionView(viewProperties, ViewProperties.CORRELATION);
        this.referenceBorderPane.setCenter(this.referenceExpressionView.getNode());

        this.correlationExpressionView = new ExpressionView(viewProperties, ViewProperties.CORRELATION);
        this.correlationBorderPane.setCenter(this.correlationExpressionView.getNode());

        this.comparisonExpressionView = new ExpressionView(viewProperties, ViewProperties.CORRELATION);
        this.comparisonBorderPane.setCenter(this.comparisonExpressionView.getNode());

    }

    public void handleComparisonAction(ActionEvent event) {
        switch (comparisonChoiceBox.getSelectionModel().getSelectedItem()) {
            case "Problem 1":
                updateComparison(CorrelationProblem1.getReferenceExpression(), CorrelationProblem1.getComparisonExpression());
                break;
            case "Problem 2":
                updateComparison(CorrelationProblem2.getReferenceExpression(), CorrelationProblem2.getComparisonExpression());
                break;
            case "Problem 3":
                updateComparison(CorrelationProblem3.getReferenceExpression(), CorrelationProblem3.getComparisonExpression());
                break;
            case "Problem 4":
                updateComparison(CorrelationProblem4.getReferenceExpression(), CorrelationProblem4.getComparisonExpression());
                break;
            case "Problem 5":
                updateComparison(CorrelationProblem5.getReferenceExpression(), CorrelationProblem5.getComparisonExpression());
                break;
           case "Problem 6":
                updateComparison(CorrelationProblem6.getReferenceExpression(), CorrelationProblem6.getComparisonExpression());
                break;
        }
    }

    protected void updateComparison(LogicalExpression referenceExpression, LogicalExpression comparisonExpression) {
        try {
            this.referenceExpressionView.setExpression(referenceExpression);
            this.comparisonExpressionView.setExpression(comparisonExpression);
            long startTime = System.currentTimeMillis();
            IsomorphicResultsBottomUp bottomUpResults = new IsomorphicResultsBottomUp(referenceExpression, comparisonExpression);
            IsomorphicResults bottomUpSolution = Get.executor().submit(bottomUpResults).get();
            
            long duration1 = System.currentTimeMillis() - startTime;
            System.out.println("\n\nBottom up results: ");
            System.out.println(bottomUpResults.toString());
            
            System.out.println(bottomUpResults.getIsomorphicSolution());
            startTime = System.currentTimeMillis();
            
            IsomorphicResultsFromPathHash pathHashResults = new IsomorphicResultsFromPathHash(referenceExpression, comparisonExpression);
            IsomorphicResults pathHashSolution = Get.executor().submit(pathHashResults).get();
            
            long duration2 = System.currentTimeMillis() - startTime;
            System.out.println("\n\nPath hash results: ");
            System.out.println(pathHashResults.toString());
            System.out.println("\n\n");
            System.out.println("Solutions equal is " + Arrays.equals(bottomUpResults.getIsomorphicSolution().getSolution(), 
                    pathHashResults.getIsomorphicSolution().getSolution()));
            System.out.println("duration1: " + duration1);
            System.out.println("duration2: " + duration2);
            
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(ConceptCorrelationController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void handleAlgorithmAction(ActionEvent event) {
        switch (comparisonChoiceBox.getSelectionModel().getSelectedItem()) {
            case "Bottom up":
                break;
            case "Lineage Hash":
                break;
        }

    }

    @FXML
    public void handleRefreshUserCss(ActionEvent event) {
        // "Feature" to make css editing/testing easy in the dev environment. 
        referenceBorderPane.getScene()
                .getStylesheets()
                .remove(FxGet.fxConfiguration().getUserCSSURL().toString());
        referenceBorderPane.getScene()
                .getStylesheets()
                .add(FxGet.fxConfiguration().getUserCSSURL().toString());
        referenceBorderPane.getScene().getStylesheets()
                .remove(IconographyHelper.getStyleSheetStringUrl());
        referenceBorderPane.getScene().getStylesheets()
                .add(IconographyHelper.getStyleSheetStringUrl());

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
