package org.ihtsdo.otf.tcc.api.store;

import org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.otf.tcc.api.coordinate.Position;
import org.ihtsdo.otf.tcc.api.coordinate.Path;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.otf.tcc.api.changeset.ChangeSetGeneratorBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.api.db.DbDependency;

public interface TerminologyDI {

    public static enum CONCEPT_EVENT {

        PRE_COMMIT, POST_COMMIT, ADD_UNCOMMITTED
    }

    //~--- methods -------------------------------------------------------------
    void addChangeSetGenerator(String key, ChangeSetGeneratorBI writer);

    void addPropertyChangeListener(TerminologyStoreDI.CONCEPT_EVENT pce, PropertyChangeListener l);

    void addTermChangeListener(TermChangeListener cl);

    void addUncommitted(ConceptChronicleBI cc) throws IOException;

    void addUncommitted(ConceptVersionBI cv) throws IOException;

    void addUncommittedNoChecks(ConceptChronicleBI cc) throws IOException;

    void addUncommittedNoChecks(ConceptVersionBI cv) throws IOException;

    /**
     * Only CONCEPT_EVENT.PRE_COMMIT is a vetoable change
     *
     * @param pce
     * @param l
     */
    void addVetoablePropertyChangeListener(TerminologyStoreDI.CONCEPT_EVENT pce, VetoableChangeListener l);

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

    void forget(RelationshipVersionBI rel) throws IOException;
    
    /**
     * Cause all index generators implementing the <code>IndexerBI</code> to first
     * <code>clearIndex()</code> then iterate over all chronicles in the database
     * and pass those chronicles to <code>index(ComponentChronicleBI chronicle)</code>
     * and when complete, to call <code>commitWriter()</code>. <code>IndexerBI</code> services
     * will be discovered using the HK2 dependency injection framework. 
     * @throws IOException 
     */
    void index() throws IOException;

    void iterateConceptDataInParallel(ProcessUnfetchedConceptDataBI processor) throws Exception;

    void iterateConceptDataInSequence(ProcessUnfetchedConceptDataBI processor) throws Exception;

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

    void removeTermChangeListener(TermChangeListener cl);

    void resumeChangeNotifications();

    boolean satisfiesDependencies(Collection<DbDependency> dependencies);

    void suspendChangeNotifications();

    //~--- get methods ---------------------------------------------------------
    NativeIdSetBI getAllConceptNids() throws IOException;
    
    NativeIdSetBI getAllConceptNidsFromCache() throws IOException;
    
    NativeIdSetBI getAllComponentNids() throws IOException;

    NativeIdSetBI getConceptNidsForComponentNids(NativeIdSetBI componentNativeIds) throws IOException;

    /**
     * Retrieves the components nids from the input concept nids
     *
     * @param conceptNativeIds the <code>NativeIdSetBI<code> for which the components nids will be retrieved
     */
    NativeIdSetBI getComponentNidsForConceptNids(NativeIdSetBI conceptNativeIds) throws IOException;
   /**
    * Nids that are not claimed by one of the provided concept nids. 
    * @param conceptNativeIds the set of native concept nids. 
    * @return the orphan set. 
    * @throws IOException 
    */
    NativeIdSetBI getOrphanNids(NativeIdSetBI conceptNativeIds) throws IOException;
    
    int getAuthorNidForStamp(int sapNid);

    NativeIdSetBI getEmptyNidSet() throws IOException;

    ViewCoordinate getMetadataVC() throws IOException;

    int getModuleNidForStamp(int sapNid);

    Path getPath(int pathNid) throws IOException;

    int getPathNidForStamp(int sapNid);

    Set<Path> getPathSetFromPositionSet(Set<Position> positions) throws IOException;

    Set<Path> getPathSetFromSapSet(Set<Integer> sapNids) throws IOException;

    Set<Position> getPositionSet(Set<Integer> sapNids) throws IOException;

    Status getStatusForStamp(int stamp);

    long getTimeForStamp(int sapNid);

    int getNidForUuids(Collection<UUID> uuids) throws IOException;

    Collection<UUID> getUuidCollection(Collection<Integer> nids) throws IOException;

    Collection<Integer> getNidCollection(Collection<UUID> uuids) throws IOException;

    int getNidForUuids(UUID... uuids) throws IOException;

    /**
     * Retrieve the concept nid from a specified nid.
     *
     * @param nid
     * @return the <code>int</code> concept nid of the specified nid
     */
    int getConceptNidForNid(int nid);

    int getNidFromAlternateId(UUID authorityUuid, String altId) throws IOException;

    CharSequence informAboutUuid(UUID uuid);

    CharSequence informAboutNid(int nid);

    CharSequence informAboutId(Object id);
}
