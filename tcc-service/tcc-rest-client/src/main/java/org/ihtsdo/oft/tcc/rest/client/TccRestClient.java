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



package org.ihtsdo.oft.tcc.rest.client;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.api.coordinate.PathBI;
import org.ihtsdo.otf.tcc.api.coordinate.PositionBI;
import org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.otf.tcc.api.blueprint.TerminologyBuilderBI;
import org.ihtsdo.otf.tcc.api.store.TerminologyDI.CONCEPT_EVENT;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.cs.ChangeSetPolicy;
import org.ihtsdo.otf.tcc.api.cs.ChangeSetWriterThreading;
import org.ihtsdo.otf.tcc.api.db.DbDependency;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.chronicle.cc.NidPairForRefex;
import org.ihtsdo.otf.tcc.chronicle.cc.P;
import org.ihtsdo.otf.tcc.chronicle.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.chronicle.cc.concept.ConceptDataFetcherI;
import org.ihtsdo.otf.tcc.chronicle.cc.concept.NidDataInMemory;
import org.ihtsdo.otf.tcc.chronicle.cc.relationship.Relationship;
import org.ihtsdo.otf.tcc.chronicle.cc.termstore.Termstore;
import org.ihtsdo.otf.tcc.ddo.ComponentReference;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RefexPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RelationshipPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.VersionPolicy;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Path;

import java.util.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;

import javax.ws.rs.core.MediaType;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientResponse;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.ddo.store.FxTs;

/**
 *
 * @author kec
 */
public class TccRestClient extends Termstore {
   public static final String    defaultLocalHostServer = "http://localhost:8080/terminology/rest/";
   public static final MediaType bdbMediaType           = new MediaType("application", "bdb");
   private static String         serverUrlStr           = defaultLocalHostServer;
   private static Client         restClient;
   private static TccRestClient  restClientSingleton;

   @Override
   public long incrementAndGetSequence() {
      WebTarget r           = restClient.target(serverUrlStr + "sequence/next");
      String      sequenceStr = r.request(MediaType.TEXT_PLAIN).get(String.class);

      return Long.parseLong(sequenceStr);
   }

   @Override
   public void putViewCoordinate(ViewCoordinate vc) throws IOException {
      WebTarget r = restClient.target(serverUrlStr + "coordinate/view/" + vc.getVcUuid());

      r.request(bdbMediaType).accept(MediaType.TEXT_PLAIN).put(Entity.entity(vc, bdbMediaType));
   }

   public static void setup(String serverUrlStr) throws IOException {
      TccRestClient.serverUrlStr = serverUrlStr;

      ClientConfig cc = new ClientConfig(ViewCoordinateSerializationProvider.class);

      restClient          = ClientBuilder.newClient(cc);
      restClientSingleton = new TccRestClient();
      P.s                 = restClientSingleton;
      Ts.set(restClientSingleton);
      FxTs.set(restClientSingleton);
      P.s.putViewCoordinate(P.s.getMetadataVC());
      P.s.putViewCoordinate(StandardViewCoordinates.getSnomedInferredLatest());
      Ts.get().setGlobalSnapshot(Ts.get().getSnapshot(StandardViewCoordinates.getSnomedInferredLatest()));
   }

   @Override
   public void waitTillWritesFinished() {
      WebTarget r = restClient.target(serverUrlStr + "termstore/wait-for-writes");

      r.request(MediaType.TEXT_PLAIN).get(String.class);
   }

   @Override
   public int getAuthorNidForStamp(int sapNid) {
      WebTarget r      = restClient.target(serverUrlStr + "stamp/author/" + sapNid);
      String      nidStr = r.request(MediaType.TEXT_PLAIN).get(String.class);

      return Integer.parseInt(nidStr);
   }

   @Override
   public ConceptDataFetcherI getConceptDataFetcher(int cNid) throws IOException {
      WebTarget r  = restClient.target(serverUrlStr + "concept/" + cNid);
      InputStream is = r.request(bdbMediaType).get(InputStream.class);

      try (DataInputStream dis = new DataInputStream(is)) {
         int returnNid = dis.readInt();    // the cnid

         assert returnNid == cNid : "cNid: " + cNid + " returnNid: " + returnNid;

         ConceptDataFetcherI fetcher = new NidDataInMemory(is);

         return fetcher;
      }
   }

   @Override
   public int getConceptNidForNid(int nid) {
      WebTarget r      = restClient.target(serverUrlStr + "nid/concept/" + nid);
      String      nidStr = r.request(MediaType.TEXT_PLAIN).get(String.class);

      return Integer.parseInt(nidStr);
   }

