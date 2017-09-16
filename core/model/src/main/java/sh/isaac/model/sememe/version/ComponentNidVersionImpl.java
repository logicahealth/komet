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



package sh.isaac.model.sememe.version;

//~--- JDK imports ------------------------------------------------------------

import java.util.Optional;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.model.sememe.SememeChronologyImpl;
import sh.isaac.api.component.sememe.version.MutableComponentNidVersion;
import sh.isaac.api.coordinate.EditCoordinate;

//~--- classes ----------------------------------------------------------------

/**
 * Used for description dialect preferences.
 *
 * @author kec
 */
public class ComponentNidVersionImpl
        extends AbstractSememeVersionImpl
         implements MutableComponentNidVersion {
   /** The component nid. */
   int componentNid = Integer.MAX_VALUE;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new component nid sememe impl.
    *
    * @param container the container
    * @param stampSequence the stamp sequence
    * @param versionSequence the version sequence
    */
   public ComponentNidVersionImpl(SememeChronology container,
                                 int stampSequence,
                                 short versionSequence) {
      super(container, stampSequence, versionSequence);
   }

   /**
    * Instantiates a new component nid sememe impl.
    *
    * @param container the container
    * @param stampSequence the stamp sequence
    * @param versionSequence the version sequence
    * @param data the data
    */
   public ComponentNidVersionImpl(SememeChronologyImpl container,
                                 int stampSequence,
                                 short versionSequence,
                                 ByteArrayDataBuffer data) {
      super(container, stampSequence, versionSequence);
      this.componentNid = data.getNid();
   }
   private ComponentNidVersionImpl(ComponentNidVersionImpl other, int stampSequence, short versionSequence) {
      super(other.getChronology(), stampSequence, versionSequence);
      this.componentNid = other.componentNid;
   }

   @Override
   public <V extends Version> V makeAnalog(EditCoordinate ec) {
      final int stampSequence = Get.stampService()
                                   .getStampSequence(
                                       this.getState(),
                                       Long.MAX_VALUE,
                                       ec.getAuthorSequence(),
                                       this.getModuleSequence(),
                                       ec.getPathSequence());
      SememeChronologyImpl chronologyImpl = (SememeChronologyImpl) this.chronicle;
      final ComponentNidVersionImpl newVersion = new ComponentNidVersionImpl(this, stampSequence, 
              chronologyImpl.nextVersionSequence());

      chronologyImpl.addVersion(newVersion);
      return (V) newVersion;   
   }


   //~--- methods -------------------------------------------------------------

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      final StringBuilder sb = new StringBuilder();

      sb.append("{Component Nid≤");

      switch (Get.identifierService()
                 .getChronologyTypeForNid(this.componentNid)) {
      case CONCEPT:
         sb.append(Get.conceptDescriptionText(this.componentNid));
         break;

      case SEMEME:
         final Optional<? extends SememeChronology> optionalSememe = Get.assemblageService()
                                                                                                    .getOptionalSememe(
                                                                                                       this.componentNid);

         if (optionalSememe.isPresent()) {
            sb.append(optionalSememe.get()
                                    .getSememeType());
         } else {
            sb.append("no such sememe: ")
              .append(this.componentNid);
         }

         break;

      default:
         sb.append(Get.identifierService()
                      .getChronologyTypeForNid(this.componentNid))
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
   protected void writeVersionData(ByteArrayDataBuffer data) {
      super.writeVersionData(data);
      data.putNid(this.componentNid);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the component nid.
    *
    * @return the component nid
    */
   @Override
   public int getComponentNid() {
      return this.componentNid;
   }

   //~--- set methods ---------------------------------------------------------

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

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the sememe type.
    *
    * @return the sememe type
    */
   @Override
   public VersionType getSememeType() {
      return VersionType.COMPONENT_NID;
   }
   

   @Override
   protected int editDistance3(AbstractSememeVersionImpl other, int editDistance) {
      ComponentNidVersionImpl otherImpl = (ComponentNidVersionImpl) other;
      if (this.componentNid != otherImpl.componentNid) {
         editDistance++;
      }
      return editDistance;
   }

   @Override
   protected boolean deepEquals3(AbstractSememeVersionImpl other) {
      if (!(other instanceof ComponentNidVersionImpl)) {
         return false;
      }
      ComponentNidVersionImpl otherImpl = (ComponentNidVersionImpl) other;
      return this.componentNid == otherImpl.componentNid;
   }
      
}

