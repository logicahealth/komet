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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.function.IntObjectProcedure;
import org.apache.mahout.math.list.IntArrayList;
import org.apache.mahout.math.map.OpenIntObjectHashMap;
import org.apache.mahout.math.set.OpenIntHashSet;

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.snapshot.calculator.RelativePositionCalculator;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.provider.bdb.taxonomy.TypeStampTaxonomyRecords.TypeStampTaxonomyRecord;

//~--- classes ----------------------------------------------------------------

/**
 * For each concept sequence (a key in the map), there is a list of type-stamp-flags. These
 * type-stamp-flags compact into a primitive long value. 
 * 
 * 
 * A {@code TaxonomyRecord} is the value for a map where the key is the
 * concept sequence for a concept in the taxonomy, and the value is a map to
 * other concept sequences, and the associated stamps and taxonomy flags for
 * these other concept sequences. From the stamp value and the taxonomy flags,
 * all historic taxonomic associations (parent, child, stated, and inferred) can
 * be computed.
 *
 *
 * origin concept sequence [1 -> n] {destination concept sequence [1 -> n] stamp
 * + inferred + stated + parent + child}
 * <p>
 * <p>
 * Created by kec on 11/8/14.
 */
public class TaxonomyRecord {
   /** key = origin concept sequence; value = TypeStampTaxonomyRecords. */
   private final OpenIntObjectHashMap<TypeStampTaxonomyRecords> conceptSequenceRecordMap =
      new OpenIntObjectHashMap<>(11);

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new taxonomy record unpacked.
    */
   public TaxonomyRecord() {}

