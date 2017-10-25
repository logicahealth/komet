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

import java.util.Optional;
import java.util.stream.IntStream;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.collections.SpinedIntObjectMap;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.ManifoldCoordinate;

//~--- classes ----------------------------------------------------------------

/**
 * The Class TaxonomyRecordPrimitive.
 *
 * @author kec
 */
public class TaxonomyRecordPrimitive {
   /** The Constant SEQUENCE_BIT_MASK. */
   public static final int SEQUENCE_BIT_MASK = 0x00FFFFFF;

   /** The Constant STAMP_BIT_MASK. */
   public static final int STAMP_BIT_MASK = 0x00FFFFFF;

   /** The Constant LENGTH_BIT_MASK. */
   public static final int LENGTH_BIT_MASK = 0xFF000000;

   /** The Constant FLAGS_BIT_MASK. */
   public static final int FLAGS_BIT_MASK = 0xFF000000;

   //~--- fields --------------------------------------------------------------

   /** The unpacked. */
   transient TaxonomyRecord unpacked = null;

   /** The taxonomy data. */
   int[] taxonomyData;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new taxonomy record primitive.
    */
   public TaxonomyRecordPrimitive() {
      this.taxonomyData = new int[0];
   }

   /**
    * Instantiates a new taxonomy record primitive.
    *
    * @param taxonomyData the taxonomy data
    */
   public TaxonomyRecordPrimitive(int[] taxonomyData) {
      this.taxonomyData  = taxonomyData;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the concept sequence stamp record.
    *
    * @param conceptSequenceStampRecord the concept sequence stamp record
    */
   public void addConceptSequenceStampRecord(int[] conceptSequenceStampRecord) {
      conceptSequenceStampRecord[0] = conceptSequenceStampRecord[0] + (conceptSequenceStampRecord.length << 24);
   }

   /**
    * Concept satisfies stamp.
    *
    * @param conceptSequence the concept sequence
    * @param stampCoordinate the stamp coordinate
    * @return true, if successful
    */
   public boolean conceptSatisfiesStamp(int conceptSequence, StampCoordinate stampCoordinate) {
      return getTaxonomyRecordUnpacked().conceptSatisfiesStamp(conceptSequence, stampCoordinate);
   }

   /**
    * Contains sequence via type.
    *
    * @param conceptSequence the concept sequence
    * @param typeSequenceSet the type sequence set
    * @param flags the flags
    * @return true, if successful
    */
   public boolean containsSequenceViaType(int conceptSequence, ConceptSequenceSet typeSequenceSet, int flags) {
      return getTaxonomyRecordUnpacked().containsConceptSequenceViaType(conceptSequence, typeSequenceSet, flags);
   }

   /**
    * Contains sequence via type.
    *
    * @param conceptSequence the concept sequence
    * @param typeSequenceSet the type sequence set
    * @param tc the tc
    * @return true, if successful
    */
   public boolean containsSequenceViaType(int conceptSequence,
         ConceptSequenceSet typeSequenceSet,
         ManifoldCoordinate tc) {
      return getTaxonomyRecordUnpacked().containsConceptSequenceViaType(conceptSequence, typeSequenceSet, tc);
   }

   /**
    * Contains sequence via type.
    *
    * @param conceptSequence the concept sequence
    * @param typeSequence the type sequence
    * @param tc the tc
    * @return true, if successful
    */
   public boolean containsSequenceViaType(int conceptSequence, int typeSequence, ManifoldCoordinate tc) {
      return getTaxonomyRecordUnpacked().containsConceptSequenceViaType(conceptSequence, typeSequence, tc);
   }

   /**
    * Contains sequence via type.
    *
    * @param conceptSequence the concept sequence
    * @param typeSequenceSet the type sequence set
    * @param tc the tc
    * @param flags the flags
    * @return true, if successful
    */
   public boolean containsSequenceViaType(int conceptSequence,
         ConceptSequenceSet typeSequenceSet,
         ManifoldCoordinate tc,
         int flags) {
      return getTaxonomyRecordUnpacked().containsConceptSequenceViaType(conceptSequence, typeSequenceSet, tc, flags);
   }

   /**
    * Contains sequence via type.
    *
    * @param conceptSequence the concept sequence
    * @param typeSequence the type sequence
    * @param tc the tc
    * @param flags the flags
    * @return true, if successful
    */
   public boolean containsSequenceViaType(int conceptSequence, int typeSequence, ManifoldCoordinate tc, int flags) {
      return getTaxonomyRecordUnpacked().containsConceptSequenceViaType(conceptSequence, typeSequence, tc, flags);
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
      return getTaxonomyRecordUnpacked().containsSequenceViaTypeWithFlags(conceptSequence, typeSequence, flags);
   }

   /**
    * Inferred flag set.
    *
    * @param index the index
    * @return true, if successful
    */
   public boolean inferredFlagSet(int index) {
      return (this.taxonomyData[index] & TaxonomyFlag.INFERRED.bits) == TaxonomyFlag.INFERRED.bits;
   }

   /**
    * Next record index.
    *
    * @param index the index
    * @return the int
    */
   public int nextRecordIndex(int index) {
      return this.taxonomyData[index] >>> 24;
   }

   /**
    * Stated flag set.
    *
    * @param index the index
    * @return true, if successful
    */
   public boolean statedFlagSet(int index) {
      return (this.taxonomyData[index] & TaxonomyFlag.STATED.bits) == TaxonomyFlag.STATED.bits;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return getTaxonomyRecordUnpacked().toString();
   }

   /**
    * Unpack.
    *
    * @return the taxonomy record unpacked
    */
   public TaxonomyRecord unpack() {
      return new TaxonomyRecord(this.taxonomyData);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the array.
    *
    * @return the array
    */
   public int[] getArray() {
      if (this.unpacked != null) {
         this.taxonomyData = this.unpacked.pack();
      }

      return this.taxonomyData;
   }

   /**
    * Checks if concept active.
    *
    * @param conceptSequence the concept sequence
    * @param stampCoordinate the stamp coordinate
    * @return true, if concept active
    */
   public boolean isConceptActive(int conceptSequence, StampCoordinate stampCoordinate) {
      return getTaxonomyRecordUnpacked().conceptSatisfiesStamp(conceptSequence, stampCoordinate);
   }

   /**
    * Checks if concept active.
    *
    * @param conceptSequence the concept sequence
    * @param taxonomyMap the taxonomy map
    * @param sc the sc
    * @return true, if concept active
    */
   public static boolean isConceptActive(int conceptSequence,
         SpinedIntObjectMap<int[]> taxonomyMap,
         StampCoordinate sc) {
      final int[] taxonomyData = taxonomyMap.get(conceptSequence);

      if (taxonomyData != null) {
         if (new TaxonomyRecordPrimitive(taxonomyData).isConceptActive(conceptSequence, sc)) {
            return true;
         }
      }

      return false;
   }

   /**
    * Gets the concept sequence.
    *
    * @param index the index
    * @return the concept sequence
    */
   public int getConceptSequence(int index) {
      return this.taxonomyData[index] & SEQUENCE_BIT_MASK;
   }

   /**
    * Gets the concept sequence index.
    *
    * @param conceptSequence the concept sequence
    * @return the concept sequence index
    */
   public int getConceptSequenceIndex(int conceptSequence) {
      throw new UnsupportedOperationException();
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set concept sequence stamp record length.
    *
    * @param index the index
    * @param length the length
    */
   public void setConceptSequenceStampRecordLength(int index, int length) {
      this.taxonomyData[index] = this.taxonomyData[index] & SEQUENCE_BIT_MASK;
      length                   = length << 24;
      this.taxonomyData[index] = this.taxonomyData[index] + length;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the destination sequences.
    *
    * @return the destination sequences
    */
   public IntStream getDestinationSequences() {
      return getTaxonomyRecordUnpacked().getDestinationConceptSequences();
   }

   /**
    * Gets the destination sequences not of type.
    *
    * @param typeSequenceSet the type sequence set
    * @param tc the tc
    * @return the destination sequences not of type
    */
   public int[] getDestinationSequencesNotOfType(ConceptSequenceSet typeSequenceSet, ManifoldCoordinate tc) {
      return getTaxonomyRecordUnpacked().getDestinationConceptSequencesNotOfType(typeSequenceSet, tc);
   }

   /**
    * Gets the destination sequences of type.
    *
    * @param typeSequenceSet the type sequence set
    * @return the destination sequences of type
    */
   public IntStream getDestinationSequencesOfType(ConceptSequenceSet typeSequenceSet) {
      return getTaxonomyRecordUnpacked().getDestinationConceptSequencesOfType(typeSequenceSet);
   }

   /**
    * Gets the destination sequences of type.
    *
    * @param typeSequenceSet the type sequence set
    * @param tc the tc
    * @return the destination sequences of type
    */
   public int[] getDestinationSequencesOfType(ConceptSequenceSet typeSequenceSet, ManifoldCoordinate tc) {
      return getTaxonomyRecordUnpacked().getDestinationConceptSequencesOfType(typeSequenceSet, tc);
   }

   /**
    * Gets the if active via type.
    *
    * @param conceptSequence the concept sequence
    * @param typeSequence the type sequence
    * @param taxonomyMap the taxonomy map
    * @param vp the vp
    * @param flags the flags
    * @return the if active via type
    */
   public static Optional<TaxonomyRecordPrimitive> getIfActiveViaType(int conceptSequence,
         int typeSequence,
         SpinedIntObjectMap<int[]> taxonomyMap,
         ManifoldCoordinate vp,
         int flags) {
      final int[] taxonomyData = taxonomyMap.get(conceptSequence);

      if (taxonomyData != null) {
         TaxonomyRecordPrimitive record = new TaxonomyRecordPrimitive(taxonomyData);
         if (record.containsSequenceViaType(conceptSequence, typeSequence, vp, flags)) {
            return Optional.of(record);
         }
      }

      return Optional.empty();
   }

   /**
    * Gets the if concept active.
    *
    * @param conceptSequence the concept sequence
    * @param taxonomyMap the taxonomy map
    * @param vp the vp
    * @return the if concept active
    */
   public static Optional<TaxonomyRecordPrimitive> getIfConceptActive(int conceptSequence,
         SpinedIntObjectMap<int[]> taxonomyMap,
         ManifoldCoordinate vp) {
      final int[] taxonomyData = taxonomyMap.get(conceptSequence);

      if (taxonomyData != null) {
         TaxonomyRecordPrimitive record = new TaxonomyRecordPrimitive(taxonomyData);
         if (record.containsSequenceViaType(conceptSequence, conceptSequence, vp, TaxonomyFlag.CONCEPT_STATUS.bits)) {
            return Optional.of(record);
         }
      }

      return Optional.empty();
   }

   /**
    * Gets the parent sequences.
    *
    * @return the parent sequences
    */
   public IntStream getParentSequences() {
      return getTaxonomyRecordUnpacked().getParentConceptSequences();
   }

   /**
    * Gets the parent sequences.
    *
    * @param tc the tc
    * @return the parent sequences
    */
   public int[] getParentSequences(ManifoldCoordinate tc) {
      return getTaxonomyRecordUnpacked().getConceptSequencesForType(tc.getIsaConceptSequence(), tc);
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set sequence.
    *
    * @param index the index
    * @param sequence the sequence
    */
   public void setSequence(int index, int sequence) {
      this.taxonomyData[index] = sequence;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the stamp.
    *
    * @param index the index
    * @return the stamp
    */
   public int getStamp(int index) {
      // clear any flag bits
      return this.taxonomyData[index] & SEQUENCE_BIT_MASK;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set stamp and flags.
    *
    * @param index the index
    * @param stamp the stamp
    * @param flags the flags
    */
   public void setStampAndFlags(int index, int stamp, TaxonomyFlag... flags) {
      this.taxonomyData[index] = stamp;

      for (final TaxonomyFlag flag: flags) {
         this.taxonomyData[index] = this.taxonomyData[index] | flag.bits;
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the taxonomy record unpacked.
    *
    * @return the taxonomy record unpacked
    */
   public TaxonomyRecord getTaxonomyRecordUnpacked() {
      if (this.unpacked != null) {
         return this.unpacked;
      }

      this.unpacked = new TaxonomyRecord(this.taxonomyData);
      return this.unpacked;
   }

   /**
    * Gets the types for relationship.
    *
    * @param destinationId the destination id
    * @param tc the tc
    * @return the types for relationship
    */
   public int[] getTypesForRelationship(int destinationId, ManifoldCoordinate tc) {
      return getTaxonomyRecordUnpacked().getTypesForRelationship(destinationId, tc);
   }
}

