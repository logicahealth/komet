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


import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import org.apache.mahout.math.set.OpenIntHashSet;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.DataTarget;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.tree.TreeNodeVisitData;
import sh.isaac.model.logic.LogicalExpressionImpl;
import sh.isaac.model.logic.node.internal.ConceptNodeWithNids;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 12/10/14.
 */
public abstract class AbstractLogicNode
         implements LogicNode {
   /** The Constant NAMESPACE_UUID. */
   protected static final UUID NAMESPACE_UUID = UUID.fromString("d64c6d91-a37d-11e4-bcd8-0800200c9a66");

   //~--- fields --------------------------------------------------------------

   /** The node index. */
   private short nodeIndex = Short.MIN_VALUE;

   /** The node uuid. */
   protected UUID nodeUuid = null;

   /** The logic graph version. */
   LogicalExpressionImpl logicalExpression;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new abstract logic node.
    *
    * @param anotherNode the another node
    */
   protected AbstractLogicNode(AbstractLogicNode anotherNode) {
      this.nodeIndex = anotherNode.nodeIndex;
      this.nodeUuid  = anotherNode.nodeUuid;
   }

   /**
    * Instantiates a new abstract logic node.
    *
    * @param logicalExpression the logic graph version
    */
   public AbstractLogicNode(LogicalExpressionImpl logicalExpression) {
      this.logicalExpression = logicalExpression;
      logicalExpression.addNode(this);
   }

   /**
    * Instantiates a new abstract logic node.
    *
    * @param logicGraphVersion the logic graph version
    * @param dataInputStream the data input stream
    */
   public AbstractLogicNode(LogicalExpressionImpl logicGraphVersion, ByteArrayDataBuffer dataInputStream) {
      if (dataInputStream.getObjectDataFormatVersion() != 1) {
         System.out.println("Format version error: " + dataInputStream.getObjectDataFormatVersion());
      }
      this.nodeIndex         = dataInputStream.getShort();
      this.logicalExpression = logicGraphVersion;
      logicGraphVersion.addNode(this);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Should be overridden by subclasses that need to add concepts.
    * Concepts from connector nodes should not be added.
    *
    * @param conceptSequenceSet the concept nid set
    */
   @Override
   public void addConceptsReferencedByNode(OpenIntHashSet conceptSequenceSet) {
      conceptSequenceSet.add(getNodeSemantic().getConceptNid());
   }

   /**
    * Compare to.
    *
    * @param o the o
    * @return the int
    */
   @Override
   public int compareTo(LogicNode o) {
      if (this.getNodeSemantic() != o.getNodeSemantic()) {
         return this.getNodeSemantic()
                    .compareTo(o.getNodeSemantic());
      }

      return compareFields(o);
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

      return !((o == null) || (getClass() != o.getClass()));
   }

   /**
    * Fragment to string.
    *
    * @return A string representing the fragment of the expression
    * rooted in this node.
    */
   @Override
   public String fragmentToString() {
      return fragmentToString("");
   }

   /**
    * Fragment to string.
    *
    * @param nodeIdSuffix the node id suffix
    * @return the string
    */
   @Override
   public String fragmentToString(String nodeIdSuffix) {
      final StringBuilder builder = new StringBuilder();

      this.logicalExpression.processDepthFirst(this,
              (LogicNode logicNode,
               TreeNodeVisitData graphVisitData) -> {
                 for (int i = 0; i < graphVisitData.getDistance(logicNode.getNodeIndex()); i++) {
                    builder.append("    ");
                 }

                 builder.append(logicNode.toString(nodeIdSuffix));
                 builder.append("\n");
              });
      return builder.toString();
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      return this.nodeIndex;
   }

   /**
    * Sort.
    */
   @Override
   public void sort() {
      // override on nodes with multiple children.
   }

   @Override
   public String toSimpleString() {
      return "";
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
      return "";
   }

   /**
    * Compare fields.
    *
    * @param o the o
    * @return the int
    */
   protected abstract int compareFields(LogicNode o);

   /**
    * Inits the node uuid.
    *
    * @return the uuid
    */
   protected abstract UUID initNodeUuid();

   /**
    * Write data.
    *
    * @param dataOutput the data output
    * @param dataTarget the data target
    */
   protected void writeData(ByteArrayDataBuffer dataOutput, DataTarget dataTarget) {}

   /**
    * Write node data.
    *
    * @param dataOutput the data output
    * @param dataTarget the data target
    */
   protected abstract void writeNodeData(ByteArrayDataBuffer dataOutput, DataTarget dataTarget);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the bytes.
    *
    * @param dataTarget the data target
    * @return the bytes
    */
   @Override
   public byte[] getBytes(DataTarget dataTarget) {
      ByteArrayDataBuffer output = new ByteArrayDataBuffer();
      output.setObjectDataFormatVersion(LogicalExpressionImpl.SERIAL_FORMAT_VERSION);
      output.putByte(LogicalExpressionImpl.SERIAL_FORMAT_VERSION);
      output.putByte((byte) getNodeSemantic().ordinal());
      output.putShort(this.nodeIndex);
      writeNodeData(output, dataTarget);
      output.trimToSize();
      return output.getData();
   }

   /**
    * Gets the children.
    *
    * @return the children
    */
   @Override
   public abstract AbstractLogicNode[] getChildren();

   /**
    * Gets the descendents.
    *
    * @return the descendents
    */
   @Override
   public AbstractLogicNode[] getDescendents() {
      final List<AbstractLogicNode> descendents = new ArrayList<>();

      getDescendents(this, descendents);
      return descendents.toArray(new AbstractLogicNode[descendents.size()]);
   }

   /**
    * Gets the descendents.
    *
    * @param parent the parent
    * @param descendents the descendents
    * @return the descendents
    */
   private void getDescendents(AbstractLogicNode parent, List<AbstractLogicNode> descendents) {
      for (final AbstractLogicNode child: parent.getChildren()) {
         descendents.add(child);
         getDescendents(child, descendents);
      }
   }

   /**
    * Gets the node index.
    *
    * @return the node index
    */
   @Override
   public final short getNodeIndex() {
      return this.nodeIndex;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the node index.
    *
    * @param nodeIndex the new node index
    */
   @Override
   public void setNodeIndex(short nodeIndex) {
      if (this.nodeIndex == Short.MIN_VALUE) {
         this.nodeIndex = nodeIndex;
      } else if (this.nodeIndex == nodeIndex) {
         // nothing to do...
      } else {
         throw new IllegalStateException(
             "LogicNode index cannot be changed once set. NodeId: " + this.nodeIndex + " attempted: " + nodeIndex);
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the node uuid.
    *
    * @return the node uuid
    */
   public UUID getNodeUuid() {
      if (this.nodeUuid == null) {
         this.nodeUuid = initNodeUuid();
      }

      return this.nodeUuid;
   }

   ;

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the node uuid set for depth.
    *
    * @param depth the depth
    * @return the node uuid set for depth
    */
   public SortedSet<UUID> getNodeUuidSetForDepth(int depth) {
      final SortedSet<UUID> uuidSet = new TreeSet<>();

      uuidSet.add(getNodeUuid());

      if (depth > 1) {
         for (final AbstractLogicNode child: getChildren()) {
            uuidSet.addAll(child.getNodeUuidSetForDepth(depth - 1));
         }
      }

      return uuidSet;
   }

   @Override
   public LatestVersion<DescriptionVersion> getPreferredDescription(StampCoordinate stampCoordinate,
         LanguageCoordinate languageCoordinate) {
      int sequenceForDescription = -1;

      switch (getNodeSemantic()) {
      case CONCEPT:
         ConceptNodeWithNids conceptNode = (ConceptNodeWithNids) this;

         sequenceForDescription = conceptNode.getConceptNid();
         break;

      case DEFINITION_ROOT:
         sequenceForDescription = this.getNidForConceptBeingDefined();
         break;

      default:
         sequenceForDescription = getNodeSemantic().getConceptNid();
      }

      LatestVersion<DescriptionVersion> latestDescription = languageCoordinate.getPreferredDescription(
                                                                sequenceForDescription,
                                                                      stampCoordinate);

      return latestDescription;
   }

   @Override
   public int getNidForConceptBeingDefined() {
      return logicalExpression.getConceptBeingDefinedNid();
   }
   
   public abstract void removeChild(short childId);
   
   public abstract void addToBuilder(StringBuilder builder);
   
   
}

