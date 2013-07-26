/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.ihtsdo.otf.tcc.dto;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptAttributeAB;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptCB;
import org.ihtsdo.otf.tcc.api.blueprint.DescriptionCAB;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.MediaCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.RelationshipCAB;
import org.ihtsdo.otf.tcc.dto.component.TtkComponentChronicle;
import org.ihtsdo.otf.tcc.dto.component.attribute.TtkConceptAttributesChronicle;
import org.ihtsdo.otf.tcc.dto.component.description.TtkDescriptionChronicle;
import org.ihtsdo.otf.tcc.dto.component.media.TtkMediaChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.TtkRefexAbstractMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_array_of_bytearray.TtkRefexArrayOfByteArrayMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_boolean.TtkRefexBooleanMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_int.TtkRefexIntMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_long.TtkRefexLongMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_member.TtkRefexMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_string.TtkRefexStringMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid.TtkRefexUuidMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_boolean.TtkRefexUuidBooleanMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_float.TtkRefexUuidFloatMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_int.TtkRefexUuidIntMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_long.TtkRefexUuidLongMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_string.TtkRefexUuidStringMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid.TtkRefexUuidUuidMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_string.TtkRefexUuidUuidStringMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid.TtkRefexUuidUuidUuidMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_float.TtkRefexUuidUuidUuidFloatMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_int.TtkRefexUuidUuidUuidIntMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_long.TtkRefexUuidUuidUuidLongMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_string.TtkRefexUuidUuidUuidStringMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.relationship.TtkRelationshipChronicle;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.List;
import java.util.UUID;

/**
 *
 * @authorUuid kec
 */
public class UuidDtoBuilder {

   /** Field description */
   long time;

   /** Field description */
   UUID authorUuid;

   /** Field description */
   UUID pathUuid;

   UUID moduleUuid;

   /**
    * Constructs ...
    *
    *
    * @param time
    * @param authorUuid
    * @param pathUuid
    */
   public UuidDtoBuilder(long time, UUID authorUuid, UUID pathUuid, UUID moduleUuid) {
      this.time   = time;
      this.authorUuid = authorUuid;
      this.pathUuid   = pathUuid;
      this.moduleUuid = moduleUuid;
   }

   /**
    * Method description
    *
    *
    * @param blueprint
    *
    * @return
    *
    * @throws ContradictionException
    * @throws IOException
    * @throws InvalidCAB
    */
   public TtkConceptChronicle construct(ConceptCB blueprint)
           throws IOException, InvalidCAB, ContradictionException {
      TtkConceptChronicle newC = new TtkConceptChronicle();

      newC.setAnnotationStyleRefex(blueprint.isAnnotationRefexExtensionIdentity());
      newC.setPrimordialUuid(blueprint.getComponentUuid());
      construct(blueprint.getConceptAttributeAB(), newC);

      List<DescriptionCAB>  fsnBps   = blueprint.getFullySpecifiedNameCABs();
      List<DescriptionCAB>  prefBps  = blueprint.getPreferredNameCABs();
      List<DescriptionCAB>  descBps  = blueprint.getDescriptionCABs();
      List<RelationshipCAB>   relBps   = blueprint.getRelationshipCABs();
      List<MediaCAB> mediaBps = blueprint.getMediaCABs();

      for (DescriptionCAB fsnBp : fsnBps) {
         this.construct(fsnBp, newC);
      }

      for (DescriptionCAB prefBp : prefBps) {
         this.construct(prefBp, newC);
      }

      for (DescriptionCAB descBp : descBps) {
         if (fsnBps.contains(descBp) || prefBps.contains(descBp)) {
            continue;
         } else {
            this.construct(descBp, newC);
         }
      }

      for (RelationshipCAB relBp : relBps) {
         this.construct(relBp, newC);
      }

      for (MediaCAB mediaBp : mediaBps) {
         construct(mediaBp, newC);
      }

      return newC;
   }

   /**
    * Method description
    *
    *
    * @param blueprint
    * @param c
    *
    * @throws ContradictionException
    * @throws IOException
    * @throws InvalidCAB
    */
   private void construct(ConceptAttributeAB blueprint, TtkConceptChronicle c)
           throws IOException, InvalidCAB, ContradictionException {
      TtkConceptAttributesChronicle ca = new TtkConceptAttributesChronicle();

      ca.primordialUuid = c.primordialUuid;
      ca.defined        = blueprint.defined;
      ca.status     = blueprint.getStatus();
      ca.time           = time;
      ca.authorUuid     = authorUuid;
      ca.moduleUuid     = moduleUuid;
      ca.pathUuid       = pathUuid;

      for (RefexCAB annotBp : blueprint.getAnnotationBlueprints()) {
         construct(annotBp, ca);
      }

      c.conceptAttributes = ca;
   }

