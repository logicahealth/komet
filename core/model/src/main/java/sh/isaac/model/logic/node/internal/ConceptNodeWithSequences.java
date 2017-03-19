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



package sh.isaac.model.logic.node.internal;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;

import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.logic.LogicalExpressionOchreImpl;
import sh.isaac.model.logic.node.AbstractLogicNode;
import sh.isaac.model.logic.node.external.ConceptNodeWithUuids;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 12/10/14.
 */
public final class ConceptNodeWithSequences
        extends AbstractLogicNode {
   int conceptSequence;

   //~--- constructors --------------------------------------------------------

   public ConceptNodeWithSequences(ConceptNodeWithUuids externalForm) {
      super(externalForm);
      this.conceptSequence = Get.identifierService()
                                .getConceptSequenceForUuids(externalForm.getConceptUuid());
   }

   public ConceptNodeWithSequences(LogicalExpressionOchreImpl logicGraphVersion,
                                   DataInputStream dataInputStream)
            throws IOException {
      super(logicGraphVersion, dataInputStream);
      this.conceptSequence = dataInputStream.readInt();
   }

   public ConceptNodeWithSequences(LogicalExpressionOchreImpl logicGraphVersion, int conceptId) {
      super(logicGraphVersion);
      this.conceptSequence = Get.identifierService()
                                .getConceptSequence(conceptId);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public final void addChildren(LogicNode... children) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void addConceptsReferencedByNode(ConceptSequenceSet conceptSequenceSet) {
      super.addConceptsReferencedByNode(conceptSequenceSet);
      conceptSequenceSet.add(this.conceptSequence);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }

      if ((o == null) || (getClass() != o.getClass())) {
         return false;
      }

      if (!super.equals(o)) {
         return false;
      }

      final ConceptNodeWithSequences that = (ConceptNodeWithSequences) o;

      return this.conceptSequence == that.conceptSequence;
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();

      result = 31 * result + this.conceptSequence;
      return result;
   }

   @Override
   public String toString() {
      return toString("");
   }

   @Override
   public String toString(String nodeIdSuffix) {
      return "Concept[" + getNodeIndex() + nodeIdSuffix + "] " + Get.conceptDescriptionText(this.conceptSequence) + " <" +
             Get.identifierService().getConceptSequence(this.conceptSequence) + ">" + super.toString(nodeIdSuffix);
   }

   @Override
   public void writeNodeData(DataOutput dataOutput, DataTarget dataTarget)
            throws IOException {
      switch (dataTarget) {
      case EXTERNAL:
         final ConceptNodeWithUuids externalForm = new ConceptNodeWithUuids(this);

         externalForm.writeNodeData(dataOutput, dataTarget);
         break;

      case INTERNAL:
         super.writeData(dataOutput, dataTarget);
         dataOutput.writeInt(this.conceptSequence);
         break;

      default:
         throw new UnsupportedOperationException("Can't handle dataTarget: " + dataTarget);
      }
   }

   @Override
   protected int compareFields(LogicNode o) {
      return Integer.compare(this.conceptSequence, ((ConceptNodeWithSequences) o).getConceptSequence());
   }

   @Override
   protected UUID initNodeUuid() {
      return UuidT5Generator.get(getNodeSemantic().getSemanticUuid(),
                                 Get.identifierService()
                                    .getUuidPrimordialFromConceptId(this.conceptSequence)
                                    .get()
                                    .toString());
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public AbstractLogicNode[] getChildren() {
      return new AbstractLogicNode[0];
   }

   public int getConceptSequence() {
      return this.conceptSequence;
   }

   @Override
   public NodeSemantic getNodeSemantic() {
      return NodeSemantic.CONCEPT;
   }
}

