/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.taxonomy.graph;

import gov.vha.isaac.taxonomy.TaxonomyFlags;
import gov.vha.isaac.taxonomy.TaxonomyRecordPrimitive;
import gov.vha.isaac.taxonomy.TaxonomyRecordUnpacked;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.bootstrap.TermAux;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.ochre.api.tree.hashtree.HashTreeBuilder;
import gov.vha.isaac.ochre.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.waitfree.CasSequenceObjectMap;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.ObjIntConsumer;
import java.util.stream.IntStream;


/**
 * Stream-based, parallelizable,  collector to create a graph, which represents a 
 * particular point in time, and a particular semantic state (stated or inferred) 
 * of a taxonomy.
 * @author kec
 */
public class GraphCollector implements 
        ObjIntConsumer<HashTreeBuilder>, BiConsumer<HashTreeBuilder,HashTreeBuilder>{
    private final int ISA_CONCEPT_SEQUENCE = TermAux.IS_A.getConceptSequence();



    final CasSequenceObjectMap<TaxonomyRecordPrimitive> taxonomyMap;
    final TaxonomyCoordinate taxonomyCoordinate;
    final int taxonomyFlags;
    int originSequenceBeingProcessed = -1;
    ConceptSequenceSet watchList = new ConceptSequenceSet();

    public GraphCollector(CasSequenceObjectMap<TaxonomyRecordPrimitive> taxonomyMap, TaxonomyCoordinate viewCoordinate) {
        this.taxonomyMap = taxonomyMap;
        this.taxonomyCoordinate = viewCoordinate;
        taxonomyFlags = TaxonomyFlags.getFlagsFromTaxonomyCoordinate(viewCoordinate);
//        addToWatchList("779ece66-7e95-323e-a261-214caf48c408");
//        addToWatchList("778a75c9-8264-36aa-9ad6-b9c6e5ee9187");
//        addToWatchList("c377a425-6ac0-3574-9110-b17deb9d49ff");
    }

    public final void addToWatchList(String uuid) throws RuntimeException {
        watchList.add(Get.identifierService().getConceptSequenceForUuids(UUID.fromString(uuid)));
    }

    @Override
    public void accept(HashTreeBuilder graphBuilder, int originSequence) {
        originSequenceBeingProcessed = originSequence;
        Optional<TaxonomyRecordPrimitive> isaacPrimitiveTaxonomyRecord = taxonomyMap.get(originSequence);
        
        if (isaacPrimitiveTaxonomyRecord.isPresent()) {
        // For debugging. 
        if (watchList.contains(originSequence)) {
            System.out.println("Found watch: " + isaacPrimitiveTaxonomyRecord);
        }
            TaxonomyRecordUnpacked taxonomyRecordUnpacked = isaacPrimitiveTaxonomyRecord.get().getTaxonomyRecordUnpacked();
            IntStream destinationStream = taxonomyRecordUnpacked.getConceptSequencesForType(ISA_CONCEPT_SEQUENCE, taxonomyCoordinate);
            destinationStream.forEach((int destinationSequence) -> graphBuilder.add(destinationSequence, originSequence));
        }
        originSequenceBeingProcessed = -1;
    }

    @Override
    public void accept(HashTreeBuilder t, HashTreeBuilder u) {
        t.combine(u);
    }

    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append("GraphCollector{");
        buff.append(TaxonomyFlags.getTaxonomyFlags(taxonomyFlags));
        
        if (originSequenceBeingProcessed != -1) {
            buff.append("} processing: ");
            buff.append(Get.conceptDescriptionText(originSequenceBeingProcessed));
            buff.append(" <");
            buff.append(originSequenceBeingProcessed);
            buff.append(">");
        } else {
            buff.append("}");
        }
        return buff.toString();
    }

}
