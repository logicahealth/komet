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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

//~--- non-JDK imports --------------------------------------------------------

import javafx.application.Platform;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.SememeSequenceSet;
import sh.isaac.api.commit.ChronologyChangeListener;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.commit.CommitStates;
import sh.isaac.api.commit.CommittableComponent;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.ObservableChronologyService;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.observable.sememe.ObservableSememeChronology;
import sh.isaac.api.snapshot.calculator.RelativePositionCalculator;
import sh.isaac.model.ChronologyImpl;
import sh.isaac.model.DeepEqualsVersionWrapper;
import sh.isaac.model.VersionImpl;
import sh.isaac.model.VersionWithScoreWrapper;
import sh.isaac.model.observable.version.ObservableVersionImpl;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ObservableChronologyImpl.
 *
 * @author kec
 */
public abstract class ObservableChronologyImpl
         implements ObservableChronology, ChronologyChangeListener, CommittableComponent {
   /**
    * The Constant ocs.
    */
   private static final ObservableChronologyService ocs = LookupService.getService(ObservableChronologyService.class);

   //~--- fields --------------------------------------------------------------

   /**
    * The version list property.
    */
   protected SimpleListProperty<ObservableVersion> versionListProperty;

   /**
    * The nid property.
    */
   private IntegerProperty nidProperty;

   /**
    * The primordial uuid property.
    */
   private ObjectProperty<UUID> primordialUuidProperty;

   /**
    * The uuid list property.
    */
   private ListProperty<UUID> uuidListProperty;

   /**
    * The commit state property.
    */
   private ObjectProperty<CommitStates> commitStateProperty;

   /**
    * The sememe list property.
    */
   private ListProperty<ObservableSememeChronology> sememeListProperty;

   /**
    * The chronicled object local.
    */
   protected Chronology chronicledObjectLocal;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new observable chronology impl.
    *
    * @param chronicledObjectLocal the chronicled object local
    */
   public ObservableChronologyImpl(Chronology chronicledObjectLocal) {
      if (chronicledObjectLocal instanceof ObservableChronology) {
         throw new IllegalStateException("Observable chronology cannot wrap another observable chronology");
      }

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
                                   .anyMatch(
                                       (version) -> ((ObservableVersionImpl) version).getCommitState() ==
                                       CommitStates.UNCOMMITTED)) {
                  return CommitStates.UNCOMMITTED;
               }

               return CommitStates.COMMITTED;
            }
         };

         this.commitStateProperty = new SimpleObjectProperty(
             this,
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
         updateChronology(cc);
      }
   }

   /**
    * Handle change.
    *
    * @param sc the sc
    */
   @Override
   public final void handleChange(SememeChronology sc) {
      if (this.getNid() == sc.getNid()) {
         updateChronology(sc);
      }

      if (sc.getReferencedComponentNid() == this.getNid()) {
         if (this.sememeListProperty != null) {
            // check to be sure sememe is in list, if not, add it.
            if (this.sememeListProperty.get()
                                       .stream()
                                       .noneMatch((element) -> element.getNid() == sc.getNid())) {
               this.sememeListProperty.get()
                                      .add((ObservableSememeChronology) ocs.getObservableSememeChronology(sc.getNid()));
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
         this.nidProperty = new CommitAwareIntegerProperty(
             this,
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
         this.primordialUuidProperty = new CommitAwareObjectProperty<>(
             this,
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
   public final ListProperty<ObservableSememeChronology> sememeListProperty() {
      if (this.sememeListProperty == null) {
         final ObservableList<ObservableSememeChronology> sememeList = FXCollections.observableArrayList();

         Get.assemblageService()
            .getSememeSequencesForComponent(getNid())
            .stream()
            .forEach((sememeSequence) -> sememeList.add(ocs.getObservableSememeChronology(sememeSequence)));
         this.sememeListProperty = new SimpleListProperty(
             this,
             ObservableFields.SEMEME_LIST_FOR_CHRONICLE.toExternalString(),
             sememeList);
      }

      return this.sememeListProperty;
   }

   @Override
   public String toString() {
      return "ObservableChronologyImpl{" + chronicledObjectLocal + '}';
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
         this.uuidListProperty = new SimpleListProperty<>(
             this,
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
   public final ListProperty<ObservableVersion> versionListProperty() {
      if (this.versionListProperty == null) {
         this.versionListProperty = new SimpleListProperty<>(
             this,
             ObservableFields.VERSION_LIST_FOR_CHRONICLE.toExternalString(),
             getVersionList());
      }

      return this.versionListProperty;
   }

   /**
    * Update chronicle.
    *
    * @param chronology the chronicled object local
    */
   protected final void updateChronology(Chronology chronology) {
      if (chronology instanceof ObservableChronology) {
         throw new IllegalStateException("Observable chronology cannot wrap another observable chronology");
      }
      if (!Platform.isFxApplicationThread()) {
         throw new UnsupportedOperationException("Cannot update on this thread: " + Thread.currentThread().getName());
      }

      if (this.versionListProperty != null) {
         Chronology                    newChronology = chronology;
         Set<DeepEqualsVersionWrapper> oldSet        = new HashSet<>();
         Map<VersionImpl, VersionImpl> oldVersionNewVersionMap = new HashMap<>();
         Set<VersionImpl> cancelSet = new HashSet<>();
         Set<VersionImpl> additionSet = new HashSet<>();
         Map<VersionImpl, Set<VersionWithScoreWrapper>> finalAlignmentMap = new HashMap<>();
                 

         this.getVersionList().forEach((observableVersion) -> {
            StampedVersion oldVersion = ((ObservableVersionImpl) observableVersion).getStampedVersion();
            oldSet.add(new DeepEqualsVersionWrapper((VersionImpl) oldVersion));
         });
         
         Set<DeepEqualsVersionWrapper> newSet = new HashSet<>();

         newChronology.getVersionList()
                      .forEach((version) -> newSet.add(new DeepEqualsVersionWrapper((VersionImpl) version)));

         // Exact match -> nothing to change in the underlying observableVersions, but else will handle observable changes. 
         if (!newSet.equals(oldSet)) {
            // Change goes only one direction uncommitted -> committed; uncommitted -> canceled.
            // Create two sets, then do a set difference.
            Set<DeepEqualsVersionWrapper> equalsSet = new HashSet<>(newSet);
            equalsSet.retainAll(oldSet);
            HashMap<Integer, Version> equalsStampVersionMap = new HashMap<>();
            equalsSet.forEach((versionWrapper) -> {
               equalsStampVersionMap.put(versionWrapper.getVersion().getStampSequence(), versionWrapper.getVersion());
            });
            
            Set<DeepEqualsVersionWrapper> oldSetCopy = new HashSet<>(oldSet);
            oldSet.removeAll(newSet);
            newSet.removeAll(oldSetCopy);
            if (oldSet.size() == newSet.size()) {
               // align by edit distance...
               if (newSet.size() == 1) {
                  // easy case... TODO, handle checking to make sure author is same, if necessary for multi-tenant authoring. 
                  oldVersionNewVersionMap.put(oldSet.iterator().next().getVersion(), newSet.iterator().next().getVersion());
               } else {
                  Map<VersionImpl, Set<VersionWithScoreWrapper>> alignmentMap = makeAlignmentMap(oldSet, newSet);
                  finalAlignmentMap.putAll(alignmentMap);
               }              
            } else {
               if (newSet.size() == 1 || oldSet.size() == 1) {
                  if (newSet.isEmpty()) {
                     // New set is one less, a cancel
                     cancelSet.add(oldSet.iterator().next().getVersion());
                  } else {
                     // New set is one more, uncommitted or committed addition
                     additionSet.add(newSet.iterator().next().getVersion());
                  }
               }
               
               // Find the outliers with the worst alignment...
               int difference = newSet.size() - oldSet.size();
               Map<VersionImpl, Set<VersionWithScoreWrapper>> alignmentMap = makeAlignmentMap(oldSet, newSet);
               if (difference < 0) {
                  AtomicInteger toFind = new AtomicInteger(Math.abs(difference));
                  // deletions
                  // find abs(difference) number of worst alignments. 
                  // remove from alignment map, and add to deletions
                  TreeSet<ScoredNewOldVersion> sortedAlignments = new TreeSet<>();
                  Set<VersionImpl> toRemoveFromAlignment = new HashSet<>();
                  alignmentMap.entrySet().forEach((entry) -> {
                     Set<VersionWithScoreWrapper> rankedMatches = entry.getValue();
                     if (rankedMatches.isEmpty()) {
                        cancelSet.add(entry.getKey());
                        // remove from tree... 
                        toRemoveFromAlignment.add(entry.getKey());
                        toFind.decrementAndGet();
                     } else {
                        if (toFind.get() > 0) {
                           VersionWithScoreWrapper bestMatch = rankedMatches.iterator().next();
                           sortedAlignments.add(new ScoredNewOldVersion(entry.getKey(), bestMatch.getVersion(), bestMatch.getScore()));
                        }
                     }
                  });
                  toRemoveFromAlignment.forEach((version) -> alignmentMap.remove(version));
                  finalAlignmentMap.putAll(alignmentMap);
                  Iterator<ScoredNewOldVersion> alignmentIterator = sortedAlignments.descendingIterator();
                  for (int i = 0; i < toFind.get(); i++) {
                     ScoredNewOldVersion scoredNewOldVersion = alignmentIterator.next();
                     cancelSet.add(scoredNewOldVersion.oldVersion);
                  }
               } else {
                  // additions
                  // find what did not get added as a first ranked priority.
                  // add to additions. 
                  Set<VersionImpl> topAlignedSet = new HashSet<>();
                  alignmentMap.entrySet().forEach((entry) -> {
                     Set<VersionWithScoreWrapper> rankedSet = entry.getValue();
                     if (rankedSet.isEmpty()) {
                        UnsupportedOperationException e = new UnsupportedOperationException("Can't handle this state yet...");
                        e.printStackTrace();
                        throw e;
                     } 
                     VersionImpl topVersion = rankedSet.iterator().next().getVersion();
                     topAlignedSet.add(topVersion);
                  });
                  topAlignedSet.forEach((version) -> {
                     newSet.remove(new DeepEqualsVersionWrapper(version));
                  });
                  newSet.forEach((versionWrapper) -> additionSet.add(versionWrapper.getVersion()));
                  finalAlignmentMap.putAll(alignmentMap);
               }
            }
            
            ObservableList<ObservableVersionImpl> observableVersionList = this.getVersionList();
            ListIterator<ObservableVersionImpl> observableVersions = observableVersionList.listIterator();
            // Handle delete or merge... 
            while (observableVersions.hasNext()) {
               ObservableVersionImpl observableVersion = observableVersions.next();
               VersionImpl version = observableVersion.getStampedVersion();
               // see if equals
               if (equalsStampVersionMap.containsKey(version.getStampSequence())) {
                  observableVersion.updateVersion(equalsStampVersionMap.get(version.getStampSequence()));
               } else if (cancelSet.contains(version)) {
                  observableVersions.remove();
               } else if (finalAlignmentMap.containsKey(version)) {
                  VersionImpl updateVersion = finalAlignmentMap.get(version).iterator().next().getVersion();
                  observableVersion.updateVersion(updateVersion);
               } else {
                  throw new IllegalStateException("No match for: " + observableVersion);
               }
            }
            // then add... 
            additionSet.forEach((version) -> {
               observableVersions.add(wrapInObservable(version));
            });
         } else {
            // Old and new sets are equal, still need to update the observable observableVersions
            // with the new chronology object...
            Map<Integer, Version> stampVersionMap = ((ChronologyImpl) chronology).getStampVersionMap();
            this.getVersionList().forEach((observableVersion) -> {
               ObservableVersionImpl observableVersionImpl = (ObservableVersionImpl) observableVersion;
               StampedVersion version = observableVersionImpl.getStampedVersion();
               observableVersionImpl.updateVersion(stampVersionMap.get(version.getStampSequence()));
            });
         }
      }

      this.chronicledObjectLocal = chronology;

      if (this.sememeListProperty != null) {
         SememeSequenceSet updatedSememeSequenceSet = Get.assemblageService()
                                                         .getSememeSequencesForComponent(chronology.getNid());

         this.sememeListProperty.forEach(
             (sememe) -> {
                updatedSememeSequenceSet.remove(sememe.getSememeSequence());
             });
         updatedSememeSequenceSet.stream()
                                 .forEach(
                                     (sememeSequence) -> {
                                        this.sememeListProperty.add(
                                            Get.observableChronologyService()
                                               .getObservableSememeChronology(sememeSequence));
                                     });
      }
   }

   public Map<VersionImpl, Set<VersionWithScoreWrapper>> makeAlignmentMap(Set<DeepEqualsVersionWrapper> oldSet, Set<DeepEqualsVersionWrapper> newSet) {
      // TODO consider possiblity of simultaneous cancel and create in a multi-user environment.
      // Will start by assuming that such will not happen for the same author. Need to enforce single login per author
      
      // align pairs...
      // A map of sorted sets should work, where there must be an equal author for all members of the sorted set.
      // Natural ordering small -> large
      Map<VersionImpl, Set<VersionWithScoreWrapper>> alignmentMap = new HashMap<>();
      oldSet.forEach((oldVersionWrapper) -> {
         VersionImpl oldVersion = oldVersionWrapper.getVersion();
         TreeSet<VersionWithScoreWrapper> alignmentSet = new TreeSet<>();
         
         alignmentMap.put(oldVersion, alignmentSet);
         newSet.forEach((newVersionWrapper) -> {
            VersionImpl newVersion = newVersionWrapper.getVersion();
            if (newVersion.getAuthorSequence() == oldVersion.getAuthorSequence()) {
               alignmentSet.add(new VersionWithScoreWrapper(newVersion, newVersion.editDistance(oldVersion)));
            }
         });
      });
      return alignmentMap;
   }

   protected abstract <OV extends ObservableVersion> OV wrapInObservable(Version version);

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
   public LatestVersion<ObservableVersion> getLatestVersion(Class<? extends StampedVersion> type,
         StampCoordinate coordinate) {
      final RelativePositionCalculator calculator = RelativePositionCalculator.getCalculator(coordinate);

      return calculator.getLatestVersion(this);
   }

   @Override
   public LatestVersion<ObservableVersion> getLatestCommittedVersion(StampCoordinate coordinate) {
      final RelativePositionCalculator calculator = RelativePositionCalculator.getCalculator(coordinate);
      return calculator.getLatestCommittedVersion(this);
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
    * Gets the sememe list.
    *
    * @return the sememe list
    */
   @Override
   public final ObservableList<ObservableSememeChronology> getObservableSememeList() {
      return sememeListProperty().get();
   }

   /**
    * Gets the sememe list from assemblage.
    *
    * @param assemblageSequence the assemblage sequence
    * @return the sememe list from assemblage
    */
   @Override
   public ObservableList<ObservableSememeChronology> getObservableSememeListFromAssemblage(int assemblageSequence) {
      return getSememeListFromAssemblage(assemblageSequence);
   }

   @Override
   public ObservableList<ObservableSememeChronology> getObservableSememeListFromAssemblageOfType(int assemblageSequence,
         VersionType type) {
      return getSememeListFromAssemblageOfType(assemblageSequence, type);
   }

   /**
    * Gets the observable version list.
    *
    * @param <OV>
    * @return the observable version list
    */
   protected abstract <OV extends ObservableVersion> ObservableList<OV> getObservableVersionList();

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
   public final ObservableList<ObservableSememeChronology> getSememeList() {
      return sememeListProperty().get();
   }

   /**
    * Gets the sememe list from assemblage.
    *
    * @param assemblageSequence the assemblage sequence
    * @return the sememe list from assemblage
    */
   @Override
   public ObservableList<ObservableSememeChronology> getSememeListFromAssemblage(int assemblageSequence) {
      return getSememeList().filtered(
          (observableSememeChronology) -> {
             return observableSememeChronology.getAssemblageSequence() == assemblageSequence;
          });
   }

   /**
    * Gets the sememe list from assemblage of type.
    *
    * @param assemblageSequence the assemblage sequence
    * @param type the type
    * @return the sememe list from assemblage of type
    */
   @Override
   public ObservableList<ObservableSememeChronology> getSememeListFromAssemblageOfType(int assemblageSequence,
         VersionType type) {
      return getSememeList().filtered(
          (observableSememeChronology) -> {
             return (observableSememeChronology.getAssemblageSequence() == assemblageSequence) &&
                    (observableSememeChronology.getSememeType() == type);
          });
   }

   @Override
   public <V extends Version> List<V> getUnwrittenVersionList() {
      return chronicledObjectLocal.getUnwrittenVersionList();
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
   public final <V extends Version> ObservableList<V> getVersionList() {
      if (this.versionListProperty != null) {
         return (ObservableList<V>) this.versionListProperty.get();
      }

      this.versionListProperty = new SimpleListProperty<>(getObservableVersionList());
      return (ObservableList<V>) this.versionListProperty.get();
   }

   /**
    * Gets the version stamp sequences.
    *
    * @return the version stamp sequences
    */
   @Override
   public final int[] getVersionStampSequences() {
      return this.chronicledObjectLocal.getVersionStampSequences();
   }

   public Chronology getWrappedChronology() {
      return chronicledObjectLocal;
   }
   
   private static class ScoredNewOldVersion implements Comparable<ScoredNewOldVersion> {
      final VersionImpl oldVersion;
      final VersionImpl newVersion;
      final int score;

      public ScoredNewOldVersion(VersionImpl oldVersion, VersionImpl newVersion, int score) {
         this.oldVersion = oldVersion;
         this.newVersion = newVersion;
         this.score = score;
      }

      @Override
      public int compareTo(ScoredNewOldVersion o) {
         return this.score - o.score;
      }

      @Override
      public int hashCode() {
         int hash = 7;
         hash = 17 * hash + this.score;
         return hash;
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         }
         if (obj == null) {
            return false;
         }
         if (getClass() != obj.getClass()) {
            return false;
         }
         final ScoredNewOldVersion other = (ScoredNewOldVersion) obj;
         return this.score == other.score;
      }
      
   }
   
      @Override
   public SememeSequenceSet getRecursiveSememeSequences() {
      return chronicledObjectLocal.getRecursiveSememeSequences();
   }

}

