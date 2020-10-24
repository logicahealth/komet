package sh.komet.gui.provider.classification;

import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import sh.isaac.api.Get;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.NaturalOrder;

import java.util.Set;

public class PrepareClassifierEquivalencies extends TimedTaskWithProgressTracker<Void> {

    final Set<int[]> equivalentSets;
    final TreeView<StringWithOptionalConceptSpec> equivalenciesTree;

    public PrepareClassifierEquivalencies(Set<int[]> equivalentSets,
                                          TreeView<StringWithOptionalConceptSpec> equivalenciesTree) {
        this.equivalentSets = equivalentSets;
        this.equivalenciesTree = equivalenciesTree;
    }

    @Override
    protected Void call() throws Exception {
        TreeItem<StringWithOptionalConceptSpec> root = new TreeItem<>(new StringWithOptionalConceptSpec("Equivalences Root"));
        root.setExpanded(true);
        for (int[] elements: equivalentSets) {
            String conceptDescriptionText = Get.conceptDescriptionText(elements[0]);
            if (conceptDescriptionText.startsWith("Product containing precisely ")) {
                conceptDescriptionText = conceptDescriptionText.replace("Product containing precisely ", "");
            }
            TreeItem<StringWithOptionalConceptSpec> setItem = new TreeItem<>(new StringWithOptionalConceptSpec(
                    "Set of " + elements.length + " containing: " + conceptDescriptionText, Get.conceptSpecification(elements[0])));
            root.getChildren().add(setItem);
            for (int i = 0; i < elements.length; i++) {
                int nid = elements[i];
                TreeItem<StringWithOptionalConceptSpec> equivalentItem =  new TreeItem<>(new StringWithOptionalConceptSpec(Get.conceptDescriptionText(nid), Get.conceptSpecification(nid)));
                setItem.getChildren().add(equivalentItem);
            }
            setItem.getChildren().sort((o1, o2) -> NaturalOrder.compareStrings(o1.getValue().label, o2.getValue().label));
        }

        root.getChildren().sort((o1, o2) -> NaturalOrder.compareStrings(o1.getValue().conceptSpecification.getFullyQualifiedName(), o2.getValue().conceptSpecification.getFullyQualifiedName()));
        Platform.runLater(() -> {
            equivalenciesTree.setRoot(root);
            equivalenciesTree.setShowRoot(false);
        });
        return null;
    }
}
