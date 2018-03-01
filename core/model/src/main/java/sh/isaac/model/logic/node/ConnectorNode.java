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

import java.io.IOException;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.list.ShortArrayList;

import sh.isaac.api.DataTarget;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.model.logic.LogicalExpressionImpl;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 12/6/14.
 */
public abstract class ConnectorNode
        extends AbstractLogicNode {
   /** The child indices. */
   private final ShortArrayList childIndices;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new connector node.
    *
    * @param another the another
    */
   public ConnectorNode(AbstractLogicNode another) {
      super(another);
      this.childIndices = new ShortArrayList(another.getChildren().length);

      for (final AbstractLogicNode child: another.getChildren()) {
         this.childIndices.add(child.getNodeIndex());
      }
   }

   /**
    * Instantiates a new connector node.
    *
    * @param logicGraphVersion the logic graph version
    * @param children the children
    */
   public ConnectorNode(LogicalExpressionImpl logicGraphVersion, AbstractLogicNode... children) {
      super(logicGraphVersion);
      this.childIndices = new ShortArrayList(children.length);

      for (final AbstractLogicNode child: children) {
         this.childIndices.add(child.getNodeIndex());
      }
   }

   /**
    * Instantiates a new connector node.
    *
    * @param logicGraphVersion the logic graph version
    * @param dataInputStream the data input stream
    */
   public ConnectorNode(LogicalExpressionImpl logicGraphVersion,
                        ByteArrayDataBuffer dataInputStream)  {
      super(logicGraphVersion, dataInputStream);

      final short childrenSize = dataInputStream.getShort();

      this.childIndices = new ShortArrayList(childrenSize);

      for (int index = 0; index < childrenSize; index++) {
         this.childIndices.add(dataInputStream.getShort());
      }
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the children.
    *
    * @param children the children
    */
   @Override
   public void addChildren(LogicNode... children) {
      for (final LogicNode child: children) {
         this.childIndices.add(child.getNodeIndex());
      }

      sort();
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

      return super.equals(o);
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      int result = super.hashCode();

      result = 31 * result + this.childIndices.hashCode();
      return result;
   }

   /**
    * Sort.
    */
   @Override
   public final void sort() {
      this.childIndices.mergeSortFromTo(0,
                                        this.childIndices.size() - 1,
                                        (short o1,
                                         short o2) -> this.logicalExpression.getNode(o1)
                                               .compareTo(this.logicalExpression.getNode(o2)));
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
      if ((this.childIndices != null) &&!this.childIndices.isEmpty()) {
         final StringBuilder builder = new StringBuilder();

         builder.append("➞[");
         this.childIndices.forEach((index) -> {
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
   public String toSimpleString() {
      if ((this.childIndices != null) &&!this.childIndices.isEmpty()) {
         return "➞";
      }

      return "";
   }

   /**
    * Compare fields.
    *
    * @param o the o
    * @return the int
    */
   @Override
   protected int compareFields(LogicNode o) {
      // node semantic is already determined to be the same...
      return compareNodeFields(o);
   }

   /**
    * Compare node fields.
    *
    * @param o the o
    * @return the int
    */
   protected abstract int compareNodeFields(LogicNode o);

   /**
    * Write data.
    *
    * @param dataOutput the data output
    * @param dataTarget the data target
    */
   @Override
   protected void writeData(ByteArrayDataBuffer dataOutput, DataTarget dataTarget) {
      sort();
      super.writeData(dataOutput, dataTarget);
      dataOutput.putShort((short) this.childIndices.size());

      for (final short value: this.childIndices.elements()) {
         dataOutput.putShort(value);
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the children.
    *
    * @return a sorted array of child <code>LogicNode</code>s.
    */
   @Override
   public AbstractLogicNode[] getChildren() {
      final AbstractLogicNode[] childNodes = new AbstractLogicNode[this.childIndices.size()];

      for (int i = 0; i < childNodes.length; i++) {
         childNodes[i] = (AbstractLogicNode) this.logicalExpression.getNode(this.childIndices.get(i));
      }

      return childNodes;
   }
}

