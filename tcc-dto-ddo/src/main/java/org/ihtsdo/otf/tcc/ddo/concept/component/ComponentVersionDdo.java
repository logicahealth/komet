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

import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.identifier.IdentifierDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexChronicleDdo;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.id.IdBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.List;
import java.util.UUID;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author kec
 */
public class ComponentVersionDdo<T extends ComponentChronicleDdo, V extends ComponentVersionDdo>
        extends VersionDdo {
   @XmlTransient
   protected T chronicle;

   //~--- constructors --------------------------------------------------------

   public ComponentVersionDdo() {}

   public ComponentVersionDdo(T chronicle, TerminologySnapshotDI ss, ComponentVersionBI another)
           throws IOException, ContradictionException {
      super(ss, another);
      this.chronicle = chronicle;
   }

   public ComponentVersionDdo(T chronicle, TerminologySnapshotDI ss, IdBI id)
           throws IOException, ContradictionException {
      super(ss, id);
      this.chronicle = chronicle;
   }

   //~--- methods -------------------------------------------------------------

   public void afterUnmarshal(Unmarshaller u, Object parent) {
      if (parent instanceof ComponentChronicleDdo) {
         this.chronicle = (T) parent;
      }
   }

   //~--- get methods ---------------------------------------------------------

   @XmlTransient
   public List<IdentifierDdo> getAdditionalIds() {
      return this.chronicle.additionalIds;
   }

   @XmlTransient
   public List<RefexChronicleDdo<?,?>> getAnnotations() {
      return this.chronicle.refexes;
   }

   @XmlTransient
   public final T getChronicle() {
      return this.chronicle;
   }

   @XmlTransient
   public ConceptChronicleDdo getConcept() {
      return this.chronicle.concept;
   }

   @XmlTransient
   public int getIdCount() {
      return this.chronicle.getIdCount();
   }

   @XmlTransient
   public UUID getPrimordialComponentUuid() {
      return this.chronicle.getPrimordialComponentUuid();
   }

   @XmlTransient
   public List<UUID> getUuids() {
      return this.chronicle.getUuids();
   }

   @XmlTransient
   public int getVersionCount() {
      return this.chronicle.getVersionCount();
   }

   @XmlTransient
   public final List<V> getVersions() {
      return this.chronicle.getVersions();
   }
   
   @XmlTransient
   public int getComponentNid() {
       return this.chronicle.getComponentNid();
   }
}
