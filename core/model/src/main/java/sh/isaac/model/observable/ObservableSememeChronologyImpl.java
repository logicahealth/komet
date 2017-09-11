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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sh.isaac.api.State;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.sememe.version.ComponentNidVersion;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.observable.sememe.ObservableSememeChronology;
import sh.isaac.model.observable.version.ObservableDescriptionVersionImpl;
import sh.isaac.model.sememe.version.DescriptionVersionImpl;
import sh.isaac.api.component.sememe.version.DescriptionVersion;
import sh.isaac.api.component.sememe.version.LogicGraphVersion;
import sh.isaac.api.component.sememe.version.LongVersion;
import sh.isaac.api.component.sememe.version.MutableSememeVersion;
import sh.isaac.api.component.sememe.version.StringVersion;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.IsaacExternalizableObjectType;
import sh.isaac.api.observable.sememe.version.ObservableSememeVersion;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.model.observable.version.ObservableComponentNidVersionImpl;
import sh.isaac.model.observable.version.ObservableLogicGraphVersionImpl;
import sh.isaac.model.observable.version.ObservableLongVersionImpl;
import sh.isaac.model.observable.version.ObservableSememeVersionImpl;
import sh.isaac.model.observable.version.ObservableStringVersionImpl;

//~--- classes ----------------------------------------------------------------
/**
 * The Class ObservableSememeChronologyImpl.
 *
 * @author kec
 */
