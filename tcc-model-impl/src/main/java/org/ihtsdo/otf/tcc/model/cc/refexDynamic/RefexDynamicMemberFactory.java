/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.otf.tcc.model.cc.refexDynamic;


import com.sleepycat.bind.tuple.TupleInput;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDynamicCAB;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.model.cc.P;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;


//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;
import java.io.IOException;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;

/**
 * 
 * {@link RefexDynamicMemberFactory}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexDynamicMemberFactory {


   public static RefexDynamicMember create(RefexDynamicCAB res, EditCoordinate ec)
           throws IOException, InvalidCAB, ContradictionException {
      RefexDynamicMember member = createBlank(res);

      return reCreate(res, member, ec);
   }

//TODO [REFEX] implement the eConcept side of the API
//   public static RefexDynamicMember create(TtkRefexAbstractMemberChronicle<?> refsetMember, int enclosingConceptNid)
//           throws IOException {
//      switch (refsetMember.getType()) {
//      case BOOLEAN :
//         return new BooleanMember((TtkRefexBooleanMemberChronicle) refsetMember, enclosingConceptNid);
//
//      case CID :
//         return new NidMember((TtkRefexUuidMemberChronicle) refsetMember, enclosingConceptNid);
//
//      case CID_CID :
//         return new NidNidMember((TtkRefexUuidUuidMemberChronicle) refsetMember, enclosingConceptNid);
//
//      case CID_CID_CID :
//         return new NidNidNidMember((TtkRefexUuidUuidUuidMemberChronicle) refsetMember, enclosingConceptNid);
//
//      case CID_CID_STR :
//         return new NidNidStringMember((TtkRefexUuidUuidStringMemberChronicle) refsetMember, enclosingConceptNid);
//
//      case CID_INT :
//         return new NidIntMember((TtkRefexUuidIntMemberChronicle) refsetMember, enclosingConceptNid);
//
//      case CID_STR :
//         return new NidStringMember((TtkRefexUuidStringMemberChronicle) refsetMember, enclosingConceptNid);
//
//      case INT :
//         return new IntMember((TtkRefexIntMemberChronicle) refsetMember, enclosingConceptNid);
//
//      case CID_FLOAT :
//         return new NidFloatMember((TtkRefexUuidFloatMemberChronicle) refsetMember, enclosingConceptNid);
//
//      case MEMBER :
//         return new MembershipMember((TtkRefexDynamicMemberChronicle) refsetMember, enclosingConceptNid);
//
//      case STR :
//         return new StringMember((TtkRefexStringMemberChronicle) refsetMember, enclosingConceptNid);
//
//      case CID_LONG :
//         return new NidLongMember((TtkRefexUuidLongMemberChronicle) refsetMember, enclosingConceptNid);
//
//      case LONG :
//         return new LongMember((TtkRefexLongMemberChronicle) refsetMember, enclosingConceptNid);
//
//      case ARRAY_BYTEARRAY :
//         return new ArrayOfByteArrayMember((TtkRefexArrayOfByteArrayMemberChronicle) refsetMember, enclosingConceptNid);
//
//      case CID_CID_CID_FLOAT :
//         return new NidNidNidFloatMember((TtkRefexUuidUuidUuidFloatMemberChronicle) refsetMember, enclosingConceptNid);
//
//      case CID_CID_CID_INT :
//         return new NidNidNidIntMember((TtkRefexUuidUuidUuidIntMemberChronicle) refsetMember, enclosingConceptNid);
//
//      case CID_CID_CID_LONG :
//         return new NidNidNidLongMember((TtkRefexUuidUuidUuidLongMemberChronicle) refsetMember, enclosingConceptNid);
//
//      case CID_CID_CID_STRING :
//         return new NidNidNidStringMember((TtkRefexUuidUuidUuidStringMemberChronicle) refsetMember,
//                                          enclosingConceptNid);
//
//      case CID_BOOLEAN :
//         return new NidBooleanMember((TtkRefexUuidBooleanMemberChronicle) refsetMember, enclosingConceptNid);
//
//      default :
//         throw new UnsupportedOperationException("Can't handle member type: " + refsetMember.getType());
//      }
//   }


   public static RefexDynamicMember create(int nid, int enclosingConceptNid, TupleInput input)
           throws IOException 
   {
       return new RefexDynamicMember(enclosingConceptNid, input);
   }

   private static RefexDynamicMember createBlank(RefexDynamicCAB res) {
       
       return new RefexDynamicMember();
   }


   public static RefexDynamicMember reCreate(RefexDynamicCAB blueprint, RefexDynamicMember member, EditCoordinate ec)
           throws IOException, InvalidCAB, ContradictionException {
      blueprint.validate();
      ConceptChronicle refexColCon = (ConceptChronicle) P.s.getConcept(blueprint.getRefexAssemblageNid());

      member.assemblageNid = refexColCon.getNid();
      member.nid               = P.s.getNidForUuids(blueprint.getMemberUUID());

       if (refexColCon.isAnnotationStyleRefex()) {
           int rcNid = P.s.getNidForUuids(blueprint.getReferencedComponentUuid());

           if (blueprint.hasProperty(ComponentProperty.ENCLOSING_CONCEPT_ID)) {
               int setCnid = blueprint.getInt(ComponentProperty.ENCLOSING_CONCEPT_ID);
               if(setCnid != P.s.getConceptNidForNid(rcNid)){
                   throw new InvalidCAB("Set enclosing concept nid does not match computed. Set: " + setCnid
                                        + " Computed: " + P.s.getConceptNidForNid(rcNid));
               }
           }
           
           member.enclosingConceptNid = P.s.getConceptNidForNid(rcNid);
           P.s.setConceptNidForNid(member.enclosingConceptNid, member.nid);

           ComponentChronicleBI<?> component = blueprint.getReferencedComponent();
           if (component == null) {
               component = P.s.getComponent(blueprint.getReferencedComponentUuid());
           }

           if (component == null) {
               throw new InvalidCAB("Component for annotation is null. Blueprint: " + blueprint);
           }

           component.addDynamicAnnotation(member);
       } else {
           if (blueprint.hasProperty(ComponentProperty.ENCLOSING_CONCEPT_ID)) {
               int setCnid = blueprint.getInt(ComponentProperty.ENCLOSING_CONCEPT_ID);
               if(setCnid != refexColCon.getNid()){
                   throw new InvalidCAB("Set enclosing concept nid does not match computed. Set: " + setCnid
                                        + " Computed: " + refexColCon.getNid());
               }
           }
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
               blueprint.writeTo(member, false);
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
