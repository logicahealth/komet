package org.ihtsdo.otf.tcc.api.store;

import gov.vha.isaac.ochre.collections.ConceptSequenceSet;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import javafx.concurrent.Task;
import org.ihtsdo.otf.tcc.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.otf.tcc.api.changeset.ChangeSetGeneratorBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.otf.tcc.api.coordinate.Path;
import org.ihtsdo.otf.tcc.api.coordinate.Position;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.db.DbDependency;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.jvnet.hk2.annotations.Contract;

@Contract
public interface TerminologyDI {

    public static enum CONCEPT_EVENT {

        PRE_COMMIT, POST_COMMIT, ADD_UNCOMMITTED
    }

    //~--- methods -------------------------------------------------------------
    void addChangeSetGenerator(String key, ChangeSetGeneratorBI writer);

    void addUncommitted(ConceptChronicleBI cc) throws IOException;

    void addUncommitted(ConceptVersionBI cv) throws IOException;

    void addUncommittedNoChecks(ConceptChronicleBI cc) throws IOException;

    void addUncommittedNoChecks(ConceptVersionBI cv) throws IOException;

    void cancel() throws IOException;

    void cancel(ConceptChronicleBI cc) throws IOException;

    void cancel(ConceptVersionBI cv) throws IOException;

    void commit() throws IOException;

    void commit(ConceptChronicleBI cc) throws IOException;

    void commit(ConceptVersionBI cv) throws IOException;

    ChangeSetGeneratorBI createDtoChangeSetGenerator(File changeSetFileName, File changeSetTempFileName,
            ChangeSetGenerationPolicy policy);

    boolean forget(ConceptAttributeVersionBI attr) throws IOException;

    void forget(ConceptChronicleBI concept) throws IOException;

    void forget(DescriptionVersionBI desc) throws IOException;

    void forget(RefexChronicleBI extension) throws IOException;
    
    void forget(RefexDynamicChronicleBI extension) throws IOException;

    void forget(RelationshipVersionBI rel) throws IOException;

    /**
     * Cause all index generators implementing the {@code IndexerBI} to
     * first {@code clearIndex()} then iterate over all chronicles in the
     * database and pass those chronicles to
     * {@code index(ComponentChronicleBI chronicle)} and when complete, to
     * call {@code commitWriter()}. {@code IndexerBI} services will be
     * discovered using the HK2 dependency injection framework.
     * @param indexesToRebuild - if null or empty - all indexes found via HK2 will be cleared and
     * reindexed.  Otherwise, only clear and reindex the instances of IndexerBI which match the specified
     * class list.  Classes passed in should be an extension of IndexerBI (but I don't have the type here to 
     * be able to enforce that)
     * 
     * Note that this runs in a background thread - and hands back a task handle.  To wait for completion, 
     * call get() on the returned task.
     * 
     * @throws IOException
     */
    Task<?> index(Class<?> ... indexesToRebuild);

    @Deprecated
    void iterateConceptDataInParallel(ProcessUnfetchedConceptDataBI processor) throws Exception;

    @Deprecated
    void iterateConceptDataInSequence(ProcessUnfetchedConceptDataBI processor) throws Exception;

    Stream<? extends ConceptChronicleBI> getConceptStream() throws IOException;
    Stream<? extends ConceptChronicleBI> getConceptStream(ConceptSequenceSet conceptSequences) throws IOException;
    
    Stream<? extends ConceptChronicleBI> getParallelConceptStream() throws IOException;
    Stream<? extends ConceptChronicleBI> getParallelConceptStream(ConceptSequenceSet conceptSequences) throws IOException;

    /**
     *
     * @param econFiles the files to load
     * @return the number of concepts loaded
     * @throws Exception
     */
    int loadEconFiles(File... econFiles) throws Exception;

    /**
     *
     * @param econFiles the files to load
     * @return the number of concepts loaded
     * @throws Exception
     */
    int loadEconFiles(java.nio.file.Path... econFiles) throws Exception;

    /**
     *
     * @param econFileStrings the files to load
     * @return the number of concepts loaded
     * @throws Exception
     */
    int loadEconFiles(String... econFileStrings) throws Exception;

    Position newPosition(Path path, long time) throws IOException;

    void removeChangeSetGenerator(String key);

    boolean satisfiesDependencies(Collection<DbDependency> dependencies);

    //~--- get methods ---------------------------------------------------------
    NativeIdSetBI getAllConceptNids() throws IOException;

    NativeIdSetBI getAllConceptNidsFromCache() throws IOException;

    NativeIdSetBI getAllComponentNids() throws IOException;

    NativeIdSetBI getConceptNidsForComponentNids(NativeIdSetBI componentNativeIds) throws IOException;

    /**
     * Retrieves the components nids from the input concept nids
     *
     * @param conceptNativeIds the
     * {@link org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI} for which the components
     * nids will be retrieved
     * @return 
     * @throws java.io.IOException
     */
    NativeIdSetBI getComponentNidsForConceptNids(NativeIdSetBI conceptNativeIds) throws IOException;

    /**
     * Nids that are not claimed by one of the provided concept nids.
     *
     * @param conceptNativeIds the set of native concept nids.
     * @return the orphan set.
     * @throws IOException
     */
    NativeIdSetBI getOrphanNids(NativeIdSetBI conceptNativeIds) throws IOException;

    int getAuthorNidForStamp(int stamp);

    NativeIdSetBI getEmptyNidSet() throws IOException;

    ViewCoordinate getMetadataVC() throws IOException;

    int getModuleNidForStamp(int stamp);

    Path getPath(int pathNid) throws IOException;

    int getPathNidForStamp(int stamp);

    Set<Path> getPathSetFromPositionSet(Set<Position> positions) throws IOException;

    Set<Path> getPathSetFromStampSet(Set<Integer> stamp) throws IOException;

    Set<Position> getPositionSet(Set<Integer> stamp) throws IOException;

    Status getStatusForStamp(int stamp);

    long getTimeForStamp(int sapNid);

    int getNidForUuids(Collection<UUID> uuids) throws IOException;

    Collection<UUID> getUuidCollection(Collection<Integer> nids) throws IOException;

    Collection<Integer> getNidCollection(Collection<UUID> uuids) throws IOException;

    int getNidForUuids(UUID... uuids);

    /**
     * Retrieve the concept nid from a specified nid.  Note that for backwards compatibility, implementations of this method
     * should function correctly if the passed in nid is a concept nid, or if it is a nid that is a member of said concept.
     * 
     * It should not be assumed that the passed nid is a component nid.
     */
    int getConceptNidForNid(int nid);

    int getNidFromAlternateId(UUID authorityUuid, String altId) throws IOException;

    CharSequence informAboutUuid(UUID uuid);

    CharSequence informAboutNid(int nid);

    CharSequence informAboutId(Object id);
}
