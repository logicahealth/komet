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
import org.ihtsdo.otf.tcc.model.cc.PersistentStore;

/**
 *
 * @author kec
 */
@Path("chronicle/termstore")
public class TermStore {

    @GET
    @Path("/wait-for-writes")
    @Produces("text/plain")
    public String waitForWrites(@PathParam("id") String idStr) throws IOException {
        PersistentStore.get().waitTillWritesFinished();
        return "OK";
    }
}
