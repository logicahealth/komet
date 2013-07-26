/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.otf.tcc.rest.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.UUID;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import org.ihtsdo.otf.tcc.api.coordinate.PathBI;

/**
 *
 * @author kec
 */
@Path("/path")
public class PathResource {
    static {
        BdbSingleton.get();
    }
    

    @GET
    @Path("{id}")
    @Produces("application/bdb")
    public StreamingOutput getPathByteArray(@PathParam("id") String id) throws IOException {
        final int pathNid;
        if (id.length() == 36) {
            UUID uuid = UUID.fromString(id);
            pathNid = BdbSingleton.get().getNidForUuids(uuid);
        } else {
            pathNid = Integer.parseInt(id);
        }
        return new StreamingOutput() {

            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                PathBI path = BdbSingleton.get().getPath(pathNid);
                ObjectOutputStream oos = new ObjectOutputStream(output);
                oos.writeObject(path);
            }
        };
    }
}
