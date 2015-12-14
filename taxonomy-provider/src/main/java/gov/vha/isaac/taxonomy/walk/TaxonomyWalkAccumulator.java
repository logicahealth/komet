/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.taxonomy.walk;

import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;

/**
 *
 * @author kec
 */
public class TaxonomyWalkAccumulator {
    public int conceptsProcessed = 0;
    public int connections = 0;
    public int maxConnections = 0;
    public int minConnections = 0;
    public int parentConnections = 0;
    ConceptChronology<?> watchConcept = null;

    void combine(TaxonomyWalkAccumulator u) {
        this.conceptsProcessed += u.conceptsProcessed;
        this.connections += u.connections;
        
        this.maxConnections = Math.max(this.maxConnections, u.maxConnections);
        this.minConnections = Math.max(this.minConnections, u.minConnections);

        this.parentConnections += u.parentConnections;
    }

    @Override
    public String toString() {
        return "TaxonomyWalkAccumulator{" + 
                "conceptsProcessed=" + conceptsProcessed + 
                ", connections=" + connections + 
                ", maxConnections=" + maxConnections + 
                ", minConnections=" + minConnections + 
                ", parentConnections=" + parentConnections + 
                '}';
    }
}
