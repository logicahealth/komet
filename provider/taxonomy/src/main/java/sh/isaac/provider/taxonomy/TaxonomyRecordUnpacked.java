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



package sh.isaac.provider.taxonomy;

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

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.TaxonomyCoordinate;
import sh.isaac.api.snapshot.calculator.RelativePositionCalculator;
import sh.isaac.provider.taxonomy.TypeStampTaxonomyRecords.TypeStampTaxonomyRecord;

//~--- classes ----------------------------------------------------------------

/**
 * A {@code TaxonomyRecordUnpacked} is the value for a map where the key is the
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
public class TaxonomyRecordUnpacked {
   /**
    * key = origin concept sequence; value = TypeStampTaxonomyRecords
    */
   private final OpenIntObjectHashMap<TypeStampTaxonomyRecords> conceptSequenceRecordMap =
      new OpenIntObjectHashMap<>(11);

   //~--- constructors --------------------------------------------------------

   public TaxonomyRecordUnpacked() {}

   public TaxonomyRecordUnpacked(int[] recordArray) {
      if (recordArray != null) {
         int index = 0;

         while (index < recordArray.length) {
            final int                      conceptSequence = recordArray[index] & TaxonomyRecordPrimitive.SEQUENCE_BIT_MASK;
            final int                      length          = recordArray[index] >>> 24;
            final TypeStampTaxonomyRecords record          = new TypeStampTaxonomyRecords(recordArray, index);

            index += length;
            this.conceptSequenceRecordMap.put(conceptSequence, record);
         }
      }
   }

   //~--- methods -------------------------------------------------------------

   public void addConceptSequenceStampRecords(int conceptSequence, TypeStampTaxonomyRecords newRecord) {
      if (this.conceptSequenceRecordMap.containsKey(conceptSequence)) {
         final TypeStampTaxonomyRecords oldRecord = this.conceptSequenceRecordMap.get(conceptSequence);

         oldRecord.merge(newRecord);
      } else {
         this.conceptSequenceRecordMap.put(conceptSequence, newRecord);
      }
   }

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

   public boolean conceptSatisfiesStamp(int conceptSequence, StampCoordinate stampCoordinate) {
      final RelativePositionCalculator computer = RelativePositionCalculator.getCalculator(stampCoordinate);

      if (this.conceptSequenceRecordMap.containsKey(conceptSequence)) {
         return this.conceptSequenceRecordMap.get(conceptSequence)
                                        .containsConceptSequenceViaType(Integer.MAX_VALUE,
                                              TaxonomyFlags.CONCEPT_STATUS.bits,
                                              computer);
      }

      return false;
   }

   public int conectionCount() {
      return this.conceptSequenceRecordMap.size();
   }

   public boolean containsConceptSequenceViaType(int conceptSequence, ConceptSequenceSet typeSequenceSet, int flags) {
      if (this.conceptSequenceRecordMap.containsKey(conceptSequence)) {
         return this.conceptSequenceRecordMap.get(conceptSequence)
                                        .isPresent(typeSequenceSet, flags);
      }

      return false;
   }

   public boolean containsConceptSequenceViaType(int conceptSequence,
         ConceptSequenceSet typeSequenceSet,
         TaxonomyCoordinate tc) {
      final RelativePositionCalculator computer = RelativePositionCalculator.getCalculator(tc.getStampCoordinate());

      if (this.conceptSequenceRecordMap.containsKey(conceptSequence)) {
         return this.conceptSequenceRecordMap.get(conceptSequence)
                                        .containsConceptSequenceViaType(typeSequenceSet, tc, computer);
      }

      return false;
   }

   public boolean containsConceptSequenceViaType(int conceptSequence, int typeSequence, TaxonomyCoordinate tc) {
      final RelativePositionCalculator computer = RelativePositionCalculator.getCalculator(tc.getStampCoordinate());

      if (this.conceptSequenceRecordMap.containsKey(conceptSequence)) {
         return this.conceptSequenceRecordMap.get(conceptSequence)
                                        .containsConceptSequenceViaType(typeSequence, tc, computer);
      }

      return false;
   }

   public boolean containsConceptSequenceViaType(int conceptSequence,
         ConceptSequenceSet typeSequenceSet,
         TaxonomyCoordinate tc,
         int flags) {
      final RelativePositionCalculator computer = RelativePositionCalculator.getCalculator(tc.getStampCoordinate());

      if (this.conceptSequenceRecordMap.containsKey(conceptSequence)) {
         return this.conceptSequenceRecordMap.get(conceptSequence)
                                        .containsConceptSequenceViaType(typeSequenceSet, flags, computer);
      }

      return false;
   }

   public boolean containsConceptSequenceViaType(int conceptSequence,
         int typeSequence,
         TaxonomyCoordinate tc,
         int flags) {
      final RelativePositionCalculator computer = RelativePositionCalculator.getCalculator(tc.getStampCoordinate());

      if (this.conceptSequenceRecordMap.containsKey(conceptSequence)) {
         return this.conceptSequenceRecordMap.get(conceptSequence)
                                        .containsConceptSequenceViaType(typeSequence, flags, computer);
      }

      return false;
   }

   public boolean containsSequenceViaTypeWithFlags(int conceptSequence, int typeSequence, int flags) {
      if (this.conceptSequenceRecordMap.containsKey(conceptSequence)) {
         return this.conceptSequenceRecordMap.get(conceptSequence)
                                        .containsStampOfTypeWithFlags(typeSequence, flags);
      }

      return false;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (getClass() != obj.getClass()) {
         return false;
      }

      final TaxonomyRecordUnpacked other = (TaxonomyRecordUnpacked) obj;

      return Objects.equals(this.conceptSequenceRecordMap, other.conceptSequenceRecordMap);
   }

   @Override
   public int hashCode() {
      throw new UnsupportedOperationException("May change values, can't put in a tree or set");
   }

   public int length() {
      int length = 0;

      length = this.conceptSequenceRecordMap.values()
                                       .stream()
                                       .map((record) -> record.length())
                                       .reduce(length, Integer::sum);
      return length;
   }

   public void merge(TaxonomyRecordUnpacked newRecord) {
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

   public int[] pack() {
      final PackConceptSequenceStampRecords packer = new PackConceptSequenceStampRecords();

      this.conceptSequenceRecordMap.forEachPair(packer);
      return packer.taxonomyRecordArray;
   }

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

   public Optional<TypeStampTaxonomyRecords> getConceptSequenceStampRecords(int conceptSequence) {
      return Optional.ofNullable(this.conceptSequenceRecordMap.get(conceptSequence));
   }

   /**
    * @param typeSequence typeSequence to match, or Integer.MAX_VALUE if a
    * wildcard.
    * @return active concepts identified by their sequence value.
    */
   public IntStream getConceptSequencesForType(int typeSequence) {
      final IntStream.Builder conceptSequenceIntStream = IntStream.builder();

      this.conceptSequenceRecordMap.forEachPair((int possibleParentSequence,
            TypeStampTaxonomyRecords stampRecords) -> {
               final IntStream.Builder stampsForConceptIntStream = IntStream.builder();

               stampRecords.getTypeStampFlagStream().forEach((typeStampFlag) -> {
                                       final TypeStampTaxonomyRecord record = new TypeStampTaxonomyRecord(typeStampFlag);

                                       if (typeSequence == Integer.MAX_VALUE) {
                                          stampsForConceptIntStream.add(record.getStampSequence());
                                       } else if (typeSequence == record.getTypeSequence()) {
                                          stampsForConceptIntStream.add(record.getStampSequence());
                                       }
                                    });
               conceptSequenceIntStream.accept(possibleParentSequence);
               return true;
            });
      return conceptSequenceIntStream.build();
   }

   /**
    * @param typeSequence typeSequence to match, or Integer.MAX_VALUE if a
    * wildcard.
    * @param tc used to determine if a concept is active.
    * @return active concepts identified by their sequence value.
    */
   public IntStream getConceptSequencesForType(int typeSequence, TaxonomyCoordinate tc) {
      final int                        flags                    = TaxonomyFlags.getFlagsFromTaxonomyCoordinate(tc);
      final RelativePositionCalculator computer = RelativePositionCalculator.getCalculator(tc.getStampCoordinate());
      final IntStream.Builder          conceptSequenceIntStream = IntStream.builder();

      this.conceptSequenceRecordMap.forEachPair((int possibleParentSequence,
            TypeStampTaxonomyRecords stampRecords) -> {
               final IntStream.Builder stampsForConceptIntStream = IntStream.builder();

               stampRecords.getTypeStampFlagStream().forEach((typeStampFlag) -> {
                                       final TypeStampTaxonomyRecord record = new TypeStampTaxonomyRecord(typeStampFlag);

                                       if ((record.getTaxonomyFlags() & flags) == flags) {
                                          if (typeSequence == Integer.MAX_VALUE) {
                                             stampsForConceptIntStream.add(record.getStampSequence());
                                          } else if (typeSequence == record.getTypeSequence()) {
                                             stampsForConceptIntStream.add(record.getStampSequence());
                                          }
                                       }
                                    });

               if (computer.isLatestActive(stampsForConceptIntStream.build())) {
                  conceptSequenceIntStream.accept(possibleParentSequence);
               }

               return true;
            });
      return conceptSequenceIntStream.build();
   }

   public IntStream getDestinationConceptSequences() {
      final IntStream.Builder conceptSequenceIntStream = IntStream.builder();

      this.conceptSequenceRecordMap.forEachPair((int destinationSequence,
            TypeStampTaxonomyRecords stampRecords) -> {
               conceptSequenceIntStream.accept(destinationSequence);
               return true;
            });
      return conceptSequenceIntStream.build();
   }

   public IntStream getDestinationConceptSequencesNotOfType(ConceptSequenceSet typeSet, TaxonomyCoordinate tc) {
      final int                  flags                    = TaxonomyFlags.getFlagsFromTaxonomyCoordinate(tc);
      final RelativePositionCalculator computer = RelativePositionCalculator.getCalculator(tc.getStampCoordinate());
      final IntStream.Builder          conceptSequenceIntStream = IntStream.builder();

      this.conceptSequenceRecordMap.forEachPair((int destinationSequence,
            TypeStampTaxonomyRecords stampRecords) -> {
               final IntStream.Builder stampsForConceptIntStream = IntStream.builder();

               stampRecords.getTypeStampFlagStream().forEach((typeStampFlag) -> {
                                       final TypeStampTaxonomyRecord record = new TypeStampTaxonomyRecord(typeStampFlag);

                                       if ((record.getTaxonomyFlags() & flags) == flags) {
                                          if (computer.onRoute(record.getStampSequence())) {
                                             if (typeSet.isEmpty()) {
                                                stampsForConceptIntStream.accept(record.getStampSequence());
                                             } else if (!typeSet.contains(record.getTypeSequence())) {
                                                stampsForConceptIntStream.accept(record.getStampSequence());
                                             }
                                          }
                                       }
                                    });

               if (computer.isLatestActive(stampsForConceptIntStream.build())) {
                  conceptSequenceIntStream.accept(destinationSequence);
               }

               return true;
            });
      return conceptSequenceIntStream.build();
   }

   public IntStream getDestinationConceptSequencesOfType(ConceptSequenceSet typeSet) {
      final IntStream.Builder conceptSequenceIntStream = IntStream.builder();

      this.conceptSequenceRecordMap.forEachPair((int destinationSequence,
            TypeStampTaxonomyRecords stampRecords) -> {
               stampRecords.getTypeStampFlagStream().forEach((typeStampFlag) -> {
                                       if (typeSet.contains((int) typeStampFlag
                                       & TaxonomyRecordPrimitive.SEQUENCE_BIT_MASK)) {
                                          conceptSequenceIntStream.accept(destinationSequence);
                                       }
                                    });
               return true;
            });
      return conceptSequenceIntStream.build();
   }

   public IntStream getDestinationConceptSequencesOfType(ConceptSequenceSet typeSet, TaxonomyCoordinate tc) {
      final int                  flags                    = TaxonomyFlags.getFlagsFromTaxonomyCoordinate(tc);
      final RelativePositionCalculator computer = RelativePositionCalculator.getCalculator(tc.getStampCoordinate());
      final IntStream.Builder          conceptSequenceIntStream = IntStream.builder();

      this.conceptSequenceRecordMap.forEachPair((int destinationSequence,
            TypeStampTaxonomyRecords stampRecords) -> {
               final IntStream.Builder stampsForConceptIntStream = IntStream.builder();

               stampRecords.getTypeStampFlagStream().forEach((typeStampFlag) -> {
                                       final TypeStampTaxonomyRecord record = new TypeStampTaxonomyRecord(typeStampFlag);

                                       if ((record.getTaxonomyFlags() & flags) == flags) {
                                          if (computer.onRoute(record.getStampSequence())) {
                                             if (typeSet.isEmpty()) {
                                                stampsForConceptIntStream.accept(record.getStampSequence());
                                             } else if (typeSet.contains(record.getTypeSequence())) {
                                                stampsForConceptIntStream.accept(record.getStampSequence());
                                             }
                                          }
                                       }
                                    });

               if (computer.isLatestActive(stampsForConceptIntStream.build())) {
                  conceptSequenceIntStream.accept(destinationSequence);
               }

               return true;
            });
      return conceptSequenceIntStream.build();
   }

   public IntStream getParentConceptSequences() {
      final int               isaSequence              = TermAux.IS_A.getConceptSequence();
      final IntStream.Builder conceptSequenceIntStream = IntStream.builder();

      this.conceptSequenceRecordMap.forEachPair((int possibleParentSequence,
            TypeStampTaxonomyRecords stampRecords) -> {
               stampRecords.getTypeStampFlagStream().forEach((typeStampFlag) -> {
                                       if ((typeStampFlag & TaxonomyRecordPrimitive.SEQUENCE_BIT_MASK) == isaSequence) {
                                          conceptSequenceIntStream.accept(possibleParentSequence);
                                       }
                                    });
               return true;
            });
      return conceptSequenceIntStream.build();
   }

   IntStream getTypesForRelationship(int destinationId, TaxonomyCoordinate tc) {
      final int                  flags                 = TaxonomyFlags.getFlagsFromTaxonomyCoordinate(tc);
      final RelativePositionCalculator computer = RelativePositionCalculator.getCalculator(tc.getStampCoordinate());
      final IntStream.Builder          typeSequenceIntStream = IntStream.builder();

      this.conceptSequenceRecordMap.forEachPair((int destinationSequence,
            TypeStampTaxonomyRecords stampRecords) -> {
               if (destinationId == destinationSequence) {
                  final Map<Integer, IntStream.Builder> typeStampStreamMap = new HashMap<>();

                  stampRecords.getTypeStampFlagStream().forEach((typeStampFlag) -> {
                                          final TypeStampTaxonomyRecord record = new TypeStampTaxonomyRecord(typeStampFlag);

                                          if ((record.getTaxonomyFlags() & flags) == flags) {
                                             if (computer.onRoute(record.getStampSequence())) {
                                                if (!typeStampStreamMap.containsKey(record.typeSequence)) {
                                                   typeStampStreamMap.put(record.typeSequence, IntStream.builder());
                                                }

                                                typeStampStreamMap.get((record.typeSequence))
                                                      .accept(record.getStampSequence());
                                             }
                                          }
                                       });
                  typeStampStreamMap.forEach((type, stampStreamBuilder) -> {
                                                if (computer.isLatestActive(stampStreamBuilder.build())) {
                                                   typeSequenceIntStream.accept(type);
                                                }
                                             });
               }

               return true;
            });
      return typeSequenceIntStream.build();
   }

   //~--- inner classes -------------------------------------------------------

   private class PackConceptSequenceStampRecords
            implements IntObjectProcedure<TypeStampTaxonomyRecords> {
      int[] taxonomyRecordArray = new int[length()];
      int   destinationPosition = 0;

      //~--- methods ----------------------------------------------------------

      @Override
      public boolean apply(int conceptSequence, TypeStampTaxonomyRecords stampRecordsUnpacked) {
         stampRecordsUnpacked.addToIntArray(conceptSequence, this.taxonomyRecordArray, this.destinationPosition);
         this.destinationPosition += stampRecordsUnpacked.length();
         return true;
      }
   }
}

