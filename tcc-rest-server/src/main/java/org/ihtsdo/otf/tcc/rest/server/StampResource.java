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
@Path("/stamp")
public class StampResource {
    static {
        BdbSingleton.get();
    }

    @GET
    @Path("/read-only-max")
    @Produces("text/plain")
    public String getRoMaxStamp() throws IOException {
        return Integer.toString(BdbSingleton.get().getMaxReadOnlyStamp());
    }
    @GET
    @Path("/time/{id}")
    @Produces("text/plain")
    public String getTime(@PathParam("id") String id) throws IOException {
        return Long.toString(BdbSingleton.get().getTimeForStamp(Integer.parseInt(id)));
    }
    @GET
    @Path("/path/{id}")
    @Produces("text/plain")
    public String getPathNid(@PathParam("id") String id) throws IOException {
        return Integer.toString(BdbSingleton.get().getPathNidForStamp(Integer.parseInt(id)));
    }
    @GET
    @Path("/author/{id}")
    @Produces("text/plain")
    public String getAuthorNid(@PathParam("id") String id) throws IOException {
        return Integer.toString(BdbSingleton.get().getAuthorNidForStamp(Integer.parseInt(id)));
    }
    @GET
    @Path("/status/{id}")
    @Produces("text/plain")
    public String getStatus(@PathParam("id") String id) throws IOException {
        return BdbSingleton.get().getStatusForStamp(Integer.parseInt(id)).name();
    }
    @GET
    @Path("/module/{id}")
    @Produces("text/plain")
    public String getModuleNid(@PathParam("id") String id) throws IOException {
        return Integer.toString(BdbSingleton.get().getModuleNidForStamp(Integer.parseInt(id)));
    }
}
