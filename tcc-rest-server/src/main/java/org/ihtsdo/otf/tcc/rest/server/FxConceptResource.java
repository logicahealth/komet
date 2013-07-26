
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.ihtsdo.otf.tcc.rest.server;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RefexPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RelationshipPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.VersionPolicy;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import org.ihtsdo.otf.tcc.chronicle.cc.concept.ConceptDataFetcherI;

/**
 *
 * @author kec
 */
@Path("/fx-concept")
public class FxConceptResource {
    static {
        BdbSingleton.get();
    }

   //~--- get methods ---------------------------------------------------------

   @GET
   @Path("{id}/{vcUuid}")
   @Produces("application/bdb")
   public StreamingOutput getConceptByteArray(@PathParam("id") String id, @PathParam("vcUuid") String vcUuid)
           throws IOException {
      final int cnid;

      if (id.length() == 36) {
         UUID uuid = UUID.fromString(id);

         cnid = BdbSingleton.get().getNidForUuids(uuid);
      } else {
         cnid = Integer.parseInt(id);
      }

      final ConceptDataFetcherI fetcher = BdbSingleton.get().getConceptDataFetcher(cnid);

      return new StreamingOutput() {
         @Override
         public void write(OutputStream output) throws IOException, WebApplicationException {
            DataOutputStream dos = new DataOutputStream(output);

            dos.writeInt(cnid);

            byte[] robs = fetcher.getReadOnlyBytes();

            dos.writeInt(robs.length);
            dos.write(robs);

            byte[] rwbs = fetcher.getReadWriteBytes();

            dos.writeInt(rwbs.length);
            dos.write(rwbs);
         }
      };
   }

   @GET
   @Path("{id}/{vcUuid}")
   @Produces("text/html")
   public String getConceptHtml(@PathParam("id") String id, @PathParam("vcUuid") String vcUuid)
           throws IOException {
      ConceptChronicleBI c;

      if (id.length() == 36) {
         c = BdbSingleton.get().getConcept(UUID.fromString(id));
      } else {
         c = BdbSingleton.get().getConcept(Integer.parseInt(id));
      }

      return "Concept html: " + id + " " + c.toLongString();
   }

   @GET
   @Path("{id}/{vcUuid}")
   @Produces("text/plain")
   public String getConceptPlain(@PathParam("id") String id, @PathParam("vcUuid") String vcUuid)
           throws IOException {
      ConceptChronicleBI c;

      if (id.length() == 36) {
         c = BdbSingleton.get().getConcept(UUID.fromString(id));
      } else {
         c = BdbSingleton.get().getConcept(Integer.parseInt(id));
      }

      return "Concept plain: " + id + " " + c.toLongString();
   }

   @GET
   @Path("{id}/{vcUuid}")
   @Produces("application/xml")
   public ConceptChronicleDdo getConceptXml(@PathParam("id") String id, @PathParam("vcUuid") String vcUuid)
           throws IOException, ContradictionException {
      ConceptChronicleBI c;

      if (id.length() == 36) {
         c = BdbSingleton.get().getConcept(UUID.fromString(id));
      } else {
         c = BdbSingleton.get().getConcept(Integer.parseInt(id));
      }

      ViewCoordinate        vc   = BdbSingleton.get().getViewCoordinate(UUID.fromString(vcUuid));
      TerminologySnapshotDI snap = BdbSingleton.get().getSnapshot(vc);

      return new ConceptChronicleDdo(snap, c, VersionPolicy.ALL_VERSIONS, RefexPolicy.REFEX_MEMBERS,
                           RelationshipPolicy.ORIGINATING_RELATIONSHIPS);
   }

   @GET
   @Path("{id}/{vcUuid}/{versionPolicy}/{refexPolicy}/{relationshipPolicy}")
   @Produces("application/xml")
   public ConceptChronicleDdo getConceptXmlPerPolicy(@PathParam("id") String id, @PathParam("vcUuid") String vcUuid,
           @PathParam("versionPolicy") VersionPolicy versionPolicy,
           @PathParam("refexPolicy") RefexPolicy refexPolicy,
           @PathParam("relationshipPolicy") RelationshipPolicy relationshipPolicy)
           throws IOException, ContradictionException {
      ConceptChronicleBI c;

      if (id.length() == 36) {
         c = BdbSingleton.get().getConcept(UUID.fromString(id));
      } else {
         c = BdbSingleton.get().getConcept(Integer.parseInt(id));
      }

      ViewCoordinate        vc   = BdbSingleton.get().getViewCoordinate(UUID.fromString(vcUuid));
      TerminologySnapshotDI snap = BdbSingleton.get().getSnapshot(vc);

      return new ConceptChronicleDdo(snap, c, versionPolicy, refexPolicy, relationshipPolicy);
   }

   @GET
   @Path("{id}/{vcUuid}")
   @Produces("application/econ")
   public StreamingOutput getEConceptByteArray(@PathParam("id") String id, @PathParam("vcUuid") String vcUuid)
           throws IOException {
      ConceptChronicleBI c;

      if (id.length() == 36) {
         c = BdbSingleton.get().getConcept(UUID.fromString(id));
      } else {
         c = BdbSingleton.get().getConcept(Integer.parseInt(id));
      }

      final TtkConceptChronicle econ = new TtkConceptChronicle(c);

      return new StreamingOutput() {
         @Override
         public void write(OutputStream output) throws IOException, WebApplicationException {
            DataOutputStream dos = new DataOutputStream(output);

            econ.writeExternal(dos);
         }
      };
   }
}
