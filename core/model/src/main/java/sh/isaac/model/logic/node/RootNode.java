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



package sh.isaac.model.logic.node;

//~--- JDK imports ------------------------------------------------------------


import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.DataTarget;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.model.logic.LogicalExpressionImpl;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 12/9/14.
 */
public class RootNode
        extends ConnectorNode {
   /**
    * Instantiates a new root node.
    *
    * @param logicGraphVersion the logic graph version
    * @param children the children
    */
   public RootNode(LogicalExpressionImpl logicGraphVersion, ConnectorNode... children) {
      super(logicGraphVersion, children);
   }

   /**
    * Instantiates a new root node.
    *
    * @param logicGraphVersion the logic graph version
    * @param dataInputStream the data input stream
    */
   public RootNode(LogicalExpressionImpl logicGraphVersion, ByteArrayDataBuffer dataInputStream) {
      super(logicGraphVersion, dataInputStream);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return toString("");
   }

    @Override
    public void addToBuilder(StringBuilder builder) {
        
        for (AbstractLogicNode child: getChildren()) {
            builder.append("\n");
            child.addToBuilder(builder);
            builder.append(";\n");
        }
    }

   /**
    * To string.
    *
    * @param nodeIdSuffix the node id suffix
    * @return the string
    */
   @Override
   public String toString(String nodeIdSuffix) {
      return "Root[" + getNodeIndex() + nodeIdSuffix + "]" + super.toString(nodeIdSuffix);
   }
   @Override
   public String toSimpleString() {
      return "";
   }


   /**
    * Compare node fields.
    *
    * @param o the o
    * @return the int
    */
   @Override
   protected int compareNodeFields(LogicNode o) {
      // no fields to compare, node semantic already determined equals.
      return 0;
   }

   /**
    * Inits the node uuid.
    *
    * @return the uuid
    */
   @Override
   protected UUID initNodeUuid() {
      return getNodeSemantic().getSemanticUuid();
   }

   /**
    * Write node data.
    *
    * @param dataOutput the data output
    * @param dataTarget the data target
    */
   @Override
   protected void writeNodeData(ByteArrayDataBuffer dataOutput, DataTarget dataTarget) {
      writeData(dataOutput, dataTarget);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the node semantic.
    *
    * @return the node semantic
    */
   @Override
   public NodeSemantic getNodeSemantic() {
      return NodeSemantic.DEFINITION_ROOT;
   }
}

