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

import sh.isaac.api.query.*;
import sh.isaac.api.query.properties.ManifoldClause;
import sh.isaac.api.query.properties.ReferencedComponentClause;

import java.util.EnumSet;

/**
 *
 * @author kec
 */
public abstract class ReferencedComponentWithManifoldAbstract extends LeafClause 
        implements ManifoldClause, ReferencedComponentClause {

    /**
     * The parent concept spec key.
     */
    LetItemKey referencedComponentSpecKey;

    /**
     * the manifold coordinate key.
     */
    LetItemKey manifoldCoordinateKey;
    
    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new refset contains concept.
     */
    public ReferencedComponentWithManifoldAbstract() {
    }

    /**
     * Instantiates a new refset contains concept.
     *
     * @param enclosingQuery the enclosing query
     * @param referencedComponentSpecKey the concept spec key
     * @param manifoldCoordinateKey the manifold coordinate key
     */
    public ReferencedComponentWithManifoldAbstract(Query enclosingQuery,
            LetItemKey referencedComponentSpecKey,
            LetItemKey manifoldCoordinateKey) {
        super(enclosingQuery);
        this.referencedComponentSpecKey = referencedComponentSpecKey;
        this.manifoldCoordinateKey = manifoldCoordinateKey;
    }

    //~--- methods -------------------------------------------------------------
 
 
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
    public LetItemKey getReferencedComponentSpecKey() {
        return referencedComponentSpecKey;
    }

    @Override
    public void setReferencedComponentSpecKey(LetItemKey referencedComponentSpecKey) {
        this.referencedComponentSpecKey = referencedComponentSpecKey;
    }

    @Override
    public LetItemKey getManifoldCoordinateKey() {
        return manifoldCoordinateKey;
    }

    @Override
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
    public final WhereClause getWhereClause() {
        final WhereClause whereClause = new WhereClause();

        whereClause.setSemantic(getClauseSemantic());
        whereClause.getLetKeys()
                .add(this.referencedComponentSpecKey);
        whereClause.getLetKeys()
                .add(this.manifoldCoordinateKey);
        return whereClause;
    }

}
