/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */
package sh.isaac.model.taxonomy;

import static sh.isaac.api.commit.StampService.FIRST_STAMP_SEQUENCE;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.coordinate.*;
import sh.isaac.api.navigation.NavigationRecord;
import sh.isaac.api.navigation.TypeStampNavigationRecord;
import sh.isaac.api.navigation.TypeStampNavigationRecords;
import sh.isaac.api.snapshot.calculator.RelativePositionCalculator;


import java.util.function.IntFunction;

//~--- JDK imports ------------------------------------------------------------
//~--- non-JDK imports --------------------------------------------------------

//~--- classes ----------------------------------------------------------------
/**
 * For each concept nid (a key in the map), there is a list of
 * type-stamp-flags. These type-stamp-flags compact into a primitive long value.
 *
 *
 * A {@code TaxonomyRecord} is the value for a map where the key is the concept
 * sequence for a concept in the taxonomy, and the value is a map to other
 * concept nids, and the associated stamps and taxonomy flags for these
 * other concept nids. From the stamp value and the taxonomy flags, all
 * historic taxonomic associations (parent, child, stated, and inferred) can be
 * computed.
 *
 *
 * origin concept nid [1 -> n] {destination concept nid [1 -> n] stamp
 * + inferred + stated + parent + child}
 * <p>
 * <p>
 * Created by kec on 11/8/14.
 */
public class TaxonomyRecord implements NavigationRecord {
    static void validate(int[] record) {
        if (record == null || record.length == 0) {
            return;
        }
        int subRecordStart = 0;

        while (subRecordStart < record.length) {
            if (record[subRecordStart] >= 0) {
                throw new IllegalStateException("record[subRecordStart] must be < 0. Found: "
                        + record[subRecordStart] + " at index: " + subRecordStart);
            }
            int subrecordLength = record[subRecordStart + 1];
            if (subrecordLength < 4 || subRecordStart + subrecordLength > record.length) {
                throw new IllegalStateException("Illegal length. Found: "
                        + subrecordLength);
            }
            for (int i = 1; i < subrecordLength; i += 3) {
                int typeNid = record[subRecordStart + i + 1]; // one for conceptNid at the beginning
                int stamp = record[subRecordStart + i + 2];
                //int flags = record[subRecordStart + i + 3];
                if (typeNid >= 0) {
                    throw new IllegalStateException("typeNid must be < 0. Found: "
                            + typeNid + " at index: " + (subRecordStart + i + 1));
                }
                if (stamp < FIRST_STAMP_SEQUENCE) {
                    throw new IllegalStateException("stamp must be >= "
                            + FIRST_STAMP_SEQUENCE + ". Found: "
                            + stamp + " at index: " + (subRecordStart + i + 2));
                }
                // TODO test for valid taxonomy flags.
            }
            subRecordStart = subRecordStart + subrecordLength + 1;
        }

    }

    /**
     * key = origin concept nid; value = TypeStampTaxonomyRecords.
     */
    private final IntObjectHashMap<TypeStampTaxonomyRecords> conceptNidRecordMap
            = new IntObjectHashMap<>(11);

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new taxonomy record unpacked.
     */
    public TaxonomyRecord() {
    }

    /**
     * Instantiates a new taxonomy record unpacked.
     *
     * @param recordArray the record array
     */
    public TaxonomyRecord(int[] recordArray) {
        //validate(recordArray);
  
        if (recordArray != null) {
            int index = 0;

            while (index < recordArray.length) {
                // the destination nid
                final int conceptNid = recordArray[index++];
                // followed by a variable number of type, stamp, flag records
                final int length = recordArray[index];
                final TypeStampTaxonomyRecords records = new TypeStampTaxonomyRecords(recordArray, index);
                this.conceptNidRecordMap.put(conceptNid, records);

                index += length;
                if (index < 0) {
                    throw new IllegalStateException("Index: " + index);
                }
            }
        }
    }

