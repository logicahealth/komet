package sh.komet.gui.control.badged;

import static sh.komet.gui.util.FxUtils.setupHeaderPanel;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.map.OpenIntIntHashMap;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.set.primitive.IntSet;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import sh.isaac.MetaData;
import sh.isaac.api.Status;
import sh.isaac.api.chronicle.CategorizedVersions;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedListProperty;
import sh.komet.gui.control.property.wrapper.PropertySheetMenuItem;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.style.PseudoClasses;
import sh.komet.gui.style.StyleClasses;

public class ComponentPaneModel extends BadgedVersionPaneModel {
    private static final Logger LOG = LogManager.getLogger();
    private final CategorizedVersions<ObservableCategorizedVersion> categorizedVersions;
    private final AnchorPane extensionHeaderPanel = setupHeaderPanel("Attachments:");
    private final AnchorPane versionHeaderPanel = setupHeaderPanel("Change history:");

    //~--- constructors --------------------------------------------------------
    public ComponentPaneModel(ViewProperties viewProperties, ObservableCategorizedVersion categorizedVersion,
                              List<ConceptSpecification> semanticOrderForChronology,
                              OpenIntIntHashMap stampOrderHashMap,
                              HashMap<String, AtomicBoolean> disclosureStateMap) {
        super(viewProperties, categorizedVersion, semanticOrderForChronology, stampOrderHashMap, disclosureStateMap);

        this.categorizedVersions = categorizedVersion.getCategorizedVersions();

        // gridpane.gridLinesVisibleProperty().set(true);
        getBadgedPane().getStyleClass()
                .add(StyleClasses.COMPONENT_PANEL.toString());
        getBadgedPane().pseudoClassStateChanged(PseudoClasses.INACTIVE_PSEUDO_CLASS, !categorizedVersion.isActive());

        ObservableVersion observableVersion = getCategorizedVersion().getObservableVersion();

        isContradiction.set(this.categorizedVersions.getLatestVersion()
                .isContradicted());

        if (!this.categorizedVersions.getUncommittedVersions().isEmpty()) {
            if (this.categorizedVersions.getUncommittedVersions().size() > 1) {
                LOG.error("Error: Can't handle more than one uncommitted version in this editor...");
            }
            ObservableCategorizedVersion uncommittedVersion = this.categorizedVersions.getUncommittedVersions().get(0);
            Optional<PropertySheetMenuItem> propertySheetMenuItem = uncommittedVersion.getUserObject(PROPERTY_SHEET_ATTACHMENT);
            if (propertySheetMenuItem.isPresent()) {
                this.addEditingPropertySheet(propertySheetMenuItem.get());
            } else {
                LOG.error("Warn: No property sheet editor for this uncommitted version...\n       " + uncommittedVersion.getPrimordialUuid()
                        + "\n       " + uncommittedVersion + "\nWill treat uncommitted as a historic version. ");
                versionPanes.add(new VersionPaneModel(viewProperties, uncommittedVersion, semanticOrderForChronology, stampOrderHashMap,
                        getDisclosureStateMap()));
            }
        }

        if (this.categorizedVersions.getLatestVersion()
                .isContradicted()) {
            this.categorizedVersions.getLatestVersion()
                    .contradictions()
                    .forEach(
                            (contradiction) -> {
                                if (contradiction.getStampSequence() != -1) {
                                    versionPanes.add(new VersionPaneModel(viewProperties, contradiction, semanticOrderForChronology, stampOrderHashMap,
                                            getDisclosureStateMap()));
                                }
                            });
        }

        this.categorizedVersions.getHistoricVersions()
                .forEach(
                        (historicVersion) -> {
                            if (historicVersion.getStampSequence() != -1 && historicVersion.getStatus() != Status.CANCELED) {
                                versionPanes.add(new VersionPaneModel(viewProperties, historicVersion, semanticOrderForChronology, stampOrderHashMap,
                                        getDisclosureStateMap()));
                            }
                        });

        List<ObservableSemanticChronology> filteredSemantics = observableVersion.getChronology()
                .getObservableSemanticList().stream().filter(observableSemanticChronology -> {
            switch (observableSemanticChronology.getVersionType()) {
                case DESCRIPTION:
                case LOGIC_GRAPH:
                case RF2_RELATIONSHIP:
                    return false;
                default:
                    return true;
            }
        }).collect(Collectors.toList());

        filterAndSortByAssemblage(filteredSemantics, semanticOrderForChronology)
                .forEach(observableSemanticChronology -> addChronology(observableSemanticChronology, stampOrderHashMap));

        expandControl.setVisible(!versionPanes.isEmpty() || !extensionPaneModels.isEmpty());
    }

