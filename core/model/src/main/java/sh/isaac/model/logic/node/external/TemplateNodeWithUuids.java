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



/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package sh.isaac.model.logic.node.external;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;

import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.logic.LogicalExpressionImpl;
import sh.isaac.model.logic.node.AbstractLogicNode;
import sh.isaac.model.logic.node.internal.TemplateNodeWithSequences;

//~--- classes ----------------------------------------------------------------

/**
 * The Class TemplateNodeWithUuids.
 *
 * @author kec
 */
public class TemplateNodeWithUuids
        extends AbstractLogicNode {
   /** Sequence of the concept that defines the template. */
   UUID templateConceptUuid;

   /**
    * Sequence of the assemblage concept that provides the substitution values
    * for the template.
    */
   UUID assemblageConceptUuid;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new template node with uuids.
    *
    * @param internalForm the internal form
    */
   public TemplateNodeWithUuids(TemplateNodeWithSequences internalForm) {
      super(internalForm);
      this.templateConceptUuid = Get.identifierService()
                                    .getUuidPrimordialFromConceptId(internalForm.getTemplateConceptSequence())
                                    .get();
      this.assemblageConceptUuid = Get.identifierService()
                                      .getUuidPrimordialFromConceptId(internalForm.getAssemblageConceptSequence())
                                      .get();
   }

   /**
    * Instantiates a new template node with uuids.
    *
    * @param logicGraphVersion the logic graph version
    * @param dataInputStream the data input stream
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public TemplateNodeWithUuids(LogicalExpressionImpl logicGraphVersion,
                                DataInputStream dataInputStream)
            throws IOException {
      super(logicGraphVersion, dataInputStream);
      this.templateConceptUuid   = new UUID(dataInputStream.readLong(), dataInputStream.readLong());
      this.assemblageConceptUuid = new UUID(dataInputStream.readLong(), dataInputStream.readLong());
   }

   /**
    * Instantiates a new template node with uuids.
    *
    * @param logicGraphVersion the logic graph version
    * @param templateConceptUuid the template concept uuid
    * @param assemblageConceptUuid the assemblage concept uuid
    */
   public TemplateNodeWithUuids(LogicalExpressionImpl logicGraphVersion,
                                UUID templateConceptUuid,
                                UUID assemblageConceptUuid) {
      super(logicGraphVersion);
      this.templateConceptUuid   = templateConceptUuid;
      this.assemblageConceptUuid = assemblageConceptUuid;
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

      final TemplateNodeWithUuids that = (TemplateNodeWithUuids) o;

      if (!this.assemblageConceptUuid.equals(that.assemblageConceptUuid)) {
         return false;
      }

      return this.templateConceptUuid.equals(that.templateConceptUuid);
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      int result = super.hashCode();

      result = 31 * result + this.templateConceptUuid.hashCode();
      result = 31 * result + this.assemblageConceptUuid.hashCode();
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
      return "TemplateNode[" + getNodeIndex() + nodeIdSuffix + "] " + "assemblage: " +
             Get.conceptService().getConcept(this.assemblageConceptUuid).toUserString() + ", template: " +
             Get.conceptService().getConcept(this.templateConceptUuid).toUserString() + super.toString(nodeIdSuffix);
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
         super.writeData(dataOutput, dataTarget);
         dataOutput.writeLong(this.templateConceptUuid.getMostSignificantBits());
         dataOutput.writeLong(this.templateConceptUuid.getLeastSignificantBits());
         dataOutput.writeLong(this.assemblageConceptUuid.getMostSignificantBits());
         dataOutput.writeLong(this.assemblageConceptUuid.getLeastSignificantBits());
         break;

      case INTERNAL:
         final TemplateNodeWithSequences internalForm = new TemplateNodeWithSequences(this);

         internalForm.writeNodeData(dataOutput, dataTarget);
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
      final TemplateNodeWithUuids that = (TemplateNodeWithUuids) o;

      if (!this.assemblageConceptUuid.equals(that.assemblageConceptUuid)) {
         return this.assemblageConceptUuid.compareTo(that.assemblageConceptUuid);
      }

      return this.templateConceptUuid.compareTo(that.templateConceptUuid);
   }

   /**
    * Inits the node uuid.
    *
    * @return the uuid
    */
   @Override
   protected UUID initNodeUuid() {
      return UuidT5Generator.get(getNodeSemantic().getSemanticUuid(),
                                 this.templateConceptUuid.toString() + this.assemblageConceptUuid.toString());
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the sequence of the assemblage concept that provides the substitution values for the template.
    *
    * @return the sequence of the assemblage concept that provides the substitution values for the template
    */
   public UUID getAssemblageConceptUuid() {
      return this.assemblageConceptUuid;
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
   public UUID getTemplateConceptUuid() {
      return this.templateConceptUuid;
   }
}

