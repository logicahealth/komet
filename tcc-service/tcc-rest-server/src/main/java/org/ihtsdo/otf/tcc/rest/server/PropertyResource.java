/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.otf.tcc.rest.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

/**
 *
 * @author kec
 */
@Path("/property")
public class PropertyResource {
    static {
        BdbSingleton.get();
    }
        
    @GET
    @Path("")
    @Produces("application/bdb")
    public StreamingOutput getProperties() throws IOException  {
        return new StreamingOutput() {

            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                ObjectOutputStream oos = new ObjectOutputStream(output);
                oos.writeObject(BdbSingleton.get().getProperties());
            }
        };
    }

    @GET
    @Path("{key}")
    @Produces("text/plain")
    public String getProperty(@PathParam("key") String key) throws IOException  {
        return BdbSingleton.get().getProperty(key);
    }

    
}
