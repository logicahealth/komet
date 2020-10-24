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



package sh.isaac.api.chronicle;


import java.util.ArrayList;
import java.util.List;
import sh.isaac.api.collections.StampSequenceSet;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.ObservableVersion;


/**
 *
 * @author kec
 * @param <V>
 */
public class CategorizedVersions<V extends Version> {
   private final List<V>          uncommittedVersions = new ArrayList<>();
   private final List<V>          historicVersions = new ArrayList<>();
   private final LatestVersion<V> latestVersion;
   private final StampSequenceSet latestStampSequences;
   private final StampSequenceSet allStampSequences;
   private final Chronology chronology;

   public CategorizedVersions(LatestVersion<V> latestVersion, Chronology chronology) {
      boolean observableWrap = false;
      this.chronology = chronology;

      if (chronology instanceof ObservableChronology) {
         observableWrap = true;
      }

      this.allStampSequences    = latestVersion.getStamps();
      this.latestStampSequences = new StampSequenceSet();

      if (latestVersion.isPresent()) {
         this.latestStampSequences.add(latestVersion.get()
               .getStampSequence());
         latestVersion.contradictions()
                      .forEach(
                          (v) -> {
                             this.latestStampSequences.add(v.getStampSequence());
                          });
      }

      this.latestVersion = wrap(chronology, observableWrap);
   }

   //~--- methods -------------------------------------------------------------
   public int getNid() {
      return this.chronology.getNid();
   }

   public int getAssemblageNid() {
      return this.chronology.getAssemblageNid();
   }


   @Override
   public String toString() {
      return "CategorizedVersions{" + "uncommittedVersions=\n" + uncommittedVersions + "historicVersions=\n" + 
              historicVersions + ", latestVersion=\n" + latestVersion +
             ", latestStampSequences=\n" + latestStampSequences + 
              ", allStampSequences=\n" + allStampSequences + '}';
   }

   private LatestVersion<V> wrap(Chronology chronology, boolean observableWrap) {
      LatestVersion<V> wrappedLatestVersion = new LatestVersion<>();

      for (Version version: chronology.getVersionList()) {
         if (version.isUncommitted()) {
            uncommittedVersions.add(wrap(version, observableWrap));
         } else if (latestStampSequences.contains(version.getStampSequence())) {
            wrappedLatestVersion.addLatest(wrap(version, observableWrap));
         } else  {
            historicVersions.add(wrap(version, observableWrap));
         }
      }

      return wrappedLatestVersion;
   }

   @SuppressWarnings("unchecked")
   private V wrap(Version version, boolean observableWrap) {
      if (observableWrap) {
         return (V) new ObservableCategorizedVersion((ObservableVersion) version, (CategorizedVersions<CategorizedVersion>) this);
      }

      return (V) new CategorizedVersion(version, (CategorizedVersions<CategorizedVersion>) this);
   }

   public List<V> getUncommittedVersions() {
      return uncommittedVersions;
   }

   public List<V> getHistoricVersions() {
      return historicVersions;
   }

   public LatestVersion<V> getLatestVersion() {
      return latestVersion;
   }

   public VersionCategory getVersionCategory(Version version) {
      int stampSequence = version.getStampSequence();
      
      if (version.isUncommitted()) {
         return VersionCategory.Uncommitted;
      }

      if (latestStampSequences.contains(stampSequence)) {
         if (latestVersion.contradictions.isEmpty()) {
            return VersionCategory.UncontradictedLatest;
         }

         return VersionCategory.ContradictedLatest;
      }

      if (this.allStampSequences.contains(stampSequence)) {
         return VersionCategory.Prior;
      }

      return VersionCategory.Uncategorized;
   }
}

