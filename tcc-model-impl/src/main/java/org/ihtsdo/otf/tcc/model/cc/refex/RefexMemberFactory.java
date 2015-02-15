package org.ihtsdo.otf.tcc.model.cc.refex;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.store.Ts;
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
import org.ihtsdo.otf.tcc.model.cc.PersistentStore;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.model.cc.refex.type_array_of_bytearray.ArrayOfByteArrayMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_boolean.BooleanMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_int.IntMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_long.LongMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_membership.MembershipMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid.NidMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_boolean.NidBooleanMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_float.NidFloatMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_int.NidIntMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_long.NidLongMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid.NidNidMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid.NidNidNidMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid_float.NidNidNidFloatMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid_int.NidNidNidIntMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid_long.NidNidNidLongMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_nid_string.NidNidNidStringMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_nid_string.NidNidStringMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_nid_string.NidStringMember;
import org.ihtsdo.otf.tcc.model.cc.refex.type_string.StringMember;

import java.beans.PropertyVetoException;
import java.io.DataInputStream;
import java.io.IOException;

//~--- JDK imports ------------------------------------------------------------

/**
 * Class description
 *
 *
 * @version        Enter version here..., 13/03/27
 * @author         Enter your name here...
 */
public class RefexMemberFactory {

   /**
    * Method description
    *
    *
    * @param res
    * @param ec
    *
    * @return
    *
    * @throws IOException
    * @throws InvalidCAB
    */
   public static RefexMember<?, ?> create(RefexCAB res, EditCoordinate ec)
           throws IOException, InvalidCAB {
      RefexMember<?, ?> member = createBlank(res);

      return reCreate(res, member, ec);
   }

   /**
    * Method description
    *
    *
    * @param refsetMember
    * @param enclosingConceptNid
    *
    * @return
    *
    * @throws IOException
    */
   public static RefexMember<?, ?> create(TtkRefexAbstractMemberChronicle<?> refsetMember, int enclosingConceptNid)
           throws IOException {
      switch (refsetMember.getType()) {
      case BOOLEAN :
         return new BooleanMember((TtkRefexBooleanMemberChronicle) refsetMember, enclosingConceptNid);

      case CID :
         return new NidMember((TtkRefexUuidMemberChronicle) refsetMember, enclosingConceptNid);

      case CID_CID :
         return new NidNidMember((TtkRefexUuidUuidMemberChronicle) refsetMember, enclosingConceptNid);

      case CID_CID_CID :
         return new NidNidNidMember((TtkRefexUuidUuidUuidMemberChronicle) refsetMember, enclosingConceptNid);

      case CID_CID_STR :
         return new NidNidStringMember((TtkRefexUuidUuidStringMemberChronicle) refsetMember, enclosingConceptNid);

      case CID_INT :
         return new NidIntMember((TtkRefexUuidIntMemberChronicle) refsetMember, enclosingConceptNid);

      case CID_STR :
         return new NidStringMember((TtkRefexUuidStringMemberChronicle) refsetMember, enclosingConceptNid);

      case INT :
         return new IntMember((TtkRefexIntMemberChronicle) refsetMember, enclosingConceptNid);

      case CID_FLOAT :
         return new NidFloatMember((TtkRefexUuidFloatMemberChronicle) refsetMember, enclosingConceptNid);

      case MEMBER :
         return new MembershipMember((TtkRefexMemberChronicle) refsetMember, enclosingConceptNid);

      case STR :
         return new StringMember((TtkRefexStringMemberChronicle) refsetMember, enclosingConceptNid);

      case CID_LONG :
         return new NidLongMember((TtkRefexUuidLongMemberChronicle) refsetMember, enclosingConceptNid);

      case LONG :
         return new LongMember((TtkRefexLongMemberChronicle) refsetMember, enclosingConceptNid);

      case ARRAY_BYTEARRAY :
         return new ArrayOfByteArrayMember((TtkRefexArrayOfByteArrayMemberChronicle) refsetMember, enclosingConceptNid);

      case CID_CID_CID_FLOAT :
         return new NidNidNidFloatMember((TtkRefexUuidUuidUuidFloatMemberChronicle) refsetMember, enclosingConceptNid);

      case CID_CID_CID_INT :
         return new NidNidNidIntMember((TtkRefexUuidUuidUuidIntMemberChronicle) refsetMember, enclosingConceptNid);

      case CID_CID_CID_LONG :
         return new NidNidNidLongMember((TtkRefexUuidUuidUuidLongMemberChronicle) refsetMember, enclosingConceptNid);

      case CID_CID_CID_STRING :
         return new NidNidNidStringMember((TtkRefexUuidUuidUuidStringMemberChronicle) refsetMember,
                                          enclosingConceptNid);

      case CID_BOOLEAN :
         return new NidBooleanMember((TtkRefexUuidBooleanMemberChronicle) refsetMember, enclosingConceptNid);

      default :
         throw new UnsupportedOperationException("Can't handle member type: " + refsetMember.getType());
      }
   }

