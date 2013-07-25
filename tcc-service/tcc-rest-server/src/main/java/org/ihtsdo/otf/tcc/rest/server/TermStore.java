/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.otf.tcc.rest.server;

import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 *
 * @author kec
 */
@Path("/termstore")
public class TermStore {
    static {
        BdbSingleton.get();
    }

    @GET
    @Path("/wait-for-writes")
    @Produces("text/plain")
    public String getConceptNid(@PathParam("id") String idStr) throws IOException {
        BdbSingleton.get().waitTillWritesFinished();
        return "OK";
    }
}
