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



package sh.isaac.model.observable;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.IntegerProperty;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import sh.isaac.api.State;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.component.sememe.version.DescriptionSememe;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.observable.sememe.ObservableSememeChronology;
import sh.isaac.model.observable.version.ObservableDescriptionImpl;
import sh.isaac.model.observable.version.ObservableSememeVersionImpl;
import sh.isaac.model.sememe.version.DescriptionSememeImpl;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 * @param <OV>
 * @param <C>
 */
public class ObservableSememeChronologyImpl<OV extends ObservableSememeVersionImpl<OV>, C extends SememeChronology<?>>
        extends ObservableChronologyImpl<OV, C>
         implements ObservableSememeChronology<OV> {
   private IntegerProperty sememeSequenceProperty;
   private IntegerProperty assemblageSequenceProperty;
   private IntegerProperty referencedComponentNidProperty;

   //~--- constructors --------------------------------------------------------

   public ObservableSememeChronologyImpl(C chronicledObjectLocal) {
      super(chronicledObjectLocal);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public IntegerProperty assemblageSequenceProperty() {
      if (assemblageSequenceProperty == null) {
         assemblageSequenceProperty = new CommitAwareIntegerProperty(this,
               ObservableFields.ASSEMBLAGE_SEQUENCE_FOR_SEMEME_CHRONICLE.toExternalString(),
               getAssemblageSequence());
      }

      return assemblageSequenceProperty;
   }

   @Override
   public <M extends OV> M createMutableVersion(Class<M> type, int stampSequence) {
      return (M) wrapInObservable(chronicledObjectLocal.createMutableVersion(getSvForOv(type), stampSequence));
   }

   @Override
   public <M extends OV> M createMutableVersion(Class<M> type, State status, EditCoordinate ec) {
      return (M) wrapInObservable(chronicledObjectLocal.createMutableVersion(getSvForOv(type), status, ec));
   }

   @Override
   public IntegerProperty referencedComponentNidProperty() {
      if (referencedComponentNidProperty == null) {
         referencedComponentNidProperty = new CommitAwareIntegerProperty(this,
               ObservableFields.REFERENCED_COMPONENT_NID_FOR_SEMEME_CHRONICLE.toExternalString(),
               getReferencedComponentNid());
      }

      return referencedComponentNidProperty;
   }

   @Override
   public IntegerProperty sememeSequenceProperty() {
      if (sememeSequenceProperty == null) {
         sememeSequenceProperty = new CommitAwareIntegerProperty(this,
               ObservableFields.SEMEME_SEQUENCE_FOR_CHRONICLE.toExternalString(),
               getSememeSequence());
      }

      return sememeSequenceProperty;
   }

   private OV wrapInObservable(SememeVersion<?> sememeVersion) {
      if (DescriptionSememe.class.isAssignableFrom(sememeVersion.getClass())) {
         return (OV) new ObservableDescriptionImpl((DescriptionSememeImpl) sememeVersion,
               (ObservableSememeChronology) this);
      }

      throw new UnsupportedOperationException("Can't convert " + sememeVersion);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getAssemblageSequence() {
      if (assemblageSequenceProperty != null) {
         return assemblageSequenceProperty.get();
      }

      return chronicledObjectLocal.getAssemblageSequence();
   }

   @Override
   protected ObservableList<? extends OV> getObservableVersionList() {
      ObservableList<OV> observableList = FXCollections.observableArrayList();

      chronicledObjectLocal.getVersionList().stream().forEach((sememeVersion) -> {
                                       observableList.add(wrapInObservable(sememeVersion));
                                    });
      return observableList;
   }

   @Override
   public int getReferencedComponentNid() {
      if (referencedComponentNidProperty != null) {
         return referencedComponentNidProperty.get();
      }

      return chronicledObjectLocal.getReferencedComponentNid();
   }

   @Override
   public int getSememeSequence() {
      if (sememeSequenceProperty != null) {
         return sememeSequenceProperty.get();
      }

      return chronicledObjectLocal.getSememeSequence();
   }

   @Override
   public SememeType getSememeType() {
      return chronicledObjectLocal.getSememeType();
   }

   private <M extends OV, T> Class<T> getSvForOv(Class<M> type) {
      if (type.isAssignableFrom(ObservableDescriptionImpl.class)) {
         return (Class<T>) DescriptionSememe.class;
      }

      throw new UnsupportedOperationException("Can't convert " + type);
   }
}

