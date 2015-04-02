package org.ihtsdo.otf.tcc.api.store;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.otf.tcc.api.blueprint.TerminologyBuilderBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentContainerBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptContainerBI;
import org.ihtsdo.otf.tcc.api.coordinate.Path;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.db.DbDependency;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.coordinate.ExternalStampBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.jvnet.hk2.annotations.Contract;

@Contract
public interface TerminologyStoreDI extends TerminologyDI {

    boolean isConceptNid(int nid);

    /**
     * Retrieves the components nids that are children of the input parent nid
     *
     * @author dylangrald
     * @param parentNid the {@code int} nid of the parent component
     * @param viewCoordinate the desired {@code ViewCoordinate}
     * @return the {@code NativeIdSetBI} of children of the parent nid
     */
    public NativeIdSetBI isChildOfSet(int parentNid, ViewCoordinate viewCoordinate);

    public NativeIdSetBI relationshipSet(int parentNid, ViewCoordinate viewCoordinate);

//    public Collection<Integer> searchLucene(String luceneMatch, SearchType searchType) throws IOException, ParseException;

/*    public Collection<Integer> searchLuceneRefset(String query, SearchType searchType)
            throws IOException, ParseException;*/
    enum DatabaseOptionPreferences {

        DB_LOCATION, BASELINE_FILES;
    }

    ComponentChronicleBI<?> getComponent(Collection<UUID> uuids) throws IOException;

    ComponentChronicleBI<?> getComponent(ComponentContainerBI cc) throws IOException;

    ComponentChronicleBI<?> getComponent(int nid) throws IOException;

    ComponentChronicleBI<?> getComponent(UUID... uuids) throws IOException;

    /**
     * Maybe should only be accessible from component? Or {@code PersistentStoreI}
     * @param refexNid
     * @return 
     */
    RefexChronicleBI<?> getRefex(int refex);

    /**
     * Maybe should only be accessible from component? Or {@code PersistentStoreI}
     * @param assemblageNid
     * @return 
     */
    Collection<? extends RefexChronicleBI<?>> getRefexesForAssemblage(int assemblageNid);

    /**
     * Maybe should only be accessible from component? Or {@code PersistentStoreI}
     * @param componentNid
     * @return 
     */
    public Collection<? extends RefexChronicleBI<?>> getRefexesForComponent(int componentNid);
    /**
     * 
     * @param authorityNid
     * @param altId
     * @return
     * @throws IOException
     * @deprecated use query service for alternative ids. 
     */
    @Deprecated
    ComponentChronicleBI<?> getComponentFromAlternateId(int authorityNid, String altId) throws IOException;

    ComponentVersionBI getComponentVersion(ViewCoordinate vc, Collection<UUID> uuids)
            throws IOException, ContradictionException;

    ComponentVersionBI getComponentVersion(ViewCoordinate vc, int nid)
            throws IOException, ContradictionException;

    ComponentVersionBI getComponentVersion(ViewCoordinate vc, UUID... uuids)
            throws IOException, ContradictionException;

    /**
     * @deprecated use query service for alternative ids. 
     */
    @Deprecated
    ComponentVersionBI getComponentVersionFromAlternateId(ViewCoordinate vc, int authorityNid, String altId)
            throws IOException, ContradictionException;

    /**
     * @deprecated use query service for alternative ids. 
     */
    @Deprecated
    ComponentVersionBI getComponentVersionFromAlternateId(ViewCoordinate vc, UUID authorityUuid, String altId)
            throws IOException, ContradictionException;

    ConceptChronicleBI getConcept(Collection<UUID> uuids) throws IOException;

    ConceptChronicleBI getConcept(ConceptContainerBI cc) throws IOException;

    ConceptChronicleBI getConcept(int cNid) throws IOException;

    ConceptChronicleBI getConcept(UUID... uuids) throws IOException;
    /**
     * @deprecated use query service for alternative ids. 
     */
    @Deprecated
    ConceptChronicleBI getConceptFromAlternateId(int authorityNid, String altId) throws IOException;

    /**
     * @deprecated use query service for alternative ids. 
     */
    @Deprecated
    ConceptChronicleBI getConceptFromAlternateId(UUID authorityUuid, String altId) throws IOException;

    /**
     * Retrieves the
     * {@code ConceptChronicleBI} from the specified component nid.
     *
     * @param nid
     * @return  {@code ConceptChronicleBI} associated with the specified
     * component nid.
     * @throws IOException
     */
    ConceptChronicleBI getConceptForNid(int nid) throws IOException;

