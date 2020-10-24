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



package sh.isaac.model.semantic;
import java.util.NoSuchElementException;
import java.util.UUID;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.MutableSemanticVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.IsaacExternalizable;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.model.ChronologyImpl;
import sh.isaac.model.semantic.version.AbstractVersionImpl;
import sh.isaac.model.semantic.version.ComponentNidVersionImpl;
import sh.isaac.model.semantic.version.DescriptionVersionImpl;
import sh.isaac.model.semantic.version.DynamicImpl;
import sh.isaac.model.semantic.version.ImageVersionImpl;
import sh.isaac.model.semantic.version.LogicGraphVersionImpl;
import sh.isaac.model.semantic.version.LongVersionImpl;
import sh.isaac.model.semantic.version.SemanticVersionImpl;
import sh.isaac.model.semantic.version.StringVersionImpl;
import sh.isaac.model.semantic.version.brittle.Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl;
import sh.isaac.model.semantic.version.brittle.Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl;
import sh.isaac.model.semantic.version.brittle.Nid1_Int2_VersionImpl;
import sh.isaac.model.semantic.version.brittle.Nid1_Long2_VersionImpl;
import sh.isaac.model.semantic.version.brittle.Nid1_Nid2_Int3_VersionImpl;
import sh.isaac.model.semantic.version.brittle.Nid1_Nid2_Str3_VersionImpl;
import sh.isaac.model.semantic.version.brittle.Nid1_Nid2_VersionImpl;
import sh.isaac.model.semantic.version.brittle.Nid1_Str2_VersionImpl;
import sh.isaac.model.semantic.version.brittle.Rf2RelationshipImpl;
import sh.isaac.model.semantic.version.brittle.Str1_Nid2_Nid3_Nid4_VersionImpl;
import sh.isaac.model.semantic.version.brittle.Str1_Str2_Nid3_Nid4_Nid5_VersionImpl;
import sh.isaac.model.semantic.version.brittle.Str1_Str2_Nid3_Nid4_VersionImpl;
import sh.isaac.model.semantic.version.brittle.Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl;
import sh.isaac.model.semantic.version.brittle.Str1_Str2_VersionImpl;

/**
 * The Class SemanticChronologyImpl.
 *
 * @author kec
 */
