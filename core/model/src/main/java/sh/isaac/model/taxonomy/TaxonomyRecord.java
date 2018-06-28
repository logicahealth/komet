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

//~--- JDK imports ------------------------------------------------------------
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

//~--- non-JDK imports --------------------------------------------------------
import org.apache.mahout.math.function.IntObjectProcedure;
import org.apache.mahout.math.list.IntArrayList;
import org.apache.mahout.math.map.OpenIntObjectHashMap;
import org.apache.mahout.math.set.OpenIntHashSet;

import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.collections.NidSet;
import static sh.isaac.api.commit.StampService.FIRST_STAMP_SEQUENCE;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.snapshot.calculator.RelativePositionCalculator;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.model.ModelGet;

//~--- classes ----------------------------------------------------------------
/**
 * For each concept sequence (a key in the map), there is a list of
 * type-stamp-flags. These type-stamp-flags compact into a primitive long value.
 *
 *
 * A {@code TaxonomyRecord} is the value for a map where the key is the concept
 * sequence for a concept in the taxonomy, and the value is a map to other
 * concept sequences, and the associated stamps and taxonomy flags for these
 * other concept sequences. From the stamp value and the taxonomy flags, all
 * historic taxonomic associations (parent, child, stated, and inferred) can be
 * computed.
 *
 *
 * origin concept sequence [1 -> n] {destination concept sequence [1 -> n] stamp
 * + inferred + stated + parent + child}
 * <p>
 * <p>
 * Created by kec on 11/8/14.
 */
public class TaxonomyRecord {

    /**
     * key = origin concept sequence; value = TypeStampTaxonomyRecords.
     */
    private final OpenIntObjectHashMap<TypeStampTaxonomyRecords> conceptNidRecordMap
            = new OpenIntObjectHashMap<>(11);

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

