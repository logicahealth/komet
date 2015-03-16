/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api;

import gov.vha.isaac.ochre.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.collections.SememeSequenceSet;
import java.util.stream.IntStream;
import org.jvnet.hk2.annotations.Contract;

/**
 *
 * @author kec
 */
@Contract
public interface SequenceProvider {
    
    int getConceptSequence(int nid);
    int getConceptNid(int conceptSequence);
    
    int getSememeSequence(int nid);
    int getSememeNid(int sememeSequence);
    
    IntStream getConceptSequenceStream();
    IntStream getParallelConceptSequenceStream();
    
    IntStream getSememeSequenceStream();
    IntStream getParallelSememeSequenceStream();
    
    ConceptSequenceSet getConceptSequencesForNids(int[] conceptNidArray);
    SememeSequenceSet getSememeSequencesForNids(int[] sememeNidArray);

    IntStream getConceptNidsForSequences(IntStream conceptSequences);
    IntStream getSememeNidsForSequences(IntStream sememSequences);

}
