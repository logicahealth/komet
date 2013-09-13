/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.otf.tcc.rest.server;

import java.io.IOException;
import java.util.UUID;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.ihtsdo.otf.tcc.api.store.Ts;

/**
 *
 * @author kec
 */
@Path("chronicle/uuid")
public class UuidResource {
    
    @GET
    @Path("{uuid}")
    @Produces("text/plain")
    public String getNid(@PathParam("uuid") String uuidStr) throws IOException {
        return Boolean.toString(Ts.get().hasUuid(UUID.fromString(uuidStr)));
    }
    

    @GET
    @Path("primordial/{nid}")
    @Produces("text/plain")
    public String getNid(@PathParam("nid") int nid) throws IOException {
        return Ts.get().getUuidPrimordialForNid(nid).toString();
    }
    
}
