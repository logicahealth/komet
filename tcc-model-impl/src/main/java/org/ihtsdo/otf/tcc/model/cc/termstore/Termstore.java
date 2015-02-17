/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.otf.tcc.model.cc.termstore;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.otf.tcc.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.otf.tcc.api.changeset.ChangeSetGeneratorBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentContainerBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptContainerBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionManagerBI;
import org.ihtsdo.otf.tcc.api.contradiction.strategy.IdentifyAllConflict;
import org.ihtsdo.otf.tcc.api.coordinate.ExternalStampBI;
import org.ihtsdo.otf.tcc.api.coordinate.LanguageSort;
import org.ihtsdo.otf.tcc.api.coordinate.Path;
import org.ihtsdo.otf.tcc.api.coordinate.Position;
import org.ihtsdo.otf.tcc.api.coordinate.Precedence;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.relationship.RelAssertionType;
import org.ihtsdo.otf.tcc.api.store.TermChangeListener;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.uuid.UuidFactory;
import org.ihtsdo.otf.tcc.api.uuid.UuidT5Generator;
import org.ihtsdo.otf.tcc.model.cc.PersistentStore;
import org.ihtsdo.otf.tcc.model.cc.ReferenceConcepts;
import org.ihtsdo.otf.tcc.model.cc.change.LastChange;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptVersion;
import org.ihtsdo.otf.tcc.model.cs.ChangeSetWriter;
import org.ihtsdo.otf.tcc.model.cs.ChangeSetWriterHandler;

//~--- JDK imports ------------------------------------------------------------
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.security.NoSuchAlgorithmException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.otf.tcc.api.chronicle.ProcessComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptFetcherBI;
import org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionManagerPolicy;
import org.ihtsdo.otf.tcc.api.coordinate.LanguagePreferenceList;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;
import org.ihtsdo.otf.tcc.model.index.service.IndexerBI;
import org.ihtsdo.otf.tcc.model.path.PathManager;

/**
 *
 * @author kec
 */
public abstract class Termstore implements PersistentStoreI {

    protected static ViewCoordinate metadataVC = null;

    /**
     * Field description
     */
    ConcurrentHashMap<UUID, TerminologySnapshotDI> persistentSnapshots = new ConcurrentHashMap<>();
    /**
     * Field description
     */
    private TerminologySnapshotDI globalSnapshot;

    /**
     * Method description
     *
     *
     * @param key
     * @param writer
     */
    @Override
    public void addChangeSetGenerator(String key, ChangeSetGeneratorBI writer) {
        ChangeSetWriterHandler.addWriter(key, writer);
    }

    /**
     * Method description
     *
     *
     * @param cl
     */
    @Override
    public void addTermChangeListener(TermChangeListener cl) {
        LastChange.addTermChangeListener(cl);
    }

    /**
     * Method description
     *
     *
     * @param cv
     *
     * @throws IOException
     */
    @Override
    public void addUncommitted(ConceptVersionBI cv) throws IOException {
        addUncommitted(cv.getChronicle());
    }

    /**
     * Method description
     *
     *
     * @param cv
     *
     * @throws IOException
     */
    @Override
    public void addUncommittedNoChecks(ConceptVersionBI cv) throws IOException {
        addUncommittedNoChecks(cv.getChronicle());
    }

    /**
     * Method description
     *
     *
     * @param snapshotUuid
     * @param vc
     *
     * @return
     */
    @Override
    public TerminologySnapshotDI cacheSnapshot(UUID snapshotUuid, ViewCoordinate vc) {
        if (persistentSnapshots.containsKey(snapshotUuid)) {
            TerminologySnapshotDI snapshot = getSnapshot(vc);

            persistentSnapshots.put(snapshotUuid, snapshot);
        }

        return persistentSnapshots.get(snapshotUuid);
    }

    @Override
    public boolean isConceptNid(int nid) {
        return nid == getConceptNidForNid(nid);
    }

