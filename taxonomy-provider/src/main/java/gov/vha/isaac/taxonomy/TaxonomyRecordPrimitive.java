/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.taxonomy;

import gov.vha.isaac.ochre.model.WaitFreeComparable;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.ochre.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.model.waitfree.CasSequenceObjectMap;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 *
 * @author kec
 */
public class TaxonomyRecordPrimitive implements WaitFreeComparable {

    public static final int SEQUENCE_BIT_MASK = 0x00FFFFFF;
    public static final int STAMP_BIT_MASK = 0x00FFFFFF;
    public static final int LENGTH_BIT_MASK = 0xFF000000;
    public static final int FLAGS_BIT_MASK = 0xFF000000;
        
    public static Optional<TaxonomyRecordPrimitive> getIfActiveViaType(int conceptSequence, 
            int typeSequence,
            CasSequenceObjectMap<TaxonomyRecordPrimitive> taxonomyMap, 
            TaxonomyCoordinate vp, int flags) {
        Optional<TaxonomyRecordPrimitive> optionalRecord = taxonomyMap.get(conceptSequence);
        if (optionalRecord.isPresent()) {
            TaxonomyRecordPrimitive record = optionalRecord.get();
            if (record.containsSequenceViaType(conceptSequence, typeSequence,
                    vp, flags)) {
                return optionalRecord;
            }
        }
        return Optional.empty();
    }

    public static Optional<TaxonomyRecordPrimitive> getIfConceptActive(int conceptSequence, 
             CasSequenceObjectMap<TaxonomyRecordPrimitive> taxonomyMap, 
            TaxonomyCoordinate vp) {
        Optional<TaxonomyRecordPrimitive> optionalRecord = taxonomyMap.get(conceptSequence);
        if (optionalRecord.isPresent()) {
            TaxonomyRecordPrimitive record = optionalRecord.get();
            if (record.containsSequenceViaType(conceptSequence, conceptSequence,
                    vp, TaxonomyFlags.CONCEPT_STATUS.bits)) {
                return optionalRecord;
            }
        }
        return Optional.empty();
    }
    
    public static boolean isConceptActive(int conceptSequence, 
             CasSequenceObjectMap<TaxonomyRecordPrimitive> taxonomyMap, 
            StampCoordinate sc) {
        Optional<TaxonomyRecordPrimitive> optionalRecord = taxonomyMap.get(conceptSequence);
        if (optionalRecord.isPresent()) {
            TaxonomyRecordPrimitive record = optionalRecord.get();
            if (record.isConceptActive(conceptSequence, sc)) {
                return true;
            }
        }
        return false;
    }

    int writeSequence;
    int[] taxonomyData;
    transient TaxonomyRecordUnpacked unpacked = null;

    public TaxonomyRecordPrimitive() {
        taxonomyData = new int[0];
    }

    public TaxonomyRecordPrimitive(int[] taxonomyData, int writeSequence) {

        this.taxonomyData = taxonomyData;
        this.writeSequence = writeSequence;
    }

    public int[] getArray() {
        if (unpacked != null) {
            taxonomyData = unpacked.pack();
        }
        return taxonomyData;
    }

    public TaxonomyRecordUnpacked getTaxonomyRecordUnpacked() {
        if (unpacked != null) {
            return unpacked;
        }
        unpacked = new TaxonomyRecordUnpacked(taxonomyData);
        return unpacked;
    }

    @Override
    public String toString() {
        return getTaxonomyRecordUnpacked().toString();
    }

    public int nextRecordIndex(int index) {
        return taxonomyData[index] >>> 24;
    }

    public int getConceptSequenceIndex(int conceptSequence) {
        throw new UnsupportedOperationException();
    }

    public int getConceptSequence(int index) {
        return taxonomyData[index] & SEQUENCE_BIT_MASK;
    }

    public void addConceptSequenceStampRecord(int[] conceptSequenceStampRecord) {
        conceptSequenceStampRecord[0] = conceptSequenceStampRecord[0]
                + (conceptSequenceStampRecord.length << 24);
    }

    public boolean inferredFlagSet(int index) {
        return (taxonomyData[index] & TaxonomyFlags.INFERRED.bits) == TaxonomyFlags.INFERRED.bits;
    }

    public boolean statedFlagSet(int index) {
        return (taxonomyData[index] & TaxonomyFlags.STATED.bits) == TaxonomyFlags.STATED.bits;
    }

    public int getStamp(int index) {
        // clear any flag bits
        return taxonomyData[index] & SEQUENCE_BIT_MASK;
    }

