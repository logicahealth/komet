/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
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
package sh.isaac.model.observable;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import sh.isaac.api.Status;
import sh.isaac.api.chronicle.CategorizedVersions;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.commit.CommitStates;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.dag.Graph;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.observable.ObservableChronologyService;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;

/**
 *
 * @author kec
 */
public class ObservableSemanticChronologyWeakRefImpl implements ObservableSemanticChronology {

   private final int semanticId;
   WeakReference<ObservableSemanticChronology> reference;
   ObservableChronologyService observableChronologyService;

   public ObservableSemanticChronologyWeakRefImpl(int semanticId, ObservableChronologyService observableChronologyService) {
      this.semanticId = semanticId;
      this.observableChronologyService = observableChronologyService;
   }

   /**
    * @return the chronology
    */
   private ObservableSemanticChronology getChronology() {
      ObservableSemanticChronology chronology;
      if (reference == null) {
         chronology = observableChronologyService.getObservableSemanticChronology(semanticId);
         reference = new WeakReference(chronology);
         return chronology;
      }
      chronology = reference.get();
      if (chronology == null) {
         chronology = observableChronologyService.getObservableSemanticChronology(semanticId);
         reference = new WeakReference(chronology);
      }
      return chronology;
   }

   @Override
   public IntegerProperty assemblageNidProperty() {
      return getChronology().assemblageNidProperty();
   }

   @Override
   public IntegerProperty referencedComponentNidProperty() {
      return getChronology().referencedComponentNidProperty();
   }

   @Override
   public ObjectProperty<CommitStates> commitStateProperty() {
      return getChronology().commitStateProperty();
   }

   @Override
   public IntegerProperty nidProperty() {
      return getChronology().nidProperty();
   }

   @Override
   public ObjectProperty<UUID> primordialUuidProperty() {
      return getChronology().primordialUuidProperty();
   }

   @Override
   public ListProperty<? extends ObservableSemanticChronology> semanticListProperty() {
      return getChronology().semanticListProperty();
   }

   @Override
   public ListProperty<UUID> uuidListProperty() {
      return getChronology().uuidListProperty();
   }

   @Override
   public ListProperty<ObservableVersion> versionListProperty() {
      return getChronology().versionListProperty();
   }

   @Override
   public ObservableList<ObservableSemanticChronology> getObservableSemanticList() {
      return getChronology().getObservableSemanticList();
   }

   @Override
   public ObservableList<ObservableSemanticChronology> getObservableSemanticListFromAssemblage(int assemblageSequence) {
      return getChronology().getObservableSemanticListFromAssemblage(assemblageSequence);
   }

   @Override
   public LatestVersion<? extends ObservableVersion> getLatestVersion(Class<? extends StampedVersion> type, StampCoordinate coordinate) {
      return getChronology().getLatestVersion(type, coordinate);
   }

   @Override
   public <V extends Version> V createMutableVersion(int stampSequence) {
      return getChronology().createMutableVersion(stampSequence);
   }

   @Override
   public <V extends Version> V createMutableVersion(Status state, EditCoordinate ec) {
      return getChronology().createMutableVersion(state, ec);
   }

   @Override
   public VersionType getVersionType() {
      return getChronology().getVersionType();
   }

   @Override
   public <V extends Version> LatestVersion<V> getLatestVersion(StampCoordinate coordinate) {
      return getChronology().getLatestVersion(coordinate);
   }

   @Override
   public <V extends Version> CategorizedVersions<V> getCategorizedVersions(StampCoordinate coordinate) {
      return getChronology().getCategorizedVersions(coordinate);
   }

   @Override
   public boolean isLatestVersionActive(StampCoordinate coordinate) {
      return getChronology().isLatestVersionActive(coordinate);
   }

   @Override
   public <V extends SemanticChronology> List<V> getSemanticChronologyList() {
      return getChronology().getSemanticChronologyList();
   }

   @Override
   public <V extends SemanticChronology> List<V> getSemanticChronologyListFromAssemblage(int assemblageSequence) {
      return getChronology().getSemanticChronologyListFromAssemblage(assemblageSequence);
   }


   @Override
   public <V extends Version> List<V> getUnwrittenVersionList() {
      return getChronology().getUnwrittenVersionList();
   }

   @Override
   public <V extends Version> List<Graph<V>> getVersionGraphList() {
      return getChronology().getVersionGraphList();
   }

   @Override
   public <V extends Version> List<V> getVersionList() {
      return getChronology().getVersionList();
   }

   @Override
   public int[] getVersionStampSequences() {
      return getChronology().getVersionStampSequences();
   }

   @Override
   public <V extends StampedVersion> List<V> getVisibleOrderedVersionList(StampCoordinate stampCoordinate) {
      return getChronology().getVisibleOrderedVersionList(stampCoordinate);
   }

   @Override
   public int getAssemblageNid() {
      return getChronology().getAssemblageNid();
   }

   @Override
   public int getReferencedComponentNid() {
      return getChronology().getReferencedComponentNid();
   }

   @Override
   public CommitStates getCommitState() {
      return getChronology().getCommitState();
   }

   @Override
   public boolean isUncommitted() {
      return getChronology().isUncommitted();
   }

   @Override
   public void handleChange(ConceptChronology cc) {
      getChronology().handleChange(cc);
   }

   @Override
   public void handleChange(SemanticChronology sc) {
      getChronology().handleChange(sc);
   }

   @Override
   public void handleCommit(CommitRecord commitRecord) {
      getChronology().handleCommit(commitRecord);
   }

   @Override
   public UUID getListenerUuid() {
      return getChronology().getListenerUuid();
   }

   @Override
   public void putExternal(ByteArrayDataBuffer out) {
      getChronology().putExternal(out);
   }

   @Override
   public IsaacObjectType getIsaacObjectType() {
      return getChronology().getIsaacObjectType();
   }

   @Override
   public String toUserString() {
      return getChronology().toUserString();
   }

   @Override
   public int getNid() {
      return getChronology().getNid();
   }

   @Override
   public UUID getPrimordialUuid() {
      return getChronology().getPrimordialUuid();
   }

   @Override
   public List<UUID> getUuidList() {
      return getChronology().getUuidList();
   }

   @Override
   public UUID[] getUuids() {
      return getChronology().getUuids();
   }

   @Override
   public int hashCode() {
      return getChronology().hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      return getChronology().equals(obj);
   }

   @Override
   public String toString() {
      return getChronology().toString();
   }

   @Override
   public <V extends Version> LatestVersion<V> getLatestCommittedVersion(StampCoordinate coordinate) {
      return getChronology().getLatestCommittedVersion(coordinate);
   }

   @Override
   public NidSet getRecursiveSemanticNids() {
      return getChronology().getRecursiveSemanticNids();
   }

    @Override
    public <T extends ObservableVersion> T createAutonomousMutableVersion(EditCoordinate ec) {
        return getChronology().createAutonomousMutableVersion(ec);
    }

    @Override
    public <V extends ObservableVersion> LatestVersion<V> getLatestObservableVersion(StampCoordinate coordinate) {
        return getChronology().getLatestObservableVersion(coordinate);
    }
   
}
