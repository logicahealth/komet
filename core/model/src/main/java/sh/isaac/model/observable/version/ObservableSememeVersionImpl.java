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



package sh.isaac.model.observable.version;

//~--- non-JDK imports --------------------------------------------------------

import java.util.List;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyProperty;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.model.observable.CommitAwareIntegerProperty;
import sh.isaac.model.observable.ObservableChronologyImpl;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.observable.semantic.version.ObservableSemanticVersion;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ObservableSememeVersionImpl.
 *
 * @author kec
 */
public class ObservableSememeVersionImpl
        extends ObservableVersionImpl
         implements ObservableSemanticVersion {

   /** The author sequence property. */
   ReadOnlyIntegerProperty assemblageSequenceProperty;

   /** The module sequence property. */
   ReadOnlyIntegerProperty referencedComponentNidProperty;

   /**
    * Instantiates a new observable sememe version impl.
    *
    * @param stampedVersion the stamped version
    * @param chronology the chronology
    */
   public ObservableSememeVersionImpl(SemanticVersion stampedVersion, ObservableSemanticChronology chronology) {
      super(stampedVersion, 
              chronology);
   }

   @Override
   public <V extends Version> V makeAnalog(EditCoordinate ec) {
      SemanticVersion newVersion = this.stampedVersionProperty.get().makeAnalog(ec);
      ObservableSememeVersionImpl newObservableVersion = 
              new ObservableSememeVersionImpl(newVersion, (ObservableSemanticChronology) chronology);
      ((ObservableChronologyImpl) chronology).getVersionList().add(newObservableVersion);
      return (V) newObservableVersion;
   }


   /**
    * Module sequence property.
    *
    * @return the integer property
    */
   @Override
   public final ReadOnlyIntegerProperty assemblageSequenceProperty() {
      if (this.assemblageSequenceProperty == null) {
         this.assemblageSequenceProperty = 
            ReadOnlyIntegerProperty.readOnlyIntegerProperty(new CommitAwareIntegerProperty(this,
               ObservableFields.ASSEMBLAGE_SEQUENCE_FOR_SEMEME_CHRONICLE.toExternalString(),
               getAssemblageSequence()));
      }

      return this.assemblageSequenceProperty;
   }

   /**
    * Path sequence property.
    *
    * @return the integer property
    */
   @Override
   public final ReadOnlyIntegerProperty referencedComponentNidProperty() {
      if (this.referencedComponentNidProperty == null) {
         this.referencedComponentNidProperty = 
            ReadOnlyIntegerProperty.readOnlyIntegerProperty(new CommitAwareIntegerProperty(this,
               ObservableFields.REFERENCED_COMPONENT_NID_FOR_SEMEME_CHRONICLE.toExternalString(),
               getReferencedComponentNid()));
      }
      return this.referencedComponentNidProperty;
   }
   @Override
   public List<ReadOnlyProperty<?>> getProperties() {
      List<ReadOnlyProperty<?>> properties = super.getProperties();
      properties.add(assemblageSequenceProperty());
      properties.add(referencedComponentNidProperty());
      return properties;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the assemblage sequence.
    *
    * @return the assemblage sequence
    */
   @Override
   public int getAssemblageSequence() {
      return ((SemanticVersion) this.stampedVersionProperty.get()).getAssemblageSequence();
   }

   /**
    * Gets the chronology.
    *
    * @return the chronology
    */
   @Override
   public ObservableSemanticChronology getChronology() {
      return (ObservableSemanticChronology) this.chronology;
   }

   /**
    * Gets the referenced component nid.
    *
    * @return the referenced component nid
    */
   @Override
   public int getReferencedComponentNid() {
      return ((SemanticVersion) this.stampedVersionProperty.get()).getReferencedComponentNid();
   }

   /**
    * Gets the sememe sequence.
    *
    * @return the sememe sequence
    */
   @Override
   public int getSemanticSequence() {
      return ((SemanticVersion) this.stampedVersionProperty.get()).getSemanticSequence();
   }
   @Override
   protected void updateVersion() {
      // nothing to update
      // only read-only values in this subclass. 
   }
   
}

