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


import java.util.UUID;
import org.apache.mahout.math.set.OpenIntHashSet;

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
import sh.isaac.model.logic.node.external.FeatureNodeWithUuids;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 12/9/14.
 */
public final class FeatureNodeWithNids
        extends TypedNodeWithNids {
   /** The concrete domain operators. */
   static ConcreteDomainOperators[] concreteDomainOperators = ConcreteDomainOperators.values();

   //~--- fields --------------------------------------------------------------

   /** The operator. */
   ConcreteDomainOperators operator;
   int measureSemanticNid;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new feature node with sequences.
    *
    * @param externalForm the external form
    */
   public FeatureNodeWithNids(FeatureNodeWithUuids externalForm) {
      super(externalForm);
      this.operator = externalForm.getOperator();
      this.measureSemanticNid = Get.identifierService().getNidForUuids(externalForm.getMeasureSemanticUuid());
   }

   /**
    * Instantiates a new feature node with sequences.
    *
    * @param logicGraphVersion the logic graph version
    * @param dataInputStream the data input stream
    */

   public FeatureNodeWithNids(LogicalExpressionImpl logicGraphVersion,
                                   ByteArrayDataBuffer dataInputStream) {
      super(logicGraphVersion, dataInputStream);
      this.operator = concreteDomainOperators[dataInputStream.getByte()];
      this.measureSemanticNid = dataInputStream.getInt();
   }

   /**
    * Instantiates a new feature node with sequences.
    *
    * @param logicGraphVersion the logic graph version
    * @param typeConceptNid the type concept id
     * @param measureSemanticNid
    * @param child the child
     * @param operator
    */
   public FeatureNodeWithNids(LogicalExpressionImpl logicGraphVersion,
                                   int typeConceptNid,
                                   int measureSemanticNid, 
                                   ConcreteDomainOperators operator,
                                   AbstractLogicNode child) {
      super(logicGraphVersion, typeConceptNid, child);
      this.operator = operator;  
      this.measureSemanticNid = measureSemanticNid;
   }

   //~--- methods -------------------------------------------------------------

   public int getMeasureSemanticNid() {
        return measureSemanticNid;
   }
    /**
     * Adds the concepts referenced by node.
     *
     * @param conceptNidSet the concept nid set
     */
    @Override
    public void addConceptsReferencedByNode(OpenIntHashSet conceptNidSet) {
        super.addConceptsReferencedByNode(conceptNidSet);
        conceptNidSet.add(measureSemanticNid);
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

      final FeatureNodeWithNids that = (FeatureNodeWithNids) o;

    if (measureSemanticNid != that.measureSemanticNid) {
        return false;
    }
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

      result = 31 * result + measureSemanticNid;
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
      return "Feature[" + getNodeIndex() + nodeIdSuffix + "] " + this.operator +
             ", units:"  + Get.conceptDescriptionText(measureSemanticNid)
            + super.toString(nodeIdSuffix);
   }

   @Override
   public String toSimpleString() {
      return this.operator +
             super.toSimpleString();
   }
    @Override
    public void addToBuilder(StringBuilder builder) {
        builder.append("\n       Feature(");
        builder.append("Get.conceptSpecification(").append(Get.identifierService().getUuidPrimoridalStringForNid(measureSemanticNid)).append("), ");
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
         final FeatureNodeWithUuids externalForm = new FeatureNodeWithUuids(this);

         externalForm.writeNodeData(dataOutput, dataTarget);
         break;

      case INTERNAL:
         super.writeNodeData(dataOutput, dataTarget);
         dataOutput.putByte((byte) this.operator.ordinal());
         dataOutput.putInt(measureSemanticNid);
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
// }
   @Override
   protected int compareTypedNodeFields(LogicNode o) {
      // node semantic already determined equals.
      final FeatureNodeWithNids other = (FeatureNodeWithNids) o;

      if (measureSemanticNid != other.measureSemanticNid) {
         return Integer.compare(measureSemanticNid, other.measureSemanticNid);
      }
      if (this.operator != other.operator) {
         return this.operator.compareTo(other.operator);
      }

      return Integer.compare(this.typeConceptNid, other.typeConceptNid);
   }

   /**
    * Inits the node uuid.
    *
    * @return the uuid
    */
   @Override
   protected UUID initNodeUuid() {
      return UuidT5Generator.get(getNodeSemantic().getSemanticUuid(),
                                 Integer.toString(typeConceptNid) +
                                  this.operator.toString());
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

