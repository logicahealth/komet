package sh.komet.gui.cell;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.*;
import sh.isaac.api.component.semantic.version.brittle.*;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.observable.semantic.version.ObservableSemanticVersion;
import sh.komet.gui.contract.GuiConceptBuilder;
import sh.komet.gui.contract.GuiSearcher;
import sh.komet.gui.control.axiom.AxiomView;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.menu.MenuItemWithText;
import sh.komet.gui.style.StyleClasses;
import sh.komet.gui.util.FxGet;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CellHelper {
    private static final Logger LOG = LogManager.getLogger();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CellFunctions cell;

    public CellHelper(CellFunctions cell) {
        this.cell = cell;
    }

    public ContextMenu makeContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem item1 = new MenuItemWithText("Search for contents");
        item1.setOnAction((ActionEvent e) -> cell.search());
        MenuItem item2 = new MenuItemWithText("Initialize concept builder");
        item2.setOnAction((ActionEvent e) ->  cell.initializeConceptBuilder());
        contextMenu.getItems().addAll(item1, item2);
        return contextMenu;
    }

    public void initializeConceptBuilder(ObservableVersion version) {
        if (version.getSemanticType() == VersionType.STRING) {
            StringVersion stringVersion = (StringVersion) version;
            String searchString = stringVersion.getString();
            for (GuiConceptBuilder builder: FxGet.builders()) {
                builder.initializeBuilder(searchString);
            }
        }
    }

    public void search(ObservableVersion version) {
        if (version.getSemanticType() == VersionType.STRING) {
            StringVersion stringVersion = (StringVersion) version;
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
        if (cell.getManifold().getLogicCoordinate()
                .getInferredAssemblageNid() == logicGraphVersion.getAssemblageNid()) {
            premiseType = PremiseType.INFERRED;
        }
        addDefToCell(expression, premiseType);
    }


    private void addDefToCell(LogicalExpression expression, PremiseType premiseType) {

        BorderPane defNodePanel = AxiomView.createWithCommitPanel(expression, premiseType, cell.getManifold());
        setupWidth(defNodePanel);
    }

    private void setupWidth(Region region) {
        region.setMinWidth(cell.getWidth() - 32);
        region.setPrefWidth(cell.getWidth());
        region.setMaxWidth(cell.getWidth());
        cell.widthProperty()
                .addListener(
                        makeWidthListener(region));
        cell.getPaneForVersionDisplay().getChildren().clear();
        cell.getPaneForVersionDisplay().getChildren().add(region);
        cell.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        cell.setGraphic(cell.getTextAndEditGrid());
    }

    private ChangeListener<Number> makeWidthListener(Region region) {
        return new ChangeListener<Number>() {
            @Override
            public void changed (ObservableValue observable, Number oldValue, Number newValue){
                double newTextFlowWidth = newValue.doubleValue() - 32;
                double newTextFlowHeight = region.prefHeight(newTextFlowWidth);

                region.setPrefWidth(newTextFlowWidth);
                region.setMaxWidth(newTextFlowWidth);
                region.setPrefHeight(newTextFlowHeight);
                region.setMaxHeight(newTextFlowHeight);

                double newFixedSizeWidth = newTextFlowWidth + 28;
                double newFixedSizeHeight = newTextFlowHeight + 28;

                switch (cell.getVersionType()) {
                    case LOGIC_GRAPH:
                        cell.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                        cell.getPaneForVersionDisplay().setWidth(newFixedSizeWidth);
                        cell.getPaneForVersionDisplay().setHeight(newFixedSizeHeight);
                        break;
                    default:
                        cell.getPaneForVersionDisplay().setWidth(newFixedSizeWidth);
                        cell.getPaneForVersionDisplay().setHeight(newFixedSizeHeight);
                }
            }
        };
    }

    public void addTextToCell(Text... text) {
        TextFlow textFlow = new TextFlow(text);
        textFlow.setLayoutX(1);
        textFlow.setLayoutY(1);
        textFlow.setPrefWidth(cell.getWidth() - (textFlow.getInsets().getLeft() + textFlow.getInsets().getRight()));

        setupWidth(textFlow);
    }

    public static String getTextForComponent(Manifold manifold, Chronology component) {
        switch (component.getVersionType()) {
            case CONCEPT: {
                Optional<String> latestDescriptionText = manifold.getDescriptionText(component.getNid());
                if (latestDescriptionText.isPresent()) {
                    return latestDescriptionText.get();
                }
                return "No description for concept: " + Arrays.toString(Get.identifierService().getUuidArrayForNid(component.getNid()));
            }
            case DESCRIPTION: {
                LatestVersion<DescriptionVersion> latest = component.getLatestVersion(manifold.getStampFilter());
                if (latest.isPresent()) {
                    return latest.get().getText();
                } else if (!latest.versionList().isEmpty()) {
                    return latest.versionList().get(0).getText();
                }
                return "No versions for: " + component;
            }

            default:
                LatestVersion<Version>  latest = component.getLatestVersion(manifold.getStampFilter());
                if (latest.isPresent()) {
                    return latest.get().toUserString();
                } else if (!latest.versionList().isEmpty()) {
                    return latest.versionList().get(0).toUserString();
                }
                return "No versions for: " + component;

        }
    }
    public static String getTextForComponent(Manifold manifold, Version version) {
        switch (version.getSemanticType()) {
            case CONCEPT: {
                Optional<String> latestDescriptionText = manifold.getDescriptionText(version.getNid());
                if (latestDescriptionText.isPresent()) {
                    return latestDescriptionText.get();
                }
                return "No description for concept: " + Arrays.toString(Get.identifierService().getUuidArrayForNid(version.getNid()));
            }
            case DESCRIPTION: {
                return ((DescriptionVersion) version).getText();
            }

            default:
                return version.toUserString();

        }
    }

    public void updateItem(ObservableVersion version, Labeled label, TableColumnBase tableColumn) {
        label.setWrapText(true);

        VersionType semanticType = version.getChronology()
                .getVersionType();

        label.setGraphic(null);
        label.setContentDisplay(ContentDisplay.TEXT_ONLY);

        Text assemblageNameText = new Text(
                cell.getManifold().getPreferredDescriptionText(version.getAssemblageNid()) + "\n");

        assemblageNameText.getStyleClass()
                .add(StyleClasses.ASSEMBLAGE_NAME_TEXT.toString());

        if (version.getSemanticType() == VersionType.CONCEPT) {
            processDescriptionText(label, tableColumn, getTextForComponent(this.cell.getManifold(), version));
        } else {
            SemanticVersion semanticVersion;
            if (version instanceof ObservableCategorizedVersion) {
                semanticVersion = ((ObservableCategorizedVersion) version).unwrap();
            } else {
                semanticVersion = (ObservableSemanticVersion) version;
            }
            String referencedComponentString = cell.getManifold().getPreferredDescriptionText(semanticVersion.getReferencedComponentNid());
            if (referencedComponentString == null || referencedComponentString.isEmpty()) {
                LOG.warn("No referenced component text for: " + semanticVersion.getReferencedComponentNid());
            }
            Text referencedComponentText = new Text("\n" + referencedComponentString);
            Text referencedComponentTextNoNewLine = new Text(
                    cell.getManifold().getPreferredDescriptionText(semanticVersion.getReferencedComponentNid()));

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
                    DescriptionVersion description = (DescriptionVersion) semanticVersion;
                    processDescriptionText(label, tableColumn, description.getText());
                    break;

                case COMPONENT_NID:
                    processCOMPONENT_NID(assemblageNameText, referencedComponentText, (ComponentNidVersion) semanticVersion);
                    break;

                case STRING:
                    StringVersion stringVersion = (StringVersion) semanticVersion;
                    processString(assemblageNameText, referencedComponentText, stringVersion.getString(), StyleClasses.SEMANTIC_TEXT);
                    break;

                case LOGIC_GRAPH:
                    addDefToCell((LogicGraphVersion) semanticVersion);
                    break;

                case MEMBER:
                    addTextToCell(assemblageNameText, referencedComponentTextNoNewLine);
                    break;

                case LONG:
                    processLONG(assemblageNameText, referencedComponentText, (LongVersion) semanticVersion);
                    break;

                case RF2_RELATIONSHIP:
                    processRF2_RELATIONSHIP(assemblageNameText, referencedComponentText, (Rf2Relationship) semanticVersion);
                    break;

                case Int1_Int2_Str3_Str4_Str5_Nid6_Nid7:
                    processInt1_Int2_Str3_Str4_Str5_Nid6_Nid7(assemblageNameText, referencedComponentText, (Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version) semanticVersion);
                    break;

                case LOINC_RECORD:
                    processLOINC_RECORD(assemblageNameText, referencedComponentText, (LoincVersion) semanticVersion);
                    break;

                case Nid1_Int2:
                    processNid1_Int2(assemblageNameText, referencedComponentText, (Nid1_Int2_Version) semanticVersion);
                    break;

                case Nid1_Long2:
                    processNid1_Long2(assemblageNameText, referencedComponentText, (Nid1_Long2_Version) semanticVersion);
                    break;

                case Nid1_Int2_Str3_Str4_Nid5_Nid6:
                    processNid1_Int2_Str3_Str4_Nid5_Nid6(assemblageNameText, referencedComponentText, (Nid1_Int2_Str3_Str4_Nid5_Nid6_Version) semanticVersion);
                    break;

                case Nid1_Nid2:
                    processNid1_Nid2(assemblageNameText, referencedComponentText, (Nid1_Nid2_Version) semanticVersion);
                    break;

                case Nid1_Nid2_Int3:
                    processNid1_Nid2_Int3(assemblageNameText, referencedComponentText, (Nid1_Nid2_Int3_Version) semanticVersion);
                    break;

                case Nid1_Nid2_Str3:
                    processNid1_Nid2_Str3(assemblageNameText, referencedComponentText, (Nid1_Nid2_Str3_Version) semanticVersion);
                    break;

                case Nid1_Str2:
                    processNid1_Str2(assemblageNameText, referencedComponentText, (Nid1_Str2_Version) semanticVersion);
                    break;

                case Str1_Str2:
                    processStr1_Str2(assemblageNameText, referencedComponentText, (Str1_Str2_Version) semanticVersion);
                    break;

                case Str1_Str2_Nid3_Nid4:
                    processStr1_Str2_Nid3_Nid4(assemblageNameText, referencedComponentText, (Str1_Str2_Nid3_Nid4_Version) semanticVersion);
                    break;

                case Str1_Str2_Str3_Str4_Str5_Str6_Str7:
                    processStr1_Str2_Str3_Str4_Str5_Str6_Str7(assemblageNameText, referencedComponentText, (Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version) semanticVersion);
                    break;

                case Str1_Nid2_Nid3_Nid4:
                    processStr1_Nid2_Nid3_Nid4(assemblageNameText, referencedComponentText, (Str1_Nid2_Nid3_Nid4_Version) semanticVersion);
                    break;

                case Str1_Str2_Nid3_Nid4_Nid5:
                    processStr1_Str2_Nid3_Nid4_Nid5(assemblageNameText, referencedComponentText, (Str1_Str2_Nid3_Nid4_Nid5_Version) semanticVersion);
                    break;
                    
                case DYNAMIC:
                    processDynamic(assemblageNameText, referencedComponentText, (DynamicVersion) semanticVersion);
                    break;

                default:
                    processString(assemblageNameText, referencedComponentText, "not implemented for type: " + semanticType, StyleClasses.ERROR_TEXT);
            }
        }

    }

    private void processDescriptionText(Labeled label, TableColumnBase tableColumn, String descriptionText) {
        label.setText(null);

        Text text = new Text(descriptionText);

        text.wrappingWidthProperty()
                .bind(tableColumn.widthProperty()
                        .subtract(5));
        label.setGraphic(text);
        label.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        text.getStyleClass()
                .addAll(label.getStyleClass());
    }

    private void processCOMPONENT_NID(Text assemblageNameText, Text referencedComponentText, ComponentNidVersion componentNidVersion) {
        switch (Get.identifierService()
                .getObjectTypeForComponent(componentNidVersion.getComponentNid())) {
            case CONCEPT:
                Text conceptText = new Text(cell.getManifold().getPreferredDescriptionText(componentNidVersion.getComponentNid()));

                conceptText.getStyleClass()
                        .add(StyleClasses.CONCEPT_TEXT.toString());
                addTextToCell(assemblageNameText, conceptText, referencedComponentText);
                break;

            case SEMANTIC:
                SemanticChronology semantic = Get.assemblageService()
                        .getSemanticChronology(componentNidVersion.getComponentNid());
                LatestVersion<SemanticVersion> latest = semantic.getLatestVersion(cell.getManifold().getStampFilter());

                if (latest.isPresent()) {
                    processString(assemblageNameText, referencedComponentText, latest.get().toUserString(), StyleClasses.SEMANTIC_TEXT);
                } else {
                    processString(assemblageNameText, referencedComponentText, "No latest version for component", StyleClasses.SEMANTIC_TEXT);
                }

                break;

            case UNKNOWN:
                LOG.warn("Unknown nid: " + componentNidVersion);

                processString(assemblageNameText, referencedComponentText, "Unknown nid: " + componentNidVersion, StyleClasses.ERROR_TEXT);
                break;
        }
    }

    private void processLONG(Text assemblageNameText, Text referencedComponentText, LongVersion longVersion) {
        // TODO, rely on semantic info from assemblage in the future to
        // eliminate this date hack...
        ZonedDateTime zonedDateTime = Instant.ofEpochMilli(longVersion.getLongValue())
                .atZone(ZoneOffset.UTC);

        if ((zonedDateTime.getYear() > 1900) && (zonedDateTime.getYear() < 2232)) {
            processString(assemblageNameText, referencedComponentText, formatter.format(zonedDateTime), StyleClasses.DATE_TEXT);
        } else {
            Text longText = new Text(Long.toString(longVersion.getLongValue()));

            longText.getStyleClass()
                    .add(StyleClasses.DATE_TEXT.toString());
            addTextToCell(assemblageNameText, longText, referencedComponentText);
        }
    }

    private void processRF2_RELATIONSHIP(Text assemblageNameText, Text referencedComponentText, Rf2Relationship rf2Relationship) {
        StringBuilder buff = new StringBuilder();
        if (!(rf2Relationship.getTypeNid() == TermAux.IS_A.getNid())) {
            buff.append(this.cell.getManifold().getPreferredDescriptionText(rf2Relationship.getModifierNid()));
            buff.append(" ");
        }
        buff.append(this.cell.getManifold().getPreferredDescriptionText(rf2Relationship.getTypeNid()));
        buff.append(" ");
        buff.append(this.cell.getManifold().getPreferredDescriptionText(rf2Relationship.getDestinationNid()));
        buff.append(" (");
        buff.append(this.cell.getManifold().getPreferredDescriptionText(rf2Relationship.getCharacteristicNid()));
        buff.append(") ");

        Text defaultText = new Text(buff.toString());
        addTextToCell(assemblageNameText, defaultText, referencedComponentText);
    }

    private void processInt1_Int2_Str3_Str4_Str5_Nid6_Nid7(Text assemblageNameText, Text referencedComponentText, Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version brittleVersion) {

        String buff = brittleVersion.getInt1() +
                "\n" +
                brittleVersion.getInt2() +
                "\n" +
                brittleVersion.getStr3() +
                "\n" +
                brittleVersion.getStr4() +
                "\n" +
                brittleVersion.getStr5() +
                "\n" +
                cell.getManifold().getPreferredDescriptionText(brittleVersion.getNid6()) +
                "\n" +
                cell.getManifold().getPreferredDescriptionText(brittleVersion.getNid7()) +
                " ";
        Text defaultText = new Text(buff);
        addTextToCell(assemblageNameText, defaultText, referencedComponentText);
    }
    private void processLOINC_RECORD(Text assemblageNameText, Text referencedComponentText, LoincVersion brittleVersion) {

        String buff = brittleVersion.getLoincNum() +
                " " +
                brittleVersion.getShortName() +
                " - " +
                brittleVersion.getLoincStatus() +
                "\n" +
                brittleVersion.getLongCommonName() +
                "\nc:" +
                brittleVersion.getComponent() +
                " m: " +
                brittleVersion.getMethodType() +
                "\np: " +
                brittleVersion.getProperty() +
                " - " +
                brittleVersion.getScaleType() +
                " s: " +
                brittleVersion.getSystem() +
                " t: " +
                brittleVersion.getTimeAspect();
        Text defaultText = new Text(buff);
        addTextToCell(assemblageNameText, defaultText, referencedComponentText);
    }

    private void processDynamic(Text assemblageNameText, Text referencedComponentText, DynamicVersion version) {
        Text defaultText = new Text(version.dataToString());
        addTextToCell(assemblageNameText, defaultText, referencedComponentText);
    }

    private void processNid1_Int2(Text assemblageNameText, Text referencedComponentText, Nid1_Int2_Version brittleVersion) {

        String buff = cell.getManifold().getPreferredDescriptionText(brittleVersion.getNid1()) +
                "\n" +
                brittleVersion.getInt2() +
                " ";
        Text defaultText = new Text(buff);
        addTextToCell(assemblageNameText, defaultText, referencedComponentText);
    }
    private void processNid1_Long2(Text assemblageNameText, Text referencedComponentText, Nid1_Long2_Version brittleVersion) {

        String buff = cell.getManifold().getPreferredDescriptionText(brittleVersion.getNid1()) +
                "\n" +
                brittleVersion.getLong2() +
                " ";
        Text defaultText = new Text(buff);
        addTextToCell(assemblageNameText, defaultText, referencedComponentText);
    }

    private void processNid1_Int2_Str3_Str4_Nid5_Nid6(Text assemblageNameText, Text referencedComponentText, Nid1_Int2_Str3_Str4_Nid5_Nid6_Version brittleVersion) {

        String buff = cell.getManifold().getPreferredDescriptionText(brittleVersion.getNid1()) +
                "\n" +
                brittleVersion.getInt2() +
                "\n" +
                brittleVersion.getStr3() +
                "\n" +
                brittleVersion.getStr4() +
                "\n" +
                cell.getManifold().getPreferredDescriptionText(brittleVersion.getNid5()) +
                "\n" +
                cell.getManifold().getPreferredDescriptionText(brittleVersion.getNid6()) +
                " ";
        Text defaultText = new Text(buff);
        addTextToCell(assemblageNameText, defaultText, referencedComponentText);
    }

    private void processNid1_Nid2(Text assemblageNameText, Text referencedComponentText, Nid1_Nid2_Version brittleVersion) {

        String buff = cell.getManifold().getPreferredDescriptionText(brittleVersion.getNid1()) +
                "\n" +
                cell.getManifold().getPreferredDescriptionText(brittleVersion.getNid2()) +
                " ";
        Text defaultText = new Text(buff);
        addTextToCell(assemblageNameText, defaultText, referencedComponentText);
    }

    private void processNid1_Nid2_Int3(Text assemblageNameText, Text referencedComponentText, Nid1_Nid2_Int3_Version brittleVersion) {

        String buff = cell.getManifold().getPreferredDescriptionText(brittleVersion.getNid1()) +
                "\n" +
                cell.getManifold().getPreferredDescriptionText(brittleVersion.getNid2()) +
                "\n" +
                brittleVersion.getInt3() +
                " ";
        Text defaultText = new Text(buff);
        addTextToCell(assemblageNameText, defaultText, referencedComponentText);
    }

    private void processNid1_Nid2_Str3(Text assemblageNameText, Text referencedComponentText, Nid1_Nid2_Str3_Version brittleVersion) {

        String buff = cell.getManifold().getPreferredDescriptionText(brittleVersion.getNid1()) +
                "\n" +
                cell.getManifold().getPreferredDescriptionText(brittleVersion.getNid2()) +
                "\n" +
                brittleVersion.getStr3() +
                " ";
        Text defaultText = new Text(buff);
        addTextToCell(assemblageNameText, defaultText, referencedComponentText);
    }

    private void processNid1_Str2(Text assemblageNameText, Text referencedComponentText, Nid1_Str2_Version brittleVersion) {

        String buff = cell.getManifold().getPreferredDescriptionText(brittleVersion.getNid1()) +
                "\n" +
                brittleVersion.getStr2() +
                " ";
        Text defaultText = new Text(buff);
        addTextToCell(assemblageNameText, defaultText, referencedComponentText);
    }

    private void processStr1_Str2(Text assemblageNameText, Text referencedComponentText, Str1_Str2_Version brittleVersion) {

        String buff = brittleVersion.getStr1() +
                "\n" +
                brittleVersion.getStr2() +
                " ";
        Text defaultText = new Text(buff);
        addTextToCell(assemblageNameText, defaultText, referencedComponentText);
    }

    private void processStr1_Str2_Nid3_Nid4(Text assemblageNameText, Text referencedComponentText, Str1_Str2_Nid3_Nid4_Version brittleVersion) {

        String buff = brittleVersion.getStr1() +
                "\n" +
                brittleVersion.getStr2() +
                "\n" +
                cell.getManifold().getPreferredDescriptionText(brittleVersion.getNid3()) +
                "\n" +
                cell.getManifold().getPreferredDescriptionText(brittleVersion.getNid4()) +
                " ";
        Text defaultText = new Text(buff);
        addTextToCell(assemblageNameText, defaultText, referencedComponentText);
    }

    private void processStr1_Str2_Str3_Str4_Str5_Str6_Str7(Text assemblageNameText, Text referencedComponentText, Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version brittleVersion) {

        String buff = brittleVersion.getStr1() +
                "\n" +
                brittleVersion.getStr2() +
                "\n" +
                brittleVersion.getStr3() +
                "\n" +
                brittleVersion.getStr4() +
                "\n" +
                brittleVersion.getStr5() +
                "\n" +
                brittleVersion.getStr6() +
                "\n" +
                brittleVersion.getStr7() +
                " ";
        Text defaultText = new Text(buff);
        addTextToCell(assemblageNameText, defaultText, referencedComponentText);
    }

    private void processStr1_Nid2_Nid3_Nid4(Text assemblageNameText, Text referencedComponentText, Str1_Nid2_Nid3_Nid4_Version brittleVersion) {

        String buff = brittleVersion.getStr1() +
                "\n" +
                cell.getManifold().getPreferredDescriptionText(brittleVersion.getNid2()) +
                "\n" +
                cell.getManifold().getPreferredDescriptionText(brittleVersion.getNid3()) +
                "\n" +
                cell.getManifold().getPreferredDescriptionText(brittleVersion.getNid4()) +
                " ";
        Text defaultText = new Text(buff);
        addTextToCell(assemblageNameText, defaultText, referencedComponentText);
    }

    private void processStr1_Str2_Nid3_Nid4_Nid5(Text assemblageNameText, Text referencedComponentText, Str1_Str2_Nid3_Nid4_Nid5_Version brittleVersion) {

        String buff = brittleVersion.getStr1() +
                "\n" +
                brittleVersion.getStr2() +
                "\n" +
                cell.getManifold().getPreferredDescriptionText(brittleVersion.getNid3()) +
                "\n" +
                cell.getManifold().getPreferredDescriptionText(brittleVersion.getNid4()) +
                "\n" +
                cell.getManifold().getPreferredDescriptionText(brittleVersion.getNid5()) +
                " ";
        Text defaultText = new Text(buff);
        addTextToCell(assemblageNameText, defaultText, referencedComponentText);
    }

    private void processString(Text assemblageNameText, Text referencedComponentText, String string, StyleClasses styleClass) {
        Text stringText = new Text(string);

        stringText.getStyleClass()
                .add(styleClass.toString());
        addTextToCell(assemblageNameText, stringText, referencedComponentText);
    }
}
