package sh.komet.gui.provider.classification;

import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;
import sh.isaac.api.ComponentProxy;
import sh.isaac.api.Get;
import sh.isaac.api.classifier.ClassifierResults;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;

public class ClassifierResultsController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Accordion resultsAccordion;

    @FXML
    private TitledPane cyclesPane;

    @FXML
    private TreeView<StringWithOptionalConceptSpec> cyclesTree;

    @FXML
    private TitledPane orphansPane;

    @FXML
    private TitledPane equivalenciesPane;

    @FXML
    private TreeView<StringWithOptionalConceptSpec> equivalenciesTree;

    @FXML
    private TitledPane inferredChangesPane;

    @FXML
    private ListView<Integer> inferredChangesList;

    @FXML
    private ListView<Integer> orphanList;

    @FXML
    private TitledPane stampCoordinatePane;

    @FXML
    private TextArea stampTextArea;

    @FXML
    private TitledPane logicCoordinatePane;

    @FXML
    private TextArea logicTextArea;

    @FXML
    private TitledPane editCoordinatePane;

    @FXML
    private TextArea editTextArea;

    @FXML
    void initialize() {
        assert resultsAccordion != null : "fx:id=\"resultsAccordion\" was not injected: check your FXML file 'ClassifierResultsInterface.fxml'.";
        assert cyclesPane != null : "fx:id=\"cyclesPane\" was not injected: check your FXML file 'ClassifierResultsInterface.fxml'.";
        assert cyclesTree != null : "fx:id=\"cyclesTree\" was not injected: check your FXML file 'ClassifierResultsInterface.fxml'.";
        assert orphansPane != null : "fx:id=\"orphansPane\" was not injected: check your FXML file 'ClassifierResultsInterface.fxml'.";
        assert equivalenciesPane != null : "fx:id=\"equivalenciesPane\" was not injected: check your FXML file 'ClassifierResultsInterface.fxml'.";
        assert equivalenciesTree != null : "fx:id=\"equivalenciesTree\" was not injected: check your FXML file 'ClassifierResultsInterface.fxml'.";
        assert inferredChangesPane != null : "fx:id=\"inferredChangesPane\" was not injected: check your FXML file 'ClassifierResultsInterface.fxml'.";
        assert inferredChangesList != null : "fx:id=\"inferredChangesList\" was not injected: check your FXML file 'ClassifierResultsInterface.fxml'.";
        assert orphanList != null : "fx:id=\"orphanList\" was not injected: check your FXML file 'ClassifierResultsInterface.fxml'.";
        assert stampCoordinatePane != null : "fx:id=\"stampCoordinatePane\" was not injected: check your FXML file 'ClassifierResultsInterface.fxml'.";
        assert stampTextArea != null : "fx:id=\"stampTextArea\" was not injected: check your FXML file 'ClassifierResultsInterface.fxml'.";
        assert logicCoordinatePane != null : "fx:id=\"logicCoordinatePane\" was not injected: check your FXML file 'ClassifierResultsInterface.fxml'.";
        assert logicTextArea != null : "fx:id=\"logicTextArea\" was not injected: check your FXML file 'ClassifierResultsInterface.fxml'.";
        assert editCoordinatePane != null : "fx:id=\"editCoordinatePane\" was not injected: check your FXML file 'ClassifierResultsInterface.fxml'.";
        assert editTextArea != null : "fx:id=\"editTextArea\" was not injected: check your FXML file 'ClassifierResultsInterface.fxml'.";

        inferredChangesList.setCellFactory(conceptCellFactory());
        orphanList.setCellFactory(conceptCellFactory());
        orphanList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        inferredChangesList.getSelectionModel().getSelectedItems().addListener(this::onChanged);
        inferredChangesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        orphanList.getSelectionModel().getSelectedItems().addListener(this::onChanged);
        orphanList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        equivalenciesTree.getSelectionModel().getSelectedItems().addListener(this::onTreeSelectionChanged);
        equivalenciesTree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        cyclesTree.getSelectionModel().getSelectedItems().addListener(this::onTreeSelectionChanged);
        cyclesTree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    }
    private void onTreeSelectionChanged(ListChangeListener.Change<? extends TreeItem<StringWithOptionalConceptSpec>> change) {
        if (!change.getList().isEmpty()) {
            ArrayList<ComponentProxy> changeList = new ArrayList<>(change.getList().size());
            for (TreeItem<StringWithOptionalConceptSpec> treeItem: change.getList()) {
                if (treeItem.getValue().getOptionalConceptSpecification().isPresent()) {
                    changeList.add(new ComponentProxy(treeItem.getValue().conceptSpecification));
                }
            }
            FxGet.manifold(Manifold.ManifoldGroup.CLASSIFICATON).manifoldSelectionProperty().setAll(changeList);

       }
    }


    private void onChanged(ListChangeListener.Change<? extends Integer> change) {
        if (!change.getList().isEmpty()) {
            ArrayList<ComponentProxy> changeList = new ArrayList<>(change.getList().size());
            for (Integer conceptNid: change.getList()) {
                changeList.add(new ComponentProxy(Get.concept(conceptNid)));
            }
            FxGet.manifold(Manifold.ManifoldGroup.CLASSIFICATON).manifoldSelectionProperty().setAll(changeList);
        }
    }

    private Callback<ListView<Integer>, ListCell<Integer>> conceptCellFactory() {
        return new Callback<ListView<Integer>, ListCell<Integer>>()
        {
            @Override
            public ListCell<Integer> call(ListView<Integer> param)
            {
                return new ListCell<Integer>()
                {
                    @Override
                    protected void updateItem(final Integer item, boolean empty)
                    {
                        super.updateItem(item, empty);
                        if (item == null || empty)
                        {
                            setText("");
                            setGraphic(null);
                        }
                        else
                        {
                            setText(Get.conceptDescriptionText(item));
                        }
                    }
                };
            }
        };
    }

    public void setResults(ClassifierResults classifierResults) {
        if (classifierResults.getCycles().isPresent()) {
            Map<Integer, Set<int[]>> cycles = classifierResults.getCycles().get();
            cyclesPane.setText(cyclesPane.getText() + ": " + NumberFormat.getInstance().format(cycles.size()));
            TreeItem<StringWithOptionalConceptSpec> root = new TreeItem<>(new StringWithOptionalConceptSpec("Cycles Root"));
            root.setExpanded(true);
            for (Map.Entry<Integer, Set<int[]>> entry: cycles.entrySet()) {
                TreeItem<StringWithOptionalConceptSpec> conceptWithCycle = new TreeItem<>(new StringWithOptionalConceptSpec(
                        Get.conceptDescriptionText(entry.getKey()), Get.conceptSpecification(entry.getKey())));
                root.getChildren().add(conceptWithCycle);
                for (int[] cycleSet: entry.getValue()) {
                    TreeItem<StringWithOptionalConceptSpec> cycleSetTreeItem = new TreeItem<>(new StringWithOptionalConceptSpec(
                            "cycle elements"));
                    cycleSetTreeItem.setExpanded(true);
                    conceptWithCycle.getChildren().add(cycleSetTreeItem);
                    for (int nid: cycleSet) {
                        TreeItem<StringWithOptionalConceptSpec> conceptInCycle = new TreeItem<>(new StringWithOptionalConceptSpec(
                                Get.conceptDescriptionText(nid), Get.conceptSpecification(nid)));
                        cycleSetTreeItem.getChildren().add(conceptInCycle);
                    }
                }
            }
            cyclesTree.setRoot(root);
            cyclesTree.setShowRoot(false);
        } else {
            cyclesPane.setText(cyclesPane.getText() + ": none");
            cyclesPane.setDisable(true);
        }

        if ( classifierResults.getOrphans().isEmpty()) {
            orphansPane.setText(orphansPane.getText() + ": none");
            orphansPane.setDisable(true);
        } else {
            orphansPane.setText(orphansPane.getText() + ": " + NumberFormat.getInstance().format(classifierResults.getOrphans().size()));
            Get.executor().execute(new PrepareConceptSet("Sorting list of orphans", classifierResults.getOrphans(), orphanList.getItems()));
        }

        if ( classifierResults.getEquivalentSets().isEmpty()) {
            equivalenciesPane.setText(equivalenciesPane.getText() + ": none");
            equivalenciesPane.setDisable(true);
        } else {
            equivalenciesPane.setText(equivalenciesPane.getText() + ": "  + NumberFormat.getInstance().format(classifierResults.getEquivalentSets().size()));
            Get.executor().execute(new PrepareClassifierEquivalencies(classifierResults.getEquivalentSets(), equivalenciesTree));
         }

        if ( classifierResults.getConceptsWithInferredChanges().isEmpty()) {
            inferredChangesPane.setText(inferredChangesPane.getText() + ": none");
            inferredChangesPane.setDisable(true);
        } else {
            inferredChangesPane.setText(inferredChangesPane.getText() + ": " + NumberFormat.getInstance().format(classifierResults.getConceptsWithInferredChanges().size()));
            Get.executor().execute(new PrepareConceptSet("Sorting list of inferred changes", classifierResults.getConceptsWithInferredChanges(), inferredChangesList.getItems()));
        }

        stampTextArea.setText(classifierResults.getStampCoordinate().toUserString());
        logicTextArea.setText(classifierResults.getLogicCoordinate().toUserString());
        editTextArea.setText(classifierResults.getEditCoordinate().toUserString());
    }
}
