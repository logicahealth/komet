/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



package sh.isaac.model.logic.definition;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.logic.assertions.AllRole;
import sh.isaac.api.logic.assertions.ConceptAssertion;
import sh.isaac.api.logic.assertions.Feature;
import sh.isaac.api.logic.assertions.NecessarySet;
import sh.isaac.api.logic.assertions.SomeRole;
import sh.isaac.api.logic.assertions.SufficientSet;
import sh.isaac.api.logic.assertions.Template;
import sh.isaac.api.logic.assertions.connectors.And;
import sh.isaac.api.logic.assertions.connectors.DisjointWith;
import sh.isaac.api.logic.assertions.connectors.Or;
import sh.isaac.api.logic.assertions.literal.BooleanLiteral;
import sh.isaac.api.logic.assertions.literal.FloatLiteral;
import sh.isaac.api.logic.assertions.literal.InstantLiteral;
import sh.isaac.api.logic.assertions.literal.IntegerLiteral;
import sh.isaac.api.logic.assertions.literal.StringLiteral;
import sh.isaac.api.logic.assertions.substitution.BooleanSubstitution;
import sh.isaac.api.logic.assertions.substitution.ConceptSubstitution;
import sh.isaac.api.logic.assertions.substitution.FloatSubstitution;
import sh.isaac.api.logic.assertions.substitution.InstantSubstitution;
import sh.isaac.api.logic.assertions.substitution.IntegerSubstitution;
import sh.isaac.api.logic.assertions.substitution.StringSubstitution;

//~--- classes ----------------------------------------------------------------

/**
 * The Class GenericAxiom.
 *
 * @author kec
 */
public class GenericAxiom
         implements NecessarySet, SufficientSet, And, DisjointWith, ConceptAssertion, AllRole, Feature, SomeRole,
                    Template, Or, BooleanLiteral, FloatLiteral, InstantLiteral, IntegerLiteral, StringLiteral,
                    BooleanSubstitution, ConceptSubstitution, FloatSubstitution, InstantSubstitution,
                    IntegerSubstitution, StringSubstitution {
   /** The builder. */
   private final LogicalExpressionBuilder builder;

   /** The index. */
   private final short index;

   /** The semantic. */
   private final NodeSemantic semantic;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new generic axiom.
    *
    * @param semantic the semantic
    * @param builder the builder
    */
   public GenericAxiom(NodeSemantic semantic, LogicalExpressionBuilderOchreImpl builder) {
      this.builder  = builder;
      this.index    = builder.getNextAxiomIndex();
      this.semantic = semantic;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Equals.
    *
    * @param obj the obj
    * @return true, if successful
    */
   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (getClass() != obj.getClass()) {
         return false;
      }

      final GenericAxiom other = (GenericAxiom) obj;

      if (this.index != other.index) {
         return false;
      }

      return this.semantic == other.semantic;
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      int hash = 3;

      hash = 97 * hash + this.index;
      return hash;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "GenericAxiom{" + "index=" + this.index + ", semantic=" + this.semantic + '}';
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the builder.
    *
    * @return the builder
    */
   @Override
   public LogicalExpressionBuilder getBuilder() {
      return this.builder;
   }

   /**
    * Gets the index.
    *
    * @return the index
    */
   public short getIndex() {
      return this.index;
   }

   /**
    * Gets the semantic.
    *
    * @return the semantic
    */
   public NodeSemantic getSemantic() {
      return this.semantic;
   }
}

