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
import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Consumer;

//~--- non-JDK imports --------------------------------------------------------
import org.apache.mahout.math.list.IntArrayList;
import sh.isaac.api.Get;
import sh.isaac.api.Status;

import sh.isaac.api.collections.NidSet;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.TaxonomyFlag;
import sh.isaac.api.navigation.TypeStampNavigationRecord;
import sh.isaac.api.navigation.TypeStampNavigationRecords;
import sh.isaac.api.snapshot.calculator.RelativePositionCalculator;

//~--- classes ----------------------------------------------------------------
/**
 * This class contains a set of records, where each record is represented as a
 * primitive long value. These records associates stamps and
 * {@code TaxonomyFlag}s. The type of a relationship between objects--such as an
 * is-a relationship expressed as the nid of the is-a concept--is associated
 * with a stamp, and the taxonomy flags (stated, inferred, and others) that
 * qualify that relationship at the coordinates in status, time, path, and
 * module specified by the stamp.
 *
 * @author kec
 */
public class TypeStampTaxonomyRecords implements TypeStampNavigationRecords {
    /*
     * 
    OpenLongObjectHashMap has a default size of 277 elements, and this was creating memory pressure. 
    Trying to go with a TreeMap to see if this reduces memmory pressure since it only
    uses what is acutally needed by the number of elements. 
    private final OpenLongObjectHashMap<TypeStampTaxonomyRecord> typeStamp_flag_map = new OpenLongObjectHashMap<>();
     */
   private final TreeMap<Long,TypeStampTaxonomyRecord> typeStamp_flag_map = new TreeMap<>();

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new type stamp taxonomy records.
     */
    public TypeStampTaxonomyRecords() {
    }

