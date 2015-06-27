/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api;

import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.collections.NidSet;
import gov.vha.isaac.ochre.collections.RefexSequenceSet;
import gov.vha.isaac.ochre.collections.SememeSequenceSet;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import org.jvnet.hk2.annotations.Contract;

/**
 *
 * @author kec
 */
@Contract
public interface IdentifierService {
    
    ObjectChronologyType getChronologyTypeForNid(int nid);
    
    NidSet getComponentNidsForConceptNids(ConceptSequenceSet conceptSequenceSet);
    
    IntStream getComponentNidStream();
    
    int getConceptNidForDescriptionNid(int descriptionNid);
    int getConceptSequenceForDescriptionNid(int descriptionNid);
    void setConceptSequenceForComponentNid(int conceptSequence, int nid);
    void resetConceptSequenceForComponentNid(int conceptSequence, int nid);
    
    int getConceptSequence(int conceptNid);
    int getConceptNid(int conceptSequence);
    
    IntStream getConceptSequenceStream();
    IntStream getParallelConceptSequenceStream();
    
    int getSememeSequence(int sememeNid);
    int getSememeNid(int sememeSequence);
    int getSememeSequenceForUuids(Collection<UUID> uuids);
    int getSememeSequenceForUuids(UUID... uuids);
    
    IntStream getSememeSequenceStream();
    IntStream getParallelSememeSequenceStream();
    
    ConceptSequenceSet getConceptSequencesForConceptNids(NidSet componentNidSet);
    ConceptSequenceSet getConceptSequencesForConceptNids(int[] conceptNidArray);
    SememeSequenceSet getSememeSequencesForSememeNids(int[] sememeNidArray);
    ConceptSequenceSet getConceptSequencesForReferencedComponents(SememeSequenceSet sememeSequences);
    ConceptSequenceSet getConceptSequenceSetForComponentNidSet(NidSet nids);
    
    IntStream getConceptNidsForConceptSequences(IntStream conceptSequences);
    IntStream getSememeNidsForSememeSequences(IntStream sememSequences);
    
    int getNidForUuids(Collection<UUID> uuids);
    int getNidForUuids(UUID... uuids);
    int getNidForProxy(ConceptProxy conceptProxy);
    
    int getConceptSequenceForUuids(Collection<UUID> uuids);
    int getConceptSequenceForUuids(UUID... uuids);
    int getConceptSequenceForProxy(ConceptProxy conceptProxy);
    
    void addUuidForNid(UUID uuid, int nid);

    Optional<UUID> getUuidPrimordialForNid(int nid);
    Optional<UUID> getUuidPrimordialFromConceptSequence(int conceptSequence);

    List<UUID> getUuidsForNid(int nid);

    boolean hasUuid(UUID... uuids);

    boolean hasUuid(Collection<UUID> uuids);

    @Deprecated
    int getConceptSequenceForComponentNid(int nid);
    
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
