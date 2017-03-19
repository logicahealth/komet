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



package sh.isaac.provider.query;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import javax.xml.bind.annotation.*;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.component.concept.ConceptVersion;

//~--- classes ----------------------------------------------------------------

/**
 * Acts as root node for the construction of queries. Sorts results from one of
 * more child clauses, which can be instances of a ParentClause or
 * <code>LeafClause</code>. Every
 * <code>Query</code> must contain a ParentClause as the root node.
 *
 * @author kec
 */
@XmlRootElement(name = "parent")
@XmlAccessorType(value = XmlAccessType.NONE)
public abstract class ParentClause
        extends Clause {
   /**
    * Array of instances of
    * <code>Clause</code> that are children of the ParentClause in the tree
    * used to compute the constructed
    * <code>Query</code>.
    */
   @XmlElementWrapper(name = "child-clauses")
   @XmlElement(name = "clause")
   private List<Clause> children = new ArrayList();;

   //~--- constructors --------------------------------------------------------

   /**
    * Default no arg constructor for Jaxb.
    */
   protected ParentClause() {
      super();
   }

   /**
    * Constructor from a Query and child clauses.
    *
    * @param enclosingQuery the enclosing query
    * @param children the children
    */
   public ParentClause(Query enclosingQuery, Clause... children) {
      super(enclosingQuery);
      setChildren(Arrays.asList(children));
   }

   /**
    * Instantiates a new parent clause.
    *
    * @param enclosingQuery the enclosing query
    * @param children the children
    */
   public ParentClause(Query enclosingQuery, List<Clause> children) {
      super(enclosingQuery);
      setChildren(children);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the array of instances of <code>Clause</code> that are children of the ParentClause in the tree used to compute the constructed <code>Query</code>.
    *
    * @return the array of instances of <code>Clause</code> that are children of the ParentClause in the tree used to compute the constructed <code>Query</code>
    */
   @Override
   public List<Clause> getChildren() {
      return this.children;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set array of instances of <code>Clause</code> that are children of the ParentClause in the tree used to compute the constructed <code>Query</code>.
    *
    * @param children the new array of instances of <code>Clause</code> that are children of the ParentClause in the tree used to compute the constructed <code>Query</code>
    */
   public void setChildren(List<Clause> children) {
      this.children = children;

      for (final Clause child: children) {
         child.setParent(this);
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the compute phases.
    *
    * @return the compute phases
    */
   @Override
   public EnumSet<ClauseComputeType> getComputePhases() {
      return PRE_AND_POST_ITERATION;
   }

   /**
    * Gets the query matches.
    *
    * @param conceptVersion the concept version
    * @return the query matches
    */
   @Override
   public final void getQueryMatches(ConceptVersion conceptVersion) {
      this.children.stream().forEach((c) -> {
                               c.getQueryMatches(conceptVersion);
                            });
   }
}

