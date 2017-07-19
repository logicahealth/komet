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

//~--- JDK imports ------------------------------------------------------------

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.apache.mahout.math.map.OpenShortObjectHashMap;

import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.commit.ChronologyChangeListener;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.commit.CommitStates;
import sh.isaac.api.commit.CommittableComponent;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.ObservableChronologyService;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.observable.sememe.ObservableSememeChronology;
import sh.isaac.api.observable.sememe.version.ObservableSememeVersion;
import sh.isaac.api.snapshot.calculator.RelativePositionCalculator;
import sh.isaac.model.VersionImpl;
import sh.isaac.model.observable.version.ObservableVersionImpl;
import sh.isaac.api.chronicle.Chronology;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ObservableChronologyImpl.
 *
 * @author kec
 * @param <OV> ofType of the observable version
 * @param <C>  ofType of the unobservable (base) chronicle
 */
public abstract class ObservableChronologyImpl
        <OV extends ObservableVersion, 
         C extends Chronology<?>>
         implements ObservableChronology<OV>, ChronologyChangeListener, CommittableComponent {
   /** The Constant ocs. */
   private static final ObservableChronologyService ocs = LookupService.getService(ObservableChronologyService.class);

   //~--- fields --------------------------------------------------------------

   /** The version list. */
   private ObservableList<? extends OV> versionList = null;

   /** The version list property. */
   private ListProperty<? extends OV> versionListProperty;

   /** The nid property. */
   private IntegerProperty nidProperty;

   /** The primordial uuid property. */
   private ObjectProperty<UUID> primordialUuidProperty;

   /** The uuid list property. */
   private ListProperty<UUID> uuidListProperty;

   /** The commit state property. */
   private ObjectProperty<CommitStates> commitStateProperty;

   /** The sememe list property. */
   private ListProperty<ObservableSememeChronology<? extends ObservableSememeVersion>> sememeListProperty;

   /** The chronicled object local. */
   protected C chronicledObjectLocal;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new observable chronology impl.
    *
    * @param chronicledObjectLocal the chronicled object local
    */
   public ObservableChronologyImpl(C chronicledObjectLocal) {
      this.chronicledObjectLocal = chronicledObjectLocal;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Commit state property.
    *
    * @return the object property
    */
   @Override
   public final ObjectProperty<CommitStates> commitStateProperty() {
      if (this.commitStateProperty == null) {
         final ObjectBinding<CommitStates> binding = new ObjectBinding<CommitStates>() {
            @Override
            protected CommitStates computeValue() {
               if (getVersionList().stream()
                                   .anyMatch((version) -> ((ObservableVersionImpl) version).getCommitState() == CommitStates.UNCOMMITTED)) {
                  return CommitStates.UNCOMMITTED;
               }

               return CommitStates.COMMITTED;
            }
         };

         this.commitStateProperty = new SimpleObjectProperty(this,
               ObservableFields.COMMITTED_STATE_FOR_CHRONICLE.toExternalString(),
               binding.get());
         this.commitStateProperty.bind(binding);
      }

      return this.commitStateProperty;
   }

   /**
    * Handle change.
    *
    * @param cc the cc
    */
   @Override
   public final void handleChange(ConceptChronology cc) {
      if (this.getNid() == cc.getNid()) {
         updateChronicle((C) cc);

         // update descriptions...
         throw new UnsupportedOperationException();
      }
   }

   /**
    * Handle change.
    *
    * @param sc the sc
    */
   @Override
   public final void handleChange(SememeChronology<? extends SememeVersion> sc) {
      if (this.getNid() == sc.getNid()) {
         updateChronicle((C) sc);
      }

      if (sc.getReferencedComponentNid() == this.getNid()) {
         if (this.sememeListProperty != null) {
            // check to be sure sememe is in list, if not, add it.
            if (this.sememeListProperty.get()
                                       .stream()
                                       .noneMatch((element) -> element.getNid() == sc.getNid())) {
               this.sememeListProperty.get()
                                      .add(
                                      (ObservableSememeChronology<? extends ObservableSememeVersion>) ocs.getObservableSememeChronology(
                                         sc.getNid()));
            }
         }

         // else, nothing to do, since no one is looking...
      }
   }

   /**
    * Handle commit.
    *
    * @param commitRecord the commit record
    */
   @Override
   public void handleCommit(CommitRecord commitRecord) {
      // TODO implement handle commit...
   }

   /**
    * Nid property.
    *
    * @return the integer property
    */
   @Override
   public final IntegerProperty nidProperty() {
      if (this.nidProperty == null) {
         this.nidProperty = new CommitAwareIntegerProperty(this,
               ObservableFields.NATIVE_ID_FOR_CHRONICLE.toExternalString(),
               getNid());
      }

      return this.nidProperty;
   }

   /**
    * Primordial uuid property.
    *
    * @return the object property
    */
   @Override
   public final ObjectProperty<UUID> primordialUuidProperty() {
      if (this.primordialUuidProperty == null) {
         this.primordialUuidProperty = new CommitAwareObjectProperty<>(this,
               ObservableFields.PRIMORDIAL_UUID_FOR_CHRONICLE.toExternalString(),
               getPrimordialUuid());
      }

      return this.primordialUuidProperty;
   }

   /**
    * Sememe list property.
    *
    * @return the list property< observable sememe chronology<? extends observable sememe version<?>>>
    */
   @Override
   public final ListProperty<ObservableSememeChronology<? extends ObservableSememeVersion>> sememeListProperty() {
      if (this.sememeListProperty == null) {
         final ObservableList<ObservableSememeChronology<? extends ObservableSememeVersion>> sememeList =
            FXCollections.emptyObservableList();

         Get.sememeService()
            .getSememeSequencesForComponent(getNid())
            .stream()
            .forEach((sememeSequence) -> sememeList.add(ocs.getObservableSememeChronology(sememeSequence)));
         this.sememeListProperty = new SimpleListProperty(this,
               ObservableFields.SEMEME_LIST_FOR_CHRONICLE.toExternalString(),
               sememeList);
      }

      return this.sememeListProperty;
   }

   /**
    * To user string.
    *
    * @return the string
    */
   @Override
   public final String toUserString() {
      return this.chronicledObjectLocal.toUserString();
   }

   /**
    * Uuid list property.
    *
    * @return the list property
    */
   @Override
   public final ListProperty<UUID> uuidListProperty() {
      if (this.uuidListProperty == null) {
         this.uuidListProperty = new SimpleListProperty<>(this,
               ObservableFields.UUID_LIST_FOR_CHRONICLE.toExternalString(),
               FXCollections.observableList(getUuidList()));
      }

      return this.uuidListProperty;
   }

   /**
    * Version list property.
    *
    * @return the list property<? extends O v>
    */
   @Override
   public final ListProperty<? extends OV> versionListProperty() {
      if (this.versionListProperty == null) {
         this.versionListProperty = new SimpleListProperty<>(this,
               ObservableFields.VERSION_LIST_FOR_CHRONICLE.toExternalString(),
               getVersionList());
      }

      return this.versionListProperty;
   }

   /**
    * Update chronicle.
    *
    * @param chronicledObjectLocal the chronicled object local
    */
   protected final void updateChronicle(C chronicledObjectLocal) {
      this.chronicledObjectLocal = chronicledObjectLocal;

      if (this.versionList != null) {
         final OpenShortObjectHashMap<OV> observableVersionMap = new OpenShortObjectHashMap<>(this.versionList.size());

         this.versionList.stream()
                         .forEach((ov) -> observableVersionMap.put(((ObservableVersionImpl)ov).getVersionSequence(), ov));
         chronicledObjectLocal.getVersionList().stream().forEach((sv) -> {
                                          final OV observableVersion =
                                             observableVersionMap.get(((VersionImpl) sv).getVersionSequence());

                                          if (observableVersion == null) {
                                             // add new version to list
                                          } else {}
                                       });

         // update versions...
         throw new UnsupportedOperationException();
      }

      // else, nothing to do, since no one is looking...
   }

   ;

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the commit state.
    *
    * @return the commit state
    */
   @Override
   public final CommitStates getCommitState() {
      if (this.commitStateProperty != null) {
         return this.commitStateProperty.get();
      }

      return this.chronicledObjectLocal.getCommitState();
   }

   /**
    * Gets the latest version.
    *
    * @param type the type
    * @param coordinate the coordinate
    * @return the latest version
    */
   @Override
   public Optional<LatestVersion<OV>> getLatestVersion(Class<OV> type, StampCoordinate coordinate) {
      final RelativePositionCalculator calculator = RelativePositionCalculator.getCalculator(coordinate);

      return calculator.getLatestVersion(this);
   }

   /**
    * Gets the listener uuid.
    *
    * @return the listener uuid
    */
   @Override
   public final UUID getListenerUuid() {
      return getPrimordialUuid();
   }

   /**
    * Gets the nid.
    *
    * @return the nid
    */
   @Override
   public final int getNid() {
      if (this.nidProperty != null) {
         return this.nidProperty.get();
      }

      return this.chronicledObjectLocal.getNid();
   }

   /**
    * Gets the observable version list.
    *
    * @return the observable version list
    */
   protected abstract ObservableList<? extends OV> getObservableVersionList();

   /**
    * Gets the primordial uuid.
    *
    * @return the primordial uuid
    */
   @Override
   public final UUID getPrimordialUuid() {
      if (this.primordialUuidProperty != null) {
         return this.primordialUuidProperty.get();
      }

      return this.chronicledObjectLocal.getPrimordialUuid();
   }

   /**
    * Gets the sememe list.
    *
    * @return the sememe list
    */
   @Override
   public final ObservableList<? extends ObservableSememeChronology<? extends ObservableSememeVersion>> getSememeList() {
      return sememeListProperty().get();
   }

   /**
    * Gets the sememe list from assemblage.
    *
    * @param assemblageSequence the assemblage sequence
    * @return the sememe list from assemblage
    */
   @Override
   public List<? extends ObservableSememeChronology<? extends SememeVersion>> getSememeListFromAssemblage(
           int assemblageSequence) {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   /**
    * Gets the sememe list from assemblage of type.
    *
    * @param <SV> the generic type
    * @param assemblageSequence the assemblage sequence
    * @param type the type
    * @return the sememe list from assemblage of type
    */
   @Override
   public <SV extends ObservableSememeVersion> List<? extends ObservableSememeChronology<SV>> getSememeListFromAssemblageOfType(
           int assemblageSequence,
           Class<SV> type) {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   /**
    * Gets the uuid list.
    *
    * @return the uuid list
    */
   @Override
   public final List<UUID> getUuidList() {
      return this.chronicledObjectLocal.getUuidList();
   }

   /**
    * Gets the version list.
    *
    * @return the version list
    */
   @Override
   public final ObservableList<? extends OV> getVersionList() {
      if (this.versionListProperty != null) {
         return this.versionListProperty.get();
      }

      if (this.versionList == null) {
         this.versionList = getObservableVersionList();
      }

      return this.versionList;
   }

   /**
    * Gets the version stamp sequences.
    *
    * @return the version stamp sequences
    */
   @Override
   public final IntStream getVersionStampSequences() {
      return this.chronicledObjectLocal.getVersionStampSequences();
   }
}

