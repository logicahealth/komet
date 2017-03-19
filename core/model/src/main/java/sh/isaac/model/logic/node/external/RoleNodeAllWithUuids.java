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
import sh.isaac.model.logic.LogicalExpressionOchreImpl;
import sh.isaac.model.logic.node.AbstractLogicNode;
import sh.isaac.model.logic.node.internal.RoleNodeAllWithSequences;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class RoleNodeAllWithUuids
        extends TypedNodeWithUuids {
   public RoleNodeAllWithUuids(RoleNodeAllWithSequences internalFrom) {
      super(internalFrom);
   }

   public RoleNodeAllWithUuids(LogicalExpressionOchreImpl logicGraphVersion,
                               DataInputStream dataInputStream)
            throws IOException {
      super(logicGraphVersion, dataInputStream);
   }

   public RoleNodeAllWithUuids(LogicalExpressionOchreImpl logicGraphVersion,
                               UUID typeConceptUuid,
                               AbstractLogicNode child) {
      super(logicGraphVersion, typeConceptUuid, child);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public String toString() {
      return "RoleNodeAll[" + getNodeIndex() + "]:" + super.toString();
   }

   @Override
   public String toString(String nodeIdSuffix) {
      return "RoleNodeAll[" + getNodeIndex() + nodeIdSuffix + "]" + super.toString(nodeIdSuffix);
   }

   @Override
   public void writeNodeData(DataOutput dataOutput, DataTarget dataTarget)
            throws IOException {
      super.writeNodeData(dataOutput, dataTarget);
   }

   @Override
   protected int compareTypedNodeFields(LogicNode o) {
      // node semantic already determined equals.
      return 0;
   }

   @Override
   protected UUID initNodeUuid() {
      return UuidT5Generator.get(getNodeSemantic().getSemanticUuid(), this.typeConceptUuid.toString());
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public NodeSemantic getNodeSemantic() {
      return NodeSemantic.ROLE_ALL;
   }
}

