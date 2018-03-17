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
package sh.komet.gui.provider.concept.detail.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.model.logic.node.RootNode;
import sh.isaac.model.observable.ObservableSemanticChronologyImpl;
import sh.isaac.model.observable.version.ObservableLogicGraphVersionImpl;
import sh.komet.gui.control.concept.ConceptLabelToolbar;
import sh.komet.gui.control.concept.ManifoldLinkedConceptLabel;
import sh.komet.gui.interfaces.DetailNode;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.control.logic.LogicDetailRootNode;
import sh.komet.gui.style.StyleClasses;

/**
 *
 * @author kec
 */
public class LogicDetailNode
        implements DetailNode, Supplier<List<MenuItem>> {

    private final BorderPane conceptDetailPane = new BorderPane();
    private final SimpleStringProperty titleProperty = new SimpleStringProperty("empty");
    private final SimpleStringProperty toolTipProperty = new SimpleStringProperty("empty");
    private final Manifold conceptDetailManifold;
    private ManifoldLinkedConceptLabel titleLabel = null;
    private final ConceptLabelToolbar conceptLabelToolbar;
    private LogicalExpression editInFlight;

    //~--- constructors --------------------------------------------------------
    public LogicDetailNode(Manifold conceptDetailManifold, Consumer<Node> nodeConsumer) {
        this.conceptDetailManifold = conceptDetailManifold;
        this.conceptDetailManifold.getStampCoordinate().allowedStatesProperty().add(Status.INACTIVE);
        conceptDetailManifold.focusedConceptProperty()
                .addListener(this::setConceptListener);
        this.conceptLabelToolbar = ConceptLabelToolbar.make(conceptDetailManifold, this);
        conceptDetailPane.setTop(this.conceptLabelToolbar.getToolbarNode());
        conceptDetailPane.getStyleClass().add(StyleClasses.CONCEPT_DETAIL_PANE.toString());
        getLogicDetail();
        if (nodeConsumer != null) {
            nodeConsumer.accept(conceptDetailPane);
        }

    }

    private void setConceptListener(ObservableValue<? extends ConceptSpecification> observable,
            ConceptSpecification oldSpec,
            ConceptSpecification newSpec) {
        setConcept(newSpec);

    }

    private void setConcept(ConceptSpecification newSpec) {
        getLogicDetail();
        ConceptChronology newValue = Get.concept(newSpec);
        if (titleLabel == null) {
            if (newValue == null) {
                titleProperty.set("empty");
                toolTipProperty.set(
                        "concept details for: empty");
            } else {
                titleProperty.set(this.conceptDetailManifold.getPreferredDescriptionText(newValue));
                toolTipProperty.set(
                        "concept details for: "
                                + this.conceptDetailManifold.getFullySpecifiedDescriptionText(
                                        newValue));
            }
        }
    }
    
    private void cancelEdit(Event event) {
        setConcept(conceptDetailManifold.getFocusedConcept().get());
    }
    
    private void commitEdit(Event event) {
        
        LatestVersion<LogicGraphVersion> latestVersion = conceptDetailManifold.getStatedLogicGraphVersion(conceptDetailManifold.getFocusedConcept().get());
        if (latestVersion.isPresent()) {
            LogicGraphVersion version = latestVersion.get();
            ObservableSemanticChronologyImpl observableSemanticChronology = new ObservableSemanticChronologyImpl(version.getChronology());
            ObservableLogicGraphVersionImpl observableVersion = new ObservableLogicGraphVersionImpl(version, observableSemanticChronology);
            ObservableLogicGraphVersionImpl mutableVersion = observableVersion.makeAutonomousAnalog(conceptDetailManifold.getEditCoordinate());
            mutableVersion.setGraphData(editInFlight.getData(DataTarget.INTERNAL));
            Get.commitService().commit(mutableVersion, conceptDetailManifold.getEditCoordinate(), "Lambda graph edit");
        }
        setConcept(conceptDetailManifold.getFocusedConcept().get());
    }

    private Node getLogicDetail() {
        if (conceptDetailManifold.getFocusedConcept().isPresent()) {
            Optional<LogicalExpression> statedExpression = conceptDetailManifold.getStatedLogicalExpression(conceptDetailManifold.getFocusedConcept().get());
            getLogicDetail(statedExpression);
        } else {
            conceptDetailPane.setCenter(new Label("Empty"));
        }

        return conceptDetailPane;
    }

    private void getLogicDetail(Optional<LogicalExpression> statedExpression) {
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        conceptDetailPane.setCenter(splitPane);
        if (statedExpression.isPresent()) {
            LogicDetailRootNode rootDetail = new LogicDetailRootNode((RootNode) statedExpression.get().getRoot(), PremiseType.STATED, statedExpression.get(), conceptDetailManifold);
            if (statedExpression.get().isUncommitted()) {
                editInFlight = statedExpression.get();
                BorderPane expressionBorderPane = new BorderPane();
                expressionBorderPane.setCenter(rootDetail.getPanelNode());

                ToolBar commitToolbar = new ToolBar();
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                spacer.setMinWidth(Region.USE_PREF_SIZE);
                Button cancel = new Button("Cancel");
                cancel.setOnAction(this::cancelEdit);
                Button commit = new Button("Commit");
                commit.setOnAction(this::commitEdit);
                commitToolbar.getItems().addAll(spacer, cancel, commit);
                expressionBorderPane.setTop(commitToolbar);
                splitPane.getItems().add(expressionBorderPane);
            } else {
                editInFlight = null;
                splitPane.getItems().add(rootDetail.getPanelNode());
            }
        } else {
            conceptDetailPane.setCenter(new Label("No stated form"));
        }
        Optional<LogicalExpression> inferredExpression = conceptDetailManifold.getInferredLogicalExpression(conceptDetailManifold.getFocusedConcept().get());
        if (inferredExpression.isPresent()) {
            LogicDetailRootNode rootDetail = new LogicDetailRootNode((RootNode) inferredExpression.get().getRoot(), PremiseType.INFERRED, inferredExpression.get(), conceptDetailManifold);
            splitPane.getItems().add(rootDetail.getPanelNode());
        } else {
            conceptDetailPane.setCenter(new Label("No inferred form"));
        }
    }

    /*
 Root[0]➞[14]
    Necessary[14]➞[13]
        And[13]➞[6, 12, 1, 2]
            Some[6] Role group (SOLOR) <-2147483595>➞[5]
                And[5]➞[4]
                    Some[4] Has focus (attribute) <-2147325119>➞[3]
                        Concept[3] Tetralogy of Fallot (disorder) <-2146599147>
            Some[12] Role group (SOLOR) <-2147483595>➞[11]
                And[11]➞[10, 8]
                    Some[10] Method (attribute) <-2146935285>➞[9]
                        Concept[9] Repair - action (qualifier value) <-2146967053>
                    Some[8] Procedure site - Direct (attribute) <-2146930267>➞[7]
                        Concept[7] Structure of cardiovascular system (body structure) <-2147414555>
            Concept[1] Cardiovascular system repair (procedure) <-2147338537>
            Concept[2] Surgical procedure for clinical finding and/or disorder (procedure) <-2146874176>
   
     */
 /*
Root[0]➞[41]
    Necessary[41]➞[40]
        And[40]➞[13, 19, 25, 31, 39, 1, 5, 3, 2, 4]
            Some[13] Role group (SOLOR) <-2147483595>➞[12]
                And[12]➞[9, 7, 11]
                    Some[9] Associated morphology (attribute) <-2147370432>➞[8]
                        Concept[8] Developmental failure of fusion (morphologic abnormality) <-2147225333>
                    Some[7] Finding site (attribute) <-2147325161>➞[6]
                        Concept[6] Interventricular septum structure (body structure) <-2147478136>
                    Some[11] Occurrence (attribute) <-2147124707>➞[10]
                        Concept[10] Congenital (qualifier value) <-2147000906>
            Some[19] Role group (SOLOR) <-2147483595>➞[18]
                And[18]➞[17, 15]
                    Some[17] Associated morphology (attribute) <-2147370432>➞[16]
                        Concept[16] Stenosis (morphologic abnormality) <-2146827479>
                    Some[15] Finding site (attribute) <-2147325161>➞[14]
                        Concept[14] Pulmonary valve structure (body structure) <-2147007255>
            Some[25] Role group (SOLOR) <-2147483595>➞[24]
                And[24]➞[21, 23]
                    Some[21] Associated morphology (attribute) <-2147370432>➞[20]
                        Concept[20] Overriding structures (morphologic abnormality) <-2147113237>
                    Some[23] Finding site (attribute) <-2147325161>➞[22]
                        Concept[22] Thoracic aorta structure (body structure) <-2147414518>
            Some[31] Role group (SOLOR) <-2147483595>➞[30]
                And[30]➞[27, 29]
                    Some[27] Associated morphology (attribute) <-2147370432>➞[26]
                        Concept[26] Hypertrophy (morphologic abnormality) <-2146824593>
                    Some[29] Finding site (attribute) <-2147325161>➞[28]
                        Concept[28] Entire right ventricle (body structure) <-2147152891>
            Some[39] Role group (SOLOR) <-2147483595>➞[38]
                And[38]➞[35, 37, 33]
                    Some[35] Associated morphology (attribute) <-2147370432>➞[34]
                        Concept[34] Developmental anomaly (morphologic abnormality) <-2147235637>
                    Some[37] Finding site (attribute) <-2147325161>➞[36]
                        Concept[36] Aortic structure (body structure) <-2147305922>
                    Some[33] Occurrence (attribute) <-2147124707>➞[32]
                        Concept[32] Congenital (qualifier value) <-2147000906>
            Concept[1] Ventricular septal defect (disorder) <-2147119288>
            Concept[5] Congenital abnormality of ventricles and ventricular septum (disorder) <-2147027176>
            Concept[3] Pulmonic valve stenosis (disorder) <-2146820412>
            Concept[2] Overriding aorta (disorder) <-2146765219>
            Concept[4] Right ventricular hypertrophy (disorder) <-2146572995>   
     */
    //~--- get methods ---------------------------------------------------------
    @Override
    public ReadOnlyProperty<String> getTitle() {
        return this.titleProperty;
    }

    @Override
    public Optional<Node> getTitleNode() {
        // MaterialDesignIcon.LAMBDA
        if (titleLabel == null) {
            this.titleLabel = new ManifoldLinkedConceptLabel(conceptDetailManifold, ManifoldLinkedConceptLabel::setPreferredText, this);
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
        return this.conceptDetailManifold;
    }

    @Override
    public boolean selectInTabOnChange() {
        return false;
    }

}
