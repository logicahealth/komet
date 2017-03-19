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

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.list.ShortArrayList;

import sh.isaac.api.DataTarget;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.model.logic.LogicalExpressionOchreImpl;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 12/6/14.
 */
public abstract class ConnectorNode
        extends AbstractLogicNode {
   private final ShortArrayList childIndices;

   //~--- constructors --------------------------------------------------------

   public ConnectorNode(AbstractLogicNode another) {
      super(another);
      childIndices = new ShortArrayList(another.getChildren().length);

      for (AbstractLogicNode child: another.getChildren()) {
         childIndices.add(child.getNodeIndex());
      }
   }

   public ConnectorNode(LogicalExpressionOchreImpl logicGraphVersion, AbstractLogicNode... children) {
      super(logicGraphVersion);
      childIndices = new ShortArrayList(children.length);

      for (AbstractLogicNode child: children) {
         childIndices.add(child.getNodeIndex());
      }
   }

   public ConnectorNode(LogicalExpressionOchreImpl logicGraphVersion,
                        DataInputStream dataInputStream)
            throws IOException {
      super(logicGraphVersion, dataInputStream);

      short childrenSize = dataInputStream.readShort();

      childIndices = new ShortArrayList(childrenSize);

      for (int index = 0; index < childrenSize; index++) {
         childIndices.add(dataInputStream.readShort());
      }
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void addChildren(LogicNode... children) {
      for (LogicNode child: children) {
         childIndices.add(child.getNodeIndex());
      }

      sort();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }

      if ((o == null) || (getClass() != o.getClass())) {
         return false;
      }

      return super.equals(o);
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();

      result = 31 * result + childIndices.hashCode();
      return result;
   }

   @Override
   public final void sort() {
      childIndices.mergeSortFromTo(0,
                                   childIndices.size() - 1,
                                   (short o1,
                                    short o2) -> logicGraphVersion.getNode(o1)
                                          .compareTo(logicGraphVersion.getNode(o2)));
   }

   @Override
   public String toString() {
      return toString("");
   }

   @Override
   public String toString(String nodeIdSuffix) {
      if ((childIndices != null) &&!childIndices.isEmpty()) {
         StringBuilder builder = new StringBuilder();

         builder.append("➞[");
         childIndices.forEach((index) -> {
                                 builder.append(index);
                                 builder.append(nodeIdSuffix);
                                 builder.append(", ");
                                 return true;
                              });
         builder.deleteCharAt(builder.length() - 1);
         builder.deleteCharAt(builder.length() - 1);
         builder.append("]");
         return builder.toString();
      }

      return "";
   }

   @Override
   protected int compareFields(LogicNode o) {
      // node semantic is already determined to be the same...
      return compareNodeFields(o);
   }

   protected abstract int compareNodeFields(LogicNode o);

   @Override
   protected void writeData(DataOutput dataOutput, DataTarget dataTarget)
            throws IOException {
      sort();
      super.writeData(dataOutput, dataTarget);
      dataOutput.writeShort(childIndices.size());

      for (short value: childIndices.elements()) {
         dataOutput.writeShort(value);
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    *
    * @return a sorted array of child <code>LogicNode</code>s.
    */
   @Override
   public AbstractLogicNode[] getChildren() {
      AbstractLogicNode[] childNodes = new AbstractLogicNode[childIndices.size()];

      for (int i = 0; i < childNodes.length; i++) {
         childNodes[i] = (AbstractLogicNode) logicGraphVersion.getNode(childIndices.get(i));
      }

      return childNodes;
   }
}