   /**
    * Instantiates a new taxonomy record unpacked.
    *
    * @param recordArray the record array
    */
   public TaxonomyRecord(int[] recordArray) {
      if (recordArray != null) {
         int index = 0;

         while (index < recordArray.length) {
            final int conceptSequence             = recordArray[index] & TaxonomyRecordPrimitive.SEQUENCE_BIT_MASK;
            final int                      length = recordArray[index] >>> 24;
            final TypeStampTaxonomyRecords record = new TypeStampTaxonomyRecords(recordArray, index);

            index += length;
            this.conceptSequenceRecordMap.put(conceptSequence, record);
         }
      }
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the concept sequence stamp records.
    *
    * @param conceptSequence the concept sequence
    * @param newRecord the new record
    */
   public void addConceptSequenceStampRecords(int conceptSequence, TypeStampTaxonomyRecords newRecord) {
      if (this.conceptSequenceRecordMap.containsKey(conceptSequence)) {
         final TypeStampTaxonomyRecords oldRecord = this.conceptSequenceRecordMap.get(conceptSequence);

         oldRecord.merge(newRecord);
      } else {
         this.conceptSequenceRecordMap.put(conceptSequence, newRecord);
      }
   }

   /**
    * Adds the stamp record.
    *
    * @param destinationSequence the destination sequence
    * @param typeSequence the type sequence
    * @param stamp the stamp
    * @param recordFlags the record flags
    */
   public void addStampRecord(int destinationSequence, int typeSequence, int stamp, int recordFlags) {
      if (destinationSequence < 0) {
         destinationSequence = Get.identifierService()
                                  .getConceptSequence(destinationSequence);
      }

      if (typeSequence < 0) {
         typeSequence = Get.identifierService()
                           .getConceptSequence(typeSequence);
      }

      TypeStampTaxonomyRecords conceptSequenceStampRecordsUnpacked;

      if (this.conceptSequenceRecordMap.containsKey(destinationSequence)) {
         conceptSequenceStampRecordsUnpacked = this.conceptSequenceRecordMap.get(destinationSequence);
      } else {
         conceptSequenceStampRecordsUnpacked = new TypeStampTaxonomyRecords();
         this.conceptSequenceRecordMap.put(destinationSequence, conceptSequenceStampRecordsUnpacked);
      }

      conceptSequenceStampRecordsUnpacked.addStampRecord(typeSequence, stamp, recordFlags);
   }

   /**
    * Concept satisfies stamp.
    *
    * @param conceptSequence the concept sequence
    * @param stampCoordinate the stamp coordinate
    * @return true, if successful
    */
   public boolean conceptSatisfiesStamp(int conceptSequence, StampCoordinate stampCoordinate) {
      final RelativePositionCalculator computer = RelativePositionCalculator.getCalculator(stampCoordinate);

      if (this.conceptSequenceRecordMap.containsKey(conceptSequence)) {
         return this.conceptSequenceRecordMap.get(conceptSequence)
               .containsConceptSequenceViaType(Integer.MAX_VALUE, TaxonomyFlag.CONCEPT_STATUS.bits, computer);
      }

      return false;
   }

   /**
    * Conection count.
    *
    * @return the int
    */
   public int conectionCount() {
      return this.conceptSequenceRecordMap.size();
   }

   /**
    * Contains concept sequence via type.
    *
    * @param conceptSequence the concept sequence
    * @param typeSequenceSet the type sequence set
    * @param flags the flags
    * @return true, if successful
    */
   public boolean containsConceptSequenceViaType(int conceptSequence, ConceptSequenceSet typeSequenceSet, int flags) {
      if (this.conceptSequenceRecordMap.containsKey(conceptSequence)) {
         return this.conceptSequenceRecordMap.get(conceptSequence)
               .isPresent(typeSequenceSet, flags);
      }

      return false;
   }

   /**
    * Contains concept sequence via type.
    *
    * @param conceptSequence the concept sequence
    * @param typeSequenceSet the type sequence set
    * @param tc the tc
    * @return true, if successful
    */
   public boolean containsConceptSequenceViaType(int conceptSequence,
         ConceptSequenceSet typeSequenceSet,
         ManifoldCoordinate tc) {
      final RelativePositionCalculator computer = RelativePositionCalculator.getCalculator(tc.getStampCoordinate());

      if (this.conceptSequenceRecordMap.containsKey(conceptSequence)) {
         return this.conceptSequenceRecordMap.get(conceptSequence)
               .containsConceptSequenceViaType(typeSequenceSet, tc, computer);
      }

      return false;
   }

   /**
    * Contains concept sequence via type.
    *
    * @param conceptSequence the concept sequence
    * @param typeSequence the type sequence
    * @param tc the tc
    * @return true, if successful
    */
   public boolean containsConceptSequenceViaType(int conceptSequence, int typeSequence, ManifoldCoordinate tc) {
      final RelativePositionCalculator computer = RelativePositionCalculator.getCalculator(tc.getStampCoordinate());

      if (this.conceptSequenceRecordMap.containsKey(conceptSequence)) {
         return this.conceptSequenceRecordMap.get(conceptSequence)
               .containsConceptSequenceViaType(typeSequence, tc, computer);
      }

      return false;
   }

   /**
    * Contains concept sequence via type.
    *
    * @param conceptSequence the concept sequence
    * @param typeSequenceSet the type sequence set
    * @param tc the tc
    * @param flags the flags
    * @return true, if successful
    */
   public boolean containsConceptSequenceViaType(int conceptSequence,
         ConceptSequenceSet typeSequenceSet,
         ManifoldCoordinate tc,
         int flags) {
      final RelativePositionCalculator computer = RelativePositionCalculator.getCalculator(tc.getStampCoordinate());

      if (this.conceptSequenceRecordMap.containsKey(conceptSequence)) {
         return this.conceptSequenceRecordMap.get(conceptSequence)
               .containsConceptSequenceViaType(typeSequenceSet, flags, computer);
      }

      return false;
   }

   /**
    * Contains concept sequence via type.
    *
    * @param conceptSequence the concept sequence
    * @param typeSequence the type sequence
    * @param tc the tc
    * @param flags the flags
    * @return true, if successful
    */
   public boolean containsConceptSequenceViaType(int conceptSequence,
         int typeSequence,
         ManifoldCoordinate tc,
         int flags) {
      final RelativePositionCalculator computer = RelativePositionCalculator.getCalculator(tc.getStampCoordinate());

      if (this.conceptSequenceRecordMap.containsKey(conceptSequence)) {
         return this.conceptSequenceRecordMap.get(conceptSequence)
               .containsConceptSequenceViaType(typeSequence, flags, computer);
      }

      return false;
   }

   /**
    * Contains sequence via type with flags.
    *
    * @param conceptSequence the concept sequence
    * @param typeSequence the type sequence
    * @param flags the flags
    * @return true, if successful
    */
   public boolean containsSequenceViaTypeWithFlags(int conceptSequence, int typeSequence, int flags) {
      if (this.conceptSequenceRecordMap.containsKey(conceptSequence)) {
         return this.conceptSequenceRecordMap.get(conceptSequence)
               .containsStampOfTypeWithFlags(typeSequence, flags);
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

      return Objects.equals(this.conceptSequenceRecordMap, other.conceptSequenceRecordMap);
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

      length = this.conceptSequenceRecordMap.values()
            .stream()
            .map((record) -> record.length())
            .reduce(length, Integer::sum);
      return length;
   }

   /**
    * Merge.
    *
    * @param newRecord the new record
    */
   public void merge(TaxonomyRecord newRecord) {
      newRecord.conceptSequenceRecordMap.forEachPair((int key,
            TypeStampTaxonomyRecords value) -> {
               if (this.conceptSequenceRecordMap.containsKey(key)) {
                  this.conceptSequenceRecordMap.get(key)
                                               .merge(value);
               } else {
                  this.conceptSequenceRecordMap.put(key, value);
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

      this.conceptSequenceRecordMap.forEachPair(packer);
      return packer.taxonomyRecordArray;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      final IntArrayList theKeys = this.conceptSequenceRecordMap.keys();

      theKeys.sort();

      final StringBuilder buf = new StringBuilder();

      buf.append("[");

      final int maxIndex = theKeys.size() - 1;

      for (int i = 0; i <= maxIndex; i++) {
         final int conceptSequence = theKeys.get(i);

         if (i > 0) {
            buf.append("\n   ");
         }

         buf.append(Get.conceptDescriptionText(conceptSequence));
         buf.append(" <");
         buf.append(conceptSequence);
         buf.append("> <-");

         final TypeStampTaxonomyRecords value = this.conceptSequenceRecordMap.get(conceptSequence);

         value.stream().forEach((TypeStampTaxonomyRecord record) -> {
                          buf.append("\n      ");
                          buf.append(record.toString());
                       });
      }

      buf.append(']');
      return buf.toString();
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the concept sequence stamp records.
    *
    * @param conceptSequence the concept sequence
    * @return the concept sequence stamp records
    */
   public Optional<TypeStampTaxonomyRecords> getConceptSequenceStampRecords(int conceptSequence) {
      return Optional.ofNullable(this.conceptSequenceRecordMap.get(conceptSequence));
   }

   /**
    * Gets the concept sequences for type.
    *
    * @param typeSequence typeSequence to match, or Integer.MAX_VALUE if a
    * wildcard.
    * @return active concepts identified by their sequence value.
    */
   public int[] getConceptSequencesForType(int typeSequence) {
      OpenIntHashSet conceptSequencesForTypeSet = new OpenIntHashSet();

      this.conceptSequenceRecordMap.forEachPair((int possibleParentSequence,
            TypeStampTaxonomyRecords stampRecords) -> {
               final OpenIntHashSet stampsForConceptSet = new OpenIntHashSet();

               stampRecords.forEachTypeStampFlag((typeStampFlag) -> {
                                       final TypeStampTaxonomyRecord record =
                                          new TypeStampTaxonomyRecord(typeStampFlag);

                                       if (typeSequence == Integer.MAX_VALUE) {
                                          stampsForConceptSet.add(record.getStampSequence());
                                       } else if (typeSequence == record.getTypeSequence()) {
                                          stampsForConceptSet.add(record.getStampSequence());
                                       }
                                       return true;
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
    * @param typeSequence typeSequence to match, or Integer.MAX_VALUE if a
    * wildcard.
    * @param tc used to determine if a concept is active.
    * @return active concepts identified by their sequence value.
    */
   public int[] getConceptSequencesForType(int typeSequence, ManifoldCoordinate tc) {
      final int                        flags                    = TaxonomyFlag.getFlagsFromManifoldCoordinate(tc);
      final RelativePositionCalculator computer = RelativePositionCalculator.getCalculator(tc);
      final OpenIntHashSet          conceptSequencesForTypeSet = new OpenIntHashSet();

      this.conceptSequenceRecordMap.forEachPair((int possibleParentSequence,
            TypeStampTaxonomyRecords stampRecords) -> {
               final OpenIntHashSet stampsForConceptIntStream = new OpenIntHashSet();

               stampRecords.forEachTypeStampFlag((typeStampFlag) -> {
                                       final TypeStampTaxonomyRecord record =
                                          new TypeStampTaxonomyRecord(typeStampFlag);

                                       if ((record.getTaxonomyFlags() & flags) == flags) {
                                          if (typeSequence == Integer.MAX_VALUE) {
                                             stampsForConceptIntStream.add(record.getStampSequence());
                                          } else if (typeSequence == record.getTypeSequence()) {
                                             stampsForConceptIntStream.add(record.getStampSequence());
                                          }
                                       }
                                       return true;
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

      this.conceptSequenceRecordMap.forEachPair((int destinationSequence,
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
   public int[] getDestinationConceptSequencesNotOfType(ConceptSequenceSet typeSet, ManifoldCoordinate tc) {
      final int                        flags                    = TaxonomyFlag.getFlagsFromManifoldCoordinate(tc);
      final RelativePositionCalculator computer = RelativePositionCalculator.getCalculator(tc.getStampCoordinate());
      final OpenIntHashSet          conceptSequenceIntSet = new OpenIntHashSet();

      this.conceptSequenceRecordMap.forEachPair((int destinationSequence,
            TypeStampTaxonomyRecords stampRecords) -> {
               final OpenIntHashSet stampsForConceptIntStream = new OpenIntHashSet();

               stampRecords.forEachTypeStampFlag((typeStampFlag) -> {
                                       final TypeStampTaxonomyRecord record =
                                          new TypeStampTaxonomyRecord(typeStampFlag);

                                       if ((record.getTaxonomyFlags() & flags) == flags) {
                                          if (computer.onRoute(record.getStampSequence())) {
                                             if (typeSet.isEmpty()) {
                                                stampsForConceptIntStream.add(record.getStampSequence());
                                             } else if (!typeSet.contains(record.getTypeSequence())) {
                                                stampsForConceptIntStream.add(record.getStampSequence());
                                             }
                                          }
                                       }
                                       return true;
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
   public IntStream getDestinationConceptSequencesOfType(ConceptSequenceSet typeSet) {
      final IntStream.Builder conceptSequenceIntStream = IntStream.builder();

      this.conceptSequenceRecordMap.forEachPair((int destinationSequence,
            TypeStampTaxonomyRecords stampRecords) -> {
               stampRecords.forEachTypeStampFlag((typeStampFlag) -> {
                                       if (typeSet.contains((int) typeStampFlag
                                       & TaxonomyRecordPrimitive.SEQUENCE_BIT_MASK)) {
                                          conceptSequenceIntStream.accept(destinationSequence);
                                       }
                                       return true;
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
   public int[] getDestinationConceptSequencesOfType(ConceptSequenceSet typeSet, ManifoldCoordinate tc) {
      final int                        flags                    = TaxonomyFlag.getFlagsFromManifoldCoordinate(tc);
      final RelativePositionCalculator computer = RelativePositionCalculator.getCalculator(tc.getStampCoordinate());
      final OpenIntHashSet          conceptSequenceIntSet = new OpenIntHashSet();

      this.conceptSequenceRecordMap.forEachPair((int destinationSequence,
            TypeStampTaxonomyRecords stampRecords) -> {
               final OpenIntHashSet stampsForConceptIntSet = new OpenIntHashSet();

               stampRecords.forEachTypeStampFlag((typeStampFlag) -> {
                                       final TypeStampTaxonomyRecord record =
                                          new TypeStampTaxonomyRecord(typeStampFlag);

                                       if ((record.getTaxonomyFlags() & flags) == flags) {
                                          if (computer.onRoute(record.getStampSequence())) {
                                             if (typeSet.isEmpty()) {
                                                stampsForConceptIntSet.add(record.getStampSequence());
                                             } else if (typeSet.contains(record.getTypeSequence())) {
                                                stampsForConceptIntSet.add(record.getStampSequence());
                                             }
                                          }
                                       }
                                       return true;
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
    * Gets the parent concept sequences.
    *
    * @return the parent concept sequences
    */
   public IntStream getParentConceptSequences() {
      final int               isaSequence              = TermAux.IS_A.getConceptSequence();
      final IntStream.Builder conceptSequenceIntStream = IntStream.builder();

      this.conceptSequenceRecordMap.forEachPair((int possibleParentSequence,
            TypeStampTaxonomyRecords stampRecords) -> {
               stampRecords.forEachTypeStampFlag((typeStampFlag) -> {
                                       if ((typeStampFlag & TaxonomyRecordPrimitive.SEQUENCE_BIT_MASK) == isaSequence) {
                                          conceptSequenceIntStream.accept(possibleParentSequence);
                                       }
                                       return true;
                                    });
               return true;
            });
      return conceptSequenceIntStream.build();
   }

   /**
    * Gets the types for relationship.
    *
    * @param destinationId the destination id
    * @param tc the tc
    * @return the types for relationship
    */
   int[] getTypesForRelationship(int destinationId, ManifoldCoordinate tc) {
      final int                        flags                 = TaxonomyFlag.getFlagsFromManifoldCoordinate(tc);
      final RelativePositionCalculator computer = RelativePositionCalculator.getCalculator(tc.getStampCoordinate());
      final OpenIntHashSet          typeSequenceIntSet = new OpenIntHashSet();

      this.conceptSequenceRecordMap.forEachPair((int destinationSequence,
            TypeStampTaxonomyRecords stampRecords) -> {
               if (destinationId == destinationSequence) {
                  final Map<Integer, OpenIntHashSet> typeStampStreamMap = new HashMap<>();

                  stampRecords.forEachTypeStampFlag((typeStampFlag) -> {
                                          final TypeStampTaxonomyRecord record =
                                             new TypeStampTaxonomyRecord(typeStampFlag);

                                          if ((record.getTaxonomyFlags() & flags) == flags) {
                                             if (computer.onRoute(record.getStampSequence())) {
                                                if (!typeStampStreamMap.containsKey(record.typeSequence)) {
                                                   typeStampStreamMap.put(record.typeSequence, new OpenIntHashSet());
                                                }

                                                typeStampStreamMap.get((record.typeSequence))
                                                      .add(record.getStampSequence());
                                             }
                                          }
                                          return true;
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
      /** The taxonomy record array. */
      int[] taxonomyRecordArray = new int[length()];

      /** The destination position. */
      int destinationPosition = 0;

      //~--- methods ----------------------------------------------------------

      /**
       * Apply.
       *
       * @param conceptSequence the concept sequence
       * @param stampRecordsUnpacked the stamp records unpacked
       * @return true, if successful
       */
      @Override
      public boolean apply(int conceptSequence, TypeStampTaxonomyRecords stampRecordsUnpacked) {
         stampRecordsUnpacked.addToIntArray(conceptSequence, this.taxonomyRecordArray, this.destinationPosition);
         this.destinationPosition += stampRecordsUnpacked.length();
         return true;
      }
   }
}

