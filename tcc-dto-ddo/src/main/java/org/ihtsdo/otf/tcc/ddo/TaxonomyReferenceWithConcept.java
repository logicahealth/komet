/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
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



package org.ihtsdo.otf.tcc.ddo;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.SimpleObjectProperty;

import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.relationship.RelationshipVersionDdo;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.UUID;

/**
 *
 * @author kec
 */
public class TaxonomyReferenceWithConcept {
   private final SimpleObjectProperty<ConceptChronicleDdo>             conceptProperty             =
      new SimpleObjectProperty<>();
   private final SimpleObjectProperty<RelationshipVersionDdo> relationshipVersionProperty =
      new SimpleObjectProperty<>();
   private WhichConcept whichConcept = WhichConcept.ORIGIN;

   public enum WhichConcept { ORIGIN, DESTINATION }

   public TaxonomyReferenceWithConcept() {}

   public TaxonomyReferenceWithConcept(RelationshipVersionDdo rv)
           throws IOException, ContradictionException {
      relationshipVersionProperty.set(rv);
   }

   public TaxonomyReferenceWithConcept(RelationshipVersionDdo rv, WhichConcept whichConcept)
           throws IOException, ContradictionException {
      this.whichConcept = whichConcept;
      relationshipVersionProperty.set(rv);
   }

   public SimpleObjectProperty<ConceptChronicleDdo> conceptProperty() {
      return conceptProperty;
   }

   public SimpleObjectProperty<RelationshipVersionDdo> relationshipVersionProperty() {
      return relationshipVersionProperty;
   }

   @Override
   public String toString() {
      if (relationshipVersionProperty.get() != null) {
         switch (whichConcept) {
         case ORIGIN :
            return relationshipVersionProperty.get().getOriginReference().getText();

         case DESTINATION :
            return relationshipVersionProperty.get().getDestinationReference().getText();
         }
      }

      if (conceptProperty.get() != null) {
         return conceptProperty.get().getConceptReference().getText();
      }

      return "root";
   }

   public SimpleObjectProperty<ComponentReference> typeReferenceProperty() {
      return relationshipVersionProperty.get().typeReferenceProperty();
   }

   public ConceptChronicleDdo getConcept() {
      return conceptProperty.get();
   }

   public RelationshipVersionDdo getRelationshipVersion() {
      return relationshipVersionProperty.get();
   }

   public ComponentReference getTypeReference() {
       if (relationshipVersionProperty.get() == null) {
           return null;
       }
      return relationshipVersionProperty.get().getTypeReference();
   }

   public void setConcept(ConceptChronicleDdo concept) {
      conceptProperty.set(concept);
   }

   public void setRelationshipVersion(RelationshipVersionDdo fxRelationshipVersion) {
      relationshipVersionProperty.set(fxRelationshipVersion);
   }

   public void setTypeReference(ComponentReference typeRef) {
      relationshipVersionProperty.get().typeReferenceProperty().set(typeRef);
   }
}
