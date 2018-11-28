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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import sh.isaac.api.Get;
import sh.isaac.api.TaxonomySnapshot;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.query.ClauseComputeType;
import sh.isaac.api.query.ClauseSemantic;
import sh.isaac.api.query.LeafClause;
import sh.isaac.api.query.LetItemKey;
import sh.isaac.api.query.Query;
import sh.isaac.api.query.WhereClause;

/**
 *
 * @author kec
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class ReferencedComponentIsKindOf
        extends LeafClause {

    /**
     * The parent concept spec key.
     */
    @XmlElement
    LetItemKey parentSpecKey;

    /**
     * the manifold coordinate key.
     */
    @XmlElement
    LetItemKey manifoldCoordinateKey;
    
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
     * @param parentSpecKey the concept spec key
     * @param manifoldCoordinateKey the manifold coordinate key
     */
    public ReferencedComponentIsKindOf(Query enclosingQuery,
            LetItemKey parentSpecKey,
            LetItemKey manifoldCoordinateKey) {
        super(enclosingQuery);
        this.parentSpecKey = parentSpecKey;
        this.manifoldCoordinateKey = manifoldCoordinateKey;
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
        ConceptSpecification parentSpec = (ConceptSpecification) this.enclosingQuery.getLetDeclarations().get(parentSpecKey);

        TaxonomySnapshot snapshot = Get.taxonomyService().getSnapshot(manifoldCoordinate);

        NidSet possibleComponents = incomingPossibleComponents.get(getAssemblageForIteration());
        
        for (int nid: possibleComponents.asArray()) {
            if (!test(snapshot, nid, parentSpec.getNid())) {
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

    public LetItemKey getParentSpecKey() {
        return parentSpecKey;
    }

    public void setParentSpecKey(LetItemKey parentSpecKey) {
        this.parentSpecKey = parentSpecKey;
    }

    public LetItemKey getManifoldCoordinateKey() {
        return manifoldCoordinateKey;
    }

    public void setManifoldCoordinateKey(LetItemKey manifoldCoordinateKey) {
        this.manifoldCoordinateKey = manifoldCoordinateKey;
    }

    @Override
    public ClauseSemantic getClauseSemantic() {
        return ClauseSemantic.REFERENCED_COMPONENT_IS_KIND_OF;
    }

    /**
     * Gets the where clause.
     *
     * @return the where clause
     */
    @Override
    public WhereClause getWhereClause() {
        final WhereClause whereClause = new WhereClause();

        whereClause.setSemantic(ClauseSemantic.REFERENCED_COMPONENT_IS_KIND_OF);
        whereClause.getLetKeys()
                .add(this.parentSpecKey);
        whereClause.getLetKeys()
                .add(this.manifoldCoordinateKey);
        return whereClause;
    }

    @Override
    public ConceptSpecification getClauseConcept() {
        return TermAux.REFERENCED_COMPONENT_IS_KIND_OF;
    }

}
