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



package sh.isaac.model.semantic.version;

import java.util.Optional;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.api.component.semantic.version.MutableComponentNidVersion;
import sh.isaac.api.component.semantic.SemanticChronology;

/**
 * Used for description dialect preferences.
 *
 * @author kec
 */
public class ComponentNidVersionImpl
        extends AbstractVersionImpl
         implements MutableComponentNidVersion {
   /** The component nid. */
   int componentNid = Integer.MAX_VALUE;

   /**
    * Instantiates a new component nid semantic impl.
    *
    * @param container the container
    * @param stampSequence the stamp sequence
    */
   public ComponentNidVersionImpl(SemanticChronology container,
                                 int stampSequence) {
      super(container, stampSequence);
   }

   /**
    * Instantiates a new component nid semantic impl.
    *
    * @param container the container
    * @param stampSequence the stamp sequence
    * @param data the data
    */
   public ComponentNidVersionImpl(SemanticChronologyImpl container,
                                 int stampSequence,
                                 ByteArrayDataBuffer data) {
      super(container, stampSequence);
      this.componentNid = data.getNid();
   }
   
   private ComponentNidVersionImpl(ComponentNidVersionImpl other, int stampSequence) {
      super(other.getChronology(), stampSequence);
      this.componentNid = other.componentNid;
   }

   @SuppressWarnings("unchecked")
   @Override
   public <V extends Version> V makeAnalog(int stampSequence) {
      SemanticChronologyImpl chronologyImpl = (SemanticChronologyImpl) this.chronicle;
      final ComponentNidVersionImpl newVersion = new ComponentNidVersionImpl(this, stampSequence);

      chronologyImpl.addVersion(newVersion);
      return (V) newVersion;   
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      final StringBuilder sb = new StringBuilder();
      sb.append("{rc: ");
      
      sb.append(Get.conceptDescriptionText(this.getReferencedComponentNid()));
      sb.append(" Component Nid: ");

      switch (Get.identifierService().getObjectTypeForComponent(this.componentNid)) {
      case CONCEPT:
         sb.append(Get.conceptDescriptionText(this.componentNid));
         break;

      case SEMANTIC:
         final Optional<? extends SemanticChronology> optionalSemantic = Get.assemblageService()
                                                                                                    .getOptionalSemanticChronology(
                                                                                                       this.componentNid);

         if (optionalSemantic.isPresent()) {
            sb.append(optionalSemantic.get()
                                    .getVersionType());
         } else {
            sb.append("no such semantic: ")
              .append(this.componentNid);
         }

         break;

      default:
         sb.append(Get.identifierService()
                      .getObjectTypeForComponent(this.componentNid))
           .append(" ")
           .append(this.componentNid)
           .append(" ");
      }

      toString(sb);
      sb.append("≥CN}");
      return sb.toString();
   }

   /**
    * Write version data.
    *
    * @param data the data
    */
   @Override
   public void writeVersionData(ByteArrayDataBuffer data) {
      super.writeVersionData(data);
      data.putNid(this.componentNid);
   }

   /**
    * Gets the component nid.
    *
    * @return the component nid
    */
   @Override
   public int getComponentNid() {
      return this.componentNid;
   }

   /**
    * Sets the component nid.
    *
    * @param componentNid the new component nid
    */
   @Override
   public void setComponentNid(int componentNid) {
      if (this.componentNid != Integer.MAX_VALUE) {
         checkUncommitted();
      }

      this.componentNid = componentNid;
   }

   /**
    * Gets the semantic type.
    *
    * @return the semantic type
    */
   @Override
   public VersionType getSemanticType() {
      return VersionType.COMPONENT_NID;
   }
   

   @Override
   protected int editDistance3(AbstractVersionImpl other, int editDistance) {
      ComponentNidVersionImpl otherImpl = (ComponentNidVersionImpl) other;
      if (this.componentNid != otherImpl.componentNid) {
         editDistance++;
      }
      return editDistance;
   }

   @Override
   protected boolean deepEquals3(AbstractVersionImpl other) {
      if (!(other instanceof ComponentNidVersionImpl)) {
         return false;
      }
      ComponentNidVersionImpl otherImpl = (ComponentNidVersionImpl) other;
      return this.componentNid == otherImpl.componentNid;
   }
}