    //~--- methods -------------------------------------------------------------
    public void addConceptSequenceStampRecords(int conceptNid, TypeStampNavigationRecords newRecord) {
        if (this.conceptNidRecordMap.containsKey(conceptNid)) {
            final TypeStampTaxonomyRecords oldRecord = this.conceptNidRecordMap.get(conceptNid);

            oldRecord.merge(newRecord);
        } else {
            this.conceptNidRecordMap.put(conceptNid, (TypeStampTaxonomyRecords) newRecord);
        }
    }

    public void addStampRecord(int destinationNid, int typeNid, int stamp, int recordFlags) {

        TypeStampTaxonomyRecords conceptSequenceStampRecordsUnpacked;
        if (this.conceptNidRecordMap.containsKey(destinationNid)) {
            conceptSequenceStampRecordsUnpacked = this.conceptNidRecordMap.get(destinationNid);
        } else {
            conceptSequenceStampRecordsUnpacked = new TypeStampTaxonomyRecords();
            this.conceptNidRecordMap.put(destinationNid, conceptSequenceStampRecordsUnpacked);
        }

        conceptSequenceStampRecordsUnpacked.addStampRecord(typeNid, stamp, recordFlags);
    }

    @Override
    public boolean conceptSatisfiesStamp(int conceptNid, StampFilterImmutable stampFilterImmutable) {
        final RelativePositionCalculator computer = RelativePositionCalculator.getCalculator(stampFilterImmutable);
        if (this.conceptNidRecordMap.containsKey(conceptNid)) {
            return this.conceptNidRecordMap.get(conceptNid)
                    .containsConceptNidViaTypeWithAllowedStatus(conceptNid, new int[] {TaxonomyFlag.CONCEPT_STATUS.bits}, computer);
        }

        return false;
    }

    @Override
    public EnumSet<Status> getConceptStates(int conceptNid, StampFilterImmutable stampFilterImmutable) {
        final RelativePositionCalculator computer = RelativePositionCalculator.getCalculator(stampFilterImmutable);
        if (this.conceptNidRecordMap.containsKey(conceptNid)) {
            return this.conceptNidRecordMap.get(conceptNid)
                    .getConceptStates(conceptNid, new int[] {TaxonomyFlag.CONCEPT_STATUS.bits}, computer);
        }
        return EnumSet.noneOf(Status.class);
    }

    @Override
    public int connectionCount() {
        return this.conceptNidRecordMap.size();
    }

    @Override
    public boolean containsConceptNidViaType(int conceptNid, NidSet typeSequenceSet, int[] flags) {
        if (this.conceptNidRecordMap.containsKey(conceptNid)) {
            return this.conceptNidRecordMap.get(conceptNid)
                    .isPresent(typeSequenceSet, flags);
        }

        return false;
    }

    @Override
    public boolean containsConceptNidViaType(int conceptNid,
                                             NidSet typeSequenceSet,
                                             ManifoldCoordinate mc) {

        if (this.conceptNidRecordMap.containsKey(conceptNid) &&
                Get.concept(conceptNid).getLatestVersion(mc.getVertexStampFilter()).isPresent()) {
            return this.conceptNidRecordMap.get(conceptNid)
                    .containsConceptNidViaTypeWithAllowedStatus(typeSequenceSet, mc, RelativePositionCalculator.getCalculator(mc.getViewStampFilter().toStampFilterImmutable()));
        }

        return false;
    }

    @Override
    public boolean containsConceptNidViaType(int conceptNid, int typeSequence, ManifoldCoordinate mc) {

        if (this.conceptNidRecordMap.containsKey(conceptNid) &&
                Get.concept(conceptNid).getLatestVersion(mc.getVertexStampFilter()).isPresent()) {
            return this.conceptNidRecordMap.get(conceptNid)
                    .containsConceptNidViaTypeWithAllowedStatus(typeSequence, mc, RelativePositionCalculator.getCalculator(mc.getViewStampFilter().toStampFilterImmutable()));
        }

        return false;
    }