   @Override
   public int[] getDestRelOriginNids(int cNid) throws IOException {
      WebTarget r  = restClient.target(serverUrlStr + "relationship/origin/" + cNid);
      InputStream is = r.request(bdbMediaType).get(InputStream.class);

      try (ObjectInputStream ois = new ObjectInputStream(is)) {
         return (int[]) ois.readObject();
      } catch (ClassNotFoundException ex) {
         throw new IOException(ex);
      }
   }

   @Override
   public int[] getDestRelOriginNids(int cNid, NidSetBI relTypes) throws IOException {
      WebTarget r  = restClient.target(serverUrlStr + "relationship/origin/" + cNid + "/typed");
      InputStream is =
         r.queryParam("relTypes", relTypes.getAmpersandString()).request(bdbMediaType).get(InputStream.class);

      try (ObjectInputStream ois = new ObjectInputStream(is)) {
         return (int[]) ois.readObject();
      } catch (ClassNotFoundException ex) {
         throw new IOException(ex);
      }
   }

   @Override
   public NativeIdSetBI getEmptyNidSet() throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   private ConceptChronicleDdo getFxConcept(UUID conceptUUID, UUID vcUuid) {
      WebTarget    r        = restClient.target(serverUrlStr + "fx-concept/" + conceptUUID + "/"
                                   + vcUuid);
      ClientResponse response = r.request(MediaType.APPLICATION_XML).get(ClientResponse.class);

      return response.readEntity(ConceptChronicleDdo.class, null);
   }

   @Override
   public ConceptChronicleDdo getFxConcept(UUID conceptUUID, ViewCoordinate vc)
           throws IOException, ContradictionException {
      return getFxConcept(conceptUUID, vc.getVcUuid());
   }

   @Override
   public ConceptChronicleDdo getFxConcept(ComponentReference ref, UUID vcUuid, VersionPolicy versionPolicy,
                                 RefexPolicy refexPolicy, RelationshipPolicy relationshipPolicy) {
      return getFxConcept(ref.getUuid(), vcUuid, versionPolicy, refexPolicy, relationshipPolicy);
   }

   @Override
   public ConceptChronicleDdo getFxConcept(ComponentReference ref, ViewCoordinate vc, VersionPolicy versionPolicy,
                                 RefexPolicy refexPolicy, RelationshipPolicy relationshipPolicy) {
      return getFxConcept(ref, vc.getVcUuid(), versionPolicy, refexPolicy, relationshipPolicy);
   }

   @Override
   public ConceptChronicleDdo getFxConcept(UUID conceptUUID, UUID vcUuid, VersionPolicy versionPolicy,
                                 RefexPolicy refexPolicy, RelationshipPolicy relationshipPolicy) {
      WebTarget r = restClient.target(serverUrlStr + "fx-concept/" + conceptUUID + "/" + vcUuid + "/"
                         + versionPolicy + "/" + refexPolicy + "/" + relationshipPolicy);
      ClientResponse response = r.request(MediaType.APPLICATION_XML).get(ClientResponse.class);

      return response.readEntity(ConceptChronicleDdo.class, null);
   }

   @Override
   public ConceptChronicleDdo getFxConcept(UUID conceptUUID, ViewCoordinate vc, VersionPolicy versionPolicy,
                                 RefexPolicy refexPolicy, RelationshipPolicy relationshipPolicy) {
      return getFxConcept(conceptUUID, vc.getVcUuid(), versionPolicy, refexPolicy, relationshipPolicy);
   }

   @Override
   public long getLastCancel() {
      WebTarget r           = restClient.target(serverUrlStr + "sequence/last-cancel");
      String      sequenceStr = r.request(MediaType.TEXT_PLAIN).get(String.class);

      return Long.parseLong(sequenceStr);
   }

   @Override
   public long getLastCommit() {
      WebTarget r           = restClient.target(serverUrlStr + "sequence/last-commit");
      String      sequenceStr = r.request(MediaType.TEXT_PLAIN).get(String.class);

      return Long.parseLong(sequenceStr);
   }

   @Override
   public int getMaxReadOnlyStamp() {
      WebTarget r      = restClient.target(serverUrlStr + "stamp/read-only-max");
      String      nidStr = r.request(MediaType.TEXT_PLAIN).get(String.class);

      return Integer.parseInt(nidStr);
   }

   @Override
   public ViewCoordinate getMetadataVC() throws IOException {
      return makeMetaVc();
   }

   @Override
   public int getModuleNidForStamp(int sapNid) {
      WebTarget r      = restClient.target(serverUrlStr + "stamp/module/" + sapNid);
      String      nidStr = r.request(MediaType.TEXT_PLAIN).get(String.class);

      return Integer.parseInt(nidStr);
   }

   private int getNidForUuidSetString(String uuidSetString) {
      WebTarget r      = restClient.target(serverUrlStr + "nid/" + uuidSetString);
      String      nidStr = r.request(MediaType.TEXT_PLAIN).get(String.class);

      return Integer.parseInt(nidStr);
   }