    /**
     * Method description
     *
     *
     * @param changeSetFileName
     * @param changeSetTempFileName
     * @param policy
     *
     * @return
     */
    @Override
    public ChangeSetGeneratorBI createDtoChangeSetGenerator(File changeSetFileName, File changeSetTempFileName,
            ChangeSetGenerationPolicy policy) {
        return new ChangeSetWriter(changeSetFileName, changeSetTempFileName, policy, true);
    }

    /**
     * Method description
     *
     *
     * @param nid
     *
     * @return
     */
    @Override
    public CharSequence informAboutNid(int nid) {
        StringBuilder sb = new StringBuilder();

        try {
            int cNid = Ts.get().getConceptNidForNid(nid);

            if (cNid == nid) {
                ConceptChronicleBI cc = Ts.get().getConcept(cNid);

                sb.append("'");
                sb.append(cc.toUserString());
                sb.append("' ");
                sb.append(cNid);
                sb.append(" ");
                sb.append(cc.getPrimordialUuid());
            } else {
                ComponentBI component = Ts.get().getComponent(nid);

                sb.append("comp: '");

                if (component != null) {
                    sb.append(component.toUserString());
                } else {
                    sb.append("null");
                }

                sb.append("' ");
                sb.append(nid);
                sb.append(" ");
                sb.append(component.getPrimordialUuid());
            }
        } catch (IOException ex) {
            Logger.getLogger(Termstore.class.getName()).log(Level.SEVERE, null, ex);
        }

        return sb;
    }

