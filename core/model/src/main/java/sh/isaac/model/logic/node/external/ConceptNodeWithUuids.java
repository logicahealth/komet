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

import java.io.IOException;

import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.logic.LogicalExpressionImpl;
import sh.isaac.model.logic.node.AbstractLogicNode;
import sh.isaac.model.logic.node.internal.ConceptNodeWithNids;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ConceptNodeWithUuids.
 *
 * @author kec
 */
public class ConceptNodeWithUuids
        extends AbstractLogicNode {
   /** The concept uuid. */
   UUID conceptUuid;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new concept node with uuids.
    *
    * @param internalForm the internal form
    */
   public ConceptNodeWithUuids(ConceptNodeWithNids internalForm) {
      super(internalForm);
      this.conceptUuid = Get.identifierService()
                            .getUuidPrimordialForNid(internalForm.getConceptNid());
   }

   /**
    * Instantiates a new concept node with uuids.
    *
    * @param logicGraphVersion the logic graph version
    * @param dataInputStream the data input stream
    */
   public ConceptNodeWithUuids(LogicalExpressionImpl logicGraphVersion,
                               ByteArrayDataBuffer dataInputStream) {
      super(logicGraphVersion, dataInputStream);
      this.conceptUuid = new UUID(dataInputStream.getLong(), dataInputStream.getLong());
      Get.identifierService().assignNid(this.conceptUuid);
   }

   /**
    * Instantiates a new concept node with uuids.
    *
    * @param logicGraphVersion the logic graph version
    * @param conceptUuid the concept uuid
    */
   public ConceptNodeWithUuids(LogicalExpressionImpl logicGraphVersion, UUID conceptUuid) {
      super(logicGraphVersion);
      this.conceptUuid = conceptUuid;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the children.
    *
    * @param children the children
    */
   @Override
   public final void addChildren(LogicNode... children) {
      throw new UnsupportedOperationException();
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

      final ConceptNodeWithUuids that = (ConceptNodeWithUuids) o;

      return this.conceptUuid.equals(that.conceptUuid);
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      int result = super.hashCode();

      result = 31 * result + this.conceptUuid.hashCode();
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
      return "ConceptNode[" + getNodeIndex() + nodeIdSuffix + "] \"" +
             Get.conceptService().getConceptChronology(this.conceptUuid).toUserString() + "\"" + super.toString(nodeIdSuffix);
   }
   @Override
   public String toSimpleString() {
      return toString("");
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
         dataOutput.putLong(this.conceptUuid.getMostSignificantBits());
         dataOutput.putLong(this.conceptUuid.getLeastSignificantBits());
         break;

      case INTERNAL:
         final ConceptNodeWithNids internalForm = new ConceptNodeWithNids(this);

         internalForm.writeNodeData(dataOutput, dataTarget);
         break;

      default:
         throw new UnsupportedOperationException("Can't handle dataTarget: " + dataTarget);
      }
   }

   /**
    * Compare fields.
    *
    * @param o the o
    * @return the int
    */
   @Override
   protected int compareFields(LogicNode o) {
      return this.conceptUuid.compareTo(((ConceptNodeWithUuids) o).conceptUuid);
   }

   /**
    * Inits the node uuid.
    *
    * @return the uuid
    */
   @Override
   protected UUID initNodeUuid() {
      return UuidT5Generator.get(getNodeSemantic().getSemanticUuid(), this.conceptUuid.toString());
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the children.
    *
    * @return the children
    */
   @Override
   public final AbstractLogicNode[] getChildren() {
      return new AbstractLogicNode[0];
   }

    @Override
    public void removeChild(short childId) {
        // nothing to do
    }

   /**
    * Gets the concept uuid.
    *
    * @return the concept uuid
    */
   public UUID getConceptUuid() {
      return this.conceptUuid;
   }

   /**
    * Gets the node semantic.
    *
    * @return the node semantic
    */
   @Override
   public NodeSemantic getNodeSemantic() {
      return NodeSemantic.CONCEPT;
   }
}

