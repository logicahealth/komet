/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */
package sh.komet.gui.cell.treetable;

//~--- JDK imports ------------------------------------------------------------
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import javafx.application.Platform;
import javafx.beans.property.Property;

//~--- non-JDK imports --------------------------------------------------------
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeTableRow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.PropertySheet;

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.commit.CommitTask;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.component.semantic.version.LongVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.observable.ObservableCategorizedVersion;

import sh.komet.gui.control.FixedSizePane;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.style.StyleClasses;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.brittle.Rf2Relationship;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.component.semantic.version.brittle.Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version;
import sh.isaac.api.component.semantic.version.brittle.LoincVersion;
import sh.isaac.api.component.semantic.version.brittle.Nid1_Int2_Str3_Str4_Nid5_Nid6_Version;
import sh.isaac.api.component.semantic.version.brittle.Nid1_Int2_Version;
import sh.isaac.api.component.semantic.version.brittle.Nid1_Nid2_Int3_Version;
import sh.isaac.api.component.semantic.version.brittle.Nid1_Nid2_Str3_Version;
import sh.isaac.api.component.semantic.version.brittle.Nid1_Nid2_Version;
import sh.isaac.api.component.semantic.version.brittle.Nid1_Str2_Version;
import sh.isaac.api.component.semantic.version.brittle.Str1_Nid2_Nid3_Nid4_Version;
import sh.isaac.api.component.semantic.version.brittle.Str1_Str2_Nid3_Nid4_Nid5_Version;
import sh.isaac.api.component.semantic.version.brittle.Str1_Str2_Nid3_Nid4_Version;
import sh.isaac.api.component.semantic.version.brittle.Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version;
import sh.isaac.api.component.semantic.version.brittle.Str1_Str2_Version;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.contract.GuiSearcher;
import sh.komet.gui.control.property.PropertyEditorFactory;
import sh.komet.gui.control.PropertyToPropertySheetItem;
import sh.komet.gui.control.axiom.AxiomView;
import sh.komet.gui.util.FxGet;

//~--- classes ----------------------------------------------------------------
/**
 *
 * @author kec
 */