   /**
    * Method description
    *
    *
    * @param blueprint
    * @param c
    *
    * @throws ContradictionException
    * @throws IOException
    * @throws InvalidCAB
    */
   private void construct(DescriptionCAB blueprint, TtkConceptChronicle c)
           throws IOException, InvalidCAB, ContradictionException {
      TtkDescriptionChronicle d = new TtkDescriptionChronicle();

      d.primordialUuid = blueprint.getComponentUuid();
      d.conceptUuid    = c.primordialUuid;
      d.typeUuid       = blueprint.getTypeUuid();
      d.setLang(blueprint.getLang());
      d.setText(blueprint.getText());
      d.setInitialCaseSignificant(blueprint.isInitialCaseSignificant());
      d.status = blueprint.getStatus();
      d.time       = time;
      d.authorUuid = authorUuid;
      d.moduleUuid = moduleUuid;
      d.pathUuid   = pathUuid;

      for (RefexCAB annotBp : blueprint.getAnnotationBlueprints()) {
         construct(annotBp, d);
      }

      c.getDescriptions().add(d);
   }

   /**
    * Method description
    *
    *
    * @param blueprint
    * @param c
    *
    * @throws ContradictionException
    * @throws IOException
    * @throws InvalidCAB
    */
   private void construct(MediaCAB blueprint, TtkConceptChronicle c)
           throws IOException, InvalidCAB, ContradictionException {
      TtkMediaChronicle img = new TtkMediaChronicle();

      img.primordialUuid  = blueprint.getComponentUuid();
      img.conceptUuid     = c.primordialUuid;
      img.dataBytes       = blueprint.dataBytes;
      img.format          = blueprint.format;
      img.textDescription = blueprint.textDescription;
      img.typeUuid        = blueprint.getTypeUuid();
      img.status     = blueprint.getStatus();
      img.time            = time;
      img.authorUuid      = authorUuid;
      img.moduleUuid      = moduleUuid;
      img.pathUuid        = pathUuid;

      for (RefexCAB annotBp : blueprint.getAnnotationBlueprints()) {
         construct(annotBp, img);
      }

      c.getMedia().add(img);
   }

   /**
    * Method description
    *
    *
    * @param blueprint
    * @param component
    *
    * @throws ContradictionException
    * @throws IOException
    * @throws InvalidCAB
    */
   private void construct(RefexCAB blueprint, TtkComponentChronicle component)
           throws IOException, InvalidCAB, ContradictionException {
      TtkRefexAbstractMemberChronicle annot = createRefex(blueprint);

      component.getAnnotations().add(annot);

      for (RefexCAB childBp : blueprint.getAnnotationBlueprints()) {
         construct(childBp, annot);
      }
   }

   /**
    * Method description
    *
    *
    * @param blueprint
    * @param c
    *
    * @throws ContradictionException
    * @throws IOException
    * @throws InvalidCAB
    */
   private void construct(RelationshipCAB blueprint, TtkConceptChronicle c)
           throws IOException, InvalidCAB, ContradictionException {
      TtkRelationshipChronicle r = new TtkRelationshipChronicle();

      r.primordialUuid     = blueprint.getComponentUuid();
      r.c1Uuid             = c.getPrimordialUuid();
      r.c2Uuid             = blueprint.getTargetUuid();
      r.characteristicUuid = blueprint.getCharacteristicUuid();
      r.group              = blueprint.getGroup();
      r.typeUuid           = blueprint.getTypeUuid();
      r.refinabilityUuid   = blueprint.getRefinabilityUuid();
      r.status         = blueprint.getStatus();
      r.time               = time;
      r.authorUuid         = authorUuid;
      r.moduleUuid         = moduleUuid;
      r.pathUuid           = pathUuid;

      for (RefexCAB annotBp : blueprint.getAnnotationBlueprints()) {
         construct(annotBp, r);
      }

      c.getRelationships().add(r);
   }