    @Override
    public boolean containsConceptNidViaType(int conceptNid,
                                             NidSet typeSequenceSet,
                                             ManifoldCoordinate mc,
                                             int[] flags) {

        if (this.conceptNidRecordMap.containsKey(conceptNid) &&
                Get.concept(conceptNid).getLatestVersion(mc.getVertexStampFilter()).isPresent()) {
            return this.conceptNidRecordMap.get(conceptNid)
                    .containsConceptNidViaTypeWithAllowedStatus(typeSequenceSet, flags, RelativePositionCalculator.getCalculator(mc.getViewStampFilter().toStampFilterImmutable()));
        }
        return false;
    }

    @Override
    public boolean containsConceptNidViaType(int conceptNid,
                                             int typeNid,
                                             ManifoldCoordinate mc,
                                             int[] flags) {

        if (this.conceptNidRecordMap.containsKey(conceptNid) &&
                Get.concept(conceptNid).getLatestVersion(mc.getVertexStampFilter()).isPresent()) {
            return this.conceptNidRecordMap.get(conceptNid)
                    .containsConceptNidViaTypeWithAllowedStatus(typeNid, flags, RelativePositionCalculator.getCalculator(mc.getViewStampFilter().toStampFilterImmutable()));
        }

        return false;
    }

    @Override
    public boolean containsNidViaTypeWithFlags(int conceptNid, int typeNid, int[] flags) {
        if (this.conceptNidRecordMap.containsKey(conceptNid)) {
            return this.conceptNidRecordMap.get(conceptNid)
                    .containsStampOfTypeWithFlags(typeNid, flags);
        }

        return false;
    }

