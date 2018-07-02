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

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifiedObjectService;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.LookupService;
import sh.isaac.api.MetadataService;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.IntSet;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.commit.CommitService;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptService;
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
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.datastore.DataStore;
import sh.isaac.api.externalizable.BinaryDataReaderService;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.model.ChronologyImpl;
import sh.isaac.model.ModelGet;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.model.concept.ConceptSnapshotImpl;
import sh.isaac.model.configuration.EditCoordinates;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.model.semantic.SemanticChronologyImpl;

//~--- classes ----------------------------------------------------------------
/**
 *
 * @author kec
 */
@Service
@RunLevel(value = LookupService.SL_L2)
public class ChronologyProvider
        implements ConceptService, AssemblageService, IdentifiedObjectService, MetadataService {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LogManager.getLogger();

    //~--- fields --------------------------------------------------------------
    private DataStore store;
    
   //set to -1, when we haven't loaded yet.  Set to 1, when we have (and did) load metadata.  Set to 0, when we have checked, 
   //but didn't load metadata because the database was already loaded, or the preferences said not to.
   private AtomicInteger metadataLoaded = new AtomicInteger(-1);

    //~--- methods -------------------------------------------------------------
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
        Get.conceptActiveService()
                .updateStatus(concept);
        store.putChronologyData((ChronologyImpl) concept);
    }

    @Override
    public void writeSemanticChronology(SemanticChronology semanticChronicle) {
        store.putChronologyData((ChronologyImpl) semanticChronicle);
    }

    private void loadMetaData()
            throws Exception {
        InputStream dataStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream("sh/isaac/IsaacMetadataAuxiliary.ibdf");
        final BinaryDataReaderService reader = Get.binaryDataReader(dataStream);
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
        
      //Store the DB id as a semantic
      Get.semanticBuilderService()
            .getStringSemanticBuilder(getDataStoreId().get().toString(), TermAux.SOLOR_ROOT.getNid(), TermAux.DATABASE_UUID.getNid())
            .build(EditCoordinates.getDefaultUserMetadata(), ChangeCheckerMode.ACTIVE).get();
      Get.commitService().commit(EditCoordinates.getDefaultUserMetadata(), "Storing database ID on root concept").get();
    }

    /**
     * Start me.
     */
    @PostConstruct
    private void startMe() {
        LOG.info("Starting chronology provider for change to runlevel: " + LookupService.getProceedingToRunLevel());
        this.metadataLoaded.set(-1);
        store = Get.service(DataStore.class);
        if (store == null) {
            throw new RuntimeException("Failed to get a data store!");
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
    public boolean isConceptActive(int conceptSequence, StampCoordinate stampCoordinate) {
        return Get.conceptActiveService()
                .isConceptActive(conceptSequence, stampCoordinate);
    }

    @Override
    public ConceptChronology getConceptChronology(ConceptSpecification conceptSpecification) {
        return getConceptChronology(conceptSpecification.getNid());
    }

    @Override
    public ConceptChronologyImpl getConceptChronology(int conceptId) {
        Optional<ByteArrayDataBuffer> optionalByteBuffer = store.getChronologyVersionData(conceptId);

        if (optionalByteBuffer.isPresent()) {
            ByteArrayDataBuffer byteBuffer = optionalByteBuffer.get();

            IsaacObjectType.CONCEPT.readAndValidateHeader(byteBuffer);
            return ConceptChronologyImpl.make(byteBuffer);
        }

        throw new NoSuchElementException("No element for: " + conceptId + Arrays.toString(Get.identifierService().getUuidsForNid(conceptId).toArray()));
    }

    @Override
    public ConceptChronology getConceptChronology(UUID... conceptUuids) {
        int nid = Get.identifierService()
                .getNidForUuids(conceptUuids);

        return ChronologyProvider.this.getConceptChronology(nid);
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
        return conceptNids.stream()
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
          LatestVersion<Version> sdi = sdic.get().getLatestVersion(StampCoordinates.getDevelopmentLatest());
          if (sdi.isPresent()) {
             try {
                UUID temp = UUID.fromString(((StringVersion) sdi.get()).getString());
                
                if (!temp.equals(fromFile)) {
                   LOG.info("Semantic Store has {} while file store has {}.  This is expected, if an IBDF file with an existing ID was merged into a datastore."
                         + "  Updating the semantic store to match the file store id.", temp, fromFile);
                   
                   MutableStringVersion sv = sdic.get().createMutableVersion(Status.ACTIVE, EditCoordinates.getDefaultUserMetadata());
                   sv.setString(fromFile.toString());
                   Get.commitService().addUncommitted(sdic.get());
                   Get.commitService().commit(EditCoordinates.getDefaultUserMetadata(), "Updating database ID on root concept").get();
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
        int[] semanticNids = getSemanticNidsForComponent(componentNid).asArray();
        for (int semanticNid : semanticNids) {
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
        }
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
          return Optional.of(getSemanticChronology(semanticNid));
       }
       return Optional.empty();
    }

    @Override
    public SemanticChronology getSemanticChronology(int semanticId) {
        Optional<ByteArrayDataBuffer> optionalByteBuffer = store.getChronologyVersionData(semanticId);

        if (optionalByteBuffer.isPresent()) {
            ByteArrayDataBuffer byteBuffer = optionalByteBuffer.get();

            IsaacObjectType.SEMANTIC.readAndValidateHeader(byteBuffer);
            return SemanticChronologyImpl.make(byteBuffer);
        }

        // Gather exception data...
        List<UUID> uuids = Get.identifierService().getUuidsForNid(semanticId);
        String assemblage = Get.conceptDescriptionText(ModelGet.identifierService().getAssemblageNid(semanticId).getAsInt());

        throw new NoSuchElementException("No element for: " + semanticId + " " + uuids + " in " + assemblage);
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
        return getSemanticNidsForComponent(componentNid).stream()
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
       final NidSet semanticSequences = getSemanticNidsForComponentFromAssemblages(componentNid, assemblageConceptNids);

       return semanticSequences.stream().mapToObj((int semanticNid) -> {
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
                final NidSet semanticSequences = getSemanticNidsFromAssemblage(assemblageConceptNid);

                return semanticSequences.stream()
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
                final NidSet elementSequences = getSemanticNidsFromAssemblage(assemblageConceptNid);
                return (Stream<C>) elementSequences.stream().mapToObj((nid) -> getChronology(nid))
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
                final NidSet elementSequences = getSemanticNidsFromAssemblage(assemblageConceptNid);
                return (Stream<C>) elementSequences.stream().mapToObj((nid) -> getChronology(nid))
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
                // TODO is this filter really necessary? It has significant performance overhead.
                .filter(nid -> hasSemantic(nid))
                .count();
    }

    @Override
    public int getSemanticCount(int assemblageNid) {
        return (int) ModelGet.identifierService()
                .getNidsForAssemblage(assemblageNid)
                .filter(nid -> hasSemantic(nid))
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
    public NidSet getSemanticNidsForComponent(int componentNid) {
        int[] semanticNids = store.getSemanticNidsForComponent(componentNid);

        return NidSet.of(semanticNids);
    }

    @Override
    public NidSet getSemanticNidsForComponentFromAssemblage(int componentNid, int assemblageNid) {
       return getSemanticNidsForComponentFromAssemblages(componentNid, Collections.singleton(assemblageNid));
    }
    
    @Override
    public NidSet getSemanticNidsForComponentFromAssemblages(int componentNid, Set<Integer> assemblageConceptNids) {
       if (componentNid >= 0) {
          throw new IndexOutOfBoundsException("Component identifiers must be negative. Found: " + componentNid);
       }
       if (assemblageConceptNids == null || assemblageConceptNids.isEmpty())
       {
          return getSemanticNidsForComponent(componentNid);
       }
       
       for (int assemblageNid : assemblageConceptNids) {
          if (assemblageNid >= 0) {
             throw new IndexOutOfBoundsException("Assemblage identifiers must be negative. Found: " + componentNid);
          }
       }
       IdentifierService identifierService = ModelGet.identifierService();
       NidSet semanticNids = new NidSet();
       for (int semanticNid: store.getSemanticNidsForComponent(componentNid)) {
          if (assemblageConceptNids.contains(identifierService.getAssemblageNid(semanticNid).getAsInt())) {
             semanticNids.add(semanticNid);
          }
       }
       return semanticNids;
    }

    @Override
    public NidSet getSemanticNidsFromAssemblage(int assemblageNid) {
        return NidSet.of(ModelGet.identifierService()
                .getNidsForAssemblage(assemblageNid)
                .filter(nid -> hasSemantic(nid)));
    }

    @Override
    public ConceptSnapshotService getSnapshot(ManifoldCoordinate manifoldCoordinate) {
        return new ConceptSnapshotProvider(manifoldCoordinate);
    }

    @Override
    public <V extends SemanticVersion> SemanticSnapshotService<V> getSnapshot(Class<V> versionType,
            StampCoordinate stampCoordinate) {
        return new AssemblageSnapshotProvider<>(versionType, stampCoordinate, this);
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
    public class ConceptSnapshotProvider
            implements ConceptSnapshotService {

        /**
         * The manifold coordinate.
         */
        ManifoldCoordinate manifoldCoordinate;

        //~--- constructors -----------------------------------------------------
        /**
         * Instantiates a new concept snapshot provider.
         *
         * @param manifoldCoordinate
         */
        public ConceptSnapshotProvider(ManifoldCoordinate manifoldCoordinate) {
            this.manifoldCoordinate = manifoldCoordinate;
        }

        //~--- methods ----------------------------------------------------------
        /**
         * Concept description text.
         *
         * @param conceptId the concept id
         * @return the string
         */
        @Override
        public String conceptDescriptionText(int conceptId) {
            final LatestVersion<DescriptionVersion> descriptionOptional = getDescriptionOptional(conceptId);

            if (descriptionOptional.isPresent()) {
                return descriptionOptional.get()
                        .getText();
            }

            return "No desc for: " + conceptId;
        }

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            return "ConceptSnapshotProvider{" + "manifoldCoordinate=" + this.manifoldCoordinate + '}';
        }

        //~--- get methods ------------------------------------------------------
        /**
         * Checks if concept active.
         *
         * @param conceptNid the concept sequence
         * @return true, if concept active
         */
        @Override
        public boolean isConceptActive(int conceptSequence) {
            return ChronologyProvider.this.isConceptActive(conceptSequence, this.manifoldCoordinate);
        }

        @Override
        public ConceptSnapshot getConceptSnapshot(ConceptSpecification conceptSpecification) {
            return getConceptSnapshot(conceptSpecification.getNid());
        }

        /**
         * Gets the concept snapshot.
         *
         * @param conceptNid the concept sequence
         * @return the concept snapshot
         */
        @Override
        public ConceptSnapshot getConceptSnapshot(int conceptSequence) {
            return new ConceptSnapshotImpl(getConceptChronology(conceptSequence), this.manifoldCoordinate);
        }

        /**
         * Gets the description list.
         *
         * @param conceptId the concept id
         * @return the description list
         */
        private List<SemanticChronology> getDescriptionList(int conceptNid) {
            if (conceptNid >= 0) {
                throw new IndexOutOfBoundsException("Component identifiers must be negative. Found: " + conceptNid);
            }

            return Get.assemblageService()
                    .getDescriptionsForComponent(conceptNid);
        }

        /**
         * Gets the description optional.
         *
         * @param conceptId the concept id
         * @return the description optional
         */
        @Override
        public LatestVersion<DescriptionVersion> getDescriptionOptional(int conceptId) {
            if (conceptId >= 0) {
                throw new IndexOutOfBoundsException("Component identifiers must be negative. Found: " + conceptId);
            }

            return this.manifoldCoordinate.getDescription(getDescriptionList(conceptId));
        }

        /**
         * Gets the fully specified description.
         *
         * @param conceptId the concept id
         * @return the fully specified description
         */
        @Override
        public LatestVersion<DescriptionVersion> getFullySpecifiedDescription(int conceptId) {
            return this.manifoldCoordinate.getFullySpecifiedDescription(getDescriptionList(conceptId));
        }

        /**
         * Gets the stamp coordinate.
         *
         * @return the stamp coordinate
         */
        @Override
        public ManifoldCoordinate getManifoldCoordinate() {
            return this.manifoldCoordinate;
        }

        /**
         * Gets the preferred description.
         *
         * @param conceptId the concept id
         * @return the preferred description
         */
        @Override
        public LatestVersion<DescriptionVersion> getPreferredDescription(int conceptId) {
            return this.manifoldCoordinate.getPreferredDescription(getDescriptionList(conceptId));
        }

    }
}
