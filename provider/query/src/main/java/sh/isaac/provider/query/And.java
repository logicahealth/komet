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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.collections.NidSet;

//~--- classes ----------------------------------------------------------------

/**
 * <code>ParentClause</code> that computes the intersection of the set results
 * from the enclosed <code>Clauses</code>.
 *
 * @author kec
 */
@XmlRootElement(name = "and")
@XmlAccessorType(value = XmlAccessType.NONE)
public class And
        extends ParentClause {
   /**
    * Default no arg constructor for Jaxb.
    */
   protected And() {
      super();
   }

   public And(Query enclosingQuery, Clause... clauses) {
      super(enclosingQuery, clauses);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public NidSet computeComponents(NidSet incomingComponents) {
      NidSet results = NidSet.of(incomingComponents.stream());

      for (Clause clause: getChildren()) {
         results.and(clause.computeComponents(incomingComponents));
      }

      return results;
   }

   @Override
   public NidSet computePossibleComponents(NidSet incomingPossibleComponents) {
      NidSet results = NidSet.of(incomingPossibleComponents.stream());

      getChildren().stream().forEach((clause) -> {
                               results.and(clause.computePossibleComponents(incomingPossibleComponents));
                            });
      return results;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public WhereClause getWhereClause() {
      WhereClause whereClause = new WhereClause();

      whereClause.setSemantic(ClauseSemantic.AND);

      for (Clause clause: getChildren()) {
         whereClause.getChildren()
                    .add(clause.getWhereClause());
      }

      return whereClause;
   }
}

