/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.otf.tcc.rest.server;

import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.ihtsdo.otf.tcc.model.cc.PersistentStore;

/**
 *
 * @author kec
 */
@Path("chronicle/sequence")
public class SequenceResource {

    @GET
    @Produces("text/plain")
    public String getSequence() throws IOException {
        return Long.toString(PersistentStore.get().getSequence());
    }
    @GET
    @Path("/last-cancel")
    @Produces("text/plain")
    public String getLastCancel() throws IOException {
        return Long.toString(PersistentStore.get().getLastCancel());
    }
    @GET
    @Path("/last-commit")
    @Produces("text/plain")
    public String getLastCommit() throws IOException {
        return Long.toString(PersistentStore.get().getLastCommit());
    }
    @GET
    @Path("/next")
    @Produces("text/plain")
    public String getNextSequence() throws IOException {
        return Long.toString(PersistentStore.get().incrementAndGetSequence());
    }
}
