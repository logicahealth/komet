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

import java.util.Arrays;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.model.logic.LogicalExpressionOchreImpl;
import sh.isaac.model.logic.node.AbstractLogicNode;
import sh.isaac.model.logic.node.ConnectorNode;
import sh.isaac.model.logic.node.internal.FeatureNodeWithSequences;
import sh.isaac.model.logic.node.internal.RoleNodeAllWithSequences;
import sh.isaac.model.logic.node.internal.RoleNodeSomeWithSequences;
import sh.isaac.model.logic.node.internal.TypedNodeWithSequences;

//~--- classes ----------------------------------------------------------------

/**
 * The Class TypedNodeWithUuids.
 *
 * @author kec
 */
public abstract class TypedNodeWithUuids
        extends ConnectorNode {
   /** The type concept uuid. */
   UUID typeConceptUuid;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new typed node with uuids.
    *
    * @param internalForm the internal form
    */
   public TypedNodeWithUuids(TypedNodeWithSequences internalForm) {
      super(internalForm);
      this.typeConceptUuid = Get.identifierService()
                                .getUuidPrimordialFromConceptId(internalForm.getTypeConceptSequence())
                                .get();
   }

   /**
    * Instantiates a new typed node with uuids.
    *
    * @param logicGraphVersion the logic graph version
    * @param dataInputStream the data input stream
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public TypedNodeWithUuids(LogicalExpressionOchreImpl logicGraphVersion,
                             DataInputStream dataInputStream)
            throws IOException {
      super(logicGraphVersion, dataInputStream);
      this.typeConceptUuid = new UUID(dataInputStream.readLong(), dataInputStream.readLong());
   }

   /**
    * Instantiates a new typed node with uuids.
    *
    * @param logicGraphVersion the logic graph version
    * @param typeConceptUuid the type concept uuid
    * @param child the child
    */
   public TypedNodeWithUuids(LogicalExpressionOchreImpl logicGraphVersion,
                             UUID typeConceptUuid,
                             AbstractLogicNode child) {
      super(logicGraphVersion, child);
      this.typeConceptUuid = typeConceptUuid;
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
      if (this == obj) {
         return true;
      }

      if (obj == null) {
         return false;
      }

      if (getClass() != obj.getClass()) {
         return false;
      }

      final TypedNodeWithUuids other = (TypedNodeWithUuids) obj;

      if (!this.typeConceptUuid.equals(other.typeConceptUuid)) {
         return false;
      }

      return super.equals(obj);
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      int hash = super.hashCode();

      hash = 31 * hash + this.typeConceptUuid.hashCode();
      return hash;
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
      return " " + Get.conceptService().getConcept(this.typeConceptUuid).toUserString();
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
         super.writeData(dataOutput, dataTarget);
         dataOutput.writeLong(this.typeConceptUuid.getMostSignificantBits());
         dataOutput.writeLong(this.typeConceptUuid.getLeastSignificantBits());
         break;

      case INTERNAL:
         TypedNodeWithSequences internalForm = null;

         if (this instanceof FeatureNodeWithUuids) {
            internalForm = new FeatureNodeWithSequences((FeatureNodeWithUuids) this);
            ((FeatureNodeWithSequences) internalForm).writeNodeData(dataOutput, dataTarget);
         } else if (this instanceof RoleNodeAllWithUuids) {
            internalForm = new RoleNodeAllWithSequences((RoleNodeAllWithUuids) this);
            ((RoleNodeAllWithSequences) internalForm).writeNodeData(dataOutput, dataTarget);
         } else if (this instanceof RoleNodeSomeWithUuids) {
            internalForm = new RoleNodeSomeWithSequences((RoleNodeSomeWithUuids) this);
            ((RoleNodeSomeWithSequences) internalForm).writeNodeData(dataOutput, dataTarget);
         } else {
            throw new RuntimeException("Can't write internal form!");
         }

         break;

      default:
         throw new UnsupportedOperationException("Can't handle dataTarget: " + dataTarget);
      }
   }

   /**
    * Compare node fields.
    *
    * @param o the o
    * @return the int
    */
   @Override
   protected final int compareNodeFields(LogicNode o) {
      // node semantic already determined equals.
      final TypedNodeWithUuids other = (TypedNodeWithUuids) o;

      if (!this.typeConceptUuid.equals(other.typeConceptUuid)) {
         return this.typeConceptUuid.compareTo(other.typeConceptUuid);
      }

      return compareTypedNodeFields(o);
   }

   /**
    * Compare typed node fields.
    *
    * @param o the o
    * @return the int
    */
   protected abstract int compareTypedNodeFields(LogicNode o);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the only child.
    *
    * @return the only child
    */
   public LogicNode getOnlyChild() {
      final LogicNode[] children = getChildren();

      if (children.length == 1) {
         return children[0];
      }

      throw new IllegalStateException("Typed nodes can have only one child. Found: " + Arrays.toString(children));
   }

   /**
    * Gets the type concept uuid.
    *
    * @return the type concept uuid
    */
   public UUID getTypeConceptUuid() {
      return this.typeConceptUuid;
   }
}

