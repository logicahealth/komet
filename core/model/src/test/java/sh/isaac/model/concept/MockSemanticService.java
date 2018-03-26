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
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.SemanticSnapshotService;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.externalizable.IsaacObjectType;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 1/3/16.
 */
@Service
@Rank(value = -50)
public class MockSemanticService
         implements AssemblageService {
   /** The component semantic map. */
   ConcurrentHashMap<Integer, NidSet> componentSemanticMap = new ConcurrentHashMap<>();

   /** The semantic map. */
   ConcurrentHashMap<Integer, SemanticChronology> semanticMap = new ConcurrentHashMap<>();

   //~--- methods -------------------------------------------------------------


   /**
    * Write semantic.
    *
    * @param semanticChronicle the semantic chronicle
    */
   @Override
   public void writeSemanticChronology(SemanticChronology semanticChronicle) {
      if (this.componentSemanticMap.containsKey(semanticChronicle.getReferencedComponentNid())) {
         this.componentSemanticMap.get(semanticChronicle.getReferencedComponentNid())
                                .add(semanticChronicle.getNid());
      } else {
         final NidSet set = NidSet.of(semanticChronicle.getNid());

         this.componentSemanticMap.put(semanticChronicle.getReferencedComponentNid(), set);
      }

      this.semanticMap.put(semanticChronicle.getNid(),
                         (SemanticChronology) semanticChronicle);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the database folder.
    *
    * @return the database folder
    */
   @Override
   public Path getDataStorePath() {
      return null;
   }

   /**
    * Gets the database validity status.
    *
    * @return the database validity status
    */
   @Override
   public DataStoreStartState getDataStoreStartState() {
      return DataStoreStartState.NO_DATASTORE;
   }

   /**
    * Gets the descriptions for component.
    *
    * @param componentNid the component nid
    * @return the descriptions for component
    */
   @Override
   public List<SemanticChronology> getDescriptionsForComponent(int componentNid) {
      final NidSet set = this.componentSemanticMap.get(componentNid);
      List<SemanticChronology> results = new ArrayList<>();
        set.stream().forEach((semanticSequence) -> {
                        final SemanticChronology semanticChronology = this.semanticMap.get(semanticSequence);

                        if (semanticChronology.getVersionType() == VersionType.DESCRIPTION) {
                           results.add(semanticChronology);
                        }
                     });
      return results;
   }

   /**
    * Gets the optional semantic.
    *
    * @param semanticId the semantic id
    * @return the optional semantic
    */
   @Override
   public Optional<? extends SemanticChronology> getOptionalSemanticChronology(int semanticId) {
      return Optional.ofNullable(getSemanticChronology(semanticId));
   }

   /**
    * Gets the semantic.
    *
    * @param semanticId the semantic id
    * @return the semantic
    */
   @Override
   public SemanticChronology getSemanticChronology(int semanticId) {
      return this.semanticMap.get(semanticId);
   }

   /**
    * Gets the semantic chronology stream.
    *
    * @return the semantic chronology stream
    */
   @Override
   public Stream<SemanticChronology> getSemanticChronologyStream() {
      return this.semanticMap.values()
                           .stream();
   }

   /**
    * Gets the semantic key stream.
    *
    * @return the semantic key stream
    */
   @Override
   public IntStream getSemanticNidStream() {
      return this.semanticMap.keySet()
                           .stream()
                           .mapToInt(i -> i);
   }

   /**
    * Gets the semantic sequences for component.
    *
    * @param componentNid the component nid
    * @return the semantic sequences for component
    */
   @Override
   public NidSet getSemanticNidsForComponent(int componentNid) {
      throw new UnsupportedOperationException();
   }

   /**
    * Gets the semantic sequences for component from assemblage.
    *
    * @param componentNid the component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the semantic sequences for component from assemblage
    */
   @Override
   public NidSet getSemanticNidsForComponentFromAssemblage(int componentNid,
         int assemblageConceptSequence) {
      throw new UnsupportedOperationException();
   }


   /**
    * Gets the semantic sequences from assemblage.
    *
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the semantic sequences from assemblage
    */
   @Override
   public NidSet getSemanticNidsFromAssemblage(int assemblageConceptSequence) {
      throw new UnsupportedOperationException();
   }

   /**
    * Gets the semantics for component.
    *
    * @param componentNid the component nid
    * @return the semantics for component
    */
   @Override
   public Stream<SemanticChronology> getSemanticChronologyStreamForComponent(int componentNid) {
      throw new UnsupportedOperationException();
   }

   /**
    * Gets the semantics for component from assemblage.
    *
    * @param componentNid the component nid
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the semantics for component from assemblage
    */
   @Override
   public <C extends SemanticChronology> Stream<C> getSemanticChronologyStreamForComponentFromAssemblage(int componentNid,
         int assemblageConceptSequence) {
      throw new UnsupportedOperationException();
   }

   /**
    * Gets the semantics from assemblage.
    *
    * @param assemblageConceptSequence the assemblage concept sequence
    * @return the semantics from assemblage
    */
   @Override
   public Stream<SemanticChronology> getSemanticChronologyStream(int assemblageConceptSequence) {
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
   public Optional<UUID> getDataStoreId() {
      return Optional.of(UUID.randomUUID());
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

   public int[] getAssemblageConceptNids() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

    @Override
    public IsaacObjectType getObjectTypeForAssemblage(int assemblageNid) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public VersionType getVersionTypeForAssemblage(int assemblageNid) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getAssemblageMemoryInUse(int assemblageNid) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getAssemblageSizeOnDisk(int assemblageNid) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <C extends Chronology> Stream<C> getChronologyStream(int assemblageConceptSequence) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

