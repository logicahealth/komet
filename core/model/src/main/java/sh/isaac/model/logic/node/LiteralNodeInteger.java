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

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 12/9/14.
 */
public class LiteralNodeInteger
        extends LiteralNode {
   int literalValue;

   //~--- constructors --------------------------------------------------------

   public LiteralNodeInteger(LogicalExpressionOchreImpl logicGraphVersion,
                             DataInputStream dataInputStream)
            throws IOException {
      super(logicGraphVersion, dataInputStream);
      literalValue = dataInputStream.readInt();
   }

   public LiteralNodeInteger(LogicalExpressionOchreImpl logicGraphVersion, int literalValue) {
      super(logicGraphVersion);
      this.literalValue = literalValue;
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

      LiteralNodeInteger that = (LiteralNodeInteger) o;

      return literalValue == that.literalValue;
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();

      result = 31 * result + literalValue;
      return result;
   }

   @Override
   public String toString() {
      return toString("");
   }

   @Override
   public String toString(String nodeIdSuffix) {
      return "Integer literal[" + getNodeIndex() + nodeIdSuffix + "]" + literalValue + super.toString(nodeIdSuffix);
   }

   @Override
   protected int compareFields(LogicNode o) {
      LiteralNodeInteger that = (LiteralNodeInteger) o;

      return that.literalValue - this.literalValue;
   }

   @Override
   protected UUID initNodeUuid() {
      return UuidT5Generator.get(getNodeSemantic().getSemanticUuid(), Integer.toString(literalValue));
   }

   @Override
   protected void writeNodeData(DataOutput dataOutput, DataTarget dataTarget)
            throws IOException {
      super.writeData(dataOutput, dataTarget);
      dataOutput.writeInt(literalValue);
   }

   //~--- get methods ---------------------------------------------------------

   public int getLiteralValue() {
      return literalValue;
   }

   @Override
   public NodeSemantic getNodeSemantic() {
      return NodeSemantic.LITERAL_INTEGER;
   }
}