    public static List<ObservableSemanticChronology> filterAndSortByAssemblage(List<ObservableSemanticChronology> semantics,
                                                                               List<ConceptSpecification> assemblagePriorityList) {
        // SimpleEqualityBasedListProperty<ConceptSpecification>
        // Delete any versions not active in configuration
        IntList assemblageOrderList = IntLists.immutable.ofAll(assemblagePriorityList.stream().mapToInt(value -> value.getNid()));
        List<ObservableSemanticChronology> filteredAndSortedSemantics = new ArrayList<>(semantics.size());
        if (!assemblageOrderList.contains(MetaData.ANY_COMPONENT____SOLOR.getNid())) {
            // need to filter
            IntSet allowedAssemblageSet = IntSets.immutable.ofAll(assemblageOrderList);
            semantics.stream().forEach(observableCategorizedVersion -> {
                if (allowedAssemblageSet.contains(observableCategorizedVersion.getAssemblageNid())) {
                    filteredAndSortedSemantics.add(observableCategorizedVersion);
                }
            });
        } else {
            filteredAndSortedSemantics.addAll(semantics);
        }
        // now need to sort...
        filteredAndSortedSemantics.sort(compareWithList(assemblagePriorityList));
        return filteredAndSortedSemantics;
    }
    public static Comparator<ObservableSemanticChronology> compareWithList(List<ConceptSpecification> semanticOrderForChronology) {
        final IntList assemblageOrderList = IntLists.immutable.ofAll(semanticOrderForChronology.stream().mapToInt(value -> value.getNid()));
        return compareWithList(assemblageOrderList);
    }

    public static Comparator<ObservableSemanticChronology> compareWithList(IntList assemblageOrderList) {
        return (o1, o2) -> {
            int o1index = assemblageOrderList.indexOf(o1.getAssemblageNid());
            int o2index = assemblageOrderList.indexOf(o2.getAssemblageNid());
            if (o1index == o2index) {
                // same assemblage
                return o1.toString().compareTo(o2.toString());
            }
            if (o1index == -1) {
                return 1;
            }
            if (o2index == -1) {
                return -1;
            }
            return (o1index < o2index) ? -1 : 1;
        };
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
            case Nid1_Long2:
            case IMAGE:
            case DYNAMIC:
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
                if (!versionPanes.isEmpty()) {
                    getBadgedPane().getChildren()
                            .remove(versionHeaderPanel);
                }
                versionPanes.forEach(
                        (paneModel) -> {
                            getBadgedPane().getChildren()
                                    .remove(paneModel.getBadgedPane());
                        });
                if (!extensionPaneModels.isEmpty()) {
                    getBadgedPane().getChildren()
                            .remove(extensionHeaderPanel);
                }
                extensionPaneModels.forEach(
                        (paneModel) -> {
                            getBadgedPane().getChildren()
                                    .remove(paneModel.getBadgedPane());
                        });
                break;

            case SHOW_CHILDREN:
                if (!versionPanes.isEmpty()) {
                    addVersionPane(versionHeaderPanel);
                }
                // Add most recent first, and add oldest last... Reverse of the sort.
                for (int i = versionPanes.size() - 1; i > -1; i--) {
                    this.addVersionPane(versionPanes.get(i));
                }
                if (!extensionPaneModels.isEmpty()) {
                    addAttachmentPane(extensionHeaderPanel);
                }
                extensionPaneModels.forEach(this::addAttachmentPane);
                break;

            default:
                throw new UnsupportedOperationException("am Can't handle: " + expandControl.getExpandAction());
        }
    }

    private void addChronology(ObservableChronology observableChronology, OpenIntIntHashMap stampOrderHashMap) {
        if (isSemanticTypeSupported(observableChronology)) {
            CategorizedVersions<ObservableCategorizedVersion> oscCategorizedVersions
                    = observableChronology.getCategorizedVersions(
                    getManifoldCoordinate().getViewStampFilter());

            if (oscCategorizedVersions.getLatestVersion()
                    .isPresent()) {
                ComponentPaneModel newPanel = new ComponentPaneModel(getViewProperties(),
                        oscCategorizedVersions.getLatestVersion().get(), this.semanticOrderForChronology,
                        stampOrderHashMap, getDisclosureStateMap());

                extensionPaneModels.add(newPanel);
            } else if (!oscCategorizedVersions.getUncommittedVersions().isEmpty()) {
                ComponentPaneModel newPanel = new ComponentPaneModel(getViewProperties(),
                        oscCategorizedVersions.getUncommittedVersions().get(0), this.semanticOrderForChronology,
                        stampOrderHashMap, getDisclosureStateMap());
                extensionPaneModels.add(newPanel);
            }
        }
    }

    private void addVersionPane(VersionPaneModel versionPane) {
        addVersionPane(versionPane.getBadgedPane());
    }

    private void addAttachmentPane(ComponentPaneModel componentPane) {
        componentPane.wrapAttachmentPane();
        addVersionPane(componentPane.getBadgedPane());
    }

    private void addVersionPane(Node panel) {
        getBadgedPane().getChildren()
                .remove(panel);
        getBadgedPane().getChildren()
                .add(panel);
    }

    private void addAttachmentPane(Node panel) {
        getBadgedPane().getChildren()
                .remove(panel);
        getBadgedPane().getChildren()
                .add(panel);
    }

    @Override
    protected boolean isLatestPanel() {
        return true;
    }
}
