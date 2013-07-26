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



package org.ihtsdo.otf.tcc.ddo.concept.component;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.SimpleObjectProperty;

import org.ihtsdo.otf.tcc.ddo.ComponentReference;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.chronicle.TypedComponentVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

/**
 *
 * @author kec
 */
public class TypedComponentVersionDdo<V extends ComponentChronicleDdo, T extends TypedComponentVersionDdo>
        extends ComponentVersionDdo<V, T> {
   protected SimpleObjectProperty<ComponentReference> typeReferenceProperty =
      new SimpleObjectProperty<>(this, "type");

   //~--- constructors --------------------------------------------------------

   public TypedComponentVersionDdo() {}

   public TypedComponentVersionDdo(V chronicle, TerminologySnapshotDI ss, TypedComponentVersionBI another)
           throws IOException, ContradictionException {
      super(chronicle, ss, another);
      typeReferenceProperty.set(new ComponentReference(ss.getConceptForNid(another.getTypeNid())));
   }

   //~--- methods -------------------------------------------------------------
  /**
    * Returns a string representation of the object.
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(" type:");
      buff.append(getTypeReference().getText());
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }


   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (obj instanceof TypedComponentVersionDdo) {
         TypedComponentVersionDdo another = (TypedComponentVersionDdo) obj;

         if (!this.typeReferenceProperty.equals(another.typeReferenceProperty)) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   public final SimpleObjectProperty<ComponentReference> typeReferenceProperty() {
      return typeReferenceProperty;
   }

   //~--- get methods ---------------------------------------------------------

   public final ComponentReference getTypeReference() {
      return typeReferenceProperty.get();
   }

   //~--- set methods ---------------------------------------------------------

   public final void setTypeReference(ComponentReference typeReference) {
      this.typeReferenceProperty.set(typeReference);
   }
}