public class TreeTableGeneralCell
        extends KometTreeTableCell<ObservableCategorizedVersion> {

    private static final Logger LOG = LogManager.getLogger();

    //~--- fields --------------------------------------------------------------
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final Manifold manifold;
    private final Button editButton = new Button("", Iconography.EDIT_PENCIL.getIconographic());
    private final GridPane textAndEditGrid = new GridPane();
    private final BorderPane editPanel = new BorderPane();
    private SemanticVersion semanticVersion;
    private final FixedSizePane paneForText = new FixedSizePane();
    private final ToolBar toolBar;
    private ObservableVersion mutableVersion;

    //~--- constructors --------------------------------------------------------
    public TreeTableGeneralCell(Manifold manifold) {
        this.manifold = manifold;
        getStyleClass().add("komet-version-general-cell");
        getStyleClass().add("isaac-version");
        editButton.getStyleClass()
                .setAll(StyleClasses.EDIT_COMPONENT_BUTTON.toString());
        editButton.setOnAction(this::toggleEdit);
        textAndEditGrid.getChildren().addAll(paneForText, editButton, editPanel);
        setContextMenu(makeContextMenu());
        // setConstraints(Node child, int columnIndex, int rowIndex, int columnspan, int rowspan, HPos halignment, VPos valignment, Priority hgrow, Priority vgrow)
        GridPane.setConstraints(paneForText, 0, 0, 1, 2, HPos.LEFT, VPos.TOP, Priority.ALWAYS, Priority.NEVER);
        GridPane.setConstraints(editButton, 2, 0, 1, 1, HPos.RIGHT, VPos.TOP, Priority.NEVER, Priority.NEVER);
        GridPane.setConstraints(editPanel, 0, 2, 3, 1, HPos.LEFT, VPos.TOP, Priority.ALWAYS, Priority.ALWAYS);
        final Pane leftSpacer = new Pane();
        HBox.setHgrow(
                leftSpacer,
                Priority.SOMETIMES
        );
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(this::toggleEdit);
        Button commitButton = new Button("Commit");
        commitButton.setOnAction(this::commitEdit);
        toolBar = new ToolBar(
                leftSpacer,
                cancelButton,
                new Separator(),
                commitButton
        );

    }

    //~--- methods -------------------------------------------------------------
    public final ContextMenu makeContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem item1 = new MenuItem("Search for contents");
        item1.setOnAction((ActionEvent e) -> {
            this.search();
        });
        contextMenu.getItems().addAll(item1);
        return contextMenu;
    }

    private void search() {
        if (semanticVersion.getSemanticType() == VersionType.STRING) {
            StringVersion stringVersion = (StringVersion) semanticVersion;
            String searchString = stringVersion.getString();
            String[] searchParts = searchString.split("\\s+");
            List<String> searchPartsList = new ArrayList<>();
            for (String part: searchParts) {
                if (part.length() > 2) {
                    searchPartsList.add(part);
                }
            }
            StringBuilder searchBuilder = new StringBuilder();
            for (String part: searchPartsList) {
                searchBuilder.append("+").append(part).append(" ");
            }
            
            for (GuiSearcher searcher: FxGet.searchers()) {
                searcher.executeSearch(searchBuilder.toString());
            }
        }
        
    }
    public void addDefToCell(LogicGraphVersion logicGraphVersion) {
        LogicalExpression expression = logicGraphVersion.getLogicalExpression();
        PremiseType premiseType = PremiseType.STATED;
        if (manifold.getLogicCoordinate()
                .getInferredAssemblageNid() == logicGraphVersion.getAssemblageNid()) {
            premiseType = PremiseType.INFERRED;
        } else if (manifold.getLogicCoordinate()
                .getStatedAssemblageNid() == logicGraphVersion.getAssemblageNid()) {
            premiseType = PremiseType.STATED;
        }
        addDefToCell(expression, premiseType);
    }

    private void addDefToCell(LogicalExpression expression, PremiseType premiseType) {

        BorderPane defNodePanel = AxiomView.createWithCommitPanel(expression, premiseType, manifold);
        defNodePanel.setMaxWidth(this.getWidth());
        this.widthProperty()
                .addListener(
                        new WeakChangeListener<>(
                                (ObservableValue<? extends Number> observable,
                                        Number oldValue,
                                        Number newValue) -> {
                                    double newTextFlowWidth = newValue.doubleValue() - 32;
                                    double newTextFlowHeight = defNodePanel.prefHeight(newTextFlowWidth);

                                    defNodePanel.setPrefWidth(newTextFlowWidth);
                                    defNodePanel.setMaxWidth(newTextFlowWidth);
                                    defNodePanel.setPrefHeight(newTextFlowHeight);
                                    defNodePanel.setMaxHeight(newTextFlowHeight);

                                    double newFixedSizeWidth = newTextFlowWidth + 28;
                                    double newFixedSizeHeight = newTextFlowHeight + 28;

                                    paneForText.setWidth(newFixedSizeWidth);
                                    paneForText.setHeight(newFixedSizeHeight);
                                }));
        paneForText.getChildren().clear();
        paneForText.getChildren().add(defNodePanel);
        this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        this.setGraphic(textAndEditGrid);
    }

    public void addTextToCell(Text... text) {
        TextFlow textFlow = new TextFlow(text);

        textFlow.setPrefWidth(this.getWidth() - (textFlow.getInsets().getLeft() + textFlow.getInsets().getRight()));
        textFlow.setMaxWidth(this.getWidth());
        textFlow.setLayoutX(1);
        textFlow.setLayoutY(1);

        this.widthProperty()
                .addListener(
                        new WeakChangeListener<>(
                                (ObservableValue<? extends Number> observable,
                                        Number oldValue,
                                        Number newValue) -> {
                                    double newTextFlowWidth = newValue.doubleValue() - 32;
                                    double newTextFlowHeight = textFlow.prefHeight(newTextFlowWidth);

                                    textFlow.setPrefWidth(newTextFlowWidth);
                                    textFlow.setMaxWidth(newTextFlowWidth);
                                    textFlow.setPrefHeight(newTextFlowHeight);
                                    textFlow.setMaxHeight(newTextFlowHeight);

                                    double newFixedSizeWidth = newTextFlowWidth + 28;
                                    double newFixedSizeHeight = newTextFlowHeight + 28;

                                    paneForText.setWidth(newFixedSizeWidth);
                                    paneForText.setHeight(newFixedSizeHeight);
                                }));
        paneForText.getChildren().clear();
        paneForText.getChildren().add(textFlow);
        this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        this.setGraphic(textAndEditGrid);
    }

    private void commitEdit(ActionEvent event) {
        CommitTask commitTask = Get.commitService().commit(
                this.manifold.getEditCoordinate(),
                "No comment",
                this.mutableVersion);
        Get.executor().execute(() -> {
            try {
                Optional<CommitRecord> commitRecord = commitTask.get();
                if (commitRecord.isPresent()) {
                    Platform.runLater(() -> {
                        editPanel.getChildren().clear();
                        editButton.setVisible(true);
                    });
                } else {
                    // TODO show errors. 
                    commitTask.getAlerts();
                }
            } catch (InterruptedException | ExecutionException ex) {
                LOG.error("Error committing change.", ex);
            } finally {
            }
        });
    }

    private void toggleEdit(ActionEvent event) {

        if (editPanel.getChildren().isEmpty()) {
            if (this.semanticVersion != null) {
                if (this.semanticVersion instanceof ObservableVersion) {
                    ObservableVersion currentVersion = (ObservableVersion) this.semanticVersion;
                    mutableVersion = currentVersion.makeAutonomousAnalog(this.manifold.getEditCoordinate());

                    List<Property<?>> propertiesToEdit = mutableVersion.getEditableProperties();
                    PropertySheet propertySheet = new PropertySheet();
                    propertySheet.setMode(PropertySheet.Mode.NAME);
                    propertySheet.setSearchBoxVisible(false);
                    propertySheet.setModeSwitcherVisible(false);
                    propertySheet.setPropertyEditorFactory(new PropertyEditorFactory(this.manifold));
                    propertySheet.getItems().addAll(PropertyToPropertySheetItem.getItems(propertiesToEdit, this.manifold));

                    editPanel.setTop(toolBar);
                    editPanel.setCenter(propertySheet);
                    editButton.setVisible(false);
                }
            }
        } else {
            editPanel.getChildren().clear();
            editButton.setVisible(true);
        }
    }

    @Override
    protected void updateItem(TreeTableRow<ObservableCategorizedVersion> row, ObservableCategorizedVersion version) {
        setWrapText(true);

        this.semanticVersion = version.unwrap();
        VersionType semanticType = semanticVersion.getChronology()
                .getVersionType();

        this.setGraphic(null);
        this.setContentDisplay(ContentDisplay.TEXT_ONLY);

        Text assemblageNameText = new Text(
                manifold.getPreferredDescriptionText(semanticVersion.getAssemblageNid()) + "\n");

        assemblageNameText.getStyleClass()
                .add(StyleClasses.ASSEMBLAGE_NAME_TEXT.toString());

        String referencedComponentString = manifold.getPreferredDescriptionText(semanticVersion.getReferencedComponentNid());
        if (referencedComponentString == null || referencedComponentString.isEmpty()) {
            LOG.warn("No referenced component text for: " + semanticVersion.getReferencedComponentNid());
        }
        Text referencedComponentText = new Text("\n" + referencedComponentString);
        Text referencedComponentTextNoNewLine = new Text(
                manifold.getPreferredDescriptionText(semanticVersion.getReferencedComponentNid()));

        switch (Get.identifierService().getObjectTypeForComponent(semanticVersion.getReferencedComponentNid())) {
            case CONCEPT:
                referencedComponentText.getStyleClass()
                        .add(StyleClasses.CONCEPT_COMPONENT_REFERENCE.toString());
                referencedComponentTextNoNewLine.getStyleClass()
                        .add(StyleClasses.CONCEPT_COMPONENT_REFERENCE.toString());
                break;

            case SEMANTIC:
                referencedComponentText.getStyleClass()
                        .add(StyleClasses.SEMANTIC_COMPONENT_REFERENCE.toString());
                referencedComponentTextNoNewLine.getStyleClass()
                        .add(StyleClasses.SEMANTIC_COMPONENT_REFERENCE.toString());
                break;

            case UNKNOWN:
            default:
                referencedComponentText.getStyleClass()
                        .add(StyleClasses.ERROR_TEXT.toString());
                referencedComponentTextNoNewLine.getStyleClass()
                        .add(StyleClasses.ERROR_TEXT.toString());
        }

        switch (semanticType) {
            case DESCRIPTION:
                DescriptionVersion description = version.unwrap();

                this.setText(null);

                Text text = new Text(description.getText());

                text.wrappingWidthProperty()
                        .bind(getTableColumn().widthProperty()
                                .subtract(5));
                this.setGraphic(text);
                this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                text.getStyleClass()
                        .addAll(this.getStyleClass());
                break;

            case COMPONENT_NID:
                ComponentNidVersion componentNidVersion = version.unwrap();

                switch (Get.identifierService()
                        .getObjectTypeForComponent(componentNidVersion.getComponentNid())) {
                    case CONCEPT:
                        Text conceptText = new Text(manifold.getPreferredDescriptionText(componentNidVersion.getComponentNid()));

                        conceptText.getStyleClass()
                                .add(StyleClasses.CONCEPT_TEXT.toString());
                        addTextToCell(assemblageNameText, conceptText, referencedComponentText);
                        break;

                    case SEMANTIC:
                        SemanticChronology semantic = Get.assemblageService()
                                .getSemanticChronology(componentNidVersion.getComponentNid());
                        LatestVersion<SemanticVersion> latest = semantic.getLatestVersion(manifold);

                        if (latest.isPresent()) {
                            Text semanticText = new Text(latest.get().toUserString());

                            semanticText.getStyleClass()
                                    .add(StyleClasses.SEMANTIC_TEXT.toString());
                            addTextToCell(assemblageNameText, semanticText, referencedComponentText);
                        } else {
                            Text semanticText = new Text("No latest version for component");

                            semanticText.getStyleClass()
                                    .add(StyleClasses.SEMANTIC_TEXT.toString());
                            addTextToCell(assemblageNameText, semanticText, referencedComponentText);
                        }

                        break;

                    case UNKNOWN:
                        LOG.warn("Unknown nid: " + componentNidVersion);

                        Text unknownText = new Text("Unknown nid: " + componentNidVersion);

                        unknownText.getStyleClass()
                                .add(StyleClasses.ERROR_TEXT.toString());
                        addTextToCell(assemblageNameText, unknownText, referencedComponentText);
                        break;
                }

                break;

            case STRING:
                StringVersion stringVersion = version.unwrap();
                Text stringText = new Text(stringVersion.getString());

                stringText.getStyleClass()
                        .add(StyleClasses.SEMANTIC_TEXT.toString());
                addTextToCell(assemblageNameText, stringText, referencedComponentText);
                break;

            case LOGIC_GRAPH:
                LogicGraphVersion logicGraphVersion = version.unwrap();
                addDefToCell(logicGraphVersion);
                break;

            case MEMBER:
                addTextToCell(assemblageNameText, referencedComponentTextNoNewLine);
                break;

            case LONG:
                LongVersion longVersion = version.unwrap();

                // TODO, rely on semantic info from assemblage in the future to
                // eliminate this date hack...
                ZonedDateTime zonedDateTime = Instant.ofEpochMilli(longVersion.getLongValue())
                        .atZone(ZoneOffset.UTC);

                if ((zonedDateTime.getYear() > 1900) && (zonedDateTime.getYear() < 2232)) {
                    Text dateText = new Text(formatter.format(zonedDateTime));

                    dateText.getStyleClass()
                            .add(StyleClasses.DATE_TEXT.toString());
                    addTextToCell(assemblageNameText, dateText, referencedComponentText);
                } else {
                    Text longText = new Text(Long.toString(longVersion.getLongValue()));

                    longText.getStyleClass()
                            .add(StyleClasses.DATE_TEXT.toString());
                    addTextToCell(assemblageNameText, longText, referencedComponentText);
                }

                break;

            case RF2_RELATIONSHIP: {
                Rf2Relationship rf2Relationship = version.unwrap();
                StringBuilder buff = new StringBuilder();
                if (!(rf2Relationship.getTypeNid() == TermAux.IS_A.getNid())) {
                    buff.append(this.manifold.getPreferredDescriptionText(rf2Relationship.getModifierNid()));
                    buff.append(" ");
                }
                buff.append(this.manifold.getPreferredDescriptionText(rf2Relationship.getTypeNid()));
                buff.append(" ");
                buff.append(this.manifold.getPreferredDescriptionText(rf2Relationship.getDestinationNid()));
                buff.append(" (");
                buff.append(this.manifold.getPreferredDescriptionText(rf2Relationship.getCharacteristicNid()));
                buff.append(") ");

                Text defaultText = new Text(buff.toString());
                addTextToCell(assemblageNameText, defaultText, referencedComponentText);
            }
            break;

            case Int1_Int2_Str3_Str4_Str5_Nid6_Nid7: {
                Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version brittleVersion = version.unwrap();
                StringBuilder buff = new StringBuilder();
                buff.append(brittleVersion.getInt1());
                buff.append("\n");
                buff.append(brittleVersion.getInt2());
                buff.append("\n");
                buff.append(brittleVersion.getStr3());
                buff.append("\n");
                buff.append(brittleVersion.getStr4());
                buff.append("\n");
                buff.append(brittleVersion.getStr5());
                buff.append("\n");
                buff.append(manifold.getPreferredDescriptionText(brittleVersion.getNid6()));
                buff.append("\n");
                buff.append(manifold.getPreferredDescriptionText(brittleVersion.getNid7()));
                buff.append(" ");

                Text defaultText = new Text(buff.toString());
                addTextToCell(assemblageNameText, defaultText, referencedComponentText);
            }
            break;
            case LOINC_RECORD: {
                LoincVersion brittleVersion = version.unwrap();
                StringBuilder buff = new StringBuilder();
                buff.append(brittleVersion.getLoincNum());
                buff.append(" ");
                buff.append(brittleVersion.getShortName());
                buff.append(" - ");
                buff.append(brittleVersion.getLoincStatus());
                buff.append("\n");
                buff.append(brittleVersion.getLongCommonName());
                buff.append("\nc:");
                buff.append(brittleVersion.getComponent());
                buff.append(" m: ");
                buff.append(brittleVersion.getMethodType());
                buff.append("\np: ");
                buff.append(brittleVersion.getProperty());
                buff.append(" - ");
                buff.append(brittleVersion.getScaleType());
                buff.append(" s: ");
                buff.append(brittleVersion.getSystem());
                buff.append(" t: ");
                buff.append(brittleVersion.getTimeAspect());

                Text defaultText = new Text(buff.toString());
                addTextToCell(assemblageNameText, defaultText, referencedComponentText);
            }
            break;
            case Nid1_Int2: {
                Nid1_Int2_Version brittleVersion = version.unwrap();

                StringBuilder buff = new StringBuilder();
                buff.append(manifold.getPreferredDescriptionText(brittleVersion.getNid1()));
                buff.append("\n");
                buff.append(brittleVersion.getInt2());
                buff.append(" ");

                Text defaultText = new Text(buff.toString());
                addTextToCell(assemblageNameText, defaultText, referencedComponentText);
            }
            break;
            case Nid1_Int2_Str3_Str4_Nid5_Nid6: {
                Nid1_Int2_Str3_Str4_Nid5_Nid6_Version brittleVersion = version.unwrap();

                StringBuilder buff = new StringBuilder();
                buff.append(manifold.getPreferredDescriptionText(brittleVersion.getNid1()));
                buff.append("\n");
                buff.append(brittleVersion.getInt2());
                buff.append("\n");
                buff.append(brittleVersion.getStr3());
                buff.append("\n");
                buff.append(brittleVersion.getStr4());
                buff.append("\n");
                buff.append(manifold.getPreferredDescriptionText(brittleVersion.getNid5()));
                buff.append("\n");
                buff.append(manifold.getPreferredDescriptionText(brittleVersion.getNid6()));
                buff.append(" ");

                Text defaultText = new Text(buff.toString());
                addTextToCell(assemblageNameText, defaultText, referencedComponentText);
            }
            break;
            case Nid1_Nid2: {
                Nid1_Nid2_Version brittleVersion = version.unwrap();

                StringBuilder buff = new StringBuilder();
                buff.append(manifold.getPreferredDescriptionText(brittleVersion.getNid1()));
                buff.append("\n");
                buff.append(manifold.getPreferredDescriptionText(brittleVersion.getNid2()));
                buff.append(" ");

                Text defaultText = new Text(buff.toString());
                addTextToCell(assemblageNameText, defaultText, referencedComponentText);
            }
            break;
            case Nid1_Nid2_Int3: {
                Nid1_Nid2_Int3_Version brittleVersion = version.unwrap();

                StringBuilder buff = new StringBuilder();
                buff.append(manifold.getPreferredDescriptionText(brittleVersion.getNid1()));
                buff.append("\n");
                buff.append(manifold.getPreferredDescriptionText(brittleVersion.getNid2()));
                buff.append("\n");
                buff.append(brittleVersion.getInt3());
                buff.append(" ");

                Text defaultText = new Text(buff.toString());
                addTextToCell(assemblageNameText, defaultText, referencedComponentText);
            }
            break;
            case Nid1_Nid2_Str3: {
                Nid1_Nid2_Str3_Version brittleVersion = version.unwrap();

                StringBuilder buff = new StringBuilder();
                buff.append(manifold.getPreferredDescriptionText(brittleVersion.getNid1()));
                buff.append("\n");
                buff.append(manifold.getPreferredDescriptionText(brittleVersion.getNid2()));
                buff.append("\n");
                buff.append(brittleVersion.getStr3());
                buff.append(" ");

                Text defaultText = new Text(buff.toString());
                addTextToCell(assemblageNameText, defaultText, referencedComponentText);
            }
            break;
            case Nid1_Str2: {
                Nid1_Str2_Version brittleVersion = version.unwrap();

                StringBuilder buff = new StringBuilder();
                buff.append(manifold.getPreferredDescriptionText(brittleVersion.getNid1()));
                buff.append("\n");
                buff.append(brittleVersion.getStr2());
                buff.append(" ");

                Text defaultText = new Text(buff.toString());
                addTextToCell(assemblageNameText, defaultText, referencedComponentText);
            }
            break;
            case Str1_Str2: {
                Str1_Str2_Version brittleVersion = version.unwrap();

                StringBuilder buff = new StringBuilder();
                buff.append(brittleVersion.getStr1());
                buff.append("\n");
                buff.append(brittleVersion.getStr2());
                buff.append(" ");

                Text defaultText = new Text(buff.toString());
                addTextToCell(assemblageNameText, defaultText, referencedComponentText);
            }
            break;
            case Str1_Str2_Nid3_Nid4: {
                Str1_Str2_Nid3_Nid4_Version brittleVersion = version.unwrap();

                StringBuilder buff = new StringBuilder();
                buff.append(brittleVersion.getStr1());
                buff.append("\n");
                buff.append(brittleVersion.getStr2());
                buff.append("\n");
                buff.append(manifold.getPreferredDescriptionText(brittleVersion.getNid3()));
                buff.append("\n");
                buff.append(manifold.getPreferredDescriptionText(brittleVersion.getNid4()));
                buff.append(" ");

                Text defaultText = new Text(buff.toString());
                addTextToCell(assemblageNameText, defaultText, referencedComponentText);
            }
            break;
            case Str1_Str2_Str3_Str4_Str5_Str6_Str7: {
                Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version brittleVersion = version.unwrap();

                StringBuilder buff = new StringBuilder();
                buff.append(brittleVersion.getStr1());
                buff.append("\n");
                buff.append(brittleVersion.getStr2());
                buff.append("\n");
                buff.append(brittleVersion.getStr3());
                buff.append("\n");
                buff.append(brittleVersion.getStr4());
                buff.append("\n");
                buff.append(brittleVersion.getStr5());
                buff.append("\n");
                buff.append(brittleVersion.getStr6());
                buff.append("\n");
                buff.append(brittleVersion.getStr7());
                buff.append(" ");

                Text defaultText = new Text(buff.toString());
                addTextToCell(assemblageNameText, defaultText, referencedComponentText);
            }
            break;

            case Str1_Nid2_Nid3_Nid4: {
                Str1_Nid2_Nid3_Nid4_Version brittleVersion = version.unwrap();

                StringBuilder buff = new StringBuilder();
                buff.append(brittleVersion.getStr1());
                buff.append("\n");
                buff.append(manifold.getPreferredDescriptionText(brittleVersion.getNid2()));
                buff.append("\n");
                buff.append(manifold.getPreferredDescriptionText(brittleVersion.getNid3()));
                buff.append("\n");
                buff.append(manifold.getPreferredDescriptionText(brittleVersion.getNid4()));
                buff.append(" ");

                Text defaultText = new Text(buff.toString());
                addTextToCell(assemblageNameText, defaultText, referencedComponentText);
            }
            break;
            case Str1_Str2_Nid3_Nid4_Nid5: {
                Str1_Str2_Nid3_Nid4_Nid5_Version brittleVersion = version.unwrap();

                StringBuilder buff = new StringBuilder();
                buff.append(brittleVersion.getStr1());
                buff.append("\n");
                buff.append(brittleVersion.getStr2());
                buff.append("\n");
                buff.append(manifold.getPreferredDescriptionText(brittleVersion.getNid3()));
                buff.append("\n");
                buff.append(manifold.getPreferredDescriptionText(brittleVersion.getNid4()));
                buff.append("\n");
                buff.append(manifold.getPreferredDescriptionText(brittleVersion.getNid5()));
                buff.append(" ");

                Text defaultText = new Text(buff.toString());
                addTextToCell(assemblageNameText, defaultText, referencedComponentText);
            }
            break;
            default:
                Text defaultText = new Text("not implemented for type: " + semanticType);

                defaultText.getStyleClass()
                        .add(StyleClasses.ERROR_TEXT.toString());
                addTextToCell(assemblageNameText, defaultText, referencedComponentText);
        }
    }
}
