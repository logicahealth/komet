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
import sh.isaac.model.logic.LogicalExpressionImpl;
import sh.isaac.model.logic.node.AbstractLogicNode;
import sh.isaac.model.logic.node.external.ConceptNodeWithUuids;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 12/10/14.
 */
public final class ConceptNodeWithNids
        extends AbstractLogicNode {
   /** The concept nid. */
   int conceptNid;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new concept node with sequences.
    * 
    * Note that this constructor is not safe for all uses, and is only intended to aid in serialization / deserialization.
    * This should be protected, but can't be, due to current package structure.
    *
    * @param externalForm the external form
    */
   public ConceptNodeWithNids(ConceptNodeWithUuids externalForm) {
      super(externalForm);
      this.conceptNid = Get.identifierService()
                                .getNidForUuids(externalForm.getConceptUuid());
      if (this.conceptNid >= 0) {
         throw new IllegalStateException("Nid must be negative, found: " + conceptNid);
      }
   }

   /**
    * Instantiates a new concept node with sequences.
    *
    * @param logicGraphVersion the logic graph version
    * @param dataInputStream the data input stream
    */
   public ConceptNodeWithNids(LogicalExpressionImpl logicGraphVersion,
                                   ByteArrayDataBuffer dataInputStream) {
      super(logicGraphVersion, dataInputStream);
      this.conceptNid = dataInputStream.getInt();
      if (this.conceptNid >= 0) {
         throw new IllegalStateException("Nid must be negative, found: " + conceptNid);
      }
   }

   /**
    * Instantiates a new concept node with sequences.
    *
    * @param logicGraphVersion the logic graph version
    * @param conceptId the concept id
    */
   public ConceptNodeWithNids(LogicalExpressionImpl logicGraphVersion, int conceptId) {
      super(logicGraphVersion);
      this.conceptNid = conceptId;
      if (this.conceptNid >= 0) {
         throw new IllegalStateException("Nid must be negative, found: " + conceptNid);
      }
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
    * Adds the concepts referenced by node.
    *
    * @param conceptSequenceSet the concept nid set
    */
   @Override
   public void addConceptsReferencedByNode(OpenIntHashSet conceptSequenceSet) {
      super.addConceptsReferencedByNode(conceptSequenceSet);
      conceptSequenceSet.add(this.conceptNid);
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

      final ConceptNodeWithNids that = (ConceptNodeWithNids) o;

      return this.conceptNid == that.conceptNid;
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      int result = super.hashCode();

      result = 31 * result + this.conceptNid;
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
      return "Concept[" + getNodeIndex() + nodeIdSuffix + "] " + Get.conceptDescriptionText(this.conceptNid) +
             " <" + this.conceptNid + ">" +
             super.toString(nodeIdSuffix);
   }

   @Override
   public String toSimpleString() {
      return Get.defaultCoordinate().getPreferredDescriptionText(this.conceptNid) +
             super.toSimpleString();
   }
   @Override
    public void addToBuilder(StringBuilder builder) {
        builder.append("\n       ConceptAssertion(");
        builder.append("Get.concept(\"").append(Get.identifierService().getUuidPrimordialStringForNid(conceptNid)).append("\")");
        builder.append(", leb)\n");
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
         final ConceptNodeWithUuids externalForm = new ConceptNodeWithUuids(this);

         externalForm.writeNodeData(dataOutput, dataTarget);
         break;

      case INTERNAL:
         super.writeData(dataOutput, dataTarget);
         dataOutput.putInt(this.conceptNid);
         break;

      default:
         throw new UnsupportedOperationException("Can't handle dataTarget: " + dataTarget);
      }
   }

   /**
    * Compare fields.
    *
    * @param o the o
    * @return the int
    */
   @Override
   protected int compareFields(LogicNode o) {
      return Integer.compare(this.conceptNid, ((ConceptNodeWithNids) o).getConceptNid());
   }

   /**
    * Inits the node uuid.
    *
    * @return the uuid
    */
   @Override
   protected UUID initNodeUuid() {
      return UuidT5Generator.get(getNodeSemantic().getSemanticUuid(),
                                 Integer.toString(this.conceptNid));
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
        // nothing to do
    }


   /**
    * Gets the concept nid.
    *
    * @return the concept nid
    */
   public int getConceptNid() {
      return this.conceptNid;
   }

   public void setConceptNid(int conceptNid) {
      this.conceptNid = conceptNid;
   }

   /**
    * Gets the node semantic.
    *
    * @return the node semantic
    */
   @Override
   public NodeSemantic getNodeSemantic() {
      return NodeSemantic.CONCEPT;
   }
}

