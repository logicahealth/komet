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
@Path("chronicle/stamp")
public class StampResource {

    @GET
    @Path("/read-only-max")
    @Produces("text/plain")
    public String getRoMaxStamp() throws IOException {
        return Integer.toString(PersistentStore.get().getMaxReadOnlyStamp());
    }
    @GET
    @Path("/time/{id}")
    @Produces("text/plain")
    public String getTime(@PathParam("id") String id) throws IOException {
        return Long.toString(PersistentStore.get().getTimeForStamp(Integer.parseInt(id)));
    }
    @GET
    @Path("/path/{id}")
    @Produces("text/plain")
    public String getPathNid(@PathParam("id") String id) throws IOException {
        return Integer.toString(PersistentStore.get().getPathNidForStamp(Integer.parseInt(id)));
    }
    @GET
    @Path("/author/{id}")
    @Produces("text/plain")
    public String getAuthorNid(@PathParam("id") String id) throws IOException {
        return Integer.toString(PersistentStore.get().getAuthorNidForStamp(Integer.parseInt(id)));
    }
    @GET
    @Path("/status/{id}")
    @Produces("text/plain")
    public String getStatus(@PathParam("id") String id) throws IOException {
        return PersistentStore.get().getStatusForStamp(Integer.parseInt(id)).name();
    }
    @GET
    @Path("/module/{id}")
    @Produces("text/plain")
    public String getModuleNid(@PathParam("id") String id) throws IOException {
        return Integer.toString(PersistentStore.get().getModuleNidForStamp(Integer.parseInt(id)));
    }
}
