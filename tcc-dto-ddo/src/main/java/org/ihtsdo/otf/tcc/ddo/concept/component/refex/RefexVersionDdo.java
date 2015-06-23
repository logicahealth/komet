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



package org.ihtsdo.otf.tcc.ddo.concept.component.refex;

//~--- non-JDK imports --------------------------------------------------------

import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import org.ihtsdo.otf.tcc.ddo.ComponentReference;
import org.ihtsdo.otf.tcc.ddo.concept.component.ComponentVersionDdo;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp.RefexCompVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_long.RefexLongVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_string.RefexStringVersionDdo;

/**
 *
 * @author kec
 */
@XmlSeeAlso( {
    RefexCompVersionDdo.class, 
    RefexLongVersionDdo.class, 
    RefexStringVersionDdo.class, 
    RefexCompVersionDdo.class, 
})
public class RefexVersionDdo<T extends RefexChronicleDdo, V extends RefexVersionDdo>
        extends ComponentVersionDdo<T, V> {
   public RefexVersionDdo() {}

   public RefexVersionDdo(T chronicle, TaxonomyCoordinate ss, SememeVersion<?> another)
           throws IOException, ContradictionException {
      super(chronicle, ss, another);
   }

   //~--- get methods ---------------------------------------------------------

   @XmlTransient
   public ComponentReference getComponentRef() {
      return chronicle.referencedComponentReference;
   }

   @XmlTransient
   public ComponentReference getRefexRef() {
      return chronicle.refexExtensionIdentifierReference;
   }

   @XmlTransient
   public REFEX_TYPE_DDO getType() {
      return chronicle.getType();
   }
}
