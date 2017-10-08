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



package sh.isaac.model.sememe;

//~--- JDK imports ------------------------------------------------------------

import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.State;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.sememe.version.MutableSememeVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.model.ChronologyImpl;
import sh.isaac.model.sememe.version.ComponentNidVersionImpl;
import sh.isaac.model.sememe.version.DescriptionVersionImpl;
import sh.isaac.model.sememe.version.DynamicSememeImpl;
import sh.isaac.model.sememe.version.LogicGraphVersionImpl;
import sh.isaac.model.sememe.version.LongVersionImpl;
import sh.isaac.model.sememe.version.SememeVersionImpl;
import sh.isaac.model.sememe.version.StringVersionImpl;
import sh.isaac.api.externalizable.IsaacExternalizable;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.model.sememe.version.AbstractSememeVersionImpl;

//~--- classes ----------------------------------------------------------------

/**
 * The Class SememeChronologyImpl.
 *
 * @author kec
 */
public class SememeChronologyImpl extends ChronologyImpl
         implements  SememeChronology, IsaacExternalizable {
   /** The sememe type token. */
   byte sememeTypeToken = -1;

   /** The assemblage sequence. */
   int assemblageSequence = -1;

   /** The referenced component nid. */
   int referencedComponentNid = Integer.MAX_VALUE;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new sememe chronology impl.
    */
   private SememeChronologyImpl() {}

   /**
    * Instantiates a new sememe chronology impl.
    *
    * @param sememeType the sememe type
    * @param primordialUuid the primordial uuid
    * @param nid the nid
    * @param assemblageSequence the assemblage sequence
    * @param referencedComponentNid the referenced component nid
    * @param containerSequence the container sequence
    */
   public SememeChronologyImpl(VersionType sememeType,
                               UUID primordialUuid,
                               int nid,
                               int assemblageSequence,
                               int referencedComponentNid,
                               int containerSequence) {
      super(primordialUuid, nid, containerSequence);
      this.sememeTypeToken        = sememeType.getSememeToken();
      this.assemblageSequence     = assemblageSequence;
      this.referencedComponentNid = referencedComponentNid;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Creates the mutable version.
    *
    * @param <V> the generic type
    * @param stampSequence the stamp sequence
    * @return the m
    */
   @Override
   public <V extends Version> V createMutableVersion(int stampSequence) {
      final V version = createMutableVersionInternal(stampSequence);
      addVersion(version);
      return version;
   }

   /**
    * Creates the mutable version.
    *
    * @param <V> the generic type
    * @param status the status
    * @param ec the ec
    * @return the m
    */
   @Override
   public <V extends Version> V  createMutableVersion(State status, EditCoordinate ec) {
      final int stampSequence = Get.stampService()
                                   .getStampSequence(status,
                                         Long.MAX_VALUE,
                                         ec.getAuthorSequence(),
                                         ec.getModuleSequence(),
                                         ec.getPathSequence());
      final V version = createMutableVersionInternal(stampSequence);

      addVersion(version);
      return version;
   }

   /**
    * Creates the sememe.
    *
    * @param token the token
    * @param container the container
    * @param stampSequence the stamp sequence
    * @param bb the bb
    * @return the sememe version impl
    */
   public static AbstractSememeVersionImpl createSememe(byte token,
         SememeChronologyImpl container,
         int stampSequence,
         ByteArrayDataBuffer bb) {
      final VersionType st = VersionType.getFromToken(token);

      switch (st) {
      case MEMBER:
         return new SememeVersionImpl(container, stampSequence);

      case COMPONENT_NID:
         return new ComponentNidVersionImpl((SememeChronologyImpl) container,
                                           stampSequence,
                                           bb);

      case LONG:
         return new LongVersionImpl((SememeChronologyImpl) container,
                                   stampSequence,
                                   bb);

      case LOGIC_GRAPH:
         return new LogicGraphVersionImpl((SememeChronologyImpl) container,
                                         stampSequence,
                                         bb);

      case DYNAMIC:
         return new DynamicSememeImpl((SememeChronologyImpl) container,
                                      stampSequence,
                                      bb);

      case STRING:
         return new StringVersionImpl((SememeChronologyImpl) container,
                                     stampSequence,
                                     bb);

      case DESCRIPTION:
         return (new DescriptionVersionImpl((SememeChronologyImpl) container,
                                           stampSequence,
                                           bb));

      default:
         throw new UnsupportedOperationException("ae Can't handle: " + token);
      }
   }

   /**
    * Make.
    *
    * @param data the data
    * @return the sememe chronology impl
    */
   public static SememeChronologyImpl make(ByteArrayDataBuffer data) {
      if (IsaacObjectType.SEMEME.getDataFormatVersion() != data.getObjectDataFormatVersion()) {
         throw new UnsupportedOperationException("Data format version not supported: " + data.getObjectDataFormatVersion());
      }
      final SememeChronologyImpl sememeChronology = new SememeChronologyImpl();
      sememeChronology.readData(data);
      return sememeChronology;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      final StringBuilder builder = new StringBuilder();
      toString(builder, true);
      return builder.toString();
   }

   @Override
   public void toString(StringBuilder builder, boolean addAttachments) {
      
      
      builder.append("SememeChronology{");

      if (this.sememeTypeToken == -1) {
         builder.append("SememeType token not initialized");
      } else {
         builder.append(VersionType.getFromToken(this.sememeTypeToken));
      }

      builder.append("\n assemblage:")
              .append(Get.conceptDescriptionText(this.assemblageSequence))
              .append(" <")
              .append(this.assemblageSequence)
              .append(">\n rc:");
      
      switch (Get.identifierService()
              .getChronologyTypeForNid(this.referencedComponentNid)) {
         case CONCEPT:
            builder.append("CONCEPT: ")
                    .append(Get.conceptDescriptionText(this.referencedComponentNid));
            break;
            
         case SEMEME:
            SememeChronology sememeChronicle = Get.assemblageService()
                    .getSememe(this.referencedComponentNid);
            builder.append("SEMEME: ")
                    .append(sememeChronicle.getVersionType()).append(" <")
                    .append(sememeChronicle.getSememeSequence())
                    .append(">\n from assemblage:")
                    .append(Get.conceptDescriptionText(sememeChronicle.getAssemblageSequence()))
                    .append(" <")
                    .append(sememeChronicle.getAssemblageSequence())
                    .append(">\n");
            
            break;
            
         default:
            builder.append(Get.identifierService()
                    .getChronologyTypeForNid(this.referencedComponentNid))
                    .append(" ")
                    .append(this.referencedComponentNid);
      }

      builder.append(" <")
              .append(this.referencedComponentNid)
              .append(">\n ");
      super.toString(builder, addAttachments);
   }

   /**
    * Write chronicle data.
    *
    * @param data the data
    */
   @Override
   public void writeChronicleData(ByteArrayDataBuffer data) {
      super.writeChronicleData(data);
   }

   /**
    * Creates the mutable version internal.
    *
    * @param <M> the generic type
    * @param stampSequence the stamp sequence
    * @return the m
    * @throws UnsupportedOperationException the unsupported operation exception
    */
   protected <M extends MutableSememeVersion> M createMutableVersionInternal(
         int stampSequence)
            throws UnsupportedOperationException {
      switch (getSememeType()) {
      case COMPONENT_NID:
            return (M) new ComponentNidVersionImpl((SememeChronology) this,
                  stampSequence);
      case LONG:
            return (M) new LongVersionImpl((SememeChronologyImpl) this, stampSequence);

      case DYNAMIC:
            return (M) new DynamicSememeImpl((SememeChronologyImpl) this,
                                             stampSequence);

      case LOGIC_GRAPH:
            return (M) new LogicGraphVersionImpl((SememeChronologyImpl) this,
                  stampSequence);

      case STRING:
            return (M) new StringVersionImpl((SememeChronology) this,
                                            stampSequence);

      case MEMBER:
            return (M) new SememeVersionImpl(this, stampSequence);

      case DESCRIPTION:
            return (M) new DescriptionVersionImpl((SememeChronology) this,
                  stampSequence);

      default:
         throw new UnsupportedOperationException("af Can't handle: " + getSememeType());
      }
   }

   /**
    * Make version.
    *
    * @param stampSequence the stamp sequence
    * @param db the db
    * @return the v
    */
   @Override
   protected <V extends StampedVersion> V makeVersion(int stampSequence, ByteArrayDataBuffer db) {
      // consume legacy version sequence. 
      db.getShort();
      return (V) createSememe(this.sememeTypeToken, this, stampSequence, db);
   }

   /**
    * Put additional chronicle fields.
    *
    * @param out the out
    */
   @Override
   protected void putAdditionalChronicleFields(ByteArrayDataBuffer out) {
      out.putByte(this.sememeTypeToken);
      out.putConceptSequence(this.assemblageSequence);
      out.putNid(this.referencedComponentNid);
   }

   /**
    * Skip additional chronicle fields.
    *
    * @param in the in
    */
   @Override
   protected void skipAdditionalChronicleFields(ByteArrayDataBuffer in) {
      in.getByte();             // sememeTypeToken =
      in.getConceptSequence();  // assemblageSequence =
      in.getNid();              // referencedComponentNid =
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the additional chronicle fields.
    *
    * @param in the in
    */
   @Override
   protected void setAdditionalChronicleFieldsFromBuffer(ByteArrayDataBuffer in) {
      this.sememeTypeToken        = in.getByte();
      this.assemblageSequence     = in.getConceptSequence();
      this.referencedComponentNid = in.getNid();
   }

   /**
    * Gets the assemblage sequence.
    *
    * @return the assemblage sequence
    */
   @Override
   public int getAssemblageSequence() {
      return this.assemblageSequence;
   }

   /**
    * Gets the ochre object type.
    *
    * @return the ochre object type
    */
   @Override
   public IsaacObjectType getIsaacObjectType() {
      return IsaacObjectType.SEMEME;
   }

   /**
    * Gets the referenced component nid.
    *
    * @return the referenced component nid
    */
   @Override
   public int getReferencedComponentNid() {
      return this.referencedComponentNid;
   }

   /**
    * Gets the sememe sequence.
    *
    * @return the sememe sequence
    */
   @Override
   public int getSememeSequence() {
      return getContainerSequence();
   }

   /**
    * Gets the sememe type.
    *
    * @return the sememe type
    */
   @Override
   public VersionType getSememeType() {
      return VersionType.getFromToken(this.sememeTypeToken);
   }
}