   /**
    * Method description
    *
    *
    * @param blueprint
    *
    * @return
    *
    * @throws ContradictionException
    * @throws IOException
    * @throws InvalidCAB
    */
   private TtkRefexAbstractMemberChronicle createRefex(RefexCAB blueprint)
           throws IOException, InvalidCAB, ContradictionException {
      switch (blueprint.getMemberType()) {
      case ARRAY_BYTEARRAY :
         TtkRefexArrayOfByteArrayMemberChronicle rm1 = new TtkRefexArrayOfByteArrayMemberChronicle();

         rm1.arrayOfByteArray1 = blueprint.getArrayOfByteArray();
         setStandardFields(rm1, blueprint);

         return rm1;

      case BOOLEAN :
         TtkRefexBooleanMemberChronicle rm2 = new TtkRefexBooleanMemberChronicle();

         rm2.booleanValue = blueprint.getBoolean(ComponentProperty.BOOLEAN_EXTENSION_1);
         setStandardFields(rm2, blueprint);

         return rm2;

      case CID :
         TtkRefexUuidMemberChronicle rm3 = new TtkRefexUuidMemberChronicle();

         rm3.uuid1 = blueprint.getUuid(ComponentProperty.COMPONENT_EXTENSION_1_ID);
         setStandardFields(rm3, blueprint);

         return rm3;

      case CID_CID :
         TtkRefexUuidUuidMemberChronicle rm4 = new TtkRefexUuidUuidMemberChronicle();

         rm4.uuid1 = blueprint.getUuid(ComponentProperty.COMPONENT_EXTENSION_1_ID);
         rm4.uuid2 = blueprint.getUuid(ComponentProperty.COMPONENT_EXTENSION_2_ID);
         setStandardFields(rm4, blueprint);

         return rm4;

      case CID_CID_CID :
         TtkRefexUuidUuidUuidMemberChronicle rm5 = new TtkRefexUuidUuidUuidMemberChronicle();

         rm5.uuid1 = blueprint.getUuid(ComponentProperty.COMPONENT_EXTENSION_1_ID);
         rm5.uuid2 = blueprint.getUuid(ComponentProperty.COMPONENT_EXTENSION_2_ID);
         rm5.uuid3 = blueprint.getUuid(ComponentProperty.COMPONENT_EXTENSION_3_ID);
         setStandardFields(rm5, blueprint);

         return rm5;

      case CID_CID_CID_FLOAT :
         TtkRefexUuidUuidUuidFloatMemberChronicle rm6 = new TtkRefexUuidUuidUuidFloatMemberChronicle();

         rm6.uuid1  = blueprint.getUuid(ComponentProperty.COMPONENT_EXTENSION_1_ID);
         rm6.uuid2  = blueprint.getUuid(ComponentProperty.COMPONENT_EXTENSION_2_ID);
         rm6.uuid3  = blueprint.getUuid(ComponentProperty.COMPONENT_EXTENSION_3_ID);
         rm6.float1 = blueprint.getFloat(ComponentProperty.FLOAT_EXTENSION_1);
         setStandardFields(rm6, blueprint);

         return rm6;

      case CID_CID_CID_INT :
         TtkRefexUuidUuidUuidIntMemberChronicle rm7 = new TtkRefexUuidUuidUuidIntMemberChronicle();

         rm7.uuid1 = blueprint.getUuid(ComponentProperty.COMPONENT_EXTENSION_1_ID);
         rm7.uuid2 = blueprint.getUuid(ComponentProperty.COMPONENT_EXTENSION_2_ID);
         rm7.uuid3 = blueprint.getUuid(ComponentProperty.COMPONENT_EXTENSION_3_ID);
         rm7.int1  = blueprint.getInt(ComponentProperty.INTEGER_EXTENSION_1);
         setStandardFields(rm7, blueprint);

         return rm7;

      case CID_CID_CID_LONG :
         TtkRefexUuidUuidUuidLongMemberChronicle rm8 = new TtkRefexUuidUuidUuidLongMemberChronicle();

         rm8.uuid1 = blueprint.getUuid(ComponentProperty.COMPONENT_EXTENSION_1_ID);
         rm8.uuid2 = blueprint.getUuid(ComponentProperty.COMPONENT_EXTENSION_2_ID);
         rm8.uuid3 = blueprint.getUuid(ComponentProperty.COMPONENT_EXTENSION_3_ID);
         rm8.long1 = blueprint.getLong(ComponentProperty.LONG_EXTENSION_1);
         setStandardFields(rm8, blueprint);

         return rm8;

      case CID_CID_CID_STRING :
         TtkRefexUuidUuidUuidStringMemberChronicle rm9 = new TtkRefexUuidUuidUuidStringMemberChronicle();

         rm9.uuid1   = blueprint.getUuid(ComponentProperty.COMPONENT_EXTENSION_1_ID);
         rm9.uuid2   = blueprint.getUuid(ComponentProperty.COMPONENT_EXTENSION_2_ID);
         rm9.uuid3   = blueprint.getUuid(ComponentProperty.COMPONENT_EXTENSION_3_ID);
         rm9.string1 = blueprint.getString(ComponentProperty.STRING_EXTENSION_1);
         setStandardFields(rm9, blueprint);

         return rm9;

      case CID_BOOLEAN :
         TtkRefexUuidBooleanMemberChronicle rm9b = new TtkRefexUuidBooleanMemberChronicle();

         rm9b.uuid1    = blueprint.getUuid(ComponentProperty.COMPONENT_EXTENSION_1_ID);
         rm9b.boolean1 = blueprint.getBoolean(ComponentProperty.BOOLEAN_EXTENSION_1);

         return rm9b;

      case CID_CID_STR :
         TtkRefexUuidUuidStringMemberChronicle rm10 = new TtkRefexUuidUuidStringMemberChronicle();

         rm10.uuid1   = blueprint.getUuid(ComponentProperty.COMPONENT_EXTENSION_1_ID);
         rm10.uuid2   = blueprint.getUuid(ComponentProperty.COMPONENT_EXTENSION_2_ID);
         rm10.string1 = blueprint.getString(ComponentProperty.STRING_EXTENSION_1);
         setStandardFields(rm10, blueprint);

         return rm10;

      case CID_FLOAT :
         TtkRefexUuidFloatMemberChronicle rm11 = new TtkRefexUuidFloatMemberChronicle();

         rm11.uuid1  = blueprint.getUuid(ComponentProperty.COMPONENT_EXTENSION_1_ID);
         rm11.float1 = blueprint.getFloat(ComponentProperty.FLOAT_EXTENSION_1);
         setStandardFields(rm11, blueprint);

         return rm11;

      case CID_INT :
         TtkRefexUuidIntMemberChronicle rm12 = new TtkRefexUuidIntMemberChronicle();

         rm12.uuid1 = blueprint.getUuid(ComponentProperty.COMPONENT_EXTENSION_1_ID);
         rm12.int1  = blueprint.getInt(ComponentProperty.INTEGER_EXTENSION_1);
         setStandardFields(rm12, blueprint);

         return rm12;

      case CID_LONG :
         TtkRefexUuidLongMemberChronicle rm13 = new TtkRefexUuidLongMemberChronicle();

         rm13.uuid1 = blueprint.getUuid(ComponentProperty.COMPONENT_EXTENSION_1_ID);
         rm13.long1 = blueprint.getLong(ComponentProperty.LONG_EXTENSION_1);
         setStandardFields(rm13, blueprint);

         return rm13;

      case CID_STR :
         TtkRefexUuidStringMemberChronicle rm14 = new TtkRefexUuidStringMemberChronicle();

         rm14.uuid1   = blueprint.getUuid(ComponentProperty.COMPONENT_EXTENSION_1_ID);
         rm14.string1 = blueprint.getString(ComponentProperty.STRING_EXTENSION_1);
         setStandardFields(rm14, blueprint);

         return rm14;

      case INT :
         TtkRefexIntMemberChronicle rm15 = new TtkRefexIntMemberChronicle();

         rm15.int1 = blueprint.getInt(ComponentProperty.INTEGER_EXTENSION_1);
         setStandardFields(rm15, blueprint);

         return rm15;

      case LONG :
         TtkRefexLongMemberChronicle rm16 = new TtkRefexLongMemberChronicle();

         rm16.long1 = blueprint.getLong(ComponentProperty.LONG_EXTENSION_1);
         setStandardFields(rm16, blueprint);

         return rm16;

      case MEMBER :
         TtkRefexMemberChronicle rm17 = new TtkRefexMemberChronicle();

         setStandardFields(rm17, blueprint);

         return rm17;

      case STR :
         TtkRefexStringMemberChronicle rm18 = new TtkRefexStringMemberChronicle();

         rm18.string1 = blueprint.getString(ComponentProperty.STRING_EXTENSION_1);
         setStandardFields(rm18, blueprint);

         return rm18;

      case UNKNOWN :
      default :
         throw new UnsupportedOperationException("Can't handle: " + blueprint.getMemberType());
      }
   }

   /**
    * Method description
    *
    *
    * @param rm1
    * @param blueprint
    *
    * @throws IOException
    */
   private void setStandardFields(TtkRefexAbstractMemberChronicle rm1, RefexCAB blueprint) throws IOException {
      rm1.primordialUuid     = blueprint.getMemberUUID();
      rm1.componentUuid      = blueprint.getComponentUuid();
      rm1.refexExtensionUuid = blueprint.getRefexCollectionUuid();
      rm1.status         = blueprint.getStatus();
      rm1.time               = time;
      rm1.authorUuid         = authorUuid;
      rm1.moduleUuid         = moduleUuid;
   }
}
