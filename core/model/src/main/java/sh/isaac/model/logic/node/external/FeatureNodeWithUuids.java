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
 * The Class FeatureNodeWithUuids.
 *
 * @author kec
 */
public class FeatureNodeWithUuids
        extends TypedNodeWithUuids {
   
   /** The concrete domain operators. */
   static ConcreteDomainOperators[] concreteDomainOperators = ConcreteDomainOperators.values();

   //~--- fields --------------------------------------------------------------

   /** The operator. */
   ConcreteDomainOperators operator;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new feature node with uuids.
    *
    * @param internalNode the internal node
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public FeatureNodeWithUuids(FeatureNodeWithSequences internalNode)
            throws IOException {
      super(internalNode);
      this.operator = internalNode.getOperator();

//    unitsConceptUuid = Get.identifierService().getUuidPrimordialForNid(internalNode.getUnitsConceptSequence()).get();
   }

/**
 * Instantiates a new feature node with uuids.
 *
 * @param logicGraphVersion the logic graph version
 * @param dataInputStream the data input stream
 * @throws IOException Signals that an I/O exception has occurred.
 */
// UUID unitsConceptUuid;
   public FeatureNodeWithUuids(LogicalExpressionOchreImpl logicGraphVersion,
                               DataInputStream dataInputStream)
            throws IOException {
      super(logicGraphVersion, dataInputStream);
      this.operator = concreteDomainOperators[dataInputStream.readByte()];

//    unitsConceptUuid = new UUID(dataInputStream.readLong(), dataInputStream.readLong());
   }

   /**
    * Instantiates a new feature node with uuids.
    *
    * @param logicGraphVersion the logic graph version
    * @param typeConceptUuid the type concept uuid
    * @param child the child
    */
   public FeatureNodeWithUuids(LogicalExpressionOchreImpl logicGraphVersion,
                               UUID typeConceptUuid,
                               AbstractLogicNode child) {
      super(logicGraphVersion, typeConceptUuid, child);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Equals.
    *
    * @param o the o
    * @return true, if successful
    */
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

      final FeatureNodeWithUuids that = (FeatureNodeWithUuids) o;

      return this.operator == that.operator;
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      int result = super.hashCode();

      result = 31 * result + this.operator.hashCode();

//    result = 31 * result + unitsConceptUuid.hashCode();
      return result;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return toString("");
   }

   /**
    * To string.
    *
    * @param nodeIdSuffix the node id suffix
    * @return the string
    */
   @Override
   public String toString(String nodeIdSuffix) {
      return "FeatureNode[" + getNodeIndex() + nodeIdSuffix + "] " + this.operator + ", units:"

      // + Get.conceptService().getConcept(unitsConceptUuid).toUserString()
      + super.toString(nodeIdSuffix);
   }

   /**
    * Write node data.
    *
    * @param dataOutput the data output
    * @param dataTarget the data target
    * @throws IOException Signals that an I/O exception has occurred.
    */
   @Override
   public void writeNodeData(DataOutput dataOutput, DataTarget dataTarget)
            throws IOException {
      switch (dataTarget) {
      case EXTERNAL:
         super.writeNodeData(dataOutput, dataTarget);
         dataOutput.writeByte(this.operator.ordinal());

//       dataOutput.writeLong(unitsConceptUuid.getMostSignificantBits());
//       dataOutput.writeLong(unitsConceptUuid.getLeastSignificantBits());
         break;

      case INTERNAL:
         final FeatureNodeWithSequences internalForm = new FeatureNodeWithSequences(this);

         internalForm.writeNodeData(dataOutput, dataTarget);
         break;

      default:
         throw new UnsupportedOperationException("Can't handle dataTarget: " + dataTarget);
      }
   }

// public UUID getUnitsConceptUuid() {
//     return unitsConceptUuid;
/**
 * Compare typed node fields.
 *
 * @param o the o
 * @return the int
 */
// }
   @Override
   protected int compareTypedNodeFields(LogicNode o) {
      // node semantic already determined equals.
      final FeatureNodeWithUuids other = (FeatureNodeWithUuids) o;

      if (!this.typeConceptUuid.equals(other.typeConceptUuid)) {
         return this.typeConceptUuid.compareTo(other.typeConceptUuid);
      }

      return this.operator.compareTo(other.operator);
   }

   /**
    * Inits the node uuid.
    *
    * @return the uuid
    */
   @Override
   protected UUID initNodeUuid() {
      return UuidT5Generator.get(getNodeSemantic().getSemanticUuid(), this.typeConceptUuid.toString() + this.operator

      // + unitsConceptUuid.toString()
      );
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the node semantic.
    *
    * @return the node semantic
    */
   @Override
   public NodeSemantic getNodeSemantic() {
      return NodeSemantic.FEATURE;
   }

   /**
    * Gets the operator.
    *
    * @return the operator
    */
   public ConcreteDomainOperators getOperator() {
      return this.operator;
   }
}