public class ObservableSememeChronologyImpl
        extends ObservableChronologyImpl
        implements ObservableSememeChronology {

   private static final Logger LOG = LogManager.getLogger();

   /**
    * The sememe sequence property.
    */
   private IntegerProperty sememeSequenceProperty;

   /**
    * The assemblage sequence property.
    */
   private IntegerProperty assemblageSequenceProperty;

   /**
    * The referenced component nid property.
    */
   private IntegerProperty referencedComponentNidProperty;

   //~--- constructors --------------------------------------------------------
   /**
    * Instantiates a new observable sememe chronology impl.
    *
    * @param chronicledObjectLocal the chronicled object local
    */
   public ObservableSememeChronologyImpl(SememeChronology chronicledObjectLocal) {
      super(chronicledObjectLocal);
   }

   protected SememeChronology getSememeChronology() {
      return (SememeChronology) this.chronicledObjectLocal;
   }
   //~--- methods -------------------------------------------------------------

   /**
    * Assemblage sequence property.
    *
    * @return the integer property
    */
   @Override
   public IntegerProperty assemblageSequenceProperty() {
      if (this.assemblageSequenceProperty == null) {
         this.assemblageSequenceProperty = new CommitAwareIntegerProperty(this,
                 ObservableFields.ASSEMBLAGE_SEQUENCE_FOR_SEMEME_CHRONICLE.toExternalString(),
                 getAssemblageSequence());
      }

      return this.assemblageSequenceProperty;
   }

   /**
    * Creates the mutable version.
    *
    * @param stampSequence the stamp sequence
    * @return the m
    */
   @Override
   public <V extends Version> V createMutableVersion(int stampSequence) {
      return (V) wrapInObservable(getSememeChronology().createMutableVersion(stampSequence));
   }

   /**
    * Creates the mutable version.
    *
    * @param status the status
    * @param ec the ec
    * @return the m
    */
   @Override
   public <V extends Version> V createMutableVersion(State status, EditCoordinate ec) {
      return (V) wrapInObservable(getSememeChronology().createMutableVersion(status, ec));
   }

   /**
    * Referenced component nid property.
    *
    * @return the integer property
    */
   @Override
   public IntegerProperty referencedComponentNidProperty() {
      if (this.referencedComponentNidProperty == null) {
         this.referencedComponentNidProperty = new CommitAwareIntegerProperty(this,
                 ObservableFields.REFERENCED_COMPONENT_NID_FOR_SEMEME_CHRONICLE.toExternalString(),
                 getReferencedComponentNid());
      }

      return this.referencedComponentNidProperty;
   }

   /**
    * Sememe sequence property.
    *
    * @return the integer property
    */
   @Override
   public IntegerProperty sememeSequenceProperty() {
      if (this.sememeSequenceProperty == null) {
         this.sememeSequenceProperty = new CommitAwareIntegerProperty(this,
                 ObservableFields.SEMEME_SEQUENCE_FOR_CHRONICLE.toExternalString(),
                 getSememeSequence());
      }

      return this.sememeSequenceProperty;
   }

   /**
    * Wrap in observable.
    *
    * @param sememeVersion the sememe version
    * @return the ov
    */
   private <OV extends ObservableSememeVersion>
           OV wrapInObservable(SememeVersion sememeVersion) {
      switch (sememeVersion.getChronology().getSememeType()) {
         case DESCRIPTION:
            return (OV) new ObservableDescriptionVersionImpl((DescriptionVersionImpl) sememeVersion,
                    (ObservableSememeChronology) this);
         case COMPONENT_NID:
            return (OV) new ObservableComponentNidVersionImpl((ComponentNidVersion) sememeVersion,
                    (ObservableSememeChronology) this);
         case MEMBER:
            return (OV) new ObservableSememeVersionImpl(sememeVersion,
                    (ObservableSememeChronology) this);
         case LONG:
            return (OV) new ObservableLongVersionImpl((LongVersion) sememeVersion,
                    (ObservableSememeChronology) this);
         case STRING:
            return (OV) new ObservableStringVersionImpl((StringVersion) sememeVersion,
                    (ObservableSememeChronology) this);
         case LOGIC_GRAPH:
            return (OV) new ObservableLogicGraphVersionImpl((LogicGraphVersion) sememeVersion,
                    (ObservableSememeChronology) this);
         case DYNAMIC:
            LOG.warn("Incomplete implementation of dynamic sememe: " + 
                    sememeVersion.getClass().getSimpleName() + " " + sememeVersion);
            return (OV) new ObservableSememeVersionImpl(sememeVersion,
                    (ObservableSememeChronology) this);
            
           // fall through to default...
         case UNKNOWN:
         default:
            throw new UnsupportedOperationException("Can't convert to observable "
                    + sememeVersion.getChronology().getSememeType() + "from \n:    "
                    + sememeVersion);
      }

   }

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the assemblage sequence.
    *
    * @return the assemblage sequence
    */
   @Override
   public int getAssemblageSequence() {
      if (this.assemblageSequenceProperty != null) {
         return this.assemblageSequenceProperty.get();
      }

      return getSememeChronology().getAssemblageSequence();
   }

   /**
    * Gets the observable version list.
    *
    * @return the observable version list
    */
   @Override
   public <OV extends ObservableVersion> ObservableList<OV> getObservableVersionList() {
      final ObservableList<OV> observableList = FXCollections.observableArrayList();

      this.chronicledObjectLocal.getVersionList().stream().forEach((sememeVersion) -> {
         observableList.add(wrapInObservable((SememeVersion) sememeVersion));
      });
      return observableList;
   }

   /**
    * Gets the referenced component nid.
    *
    * @return the referenced component nid
    */
   @Override
   public int getReferencedComponentNid() {
      if (this.referencedComponentNidProperty != null) {
         return this.referencedComponentNidProperty.get();
      }

      return getSememeChronology().getReferencedComponentNid();
   }

   /**
    * Gets the sememe sequence.
    *
    * @return the sememe sequence
    */
   @Override
   public int getSememeSequence() {
      if (this.sememeSequenceProperty != null) {
         return this.sememeSequenceProperty.get();
      }

      return getSememeChronology().getSememeSequence();
   }

   /**
    * Gets the sememe type.
    *
    * @return the sememe type
    */
   @Override
   public VersionType getSememeType() {
      return getSememeChronology().getSememeType();
   }

   /**
    * Gets the sv for ov.
    *
    * @param <M> the generic type
    * @param <T> the generic type
    * @param type the type
    * @return the sv for ov
    */
   private <M extends MutableSememeVersion, T>
           Class<T> getSvForOv(Class<M> type) {
      if (type.isAssignableFrom(ObservableDescriptionVersionImpl.class)) {
         return (Class<T>) DescriptionVersion.class;
      }

      throw new UnsupportedOperationException("Can't convert " + type);
   }

   @Override
   public <V extends Version> LatestVersion<V> getLatestVersion(StampCoordinate coordinate) {
      return getSememeChronology().getLatestVersion(coordinate);
   }

   @Override
   public boolean isLatestVersionActive(StampCoordinate coordinate) {
      return getSememeChronology().isLatestVersionActive(coordinate);
   }

   @Override
   public void putExternal(ByteArrayDataBuffer out) {
      getSememeChronology().putExternal(out);
   }

   @Override
   public byte getDataFormatVersion() {
      return getSememeChronology().getDataFormatVersion();
   }

   @Override
   public IsaacExternalizableObjectType getExternalizableObjectType() {
      return getSememeChronology().getExternalizableObjectType();
   }

   @Override
   public String toString() {
      return "ObservableSememeChronologyImpl{" + getSememeChronology().toUserString() + '}';
   }
}