   /**
    * Method description
    *
    *
    * @param res
    *
    * @return
    */
   private static RefexMember<?, ?> createBlank(RefexCAB res) {
      switch (res.getMemberType()) {
      case BOOLEAN :
         return new BooleanMember();

      case CID :
         return new NidMember();

      case CID_CID :
         return new NidNidMember();

      case CID_CID_CID :
         return new NidNidNidMember();

      case CID_CID_STR :
         return new NidNidStringMember();

      case CID_INT :
         return new NidIntMember();

      case CID_STR :
         return new NidStringMember();

      case INT :
         return new IntMember();

      case CID_FLOAT :
         return new NidFloatMember();

      case MEMBER :
         return new MembershipMember();

      case STR :
         return new StringMember();

      case CID_LONG :
         return new NidLongMember();

      case LONG :
         return new LongMember();

      case ARRAY_BYTEARRAY :
         return new ArrayOfByteArrayMember();

      case CID_CID_CID_FLOAT :
         return new NidNidNidFloatMember();

      case CID_CID_CID_INT :
         return new NidNidNidIntMember();

      case CID_CID_CID_LONG :
         return new NidNidNidLongMember();

      case CID_CID_CID_STRING :
         return new NidNidNidStringMember();

      case CID_BOOLEAN :
         return new NidBooleanMember();

      default :
         throw new UnsupportedOperationException("Can't handle member type: " + res.getMemberType());
      }
   }

   /**
    * Method description
    *
    *
    * @param blueprint
    * @param member
    * @param ec
    *
    * @return
    *
    * @throws IOException
    * @throws InvalidCAB
    */
   public static RefexMember<?, ?> reCreate(RefexCAB blueprint, RefexMember<?, ?> member, EditCoordinate ec)
           throws IOException, InvalidCAB {
      ConceptChronicle refexColCon = (ConceptChronicle) PersistentStore.get().getConcept(blueprint.getRefexCollectionNid());

      member.assemblageNid = refexColCon.getNid();
      member.nid               = PersistentStore.get().getNidForUuids(blueprint.getMemberUUID());

       if (refexColCon.isAnnotationStyleRefex()) {
           int rcNid = PersistentStore.get().getNidForUuids(blueprint.getReferencedComponentUuid());

           if (blueprint.hasProperty(ComponentProperty.ENCLOSING_CONCEPT_ID)) {
               int setCnid = blueprint.getInt(ComponentProperty.ENCLOSING_CONCEPT_ID);
               if(setCnid != PersistentStore.get().getConceptNidForNid(rcNid)){
                   throw new InvalidCAB("Set enclosing concept nid does not match computed. Set: " + setCnid
                                        + " Computed: " + PersistentStore.get().getConceptNidForNid(rcNid));
               }
           }
           
           member.enclosingConceptNid = PersistentStore.get().getConceptNidForNid(rcNid);
           PersistentStore.get().setConceptNidForNid(member.enclosingConceptNid, member.nid);

           ComponentChronicleBI<?> component = blueprint.getReferencedComponent();
           if (component == null) {
               component = PersistentStore.get().getComponent(blueprint.getReferencedComponentUuid());
           }

           if (component == null) {
               throw new InvalidCAB("Component for annotation is null. Blueprint: " + blueprint);
           }

           component.addAnnotation(member);
       } else {
           if (blueprint.hasProperty(ComponentProperty.ENCLOSING_CONCEPT_ID)) {
               int setCnid = blueprint.getInt(ComponentProperty.ENCLOSING_CONCEPT_ID);
               if(setCnid != refexColCon.getNid()){
                   throw new InvalidCAB("Set enclosing concept nid does not match computed. Set: " + setCnid
                                        + " Computed: " + refexColCon.getNid());
               }
           }
           member.enclosingConceptNid = refexColCon.getNid();
           PersistentStore.get().setConceptNidForNid(member.enclosingConceptNid, member.nid);
           refexColCon.getData().add(member);
       }

      for (int i = 0; i < ec.getEditPaths().size(); i++) {
         if (i == 0) {
            member.setSTAMP(PersistentStore.get().getStamp(blueprint.getStatus(), Long.MAX_VALUE,
                    ec.getAuthorNid(), ec.getModuleNid(), ec.getEditPaths().getSetValues()[i]));
            member.setPrimordialUuid(blueprint.getMemberUUID());

            try {
               blueprint.setPropertiesExceptStamp(member);
            } catch (PropertyVetoException ex) {
               throw new InvalidCAB("RefexAmendmentSpec: " + blueprint, ex);
            }
         } else {
            member.makeAnalog(blueprint.getStatus(), Long.MAX_VALUE, ec.getAuthorNid(),
                              ec.getModuleNid(), ec.getEditPaths().getSetValues()[i]);
         }
      }

      return member;
   }
}