    public void setStampAndFlags(int index, int stamp, TaxonomyFlags... flags) {
        taxonomyData[index] = stamp;
        for (TaxonomyFlags flag : flags) {
            taxonomyData[index] = taxonomyData[index] | flag.bits;
        }
    }

    public void setSequence(int index, int sequence) {
        taxonomyData[index] = sequence;
    }

    public void setConceptSequenceStampRecordLength(int index, int length) {
        taxonomyData[index] = taxonomyData[index] & SEQUENCE_BIT_MASK;
        length = length << 24;
        taxonomyData[index] = taxonomyData[index] + length;
    }
    
    public IntStream getParentSequences(TaxonomyCoordinate tc) {
        return getTaxonomyRecordUnpacked().getConceptSequencesForType(tc.getIsaConceptSequence(), tc);
    }

    public IntStream getParentSequences() {
       return getTaxonomyRecordUnpacked().getParentConceptSequences();
    }

    public IntStream getDestinationSequences() {
       return getTaxonomyRecordUnpacked().getDestinationConceptSequences();
    }

    public IntStream getDestinationSequencesOfType(ConceptSequenceSet typeSequenceSet) {
       return getTaxonomyRecordUnpacked().getDestinationConceptSequencesOfType(typeSequenceSet);
    }

    public IntStream getDestinationSequencesOfType(ConceptSequenceSet typeSequenceSet, TaxonomyCoordinate tc) {
       return getTaxonomyRecordUnpacked().getDestinationConceptSequencesOfType(typeSequenceSet, tc);
    }

    public IntStream getDestinationSequencesNotOfType(ConceptSequenceSet typeSequenceSet, TaxonomyCoordinate tc) {
       return getTaxonomyRecordUnpacked().getDestinationConceptSequencesNotOfType(typeSequenceSet, tc);
    }

    public TaxonomyRecordUnpacked unpack() {
        return new TaxonomyRecordUnpacked(taxonomyData);
    }

    public int[] getTaxonomyData() {
        return taxonomyData;
    }

    public boolean containsSequenceViaTypeWithFlags(int conceptSequence, int typeSequence, int flags) {
        return getTaxonomyRecordUnpacked().containsSequenceViaTypeWithFlags(conceptSequence, typeSequence, flags);
    }
    
    public boolean containsSequenceViaType(int conceptSequence, int typeSequence, TaxonomyCoordinate tc) {
        return getTaxonomyRecordUnpacked().containsConceptSequenceViaType(conceptSequence, typeSequence, tc);
    }
    
    public boolean containsSequenceViaType(int conceptSequence, ConceptSequenceSet typeSequenceSet, TaxonomyCoordinate tc) {
        return getTaxonomyRecordUnpacked().containsConceptSequenceViaType(conceptSequence, typeSequenceSet, tc);
    }

    public boolean containsSequenceViaType(int conceptSequence, int typeSequence, TaxonomyCoordinate tc, int flags) {
        return getTaxonomyRecordUnpacked().containsConceptSequenceViaType(conceptSequence, typeSequence, tc, flags);
    }
    
    public boolean containsSequenceViaType(int conceptSequence, ConceptSequenceSet typeSequenceSet, TaxonomyCoordinate tc, int flags) {
        return getTaxonomyRecordUnpacked().containsConceptSequenceViaType(conceptSequence, typeSequenceSet, tc, flags);
    }
    
    public boolean isConceptActive(int conceptSequence, StampCoordinate stampCoordinate) {
        return getTaxonomyRecordUnpacked().conceptSatisfiesStamp(conceptSequence, stampCoordinate);
    }

    public boolean conceptSatisfiesStamp(int conceptSequence, StampCoordinate stampCoordinate) {
        return getTaxonomyRecordUnpacked().conceptSatisfiesStamp(conceptSequence, stampCoordinate);
    }

    public boolean containsSequenceViaType(int conceptSequence, ConceptSequenceSet typeSequenceSet, int flags) {
        return getTaxonomyRecordUnpacked().containsConceptSequenceViaType(conceptSequence, typeSequenceSet, flags);
    }

    @Override
    public int getWriteSequence() {
        return writeSequence;
    }

    @Override
    public void setWriteSequence(int sequence) {
        this.writeSequence = sequence;
    }

    public IntStream getTypesForRelationship(int destinationId, TaxonomyCoordinate tc) {
        return getTaxonomyRecordUnpacked().getTypesForRelationship(destinationId, tc);
    }
}
