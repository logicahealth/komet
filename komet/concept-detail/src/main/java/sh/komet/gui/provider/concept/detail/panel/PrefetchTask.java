package sh.komet.gui.provider.concept.detail.panel;

import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMapUnsafe;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.task.TimedTask;

import java.time.Duration;
import java.util.Arrays;

public class PrefetchTask extends TimedTask<ConcurrentHashMapUnsafe<Integer, Chronology>> {
    private final int conceptNid;
    private final ConcurrentHashMapUnsafe<Integer, Chronology> componentMap = ConcurrentHashMapUnsafe.newMap();

    public PrefetchTask(int conceptNid) {
        this.conceptNid = conceptNid;
    }

    public PrefetchTask(Duration progressUpdateDuration, int conceptNid) {
        super(progressUpdateDuration);
        this.conceptNid = conceptNid;
    }

    @Override
    protected ConcurrentHashMapUnsafe<Integer, Chronology> call() throws Exception {
        componentMap.put(conceptNid, Get.concept(conceptNid));


        ImmutableIntSet semanticNidsForComponent = Get.assemblageService().getSemanticNidsForComponent(conceptNid);
        MutableIntSet recursiveSemanticNids = IntSets.mutable.ofAll(semanticNidsForComponent);
        semanticNidsForComponent.forEach(semanticNid -> addRecursiveSequences(recursiveSemanticNids, semanticNid));

        // Trying to retrieve the semantics in parallel on other threads.
        Arrays.stream(recursiveSemanticNids.toArray()).parallel().forEach(semanticNid -> {
            componentMap.getIfAbsentPut(semanticNid, () -> Get.identifiedObjectService().getChronology(semanticNid).get());
        });
        return componentMap;
    }

    private void addRecursiveSequences(MutableIntSet recursiveSemanticNids, int semanticNid) {
        ImmutableIntSet semanticNidsForComponent = Get.assemblageService().getSemanticNidsForComponent(semanticNid);
        recursiveSemanticNids.addAll(semanticNidsForComponent);
        semanticNidsForComponent.forEach((sequence) -> {
            addRecursiveSequences(recursiveSemanticNids, sequence);
        });
    }

    public Chronology getChronology(int nid) {
        return componentMap.getIfAbsentPut(nid, () -> Get.identifiedObjectService().getChronology(nid).get());
    }
}
