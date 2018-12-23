package sh.komet.gui.control.badged;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.apache.mahout.math.map.OpenIntIntHashMap;
import sh.isaac.api.chronicle.CategorizedVersions;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.ObservableVersion;
import sh.komet.gui.control.ComponentPanel;
import sh.komet.gui.control.PropertySheetMenuItem;
import sh.komet.gui.control.VersionPanel;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.style.StyleClasses;

import java.util.Optional;

import static sh.komet.gui.util.FxUtils.setupHeaderPanel;

public class Component extends BadgedVersion {
    private final CategorizedVersions<ObservableCategorizedVersion> categorizedVersions;
    private final AnchorPane extensionHeaderPanel = setupHeaderPanel("Extensions:");
    private final AnchorPane versionHeaderPanel = setupHeaderPanel("Change history:", "Revert");

    //~--- constructors --------------------------------------------------------
    public Component(Manifold manifold, ObservableCategorizedVersion categorizedVersion,
                          OpenIntIntHashMap stampOrderHashMap) {
        super(manifold, categorizedVersion, stampOrderHashMap);

        this.categorizedVersions = categorizedVersion.getCategorizedVersions();

        // gridpane.gridLinesVisibleProperty().set(true);
        getPane().getStyleClass()
                .add(StyleClasses.COMPONENT_PANEL.toString());

        ObservableVersion observableVersion = getCategorizedVersion().getObservableVersion();

        isContradiction.set(this.categorizedVersions.getLatestVersion()
                .isContradicted());

        if (!this.categorizedVersions.getUncommittedVersions().isEmpty()) {
            if (this.categorizedVersions.getUncommittedVersions().size() > 1) {
                System.err.println("Error: can't handle more than one uncommitted version in this editor...");
            }
            ObservableCategorizedVersion uncommittedVersion = this.categorizedVersions.getUncommittedVersions().get(0);
            Optional<PropertySheetMenuItem> propertySheetMenuItem = uncommittedVersion.getUserObject(PROPERTY_SHEET_ATTACHMENT);
            if (propertySheetMenuItem.isPresent()) {
                this.addEditingPropertySheet(propertySheetMenuItem.get());
            } else {
                System.err.println("Error: No property sheet editor for this uncommitted version...\n       " + uncommittedVersion.getPrimordialUuid()
                        + "\n       " + uncommittedVersion);
            }
        }

        if (this.categorizedVersions.getLatestVersion()
                .isContradicted()) {
            this.categorizedVersions.getLatestVersion()
                    .contradictions()
                    .forEach(
                            (contradiction) -> {
                                if (contradiction.getStampSequence() != -1) {
                                    versionPanels.add(new VersionPanel(manifold, contradiction, stampOrderHashMap));
                                }
                            });
        }

        this.categorizedVersions.getHistoricVersions()
                .forEach(
                        (historicVersion) -> {
                            if (historicVersion.getStampSequence() != -1) {
                                versionPanels.add(new VersionPanel(manifold, historicVersion, stampOrderHashMap));
                            }
                        });
        observableVersion.getChronology()
                .getObservableSemanticList()
                .forEach(
                        (osc) -> {
                            switch (osc.getVersionType()) {
                                case DESCRIPTION:
                                case LOGIC_GRAPH:
                                case RF2_RELATIONSHIP:
                                    break;  // Ignore, description and logic graph where already added as an independent panel

                                default:
                                    addChronology(osc, stampOrderHashMap);
                            }
                        });
        expandControl.setVisible(!versionPanels.isEmpty() || !extensionPanels.isEmpty());
    }

    public static boolean isSemanticTypeSupported(Chronology chronology) {
        return isSemanticTypeSupported(chronology.getVersionType());
    }
    public static boolean isSemanticTypeSupported(VersionType semanticType) {
        switch (semanticType) {
            case STRING:
            case COMPONENT_NID:
            case LOGIC_GRAPH:
            case LONG:
            case MEMBER:
            case CONCEPT:
            case DESCRIPTION:
            case Nid1_Int2:
                return true;

            case RF2_RELATIONSHIP:
                return false;

            default:
                //may consider supporting more types in the future.
                return false;
        }
    }

    //~--- methods -------------------------------------------------------------
    @Override
    public void addExtras() {
        switch (expandControl.getExpandAction()) {
            case HIDE_CHILDREN:
                if (!versionPanels.isEmpty()) {
                    gridpane.getChildren()
                            .remove(versionHeaderPanel);
                }
                versionPanels.forEach(
                        (panel) -> {
                            gridpane.getChildren()
                                    .remove(panel);
                        });
                if (!extensionPanels.isEmpty()) {
                    gridpane.getChildren()
                            .remove(extensionHeaderPanel);
                }
                extensionPanels.forEach(
                        (panel) -> {
                            gridpane.getChildren()
                                    .remove(panel);
                        });
                break;

            case SHOW_CHILDREN:
                if (!versionPanels.isEmpty()) {
                    addPanel(versionHeaderPanel);
                }
                versionPanels.forEach(this::addPanel);
                if (!extensionPanels.isEmpty()) {
                    addPanel(extensionHeaderPanel);
                }
                extensionPanels.forEach(this::addPanel);
                break;

            default:
                throw new UnsupportedOperationException("am Can't handle: " + expandControl.getExpandAction());
        }
    }

    private void addChronology(ObservableChronology observableChronology, OpenIntIntHashMap stampOrderHashMap) {
        if (isSemanticTypeSupported(observableChronology)) {
            CategorizedVersions<ObservableCategorizedVersion> oscCategorizedVersions
                    = observableChronology.getCategorizedVersions(
                    getManifold());

            if (oscCategorizedVersions.getLatestVersion()
                    .isPresent()) {
                ComponentPanel newPanel = new ComponentPanel(getManifold(),
                        oscCategorizedVersions.getLatestVersion().get(), stampOrderHashMap);

                extensionPanels.add(newPanel);
            } else if (!oscCategorizedVersions.getUncommittedVersions().isEmpty()) {
                ComponentPanel newPanel = new ComponentPanel(getManifold(),
                        oscCategorizedVersions.getUncommittedVersions().get(0), stampOrderHashMap);
                extensionPanels.add(newPanel);
            }
        }
    }

    private void addPanel(Node panel) {
        extraGridRows++;
        getPane().getChildren()
                .remove(panel);
        GridPane.setConstraints(
                panel,
                0,
                getRows() + extraGridRows,
                getColumns(),
                1,
                HPos.LEFT,
                VPos.CENTER,
                Priority.ALWAYS,
                Priority.NEVER,
                new Insets(2));
        gridpane.getChildren()
                .add(panel);
    }

    @Override
    protected boolean isLatestPanel() {
        return true;
    }
}