    /**
     * Instantiates a new type stamp taxonomy records.
     *
     * @param sourceArray the source array
     * @param sourceStartPosition the source position
     */
    public TypeStampTaxonomyRecords(int[] sourceArray, int sourceStartPosition) {
        final int length = sourceArray[sourceStartPosition];
        final int recordEnd = sourceStartPosition + length;
        sourceStartPosition++;
        while (sourceStartPosition < recordEnd) {

            TypeStampTaxonomyRecord newRecord = new TypeStampTaxonomyRecord(
                    sourceArray[sourceStartPosition++],
                    sourceArray[sourceStartPosition++],
                    sourceArray[sourceStartPosition++]);
            addNewRecord(newRecord);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeStampTaxonomyRecords that = (TypeStampTaxonomyRecords) o;
        return Objects.equals(typeStamp_flag_map, that.typeStamp_flag_map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeStamp_flag_map);
    }

    @Override
    public Collection<? extends TypeStampNavigationRecord> values() {
        return typeStamp_flag_map.values();
    }
    
    @Override
    public int[] toArray() {
        int[] valueArray = new int[this.typeStamp_flag_map.size() * 3];
        int i = 0;
        for (TypeStampTaxonomyRecord record: this.typeStamp_flag_map.values()) {
            valueArray[i++] = record.getTypeNid();
            valueArray[i++] = record.getStampSequence();
            valueArray[i++] = record.getTaxonomyFlags();
        }
        return valueArray;
    }

    /**
     * Instantiates a new type stamp taxonomy records.
     *
     * @param typeNid the type nid
     * @param stampSequence the stamp sequence
     * @param flag the flag
     */
    public TypeStampTaxonomyRecords(int typeNid, int stampSequence, TaxonomyFlag flag) {
        TypeStampTaxonomyRecord newRecord = new TypeStampTaxonomyRecord(typeNid, stampSequence, flag.bits);
        typeStamp_flag_map.put(newRecord.getTypeStampKey(), newRecord);
    }

    //~--- methods -------------------------------------------------------------
    public void addNewRecord(TypeStampNavigationRecord newRecord) {
        long typeStampKey = newRecord.getTypeStampKey();
        if (typeStamp_flag_map.containsKey(typeStampKey)) {
            if (!typeStamp_flag_map.get(typeStampKey).merge(newRecord)) {
                throw new IllegalStateException("Merge failed for: \n    "
                        + this + "\n    " + newRecord);
            }
        } else {
            typeStamp_flag_map.put(typeStampKey, (TypeStampTaxonomyRecord) newRecord);
        }
    }

    public void addStampRecord(int typeNid, int stampSequence, int taxonomyFlags) {
        addNewRecord(new TypeStampTaxonomyRecord(typeNid, stampSequence, taxonomyFlags));
    }

    public void addToIntArray(int[] destinationArray, int destinationPosition) {
        destinationArray[destinationPosition++] = length();
        for (TypeStampTaxonomyRecord record : this.typeStamp_flag_map.values()) {
            destinationArray[destinationPosition++] = record.typeNid;
            destinationArray[destinationPosition++] = record.stamp;
            destinationArray[destinationPosition++] = record.taxonomyFlagBits;
        }
    }

    @Override
    public boolean containsConceptNidViaTypeWithAllowedStatus(int typeNid, int[] flags, RelativePositionCalculator computer) {
        final int[] latestStamps = computer.getLatestStampSequencesAsSet(getStampsOfTypeWithFlags(typeNid, flags));

        return latestStamps.length > 0;
    }
    @Override
    public int[] latestStampsForConceptNidViaTypeWithAllowedStatus(int typeNid, int[] flags, RelativePositionCalculator computer) {
        return computer.getLatestStampSequencesAsSet(getStampsOfTypeWithFlags(typeNid, flags));
    }

    @Override
    public EnumSet<Status> getConceptStates(int typeNid, int[] flags, RelativePositionCalculator computer) {
        final int[] latestStamps = computer.getLatestStampSequencesAsSet(getStampsOfTypeWithFlags(typeNid, flags));
        EnumSet<Status> statusSet = EnumSet.noneOf(Status.class);
        for (int stamp : latestStamps) {
            statusSet.add(Get.stampService().getStatusForStamp(stamp));
        }
        return statusSet;
    }

    @Override
    public boolean containsConceptNidViaTypeWithAllowedStatus(int typeNid, ManifoldCoordinate tc, RelativePositionCalculator computer) {
        final int[] flags = tc.getPremiseTypes().getFlags();

        return TypeStampTaxonomyRecords.this.containsConceptNidViaTypeWithAllowedStatus(typeNid, flags, computer);
    }

    @Override
    public boolean containsConceptNidViaTypeWithAllowedStatus(NidSet typeNidSet, int[] flags, RelativePositionCalculator computer) {

        final int[] latestStamps = computer.getLatestStampSequencesAsSet(
                getStampsOfTypeWithFlags(typeNidSet, flags));

        return latestStamps.length > 0;
    }

    @Override
    public boolean containsConceptNidViaTypeWithAllowedStatus(NidSet typeNidSet,
                                                              ManifoldCoordinate tc,
                                                              RelativePositionCalculator computer) {
        final int[] flags = tc.getPremiseTypes().getFlags();

        return TypeStampTaxonomyRecords.this.containsConceptNidViaTypeWithAllowedStatus(typeNidSet, flags, computer);
    }

    @Override
    public boolean containsStampOfTypeWithFlags(int typeNid, int[] flags) {
        for (int flag: flags) {
            for (TypeStampTaxonomyRecord record : this.typeStamp_flag_map.values()) {
                if (typeNid == Integer.MAX_VALUE) {  // wildcard
                    if (flag == 0) {                 // taxonomy flag wildcard--inferred, stated, non-defining, ...
                        return true;                   // finish search
                    } else if ((flag & record.taxonomyFlagBits) == flag) {
                        return true;                   // finish search.
                    }
                } else if (record.getTypeNid() == typeNid) {
                    if (flag == 0) {                 // taxonomy flag wildcard--inferred, stated, non-defining, ...
                        return true;                   // finish search
                    } else if ((flag & record.taxonomyFlagBits) == flag) {
                        return true;                   // finish search.
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean containsStampOfTypeWithFlags(NidSet typeNidSet, int[] flags) {
        for (int flag: flags) {
            for (TypeStampTaxonomyRecord record : this.typeStamp_flag_map.values()) {
                if (typeNidSet.isEmpty()) {  // wildcard
                    if ((flag & record.taxonomyFlagBits) == flag) {
                        return true;
                    }
                } else if (typeNidSet.contains(record.typeNid)) {
                    if ((flag & record.taxonomyFlagBits) == flag) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void forEach(Consumer<? super TypeStampNavigationRecord> procedure) {
        this.typeStamp_flag_map.values().forEach(procedure);
    }

    @Override
    public int length() {
        // 1 is for the length of the int[] used to represent these records
        // this.typeStampFlagsSet.size() * 3 is the size of the associated stampNid records.
        return 1 + (this.typeStamp_flag_map.size() * 3);
    }

    public void merge(TypeStampNavigationRecords newRecords) {
        TypeStampTaxonomyRecords newTaxonomyRecords = (TypeStampTaxonomyRecords) newRecords;
        try {
            for (TypeStampNavigationRecord newRecord : newTaxonomyRecords.typeStamp_flag_map.values()) {
                long typeStampKey = newRecord.getTypeStampKey();
                if (typeStamp_flag_map.containsKey(typeStampKey)) {
                    if (!typeStamp_flag_map.get(typeStampKey).merge(newRecord)) {
                        throw new IllegalStateException("Merge failed for: \n    "
                                + this + "\n    " + newRecord);
                    }
                } else {
                    typeStamp_flag_map.put(typeStampKey, (TypeStampTaxonomyRecord) newRecord);
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        for (TypeStampTaxonomyRecord record : typeStamp_flag_map.values()) {
            sb.append(record.toString());
        }
        return sb.toString();
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public boolean isPresent(NidSet typeNidSet, int[] flags) {
        return containsStampOfTypeWithFlags(typeNidSet, flags);
    }

    @Override
    public int[] getStampsOfTypeWithFlags(int typeNid, int[] flags) {
        final IntArrayList stampList = new IntArrayList();
        for (int flag: flags) {
            for (TypeStampTaxonomyRecord record : typeStamp_flag_map.values()) {
                if (typeNid == Integer.MAX_VALUE) {  // wildcard
                    if ((flag & record.taxonomyFlagBits) == flag) {
                        stampList.add(record.stamp);
                    }
                } else if (record.typeNid == typeNid) {
                    if ((flag & record.taxonomyFlagBits) == flag) {
                        stampList.add(record.stamp);
                    }
                }
            }
        }
        stampList.trimToSize();
        return stampList.elements();
    }

    @Override
    public int[] getStampsOfTypeWithFlags(NidSet typeNidSet, int[] flags) {
        final IntArrayList stampList = new IntArrayList();
        for (int flag: flags) {
            for (TypeStampTaxonomyRecord record : typeStamp_flag_map.values()) {
                if (typeNidSet.isEmpty()) {  // wildcard
                    if (record.getTaxonomyFlags() == flag) {
                        stampList.add(record.getTypeNid());
                    }
                } else if (typeNidSet.contains(record.typeNid)) {
                    if ((flag & record.taxonomyFlagBits) == flag) {
                        stampList.add(record.getTypeNid());
                    }
                }
            }
        }

        stampList.trimToSize();
        return stampList.elements();
    }

}
