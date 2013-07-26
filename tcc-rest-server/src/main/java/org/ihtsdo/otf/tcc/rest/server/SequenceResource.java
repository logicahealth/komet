/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.otf.tcc.rest.server;

import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 *
 * @author kec
 */
@Path("/sequence")
public class SequenceResource {
    static {
        BdbSingleton.get();
    }

    @GET
    @Path("")
    @Produces("text/plain")
    public String getSequence() throws IOException {
        return Long.toString(BdbSingleton.get().getSequence());
    }
    @GET
    @Path("/last-cancel")
    @Produces("text/plain")
    public String getLastCancel() throws IOException {
        return Long.toString(BdbSingleton.get().getLastCancel());
    }
    @GET
    @Path("/last-commit")
    @Produces("text/plain")
    public String getLastCommit() throws IOException {
        return Long.toString(BdbSingleton.get().getLastCommit());
    }
    @GET
    @Path("/next")
    @Produces("text/plain")
    public String getNextSequence() throws IOException {
        return Long.toString(BdbSingleton.get().incrementAndGetSequence());
    }
}
