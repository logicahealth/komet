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



package sh.isaac.model.logic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.Get;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.model.logic.node.AbstractLogicNode;
import sh.isaac.model.logic.node.AndNode;
import sh.isaac.model.logic.node.external.ConceptNodeWithUuids;
import sh.isaac.model.logic.node.internal.ConceptNodeWithNids;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Class LogicalExpressionAbstract.
 *
 * @author kec
 */
public abstract class LogicalExpressionAbstract
        extends LogicalExpressionImpl {
   private static Logger LOG = LogManager.getLogger();

   public static Set<Integer> getParentConceptNids(LogicalExpression expression)
   {
      Set<Integer> parentConceptSequences = new HashSet<>();
      List<LogicNode> necessarySets = expression.getNodesOfType(NodeSemantic.NECESSARY_SET);
      for (LogicNode necessarySetNode : necessarySets)
      {
         for (LogicNode childOfNecessarySetNode : necessarySetNode.getChildren())
         {
            if (null == childOfNecessarySetNode.getNodeSemantic())
            {
               String msg = "Logic graph for concept NID=" + expression.getConceptBeingDefinedNid()
                       + " has child of NecessarySet logic graph node of unexpected type \"" + childOfNecessarySetNode.getNodeSemantic()
                       + "\". Expected AndNode or ConceptNode in " + expression;
               LOG.error(msg);
               throw new RuntimeException(msg);
            }
            else
               switch (childOfNecessarySetNode.getNodeSemantic())
               {
                  case AND:
                     AndNode andNode = (AndNode) childOfNecessarySetNode;
                     for (AbstractLogicNode childOfAndNode : andNode.getChildren())
                     {
                        if (childOfAndNode.getNodeSemantic() == NodeSemantic.CONCEPT)
                        {
                           if (childOfAndNode instanceof ConceptNodeWithNids)
                           {
                              ConceptNodeWithNids conceptNode = (ConceptNodeWithNids) childOfAndNode;
                              parentConceptSequences.add(conceptNode.getConceptNid());
                           }
                           else if (childOfAndNode instanceof ConceptNodeWithUuids)
                           {
                              ConceptNodeWithUuids conceptNode = (ConceptNodeWithUuids) childOfAndNode;
                              parentConceptSequences.add(Get.identifierService().getNidForUuids(conceptNode.getConceptUuid()));
                           }
                           else
                           {
                              // Should never happen
                              String msg = "Logic graph for concept NID=" + expression.getConceptBeingDefinedNid()
                                      + " has child of AndNode logic graph node of unexpected type \"" + childOfAndNode.getClass().getSimpleName()
                                      + "\". Expected ConceptNodeWithNids or ConceptNodeWithUuids in " + expression;
                              LOG.error(msg);
                              throw new RuntimeException(msg);
                           }
                        }
                     }
                     break;
                  case CONCEPT:
                     if (childOfNecessarySetNode instanceof ConceptNodeWithNids)
                     {
                        ConceptNodeWithNids conceptNode = (ConceptNodeWithNids) childOfNecessarySetNode;
                        parentConceptSequences.add(conceptNode.getConceptNid());
                     }
                     else if (childOfNecessarySetNode instanceof ConceptNodeWithUuids)
                     {
                        ConceptNodeWithUuids conceptNode = (ConceptNodeWithUuids) childOfNecessarySetNode;
                        parentConceptSequences.add(Get.identifierService().getNidForUuids(conceptNode.getConceptUuid()));
                     }
                     else
                     {
                        // Should never happen
                        String msg = "Logic graph for concept NID=" + expression.getConceptBeingDefinedNid()
                                + " has child of NecessarySet logic graph node of unexpected type \""
                                + childOfNecessarySetNode.getClass().getSimpleName() + "\". Expected ConceptNodeWithNids or ConceptNodeWithUuids in "
                                + expression;
                        LOG.error(msg);
                        throw new RuntimeException(msg);
                     }
                     break;
                  default :
                     String msg = "Logic graph for concept NID=" + expression.getConceptBeingDefinedNid()
                             + " has child of NecessarySet logic graph node of unexpected type \"" + childOfNecessarySetNode.getNodeSemantic()
                             + "\". Expected AndNode or ConceptNode in " + expression;
                     LOG.error(msg);
                     throw new RuntimeException(msg);
               }
         }
      }

      return parentConceptSequences;
   }
   /**
    * Retrieve the set of integer parent concept nids stored in the logic graph necessary sets
    *
    * @return the parents
    */
   public Set<Integer> getParentConceptNids()
   {
      return getParentConceptNids(this);
   }
   /**
    * Instantiates a new logical expression abstract.
    */
   public LogicalExpressionAbstract() {
      super();
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Creates the.
    */
   public abstract void create();

   /**
    * Inits the.
    */
   @Override
   public final void init() {
      create();
      super.init();
   }
}

