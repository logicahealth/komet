/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.otf.tcc.rest.server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import javax.ws.rs.*;
import javax.ws.rs.core.StreamingOutput;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RefexPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RelationshipPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.VersionPolicy;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;
import org.ihtsdo.otf.tcc.chronicle.cc.concept.ConceptDataFetcherI;

/**
 *
 * @author kec
 */
@Path("/concept")
public class ConceptResource {

    static {
        BdbSingleton.get();
    }
    
    @GET
    @Path("{id}")
    @Produces("text/plain")
    public String getConceptPlain(@PathParam("id") String id) throws IOException {
        ConceptChronicleBI c;

        if (id.length() == 36) {
            c = BdbSingleton.get().getConcept(UUID.fromString(id));
            
        } else {
            c = BdbSingleton.get().getConcept(Integer.parseInt(id));
        }
        return c.toLongString();
    }

    @GET
    @Path("{id}")
    @Produces("text/html")
    
    public String getConceptHtml(@PathParam("id") String id) throws IOException, ContradictionException {
         UUID uuid;
        
        if (id.length() == 36) {
            uuid = UUID.fromString(id);
        } else {
            uuid = BdbSingleton.get().getUuidPrimordialForNid(Integer.parseInt(id));
        }
        ViewCoordinate vc = StandardViewCoordinates.getSnomedInferredLatest();
        ConceptChronicleDdo fxc = BdbSingleton.get().getFxConcept(uuid, vc,
                    VersionPolicy.ACTIVE_VERSIONS,
                    RefexPolicy.ANNOTATION_MEMBERS,
                    RelationshipPolicy.ORIGINATING_AND_DESTINATION_RELATIONSHIPS);
        return fxc.toHtml();
    }

    @GET
    @Path("{id}")
    @Produces("application/xml")
    public TtkConceptChronicle getConceptXml(@PathParam("id") String id) throws IOException {
        ConceptChronicleBI c;
        if (id.length() == 36) {
            c = BdbSingleton.get().getConcept(UUID.fromString(id));
        } else {
            c = BdbSingleton.get().getConcept(Integer.parseInt(id));
        }
        return new TtkConceptChronicle(c);
    }

    @GET
    @Path("{id}")
    @Produces("application/bdb")
    public StreamingOutput getConceptByteArray(@PathParam("id") String id) throws IOException {
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
    @Path("{id}")
    @Produces("application/econ")
    public StreamingOutput getEConceptByteArray(@PathParam("id") String id) throws IOException {
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