    /**
     * Method description
     *
     *
     * @param uuid
     *
     * @return
     */
    @Override
    public CharSequence informAboutUuid(UUID uuid) {
        if (uuid == null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder();

        if (Ts.get().hasUuid(uuid)) {
            try {
                int nid = Ts.get().getNidForUuids(uuid);

                if (nid == Integer.MAX_VALUE) {
                    sb.append("Component unassigned (Integer.MAX_VALUE) ").append(uuid);

                    return sb;
                }

                int cNid = Ts.get().getConceptNidForNid(nid);

                if (cNid == Integer.MAX_VALUE) {
                    sb.append("Component: ").append(nid).append(" ").append(uuid);
                    sb.append("Concept unassigned (Integer.MAX_VALUE) ").append(uuid);

                    return sb;
                }

                if (cNid == nid) {
                    ConceptChronicleBI cc = Ts.get().getConcept(cNid);

                    sb.append("'");
                    sb.append(cc.toUserString());
                    sb.append("' ");
                    sb.append(cNid);
                    sb.append(" ");
                } else {
                    ComponentBI component = Ts.get().getComponent(nid);

                    sb.append("comp: '");

                    if (component != null) {
                        sb.append(component.toUserString());
                    } else {
                        sb.append("null");
                    }

                    sb.append("' ");
                    sb.append(nid);
                    sb.append(" ");
                }
            } catch (IOException ex) {
                Logger.getLogger(Termstore.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        sb.append(uuid.toString());

        return sb;
    }

    /**
     * Method description
     *
     *
     * @param econFileStrings
     *
     * @throws Exception
     */
    @Override
    public final int loadEconFiles(String... econFileStrings) throws Exception {
        List<File> econFiles = new ArrayList<>(econFileStrings.length);

        for (String fileString : econFileStrings) {
            econFiles.add(new File(fileString));
        }

        LastChange.suspendChangeNotifications();
        int conceptsLoaded = loadEconFiles(econFiles.toArray(new File[econFiles.size()]));
        LastChange.resumeChangeNotifications();
        return conceptsLoaded;
    }

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws IOException
     */
    protected abstract ViewCoordinate makeMetaVc() throws IOException;

    /**
     * Method description
     *
     *
     * @param path
     * @param time
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public Position newPosition(Path path, long time) throws IOException {
        return new Position(time, path);
    }

    /**
     * Method description
     *
     *
     * @param key
     */
    @Override
    public void removeChangeSetGenerator(String key) {
        ChangeSetWriterHandler.removeWriter(key);
    }

    /**
     * Method description
     *
     *
     * @param cl
     */
    @Override
    public void removeTermChangeListener(TermChangeListener cl) {
        LastChange.removeTermChangeListener(cl);
    }

    /**
     * Method description
     *
     *
     * @param snapshotUuid
     *
     * @return
     *
     * @throws NoSuchElementException
     */
    @Override
    public TerminologySnapshotDI getCachedSnapshot(UUID snapshotUuid) throws NoSuchElementException {
        if (persistentSnapshots.containsKey(snapshotUuid)) {
            return persistentSnapshots.get(snapshotUuid);
        }

        throw new NoSuchElementException("Snapshot uuid: " + snapshotUuid);
    }

    /**
     * Method description
     *
     *
     * @param uuids
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public ComponentChronicleBI<?> getComponent(Collection<UUID> uuids) throws IOException {
        return getComponent(getNidForUuids(uuids));
    }

    /**
     * Method description
     *
     *
     * @param cc
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public ComponentChronicleBI<?> getComponent(ComponentContainerBI cc) throws IOException {
        return getComponent(cc.getNid());
    }

    /**
     * Method description
     *
     *
     * @param nid
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public final ComponentChronicleBI<?> getComponent(int nid) throws IOException {
        if (getConceptNidForNid(nid) == Integer.MAX_VALUE) {
            return null;
        }

        return getConceptForNid(nid).getComponent(nid);
    }

    /**
     * Method description
     *
     *
     * @param uuids
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public ComponentChronicleBI<?> getComponent(UUID... uuids) throws IOException {
        return getComponent(getNidForUuids(uuids));
    }

    /**
     * Method description
     *
     *
     * @param authorityNid
     * @param altId
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public ComponentChronicleBI<?> getComponentFromAlternateId(int authorityNid, String altId) throws IOException {
        try {
            return getComponent(PersistentStore.get().getNidForUuids(UuidT5Generator.get(PersistentStore.get().getUuidPrimordialForNid(authorityNid),
                    altId)));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Method description
     *
     *
     * @param c
     * @param uuids
     *
     * @return
     *
     * @throws ContradictionException
     * @throws IOException
     */
    @Override
    public ComponentVersionBI getComponentVersion(ViewCoordinate c, Collection<UUID> uuids)
            throws IOException, ContradictionException {
        return getComponentVersion(c, getNidForUuids(uuids));
    }

    /**
     * Method description
     *
     *
     * @param coordinate
     * @param nid
     *
     * @return
     *
     * @throws ContradictionException
     * @throws IOException
     */
    @Override
    public ComponentVersionBI getComponentVersion(ViewCoordinate coordinate, int nid)
            throws IOException, ContradictionException {
        ComponentBI component = getComponent(nid);
        if (component != null) {
            if (ConceptChronicle.class.isAssignableFrom(component.getClass())) {
                return new ConceptVersion((ConceptChronicle) component, coordinate);
            }

            return ((ComponentChronicleBI<?>) component).getVersion(coordinate);
        }
        return null;
    }

    /**
     * Method description
     *
     *
     * @param c
     * @param uuids
     *
     * @return
     *
     * @throws ContradictionException
     * @throws IOException
     */
    @Override
    public ComponentVersionBI getComponentVersion(ViewCoordinate c, UUID... uuids)
            throws IOException, ContradictionException {
        return getComponentVersion(c, getNidForUuids(uuids));
    }

    /**
     * Method description
     *
     *
     * @param vc
     * @param authorityNid
     * @param altId
     *
     * @return
     *
     * @throws ContradictionException
     * @throws IOException
     */
    @Override
    public ComponentVersionBI getComponentVersionFromAlternateId(ViewCoordinate vc, int authorityNid, String altId)
            throws IOException, ContradictionException {
        try {
            return getComponentVersion(
                    vc, PersistentStore.get().getNidForUuids(UuidT5Generator.get(PersistentStore.get().getUuidPrimordialForNid(authorityNid), altId)));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Method description
     *
     *
     * @param vc
     * @param authorityUUID
     * @param altId
     *
     * @return
     *
     * @throws ContradictionException
     * @throws IOException
     */
    @Override
    public ComponentVersionBI getComponentVersionFromAlternateId(ViewCoordinate vc, UUID authorityUUID, String altId)
            throws IOException, ContradictionException {
        try {
            return getComponentVersion(vc, PersistentStore.get().getNidForUuids(UuidT5Generator.get(authorityUUID, altId)));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Method description
     *
     *
     * @param uuids
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public ConceptChronicleBI getConcept(Collection<UUID> uuids) throws IOException {
        return getConcept(getNidForUuids(uuids));
    }

    /**
     * Method description
     *
     *
     * @param cc
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public ConceptChronicleBI getConcept(ConceptContainerBI cc) throws IOException {
        return getConcept(cc.getCnid());
    }

    /**
     * Method description
     *
     *
     * @param cNid
     *
     * @return
     *
     */
    @Override
    public ConceptChronicle getConcept(int cNid)  {
        try {
            return ConceptChronicle.get(cNid);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Method description
     *
     *
     * @param uuids
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public ConceptChronicleBI getConcept(UUID... uuids) throws IOException {
        return getConcept(getNidForUuids(uuids));
    }

    /**
     * Method description
     *
     *
     * @param nid
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public ConceptChronicleBI getConceptForNid(int nid) throws IOException {
        return getConcept(getConceptNidForNid(nid));
    }

    /**
     * Method description
     *
     *
     * @param authorityNid
     * @param altId
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public ConceptChronicle getConceptFromAlternateId(int authorityNid, String altId) throws IOException {
        try {
            return ConceptChronicle.get(
                    PersistentStore.get().getNidForUuids(UuidT5Generator.get(PersistentStore.get().getUuidPrimordialForNid(authorityNid), altId)));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Method description
     *
     *
     * @param authorityUuid
     * @param altId
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public ConceptChronicleBI getConceptFromAlternateId(UUID authorityUuid, String altId) throws IOException {
        try {
            return ConceptChronicle.get(PersistentStore.get().getNidForUuids(UuidT5Generator.get(authorityUuid, altId)));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Method description
     *
     *
     * @param c
     * @param uuids
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public ConceptVersionBI getConceptVersion(ViewCoordinate c, Collection<UUID> uuids) throws IOException {
        return getConceptVersion(c, getNidForUuids(uuids));
    }

    /**
     * Method description
     *
     *
     * @param c
     * @param cNid
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public ConceptVersionBI getConceptVersion(ViewCoordinate c, int cNid) throws IOException {
        return new ConceptVersion(ConceptChronicle.get(cNid), c);
    }

    /**
     * Method description
     *
     *
     * @param c
     * @param uuids
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public ConceptVersionBI getConceptVersion(ViewCoordinate c, UUID... uuids) throws IOException {
        return getConceptVersion(c, getNidForUuids(uuids));
    }

    /**
     * Method description
     *
     *
     * @param vc
     * @param authorityNid
     * @param altId
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public ConceptVersion getConceptVersionFromAlternateId(ViewCoordinate vc, int authorityNid, String altId)
            throws IOException {
        ConceptChronicle c = getConceptFromAlternateId(authorityNid, altId);

        return new ConceptVersion(c, vc);
    }

    /**
     * Method description
     *
     *
     * @param vc
     * @param authorityUuid
     * @param altId
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public ConceptVersion getConceptVersionFromAlternateId(ViewCoordinate vc, UUID authorityUuid, String altId)
            throws IOException {
        ConceptChronicle c = (ConceptChronicle) getConceptFromAlternateId(authorityUuid, altId);

        return new ConceptVersion(c, vc);
    }

    /**
     * Method description
     *
     *
     * @param c
     * @param cNids
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public Map<Integer, ConceptVersionBI> getConceptVersions(ViewCoordinate c, NativeIdSetBI cNids) throws IOException {
        ConceptVersionGetter processor = new ConceptVersionGetter(cNids, c);

        try {
            PersistentStore.get().iterateConceptDataInParallel(processor);
        } catch (Exception e) {
            throw new IOException(e);
        }

        return Collections.unmodifiableMap(new HashMap<>(processor.conceptMap));
    }

    /**
     * Method description
     *
     *
     * @param cNids
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public Map<Integer, ConceptChronicleBI> getConcepts(NativeIdSetBI cNids) throws IOException {
        ConceptGetter processor = new ConceptGetter(cNids);

        try {
            PersistentStore.get().iterateConceptDataInParallel(processor);
        } catch (Exception e) {
            throw new IOException(e);
        }

        return Collections.unmodifiableMap(new HashMap<>(processor.conceptMap));
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public TerminologySnapshotDI getGlobalSnapshot() {
        if (globalSnapshot == null) {
            throw new NoSuchElementException("global snapshot not set");
        }

        return globalSnapshot;
    }

    /**
     * Method description
     *
     *
     * @param uuids
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public Collection<Integer> getNidCollection(Collection<UUID> uuids) throws IOException {
        List<Integer> nids = new ArrayList<>();

        for (UUID uuid : uuids) {
            nids.add(getNidForUuids(uuid));
        }

        return nids;
    }

    /**
     * Method description
     *
     *
     * @param authorityUuid
     * @param altId
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public int getNidFromAlternateId(UUID authorityUuid, String altId) throws IOException {
        return PersistentStore.get().getNidForUuids(UuidFactory.getUuidFromAlternateId(authorityUuid, altId));
    }

    /**
     * Method description
     *
     *
     * @param vc
     *
     * @return
     */
    @Override
    public TerminologySnapshotDI getSnapshot(ViewCoordinate vc) {
        return new TerminologySnapshot(this, vc);
    }

    /**
     * Method description
     *
     *
     * @param version
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public int getStamp(ExternalStampBI version) throws IOException {
        return getStamp(version.getStatus(), version.getTime(), getNidForUuids(version.getAuthorUuid()),
                getNidForUuids(version.getModuleUuid()), getNidForUuids(version.getPathUuid()));
    }

    /**
     * Method description
     *
     *
     * @param nids
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public Collection<UUID> getUuidCollection(Collection<Integer> nids) throws IOException {
        List<UUID> uuids = new ArrayList<>();

        for (Integer nid : nids) {
            uuids.add(getUuidPrimordialForNid(nid));
        }

        return uuids;
    }

    /**
     * Method description
     *
     *
     * @param globalSnapshot
     */
    @Override
    public void setGlobalSnapshot(TerminologySnapshotDI globalSnapshot) {
        this.globalSnapshot = globalSnapshot;
    }

    @Override
    public CharSequence informAboutId(Object id) {
        if (id instanceof UUID) {
            return informAboutUuid((UUID) id);
        } else if (id instanceof Number) {
            informAboutNid((Integer) id);
        }

        return id.toString();
    }

    @Override
    public final ViewCoordinate getMetadataVC() throws IOException {
        if (metadataVC == null) {
            metadataVC = makeMetaVc();
            putViewCoordinate(metadataVC);
        }
        return metadataVC;
    }

    @Override
    public final Path getPath(int pathNid) throws IOException {
        return PathManager.get().get(pathNid);
    }

    @Override
    public final List<? extends Path> getPathChildren(int nid) {
        return PathManager.get().getPathChildren(nid);
    }

    @Override
    public final Set<Path> getPathSetFromPositionSet(Set<Position> positions) throws IOException {
        HashSet<Path> paths = new HashSet<>(positions.size());
        for (Position position : positions) {
            paths.add(position.getPath());
            // addOrigins(paths, position.getPath().getInheritedOrigins());
        }
        return paths;
    }

    @Override
    public final Set<Path> getPathSetFromStampSet(Set<Integer> stamps) throws IOException {
        HashSet<Path> paths = new HashSet<>(stamps.size());
        for (int stamp : stamps) {
            Path path = PathManager.get().get(getPathNidForStamp(stamp));
            paths.add(path);
        }
        return paths;
    }

    @Override
    public final Set<Position> getPositionSet(Set<Integer> stamps) throws IOException {
        TreeSet<Position> positions = new TreeSet<>();
        for (int stamp : stamps) {
            if (stamp >= 0) {
                Path path = PathManager.get().get(getPathNidForStamp(stamp));
                positions.add(new Position(getTimeForStamp(stamp), path));
            }
        }
        return positions;
    }

}
