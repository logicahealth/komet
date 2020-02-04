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

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;

import org.apache.mahout.math.list.IntArrayList;
import sh.isaac.api.Status;
import sh.isaac.api.collections.NidSet;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.snapshot.calculator.RelativePositionCalculator;

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
    * Adds the concept nid stamp record.
    *
    * @param conceptNidStampRecord the concept nid stamp record
    */
   public void addConceptNidStampRecord(int[] conceptNidStampRecord) {
      conceptNidStampRecord[0] = conceptNidStampRecord[0] + (conceptNidStampRecord.length << 24);
   }

   /**
    * The even indexes are the nids... 0, 2, 4, ...
    * The odd indexes are the position within the array... 1, 3, 5, ...
    * @return
    */
   public static int[][] getDestinationNidPositionArray(int[] data) {
      int index = 0;
      IntArrayList destNidList = new IntArrayList();
      IntArrayList positionList = new IntArrayList();
      IntArrayList lengthList = new IntArrayList();
      while (index < data.length) {
         // the destination nid
         destNidList.add(data[index]);
         // the position in the array
         positionList.add(index);
         index++;
         final int length = data[index];
         lengthList.add(length);
         //final TypeStampTaxonomyRecords records = new TypeStampTaxonomyRecords(recordArray, index);
         //this.conceptNidRecordMap.put(conceptNid, records);

         index += length;
         if (index < 0) {
            throw new IllegalStateException("Index: " + index);
         }
      }
      destNidList.trimToSize();
      positionList.trimToSize();
      lengthList.trimToSize();
      int[][] results = new int[3][];
      results[0] = destNidList.elements();
      results[1] = positionList.elements();
      results[2] = lengthList.elements();
      return results;
   }

   public static int[] merge(int[] thisArray, int[] thatArray) {
      int[][] thisDnpa = getDestinationNidPositionArray(thisArray);
      int[][] thatDnpa = getDestinationNidPositionArray(thatArray);
      IntArrayList solution = new IntArrayList(thisDnpa.length + thatDnpa.length);

      // find differences
      int thisIndex = 0;
      int thatIndex = 0;
      while (true) {
         if (thisDnpa[0][thisIndex] == thatDnpa[0][thatIndex]) {
            // Nids are the same... Is the length the same?
            int thisStart = thisDnpa[1][thisIndex];
            int thisEnd = thisStart +  thisDnpa[2][thisIndex] + 1;
            int thatStart = thatDnpa[1][thatIndex];
            int thatEnd = thatStart +  thatDnpa[2][thatIndex] + 1;
            if (thisDnpa[2][thisIndex] == thatDnpa[2][thatIndex]) {
               // the length is the same, is the content the same?

               if (Arrays.compare(thisArray, thisStart, thisEnd, thatArray, thatStart, thatEnd) == 0) {
                  // Copy the content to the solution.
                  for (int i = thisStart; i < thisEnd; i++) {
                     solution.add(thisArray[i]);
                  }
               } else {
                  // content is not the same... Must merge the two arrays.
                  // add the nid
                  solution.add(thisArray[thisStart]);
                  // the + 2 is to skip the nid and length ints.
                  solution.addAllOf(lengthAndMergedArrays(thisArray, thisStart + 2, thisEnd, thatArray, thatStart + 2, thatEnd));
               }
            } else {
               // length is not the same... Must merge the two arrays.
               // add the nid
               solution.add(thisArray[thisStart]);
               // the + 2 is to skip the nid and length ints.
               solution.addAllOf(lengthAndMergedArrays(thisArray, thisStart + 2, thisEnd, thatArray, thatStart + 2, thatEnd));
            }
            thisIndex++;
            thatIndex++;
         } else {
            // nids are not the same... Copy data from one array or the other...
            // since nids are sorted, copy the data from the array with the smaller nid
            // and increment the counter from the array with the smaller nid.
            if (thisDnpa[0][thisIndex] < thatDnpa[0][thatIndex]) {
               int thisStart = thisDnpa[1][thisIndex];
               int thisEnd = thisStart +  thisDnpa[2][thisIndex] + 1;
               for (int i = thisStart; i < thisEnd; i++) {
                  solution.add(thisArray[i]);
               }
               thisIndex++;
            } else {
               int thatStart = thatDnpa[1][thatIndex];
               int thatEnd = thatStart +  thatDnpa[2][thatIndex] + 1;
               for (int i = thatStart; i < thatEnd; i++) {
                  solution.add(thatArray[i]);
               }
               thatIndex++;
            }
         }
         if (thisIndex == thisDnpa[0].length) {
            // this is done, copy remaining that if any...
            while (thatIndex < thatDnpa[0].length) {
               int thatStart = thatDnpa[1][thatIndex];
               int thatEnd = thatStart +  thatDnpa[2][thatIndex] + 1;
               for (int i = thatStart; i < thatEnd; i++) {
                  solution.add(thatArray[i]);
               }
               thatIndex++;
            }

            break;
         }
         if (thatIndex == thatDnpa[0].length) {
            // that is done, copy remaining this if any
            while (thisIndex < thisDnpa[0].length) {
               int thisStart = thisDnpa[1][thisIndex];
               int thisEnd = thisStart +  thisDnpa[2][thisIndex] + 1;
               for (int i = thisStart; i < thisEnd; i++) {
                  solution.add(thisArray[i]);
               }
               thisIndex++;
            }
            break;
         }
      }
      solution.trimToSize();
      return solution.elements();
   }

   /**
    *
    * @param array1
    * @param array1Start
    * @param array1End
    * @param array2
    * @param array2Start
    * @param array2End
    * @return merged array, with int[0] being the length of the merged array.
    */
   private static IntArrayList lengthAndMergedArrays(int[] array1, int array1Start, int array1End, int[] array2, int array2Start, int array2End) {
      // records fixed three integer sequence: int typeNid, int stampSequence, int taxonomyFlags
      IntArrayList solution = new IntArrayList((array1End - array1Start) + (array2End - array2Start) + 1);
      solution.add(-1); // reserve space for length.
      int array1Index = array1Start;
      int array2Index = array2Start;
      while (true) {
         if (array1[array1Index] == array2[array2Index]) {
            // typeNid is the same... Is the stampSequence the same?
            if (array1[array1Index + 1] == array2[array2Index + 1]) {
               // stampSequences are the same... Are the taxonomyFlags the same?
               if (array1[array1Index + 2] == array2[array2Index + 2]) {
                  // taxonomyFlags are the same
                  solution.add(array1[array1Index]);
                  solution.add(array1[array1Index + 1]);
                  solution.add(array1[array1Index + 2]);
                  array1Index += 3;
                  array2Index += 3;
               } else {
                  solution.add(array1[array1Index]);
                  solution.add(array1[array1Index + 1]);
                  solution.add(array1[array1Index + 2] | array2[array2Index + 2]);
                  array1Index += 3;
                  array2Index += 3;
               }
            } else {
               // stampSequences are not the same
               if (array1[array1Index + 1] < array2[array2Index + 1]) {
                  solution.add(array1[array1Index]);
                  solution.add(array1[array1Index + 1]);
                  solution.add(array1[array1Index + 2]);
                  array1Index += 3;
               } else {
                  solution.add(array2[array2Index]);
                  solution.add(array2[array2Index + 1]);
                  solution.add(array2[array2Index + 2]);
                  array2Index += 3;
               }
            }
         } else {
            // typeNids are not the same...
            if (array1[array1Index] < array2[array2Index]) {
               solution.add(array1[array1Index]);
               solution.add(array1[array1Index + 1]);
               solution.add(array1[array1Index + 2]);
               array1Index += 3;
            } else {
               solution.add(array2[array2Index]);
               solution.add(array2[array2Index + 1]);
               solution.add(array2[array2Index + 2]);
               array2Index += 3;
            }
         }
         if (array1Index == array1End) {
            // need to finish here
            while (array2Index < array2End) {
               solution.add(array2[array2Index]);
               solution.add(array2[array2Index + 1]);
               solution.add(array2[array2Index + 2]);
               array2Index += 3;
            }
            break;
         }
         if (array2Index == array2End) {
            // need to finish here
            while (array1Index < array1End) {
               solution.add(array1[array1Index]);
               solution.add(array1[array1Index + 1]);
               solution.add(array1[array1Index + 2]);
               array1Index += 3;
            }
            break;
         }
      }
      solution.set(0, solution.size());
      return solution;
   }

   /**
    * Concept satisfies stamp.
    *
    * @param conceptNid the concept nid
    * @param stampCoordinate the stamp coordinate
    * @return true, if successful
    */
   public boolean conceptSatisfiesStamp(int conceptNid, StampCoordinate stampCoordinate) {
      final RelativePositionCalculator computer = RelativePositionCalculator.getCalculator(stampCoordinate);

      return conceptSatisfiesStamp(conceptNid, computer);
   }

   public boolean conceptSatisfiesStamp(int conceptNid, RelativePositionCalculator calculator) {
      int index = 0;

      while (index < taxonomyData.length) {
         // the destination nid
         final int recordConceptNid = taxonomyData[index++];
         // followed by a variable number of type, stamp, flag records
         final int length = taxonomyData[index];
         if (recordConceptNid == conceptNid) {
            final TypeStampTaxonomyRecords records = new TypeStampTaxonomyRecords(taxonomyData, index);
            return records.containsConceptNidViaType(conceptNid, TaxonomyFlag.CONCEPT_STATUS.bits, calculator);
         }

         index += length;
         if (index < 0) {
            throw new IllegalStateException("Index: " + index);
         }
      }
      return false;
   }

   public EnumSet<Status> getConceptStates(int conceptNid, StampCoordinate stampCoordinate) {
      return getTaxonomyRecordUnpacked().getConceptStates(conceptNid, stampCoordinate);
   }
   /**
    * Contains nid via type.
    *
    * @param conceptNid the concept nid
    * @param typeNidSet the type nid set
    * @param flags the flags
    * @return true, if successful
    */
   public boolean containsNidViaType(int conceptNid, NidSet typeNidSet, int flags) {
      return getTaxonomyRecordUnpacked().containsConceptNidViaType(conceptNid, typeNidSet, flags);
   }

   /**
    * Contains nid via type.
    *
    * @param conceptNid the concept nid
    * @param typeNidSet the type nid set
    * @param tc the tc
    * @return true, if successful
    */
   public boolean containsNidViaType(int conceptNid,
         NidSet typeNidSet,
         ManifoldCoordinate tc) {
      return getTaxonomyRecordUnpacked().containsConceptNidViaType(conceptNid, typeNidSet, tc);
   }

   /**
    * Contains nid via type.
    *
    * @param conceptNid the concept nid
    * @param typeNid the type nid
    * @param mc the tc
    * @return true, if successful
    */
   public boolean containsNidViaType(int conceptNid, int typeNid, ManifoldCoordinate mc) {
      return getTaxonomyRecordUnpacked().containsConceptNidViaType(conceptNid, typeNid, mc);
   }

   /**
    * Contains nid via type.
    *
    * @param conceptNid the concept nid
    * @param typeNidSet the type nid set
    * @param tc the tc
    * @param flags the flags
    * @return true, if successful
    */
   public boolean containsNidViaType(int conceptNid,
         NidSet typeNidSet,
         ManifoldCoordinate tc,
         int flags) {
      return getTaxonomyRecordUnpacked().containsConceptNidViaType(conceptNid, typeNidSet, tc, flags);
   }

   /**
    * Contains nid via type.
    *
    * @param conceptNid the concept nid
    * @param typeNid the type nid
    * @param tc the tc
    * @param flags the flags
    * @return true, if successful
    */
   public boolean containsNidViaType(int conceptNid, int typeNid, ManifoldCoordinate tc, int flags) {
      return getTaxonomyRecordUnpacked().containsConceptNidViaType(conceptNid, typeNid, tc, flags);
   }

   /**
    * Contains nid via type with flags.
    *
    * @param conceptNid the concept nid
    * @param typeNid the type nid
    * @param flags the flags
    * @return true, if successful
    */
   public boolean containsNidViaTypeWithFlags(int conceptNid, int typeNid, int flags) {
      return getTaxonomyRecordUnpacked().containsNidViaTypeWithFlags(conceptNid, typeNid, flags);
   }

   /**
    * Contains stamp of type with flags.
    *
    * @param typeNid Integer.MAX_VALUE is a wildcard and will match all types.
    * @param flags the flags
    * @return true if found.
    */

   public boolean containsStampOfTypeWithFlags(int typeNid, int flags) {
      return getTaxonomyRecordUnpacked().containsStampOfTypeWithFlags(typeNid, flags);
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
    * @param conceptNid the concept nid
    * @param stampCoordinate the stamp coordinate
    * @return true, if concept active
    * @deprecated replace with calls that use RelativePositionCalculator
    * TODO replace with calls that use RelativePositionCalculator
    */
   public boolean isConceptActive(int conceptNid, StampCoordinate stampCoordinate) {
      return conceptSatisfiesStamp(conceptNid, stampCoordinate);
   }

   public boolean isConceptActive(int conceptNid, RelativePositionCalculator relativePositionCalculator) {
      return conceptSatisfiesStamp(conceptNid, relativePositionCalculator);
   }

   /**
    * Checks if concept active.
    *
    * @param conceptNid the concept nid
    * @param taxonomyData the taxonomy map
    * @param sc the sc
    * @return true, if concept active
    */
   public static boolean isConceptActive(int conceptNid,
         final int[] taxonomyData,
         StampCoordinate sc) {

      if (taxonomyData != null) {
         if (new TaxonomyRecordPrimitive(taxonomyData).isConceptActive(conceptNid, sc)) {
            return true;
         }
      }

      return false;
   }

   /**
    * Gets the concept nid.
    *
    * @param index the index
    * @return the concept nid
    */
   public int getConceptNid(int index) {
      return this.taxonomyData[index] & SEQUENCE_BIT_MASK;
   }

   /**
    * Gets the concept nid index.
    *
    * @param conceptNid the concept nid
    * @return the concept nid index
    */
   public int getConceptNidIndex(int conceptNid) {
      throw new UnsupportedOperationException();
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set concept nid stamp record length.
    *
    * @param index the index
    * @param length the length
    */
   public void setConceptNidStampRecordLength(int index, int length) {
      this.taxonomyData[index] = this.taxonomyData[index] & SEQUENCE_BIT_MASK;
      length                   = length << 24;
      this.taxonomyData[index] = this.taxonomyData[index] + length;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the destination nids not of type.
    *
    * @param typeNidSet the type nid set
    * @param tc the tc
    * @return the destination nids not of type
    */
   public int[] getDestinationNidsNotOfType(NidSet typeNidSet, ManifoldCoordinate tc) {
      return getTaxonomyRecordUnpacked().getDestinationConceptNidsNotOfType(typeNidSet, tc);
   }

   /**
    * Gets the destination nids of type.
    *
    * @param typeNidSet the type nid set
    * @param mc the mc
    * @return the destination nids of type
    */
   public int[] getDestinationNidsOfType(NidSet typeNidSet, ManifoldCoordinate mc) {
      return getTaxonomyRecordUnpacked().getDestinationConceptNidsOfType(typeNidSet, mc);
   }

   /**
    * Gets the destination nids of type.
    *
    * @param typeNidSet the type nid set
    * @param mc the mc
    * @return the destination nids of type
    * TODO speed up by scanning native list for match, rather than unpacking taxonomy record...
    */
   public boolean hasDestinationNidsOfType(NidSet typeNidSet, ManifoldCoordinate mc) {
      return getTaxonomyRecordUnpacked().hasDestinationConceptNidsOfType(typeNidSet, mc);
   }

   /**
    * Gets the if active via type.
    *
    * @param conceptNid the concept nid
    * @param typeNid the type nid
    * @param taxonomyData the taxonomy map
    * @param vp the vp
    * @param flags the flags
    * @return the if active via type
    */
   public static Optional<TaxonomyRecordPrimitive> getIfActiveViaType(int conceptNid,
         int typeNid,
         final int[] taxonomyData,
         ManifoldCoordinate vp,
         int flags) {

      if (taxonomyData != null) {
         TaxonomyRecordPrimitive record = new TaxonomyRecordPrimitive(taxonomyData);
         if (record.containsNidViaType(conceptNid, typeNid, vp, flags)) {
            return Optional.of(record);
         }
      }

      return Optional.empty();
   }

   /**
    * Gets the if concept active.
    *
    * @param conceptNid the concept nid
    * @param taxonomyData the taxonomy map
    * @param vp the vp
    * @return the if concept active
    */
   public static Optional<TaxonomyRecordPrimitive> getIfConceptActive(int conceptNid,
         final int[] taxonomyData,
         ManifoldCoordinate vp) {

      if (taxonomyData != null) {
         TaxonomyRecordPrimitive record = new TaxonomyRecordPrimitive(taxonomyData);
         if (record.containsNidViaType(conceptNid, conceptNid, vp, TaxonomyFlag.CONCEPT_STATUS.bits)) {
            return Optional.of(record);
         }
      }

      return Optional.empty();
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set nid.
    *
    * @param index the index
    * @param nid the nid
    */
   public void setNid(int index, int nid) {
      this.taxonomyData[index] = nid;
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

