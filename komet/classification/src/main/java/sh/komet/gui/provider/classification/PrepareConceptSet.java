package sh.komet.gui.provider.classification;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import org.apache.shiro.util.CollectionUtils;
import sh.isaac.api.Get;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.NaturalOrder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class PrepareConceptSet extends TimedTaskWithProgressTracker<Void> {


    private final Set<Integer> affectedConcepts;
    private final ObservableList<Integer> affectedConceptsForDisplay;

    public PrepareConceptSet(String title, Set<Integer> affectedConcepts,
                             ObservableList<Integer> affectedConceptsForDisplay) {
        this.affectedConcepts = affectedConcepts;
        this.affectedConceptsForDisplay = affectedConceptsForDisplay;
        this.updateTitle(title);
        this.addToTotalWork(affectedConcepts.size());
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() throws Exception {
        try {
            TreeSet<Integer> sortedSet = new TreeSet<>(this::compare);
            for (Integer nid: affectedConcepts) {
                sortedSet.add(nid);
                this.completedUnitOfWork();
            }
            Platform.runLater(() -> {
                affectedConceptsForDisplay.setAll(sortedSet);
            });
            return null;
        } finally {
            Get.activeTasks().remove(this);
        }
    }

    private int compare(Integer o1, Integer o2) {

        return NaturalOrder.compareStrings(Get.conceptDescriptionText(o1), Get.conceptDescriptionText(o2));
    }
}