public class SemanticChronologyImpl
        extends ChronologyImpl
         implements SemanticChronology, IsaacExternalizable {

   /** The referenced component nid. */
   int referencedComponentNid = Integer.MAX_VALUE;

   /**
    * Instantiates a new semantic chronology impl.
    */
   private SemanticChronologyImpl() {}

   /**
    * Instantiates a new semantic chronology impl.
    *
    * @param semanticType the semantic type
    * @param primordialUuid the primordial uuid
    * @param assemblageNid the assemblage sequence
    * @param referencedComponentNid the referenced component nid
    */
   public SemanticChronologyImpl(VersionType semanticType,
                                 UUID primordialUuid,
                                 int assemblageNid,
                                 int referencedComponentNid) {
      super(primordialUuid, assemblageNid, semanticType);
      this.referencedComponentNid = referencedComponentNid;
   }

   @Override
   public <V extends Version> V createMutableVersion(int stampSequence) {
      final V version = createMutableVersionInternal(stampSequence);
      addVersion(version);
      return version;
   }
   
   @Override
   public <V extends Version> V createMutableVersion(Transaction transaction, int stampSequence) {
      final V version = createMutableVersionInternal(stampSequence);
      transaction.addVersionToTransaction(version);
      addVersion(version);
      return version;
   }

   /**
    * Creates the semantic.
    *
    * @param chronology the container
    * @param stampSequence the stamp sequence
    * @param bb the bb
    * @return the semantic version impl
    */
   public static AbstractVersionImpl createSemantic(
         SemanticChronologyImpl chronology,
         int stampSequence,
         ByteArrayDataBuffer bb) {

      switch (chronology.versionType) {
      case MEMBER:
         return new SemanticVersionImpl(chronology, stampSequence);

      case COMPONENT_NID:
         return new ComponentNidVersionImpl(chronology, stampSequence, bb);

      case LONG:
         return new LongVersionImpl(chronology, stampSequence, bb);

      case LOGIC_GRAPH:
         return new LogicGraphVersionImpl(chronology, stampSequence, bb);

      case IMAGE:
         return new ImageVersionImpl(chronology, stampSequence, bb);

      case DYNAMIC:
         return new DynamicImpl(chronology, stampSequence, bb);

      case STRING:
         return new StringVersionImpl(chronology, stampSequence, bb);

      case DESCRIPTION:
         return (new DescriptionVersionImpl(chronology, stampSequence, bb));

      case RF2_RELATIONSHIP:
         return new Rf2RelationshipImpl(chronology, stampSequence, bb);

      case Int1_Int2_Str3_Str4_Str5_Nid6_Nid7:
         return new Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl(chronology, stampSequence, bb);

         case Nid1_Int2:
            return new Nid1_Int2_VersionImpl(chronology, stampSequence, bb);

         case Nid1_Long2:
            return new Nid1_Long2_VersionImpl(chronology, stampSequence, bb);

         case Nid1_Int2_Str3_Str4_Nid5_Nid6:
         return new Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl(chronology, stampSequence, bb);

      case Nid1_Nid2_Int3:
         return new Nid1_Nid2_Int3_VersionImpl(chronology, stampSequence, bb);

      case Nid1_Nid2:
         return new Nid1_Nid2_VersionImpl(chronology, stampSequence, bb);

      case Nid1_Nid2_Str3:
         return new Nid1_Nid2_Str3_VersionImpl(chronology, stampSequence, bb);

      case Nid1_Str2:
         return new Nid1_Str2_VersionImpl(chronology, stampSequence, bb);

      case Str1_Str2:
         return new Str1_Str2_VersionImpl(chronology, stampSequence, bb);

      case Str1_Str2_Nid3_Nid4:
         return new Str1_Str2_Nid3_Nid4_VersionImpl(chronology, stampSequence, bb);

      case Str1_Str2_Str3_Str4_Str5_Str6_Str7:
         return new Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl(chronology, stampSequence, bb);

      case Str1_Nid2_Nid3_Nid4:
         return new Str1_Nid2_Nid3_Nid4_VersionImpl(chronology, stampSequence, bb);

      case Str1_Str2_Nid3_Nid4_Nid5:
         return new Str1_Str2_Nid3_Nid4_Nid5_VersionImpl(chronology, stampSequence, bb);

      default:
         throw new UnsupportedOperationException("ae Can't handle: " + chronology.versionType);
      }
   }

   /**
    * Make.
    *
    * @param data the data
    * @return the semantic chronology impl
    */
   public static SemanticChronologyImpl make(ByteArrayDataBuffer data) {
      if (IsaacObjectType.SEMANTIC.getDataFormatVersion() != data.getObjectDataFormatVersion()) {
         throw new UnsupportedOperationException(
             "Data format version not supported: " + data.getObjectDataFormatVersion());
      }

      final SemanticChronologyImpl semanticChronology = new SemanticChronologyImpl();

      semanticChronology.readData(data);
//      ModelGet.identifierService()
//              .addToSemanticIndex(semanticChronology.referencedComponentNid, semanticChronology.getNid());
      return semanticChronology;
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
      builder.append("SemanticChronology{");

      if (this.versionType == null) {
         builder.append("versionType not initialized");
      } else {
         builder.append(versionType);
         switch (versionType) {
             case DESCRIPTION:
                 try {
                     SemanticChronology descriptionChronology = Get.assemblageService().getSemanticChronology(this.getNid());
                     DescriptionVersion descriptionVersion = (DescriptionVersion) descriptionChronology.getVersionList().get(0);
                     builder.append(": ");
                     builder.append(descriptionVersion.getText());
                 } catch (Throwable e) {
                     LOG.warn("Unexpected error in toString for Semantic: " + e);
                 }
                 break;
             
         }
      }

      builder.append("\n assemblage:")
             .append(Get.conceptDescriptionText(getAssemblageNid()))
             .append(" <")
             .append(getAssemblageNid())
             .append(">\n rc:");

      switch (Get.identifierService()
                 .getObjectTypeForComponent(this.referencedComponentNid)) {
      case CONCEPT:
         builder.append("CONCEPT: ")
                .append(Get.conceptDescriptionText(this.referencedComponentNid));
         break;

      case SEMANTIC:
          try {
              SemanticChronologyImpl semanticChronicle = (SemanticChronologyImpl) Get.assemblageService()
                      .getSemanticChronology(
                              this.referencedComponentNid);
              
              builder.append("SEMANTIC: ")
                      .append(semanticChronicle.getVersionType())
                      .append("\n from assemblage:")
                      .append(Get.conceptDescriptionText(semanticChronicle.getAssemblageNid()))
                      .append(" <")
                      .append(semanticChronicle.getAssemblageNid())
                      .append(">\n");
          } catch (NoSuchElementException e) {
              builder.append("SEMANTIC: ");
              builder.append(this.referencedComponentNid);
              builder.append(" is primordial. ");
          }
         break;

      default:
         //probably means UNKNOWN, which is a typically a problem - write both the nid and the UUID to aid in tracking it down.
         builder.append(Get.identifierService()
                           .getObjectTypeForComponent(this.referencedComponentNid))
                .append(" ")
                .append(this.referencedComponentNid)
                .append(" ")
                .append(Get.identifierService().getUuidPrimordialStringForNid(this.referencedComponentNid));
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
   protected <M extends MutableSemanticVersion> M createMutableVersionInternal(int stampSequence)
            throws UnsupportedOperationException {
      switch (getVersionType()) {
      case COMPONENT_NID:
         return (M) new ComponentNidVersionImpl((SemanticChronology) this, stampSequence);

      case LONG:
         return (M) new LongVersionImpl((SemanticChronologyImpl) this, stampSequence);

      case DYNAMIC:
         return (M) new DynamicImpl((SemanticChronologyImpl) this, stampSequence);

      case LOGIC_GRAPH:
         return (M) new LogicGraphVersionImpl((SemanticChronologyImpl) this, stampSequence);

      case IMAGE:
            return (M) new ImageVersionImpl((SemanticChronologyImpl) this, stampSequence);

      case STRING:
         return (M) new StringVersionImpl((SemanticChronology) this, stampSequence);

      case MEMBER:
         return (M) new SemanticVersionImpl(this, stampSequence);

      case DESCRIPTION:
         return (M) new DescriptionVersionImpl((SemanticChronology) this, stampSequence);

      case RF2_RELATIONSHIP:
         return (M) new Rf2RelationshipImpl((SemanticChronology) this, stampSequence);

      case Int1_Int2_Str3_Str4_Str5_Nid6_Nid7:
         return (M) new Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl((SemanticChronology) this, stampSequence);

         case Nid1_Int2:
            return (M) new Nid1_Int2_VersionImpl((SemanticChronology) this, stampSequence);

         case Nid1_Long2:
            return (M) new Nid1_Long2_VersionImpl((SemanticChronology) this, stampSequence);

         case Nid1_Int2_Str3_Str4_Nid5_Nid6:
         return (M) new Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl((SemanticChronology) this, stampSequence);

      case Nid1_Nid2_Int3:
         return (M) new Nid1_Nid2_Int3_VersionImpl((SemanticChronology) this, stampSequence);

      case Nid1_Nid2:
         return (M) new Nid1_Nid2_VersionImpl((SemanticChronology) this, stampSequence);

      case Nid1_Nid2_Str3:
         return (M) new Nid1_Nid2_Str3_VersionImpl((SemanticChronology) this, stampSequence);

      case Nid1_Str2:
         return (M) new Nid1_Str2_VersionImpl((SemanticChronology) this, stampSequence);

      case Str1_Str2:
         return (M) new Str1_Str2_VersionImpl((SemanticChronology) this, stampSequence);

      case Str1_Str2_Nid3_Nid4:
         return (M) new Str1_Str2_Nid3_Nid4_VersionImpl((SemanticChronology) this, stampSequence);

      case Str1_Str2_Str3_Str4_Str5_Str6_Str7:
         return (M) new Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl((SemanticChronology) this, stampSequence);

      case Str1_Nid2_Nid3_Nid4:
          return (M) new Str1_Nid2_Nid3_Nid4_VersionImpl((SemanticChronology) this, stampSequence);
          
      case Str1_Str2_Nid3_Nid4_Nid5:
          return (M) new Str1_Str2_Nid3_Nid4_Nid5_VersionImpl((SemanticChronology) this, stampSequence);

      default:
         throw new UnsupportedOperationException("af Can't handle: " + getVersionType());
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
      return (V) createSemantic(this, stampSequence, db);
   }

   /**
    * Put additional chronicle fields.
    *
    * @param out the out
    */
   @Override
   protected void putAdditionalChronicleFields(ByteArrayDataBuffer out) {
      out.putNid(this.referencedComponentNid);
   }

   /**
    * Skip additional chronicle fields.
    *
    * @param in the in
    */
   @Override
   protected void skipAdditionalChronicleFields(ByteArrayDataBuffer in) {
      in.getNid();   // referencedComponentNid =
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Gets the additional chronicle fields.
    *
    * @param in the in
    */
   @Override
   protected void setAdditionalChronicleFieldsFromBuffer(ByteArrayDataBuffer in) {
      this.referencedComponentNid = in.getNid();
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the ochre object type.
    *
    * @return the ochre object type
    */
   @Override
   public IsaacObjectType getIsaacObjectType() {
      return IsaacObjectType.SEMANTIC;
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
    * Gets the semantic type.
    *
    * @return the semantic type
    */
   @Override
   public VersionType getVersionType() {
      if (this.versionType == null) {
          throw new IllegalStateException();
      }
      return this.versionType;
   }
}

