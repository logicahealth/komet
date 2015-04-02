/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api.chronicle;

/**
 *
 * @author kec
 * @param <V>
 */
public interface ChronicledConcept<V extends StampedVersion> extends ChronicledObjectLocal<V> {
    int getConceptSequence();
}
