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


import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDynamicCAB;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.TtkRefexDynamicMemberChronicle;
import org.ihtsdo.otf.tcc.model.cc.PersistentStore;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;

import java.beans.PropertyVetoException;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * 
 * {@link RefexDynamicMemberFactory}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexDynamicMemberFactory {


   public static RefexDynamicMember create(RefexDynamicCAB res, EditCoordinate ec, ViewCoordinate vc)
           throws IOException, InvalidCAB, ContradictionException {
      RefexDynamicMember member = createBlank(res);

      return reCreate(res, member, ec, vc);
   }

   public static RefexDynamicMember create(TtkRefexDynamicMemberChronicle refsetMember, int enclosingConceptNid)
           throws IOException {
       return new RefexDynamicMember(refsetMember, enclosingConceptNid);
   }

   private static RefexDynamicMember createBlank(RefexDynamicCAB res) {
       
       return new RefexDynamicMember();
   }


   public static RefexDynamicMember reCreate(RefexDynamicCAB blueprint, RefexDynamicMember member, EditCoordinate ec, ViewCoordinate vc)
           throws IOException, InvalidCAB, ContradictionException {
      blueprint.validate(vc);
      ConceptChronicle refexColCon = (ConceptChronicle) PersistentStore.get().getConcept(blueprint.getRefexAssemblageNid());

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
           PersistentStore.get().setConceptNidForNid(member.enclosingConceptNid, member.nid);
           refexColCon.getData().add(member);
       }

      for (int i = 0; i < ec.getEditPaths().size(); i++) {
         if (i == 0) {
            member.setSTAMP(PersistentStore.get().getStamp(blueprint.getStatus(), Long.MAX_VALUE,
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