    public static void validate(int[] record) {
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
                int flags = record[subRecordStart + i + 3];
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

    //~--- methods -------------------------------------------------------------
    /**
     * Adds the concept sequence stamp records.
     *
     * @param conceptNid the concept nid
     * @param newRecord the new record
     */
    public void addConceptSequenceStampRecords(int conceptNid, TypeStampTaxonomyRecords newRecord) {
        if (this.conceptNidRecordMap.containsKey(conceptNid)) {
            final TypeStampTaxonomyRecords oldRecord = this.conceptNidRecordMap.get(conceptNid);

            oldRecord.merge(newRecord);
        } else {
            this.conceptNidRecordMap.put(conceptNid, newRecord);
        }
    }

    /**
     * Adds the stamp record.
     *
     * @param destinationNid the destination sequence
     * @param typeNid the type sequence
     * @param stamp the stamp
     * @param recordFlags the record flags
     */
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

    /**
     * Concept satisfies stamp.
     *
     * @param conceptNid the concept sequence
     * @param stampCoordinate the stamp coordinate
     * @return true, if successful
     */
    public boolean conceptSatisfiesStamp(int conceptNid, StampCoordinate stampCoordinate) {
        final RelativePositionCalculator computer = RelativePositionCalculator.getCalculator(stampCoordinate);
        if (this.conceptNidRecordMap.containsKey(conceptNid)) {
            return this.conceptNidRecordMap.get(conceptNid)
                    .containsConceptNidViaType(conceptNid, TaxonomyFlag.CONCEPT_STATUS.bits, computer);
        }

        return false;
    }

    public EnumSet<Status> getConceptStates(int conceptNid, StampCoordinate stampCoordinate) {
        final RelativePositionCalculator computer = RelativePositionCalculator.getCalculator(stampCoordinate);
        if (this.conceptNidRecordMap.containsKey(conceptNid)) {
            return this.conceptNidRecordMap.get(conceptNid)
                    .getConceptStates(conceptNid, TaxonomyFlag.CONCEPT_STATUS.bits, computer);
        }
        return EnumSet.noneOf(Status.class);
    }

    /**
     * Conection count.
     *
     * @return the int
     */
    public int conectionCount() {
        return this.conceptNidRecordMap.size();
    }

    /**
     * Contains concept sequence via type.
     *
     * @param conceptNid the concept sequence
     * @param typeSequenceSet the type sequence set
     * @param flags the flags
     * @return true, if successful
     */
    public boolean containsConceptNidViaType(int conceptNid, NidSet typeSequenceSet, int flags) {
        if (this.conceptNidRecordMap.containsKey(conceptNid)) {
            return this.conceptNidRecordMap.get(conceptNid)
                    .isPresent(typeSequenceSet, flags);
        }

        return false;
    }

    /**
     * Contains concept sequence via type.
     *
     * @param conceptNid the concept sequence
     * @param typeSequenceSet the type sequence set
     * @param tc the tc
     * @return true, if successful
     */
    public boolean containsConceptNidViaType(int conceptNid,
            NidSet typeSequenceSet,
            ManifoldCoordinate tc) {
        final RelativePositionCalculator computer = RelativePositionCalculator.getCalculator(tc.getStampCoordinate());

        if (this.conceptNidRecordMap.containsKey(conceptNid)) {
            return this.conceptNidRecordMap.get(conceptNid)
                    .containsConceptNidViaType(typeSequenceSet, tc, computer);
        }

        return false;
    }

    /**
     * Contains concept sequence via type.
     *
     * @param conceptNid the concept sequence
     * @param typeSequence the type sequence
     * @param tc the tc
     * @return true, if successful
     */
    public boolean containsConceptNidViaType(int conceptNid, int typeSequence, ManifoldCoordinate tc) {
        final RelativePositionCalculator computer = RelativePositionCalculator.getCalculator(tc.getStampCoordinate());

        if (this.conceptNidRecordMap.containsKey(conceptNid)) {
            return this.conceptNidRecordMap.get(conceptNid)
                    .containsConceptNidViaType(typeSequence, tc, computer);
        }

        return false;
    }

    /**
     * Contains concept sequence via type.
     *
     * @param conceptNid the concept sequence
     * @param typeSequenceSet the type sequence set
     * @param tc the tc
     * @param flags the flags
     * @return true, if successful
     */
    public boolean containsConceptNidViaType(int conceptNid,
            NidSet typeSequenceSet,
            ManifoldCoordinate tc,
            int flags) {
        final RelativePositionCalculator computer = RelativePositionCalculator.getCalculator(tc.getStampCoordinate());

        if (this.conceptNidRecordMap.containsKey(conceptNid)) {
            return this.conceptNidRecordMap.get(conceptNid)
                    .containsConceptNidViaType(typeSequenceSet, flags, computer);
        }

        return false;
    }

    /**
     * Contains concept sequence via type.
     *
     * @param conceptNid the concept sequence
     * @param typeNid the type sequence
     * @param tc the tc
     * @param flags the flags
     * @return true, if successful
     */
    public boolean containsConceptNidViaType(int conceptNid,
            int typeNid,
            ManifoldCoordinate tc,
            int flags) {
        final RelativePositionCalculator computer = RelativePositionCalculator.getCalculator(tc.getStampCoordinate());

        if (this.conceptNidRecordMap.containsKey(conceptNid)) {
            return this.conceptNidRecordMap.get(conceptNid)
                    .containsConceptNidViaType(typeNid, flags, computer);
        }

        return false;
    }

    /**
     * Contains sequence via type with flags.
     *
     * @param conceptNid the concept sequence
     * @param typeNid the type sequence
     * @param flags the flags
     * @return true, if successful
     */
    public boolean containsNidViaTypeWithFlags(int conceptNid, int typeNid, int flags) {
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

    /**
     * Length.
     *
     * @return the int
     */
    public int length() {
        int length = 0;

        length = this.conceptNidRecordMap.values()
                .stream()
                .map((record) -> record.length())
                .reduce(length, Integer::sum);
        return length + this.conceptNidRecordMap.size();
    }

    /**
     * Merge.
     *
     * @param newRecord the new record
     */
    public void merge(TaxonomyRecord newRecord) {
        newRecord.conceptNidRecordMap.forEachPair((int key,
                TypeStampTaxonomyRecords value) -> {
            if (this.conceptNidRecordMap.containsKey(key)) {
                this.conceptNidRecordMap.get(key)
                        .merge(value);
            } else {
                this.conceptNidRecordMap.put(key, value);
            }

            return true;
        });
    }

    /**
     * Pack.
     *
     * @return the int[]
     */
    public int[] pack() {
        final PackConceptSequenceStampRecords packer = new PackConceptSequenceStampRecords();

        this.conceptNidRecordMap.forEachPair(packer);
        //validate(packer.taxonomyRecordArray);
        return packer.taxonomyRecordArray;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        final IntArrayList theKeys = this.conceptNidRecordMap.keys();

        theKeys.sort();

        final StringBuilder buf = new StringBuilder();

        buf.append("[");

        final int maxIndex = theKeys.size() - 1;

        for (int i = 0; i <= maxIndex; i++) {
            final int conceptNid = theKeys.get(i);

            if (i > 0) {
                buf.append("\n   ");
            }

            buf.append(Get.conceptDescriptionText(conceptNid));
            buf.append(" <");
            buf.append(conceptNid);
            buf.append("> <-");

            final TypeStampTaxonomyRecords records = this.conceptNidRecordMap.get(conceptNid);

            for (TypeStampTaxonomyRecord record : records.values()) {
                buf.append("\n      ");
                buf.append(record.toString());
            }
        }

        buf.append(']');
        return buf.toString();
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the concept sequence stamp records.
     *
     * @param conceptNid the concept sequence
     * @return the concept sequence stamp records
     */
    public Optional<TypeStampTaxonomyRecords> getConceptNidStampRecords(int conceptNid) {
        return Optional.ofNullable(this.conceptNidRecordMap.get(conceptNid));
    }

    /**
     * Gets the concept sequences for type.
     *
     * @param typeNid typeNid to match, or Integer.MAX_VALUE if a wildcard.
     * @return active concepts identified by their sequence value.
     */
    public int[] getConceptNidsForType(int typeNid) {
        OpenIntHashSet conceptSequencesForTypeSet = new OpenIntHashSet();
        this.conceptNidRecordMap.forEachPair((int possibleParentSequence,
                TypeStampTaxonomyRecords stampRecords) -> {
            final OpenIntHashSet stampsForConceptSet = new OpenIntHashSet();

            stampRecords.forEach((record) -> {
                if (typeNid == Integer.MAX_VALUE) {
                    stampsForConceptSet.add(record.getStampSequence());
                } else if (typeNid == record.getTypeNid()) {
                    stampsForConceptSet.add(record.getStampSequence());
                }
            });
            conceptSequencesForTypeSet.add(possibleParentSequence);
            return true;
        });
        IntArrayList conceptSequencesForTypeList = conceptSequencesForTypeSet.keys();
        conceptSequencesForTypeList.sort();
        return conceptSequencesForTypeList.elements();
    }

    /**
     * Gets the concept sequences for type.
     *
     * @param typeSequence typeNid to match, or Integer.MAX_VALUE if a wildcard.
     * @param tc used to determine if a concept is active.
     * @return active concepts identified by their sequence value.
     */
    public int[] getConceptNidsForType(int typeSequence, ManifoldCoordinate tc) {
        final int flags = TaxonomyFlag.getFlagsFromManifoldCoordinate(tc);
        final RelativePositionCalculator computer = RelativePositionCalculator.getCalculator(tc);
        final OpenIntHashSet conceptSequencesForTypeSet = new OpenIntHashSet();

        this.conceptNidRecordMap.forEachPair((int possibleParentSequence,
                TypeStampTaxonomyRecords stampRecords) -> {
            final OpenIntHashSet stampsForConceptIntStream = new OpenIntHashSet();

            stampRecords.forEach((record) -> {
                if ((record.getTaxonomyFlags() & flags) == flags) {
                    if (typeSequence == Integer.MAX_VALUE) {
                        stampsForConceptIntStream.add(record.getStampSequence());
                    } else if (typeSequence == record.getTypeNid()) {
                        stampsForConceptIntStream.add(record.getStampSequence());
                    }
                }
            });

            if (computer.isLatestActive(stampsForConceptIntStream.keys().elements())) {
                conceptSequencesForTypeSet.add(possibleParentSequence);
            }

            return true;
        });
        IntArrayList conceptSequencesForTypeList = conceptSequencesForTypeSet.keys();
        conceptSequencesForTypeList.sort();
        return conceptSequencesForTypeList.elements();
    }

    /**
     * Gets the destination concept sequences.
     *
     * @return the destination concept sequences
     */
    public IntStream getDestinationConceptSequences() {
        final IntStream.Builder conceptSequenceIntStream = IntStream.builder();

        this.conceptNidRecordMap.forEachPair((int destinationSequence,
                TypeStampTaxonomyRecords stampRecords) -> {
            conceptSequenceIntStream.accept(destinationSequence);
            return true;
        });
        return conceptSequenceIntStream.build();
    }

    /**
     * Gets the destination concept sequences not of type.
     *
     * @param typeSet the type set
     * @param tc the tc
     * @return the destination concept sequences not of type
     */
    public int[] getDestinationConceptNidsNotOfType(NidSet typeSet, ManifoldCoordinate tc) {
        final int flags = TaxonomyFlag.getFlagsFromManifoldCoordinate(tc);
        final RelativePositionCalculator computer = RelativePositionCalculator.getCalculator(tc.getStampCoordinate());
        final OpenIntHashSet conceptSequenceIntSet = new OpenIntHashSet();

        this.conceptNidRecordMap.forEachPair((int destinationSequence,
                TypeStampTaxonomyRecords stampRecords) -> {
            final OpenIntHashSet stampsForConceptIntStream = new OpenIntHashSet();

            stampRecords.forEach((record) -> {
                if ((record.getTaxonomyFlags() & flags) == flags) {
                    if (computer.onRoute(record.getStampSequence())) {
                        if (typeSet.isEmpty()) {
                            stampsForConceptIntStream.add(record.getStampSequence());
                        } else if (!typeSet.contains(record.getTypeNid())) {
                            stampsForConceptIntStream.add(record.getStampSequence());
                        }
                    }
                }
            });

            if (computer.isLatestActive(stampsForConceptIntStream.keys().elements())) {
                conceptSequenceIntSet.add(destinationSequence);
            }

            return true;
        });
        IntArrayList setAsList = conceptSequenceIntSet.keys();
        setAsList.sort();
        return setAsList.elements();
    }

    /**
     * Gets the destination concept sequences of type.
     *
     * @param typeSet the type set
     * @return the destination concept sequences of type
     */
    public IntStream getDestinationConceptNidsOfType(NidSet typeSet) {
        final IntStream.Builder conceptSequenceIntStream = IntStream.builder();

        this.conceptNidRecordMap.forEachPair((int destinationSequence,
                TypeStampTaxonomyRecords stampRecords) -> {
            stampRecords.forEach((typeStampFlag) -> {
                if (typeSet.contains(typeStampFlag.typeNid)) {
                    conceptSequenceIntStream.accept(destinationSequence);
                }
            });
            return true;
        });
        return conceptSequenceIntStream.build();
    }

    /**
     * Gets the destination concept sequences of type.
     *
     * @param typeSet the type set
     * @param tc the tc
     * @return the destination concept sequences of type
     */
    public int[] getDestinationConceptNidsOfType(NidSet typeSet, ManifoldCoordinate tc) {
        final int flags = TaxonomyFlag.getFlagsFromManifoldCoordinate(tc);
        final RelativePositionCalculator computer = RelativePositionCalculator.getCalculator(tc.getStampCoordinate());
        final OpenIntHashSet conceptSequenceIntSet = new OpenIntHashSet();

        this.conceptNidRecordMap.forEachPair((int destinationSequence,
                TypeStampTaxonomyRecords stampRecords) -> {
            final OpenIntHashSet stampsForConceptIntSet = new OpenIntHashSet();

            stampRecords.forEach((record) -> {
                if ((record.getTaxonomyFlags() & flags) == flags) {
                    if (computer.onRoute(record.getStampSequence())) {
                        if (typeSet.isEmpty()) {
                            stampsForConceptIntSet.add(record.getStampSequence());
                        } else if (typeSet.contains(record.getTypeNid())) {
                            stampsForConceptIntSet.add(record.getStampSequence());
                        }
                    }
                }
            });

            if (computer.isLatestActive(stampsForConceptIntSet.keys().elements())) {
                conceptSequenceIntSet.add(destinationSequence);
            }

            return true;
        });
        IntArrayList conceptSequenceList = conceptSequenceIntSet.keys();
        conceptSequenceList.sort();
        return conceptSequenceList.elements();
    }

    /**
     * Gets the destination concept sequences of type.
     *
     * @param typeSet the type set
     * @param tc the tc
     * @return the destination concept sequences of type
     */
    public boolean hasDestinationConceptNidsOfType(NidSet typeSet, ManifoldCoordinate tc) {
        final int flags = TaxonomyFlag.getFlagsFromManifoldCoordinate(tc);
        final RelativePositionCalculator computer = RelativePositionCalculator.getCalculator(tc.getStampCoordinate());
        final AtomicBoolean found = new AtomicBoolean(false);

        this.conceptNidRecordMap.forEachPair((int destinationSequence,
                TypeStampTaxonomyRecords stampRecords) -> {
            final OpenIntHashSet stampsForConceptIntSet = new OpenIntHashSet();

            stampRecords.forEach((record) -> {
                if ((record.getTaxonomyFlags() & flags) == flags) {
                    if (computer.onRoute(record.getStampSequence())) {
                        if (typeSet.isEmpty()) {
                            stampsForConceptIntSet.add(record.getStampSequence());
                        } else if (typeSet.contains(record.getTypeNid())) {
                            stampsForConceptIntSet.add(record.getStampSequence());
                        }
                    }
                }
            });

            if (computer.isLatestActive(stampsForConceptIntSet.keys().elements())) {
                found.set(true);
                return false;
            }

            return true;
        });
        return found.get();
    }

    /**
     * Gets the parent concept sequences.
     *
     * @return the parent concept sequences
     */
    public IntStream getParentConceptSequences() {
        final int isaNid = TermAux.IS_A.getNid();
        final IntStream.Builder conceptNidIntStream = IntStream.builder();

        this.conceptNidRecordMap.forEachPair((int possibleParentNid,
                TypeStampTaxonomyRecords stampRecords) -> {
            stampRecords.forEach((typeStampFlag) -> {
                if (typeStampFlag.typeNid == isaNid) {
                    conceptNidIntStream.accept(possibleParentNid);
                }
            });
            return true;
        });
        return conceptNidIntStream.build();
    }

    /**
     * Gets the types for relationship.
     *
     * @param destinationId the destination id
     * @param tc the tc
     * @return the types for relationship
     */
    int[] getTypesForRelationship(int destinationId, ManifoldCoordinate tc) {
        final int flags = TaxonomyFlag.getFlagsFromManifoldCoordinate(tc);
        final RelativePositionCalculator computer = RelativePositionCalculator.getCalculator(tc.getStampCoordinate());
        final OpenIntHashSet typeSequenceIntSet = new OpenIntHashSet();

        this.conceptNidRecordMap.forEachPair((int destinationNid,
                TypeStampTaxonomyRecords stampRecords) -> {
            if (destinationId == destinationNid) {
                final Map<Integer, OpenIntHashSet> typeStampStreamMap = new HashMap<>();

                stampRecords.forEach((record) -> {
                    if ((record.getTaxonomyFlags() & flags) == flags) {
                        if (computer.onRoute(record.getStampSequence())) {
                            if (!typeStampStreamMap.containsKey(record.typeNid)) {
                                typeStampStreamMap.put(record.typeNid, new OpenIntHashSet());
                            }

                            typeStampStreamMap.get((record.typeNid))
                                    .add(record.getStampSequence());
                        }
                    }
                });
                typeStampStreamMap.forEach((type, stampStreamSet) -> {
                    if (computer.isLatestActive(stampStreamSet.keys().elements())) {
                        typeSequenceIntSet.add(type);
                    }
                });
            }

            return true;
        });
        IntArrayList typeSequenceList = typeSequenceIntSet.keys();
        typeSequenceList.sort();
        return typeSequenceList.elements();
    }

    //~--- inner classes -------------------------------------------------------
    /**
     * The Class PackConceptSequenceStampRecords.
     */
    private class PackConceptSequenceStampRecords
            implements IntObjectProcedure<TypeStampTaxonomyRecords> {

        /**
         * The taxonomy record array.
         */
        int[] taxonomyRecordArray = new int[length()];

        /**
         * The destination position.
         */
        int destinationPosition = 0;

        //~--- methods ----------------------------------------------------------
        /**
         * Apply.
         *
         * @param conceptNid the concept sequence
         * @param stampRecordsUnpacked the stamp records unpacked
         * @return true, if successful
         */
        @Override
        public boolean apply(int conceptSequence, TypeStampTaxonomyRecords stampRecordsUnpacked) {
            this.taxonomyRecordArray[this.destinationPosition++] = conceptSequence;
            stampRecordsUnpacked.addToIntArray(this.taxonomyRecordArray, this.destinationPosition);
            this.destinationPosition += stampRecordsUnpacked.length();
            return true;
        }
    }
}
