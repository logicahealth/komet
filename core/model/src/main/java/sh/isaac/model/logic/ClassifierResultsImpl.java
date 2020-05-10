package sh.isaac.model.logic;

import org.apache.mahout.math.list.IntArrayList;
import sh.isaac.api.Get;
import sh.isaac.api.classifier.ClassifierResults;
import sh.isaac.api.collections.IntArrayWrapper;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.coordinate.*;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;

import java.time.Instant;
import java.util.*;

public class ClassifierResultsImpl implements ClassifierResults {

    /**
     * Set of concepts potentially affected by the last classification.
     */
    private final Set<Integer> classificationConceptSet;

    private final Set<Integer> conceptsWithInferredChanges = new HashSet<>();

    /** The equivalent sets. */
    private final Set<int[]> equivalentSets;

    /** The commit record. */
    private final Optional<CommitRecord> commitRecord;

    //A map of a concept nid, to a HashSet of int arrays, where each int[] is a cycle present on the concept.
    private Optional<Map<Integer, Set<int[]>>> conceptsWithCycles = Optional.empty();

    private HashSet<Integer> orphanedConcepts = new HashSet<>();

    private StampFilterImmutable stampFilter;

    private LogicCoordinateImmutable logicCoordinate;

    private EditCoordinateImmutable editCoordinate;

    private ClassifierResultsImpl(ByteArrayDataBuffer data) {
        this.classificationConceptSet = new HashSet<>();
        for (int nid: data.getNidArray()) {
            classificationConceptSet.add(nid);
        }
        int equivalentSetSize = data.getInt();
        equivalentSets = new HashSet<>(equivalentSetSize);
        for (int i = 0; i < equivalentSetSize; i++) {
            equivalentSets.add(data.getNidArray());
        }
        cleanUpEquivalentSets();
        if (data.getBoolean()) {
            this.commitRecord = Optional.of(CommitRecord.make(data));
            convertToConceptsWithInferredChanges();
        } else {
            this.commitRecord = Optional.empty();
        }
        if (data.getBoolean()) {
            Map<Integer, Set<int[]>> cycleMap = new HashMap<>();
            int cycleMapSize = data.getInt();
            for (int i = 0; i < cycleMapSize; i++) {
                int key = data.getInt();
                int setCount = data.getInt();
                Set<int[]> cycleSets = new TreeSet<>();
                for (int j = 0; j < setCount; j++) {
                    cycleSets.add(data.getNidArray());
                }
                cycleMap.put(key, cycleSets);
            }
            this.conceptsWithCycles = Optional.of(cycleMap);
        } else {
            this.conceptsWithCycles = Optional.empty();
        }
        for (int orphanNid: data.getNidArray()) {
            orphanedConcepts.add(orphanNid);
        }
        this.stampFilter = StampFilterImmutable.make(data);
        this.logicCoordinate = LogicCoordinateImmutable.make(data);
        this.editCoordinate = EditCoordinateImmutable.make(data);
    }

    private void convertToConceptsWithInferredChanges() {
        for (int semanticNid: this.commitRecord.get().getSemanticNidsInCommit().asArray()) {
            this.conceptsWithInferredChanges.add(
                    Get.assemblageService().getSemanticChronology(semanticNid).getReferencedComponentNid());
        }
    }

    private final void cleanUpEquivalentSets() {
        HashSet<IntArrayWrapper> cleanEquivalentSets = new HashSet<>();
        for (int[] set: equivalentSets) {
            cleanEquivalentSets.add(new IntArrayWrapper(set));
        }
        equivalentSets.clear();
        for (IntArrayWrapper wrapper: cleanEquivalentSets) {
            equivalentSets.add(wrapper.getWrappedSet());
        }

    }

    public final void putExternal(ByteArrayDataBuffer out) {
        out.putNidArray(this.classificationConceptSet);
        out.putInt(equivalentSets.size());
        for (int[] equivalentSet: equivalentSets) {
            out.putNidArray(equivalentSet);
        }
        if (commitRecord.isPresent()) {
            out.putBoolean(true);
            this.commitRecord.get().putExternal(out);
        } else {
            out.putBoolean(false);
        }

        if (conceptsWithCycles.isPresent()) {
            out.putBoolean(true);
            Map<Integer, Set<int[]>> cycles = conceptsWithCycles.get();
            out.putInt(cycles.size());
            for (Map.Entry<Integer, Set<int[]>> entry: cycles.entrySet()) {
                out.putNid(entry.getKey());
                out.putInt(entry.getValue().size());
                for (int[] cycle: entry.getValue()) {
                    out.putNidArray(cycle);
                }
            }
        } else {
            out.putBoolean(false);
        }
        out.putNidArray(orphanedConcepts);
        this.stampFilter.marshal(out);
        this.logicCoordinate.toLogicCoordinateImmutable().marshal(out);
        this.editCoordinate.marshal(out);

    }

