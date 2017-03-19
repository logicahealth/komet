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
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.logic.LogicalExpressionOchreImpl;
import sh.isaac.model.logic.node.AbstractLogicNode;
import sh.isaac.model.logic.node.external.RoleNodeAllWithUuids;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 12/6/14.
 */
public final class RoleNodeAllWithSequences
        extends TypedNodeWithSequences {
   /**
    * Instantiates a new role node all with sequences.
    *
    * @param externalForm the external form
    */
   public RoleNodeAllWithSequences(RoleNodeAllWithUuids externalForm) {
      super(externalForm);
   }

   /**
    * Instantiates a new role node all with sequences.
    *
    * @param logicGraphVersion the logic graph version
    * @param dataInputStream the data input stream
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public RoleNodeAllWithSequences(LogicalExpressionOchreImpl logicGraphVersion,
                                   DataInputStream dataInputStream)
            throws IOException {
      super(logicGraphVersion, dataInputStream);
   }

   /**
    * Instantiates a new role node all with sequences.
    *
    * @param logicGraphVersion the logic graph version
    * @param typeConceptId the type concept id
    * @param child the child
    */
   public RoleNodeAllWithSequences(LogicalExpressionOchreImpl logicGraphVersion,
                                   int typeConceptId,
                                   AbstractLogicNode child) {
      super(logicGraphVersion, typeConceptId, child);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "All[" + getNodeIndex() + "]" + super.toString();
   }

   /**
    * To string.
    *
    * @param nodeIdSuffix the node id suffix
    * @return the string
    */
   @Override
   public String toString(String nodeIdSuffix) {
      return "All[" + getNodeIndex() + nodeIdSuffix + "]" + super.toString(nodeIdSuffix);
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
         final RoleNodeAllWithUuids externalForm = new RoleNodeAllWithUuids(this);

         externalForm.writeNodeData(dataOutput, dataTarget);
         break;

      case INTERNAL:
         super.writeNodeData(dataOutput, dataTarget);
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
      return 0;
   }

   /**
    * Inits the node uuid.
    *
    * @return the uuid
    */
   @Override
   protected UUID initNodeUuid() {
      return UuidT5Generator.get(getNodeSemantic().getSemanticUuid(),
                                 Get.identifierService()
                                    .getUuidPrimordialFromConceptId(this.typeConceptSequence)
                                    .get()
                                    .toString());
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the node semantic.
    *
    * @return the node semantic
    */
   @Override
   public NodeSemantic getNodeSemantic() {
      return NodeSemantic.ROLE_ALL;
   }
}