   @Override
   public int getNidForUuids(Collection<UUID> uuids) throws IOException {
      StringBuilder  uuidSetStringBuilder = new StringBuilder();
      Iterator<UUID> uuidItr              = uuids.iterator();

      while (uuidItr.hasNext()) {
         uuidSetStringBuilder.append(uuidItr.next());

         if (uuidItr.hasNext()) {
            uuidSetStringBuilder.append("&");
         }
      }

      return getNidForUuidSetString(uuidSetStringBuilder.toString());
   }

   @Override
   public int getNidForUuids(UUID... uuids) throws IOException {
      StringBuilder uuidSetStringBuilder = new StringBuilder();

      for (int i = 0; i < uuids.length; i++) {
         uuidSetStringBuilder.append(uuids[i]);

         if (i + 1 < uuids.length) {
            uuidSetStringBuilder.append("&");
         }
      }

      return getNidForUuidSetString(uuidSetStringBuilder.toString());
   }

   @Override
   public PathBI getPath(int pathNid) throws IOException {
      WebTarget r  = restClient.target(serverUrlStr + "path/" + pathNid);
      InputStream is = r.request(bdbMediaType).get(InputStream.class);

      try (ObjectInputStream ois = new ObjectInputStream(is)) {
         return (PathBI) ois.readObject();
      } catch (ClassNotFoundException ex) {
         throw new IOException(ex);
      }
   }

   @Override
   public int getPathNidForStamp(int sapNid) {
      WebTarget r      = restClient.target(serverUrlStr + "stamp/path/" + sapNid);
      String      nidStr = r.request(MediaType.TEXT_PLAIN).get(String.class);

      return Integer.parseInt(nidStr);
   }

   @Override
   public Map<String, String> getProperties() throws IOException {
      WebTarget r  = restClient.target(serverUrlStr + "property/");
      InputStream is = r.request(bdbMediaType).get(InputStream.class);

      try (ObjectInputStream ois = new ObjectInputStream(is)) {
         return (Map<String, String>) ois.readObject();
      } catch (ClassNotFoundException ex) {
         throw new IOException(ex);
      }
   }

   @Override
   public String getProperty(String key) throws IOException {
      WebTarget r = restClient.target(serverUrlStr + "property/" + key);

      return r.request(MediaType.TEXT_PLAIN).get(String.class);
   }

   @Override
   public List<NidPairForRefex> getRefexPairs(int cNid) throws IOException {
      WebTarget r  = restClient.target(serverUrlStr + "nidpairs/refex/" + cNid);
      InputStream is = r.request(bdbMediaType).get(InputStream.class);

      try (ObjectInputStream ois = new ObjectInputStream(is)) {
         return (List<NidPairForRefex>) ois.readObject();
      } catch (ClassNotFoundException ex) {
         throw new IOException(ex);
      }
   }

   public static TccRestClient getRestClient() throws IOException {
      if (restClientSingleton == null) {
         setup(TccRestClient.defaultLocalHostServer);
      }

      return restClientSingleton;
   }

   @Override
   public long getSequence() {
      WebTarget r           = restClient.target(serverUrlStr + "sequence");
      String      sequenceStr = r.request(MediaType.TEXT_PLAIN).get(String.class);

      return Long.parseLong(sequenceStr);
   }

