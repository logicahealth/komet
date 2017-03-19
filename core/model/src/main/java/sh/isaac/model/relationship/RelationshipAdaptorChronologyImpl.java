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



package sh.isaac.model.relationship;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.State;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.StampSequenceSet;
import sh.isaac.api.commit.CommitStates;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.OchreExternalizableObjectType;
import sh.isaac.api.snapshot.calculator.RelativePositionCalculator;
import sh.isaac.api.util.UuidT5Generator;

import static sh.isaac.api.util.UuidT5Generator.REL_ADAPTOR_NAMESPACE;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class RelationshipAdaptorChronologyImpl
         implements SememeChronology<RelationshipVersionAdaptorImpl> {
   private final ArrayList<RelationshipVersionAdaptorImpl> versionList = new ArrayList<>();
   private final long                                      primordialUuidMsb;

   /**
    * Primordial uuid least significant bits for this component
    */
   private final long primordialUuidLsb;

   /**
    * Native identifier of this component
    */
   private final int nid;

   /**
    * Id of the logical expression this adaptor was generated from.
    */
   private final int referencedComponentNid;

   //~--- constructors --------------------------------------------------------

   public RelationshipAdaptorChronologyImpl(int nid, int referencedComponentNid) {
      this.nid                    = nid;
      this.referencedComponentNid = referencedComponentNid;

      final UUID computedUuid = UuidT5Generator.get(REL_ADAPTOR_NAMESPACE, Integer.toString(nid));

      this.primordialUuidLsb = computedUuid.getLeastSignificantBits();
      this.primordialUuidMsb = computedUuid.getMostSignificantBits();
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public <M extends RelationshipVersionAdaptorImpl> M createMutableVersion(Class<M> type, int stampSequence) {
      throw new UnsupportedOperationException("Not supported.");
   }

   @Override
   public <M extends RelationshipVersionAdaptorImpl> M createMutableVersion(Class<M> type,
         State state,
         EditCoordinate ec) {
      throw new UnsupportedOperationException("Not supported.");
   }

   @Override
   public void putExternal(ByteArrayDataBuffer out) {
      throw new UnsupportedOperationException("Not supported.");
   }

   @Override
   public String toString() {
      final StringBuilder sb = new StringBuilder();

      sb.append("[");
      this.versionList.stream().forEach((version) -> {
                             sb.append(version);
                             sb.append(",\n ");
                          });
      sb.delete(sb.length() - 4, sb.length() - 1);
      sb.append("]");

      final Optional<? extends SememeChronology<? extends SememeVersion<?>>> optionalSememe = Get.sememeService()
                                                                                           .getOptionalSememe(
                                                                                              this.referencedComponentNid);

      if (optionalSememe.isPresent()) {
         return "RelAdaptor{" + Get.conceptDescriptionText(optionalSememe.get().getAssemblageSequence()) + ": " +
                sb.toString() + '}';
      }

      return "RelAdaptor{" + this.referencedComponentNid + ": " + sb.toString() + '}';
   }

   @Override
   public String toUserString() {
      return toString();
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getAssemblageSequence() {
      throw new UnsupportedOperationException("Not supported.");
   }

   @Override
   public CommitStates getCommitState() {
      return CommitStates.COMMITTED;
   }

   @Override
   public byte getDataFormatVersion() {
      throw new UnsupportedOperationException("Not supported.");
   }

   @Override
   public Optional<LatestVersion<RelationshipVersionAdaptorImpl>> getLatestVersion(
           Class<RelationshipVersionAdaptorImpl> type,
           StampCoordinate coordinate) {
      final RelativePositionCalculator calc = RelativePositionCalculator.getCalculator(coordinate);

      return calc.getLatestVersion(this);
   }

   @Override
   public boolean isLatestVersionActive(StampCoordinate coordinate) {
      final RelativePositionCalculator calc       = RelativePositionCalculator.getCalculator(coordinate);
      final StampSequenceSet latestStampSequences = calc.getLatestStampSequencesAsSet(this.getVersionStampSequences());

      return !latestStampSequences.isEmpty();
   }

   @Override
   public int getNid() {
      return this.nid;
   }

   @Override
   public OchreExternalizableObjectType getOchreObjectType() {
      throw new UnsupportedOperationException("Not supported.");
   }

   @Override
   public UUID getPrimordialUuid() {
      return new UUID(this.primordialUuidMsb, this.primordialUuidLsb);
   }

   /**
    *
    * @return the sememe nid for the logical expression from which
    * this relationship adaptor was derived.
    *
    */
   @Override
   public int getReferencedComponentNid() {
      return this.referencedComponentNid;
   }

   @Override
   public List<? extends SememeChronology<? extends SememeVersion<?>>> getSememeList() {
      return Collections.emptyList();
   }

   @Override
   public List<? extends SememeChronology<? extends SememeVersion<?>>> getSememeListFromAssemblage(
           int assemblageSequence) {
      return Collections.emptyList();
   }

   @Override
   public <SV extends SememeVersion> List<? extends SememeChronology<SV>> getSememeListFromAssemblageOfType(
           int assemblageSequence,
           Class<SV> type) {
      return Collections.emptyList();
   }

   @Override
   public int getSememeSequence() {
      throw new UnsupportedOperationException("Not supported.");
   }

   @Override
   public SememeType getSememeType() {
      return SememeType.RELATIONSHIP_ADAPTOR;
   }

   @Override
   public List<? extends RelationshipVersionAdaptorImpl> getUnwrittenVersionList() {
      throw new UnsupportedOperationException("Not supported.");
   }

   @Override
   public List<UUID> getUuidList() {
      return Arrays.asList(new UUID[] { getPrimordialUuid() });
   }

   @Override
   public List<RelationshipVersionAdaptorImpl> getVersionList() {
      return this.versionList;
   }

   @Override
   public IntStream getVersionStampSequences() {
      final IntStream.Builder stampSequences = IntStream.builder();

      this.versionList.forEach((version) -> {
                             stampSequences.accept(version.stampSequence);
                          });
      return stampSequences.build();
   }
}

