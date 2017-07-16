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
import sh.isaac.api.collections.ConceptSequenceSet;
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
public final class FeatureNodeWithSequences
        extends TypedNodeWithSequences {
   /** The concrete domain operators. */
   static ConcreteDomainOperators[] concreteDomainOperators = ConcreteDomainOperators.values();

   //~--- fields --------------------------------------------------------------

   /** The operator. */
   ConcreteDomainOperators operator;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new feature node with sequences.
    *
    * @param externalForm the external form
    */
   public FeatureNodeWithSequences(FeatureNodeWithUuids externalForm) {
      super(externalForm);
      this.operator = externalForm.getOperator();

//    unitsConceptSequence = Get.identifierService().getConceptSequenceForUuids(externalForm.getUnitsConceptUuid());
   }

   /**
    * Instantiates a new feature node with sequences.
    *
    * @param logicGraphVersion the logic graph version
    * @param dataInputStream the data input stream
    * @throws IOException Signals that an I/O exception has occurred.
    */

// int unitsConceptSequence;
   public FeatureNodeWithSequences(LogicalExpressionImpl logicGraphVersion,
                                   DataInputStream dataInputStream)
            throws IOException {
      super(logicGraphVersion, dataInputStream);
      this.operator = concreteDomainOperators[dataInputStream.readByte()];

//    unitsConceptSequence = dataInputStream.readInt();
   }

   /**
    * Instantiates a new feature node with sequences.
    *
    * @param logicGraphVersion the logic graph version
    * @param typeConceptId the type concept id
    * @param child the child
    */
   public FeatureNodeWithSequences(LogicalExpressionImpl logicGraphVersion,
                                   int typeConceptId,
                                   AbstractLogicNode child) {
      super(logicGraphVersion, typeConceptId, child);
      this.operator = ConcreteDomainOperators.EQUALS;  // TODO - Keith, Dan hardcoded it, it broke when not set.
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the concepts referenced by node.
    *
    * @param conceptSequenceSet the concept sequence set
    */
   @Override
   public void addConceptsReferencedByNode(ConceptSequenceSet conceptSequenceSet) {
      super.addConceptsReferencedByNode(conceptSequenceSet);

//    conceptSequenceSet.add(unitsConceptSequence);
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

      final FeatureNodeWithSequences that = (FeatureNodeWithSequences) o;

//    if (unitsConceptSequence != that.unitsConceptSequence) {
//        return false;
//    }
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

//    result = 31 * result + unitsConceptSequence;
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
             ", units:"  // + Get.conceptDescriptionText(unitsConceptSequence)
            + super.toString(nodeIdSuffix);
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
         final FeatureNodeWithUuids externalForm = new FeatureNodeWithUuids(this);

         externalForm.writeNodeData(dataOutput, dataTarget);
         break;

      case INTERNAL:
         super.writeNodeData(dataOutput, dataTarget);
         dataOutput.writeByte(this.operator.ordinal());

//       dataOutput.writeInt(unitsConceptSequence);
         break;

      default:
         throw new UnsupportedOperationException("Can't handle dataTarget: " + dataTarget);
      }
   }

// public int getUnitsConceptSequence() {
//     return unitsConceptSequence;

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
      final FeatureNodeWithSequences other = (FeatureNodeWithSequences) o;

//    if (unitsConceptSequence != other.unitsConceptSequence) {
//        return Integer.compare(unitsConceptSequence, other.unitsConceptSequence);
//    }
      if (this.operator != other.operator) {
         return this.operator.compareTo(other.operator);
      }

      return Integer.compare(this.typeConceptSequence, other.typeConceptSequence);
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
                                    .toString() + this.operator

      // + Get.identifierService().getUuidPrimordialForNid(unitsConceptSequence)
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

