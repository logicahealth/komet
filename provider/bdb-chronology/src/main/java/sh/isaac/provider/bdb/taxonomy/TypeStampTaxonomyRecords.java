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



package sh.isaac.provider.bdb.taxonomy;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashSet;
import java.util.function.Consumer;
import java.util.stream.Stream;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.list.IntArrayList;

import sh.isaac.api.collections.NidSet;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.snapshot.calculator.RelativePositionCalculator;

//~--- classes ----------------------------------------------------------------

/**
 * This class contains a set of records, where each record is represented as a primitive long value.
 * These records associates stamps and {@code TaxonomyFlag}s. The type of a relationship between
 * objects--such as an is-a relationship
 * expressed as the nid of the is-a concept--is associated with a stamp, and the
 * taxonomy flags (stated, inferred, and others) that qualify that relationship at the coordinates
 * in status, time, path, and module specified by the stamp.
 *
 * @author kec
 */
public class TypeStampTaxonomyRecords {
   private final HashSet<TypeStampTaxonomyRecord> typeStampFlagsSet = new HashSet<>();

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new type stamp taxonomy records.
    */
   public TypeStampTaxonomyRecords() {}

   /**
    * Instantiates a new type stamp taxonomy records.
    *
    * @param sourceArray the source array
    * @param sourceStartPosition the source position
    */
   public TypeStampTaxonomyRecords(int[] sourceArray, int sourceStartPosition) {
      final int length    = sourceArray[sourceStartPosition];
      final int recordEnd = sourceStartPosition + length;
      sourceStartPosition++;
      while (sourceStartPosition < recordEnd) {
         this.typeStampFlagsSet.add(new TypeStampTaxonomyRecord(
                                              sourceArray[sourceStartPosition++],
                                              sourceArray[sourceStartPosition++],
                                              sourceArray[sourceStartPosition++]));
      }
   }

