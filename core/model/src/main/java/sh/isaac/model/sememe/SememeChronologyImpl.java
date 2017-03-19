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
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.component.sememe.version.DescriptionSememe;
import sh.isaac.api.component.sememe.version.LongSememe;
import sh.isaac.api.component.sememe.version.MutableComponentNidSememe;
import sh.isaac.api.component.sememe.version.MutableDynamicSememe;
import sh.isaac.api.component.sememe.version.MutableLogicGraphSememe;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.component.sememe.version.StringSememe;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.OchreExternalizable;
import sh.isaac.api.externalizable.OchreExternalizableObjectType;
import sh.isaac.model.ObjectChronologyImpl;
import sh.isaac.model.sememe.version.ComponentNidSememeImpl;
import sh.isaac.model.sememe.version.DescriptionSememeImpl;
import sh.isaac.model.sememe.version.DynamicSememeImpl;
import sh.isaac.model.sememe.version.LogicGraphSememeImpl;
import sh.isaac.model.sememe.version.LongSememeImpl;
import sh.isaac.model.sememe.version.SememeVersionImpl;
import sh.isaac.model.sememe.version.StringSememeImpl;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 * @param <V>
 */
public class SememeChronologyImpl<V extends SememeVersionImpl<V>>
        extends ObjectChronologyImpl<V>
         implements SememeChronology<V>, OchreExternalizable {
   byte sememeTypeToken        = -1;
   int  assemblageSequence     = -1;
   int  referencedComponentNid = Integer.MAX_VALUE;

   //~--- constructors --------------------------------------------------------

   private SememeChronologyImpl() {}

   public SememeChronologyImpl(SememeType sememeType,
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

   @Override
   public <M extends V> M createMutableVersion(Class<M> type, int stampSequence) {
      final M version = createMutableVersionInternal(type, stampSequence, nextVersionSequence());

      addVersion(version);
      return version;
   }

   @Override
   public <M extends V> M createMutableVersion(Class<M> type, State status, EditCoordinate ec) {
      final int stampSequence = Get.stampService()
                             .getStampSequence(status,
                                   Long.MAX_VALUE,
                                   ec.getAuthorSequence(),
                                   ec.getModuleSequence(),
                                   ec.getPathSequence());
      final M version = createMutableVersionInternal(type, stampSequence, nextVersionSequence());

      addVersion(version);
      return version;
   }

   public static SememeVersionImpl<?> createSememe(byte token,
         SememeChronologyImpl<?> container,
         int stampSequence,
         short versionSequence,
         ByteArrayDataBuffer bb) {
      final SememeType st = SememeType.getFromToken(token);

      switch (st) {
      case MEMBER:
         return new SememeVersionImpl<>(container, stampSequence, versionSequence);

      case COMPONENT_NID:
         return new ComponentNidSememeImpl(
             (SememeChronologyImpl<ComponentNidSememeImpl>) container,
             stampSequence,
             versionSequence,
             bb);

      case LONG:
         return new LongSememeImpl((SememeChronologyImpl<LongSememeImpl>) container,
               stampSequence,
               versionSequence,
               bb);

      case LOGIC_GRAPH:
         return new LogicGraphSememeImpl((SememeChronologyImpl<LogicGraphSememeImpl>) container,
               stampSequence,
               versionSequence,
               bb);

      case DYNAMIC:
         return new DynamicSememeImpl((SememeChronologyImpl<DynamicSememeImpl>) container,
               stampSequence,
               versionSequence,
               bb);

      case STRING:
         return new StringSememeImpl((SememeChronologyImpl<StringSememeImpl>) container,
               stampSequence,
               versionSequence,
               bb);

      case DESCRIPTION:
         return (new DescriptionSememeImpl(
             (SememeChronologyImpl<DescriptionSememeImpl>) container,
             stampSequence,
             versionSequence,
             bb));

      default:
         throw new UnsupportedOperationException("Can't handle: " + token);
      }
   }

   public static SememeChronologyImpl make(ByteArrayDataBuffer data) {
      final SememeChronologyImpl sememeChronology = new SememeChronologyImpl();

      sememeChronology.readData(data);
      return sememeChronology;
   }

   @Override
   public String toString() {
      final StringBuilder builder = new StringBuilder();

      builder.append("SememeChronology{");

      if (this.sememeTypeToken == -1) {
         builder.append("SememeType token not initialized");
      } else {
         builder.append(SememeType.getFromToken(this.sememeTypeToken));
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
         builder.append("SEMEME: ")
                .append(Get.sememeService()
                           .getSememe(this.referencedComponentNid));
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
      super.toString(builder);
      builder.append('}');
      return builder.toString();
   }

   @Override
   public void writeChronicleData(ByteArrayDataBuffer data) {
      super.writeChronicleData(data);
   }

   protected <M extends V> M createMutableVersionInternal(Class<M> type,
         int stampSequence,
         short versionSequence)
            throws UnsupportedOperationException {
      switch (getSememeType()) {
      case COMPONENT_NID:
         if (MutableComponentNidSememe.class.isAssignableFrom(type)) {
            return (M) new ComponentNidSememeImpl((SememeChronologyImpl<ComponentNidSememeImpl>) this,
                  stampSequence,
                  versionSequence);
         }

         break;

      case LONG:
         if (LongSememe.class.isAssignableFrom(type)) {
            return (M) new LongSememeImpl((SememeChronologyImpl<LongSememeImpl>) this, stampSequence, versionSequence);
         }

         break;

      case DYNAMIC:
         if (MutableDynamicSememe.class.isAssignableFrom(type)) {
            return (M) new DynamicSememeImpl((SememeChronologyImpl<DynamicSememeImpl>) this,
                                             stampSequence,
                                             versionSequence);
         }

         break;

      case LOGIC_GRAPH:
         if (MutableLogicGraphSememe.class.isAssignableFrom(type)) {
            return (M) new LogicGraphSememeImpl((SememeChronologyImpl<LogicGraphSememeImpl>) this,
                  stampSequence,
                  versionSequence);
         }

         break;

      case STRING:
         if (StringSememe.class.isAssignableFrom(type)) {
            return (M) new StringSememeImpl((SememeChronologyImpl<StringSememeImpl>) this,
                                            stampSequence,
                                            versionSequence);
         }

         break;

      case MEMBER:
         if (SememeVersion.class.isAssignableFrom(type)) {
            return (M) new SememeVersionImpl(this, stampSequence, versionSequence);
         }

         break;

      case DESCRIPTION:
         if (DescriptionSememe.class.isAssignableFrom(type)) {
            return (M) new DescriptionSememeImpl((SememeChronologyImpl<DescriptionSememeImpl>) this,
                  stampSequence,
                  versionSequence);
         }

         break;

      default:
         throw new UnsupportedOperationException("Can't handle: " + getSememeType());
      }

      throw new UnsupportedOperationException("Chronicle is of type: " + getSememeType() +
            " cannot create version of type: " + type.getCanonicalName());
   }

   @Override
   protected V makeVersion(int stampSequence, ByteArrayDataBuffer db) {
      return (V) createSememe(this.sememeTypeToken, this, stampSequence, db.getShort(), db);
   }

   @Override
   protected void putAdditionalChronicleFields(ByteArrayDataBuffer out) {
      out.putByte(this.sememeTypeToken);
      out.putConceptSequence(this.assemblageSequence);
      out.putNid(this.referencedComponentNid);
   }

   @Override
   protected void skipAdditionalChronicleFields(ByteArrayDataBuffer in) {
      in.getByte();             // sememeTypeToken =
      in.getConceptSequence();  // assemblageSequence =
      in.getNid();              // referencedComponentNid =
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   protected void getAdditionalChronicleFields(ByteArrayDataBuffer in) {
      this.sememeTypeToken        = in.getByte();
      this.assemblageSequence     = in.getConceptSequence();
      this.referencedComponentNid = in.getNid();
   }

   @Override
   public int getAssemblageSequence() {
      return this.assemblageSequence;
   }

   @Override
   public byte getDataFormatVersion() {
      return 0;
   }

   @Override
   public OchreExternalizableObjectType getOchreObjectType() {
      return OchreExternalizableObjectType.SEMEME;
   }

   @Override
   public int getReferencedComponentNid() {
      return this.referencedComponentNid;
   }

   @Override
   public int getSememeSequence() {
      return getContainerSequence();
   }

   @Override
   public SememeType getSememeType() {
      return SememeType.getFromToken(this.sememeTypeToken);
   }
}

