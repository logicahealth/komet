/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api;

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
    
}
