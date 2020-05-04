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
package sh.isaac.provider.datastore.chronology;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.*;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.IntSet;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.collections.jsr166y.ConcurrentReferenceHashMap;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.commit.ChronologyChangeListener;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.commit.CommitService;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSnapshot;
import sh.isaac.api.component.concept.ConceptSnapshotService;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.SemanticSnapshotService;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.MutableStringVersion;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.constants.DatabaseInitialization;
import sh.isaac.api.coordinate.*;
import sh.isaac.api.datastore.DataStore;
import sh.isaac.api.externalizable.BinaryDataReaderService;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.task.LabelTaskWithIndeterminateProgress;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.model.ChronologyImpl;
import sh.isaac.model.ChronologyService;
import sh.isaac.model.ModelGet;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.model.concept.ConceptSnapshotImpl;
import sh.isaac.model.configuration.EditCoordinates;
import sh.isaac.model.semantic.SemanticChronologyImpl;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import java.util.stream.Stream;

//~--- classes ----------------------------------------------------------------
/**
 *
 * @author kec
 */
@Service
@RunLevel(value = LookupService.SL_L2)
public class ChronologyProvider
        implements ChronologyService, MetadataService {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LogManager.getLogger();

    //~--- fields --------------------------------------------------------------
    private DataStore store;
    
   //set to -1, when we haven't loaded yet.  Set to 1, when we have (and did) load metadata.  Set to 0, when we have checked, 
   //but didn't load metadata because the database was already loaded, or the preferences said not to.
   private AtomicInteger metadataLoaded = new AtomicInteger(-1);

   private AtomicLong writeSequence = new AtomicLong();

   private Cache<Integer, Chronology> nidToChronologyCache = Caffeine.newBuilder()
           .initialCapacity(1000).maximumSize(1000).build();

    //~--- methods -------------------------------------------------------------


    @Override
    public long getWriteSequence() {
        return this.writeSequence.get();
    }

    @Override
    public void importMetadata()
            throws Exception {
       //set to -1, when we haven't loaded yet.  Set to 1, when we have (and did) load metadata.  Set to 0, when we have checked, 
       //but didn't load metadata because the database was already loaded, or the preferences said not to.
       if (metadataLoaded.get() < 0) {
          synchronized (metadataLoaded) {
             
             if (store.getDataStoreStartState() == DataStoreStartState.NO_DATASTORE) {
                DatabaseInitialization initializationPreference = Get.configurationService().getDatabaseInitializationMode();
       
                if (initializationPreference == DatabaseInitialization.LOAD_METADATA) {
                   LOG.info("loading system metadata");
                   loadMetaData();
                   metadataLoaded.set(1);
                }
             }
             //mark this method as called, but executed as a noop.
             if (metadataLoaded.get() < 0)
             {
                LOG.info("import metadata called, but not loading.  Pref: {}, Store start state: {}", Get.configurationService().getDatabaseInitializationMode().name(),
                      store.getDataStoreStartState().name());
                metadataLoaded.set(0);
             }
          }
       }
    }

    @Override
    public boolean reimportMetadata() throws Exception {
        AtomicBoolean changed = new AtomicBoolean(false);
        LOG.info("reloading system metadata");
         final CommitService commitService = Get.commitService();

         try (BinaryDataReaderService reader = getMetadataStream()) {
             reader.getStream()
                     .forEach(
                             (object) -> {
                                 try {
                                     commitService.importIfContentChanged(object);
                                 } catch (Throwable e) {
                                     e.printStackTrace();
                                     throw e;
                                 }
                             });
        }

        commitService.postProcessImportNoChecks();
        return changed.get();
    }

    private BinaryDataReaderService getMetadataStream() throws FileNotFoundException {
        InputStream dataStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream("sh/isaac/IsaacMetadataAuxiliary.ibdf");
        return Get.binaryDataReader(dataStream);
    }
    @Override
    public Future<?> sync() {
        return this.store.sync();
    }
    
    /**
    * {@inheritDoc}
    */
   @Override
   public boolean wasMetadataImported() {
      return metadataLoaded.get() == 1;
   }

    @Override
    public void putChronologyData(Chronology chronology) {
        if (chronology instanceof ConceptChronology) {
            writeConcept((ConceptChronology) chronology);
        } else if (chronology instanceof SemanticChronology) {
            writeSemanticChronology((SemanticChronology) chronology);
        } else {
            throw new UnsupportedOperationException("Cant handle: " + chronology);
        }
            
    }

   
    @Override
    public void writeConcept(ConceptChronology concept) {
        this.writeSequence.incrementAndGet();
        this.nidToChronologyCache.invalidate(concept.getNid());
        Get.conceptActiveService()
                .updateStatus(concept);
        this.store.putChronologyData((ChronologyImpl) concept);
    }

    @Override
    public void writeSemanticChronology(SemanticChronology semanticChronicle) {
        this.writeSequence.incrementAndGet();
        this.nidToChronologyCache.invalidate(semanticChronicle.getNid());
        this.store.putChronologyData((ChronologyImpl) semanticChronicle);
//        if (semanticChronicle.getVersionType().equals(VersionType.LOGIC_GRAPH)) {
//            Get.taxonomyService().updateTaxonomy(semanticChronicle);
//        }
    }

    private void loadMetaData()
            throws Exception {

        try (BinaryDataReaderService reader = getMetadataStream()) {
            final CommitService commitService = Get.commitService();
            reader.getStream()
                    .forEach(
                            (object) -> {
                                try {
                                    commitService.importNoChecks(object);
                                } catch (Throwable e) {
                                    e.printStackTrace();
                                    throw e;
                                }
                            });
            commitService.postProcessImportNoChecks();
        }

      //Store the DB id as a semantic
      Transaction transaction = Get.commitService().newTransaction(Optional.empty(), ChangeCheckerMode.ACTIVE);
      Get.semanticBuilderService()
            .getStringSemanticBuilder(getDataStoreId().get().toString(), TermAux.SOLOR_ROOT.getNid(), TermAux.DATABASE_UUID.getNid())
            .build(transaction, EditCoordinates.getDefaultUserMetadata()).get();
      transaction.commit("Storing database ID on root concept").get();
    }

    /**
     * Start me.
     */
    @PostConstruct
    private void startMe() {
        LabelTaskWithIndeterminateProgress progressTask = new LabelTaskWithIndeterminateProgress("Starting chronology provider");
        Get.executor().execute(progressTask);
        try {
            LOG.info("Starting chronology provider for change to runlevel: " + LookupService.getProceedingToRunLevel());
            this.metadataLoaded.set(-1);
            store = Get.service(DataStore.class);
            if (store == null) {
                throw new RuntimeException("Failed to get a data store!");
            }
        } finally {
            progressTask.finished();
        }
    }

    /**
     * Stop me.
     */
    @PreDestroy
    private void stopMe() {
        try {
            LOG.info("Stopping chronology provider for change to runlevel: " + LookupService.getProceedingToRunLevel());
            this.sync().get();
            this.metadataLoaded.set(-1);
        } catch (InterruptedException | ExecutionException ex) {
            LOG.error(ex);
        }
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public IsaacObjectType getObjectTypeForAssemblage(int assemblageNid) {
        return store.getIsaacObjectTypeForAssemblageNid(assemblageNid);
    }

    @Override
    public int[] getAssemblageConceptNids() {
        return this.store.getAssemblageConceptNids();
    }

    private Optional<ByteArrayDataBuffer> getChronologyData(int nid) {
        return this.store.getChronologyVersionData(nid);
    }

    @Override
    public boolean isConceptActive(int conceptSequence, StampFilterImmutable stampFilter) {
        return Get.conceptActiveService()
                .isConceptActive(conceptSequence, stampFilter);
    }

    @Override
    public ConceptChronology getConceptChronology(ConceptSpecification conceptSpecification) {
        return ChronologyProvider.this.getConceptChronology(conceptSpecification.getNid());
    }

    @Override
    public ConceptChronologyImpl getConceptChronology(int conceptId) {
        Object possibleConcept = nidToChronologyCache.getIfPresent(conceptId);
        if (possibleConcept != null && !(possibleConcept instanceof ConceptChronologyImpl)) {
            throw new IllegalStateException("Concept cache cannot contain: " + possibleConcept);
        }

        ConceptChronologyImpl chronology = (ConceptChronologyImpl) possibleConcept;
        if (chronology == null) {
            Optional<ByteArrayDataBuffer> optionalByteBuffer = store.getChronologyVersionData(conceptId);

            if (optionalByteBuffer.isPresent()) {
                ByteArrayDataBuffer byteBuffer = optionalByteBuffer.get();
                IsaacObjectType.CONCEPT.readAndValidateHeader(byteBuffer);
                chronology = ConceptChronologyImpl.make(byteBuffer);
                if (!chronology.isUncommitted()) {
                    nidToChronologyCache.put(chronology.getNid(), chronology);
                }

            } else {
                throw new NoSuchElementException("No element for: " + conceptId + " " + Arrays.toString(Get.identifierService().getUuidsForNid(conceptId).toArray()));
            }
        }

        return chronology;

    }

    @Override
    public ConceptChronology getConceptChronology(UUID... conceptUuids) {
        return ChronologyProvider.this.getConceptChronology(Get.identifierService()
                .getNidForUuids(conceptUuids));
    }

    @Override
    public Stream<ConceptChronology> getConceptChronologyStream() {
      return ModelGet.identifierService().getNidStreamOfType(IsaacObjectType.CONCEPT).mapToObj((nid) -> {
         try {
            return (ConceptChronology) getConceptChronology(nid);
         } catch (NoSuchElementException e) {
            return null;  //This will happen if a nid was mapped, but the object wasn't stored.
         }
      }).filter(obj -> obj != null);  //remove the nulls
    }

   @Override
   public Stream<ConceptChronology> getConceptChronologyStream(int assemblageNid) {
      return Get.identifierService().getNidsForAssemblage(assemblageNid).mapToObj((nid) -> {
         try {
            return (ConceptChronology) getConceptChronology(nid);
         } catch (NoSuchElementException e) {
            return null; // This will happen if a nid was mapped, but the object wasn't stored.
         }
      }).filter(obj -> obj != null); // remove the nulls
   }

    @Override
    public Stream<ConceptChronology> getConceptChronologyStream(IntSet conceptNids) {
        return conceptNids.parallelStream()
                .mapToObj(
                        (nid) -> {
                            return getConceptChronology(nid);
                        });
    }

    @Override
    public int getConceptCount() {
        return (int) ModelGet.identifierService()
                .getNidStreamOfType(IsaacObjectType.CONCEPT)
                .parallel()
                .filter(nid -> hasConcept(nid))
                .count();
    }

    @Override
   public int getConceptCount(int assemblageNid) {
      return (int) getConceptNidStream(assemblageNid)
            .parallel()
            .filter(nid -> hasConcept(nid))
            .count();
   }

    @Override
    public IntStream getConceptNidStream() {
        return ModelGet.identifierService()
                .getNidStreamOfType(IsaacObjectType.CONCEPT)
                .filter(nid -> hasConcept(nid));
    }

    @Override
    public IntStream getConceptNidStream(int assemblageNid) {
        return Get.identifierService()
                .getNidsForAssemblage(assemblageNid)
                .filter(nid -> hasConcept(nid));
    }

    @Override
    public Optional<UUID> getDataStoreId() {
       UUID fromFile = store.getDataStoreId().orElse(null);
       
       //This is a sanity check, which gets run by the Lookup Service during the startup sequence.
      Optional<SemanticChronology> sdic = getSemanticChronologyStreamForComponentFromAssemblage(TermAux.SOLOR_ROOT.getNid(), TermAux.DATABASE_UUID.getNid())
             .findFirst();
       if (sdic.isPresent()) {
          LatestVersion<Version> sdi = sdic.get().getLatestVersion(Coordinates.Filter.DevelopmentLatest());
          if (sdi.isPresent()) {
             try {
                UUID temp = UUID.fromString(((StringVersion) sdi.get()).getString());
                
                if (!temp.equals(fromFile)) {
                   LOG.info("Semantic Store has {} while file store has {}.  This is expected, if an IBDF file with an existing ID was merged into a datastore."
                         + "  Updating the semantic store to match the file store id.", temp, fromFile);

                   Transaction transaction = Get.commitService().newTransaction(Optional.empty(), ChangeCheckerMode.ACTIVE);
                   MutableStringVersion sv = sdic.get().createMutableVersion(transaction, Status.ACTIVE, EditCoordinates.getDefaultUserMetadata());
                   sv.setString(fromFile.toString());
                   Get.commitService().addUncommitted(transaction, sv);
                   transaction.commit("Updating database ID on root concept").get();
                }
                   
             } catch (Exception e) {
                LOG.warn("Unexpected error checking alignment of database UUID!", e);
             }
          }
       }
       return Optional.ofNullable(fromFile);
    }

    @Override
    public Path getDataStorePath() {
        return store.getDataStorePath();
    }

    @Override
    public DataStoreStartState getDataStoreStartState() {
        return store.getDataStoreStartState();
    }

    @Override
    public List<SemanticChronology> getDescriptionsForComponent(int componentNid) {
        if (componentNid >= 0) {
            throw new IndexOutOfBoundsException("Component identifiers must be negative. Found: " + componentNid);
        }
        IdentifierService identifierService = ModelGet.identifierService();

        List<SemanticChronology> results = new ArrayList<>();
        ImmutableIntSet semanticNids = getSemanticNidsForComponent(componentNid);
        semanticNids.forEach(semanticNid -> {
            int assemblageNid = identifierService.getAssemblageNid(semanticNid).getAsInt();
            VersionType versionType = getVersionTypeForAssemblage(assemblageNid);
            if (versionType == VersionType.DESCRIPTION) {
                try {
                    SemanticChronology semanticChronology = getSemanticChronology(semanticNid);
                    if ((semanticChronology != null)) {
                        results.add(semanticChronology);
                    }
                } catch (NoSuchElementException e) {
                    LOG.error(e);
                }
            }
        });
        return results;
    }

    @Override
    public Optional<? extends Chronology> getChronology(int nid) {
        try {
            Optional<ByteArrayDataBuffer> optionalByteBuffer = getChronologyData(nid);

            if (optionalByteBuffer.isPresent()) {
                ByteArrayDataBuffer byteBuffer = optionalByteBuffer.get();

                // concept or semantic?
                switch (ModelGet.identifierService()
                        .getObjectTypeForComponent(nid)) {
                    case CONCEPT:
                        IsaacObjectType.CONCEPT.readAndValidateHeader(byteBuffer);
                        return Optional.of(ConceptChronologyImpl.make(byteBuffer));

                    case SEMANTIC:
                        IsaacObjectType.SEMANTIC.readAndValidateHeader(byteBuffer);
                        return Optional.of(SemanticChronologyImpl.make(byteBuffer));

                    default:
                        throw new UnsupportedOperationException(
                                "Can't handle: " + ModelGet.identifierService().getObjectTypeForComponent(nid));
                }
            }
        } catch (NoSuchElementException nse) {
            return Optional.empty();
        }

        return Optional.empty();
    }

    @Override
    public Optional<? extends ConceptChronology> getOptionalConcept(int conceptNid) {
        if (hasConcept(conceptNid)) {
           return Optional.of(getConceptChronology(conceptNid));
        }
        return Optional.empty();
    }

    @Override
    public Optional<? extends ConceptChronology> getOptionalConcept(UUID... conceptUuids) {
       if (Get.identifierService().hasUuid(conceptUuids)) {
          return getOptionalConcept(Get.identifierService().getNidForUuids(conceptUuids));
       }
       return Optional.empty();
    }

    @Override
    public Optional<? extends SemanticChronology> getOptionalSemanticChronology(int semanticNid) {
        if (hasSemantic(semanticNid)) {
            try {
                return Optional.of(getSemanticChronology(semanticNid));
            }
            //There are some rare, but possible timing issues if reads and writes are happening in parallel, where hasSemantic might return true, but
            //it is in fact, not yet readable.
            catch (NoSuchElementException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    @Override
    public SemanticChronology getSemanticChronology(int semanticId) {

        SemanticChronology chronology = (SemanticChronology) nidToChronologyCache.getIfPresent(semanticId);
        if (chronology == null) {
            Optional<ByteArrayDataBuffer> optionalByteBuffer = store.getChronologyVersionData(semanticId);

            if (optionalByteBuffer.isPresent()) {
                ByteArrayDataBuffer byteBuffer = optionalByteBuffer.get();

                IsaacObjectType.SEMANTIC.readAndValidateHeader(byteBuffer);
                chronology = SemanticChronologyImpl.make(byteBuffer);
                if (!chronology.isUncommitted()) {
                    nidToChronologyCache.put(chronology.getNid(), chronology);
                }
            } else {
                throw new NoSuchElementException("No element for: " + semanticId + " " + Arrays.toString(Get.identifierService().getUuidsForNid(semanticId).toArray()));
            }
        }
        return chronology;
    }

   @Override
   public Stream<SemanticChronology> getSemanticChronologyStream() {
      return getSemanticNidStream().mapToObj((value) -> {
         try {
            return (SemanticChronology) getSemanticChronology(value);
         } catch (NoSuchElementException e) {
            return null; // This will happen if a nid was mapped, but the object wasn't stored.
         }
      }).filter(obj -> obj != null); // remove the nulls
   }

    @Override
    public <C extends SemanticChronology> Stream<C> getSemanticChronologyStreamForComponent(int componentNid) {
        return Arrays.stream(getSemanticNidsForComponent(componentNid).toArray())
                .mapToObj((int semanticNid) -> { 
                try {
                  return (C) getSemanticChronology(semanticNid);
               } catch (NoSuchElementException e) {
                  return null; // This will happen if a nid was mapped, but the object wasn't stored.
               }
            }).filter(obj -> obj != null); // remove the nulls
    }
    
    @Override
    public <C extends SemanticChronology> Stream<C> getSemanticChronologyStreamForComponentFromAssemblage(int componentNid, int assemblageConceptNid) {
       return getSemanticChronologyStreamForComponentFromAssemblages(componentNid, Collections.singleton(assemblageConceptNid));
    }
    
    @Override
    public <C extends SemanticChronology> Stream<C> getSemanticChronologyStreamForComponentFromAssemblages(int componentNid,
          Set<Integer> assemblageConceptNids) {
       final ImmutableIntSet semanticSequences = getSemanticNidsForComponentFromAssemblages(componentNid, assemblageConceptNids);

       return Arrays.stream(semanticSequences.toArray()).parallel().mapToObj((int semanticNid) -> {
           try {
             return (C) getSemanticChronology(semanticNid);
          } catch (NoSuchElementException e) {
             return null; // This will happen if a nid was mapped, but the object wasn't stored.
          }
       }).filter(obj -> obj != null); // remove the nulls
    }

    @Override
    public <C extends SemanticChronology> Stream<C> getSemanticChronologyStream(int assemblageConceptNid) {
        switch (getObjectTypeForAssemblage(assemblageConceptNid)) {
            case SEMANTIC:
                final ImmutableIntSet semanticSequences = getSemanticNidsFromAssemblage(assemblageConceptNid);

                return Arrays.stream(semanticSequences.toArray()).parallel()
                        .mapToObj((int semanticNid) -> 
                        {
                             try {
                               return (C) getSemanticChronology(semanticNid);
                            } catch (NoSuchElementException e) {
                               return null; // This will happen if a nid was mapped, but the object wasn't stored.
                            }
                         }).filter(obj -> obj != null); // remove the nulls

            case UNKNOWN:
                // perhaps not initialized...
                final ImmutableIntSet elementSequences = getSemanticNidsFromAssemblage(assemblageConceptNid);
                return (Stream<C>) Arrays.stream(elementSequences.toArray()).parallel().mapToObj((nid) -> getChronology(nid))
                        .filter((optionalObject) -> optionalObject.isPresent())
                        .map((optionalObject) -> optionalObject.get());
        }
        throw new IllegalStateException("Assemblage is of type "
                + getObjectTypeForAssemblage(assemblageConceptNid)
                + " not of type " + IsaacObjectType.SEMANTIC);
    }

    @Override
    public <C extends Chronology> Stream<C> getChronologyStream(int assemblageConceptNid) {
        switch (getObjectTypeForAssemblage(assemblageConceptNid)) {
            case CONCEPT:
                return (Stream<C>) getConceptChronologyStream(assemblageConceptNid);
            case SEMANTIC:
                return (Stream<C>) getSemanticChronologyStream(assemblageConceptNid);
            case UNKNOWN:
                // perhaps not initialized...
                final ImmutableIntSet elementSequences = getSemanticNidsFromAssemblage(assemblageConceptNid);
                return (Stream<C>) Arrays.stream(elementSequences.toArray()).parallel().mapToObj((nid) -> getChronology(nid))
                        .filter((optionalObject) -> optionalObject.isPresent())
                        .map((optionalObject) -> optionalObject.get());

        }
        throw new IllegalStateException("Assemblage is of type "
                + getObjectTypeForAssemblage(assemblageConceptNid)
                + " not of type IsaacObjectType.SEMANTIC or IsaacObjectType.CONCEPT");
    }

    @Override
    public int getSemanticCount() {
        return (int) ModelGet.identifierService()
                .getNidStreamOfType(IsaacObjectType.SEMANTIC)
                .count();
    }

    @Override
    public int getSemanticCount(int assemblageNid) {
        return (int) ModelGet.identifierService()
                .getNidsForAssemblage(assemblageNid)
                .count();
    }

    @Override
    public IntStream getSemanticNidStream() {
        return ModelGet.identifierService()
                .getNidStreamOfType(IsaacObjectType.SEMANTIC)
                .filter(nid -> hasSemantic(nid));
    }

    @Override
    public IntStream getSemanticNidStream(int assemblageNid) {
        return ModelGet.identifierService()
                .getNidsForAssemblage(assemblageNid)
                .filter(nid -> hasSemantic(nid));
    }

    @Override
    public ImmutableIntSet getSemanticNidsForComponent(int componentNid) {
        return IntSets.immutable.of(store.getSemanticNidsForComponent(componentNid));
    }

    @Override
    public ImmutableIntSet getSemanticNidsForComponentFromAssemblage(int componentNid, int assemblageNid) {
       return getSemanticNidsForComponentFromAssemblages(componentNid, Collections.singleton(assemblageNid));
    }
    
    @Override
    public ImmutableIntSet getSemanticNidsForComponentFromAssemblages(int componentNid, Set<Integer> assemblageConceptNids) {
       if (componentNid >= 0) {
          throw new IndexOutOfBoundsException("Component identifiers must be negative. Found: " + componentNid);
       }
       if (assemblageConceptNids == null || assemblageConceptNids.isEmpty())
       {
          return getSemanticNidsForComponent(componentNid);
       }
       
       for (int assemblageNid : assemblageConceptNids) {
          if (assemblageNid >= 0) {
             throw new IndexOutOfBoundsException("Assemblage identifiers must be negative. Found: " + assemblageNid);
          }
       }
       IdentifierService identifierService = ModelGet.identifierService();

       MutableIntSet semanticNids = IntSets.mutable.empty();
       for (int semanticNid: store.getSemanticNidsForComponent(componentNid)) {
          if (assemblageConceptNids.contains(identifierService.getAssemblageNid(semanticNid).getAsInt())) {
             semanticNids.add(semanticNid);
          }
       }
       return semanticNids.toImmutable();
    }

    @Override
    public ImmutableIntSet getSemanticNidsFromAssemblage(int assemblageNid) {
        return IntSets.immutable.ofAll(ModelGet.identifierService()
                .getNidsForAssemblage(assemblageNid)
                .filter(nid -> hasSemantic(nid)));
    }

    private static final ConcurrentReferenceHashMap<ManifoldCoordinateImmutable, ConceptSnapshotService> CONCEPT_SNAPSHOTS =
            new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.WEAK,
                    ConcurrentReferenceHashMap.ReferenceType.WEAK);

    @Override
    public ConceptSnapshotService getSnapshot(ManifoldCoordinateImmutable manifoldCoordinate) {
        return CONCEPT_SNAPSHOTS.computeIfAbsent(manifoldCoordinate,
                manifoldCoordinateImmutable -> new ConceptSnapshotProvider(manifoldCoordinate));
    }

    @Override
    public <V extends SemanticVersion> SemanticSnapshotService<V> getSnapshot(Class<V> versionType,
                                                                              StampFilter stampFilter) {
        return new AssemblageSnapshotProvider<>(versionType, stampFilter, this);
    }

    // TODO implement with a persistent cache of version types...
    @Override
    public VersionType getVersionTypeForAssemblage(int assemblageNid) {
        VersionType versionType = this.store.getVersionTypeForAssemblageNid(assemblageNid);
        if (versionType != VersionType.UNKNOWN) {
            return versionType;
        }

        IsaacObjectType objectType = getObjectTypeForAssemblage(assemblageNid);
        switch (objectType) {
            case CONCEPT:
                this.store.putAssemblageVersionType(assemblageNid, VersionType.CONCEPT);
                return VersionType.CONCEPT;
            default:
            // fall through. 
        }
        Optional<SemanticChronology> semanticChronologyOptional = getSemanticChronologyStream(assemblageNid).findFirst();
        if (semanticChronologyOptional.isPresent()) {
            this.store.putAssemblageVersionType(assemblageNid, semanticChronologyOptional.get().getVersionType());
            return semanticChronologyOptional.get().getVersionType();
        }
        return VersionType.UNKNOWN;
    }
    
    @Override
    public boolean hasConcept(int conceptId) {
      return store.hasChronologyData(conceptId, IsaacObjectType.CONCEPT);
    }
    
    @Override
    public boolean hasSemantic(int semanticId) {
       return store.hasChronologyData(semanticId, IsaacObjectType.SEMANTIC);
    }

    @Override
    public int getAssemblageMemoryInUse(int assemblageNid) {
        return store.getAssemblageMemoryInUse(assemblageNid);
    }

    @Override
    public int getAssemblageSizeOnDisk(int assemblageNid) {
        return store.getAssemblageSizeOnDisk(assemblageNid);
    }

    //~--- inner classes -------------------------------------------------------
    /**
     * The Class ConceptSnapshotProvider.
     */
    public class ConceptSnapshotProvider implements ConceptSnapshotService {
        final ManifoldCoordinate manifoldCoordinate;
        final LanguageCoordinate regNameCoord;
        final StampFilterImmutable stampFilterImmutable;
        final UUID listenerUuid = UUID.randomUUID();

        /**
         * Instantiates a new concept snapshot provider.
         *
         * @param manifoldCoordinate
         */
        public ConceptSnapshotProvider(ManifoldCoordinate manifoldCoordinate) {
            this.manifoldCoordinate = manifoldCoordinate;
            this.stampFilterImmutable = manifoldCoordinate.getStampFilter().toStampFilterImmutable();
            this.regNameCoord = Coordinates.Language.AnyLanguageRegularName();
        }

        @Override
        public String toString() {
            return "ConceptSnapshotProvider{" + "manifoldCoordinate=" + this.manifoldCoordinate + '}';
        }

        @Override
        public boolean isConceptActive(int conceptNid) {
            return ChronologyProvider.this.isConceptActive(conceptNid, this.stampFilterImmutable);
        }

        @Override
        public ConceptSnapshot getConceptSnapshot(ConceptSpecification conceptSpecification) {
            return getConceptSnapshot(conceptSpecification.getNid());
        }

        @Override
        public ConceptSnapshot getConceptSnapshot(int conceptNid) {
            return new ConceptSnapshotImpl(getConceptChronology(conceptNid), this.manifoldCoordinate);
        }

        @Override
        public String conceptDescriptionText(int conceptNid) {
            Optional<String> description = this.manifoldCoordinate.getLanguageCoordinate().getPreferredDescriptionText(conceptNid, this.manifoldCoordinate.getLanguageStampFilter());
            if (description.isPresent()) {
                return description.get();
            }
            description = this.manifoldCoordinate.getLanguageCoordinate().getFullyQualifiedNameText(conceptNid, this.manifoldCoordinate.getLanguageStampFilter());
            if (description.isPresent()) {
                return description.get();
            }
            return this.manifoldCoordinate.getLanguageCoordinate().getAnyName(conceptNid, this.manifoldCoordinate.getLanguageStampFilter());
        }

        @Override
        public LatestVersion<DescriptionVersion> getDescriptionOptional(int conceptNid) {
            if (conceptNid >= 0) {
                throw new IndexOutOfBoundsException("Component identifiers must be negative. Found: " + conceptNid);
            }
            LatestVersion<DescriptionVersion> lv = this.manifoldCoordinate.getDescription(conceptNid);
            if (lv.isAbsent()) {
                //Use a coordinate that will return anything
                return regNameCoord.getDescription(Get.assemblageService().getDescriptionsForComponent(conceptNid), this.manifoldCoordinate.getLanguageStampFilter());
            } else {
                return lv;
            }
        }

        @Override
        public ManifoldCoordinate getManifoldCoordinate() {
            return this.manifoldCoordinate;
        }
    }

    @Override
    public <V extends SemanticVersion> SingleAssemblageSnapshot<V> getSingleAssemblageSnapshot(int assemblageConceptNid, Class<V> versionType, StampFilter stampFilter) {
        return (SingleAssemblageSnapshot<V>) new SingleAssemblageSnapshotProvider(assemblageConceptNid, 
                (SemanticSnapshotService<V>) getSnapshot(versionType, stampFilter));
    }

    @Override
    public ConceptSnapshot getConceptSnapshot(int conceptNid, ManifoldCoordinate manifoldCoordinate) {
        return new ConceptSnapshotImpl(getConceptChronology(conceptNid), manifoldCoordinate);
    }
}
