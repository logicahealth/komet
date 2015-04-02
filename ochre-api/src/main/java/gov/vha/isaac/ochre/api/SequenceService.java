/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api;

import gov.vha.isaac.ochre.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.collections.RefexSequenceSet;
import gov.vha.isaac.ochre.collections.SememeSequenceSet;
import java.util.stream.IntStream;
import org.jvnet.hk2.annotations.Contract;

/**
 *
 * @author kec
 */
@Contract
public interface SequenceService {
    
    int getConceptSequence(int nid);
    int getConceptNid(int conceptSequence);
    
    IntStream getConceptSequenceStream();
    IntStream getParallelConceptSequenceStream();
    
    int getSememeSequence(int nid);
    int getSememeNid(int sememeSequence);
    
    IntStream getSememeSequenceStream();
    IntStream getParallelSememeSequenceStream();
    
    ConceptSequenceSet getConceptSequencesForNids(int[] conceptNidArray);
    SememeSequenceSet getSememeSequencesForNids(int[] sememeNidArray);

    IntStream getConceptNidsForSequences(IntStream conceptSequences);
    IntStream getSememeNidsForSequences(IntStream sememSequences);
    
    
    @Deprecated
    int getRefexSequence(int nid);
    @Deprecated
    int getRefexNid(int refexSequence);

    @Deprecated
    IntStream getRefexSequenceStream();
    @Deprecated
    IntStream getParallelRefexSequenceStream();

    @Deprecated
    IntStream getRefexNidsForSequences(IntStream refexSequences);
    @Deprecated
    RefexSequenceSet getRefexSequencesForNids(int[] refexNidArray);
}
