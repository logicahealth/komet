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

import org.roaringbitmap.RoaringBitmap;
import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.logic.LogicalExpressionImpl;
import sh.isaac.model.logic.node.AbstractLogicNode;
import sh.isaac.model.logic.node.external.TemplateNodeWithUuids;

//~--- classes ----------------------------------------------------------------

/**
 * A node that specifies a template to be substituted in place of this node, and
 * the assemblage concept that will be used to fill template substitution
 * values. Created by kec on 12/10/14.
 */
public final class TemplateNodeWithNids
        extends AbstractLogicNode {
   /** Sequence of the concept that defines the template. */
   int templateConceptNid;

   /**
    * Sequence of the assemblage concept that provides the substitution values
    * for the template.
    */
   int assemblageConceptNid;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new template node with sequences.
    * 
    * Note that this constructor is not safe for all uses, and is only intended to aid in serialization / deserialization.
    * This should be protected, but can't be, due to current package structure.
    *
    * @param externalForm the external form
    */
   public TemplateNodeWithNids(TemplateNodeWithUuids externalForm) {
      super(externalForm);
      this.templateConceptNid = Get.identifierService()
                                        .getNidForUuids(externalForm.getTemplateConceptUuid());
      this.assemblageConceptNid = Get.identifierService()
            .getNidForUuids(externalForm.getAssemblageConceptUuid());
   }

   /**
    * Instantiates a new template node with sequences.
    *
    * @param logicGraphVersion the logic graph version
    * @param dataInputStream the data input stream
    */
   public TemplateNodeWithNids(LogicalExpressionImpl logicGraphVersion,
                                    ByteArrayDataBuffer dataInputStream) {
      super(logicGraphVersion, dataInputStream);
      this.templateConceptNid   = dataInputStream.getInt();
      this.assemblageConceptNid = dataInputStream.getInt();
   }

   /**
    * Instantiates a new template node with sequences.
    *
    * @param logicGraphVersion the logic graph version
    * @param templateConceptId the template concept id
    * @param assemblageConceptId the assemblage concept id
    */
   public TemplateNodeWithNids(LogicalExpressionImpl logicGraphVersion,
                                    int templateConceptId,
                                    int assemblageConceptId) {
      super(logicGraphVersion);
      this.templateConceptNid   = templateConceptId;
      this.assemblageConceptNid = assemblageConceptId;
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
   public void addConceptsReferencedByNode(RoaringBitmap conceptSequenceSet) {
      super.addConceptsReferencedByNode(conceptSequenceSet);
      conceptSequenceSet.add(this.templateConceptNid);
      conceptSequenceSet.add(this.assemblageConceptNid);
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

      final TemplateNodeWithNids that = (TemplateNodeWithNids) o;

      if (this.assemblageConceptNid != that.assemblageConceptNid) {
         return false;
      }

      return this.templateConceptNid == that.templateConceptNid;
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      int result = super.hashCode();

      result = 31 * result + this.templateConceptNid;
      result = 31 * result + this.assemblageConceptNid;
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
      return "Template[" + getNodeIndex() + nodeIdSuffix + "] " + "assemblage: " +
             Get.conceptDescriptionText(this.assemblageConceptNid) + ", template: " +
             Get.conceptDescriptionText(this.templateConceptNid) + super.toString(nodeIdSuffix);
   }

   @Override
   public String toSimpleString() {
      return "assemblage: " +
             Get.defaultCoordinate().getPreferredDescriptionText(this.assemblageConceptNid) + ", template: " +
             Get.defaultCoordinate().getPreferredDescriptionText(this.templateConceptNid) +
             super.toSimpleString();
   }
   
    @Override
    public void addToBuilder(StringBuilder builder) {
        builder.append("\n       Template(");
        builder.append("Get.concept(new UUID(\"");
        builder.append(Get.identifierService().getUuidPrimordialStringForNid(templateConceptNid));
        builder.append("\"),");
        builder.append("Get.concept(new UUID(\"");
        builder.append(Get.identifierService().getUuidPrimordialStringForNid(assemblageConceptNid));
        builder.append("\")");
        builder.append(", leb");
        for (AbstractLogicNode child: getChildren()) {
            child.addToBuilder(builder);
        }
        builder.append("),\n");
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
         final TemplateNodeWithUuids externalForm = new TemplateNodeWithUuids(this);

         externalForm.writeNodeData(dataOutput, dataTarget);
         break;

      case INTERNAL:
         super.writeData(dataOutput, dataTarget);
         dataOutput.putInt(this.templateConceptNid);
         dataOutput.putInt(this.assemblageConceptNid);
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
      final TemplateNodeWithNids that = (TemplateNodeWithNids) o;

      if (this.assemblageConceptNid != that.assemblageConceptNid) {
         return Integer.compare(this.assemblageConceptNid, that.assemblageConceptNid);
      }

      return this.templateConceptNid - that.templateConceptNid;
   }

   /**
    * Inits the node uuid.
    *
    * @return the uuid
    */
   @Override
   protected UUID initNodeUuid() {
      return UuidT5Generator.get(getNodeSemantic().getSemanticUuid(),
                                 Integer.toString(this.assemblageConceptNid) +
                                    Integer.toString(templateConceptNid));
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the sequence of the assemblage concept that provides the substitution values for the template.
    *
    * @return the sequence of the assemblage concept that provides the substitution values for the template
    */
   public int getAssemblageConceptNid() {
      return this.assemblageConceptNid;
   }
   public void setAssemblageConceptNid(int assemblageConceptNid) {
      this.assemblageConceptNid = assemblageConceptNid;
   }

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
    * Gets the node semantic.
    *
    * @return the node semantic
    */
   @Override
   public NodeSemantic getNodeSemantic() {
      return NodeSemantic.TEMPLATE;
   }

   /**
    * Gets the sequence of the concept that defines the template.
    *
    * @return the sequence of the concept that defines the template
    */
   public int getTemplateConceptNid() {
      return this.templateConceptNid;
   }
   public void setTemplateConceptNid(int templateConceptNid) {
      this.templateConceptNid = templateConceptNid;
   }
}

