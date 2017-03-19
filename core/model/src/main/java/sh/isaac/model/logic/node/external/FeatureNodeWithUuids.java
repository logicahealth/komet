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
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.logic.ConcreteDomainOperators;
import sh.isaac.model.logic.LogicalExpressionOchreImpl;
import sh.isaac.model.logic.node.AbstractLogicNode;
import sh.isaac.model.logic.node.internal.FeatureNodeWithSequences;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class FeatureNodeWithUuids
        extends TypedNodeWithUuids {
   static ConcreteDomainOperators[] concreteDomainOperators = ConcreteDomainOperators.values();

   //~--- fields --------------------------------------------------------------

   ConcreteDomainOperators operator;

   //~--- constructors --------------------------------------------------------

   public FeatureNodeWithUuids(FeatureNodeWithSequences internalNode)
            throws IOException {
      super(internalNode);
      operator = internalNode.getOperator();

//    unitsConceptUuid = Get.identifierService().getUuidPrimordialForNid(internalNode.getUnitsConceptSequence()).get();
   }

// UUID unitsConceptUuid;
   public FeatureNodeWithUuids(LogicalExpressionOchreImpl logicGraphVersion,
                               DataInputStream dataInputStream)
            throws IOException {
      super(logicGraphVersion, dataInputStream);
      operator = concreteDomainOperators[dataInputStream.readByte()];

//    unitsConceptUuid = new UUID(dataInputStream.readLong(), dataInputStream.readLong());
   }

   public FeatureNodeWithUuids(LogicalExpressionOchreImpl logicGraphVersion,
                               UUID typeConceptUuid,
                               AbstractLogicNode child) {
      super(logicGraphVersion, typeConceptUuid, child);
   }

   //~--- methods -------------------------------------------------------------

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

      FeatureNodeWithUuids that = (FeatureNodeWithUuids) o;

      return operator == that.operator;
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();

      result = 31 * result + operator.hashCode();

//    result = 31 * result + unitsConceptUuid.hashCode();
      return result;
   }

   @Override
   public String toString() {
      return toString("");
   }

   @Override
   public String toString(String nodeIdSuffix) {
      return "FeatureNode[" + getNodeIndex() + nodeIdSuffix + "] " + operator + ", units:"

      // + Get.conceptService().getConcept(unitsConceptUuid).toUserString()
      + super.toString(nodeIdSuffix);
   }

   @Override
   public void writeNodeData(DataOutput dataOutput, DataTarget dataTarget)
            throws IOException {
      switch (dataTarget) {
      case EXTERNAL:
         super.writeNodeData(dataOutput, dataTarget);
         dataOutput.writeByte(operator.ordinal());

//       dataOutput.writeLong(unitsConceptUuid.getMostSignificantBits());
//       dataOutput.writeLong(unitsConceptUuid.getLeastSignificantBits());
         break;

      case INTERNAL:
         FeatureNodeWithSequences internalForm = new FeatureNodeWithSequences(this);

         internalForm.writeNodeData(dataOutput, dataTarget);
         break;

      default:
         throw new UnsupportedOperationException("Can't handle dataTarget: " + dataTarget);
      }
   }

// public UUID getUnitsConceptUuid() {
//     return unitsConceptUuid;
// }
   @Override
   protected int compareTypedNodeFields(LogicNode o) {
      // node semantic already determined equals.
      FeatureNodeWithUuids other = (FeatureNodeWithUuids) o;

      if (!typeConceptUuid.equals(other.typeConceptUuid)) {
         return typeConceptUuid.compareTo(other.typeConceptUuid);
      }

      return operator.compareTo(other.operator);
   }

   @Override
   protected UUID initNodeUuid() {
      return UuidT5Generator.get(getNodeSemantic().getSemanticUuid(), typeConceptUuid.toString() + operator

      // + unitsConceptUuid.toString()
      );
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public NodeSemantic getNodeSemantic() {
      return NodeSemantic.FEATURE;
   }

   public ConcreteDomainOperators getOperator() {
      return operator;
   }
}

