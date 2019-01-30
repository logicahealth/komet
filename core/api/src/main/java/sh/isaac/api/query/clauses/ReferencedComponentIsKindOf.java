/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.api.query.clauses;

import java.util.EnumSet;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import sh.isaac.api.Get;
import sh.isaac.api.TaxonomySnapshot;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.query.ClauseComputeType;
import sh.isaac.api.query.ClauseSemantic;
import sh.isaac.api.query.LetItemKey;
import sh.isaac.api.query.Query;

/**
 *
 * @author kec
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class ReferencedComponentIsKindOf
        extends ReferencedComponentWithManifoldAbstract {

   
    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new refset contains concept.
     */
    public ReferencedComponentIsKindOf() {
    }

    /**
     * Instantiates a new refset contains concept.
     *
     * @param enclosingQuery the enclosing query
     * @param referencedComponentSpecKey the concept spec key
     * @param manifoldCoordinateKey the manifold coordinate key
     */
    public ReferencedComponentIsKindOf(Query enclosingQuery,
            LetItemKey referencedComponentSpecKey,
            LetItemKey manifoldCoordinateKey) {
        super(enclosingQuery, referencedComponentSpecKey, manifoldCoordinateKey);
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compute possible components.
     *
     * @param incomingPossibleComponents the incoming possible components
     * @return the nid set
     */
    @Override
    public Map<ConceptSpecification, NidSet> computePossibleComponents(Map<ConceptSpecification, NidSet> incomingPossibleComponents) {

        ManifoldCoordinate manifoldCoordinate = (ManifoldCoordinate) this.enclosingQuery.getLetDeclarations().get(manifoldCoordinateKey);
        ConceptSpecification parentSpec = (ConceptSpecification) this.enclosingQuery.getLetDeclarations().get(referencedComponentSpecKey);
        ConceptChronology parentConcept = Get.concept(parentSpec);
        if (!parentConcept.isLatestVersionActive(manifoldCoordinate)) {
            throw new IllegalStateException("Parent concept in kind-of query is inactive.");
        }

        TaxonomySnapshot snapshot = Get.taxonomyService().getSnapshot(manifoldCoordinate);
        NidSet kindOfSet = snapshot.getKindOfConceptNidSet(parentSpec.getNid());

        NidSet possibleComponents = incomingPossibleComponents.get(getAssemblageForIteration());
        
        for (int nid: possibleComponents.asArray()) {
            SemanticChronology semanticChronology = Get.assemblageService().getSemanticChronology(nid);
            if (!kindOfSet.contains(semanticChronology.getReferencedComponentNid())) {
                possibleComponents.remove(nid);
            }
        }
        return incomingPossibleComponents;
    }
    
    protected boolean test(TaxonomySnapshot snapshot, int childNid, int parentNid) {
        return snapshot.isKindOf(childNid, parentNid);
    }
    
    
 
    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the compute phases.
     *
     * @return the compute phases
     */
    @Override
    public EnumSet<ClauseComputeType> getComputePhases() {
        return PRE_ITERATION;
    }

    @Override
    public ClauseSemantic getClauseSemantic() {
        return ClauseSemantic.REFERENCED_COMPONENT_IS_KIND_OF;
    }

}
