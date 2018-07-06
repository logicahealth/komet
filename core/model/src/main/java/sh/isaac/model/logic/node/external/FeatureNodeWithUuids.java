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


import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.logic.ConcreteDomainOperators;
import sh.isaac.model.logic.LogicalExpressionImpl;
import sh.isaac.model.logic.node.AbstractLogicNode;
import sh.isaac.model.logic.node.internal.FeatureNodeWithNids;

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
   UUID measureSemanticUuid;
   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new feature node with uuids.
    *
    * @param internalNode the internal node
    */
   public FeatureNodeWithUuids(FeatureNodeWithNids internalNode) {
      super(internalNode);
      this.operator = internalNode.getOperator();
      this.measureSemanticUuid = Get.identifierService().getUuidPrimordialForNid(internalNode.getMeasureSemanticNid());
   }

   /**
    * Instantiates a new feature node with uuids.
    *
    * @param logicGraphVersion the logic graph version
    * @param dataInputStream the data input stream
    */

   public FeatureNodeWithUuids(LogicalExpressionImpl logicGraphVersion,
                               ByteArrayDataBuffer dataInputStream) {
      super(logicGraphVersion, dataInputStream);
      this.operator = concreteDomainOperators[dataInputStream.getByte()];
      this.measureSemanticUuid = new UUID(dataInputStream.getLong(), dataInputStream.getLong());
   }

   /**
    * Instantiates a new feature node with uuids.
    *
    * @param logicGraphVersion the logic graph version
    * @param typeConceptUuid the type concept uuid
    * @param child the child
    */
   public FeatureNodeWithUuids(LogicalExpressionImpl logicGraphVersion,
                               UUID typeConceptUuid,
                               AbstractLogicNode child) {
      super(logicGraphVersion, typeConceptUuid, child);
   }

   //~--- methods -------------------------------------------------------------

   public UUID getMeasureSemanticUuid() {
      return measureSemanticUuid;
   }

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
      result = 31 * result + this.measureSemanticUuid.hashCode();
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

      + Get.concept(measureSemanticUuid).toUserString() + " <" +
              this.measureSemanticUuid + ">"
      + super.toString(nodeIdSuffix);
   }
   @Override
   public String toSimpleString() {
      return toString("");
   }
    @Override
    public void addToBuilder(StringBuilder builder) {
        builder.append("\n       Feature(");
        builder.append("Get.conceptSpecification(").append(this.measureSemanticUuid).append("), ");
        builder.append(operator);
        builder.append(")\n");
    }


   /**
    * Write node data.
    *
    * @param dataOutput the data output
    * @param dataTarget the data target
    */
   @Override
   public void writeNodeData(ByteArrayDataBuffer dataOutput, DataTarget dataTarget) {
      switch (dataTarget) {
      case EXTERNAL:
         super.writeNodeData(dataOutput, dataTarget);
         dataOutput.putByte((byte) this.operator.ordinal());

        dataOutput.putLong(this.measureSemanticUuid.getMostSignificantBits());
        dataOutput.putLong(this.measureSemanticUuid.getLeastSignificantBits());
         break;

      case INTERNAL:
         final FeatureNodeWithNids internalForm = new FeatureNodeWithNids(this);

         internalForm.writeNodeData(dataOutput, dataTarget);
         break;

      default:
         throw new UnsupportedOperationException("Can't handle dataTarget: " + dataTarget);
      }
   }


   /**
    * Compare typed node fields.
    *
    * @param o the o
    * @return the int
    */
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

