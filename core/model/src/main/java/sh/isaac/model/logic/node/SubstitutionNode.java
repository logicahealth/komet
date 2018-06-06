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


//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.DataTarget;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.assertions.substitution.SubstitutionFieldSpecification;
import sh.isaac.model.logic.LogicalExpressionImpl;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 12/10/14.
 */
public abstract class SubstitutionNode
        extends AbstractLogicNode {
   /** The substitution field specification. */
   SubstitutionFieldSpecification substitutionFieldSpecification;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new substitution node.
    *
    * @param logicGraphVersion the logic graph version
    * @param dataInputStream the data input stream
    */
   public SubstitutionNode(LogicalExpressionImpl logicGraphVersion,
                           ByteArrayDataBuffer dataInputStream) {
      super(logicGraphVersion, dataInputStream);

      final int    length = dataInputStream.getInt();
      final byte[] bytes  = new byte[length];

      dataInputStream.get(bytes, 0, length);
      throw new UnsupportedOperationException("deserializer for substitution field specification not implemented");
   }

   /**
    * Instantiates a new substitution node.
    *
    * @param logicGraphVersion the logic graph version
    * @param substitutionFieldSpecification the substitution field specification
    */
   public SubstitutionNode(LogicalExpressionImpl logicGraphVersion,
                           SubstitutionFieldSpecification substitutionFieldSpecification) {
      super(logicGraphVersion);
      this.substitutionFieldSpecification = substitutionFieldSpecification;
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

      final SubstitutionNode that = (SubstitutionNode) o;

      return this.substitutionFieldSpecification.equals(that.substitutionFieldSpecification);
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      int result = super.hashCode();

      result = 31 * result + this.substitutionFieldSpecification.hashCode();
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
      return " Substitution: '" + this.substitutionFieldSpecification + '\'' + super.toString(nodeIdSuffix);
   }

   @Override
   public String toSimpleString() {
      return " Substitution: '" + this.substitutionFieldSpecification + '\'' + super.toSimpleString();
   }

   /**
    * Compare fields.
    *
    * @param o the o
    * @return the int
    */
   @Override
   protected int compareFields(LogicNode o) {
      final SubstitutionNode that = (SubstitutionNode) o;

      return this.substitutionFieldSpecification.compareTo(that.substitutionFieldSpecification);
   }

   /**
    * Write node data.
    *
    * @param dataOutput the data output
    * @param dataTarget the data target
    */
   @Override
   protected final void writeNodeData(ByteArrayDataBuffer dataOutput, DataTarget dataTarget) {
      super.writeData(dataOutput, dataTarget);

      final byte[] bytes = this.substitutionFieldSpecification.getBytes();

      dataOutput.putInt(bytes.length);
      dataOutput.put(bytes);
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
    public final void removeChild(short childId) {
        // nothing to do. 
    }

   /**
    * Gets the substitution field specification.
    *
    * @return the substitution field specification
    */
   public SubstitutionFieldSpecification getSubstitutionFieldSpecification() {
      return this.substitutionFieldSpecification;
   }
}