    ConceptVersionBI getConceptVersion(ViewCoordinate vc, Collection<UUID> uuids) throws IOException;

    ConceptVersionBI getConceptVersion(ViewCoordinate vc, int cNid) throws IOException;

    ConceptVersionBI getConceptVersion(ViewCoordinate vc, UUID... uuids) throws IOException;

    /**
     * @deprecated use query service for alternative ids. 
     */
    @Deprecated
    ConceptVersionBI getConceptVersionFromAlternateId(ViewCoordinate vc, int authorityNid, String altId) throws IOException;

    /**
     * @deprecated use query service for alternative ids. 
     */
    @Deprecated
    ConceptVersionBI getConceptVersionFromAlternateId(ViewCoordinate vc, UUID authorityUuid, String altId) throws IOException;

    Map<Integer, ConceptVersionBI> getConceptVersions(ViewCoordinate vc, NativeIdSetBI cNids) throws IOException;

    Map<Integer, ConceptChronicleBI> getConcepts(NativeIdSetBI cNids) throws IOException;

    Collection<DbDependency> getLatestChangeSetDependencies() throws IOException;

    List<? extends Path> getPathChildren(int nid);

    /**
     * Retrieves an array of concept nids that are children of the specified
     * component nid.
     *
     * @param cNid
     * @param vc
     * @return concept nids that are children of input nid,      * stored {@code int[]}
     * @throws IOException
     */
    int[] getPossibleChildren(int cNid, ViewCoordinate vc) throws IOException;

    TerminologySnapshotDI getSnapshot(ViewCoordinate vc);

    TerminologySnapshotDI cacheSnapshot(UUID snapshotUuid, ViewCoordinate vc);

    TerminologySnapshotDI getCachedSnapshot(UUID snapshotUuid) throws NoSuchElementException;

    TerminologySnapshotDI getGlobalSnapshot();

    void setGlobalSnapshot(TerminologySnapshotDI globalSnapshot);

    TerminologyBuilderBI getTerminologyBuilder(EditCoordinate ec, ViewCoordinate vc);

    Collection<? extends ConceptChronicleBI> getUncommittedConcepts();

    /**
     * @return the primordial UUID if known. The IUnknown UUID
     * (00000000-0000-0000-C000-000000000046) if not known.
     */
    UUID getUuidPrimordialForNid(int nid) throws IOException;

    List<UUID> getUuidsForNid(int nid) throws IOException;

    boolean hasPath(int nid) throws IOException;

    boolean hasUncommittedChanges();

    boolean hasUuid(UUID memberUUID);

    boolean hasUuid(List<UUID> memberUUIDs);

    long getSequence();

    int getConceptCount() throws IOException;

    int getStamp(ExternalStampBI version) throws IOException;

    ViewCoordinate getViewCoordinate(UUID vcUuid) throws IOException;

    Collection<ViewCoordinate> getViewCoordinates() throws IOException;

    void putViewCoordinate(ViewCoordinate vc) throws IOException;

    /**
     * Calculates whether childNid is a kind of parentNid
     *
     * @param parentNid the {@code int} nid of the parent concept
     * @param childNid the {@code int} nid of the child concept
     * @param vc the desired {@code ViewCoordinate}
     * @return the {@code boolean} value of whether childNid is a kind of
     * parentNid
     */
    boolean isKindOf(int childNid, int parentNid, ViewCoordinate vc)
            throws IOException, ContradictionException;

    /**
     * Calculates whether childNid is a child of parentNid
     *
     * @param parentNid the {@code int} nid of the parent concept
     * @param childNid the {@code int} nid of the child concept
     * @param vc the desired {@code ViewCoordinate}
     * @return the {@code boolean} value of whether childNid is a child of
     * parentNid
     */
    boolean isChildOf(int childNid, int parentNid, ViewCoordinate vc)
            throws IOException, ContradictionException;

    /**
     * Calculates the set of concepts that are a kind of parentNid
     *
     * @param parentNid the {@code int} nid of the parent concept
     * @param vc the desired {@code ViewCoordinate}
     * @return the {@code NativeIdSetBI} of components that are a kind of
     * parentNid
     */
    NativeIdSetBI isKindOfSet(int parentNid, ViewCoordinate vc);
    
    void shutdown();
}
