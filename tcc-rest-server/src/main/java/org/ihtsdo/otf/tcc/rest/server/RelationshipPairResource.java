
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import org.ihtsdo.otf.tcc.api.nid.NidSet;

/**
 *
 * @author kec
 */
@Path("/relationship")
public class RelationshipPairResource {
    static {
        BdbSingleton.get();
    }
   //~--- get methods ---------------------------------------------------------

   @GET
   @Path("origin/{nid}")
   @Produces("application/bdb")
   public StreamingOutput getDestinationRelOriginNids(@PathParam("nid") final int nid) throws IOException {
      return new StreamingOutput() {
         @Override
         public void write(OutputStream output) throws IOException, WebApplicationException {
            ObjectOutputStream oos = new ObjectOutputStream(output);

            oos.writeObject(BdbSingleton.get().getDestRelOriginNids(nid));
         }
      };
   }

   @GET
   @Path("origin/{nid}/typed")
   @Produces("application/bdb")
   public StreamingOutput getDestinationRelOriginNidsOfType(@PathParam("nid") final int nid,
           @QueryParam("start") final NidSet relTypes)
           throws IOException {
      return new StreamingOutput() {
         @Override
         public void write(OutputStream output) throws IOException, WebApplicationException {
            ObjectOutputStream oos = new ObjectOutputStream(output);

            oos.writeObject(BdbSingleton.get().getDestRelOriginNids(nid, relTypes));
         }
      };
   }
}
