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



package sh.isaac.model.concept;

//~--- JDK imports ------------------------------------------------------------

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import java.util.stream.Stream;

//~--- non-JDK imports --------------------------------------------------------

import org.glassfish.hk2.api.Rank;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.AssemblageService;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.SemanticSnapshotService;
import sh.isaac.api.component.semantic.version.SemanticVersion;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 1/3/16.
 */
@Service
@Rank(value = -50)
public class MockSemanticService
         implements AssemblageService {
   /** The component sememe map. */
   ConcurrentHashMap<Integer, NidSet> componentSememeMap = new ConcurrentHashMap<>();

   /** The sememe map. */
   ConcurrentHashMap<Integer, SemanticChronology> semanticMap = new ConcurrentHashMap<>();

   //~--- methods -------------------------------------------------------------


   /**
    * Write sememe.
    *
    * @param sememeChronicle the sememe chronicle
    */
   @Override
   public void writeSemanticChronology(SemanticChronology sememeChronicle) {
      if (this.componentSememeMap.containsKey(sememeChronicle.getReferencedComponentNid())) {
         this.componentSememeMap.get(sememeChronicle.getReferencedComponentNid())
                                .add(sememeChronicle.getNid());
      } else {
         final NidSet set = NidSet.of(sememeChronicle.getNid());

         this.componentSememeMap.put(sememeChronicle.getReferencedComponentNid(), set);
      }

      this.semanticMap.put(sememeChronicle.getNid(),
                         (SemanticChronology) sememeChronicle);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the database folder.
    *
    * @return the database folder
    */
   @Override
   public Path getDatabaseFolder() {
      return null;
   }

   /**
    * Gets the database validity status.
    *
    * @return the database validity status
    */
   @Override
   public DatabaseValidity getDatabaseValidityStatus() {
      return null;
   }

   /**
    * Gets the descriptions for component.
    *
    * @param componentNid the component nid
    * @return the descriptions for component
    */
   @Override
   public List<SemanticChronology> getDescriptionsForComponent(int componentNid) {
      final NidSet set = this.componentSememeMap.get(componentNid);
      List<SemanticChronology> results = new ArrayList<>();
        set.stream().forEach((sememeSequence) -> {
                        final SemanticChronology semanticChronology = this.semanticMap.get(sememeSequence);

                        if (semanticChronology.getVersionType() == VersionType.DESCRIPTION) {
                           results.add(semanticChronology);
                        }
                     });
      return results;
   }

   /**
    * Gets the optional sememe.
    *
    * @param sememeId the sememe id
    * @return the optional sememe
    */
   @Override
   public Optional<? extends SemanticChronology> getOptionalSemanticChronology(int sememeId) {
      return Optional.ofNullable(getSemanticChronology(sememeId));
   }

   /**
    * Gets the sememe.
    *
    * @param sememeId the sememe id
    * @return the sememe
    */
   @Override
   public SemanticChronology getSemanticChronology(int sememeId) {
      return this.semanticMap.get(sememeId);
   }

   /**
    * Gets the sememe chronology stream.
    *
    * @return the sememe chronology stream
    */
   @Override
   public Stream<SemanticChronology> getSemanticChronologyStream() {
      return this.semanticMap.values()
                           .stream();
   }

   /**
    * Gets the sememe key stream.
    *
    * @return the sememe key stream
    */
   @Override
   public IntStream getSemanticNidStream() {
      return this.semanticMap.keySet()
                           .stream()
                           .mapToInt(i -> i);
   }

   /**
    * Gets the sememe sequences for component.
    *
    * @param componentNid the component nid
    * @return the sememe sequences for component
    */
   @Override
   public NidSet getSemanticNidsForComponent(int componentNid) {
      throw new UnsupportedOperationException();
   }

   /**
    * Gets the sememe sequences for component from assemblage.
    *
    * @param componentNid the component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the sememe sequences for component from assemblage
    */
   @Override
   public NidSet getSemanticNidsForComponentFromAssemblage(int componentNid,
         int assemblageConceptSequence) {
      throw new UnsupportedOperationException();
   }


   /**
    * Gets the sememe sequences from assemblage.
    *
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the sememe sequences from assemblage
    */
   @Override
   public NidSet getSemanticNidsFromAssemblage(int assemblageConceptSequence) {
      throw new UnsupportedOperationException();
   }

   /**
    * Gets the sememes for component.
    *
    * @param componentNid the component nid
    * @return the sememes for component
    */
   @Override
   public Stream<SemanticChronology> getSemanticChronologyStreamForComponent(int componentNid) {
      throw new UnsupportedOperationException();
   }

   /**
    * Gets the sememes for component from assemblage.
    *
    * @param componentNid the component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the sememes for component from assemblage
    */
   @Override
   public <C extends SemanticChronology> Stream<C> getSemanticChronologyStreamForComponentFromAssemblage(int componentNid,
         int assemblageConceptSequence) {
      throw new UnsupportedOperationException();
   }

   /**
    * Gets the sememes from assemblage.
    *
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the sememes from assemblage
    */
   @Override
   public Stream<SemanticChronology> getSemanticChronologyStreamFromAssemblage(int assemblageConceptSequence) {
      throw new UnsupportedOperationException();
   }

   /**
    * Gets the snapshot.
    *
    * @param <V> the value type
    * @param versionType the version type
    * @param stampCoordinate the stamp coordinate
    * @return the snapshot
    */
   @Override
   public <V extends SemanticVersion> SemanticSnapshotService<V> getSnapshot(Class<V> versionType,
         StampCoordinate stampCoordinate) {
      throw new UnsupportedOperationException();
   }

   @Override
   public UUID getDataStoreId() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public int getSemanticCount() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public int getSemanticCount(int assemblageNid) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public IntStream getSemanticNidStream(int assemblageNid) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public Future<?> sync() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public NidSet getSemanticNidsForComponentFromAssemblages(int componentNid, Set<Integer> assemblageConceptNids) {
      throw new UnsupportedOperationException("Not supported yet.");
   }
   
   @Override
   public <C extends SemanticChronology> Stream<C> getSemanticChronologyStreamForComponentFromAssemblages(int componentNid,
      Set<Integer> assemblageConceptNids) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

	@Override
	public boolean hasSemantic(int semanticId) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}