    public static final ClassifierResultsImpl make(ByteArrayDataBuffer data) {
        return new ClassifierResultsImpl(data);
    }

    /**
     * Instantiates a new classifier results.
     *  @param classificationConceptSet the affected concepts
     * @param equivalentSets the equivalent sets
     * @param commitRecord the commit record
     * @param stampFilter
     */
    public ClassifierResultsImpl(Set<Integer> classificationConceptSet,
                                 Set<IntArrayList> equivalentSets,
                                 Optional<CommitRecord> commitRecord,
                                 StampFilter stampFilter,
                                 LogicCoordinate logicCoordinate,
                                 EditCoordinate editCoordinate) {
        this.classificationConceptSet = classificationConceptSet;
        this.equivalentSets = new HashSet<>();
        for (IntArrayList set: equivalentSets) {
            set.sort();
            this.equivalentSets.add(set.elements());
        }
        cleanUpEquivalentSets();
        this.commitRecord     = commitRecord;
        if (this.commitRecord.isPresent()) {
            convertToConceptsWithInferredChanges();
        }
        assignCoordinates(stampFilter, logicCoordinate, editCoordinate);
    }

    /**
     * This constructor is only intended to be used when a classification wasn't performed, because there were cycles present.
     * @param conceptsWithCycles
     * @param orphans
     * @param stampFilter
     */
    public ClassifierResultsImpl(Map<Integer, Set<int[]>> conceptsWithCycles, Set<Integer> orphans,
                                 StampFilter stampFilter,
                                 LogicCoordinate logicCoordinate,
                                 EditCoordinate editCoordinate) {
        this.classificationConceptSet = new HashSet<>();
        this.equivalentSets   = new HashSet<>();
        this.commitRecord     = Optional.empty();
        this.conceptsWithCycles = Optional.of(conceptsWithCycles);
        this.orphanedConcepts.addAll(orphans);

        assignCoordinates(stampFilter, logicCoordinate, editCoordinate);
    }

    private final void assignCoordinates(StampFilter stampFilter, LogicCoordinate logicCoordinate,
                                         EditCoordinate editCoordinate) {
        if (stampFilter.getStampPosition().getTime() == Long.MAX_VALUE) {
            throw new IllegalStateException("Filter position time must reflect the actual commit time, not 'latest' (Long.MAX_VALUE) ");
        }
        if (editCoordinate == null) {
            throw new NullPointerException("Edit coordinate cannot be null. ");
        }

        this.stampFilter = stampFilter.toStampFilterImmutable();
        this.logicCoordinate  = logicCoordinate.toLogicCoordinateImmutable();
        this.editCoordinate = editCoordinate.toEditCoordinateImmutable();


        if (stampFilter.getStampPosition().getTime() == Long.MAX_VALUE) {
            throw new IllegalStateException("Filter position time must reflect the actual commit time, not 'latest' (Long.MAX_VALUE) ");
        }

    }

    @Override
    public String toString() {
        return "ClassifierResults{" + "written semantics: "
                + (this.commitRecord.isPresent() && this.commitRecord.get().getSemanticNidsInCommit() != null ? this.commitRecord.get().getSemanticNidsInCommit().size(): "0")
                + " affectedConcepts=" + this.classificationConceptSet.size() + ", equivalentSets="
                + this.equivalentSets.size() + ", Orphans detected=" + orphanedConcepts.size()
                + " Concepts with cycles=" + (conceptsWithCycles.isPresent() ? conceptsWithCycles.get().size() : 0) + '}';
    }

    @Override
    public Set<Integer> getClassificationConceptSet() {
        return this.classificationConceptSet;
    }

    @Override
    public Optional<CommitRecord> getCommitRecord() {
        return this.commitRecord;
    }

    @Override
    public Set<int[]> getEquivalentSets() {
        return this.equivalentSets;
    }

    @Override
    public Optional<Map<Integer, Set<int[]>>> getCycles() {
        return conceptsWithCycles;
    }

    @Override
    public void addOrphans(Set<Integer> orphans) {
        orphanedConcepts.addAll(orphans);
    }

    @Override
    public Set<Integer> getOrphans() {
        return orphanedConcepts;
    }

    @Override
    public StampFilter getStampFilter() {
        return stampFilter;
    }

    @Override
    public LogicCoordinate getLogicCoordinate() {
        return logicCoordinate;
    }

    @Override
    public EditCoordinate getEditCoordinate() {
        return this.editCoordinate;
    }

    @Override
    public Instant getCommitTime() {
        return this.stampFilter.getStampPosition().getTimeAsInstant();
    }

    @Override
    public Set<Integer> getConceptsWithInferredChanges() {
        return conceptsWithInferredChanges;
    }


}
