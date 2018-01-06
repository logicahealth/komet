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

//~--- JDK imports ------------------------------------------------------------
import sh.isaac.model.DataStore;
import java.io.InputStream;

import java.nio.file.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

//~--- non-JDK imports --------------------------------------------------------
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.glassfish.hk2.runlevel.RunLevel;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.AssemblageService;
import sh.isaac.api.DatabaseServices;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifiedObjectService;
import sh.isaac.api.MetadataService;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.IntSet;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.commit.CommitService;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptService;
import sh.isaac.api.component.concept.ConceptSnapshot;
import sh.isaac.api.component.concept.ConceptSnapshotService;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.SemanticSnapshotService;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.component.semantic.version.brittle.Rf2Relationship;
import sh.isaac.api.constants.DatabaseInitialization;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.externalizable.BinaryDataReaderService;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.model.ChronologyImpl;
import sh.isaac.model.ContainerSequenceService;
import sh.isaac.model.ModelGet;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.model.concept.ConceptSnapshotImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;

//~--- classes ----------------------------------------------------------------
/**
 *
 * @author kec
 */
@Service
@RunLevel(value = 1)
public class ChronologyProvider
        implements ConceptService, AssemblageService, IdentifiedObjectService, MetadataService {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LogManager.getLogger();

    //~--- fields --------------------------------------------------------------
    private DataStore store;
    private ConcurrentHashMap<Integer, IsaacObjectType> assemblageNid_ObjectType_Map
            = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, VersionType> assemblageNid_VersionType_Map
            = new ConcurrentHashMap<>();

    //~--- methods -------------------------------------------------------------
    @Override
    public boolean importMetadata()
            throws Exception {
        if (this.store.getDatabaseValidityStatus() == DatabaseServices.DatabaseValidity.MISSING_DIRECTORY) {
            Optional<DatabaseInitialization> initializationPreference = Get.applicationPreferences()
                    .getEnum(DatabaseInitialization.class);

            if (initializationPreference.isPresent()) {
                if (initializationPreference.get() == DatabaseInitialization.LOAD_METADATA) {
                    loadMetaData();
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public Future<?> sync() {
        return this.store.sync();
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

        if (semanticChronicle.getVersionType() == VersionType.RF2_RELATIONSHIP) {
            SemanticChronology writtenChronicle = getSemanticChronology(semanticChronicle.getNid());
            List<Version> versionList = writtenChronicle.getVersionList();
            if (versionList.size() > 2) {
                for (int i = 1; i < versionList.size(); i++) {
                    Rf2Relationship rel1 = (Rf2Relationship) versionList.get(i - 1);
                    Rf2Relationship rel2 = (Rf2Relationship) versionList.get(i);
                    if (rel1.getTypeNid() != rel2.getTypeNid()) {
                        LOG.warn("Rel type changed from: \n     " + Get.conceptDescriptionText(rel1.getTypeNid())
                                + "\n to: " + Get.conceptDescriptionText(rel2.getTypeNid()));
                    }
                }
            }
        }
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
    }

    /**
     * Start me.
     */
    @PostConstruct
    private void startMe() {
        LOG.info("Starting chronology provider.");
        store = Get.service(DataStore.class);
        this.assemblageNid_ObjectType_Map = store.getAssemblageObjectTypeMap();
        this.assemblageNid_VersionType_Map = store.getAssemblageVersionTypeMap();
    }

    /**
     * Stop me.
     */
    @PreDestroy
    private void stopMe() {
        LOG.info("Stopping chronology provider.");
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public IsaacObjectType getObjectTypeForAssemblage(int assemblageNid) {
        return assemblageNid_ObjectType_Map.getOrDefault(assemblageNid, IsaacObjectType.UNKNOWN);
    }

    @Override
    public int[] getAssemblageConceptNids() {
        return this.store.getAssemblageConceptNids();
    }

    private Optional<ByteArrayDataBuffer> getChronologyData(int nid) {
        return this.store.getChronologyData(nid);
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
        Optional<ByteArrayDataBuffer> optionalByteBuffer = store.getChronologyData(conceptId);

        if (optionalByteBuffer.isPresent()) {
            ByteArrayDataBuffer byteBuffer = optionalByteBuffer.get();

            IsaacObjectType.CONCEPT.readAndValidateHeader(byteBuffer);
            return ConceptChronologyImpl.make(byteBuffer);
        }

        throw new NoSuchElementException("No element for: " + conceptId);
    }

    @Override
    public ConceptChronology getConceptChronology(UUID... conceptUuids) {
        int nid = Get.identifierService()
                .getNidForUuids(conceptUuids);

        return ChronologyProvider.this.getConceptChronology(nid);
    }

    @Override
    public Stream<ConceptChronology> getConceptChronologyStream() {
        return ModelGet.identifierService()
                .getNidStreamOfType(IsaacObjectType.CONCEPT)
                .mapToObj(
                        (nid) -> {
                            return getConceptChronology(nid);
                        });
    }

    @Override
    public Stream<ConceptChronology> getConceptChronologyStream(int assemblageNid) {
        return Get.identifierService()
                .getNidsForAssemblage(assemblageNid)
                .mapToObj((nid) -> getConceptChronology(nid));
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
                .count();
    }

    @Override
    public int getConceptCount(int assemblageNid) {
        return (int) getConceptNidStream(assemblageNid).parallel()
                .count();
    }

    @Override
    public IntStream getConceptNidStream() {
        return ModelGet.identifierService()
                .getNidStreamOfType(IsaacObjectType.CONCEPT);
    }

    @Override
    public IntStream getConceptNidStream(int assemblageNid) {
        return Get.identifierService()
                .getNidsForAssemblage(assemblageNid);
    }

    @Override
    public UUID getDataStoreId() {
        return store.getDataStoreId();
    }

    @Override
    public Path getDatabaseFolder() {
        return store.getDatabaseFolder();
    }

    @Override
    public DatabaseValidity getDatabaseValidityStatus() {
        return store.getDatabaseValidityStatus();
    }

    @Override
    public List<SemanticChronology> getDescriptionsForComponent(int componentNid) {
        if (componentNid >= 0) {
            throw new IndexOutOfBoundsException("Component identifiers must be negative. Found: " + componentNid);
        }
        ContainerSequenceService identifierService = ModelGet.identifierService();

        List<SemanticChronology> results = new ArrayList<>();
        int[] semanticNids = getSemanticNidsForComponent(componentNid).asArray();
        for (int semanticNid : semanticNids) {
            int assemblageNid = identifierService.getAssemblageNidForNid(semanticNid);
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
    public Optional<? extends Chronology> getIdentifiedObjectChronology(int nid) {
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
        OptionalInt optionalAssemblageNid = Get.identifierService()
                .getAssemblageNid(conceptNid);

        if (optionalAssemblageNid.isPresent()) {
            int assemblageNid = optionalAssemblageNid.getAsInt();

            return Optional.of(getConceptChronology(conceptNid));
        }

        return Optional.empty();
    }

    @Override
    public Optional<? extends ConceptChronology> getOptionalConcept(UUID... conceptUuids) {
        int nid = Get.identifierService()
                .getNidForUuids(conceptUuids);
        OptionalInt optionalAssemblageNid = Get.identifierService()
                .getAssemblageNid(nid);

        if (optionalAssemblageNid.isPresent()) {
            int assemblageNid = optionalAssemblageNid.getAsInt();

            return Optional.of(getConceptChronology(nid));
        }

        return Optional.empty();
    }

    @Override
    public Optional<? extends SemanticChronology> getOptionalSemanticChronology(int semanticNid) {
        if (Get.identifierService()
                .getAssemblageNid(semanticNid)
                .isPresent()) {
            return Optional.of(getSemanticChronology(semanticNid));
        }

        return Optional.empty();
    }

    @Override
    public SemanticChronology getSemanticChronology(int semanticId) {
        Optional<ByteArrayDataBuffer> optionalByteBuffer = store.getChronologyData(semanticId);

        if (optionalByteBuffer.isPresent()) {
            ByteArrayDataBuffer byteBuffer = optionalByteBuffer.get();

            IsaacObjectType.SEMANTIC.readAndValidateHeader(byteBuffer);
            return SemanticChronologyImpl.make(byteBuffer);
        }

        // Gather exception data...
        List<UUID> uuids = Get.identifierService()
                .getUuidsForNid(semanticId);
        OptionalInt assemblageNidOptional = ModelGet.identifierService()
                .getAssemblageNid(semanticId);
        String assemblage = "unknown assemblage";

        if (assemblageNidOptional.isPresent()) {
            assemblage = Get.conceptDescriptionText(assemblageNidOptional.getAsInt());
        }

        throw new NoSuchElementException("No element for: " + semanticId + " " + uuids + " in " + assemblage);
    }

    @Override
    public Stream<SemanticChronology> getSemanticChronologyStream() {
        return getSemanticNidStream().mapToObj(
                (value) -> {
                    return getSemanticChronology(value);
                });
    }

    @Override
    public <C extends SemanticChronology> Stream<C> getSemanticChronologyStreamForComponent(int componentNid) {
        return getSemanticNidsForComponent(componentNid).stream()
                .mapToObj((int sememeSequence) -> (C) getSemanticChronology(sememeSequence));
    }

    @Override
    public <C extends SemanticChronology> Stream<C> getSemanticChronologyStreamForComponentFromAssemblage(
            int componentNid,
            int assemblageConceptSequence) {
        if (componentNid >= 0) {
            throw new UnsupportedOperationException("Can't substitute a sequence for a nid: " + componentNid);
        }

        final NidSet semanticNids = getSemanticNidsForComponentFromAssemblage(componentNid, assemblageConceptSequence);

        return semanticNids.stream()
                .mapToObj((int nid) -> (C) getSemanticChronology(nid));
    }

    @Override
    public <C extends SemanticChronology> Stream<C> getSemanticChronologyStream(int assemblageConceptNid) {
        switch (getObjectTypeForAssemblage(assemblageConceptNid)) {
            case SEMANTIC:
                final NidSet semanticSequences = getSemanticNidsFromAssemblage(assemblageConceptNid);

                return semanticSequences.stream()
                        .mapToObj((int semanticSequence) -> (C) getSemanticChronology(semanticSequence));
            case UNKNOWN:
                // perhaps not initialized...
                final NidSet elementSequences = getSemanticNidsFromAssemblage(assemblageConceptNid);
                return (Stream<C>) elementSequences.stream().mapToObj((nid) -> getIdentifiedObjectChronology(nid))
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
                return (Stream<C>) elementSequences.stream().mapToObj((nid) -> getIdentifiedObjectChronology(nid))
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
                .getNidStreamOfType(IsaacObjectType.SEMANTIC);
    }

    @Override
    public IntStream getSemanticNidStream(int assemblageNid) {
        return ModelGet.identifierService()
                .getNidsForAssemblage(assemblageNid);
    }

    @Override
    public NidSet getSemanticNidsForComponent(int componentNid) {
        int[] semanticNids = store.getComponentToSemanticNidsMap()
                .get(componentNid);

        return NidSet.of(semanticNids);
    }

    @Override
    public NidSet getSemanticNidsForComponentFromAssemblage(int componentNid, int assemblageNid) {
        if (componentNid >= 0) {
            throw new IndexOutOfBoundsException("Component identifiers must be negative. Found: " + componentNid);
        }

        if (assemblageNid >= 0) {
            throw new IndexOutOfBoundsException("Component identifiers must be negative. Found: " + componentNid);
        }

        ContainerSequenceService identifierService = ModelGet.identifierService();
        NidSet semanticNids = new NidSet();

        for (int semanticNid : store.getComponentToSemanticNidsMap()
                .get(componentNid)) {
            if (identifierService.getAssemblageNidForNid(semanticNid) == assemblageNid) {
                semanticNids.add(semanticNid);
            }
        }

        return semanticNids;
    }

    @Override
    public NidSet getSemanticNidsFromAssemblage(int assemblageNid) {
        return NidSet.of(ModelGet.identifierService()
                .getNidsForAssemblage(assemblageNid));
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
        VersionType versionType = assemblageNid_VersionType_Map.get(assemblageNid);
        if (versionType != null && versionType != VersionType.UNKNOWN) {
            return versionType;
        }

        IsaacObjectType objectType = getObjectTypeForAssemblage(assemblageNid);
        switch (objectType) {
            case CONCEPT:
                assemblageNid_VersionType_Map.put(assemblageNid, VersionType.CONCEPT);
                return VersionType.CONCEPT;
            default:
            // fall through. 
        }
        Optional<SemanticChronology> semanticChronologyOptional = getSemanticChronologyStream(assemblageNid).findFirst();
        if (semanticChronologyOptional.isPresent()) {
            assemblageNid_VersionType_Map.put(assemblageNid, semanticChronologyOptional.get().getVersionType());
            return semanticChronologyOptional.get().getVersionType();
        }
        return VersionType.UNKNOWN;
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
         * @param conceptSequence the concept sequence
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
         * @param conceptSequence the concept sequence
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
