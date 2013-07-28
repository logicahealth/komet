package org.ihtsdo.otf.tcc.chronicle.cc.refex;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.chronicle.cc.P;
import org.ihtsdo.otf.tcc.chronicle.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.type_array_of_bytearray.ArrayOfByteArrayMember;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.type_boolean.BooleanMember;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.type_int.IntMember;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.type_long.LongMember;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.type_membership.MembershipMember;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.type_nid.NidMember;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.type_nid_boolean.NidBooleanMember;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.type_nid_float.NidFloatMember;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.type_nid_int.NidIntMember;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.type_nid_long.NidLongMember;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.type_nid_nid.NidNidMember;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.type_nid_nid_nid.NidNidNidMember;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.type_nid_nid_nid_float.NidNidNidFloatMember;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.type_nid_nid_nid_int.NidNidNidIntMember;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.type_nid_nid_nid_long.NidNidNidLongMember;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.type_nid_nid_nid_string.NidNidNidStringMember;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.type_nid_nid_string.NidNidStringMember;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.type_nid_string.NidStringMember;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.type_string.StringMember;
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

import static org.ihtsdo.otf.tcc.api.refex.RefexType.CID_CID_CID_FLOAT;
import static org.ihtsdo.otf.tcc.api.refex.RefexType.CID_CID_CID_INT;
import static org.ihtsdo.otf.tcc.api.refex.RefexType.CID_CID_CID_LONG;
import static org.ihtsdo.otf.tcc.api.refex.RefexType.CID_CID_CID_STRING;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

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
    * @param nid
    * @param typeToken
    * @param enclosingConceptNid
    * @param input
    *
    * @return
    *
    * @throws IOException
    */
   @SuppressWarnings("rawtypes")
   public static RefexMember create(int nid, int typeToken, int enclosingConceptNid, TupleInput input)
           throws IOException {
      RefexType memberType = RefexType.getFromToken(typeToken);

      switch (memberType) {
      case BOOLEAN :
         return new BooleanMember(enclosingConceptNid, input);

      case CID :
         return new NidMember(enclosingConceptNid, input);

      case CID_CID :
         return new NidNidMember(enclosingConceptNid, input);

      case CID_CID_CID :
         return new NidNidNidMember(enclosingConceptNid, input);

      case CID_CID_STR :
         return new NidNidStringMember(enclosingConceptNid, input);

      case CID_INT :
         return new NidIntMember(enclosingConceptNid, input);

      case CID_STR :
         return new NidStringMember(enclosingConceptNid, input);

      case INT :
         return new IntMember(enclosingConceptNid, input);

      case CID_FLOAT :
         return new NidFloatMember(enclosingConceptNid, input);

      case MEMBER :
         return new MembershipMember(enclosingConceptNid, input);

      case STR :
         return new StringMember(enclosingConceptNid, input);

      case CID_LONG :
         return new NidLongMember(enclosingConceptNid, input);

      case LONG :
         return new LongMember(enclosingConceptNid, input);

      case ARRAY_BYTEARRAY :
         return new ArrayOfByteArrayMember(enclosingConceptNid, input);

      case CID_CID_CID_FLOAT :
         return new NidNidNidFloatMember(enclosingConceptNid, input);

      case CID_CID_CID_INT :
         return new NidNidNidIntMember(enclosingConceptNid, input);

      case CID_CID_CID_LONG :
         return new NidNidNidLongMember(enclosingConceptNid, input);

      case CID_CID_CID_STRING :
         return new NidNidNidStringMember(enclosingConceptNid, input);

      case CID_BOOLEAN :
         return new NidBooleanMember(enclosingConceptNid, input);

      default :
         throw new UnsupportedOperationException("Can't handle member type: " + memberType + " "
             + Ts.get().getConceptForNid(nid).toLongString());
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
      ConceptChronicle refexColCon = (ConceptChronicle) P.s.getConcept(blueprint.getRefexCollectionNid());

      member.refexExtensionNid = refexColCon.getNid();
      member.nid               = P.s.getNidForUuids(blueprint.getMemberUUID());

      if (refexColCon.isAnnotationStyleRefex()) {
         int rcNid = P.s.getNidForUuids(blueprint.getReferencedComponentUuid());

         member.enclosingConceptNid = P.s.getConceptNidForNid(rcNid);
         P.s.setConceptNidForNid(member.enclosingConceptNid, member.nid);

         ComponentChronicleBI<?> component = blueprint.getReferencedComponent();
         if (component == null) {
             component = P.s.getComponent(blueprint.getReferencedComponentUuid());
         }

         if (component == null) {
            throw new InvalidCAB("Component for annotation is null. Blueprint: " + blueprint);
         }

         component.addAnnotation(member);
         if (refexColCon.isAnnotationIndex()) {
             // TODO: add support for indexed annotations...
             throw new UnsupportedOperationException();
         }
      } else {
         member.enclosingConceptNid = refexColCon.getNid();
         P.s.setConceptNidForNid(member.enclosingConceptNid, member.nid);
         refexColCon.getData().add(member);
      }

      for (int i = 0; i < ec.getEditPaths().size(); i++) {
         if (i == 0) {
            member.setSTAMP(P.s.getStamp(blueprint.getStatus(), Long.MAX_VALUE,
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