    /**
     * Equals.
     *
     * @param obj the obj
     * @return true, if successful
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final TaxonomyRecord other = (TaxonomyRecord) obj;

        return Objects.equals(this.conceptNidRecordMap, other.conceptNidRecordMap);
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("May change values, can't put in a tree or set");
    }

    @Override
    public int length() {
        int length = 0;

        for (TypeStampTaxonomyRecords value : this.conceptNidRecordMap.values())
        {
            length += value.length();
        }
        return length + this.conceptNidRecordMap.size();
    }

    public void merge(NavigationRecord newRecord) {
        TaxonomyRecord newTaxonomyRecord = (TaxonomyRecord) newRecord;
        newTaxonomyRecord.conceptNidRecordMap.forEachKeyValue((int key,
                TypeStampTaxonomyRecords value) -> {
            if (this.conceptNidRecordMap.containsKey(key)) {
                this.conceptNidRecordMap.get(key)
                        .merge(value);
            } else {
                this.conceptNidRecordMap.put(key, value);
            }
        });
    }

    public int[] pack() {
        int[] keys = this.conceptNidRecordMap.keySet().toArray();
        Arrays.sort(keys);
        int[] taxonomyRecordArray = new int[length()];
        int destinationPosition = 0;
        for (int key: keys) {
            taxonomyRecordArray[destinationPosition++] = key;
            TypeStampTaxonomyRecords records = this.conceptNidRecordMap.get(key);
            records.addToIntArray(taxonomyRecordArray, destinationPosition);
            destinationPosition += records.length();
        }
        return taxonomyRecordArray;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        int[] theKeys = this.conceptNidRecordMap.keySet().toArray();
        Arrays.sort(theKeys);

        final StringBuilder buf = new StringBuilder();

        buf.append("[");

        final int maxIndex = theKeys.length - 1;

        for (int i = 0; i <= maxIndex; i++) {
            final int conceptNid = theKeys[i];

            if (i > 0) {
                buf.append("\n   ");
            }

            buf.append(Get.conceptDescriptionText(conceptNid));
            buf.append(" <");
            buf.append(conceptNid);
            buf.append("> <-");

            final TypeStampTaxonomyRecords records = this.conceptNidRecordMap.get(conceptNid);

            for (TypeStampNavigationRecord record : records.values()) {
                buf.append("\n      ");
                buf.append(record.toString());
            }
        }

        buf.append(']');
        return buf.toString();
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public Optional<? extends TypeStampTaxonomyRecords> getConceptNidStampRecords(int conceptNid) {
        return Optional.ofNullable(this.conceptNidRecordMap.get(conceptNid));
    }

    @Override
    public boolean containsStampOfTypeWithFlags(int typeNid, int[] flags) {
        for (TypeStampTaxonomyRecords record: this.conceptNidRecordMap.values()) {
            if (record.containsStampOfTypeWithFlags(typeNid, flags)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int[] getConceptNidsForType(int typeNid) {
        MutableIntSet conceptSequencesForTypeSet = IntSets.mutable.empty();
        this.conceptNidRecordMap.forEachKeyValue((int possibleParentSequence,
                TypeStampTaxonomyRecords stampRecords) -> {
            final MutableIntSet stampsForConceptSet = IntSets.mutable.empty();

            stampRecords.forEach((record) -> {
                if (typeNid == Integer.MAX_VALUE) {
                    stampsForConceptSet.add(record.getStampSequence());
                } else if (typeNid == record.getTypeNid()) {
                    stampsForConceptSet.add(record.getStampSequence());
                }
            });
            conceptSequencesForTypeSet.add(possibleParentSequence);
        });
        MutableIntList conceptSequencesForTypeList = conceptSequencesForTypeSet.toList();
        conceptSequencesForTypeList.sortThis();
        return conceptSequencesForTypeList.toArray();
    }

    @Override
    public int[] getConceptNidsForType(int typeSequence, ManifoldCoordinate manifoldCoordinate, IntFunction<int[]> taxonomyDataProvider) {
        final int flags[] = manifoldCoordinate.getPremiseTypes().getFlags();
        final RelativePositionCalculator edgeComputer = manifoldCoordinate.getViewStampFilter().getRelativePositionCalculator();
        final RelativePositionCalculator vertexComputer = manifoldCoordinate.getVertexStampFilter().getRelativePositionCalculator();
        return getConceptNidsForType(typeSequence, taxonomyDataProvider, flags, edgeComputer,
                vertexComputer, manifoldCoordinate.getVertexSort(), manifoldCoordinate.toManifoldCoordinateImmutable());
    }

    @Override
    public int[] getConceptNidsForType(int typeSequence, IntFunction<int[]> taxonomyDataProvider, int[] flags,
                                       RelativePositionCalculator edgeComputer,
                                       RelativePositionCalculator vertexComputer,
                                       VertexSort sort,
                                       ManifoldCoordinateImmutable digraph) {
        final MutableIntSet conceptNidsForTypeSet = IntSets.mutable.empty();

        for (int flag: flags) {
            this.conceptNidRecordMap.forEachKeyValue((int possibleParentNid,
                                                      TypeStampTaxonomyRecords stampRecords) -> {
                final MutableIntSet stampsForConceptIntStream = IntSets.mutable.empty();

                stampRecords.forEach((record) -> {
                    // collect the stamps associated with a particular type of relationship, so we can
                    // later determine if the relationship is active
                    if ((record.getTaxonomyFlags() & flag) == flag) {
                        if (typeSequence == Integer.MAX_VALUE || typeSequence == record.getTypeNid()) {
                            stampsForConceptIntStream.add(record.getStampSequence());
                        }
                    }
                });

                if (edgeComputer.isLatestActive(stampsForConceptIntStream.toArray())) {
                    // relationship of type is active per at least one relationship,
                    // now see if the vertex (destination concept) meets other criterion.
                    // if the optional destination stamp coordinate is present, we need to filter and only return
                    // the concept nids that meet the criterion of this destination stamp coordinate.
                    TaxonomyRecordPrimitive targetConceptRecord = new TaxonomyRecordPrimitive(taxonomyDataProvider.apply(possibleParentNid));
                    if (targetConceptRecord.conceptSatisfiesFilter(possibleParentNid, vertexComputer)) {
                        conceptNidsForTypeSet.add(possibleParentNid);
                    }
                }
            });
        }
        return sort.sortVertexes(conceptNidsForTypeSet.toArray(), digraph);
    }

    @Override
    public IntStream getDestinationConceptSequences() {
        final IntStream.Builder conceptSequenceIntStream = IntStream.builder();

        this.conceptNidRecordMap.forEachKeyValue((int destinationSequence,
                TypeStampTaxonomyRecords stampRecords) -> {
            conceptSequenceIntStream.accept(destinationSequence);
        });
        return conceptSequenceIntStream.build();
    }

    @Override
    public int[] getDestinationConceptNidsNotOfType(NidSet typeSet, ManifoldCoordinate mc) {
        final int[] flags = mc.getPremiseTypes().getFlags();
        final RelativePositionCalculator edgeComputer = mc.getViewStampFilter().getRelativePositionCalculator();
        final MutableIntSet conceptNidIntSet = IntSets.mutable.empty();
        for (int flag: flags) {
            this.conceptNidRecordMap.forEachKeyValue((int destinationConceptNid,
                                                      TypeStampTaxonomyRecords stampRecords) -> {
                final MutableIntSet stampsForConceptIntStream = IntSets.mutable.empty();

                stampRecords.forEach((record) -> {
                    if ((record.getTaxonomyFlags() & flag) == flag) {
                        if (edgeComputer.onRoute(record.getStampSequence())) {
                            if (typeSet.isEmpty()) {
                                stampsForConceptIntStream.add(record.getStampSequence());
                            } else if (!typeSet.contains(record.getTypeNid())) {
                                stampsForConceptIntStream.add(record.getStampSequence());
                            }
                        }
                    }
                });

                if (edgeComputer.isLatestActive(stampsForConceptIntStream.toArray()) &&
                        Get.concept(destinationConceptNid).getLatestVersion(mc.getVertexStampFilter()).isPresent()) {
                    conceptNidIntSet.add(destinationConceptNid);
                }
            });
        }

        return mc.sortVertexes(conceptNidIntSet.toArray());
    }

    @Override
    public IntStream getDestinationConceptNidsOfType(NidSet typeSet) {
        final IntStream.Builder conceptSequenceIntStream = IntStream.builder();

        this.conceptNidRecordMap.forEachKeyValue((int destinationSequence,
                TypeStampTaxonomyRecords stampRecords) -> {
            stampRecords.forEach((typeStampFlag) -> {
                if (typeSet.contains(typeStampFlag.getTypeNid())) {
                    conceptSequenceIntStream.accept(destinationSequence);
                }
            });
        });
        return conceptSequenceIntStream.build();
    }

    @Override
    public int[] getDestinationConceptNidsOfType(NidSet typeSet, ManifoldCoordinate mc) {
        final int[] flags = mc.getPremiseTypes().getFlags();
        final RelativePositionCalculator edgeComputer = mc.getViewStampFilter().getRelativePositionCalculator();
        final MutableIntSet conceptNidIntSet = IntSets.mutable.empty();
        for (int flag: flags) {

            this.conceptNidRecordMap.forEachKeyValue((int destinationConceptNid,
                                                      TypeStampTaxonomyRecords stampRecords) -> {
                final MutableIntSet stampsForConceptIntSet = IntSets.mutable.empty();

                stampRecords.forEach((record) -> {
                    if ((record.getTaxonomyFlags() & flag) == flag) {
                        if (edgeComputer.onRoute(record.getStampSequence())) {
                            if (typeSet.isEmpty()) {
                                stampsForConceptIntSet.add(record.getStampSequence());
                            } else if (typeSet.contains(record.getTypeNid())) {
                                stampsForConceptIntSet.add(record.getStampSequence());
                            }
                        }
                    }
                });

                if (edgeComputer.isLatestActive(stampsForConceptIntSet.toArray()) &&
                        Get.conceptService().getConceptChronology(destinationConceptNid).getLatestVersion(
                                mc.getVertexStampFilter()).isPresent()) {
                    conceptNidIntSet.add(destinationConceptNid);
                }
            });
        }

        return mc.sortVertexes(conceptNidIntSet.toArray());
    }

    @Override
    public boolean hasDestinationConceptNidsOfType(NidSet typeSet, ManifoldCoordinate mc) {
        final int flags[] = mc.getPremiseTypes().getFlags();
        final RelativePositionCalculator edgeComputer = mc.getViewStampFilter().getRelativePositionCalculator();
        final AtomicBoolean found = new AtomicBoolean(false);
        for (int flag: flags) {
            this.conceptNidRecordMap.forEachKeyValue((int destinationConceptNid,
                                                      TypeStampTaxonomyRecords stampRecords) -> {
                if (found.get() == false) {
                    final MutableIntSet stampsForConceptIntSet = IntSets.mutable.empty();

                    stampRecords.forEach((record) -> {
                        if ((record.getTaxonomyFlags() & flag) == flag) {
                            if (edgeComputer.onRoute(record.getStampSequence())) {
                                if (typeSet.isEmpty()) {
                                    stampsForConceptIntSet.add(record.getStampSequence());
                                } else if (typeSet.contains(record.getTypeNid())) {
                                    stampsForConceptIntSet.add(record.getStampSequence());
                                }
                            }
                        }
                    });

                    if (edgeComputer.isLatestActive(stampsForConceptIntSet.toArray()) &&
                            Get.conceptService().getConceptChronology(destinationConceptNid).getLatestVersion(
                                    mc.getVertexStampFilter()).isPresent()) {
                        found.set(true);
                    }
                }
            });
        }

        return found.get();
    }

    @Override
    public IntStream getParentConceptSequences() {
        final int isaNid = TermAux.IS_A.getNid();
        final IntStream.Builder conceptNidIntStream = IntStream.builder();

        this.conceptNidRecordMap.forEachKeyValue((int possibleParentNid,
                TypeStampTaxonomyRecords stampRecords) -> {
            stampRecords.forEach((typeStampFlag) -> {
                if (typeStampFlag.getTypeNid() == isaNid) {
                    conceptNidIntStream.accept(possibleParentNid);
                }
            });
        });
        return conceptNidIntStream.build();
    }

    @Override
    public int[] getTypesForRelationship(int destinationId, ManifoldCoordinate mc) {
        final int[] flags = mc.getPremiseTypes().getFlags();
        final RelativePositionCalculator edgeComputer = mc.getViewStampFilter().getRelativePositionCalculator();
        final MutableIntSet typeSequenceIntSet = IntSets.mutable.empty();

        if (Get.concept(destinationId).getLatestVersion(mc.getVertexStampFilter()).isAbsent()) {
           //destinations that aren't available can't have a rel
           return new int[0];
        }
        for (int flag: flags) {
            this.conceptNidRecordMap.forEachKeyValue((int destinationNid,
                                                      TypeStampTaxonomyRecords stampRecords) -> {
                if (destinationId == destinationNid) {
                    final Map<Integer, MutableIntSet> typeStampStreamMap = new HashMap<>();

                    stampRecords.forEach((record) -> {
                        if ((record.getTaxonomyFlags() & flag) == flag) {
                            if (edgeComputer.onRoute(record.getStampSequence())) {
                                if (!typeStampStreamMap.containsKey(record.getTypeNid())) {
                                    typeStampStreamMap.put(record.getTypeNid(), IntSets.mutable.empty());
                                }

                                typeStampStreamMap.get((record.getTypeNid()))
                                        .add(record.getStampSequence());
                            }
                        }
                    });
                    typeStampStreamMap.forEach((type, stampStreamSet) -> {
                        if (edgeComputer.isLatestActive(stampStreamSet.toArray())) {
                            typeSequenceIntSet.add(type);
                        }
                    });
                }
            });
        }

        return typeSequenceIntSet.toSortedArray();
    }
}