   @Override
   public int getStamp(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Status getStatusForStamp(int stamp) {
      WebTarget r      = restClient.target(serverUrlStr + "stamp/status/" + stamp);
      String      statusString = r.request(MediaType.TEXT_PLAIN).get(String.class);

      return Status.valueOf(statusString);
   }

   @Override
   public TerminologyBuilderBI getTerminologyBuilder(EditCoordinate ec, ViewCoordinate vc) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public long getTimeForStamp(int sapNid) {
      WebTarget r       = restClient.target(serverUrlStr + "stamp/time/" + sapNid);
      String      timeStr = r.request(MediaType.TEXT_PLAIN).get(String.class);

      return Long.parseLong(timeStr);
   }

   @Override
   public UUID getUuidPrimordialForNid(int nid) throws IOException {
      WebTarget r = restClient.target(serverUrlStr + "uuid/primordial/" + nid);

      return UUID.fromString(r.request(MediaType.TEXT_PLAIN).get(String.class));
   }

   @Override
   public ViewCoordinate getViewCoordinate(UUID vcUuid) throws IOException {
      WebTarget r  = restClient.target(serverUrlStr + "coordinate/view/" + vcUuid.toString());
      InputStream is = r.request(bdbMediaType).get(InputStream.class);

      try (ObjectInputStream ois = new ObjectInputStream(is)) {
         return (ViewCoordinate) ois.readObject();
      } catch (ClassNotFoundException ex) {
         throw new IOException(ex);
      }
   }

   @Override
   public Collection<ViewCoordinate> getViewCoordinates() throws IOException {
      WebTarget r  = restClient.target(serverUrlStr + "coordinate/view");
      InputStream is = r.request(bdbMediaType).get(InputStream.class);

      try (ObjectInputStream ois = new ObjectInputStream(is)) {
         return (Collection<ViewCoordinate>) ois.readObject();
      } catch (ClassNotFoundException ex) {
         throw new IOException(ex);
      }
   }

   @Override
   public boolean hasConcept(int cNid) throws IOException {
      if (ConceptChronicle.getIfInMap(cNid) != null) {
         return true;
      }

      if (getConceptNidForNid(cNid) == cNid) {
         return true;
      }

      return false;
   }

   @Override
   public boolean hasUuid(UUID memberUUID) {
      WebTarget r = restClient.target(serverUrlStr + "uuid/" + memberUUID.toString());

      return Boolean.valueOf(r.request(MediaType.TEXT_PLAIN).get(String.class));
   }

   //J-

   @Override
   public Collection<DbDependency> getLatestChangeSetDependencies() throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends ConceptChronicleBI> getUncommittedConcepts() {
      throw new UnsupportedOperationException("Not supported yet.");
   }
   
   
   @Override
   public List<UUID> getUuidsForNid(int nid) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void setConceptNidForNid(int cNid, int nid) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void setProperty(String key, String value) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }
   
   
   @Override
   public void addPropertyChangeListener(CONCEPT_EVENT pce, PropertyChangeListener l) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void addUncommitted(ConceptChronicleBI cc) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void addUncommittedNoChecks(ConceptChronicleBI cc) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void addVetoablePropertyChangeListener(CONCEPT_EVENT pce, VetoableChangeListener l) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void addXrefPair(int nid, NidPairForRefex pair) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

    @Override
    public void addRelOrigin(int destinationCNid, int originCNid) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

   @Override
   public void cancel() throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void cancel(ConceptChronicleBI cc) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void cancel(ConceptVersionBI cv) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void cancelAfterCommit(NidSetBI commitSapNids) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void commit() throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void commit(ConceptChronicleBI cc) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void commit(ConceptVersionBI cv) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public boolean commit(ConceptChronicleBI cc, ChangeSetPolicy changeSetPolicy,
                         ChangeSetWriterThreading changeSetWriterThreading)
           throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public boolean forget(ConceptAttributeVersionBI attr) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void forget(ConceptChronicleBI concept) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void forget(DescriptionVersionBI desc) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void forget(RefexChronicleBI extension) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void forget(RelationshipVersionBI rel) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void forgetXrefPair(int nid, NidPairForRefex pair) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void iterateConceptDataInParallel(ProcessUnfetchedConceptDataBI processor) throws Exception {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void iterateConceptDataInSequence(ProcessUnfetchedConceptDataBI processor) throws Exception {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void loadEconFiles(File[] econFiles) throws Exception {
      throw new UnsupportedOperationException("Not supported yet.");
   }

    @Override
    public void loadEconFiles(Path[] econFiles) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   @Override
   public void resetConceptNidForNid(int cNid, int nid) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public boolean satisfiesDependencies(Collection<DbDependency> dependencies) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void xrefAnnotation(RefexChronicleBI annotation) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public NativeIdSetBI getAllConceptNids() throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Set<PathBI> getPathSetFromPositionSet(Set<PositionBI> positions) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Set<PathBI> getPathSetFromSapSet(Set<Integer> sapNids) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Set<PositionBI> getPositionSet(Set<Integer> sapNids) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public int[] getPossibleChildren(int cNid, ViewCoordinate vc) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }


   @Override
   public int getConceptCount() throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }


   @Override
   public List<? extends PathBI> getPathChildren(int nid) {
      throw new UnsupportedOperationException("Not supported yet.");
   }


   @Override
   public boolean hasPath(int nid) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public boolean hasUncommittedChanges() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public boolean hasUuid(List<UUID> memberUUIDs) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

    @Override
    public void resumeChangeNotifications() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void suspendChangeNotifications() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<Relationship> getDestRels(int cNid) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isKindOf(int childNid, int parentNid, ViewCoordinate vc) throws IOException, ContradictionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isChildOf(int childNid, int parentNid, ViewCoordinate vc) throws IOException, ContradictionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void put(UUID uuid, int nid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    //J+

    @Override
    public NativeIdSetBI getConceptNidsForComponentNids(NativeIdSetBI componentNativeIds) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public NativeIdSetBI getComponentNidsForConceptNids(NativeIdSetBI conceptNativeIds) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
