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



/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package sh.isaac.provider.taxonomy;

//~--- JDK imports ------------------------------------------------------------

import java.util.EnumSet;
import java.util.stream.LongStream;
import java.util.stream.Stream;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.function.LongProcedure;
import org.apache.mahout.math.list.IntArrayList;
import org.apache.mahout.math.set.OpenLongHashSet;

import sh.isaac.api.Get;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.snapshot.calculator.RelativePositionCalculator;
import sh.isaac.api.coordinate.ManifoldCoordinate;

//~--- classes ----------------------------------------------------------------

/**
 * This class maps stamps to the {@code TaxonomyFlags} associated with that
 * stampSequence.
 *
 * @author kec
 */
public class TypeStampTaxonomyRecords {
   /** int (the map key) is a stampSequence TaxonomyFlags (the map value) are the flags associated with the stampSequence;. */
   private final OpenLongHashSet typeStampFlagsSet = new OpenLongHashSet(7);

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new type stamp taxonomy records.
    */
   public TypeStampTaxonomyRecords() {}

   /**
    * Instantiates a new type stamp taxonomy records.
    *
    * @param sourceArray the source array
    * @param sourcePosition the source position
    */
   public TypeStampTaxonomyRecords(int[] sourceArray, int sourcePosition) {
      final int length    = sourceArray[sourcePosition] >>> 24;
      final int recordEnd = sourcePosition + length;

      for (sourcePosition = sourcePosition + 1; sourcePosition < recordEnd; sourcePosition += 2) {
         long record = sourceArray[sourcePosition];

         record = record << 32;
         record += sourceArray[sourcePosition + 1];
         this.typeStampFlagsSet.add(record);
      }
   }

