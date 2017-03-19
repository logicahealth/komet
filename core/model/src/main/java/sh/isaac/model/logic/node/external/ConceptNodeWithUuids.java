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



/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package sh.isaac.model.logic.node.external;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;

import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.logic.LogicalExpressionOchreImpl;
import sh.isaac.model.logic.node.AbstractLogicNode;
import sh.isaac.model.logic.node.internal.ConceptNodeWithSequences;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class ConceptNodeWithUuids
        extends AbstractLogicNode {
   UUID conceptUuid;

   //~--- constructors --------------------------------------------------------

   public ConceptNodeWithUuids(ConceptNodeWithSequences internalForm) {
      super(internalForm);
      this.conceptUuid = Get.identifierService()
                            .getUuidPrimordialFromConceptId(internalForm.getConceptSequence())
                            .get();
   }

   public ConceptNodeWithUuids(LogicalExpressionOchreImpl logicGraphVersion,
                               DataInputStream dataInputStream)
            throws IOException {
      super(logicGraphVersion, dataInputStream);
      conceptUuid = new UUID(dataInputStream.readLong(), dataInputStream.readLong());
   }

   public ConceptNodeWithUuids(LogicalExpressionOchreImpl logicGraphVersion, UUID conceptUuid) {
      super(logicGraphVersion);
      this.conceptUuid = conceptUuid;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public final void addChildren(LogicNode... children) {
      throw new UnsupportedOperationException();
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

      ConceptNodeWithUuids that = (ConceptNodeWithUuids) o;

      return conceptUuid.equals(that.conceptUuid);
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();

      result = 31 * result + conceptUuid.hashCode();
      return result;
   }

   @Override
   public String toString() {
      return toString("");
   }

   @Override
   public String toString(String nodeIdSuffix) {
      return "ConceptNode[" + getNodeIndex() + nodeIdSuffix + "] \"" +
             Get.conceptService().getConcept(conceptUuid).toUserString() + "\"" + super.toString(nodeIdSuffix);
   }

   @Override
   public void writeNodeData(DataOutput dataOutput, DataTarget dataTarget)
            throws IOException {
      switch (dataTarget) {
      case EXTERNAL:
         dataOutput.writeLong(conceptUuid.getMostSignificantBits());
         dataOutput.writeLong(conceptUuid.getLeastSignificantBits());
         break;

      case INTERNAL:
         ConceptNodeWithSequences internalForm = new ConceptNodeWithSequences(this);

         internalForm.writeNodeData(dataOutput, dataTarget);
         break;

      default:
         throw new UnsupportedOperationException("Can't handle dataTarget: " + dataTarget);
      }
   }

   @Override
   protected int compareFields(LogicNode o) {
      return conceptUuid.compareTo(((ConceptNodeWithUuids) o).conceptUuid);
   }

   @Override
   protected UUID initNodeUuid() {
      return UuidT5Generator.get(getNodeSemantic().getSemanticUuid(), conceptUuid.toString());
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public AbstractLogicNode[] getChildren() {
      return new AbstractLogicNode[0];
   }

   public UUID getConceptUuid() {
      return conceptUuid;
   }

   @Override
   public NodeSemantic getNodeSemantic() {
      return NodeSemantic.CONCEPT;
   }
}