   /**
    * Instantiates a new type stamp taxonomy records.
    *
    * @param typeNid the type nid
    * @param stampSequence the stamp sequence
    * @param flag the flag
    */
   public TypeStampTaxonomyRecords(int typeNid, int stampSequence, TaxonomyFlag flag) {
      this.typeStampFlagsSet.add(new TypeStampTaxonomyRecord(typeNid, stampSequence, flag.bits));
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the stamp record.
    *
    * @param typeNid the type nid
    * @param stampSequence the stamp sequence
    * @param taxonomyFlags the taxonomy flags
    */
   public void addStampRecord(int typeNid, int stampSequence, int taxonomyFlags) {
      this.typeStampFlagsSet.add(new TypeStampTaxonomyRecord(typeNid, stampSequence, taxonomyFlags));
   }

   /**
    * Adds the to int array.
    *
    * @param destinationArray the destination array
    * @param destinationPosition the destination position
    */
   public void addToIntArray(int[] destinationArray, int destinationPosition) {
      destinationArray[destinationPosition++] = length();
      for (TypeStampTaxonomyRecord record: this.typeStampFlagsSet) {
         destinationArray[destinationPosition++] = record.typeNid;
         destinationArray[destinationPosition++] = record.stampSequence;
         destinationArray[destinationPosition++] = record.taxonomyFlags;
      }
   }

   /**
    * Contains concept nid via type.
    *
    * @param typeNid the type nid
    * @param flags the flags
    * @param computer the computer
    * @return true, if successful
    */
   public boolean containsConceptNidViaType(int typeNid, int flags, RelativePositionCalculator computer) {
      final int[] latestStamps = computer.getLatestStampSequencesAsSet(getStampsOfTypeWithFlags(typeNid, flags));

      return latestStamps.length > 0;
   }

   /**
    * Contains concept nid via type.
    *
    * @param typeNid the type nid
    * @param tc the tc
    * @param computer the computer
    * @return true, if successful
    */
   public boolean containsConceptNidViaType(int typeNid, ManifoldCoordinate tc, RelativePositionCalculator computer) {
      final int flags = TaxonomyFlag.getFlagsFromManifoldCoordinate(tc);

      return TypeStampTaxonomyRecords.this.containsConceptNidViaType(typeNid, flags, computer);
   }

   /**
    * Contains concept nid via type.
    *
    * @param typeNidSet the type nid set
    * @param flags the flags
    * @param computer the computer
    * @return true, if successful
    */
   public boolean containsConceptNidViaType(NidSet typeNidSet, int flags, RelativePositionCalculator computer) {

      final int[] latestStamps = computer.getLatestStampSequencesAsSet(
                                     getStampsOfTypeWithFlags(typeNidSet, flags));

      return latestStamps.length > 0;
   }

   /**
    * Contains concept nid via type.
    *
    * @param typeNidSet the type nid set
    * @param tc the tc
    * @param computer the computer
    * @return true, if successful
    */
   public boolean containsConceptNidViaType(NidSet typeNidSet,
         ManifoldCoordinate tc,
         RelativePositionCalculator computer) {
      final int flags = TaxonomyFlag.getFlagsFromManifoldCoordinate(tc);

      return TypeStampTaxonomyRecords.this.containsConceptNidViaType(typeNidSet, flags, computer);
   }

   /**
    * Contains stamp of type with flags.
    *
    * @param typeNid Integer.MAX_VALUE is a wildcard and will match all types.
    * @param flags the flags
    * @return true if found.
    */
   public boolean containsStampOfTypeWithFlags(int typeNid, int flags) {
      for (TypeStampTaxonomyRecord record: this.typeStampFlagsSet) {
         if (typeNid == Integer.MAX_VALUE) {  // wildcard
            if (flags == 0) {                 // taxonomy flag wildcard--inferred, stated, non-defining, ...
               return true;                   // finish search
            } else if (record.taxonomyFlags == flags) {
               return true;                   // finish search.
            }
         } else if (record.typeNid == typeNid) {
            if (flags == 0) {                 // taxonomy flag wildcard--inferred, stated, non-defining, ...
               return true;                   // finish search
            } else if (record.taxonomyFlags == flags) {
               return true;                   // finish search.
            }
         }
      }

      return false;
   }

   /**
    * Contains stamp of type with flags.
    *
    * @param typeNidSet An empty set is a wildcard and will match all types.
    * @param flags the flags
    * @return true if found.
    */
   public boolean containsStampOfTypeWithFlags(NidSet typeNidSet, int flags) {
      for (TypeStampTaxonomyRecord record: this.typeStampFlagsSet) {
         if (typeNidSet.isEmpty()) {  // wildcard
            if (record.taxonomyFlags == flags) {
               return true;
            }
         } else if (typeNidSet.contains(record.typeNid)) {
            if (record.taxonomyFlags == flags) {
               return true;
            }
         }
      }

      return false;
   }

   /**
    * Convert to long.
    *
    * @param typeNid the type nid
    * @param stampSequence the stamp sequence
    * @param taxonomyFlags the taxonomy flags
    * @return the long
    */
   public static int[] convertToArray(int typeNid, int stampSequence, int taxonomyFlags) {
      return new int[] { typeNid, stampSequence, taxonomyFlags };
   }

   public void forEach(Consumer<? super TypeStampTaxonomyRecord> procedure) {
      this.typeStampFlagsSet.forEach(procedure);
   }

   /**
    * Length.
    *
    * @return the number of integers this stampNid record will occupy when
    * packed.
    */
   public int length() {
      // 1 is for the length of the int[] used to represent these records
      // this.typeStampFlagsSet.size() * 3 is the size of the associated stampNid records.
      return 1 + (this.typeStampFlagsSet.size() * 3);
   }

   /**
    * Merge.
    *
    * @param newRecords the new records
    */
   public void merge(TypeStampTaxonomyRecords newRecords) {
      newRecords.typeStampFlagsSet.forEach(
          (TypeStampTaxonomyRecord recordAsLong) -> this.typeStampFlagsSet.add(recordAsLong));
   }

   /**
    * Stream.
    *
    * @return the stream
    */
   public Stream<TypeStampTaxonomyRecord> stream() {
      final Stream.Builder<TypeStampTaxonomyRecord> builder = Stream.builder();

      this.typeStampFlagsSet.forEach((TypeStampTaxonomyRecord record) -> builder.accept(record));
      return builder.build();
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      final StringBuilder sb = new StringBuilder();

      this.typeStampFlagsSet.forEach((TypeStampTaxonomyRecord record) -> sb.append(record.toString()));
      return sb.toString();
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Checks if present.
    *
    * @param typeNidSet the type nid set
    * @param flags the flags
    * @return true, if present
    */
   public boolean isPresent(NidSet typeNidSet, int flags) {
      return containsStampOfTypeWithFlags(typeNidSet, flags);
   }

   /**
    * Gets the stamps of type with flags.
    *
    * @param typeNid the type nid
    * @param flags the flags
    * @return the stamps of type with flags
    */
   public int[] getStampsOfTypeWithFlags(int typeNid, int flags) {
      final IntArrayList intStreamBuilder = new IntArrayList();

      this.typeStampFlagsSet.forEach(
          (TypeStampTaxonomyRecord record) -> {
             if (typeNid == Integer.MAX_VALUE) {  // wildcard
                if (record.taxonomyFlags == flags) {
                   intStreamBuilder.add(record.stampSequence);
                }
             } else if (record.typeNid == typeNid) {
                if (record.taxonomyFlags == flags) {
                   intStreamBuilder.add(record.stampSequence);
                }
             }
          });
      intStreamBuilder.trimToSize();
      return intStreamBuilder.elements();
   }

   /**
    * Gets the stamps of type with flags.
    *
    * @param typeNidSet the type nid set
    * @param flags the flags
    * @return the stamps of type with flags
    */
   public int[] getStampsOfTypeWithFlags(NidSet typeNidSet, int flags) {
      final IntArrayList intStreamBuilder = new IntArrayList();

      this.typeStampFlagsSet.forEach(
          (TypeStampTaxonomyRecord record) -> {
             if (typeNidSet.isEmpty()) {  // wildcard
                if (record.getTaxonomyFlags() == flags) {
                   intStreamBuilder.add(record.getTypeNid());
                }
             } else if (typeNidSet.contains(record.typeNid)) {
                if (record.taxonomyFlags == flags) {
                   intStreamBuilder.add(record.getTypeNid());
                }
             }
          });
      intStreamBuilder.trimToSize();
      return intStreamBuilder.elements();
   }

   /**
    * Gets the type stamp flag stream.
    *
    * @return the type stamp flag stream
    */
   public TypeStampTaxonomyRecord[] getTypeStampFlags() {
      return this.typeStampFlagsSet.toArray(new TypeStampTaxonomyRecord[this.typeStampFlagsSet.size()]);
   }

}

