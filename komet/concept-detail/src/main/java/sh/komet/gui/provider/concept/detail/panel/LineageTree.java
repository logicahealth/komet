package sh.komet.gui.provider.concept.detail.panel;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import sh.isaac.api.Get;
import sh.isaac.api.Edge;
import sh.isaac.api.TaxonomySnapshot;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;

public class LineageTree {
    public static TreeView<String> makeLineageTree(ConceptChronology chronology, Manifold manifold) {
        throw new UnsupportedOperationException();
//        TaxonomySnapshot snapshot = FxGet.graphSnapshot(FxGet.defaultViewKey());
//        TreeView<String> treeView = new TreeView<>();
//        TreeItem<String> root = new TreeItem<>(manifold.getPreferredDescriptionText(chronology));
//        addChildren(root, chronology, snapshot, manifold);
//
//        treeView.setRoot(root);
//        return treeView;
    }

    private static void addChildren(TreeItem<String> parentTreeItem, ConceptChronology parentConcept, TaxonomySnapshot snapshot, Manifold manifold) {
        for (Edge edge : snapshot.getTaxonomyParentLinks(parentConcept.getNid())) {
            if (edge.getTypeNid() == TermAux.IS_A.getNid()) {
                ConceptChronology childConcept = Get.concept(edge.getDestinationNid());
                TreeItem<String> childItem = new TreeItem<>(manifold.getPreferredDescriptionText(childConcept));
                parentTreeItem.getChildren().add(childItem);
                addChildren(childItem, childConcept, snapshot, manifold);
            }

        }
    }

}
