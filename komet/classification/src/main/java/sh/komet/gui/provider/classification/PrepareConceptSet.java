package sh.komet.gui.provider.classification;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import org.apache.shiro.util.CollectionUtils;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import sh.isaac.api.Get;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.NaturalOrder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.IntStream;

public class PrepareConceptSet extends TimedTaskWithProgressTracker<Void> {


    private final IntStream affectedConcepts;
    private final ObservableList<Integer> affectedConceptsForDisplay;

    public PrepareConceptSet(String title, Set<Integer> affectedConcepts,
                             ObservableList<Integer> affectedConceptsForDisplay) {
        this.affectedConcepts = affectedConcepts.parallelStream().mapToInt(value -> value);
        this.affectedConceptsForDisplay = affectedConceptsForDisplay;
        this.updateTitle(title);
        this.addToTotalWork(affectedConcepts.size());
        Get.activeTasks().add(this);
        Platform.runLater(() -> {
            affectedConceptsForDisplay.setAll(affectedConcepts);
        });
    }

    @Override
    protected Void call() throws Exception {
        try {

            ConcurrentSkipListSet<Integer> concurrentSortedSet = new ConcurrentSkipListSet<>(this::compare);

            this.affectedConcepts.parallel().forEach(nid -> {
                concurrentSortedSet.add(nid);
                this.completedUnitOfWork();
            });
            Platform.runLater(() -> {
                this.affectedConceptsForDisplay.setAll(concurrentSortedSet);
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