   /**
    * Instantiates a new type stamp taxonomy records.
    *
    * @param typeSequence the type sequence
    * @param stampSequence the stamp sequence
    * @param flag the flag
    */
   public TypeStampTaxonomyRecords(int typeSequence, int stampSequence, TaxonomyFlags flag) {
      this.typeStampFlagsSet.add(convertToLong(typeSequence, stampSequence, flag.bits));
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the stamp record.
    *
    * @param typeSequence the type sequence
    * @param stampSequence the stamp sequence
    * @param taxonomyFlags the taxonomy flags
    */
   public void addStampRecord(int typeSequence, int stampSequence, int taxonomyFlags) {
      final long record = convertToLong(typeSequence, stampSequence, taxonomyFlags);

      this.typeStampFlagsSet.add(record);
   }

   /**
    * Adds the to int array.
    *
    * @param conceptSequence the concept sequence
    * @param destinationArray the destination array
    * @param destinationPosition the destination position
    */
   public void addToIntArray(int conceptSequence, int[] destinationArray, int destinationPosition) {
      final int length = length();
      final int index  = destinationPosition + 1;

      destinationArray[destinationPosition] = conceptSequence + (length << 24);

      final AddToArrayProcedure addToArrayIntObjectProcedure = new AddToArrayProcedure(index, destinationArray);

      this.typeStampFlagsSet.forEachKey(addToArrayIntObjectProcedure);
   }

   /**
    * Contains concept sequence via type.
    *
    * @param typeSequenceSet the type sequence set
    * @param flags the flags
    * @param computer the computer
    * @return true, if successful
    */
   public boolean containsConceptSequenceViaType(ConceptSequenceSet typeSequenceSet,
         int flags,
         RelativePositionCalculator computer) {
      final int[] latestStamps =
         computer.getLatestStampSequencesAsSet(getStampsOfTypeWithFlags(typeSequenceSet,
                                                                        flags));

      return latestStamps.length > 0;
   }

   /**
    * Contains concept sequence via type.
    *
    * @param typeSequenceSet the type sequence set
    * @param tc the tc
    * @param computer the computer
    * @return true, if successful
    */
   public boolean containsConceptSequenceViaType(ConceptSequenceSet typeSequenceSet,
         ManifoldCoordinate tc,
         RelativePositionCalculator computer) {
      final int flags = TaxonomyFlags.getFlagsFromManifoldCoordinate(tc);

      return containsConceptSequenceViaType(typeSequenceSet, flags, computer);
   }

   /**
    * Contains concept sequence via type.
    *
    * @param typeSequence the type sequence
    * @param flags the flags
    * @param computer the computer
    * @return true, if successful
    */
   public boolean containsConceptSequenceViaType(int typeSequence, int flags, RelativePositionCalculator computer) {
      final int[] latestStamps = computer.getLatestStampSequencesAsSet(getStampsOfTypeWithFlags(typeSequence,
                                                                                                           flags));

      return latestStamps.length > 0;
   }

   /**
    * Contains concept sequence via type.
    *
    * @param typeSequence the type sequence
    * @param tc the tc
    * @param computer the computer
    * @return true, if successful
    */
   public boolean containsConceptSequenceViaType(int typeSequence,
         ManifoldCoordinate tc,
         RelativePositionCalculator computer) {
      final int flags = TaxonomyFlags.getFlagsFromManifoldCoordinate(tc);

      return containsConceptSequenceViaType(typeSequence, flags, computer);
   }

   /**
    * Contains stamp of type with flags.
    *
    * @param typeSequenceSet An empty set is a wildcard and will match all types.
    * @param flags the flags
    * @return true if found.
    */
   public boolean containsStampOfTypeWithFlags(ConceptSequenceSet typeSequenceSet, int flags) {
      final boolean found = !this.typeStampFlagsSet.forEachKey((long record) -> {
               if (typeSequenceSet.isEmpty()) {  // wildcard
                  if (((record >>> 32) & TaxonomyRecordPrimitive.FLAGS_BIT_MASK) == flags) {
                     return false;               // finish search.
                  }
               } else if (typeSequenceSet.contains(((int) record & TaxonomyRecordPrimitive.SEQUENCE_BIT_MASK))) {
                  if (((record >>> 32) & TaxonomyRecordPrimitive.FLAGS_BIT_MASK) == flags) {
                     return false;               // finish search.
                  }
               }

               return true;                      // continue search...
            });

      return found;
   }

   /**
    * Contains stamp of type with flags.
    *
    * @param typeSequence Integer.MAX_VALUE is a wildcard and will match all types.
    * @param flags the flags
    * @return true if found.
    */
   public boolean containsStampOfTypeWithFlags(int typeSequence, int flags) {
      final boolean found = !this.typeStampFlagsSet.forEachKey((long record) -> {
               if (typeSequence == Integer.MAX_VALUE) {  // wildcard
                  if (flags == 0) {                      // taxonomy flag wildcard--inferred, stated, non-defining, ...
                     return false;                       // finish search
                  } else if (((record >>> 32) & TaxonomyRecordPrimitive.FLAGS_BIT_MASK) == flags) {
                     return false;                       // finish search.
                  }
               } else if ((record & TaxonomyRecordPrimitive.SEQUENCE_BIT_MASK) == typeSequence) {
                  if (flags == 0) {                      // taxonomy flag wildcard--inferred, stated, non-defining, ...
                     return false;                       // finish search
                  } else if (((record >>> 32) & TaxonomyRecordPrimitive.FLAGS_BIT_MASK) == flags) {
                     return false;                       // finish search.
                  }
               }

               return true;                              // continue search...
            });

      return found;
   }

   /**
    * Convert to long.
    *
    * @param typeSequence the type sequence
    * @param stampSequence the stamp sequence
    * @param taxonomyFlags the taxonomy flags
    * @return the long
    */
   public static long convertToLong(int typeSequence, int stampSequence, int taxonomyFlags) {
      long record = stampSequence;

      if (taxonomyFlags > 512) {
         record += taxonomyFlags;
      } else {
         record += (taxonomyFlags << 24);
      }

      record = record << 32;
      record += typeSequence;
      return record;
   }

   /**
    * Length.
    *
    * @return the number of integers this stampSequence record will occupy when
    * packed.
    */
   public int length() {
      // 1 is for the concept sequence with the top 8 bits set to the length
      // of sequence plus the associated stampSequence records.
      return 1 + (this.typeStampFlagsSet.size() * 2);
   }

   /**
    * Merge.
    *
    * @param newRecords the new records
    */
   public void merge(TypeStampTaxonomyRecords newRecords) {
      newRecords.typeStampFlagsSet.forEachKey((long recordAsLong) -> {
               this.typeStampFlagsSet.add(recordAsLong);
               return true;
            });
   }

   /**
    * Stream.
    *
    * @return the stream
    */
   public Stream<TypeStampTaxonomyRecord> stream() {
      final Stream.Builder<TypeStampTaxonomyRecord> builder = Stream.builder();

      this.typeStampFlagsSet.forEachKey((long record) -> {
                                           builder.accept(new TypeStampTaxonomyRecord(record));
                                           return true;
                                        });
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

      this.typeStampFlagsSet.forEachKey((long record) -> {
                                           final TypeStampTaxonomyRecord str = new TypeStampTaxonomyRecord(record);

                                           sb.append(str.toString());
                                           return true;
                                        });
      return sb.toString();
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Checks if present.
    *
    * @param typeSequenceSet the type sequence set
    * @param flags the flags
    * @return true, if present
    */
   public boolean isPresent(ConceptSequenceSet typeSequenceSet, int flags) {
      return containsStampOfTypeWithFlags(typeSequenceSet, flags);
   }

   /**
    * Gets the stamps of type with flags.
    *
    * @param typeSequenceSet the type sequence set
    * @param flags the flags
    * @return the stamps of type with flags
    */
   public int[] getStampsOfTypeWithFlags(ConceptSequenceSet typeSequenceSet, int flags) {
      final IntArrayList intStreamBuilder = new IntArrayList();

      this.typeStampFlagsSet.forEachKey((long record) -> {
                                           final int stampAndFlag = (int) (record >>> 32);

                                           if (typeSequenceSet.isEmpty()) {  // wildcard
                                              if ((stampAndFlag & TaxonomyRecordPrimitive.FLAGS_BIT_MASK) == flags) {
                                                 intStreamBuilder.add(stampAndFlag
                                                 & TaxonomyRecordPrimitive.SEQUENCE_BIT_MASK);
                                              }
                                           } else if (typeSequenceSet.contains((int) record
                                           & TaxonomyRecordPrimitive.SEQUENCE_BIT_MASK)) {
                                              if ((stampAndFlag & TaxonomyRecordPrimitive.FLAGS_BIT_MASK) == flags) {
                                                 intStreamBuilder.add(stampAndFlag
                                                 & TaxonomyRecordPrimitive.SEQUENCE_BIT_MASK);
                                              }
                                           }

                                           return true;
                                        });
      intStreamBuilder.trimToSize();
      return intStreamBuilder.elements();
   }

   /**
    * Gets the stamps of type with flags.
    *
    * @param typeSequence the type sequence
    * @param flags the flags
    * @return the stamps of type with flags
    */
   public int[] getStampsOfTypeWithFlags(int typeSequence, int flags) {
      final IntArrayList intStreamBuilder = new IntArrayList();

      this.typeStampFlagsSet.forEachKey((long record) -> {
                                           final int stampAndFlag = (int) (record >>> 32);

                                           if (typeSequence == Integer.MAX_VALUE) {  // wildcard
                                              if ((stampAndFlag & TaxonomyRecordPrimitive.FLAGS_BIT_MASK) == flags) {
                                                 intStreamBuilder.add(stampAndFlag
                                                 & TaxonomyRecordPrimitive.SEQUENCE_BIT_MASK);
                                              }
                                           } else if ((record & TaxonomyRecordPrimitive.SEQUENCE_BIT_MASK) ==
                                           typeSequence) {
                                              if ((stampAndFlag & TaxonomyRecordPrimitive.FLAGS_BIT_MASK) == flags) {
                                                 intStreamBuilder.add(stampAndFlag
                                                 & TaxonomyRecordPrimitive.SEQUENCE_BIT_MASK);
                                              }
                                           }

                                           return true;
                                        });
      intStreamBuilder.trimToSize();
      return intStreamBuilder.elements();
   }

   /**
    * Gets the type stamp flag stream.
    *
    * @return the type stamp flag stream
    */
   public long[] getTypeStampFlags() {
      return this.typeStampFlagsSet.keys().elements();
   }
   
   public void forEachTypeStampFlag(LongProcedure procedure) {
      this.typeStampFlagsSet.forEachKey(procedure);
   }

   //~--- inner classes -------------------------------------------------------

   /**
    * The Class AddToArrayProcedure.
    */
   private static class AddToArrayProcedure
            implements LongProcedure {
      /** The index. */
      int index;

      /** The destination array. */
      int destinationArray[];

      //~--- constructors -----------------------------------------------------

      /**
       * Instantiates a new adds the to array procedure.
       *
       * @param index the index
       * @param destinationArray the destination array
       */
      public AddToArrayProcedure(int index, int[] destinationArray) {
         this.index            = index;
         this.destinationArray = destinationArray;
      }

      //~--- methods ----------------------------------------------------------

      /**
       * Adds the combined typeSequence + stampSequence + flags to the index location in the
       * destination array defined in the procedure constructor.
       *
       * @param record the record
       * @return true to continue.
       */
      @Override
      public boolean apply(long record) {
         final int stampAndFlags = (int) (record >>> 32);

         this.destinationArray[this.index++] = stampAndFlags;
         this.destinationArray[this.index++] = (int) record;
         return true;
      }
   }


   /**
    * The Class TypeStampTaxonomyRecord.
    */
   public static class TypeStampTaxonomyRecord {
      /** The type sequence. */
      int typeSequence;

      /** The stamp sequence. */
      int stampSequence;

      /** The taxonomy flags. */
      int taxonomyFlags;

      //~--- constructors -----------------------------------------------------

      /**
       * Instantiates a new type stamp taxonomy record.
       *
       * @param record the record
       */
      public TypeStampTaxonomyRecord(long record) {
         this.typeSequence  = (int) record & TaxonomyRecordPrimitive.SEQUENCE_BIT_MASK;
         record             = record >>> 32;
         this.stampSequence = (int) record & TaxonomyRecordPrimitive.SEQUENCE_BIT_MASK;
         this.taxonomyFlags = (int) record & TaxonomyRecordPrimitive.FLAGS_BIT_MASK;
      }

      /**
       * Instantiates a new type stamp taxonomy record.
       *
       * @param typeSequence the type sequence
       * @param stampSequence the stamp sequence
       * @param taxonomyFlags the taxonomy flags
       */
      public TypeStampTaxonomyRecord(int typeSequence, int stampSequence, int taxonomyFlags) {
         this.typeSequence  = typeSequence;
         this.stampSequence = stampSequence;
         this.taxonomyFlags = taxonomyFlags;
      }

      //~--- methods ----------------------------------------------------------

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

         final TypeStampTaxonomyRecord other = (TypeStampTaxonomyRecord) obj;

         if (this.stampSequence != other.stampSequence) {
            return false;
         }

         if (this.typeSequence != other.typeSequence) {
            return false;
         }

         return this.taxonomyFlags == other.taxonomyFlags;
      }

      /**
       * Hash code.
       *
       * @return the int
       */
      @Override
      public int hashCode() {
         int hash = 7;

         hash = 97 * hash + this.stampSequence;
         return hash;
      }

      /**
       * To string.
       *
       * @return the string
       */
      @Override
      public String toString() {
         final StringBuilder sb = new StringBuilder();

         sb.append("«");
         sb.append(Get.conceptService()
                      .getConcept(this.typeSequence)
                      .toUserString());
         sb.append(" <");
         sb.append(this.typeSequence);
         sb.append(">");
         sb.append(" ss:");
         sb.append(this.stampSequence);
         sb.append(" ");
         sb.append(Get.stampService()
                      .describeStampSequence(this.stampSequence));
         sb.append(" ");
         sb.append(getTaxonomyFlagsAsEnum());
         sb.append("»");
         return sb.toString();
      }

      //~--- get methods ------------------------------------------------------

      /**
       * Gets the as long.
       *
       * @return the as long
       */
      public long getAsLong() {
         return convertToLong(this.typeSequence, this.stampSequence, this.taxonomyFlags);
      }

      /**
       * Gets the stamp sequence.
       *
       * @return the stamp sequence
       */
      public int getStampSequence() {
         return this.stampSequence;
      }

      /**
       * Gets the taxonomy flags.
       *
       * @return the taxonomy flags
       */
      public int getTaxonomyFlags() {
         return this.taxonomyFlags;
      }

      /**
       * Gets the taxonomy flags as enum.
       *
       * @return the taxonomy flags as enum
       */
      public EnumSet<TaxonomyFlags> getTaxonomyFlagsAsEnum() {
         return TaxonomyFlags.getTaxonomyFlags(this.taxonomyFlags);
      }

      /**
       * Gets the type sequence.
       *
       * @return the type sequence
       */
      public int getTypeSequence() {
         return this.typeSequence;
      }
   }
}

