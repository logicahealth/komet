
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.ihtsdo.otf.tcc.rest.server;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import java.util.UUID;
import javax.ws.rs.Consumes;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author kec
 */
@Path("/coordinate/view")
public class ViewCoordinateResource {
    static {
        BdbSingleton.get();
    }

    @Context
   UriInfo          uriInfo;

   //~--- methods -------------------------------------------------------------

   @PUT
   @Path("{uuid}")
   @Consumes("application/bdb")
   @Produces(MediaType.TEXT_PLAIN)
   public Response putViewCoordinate(@PathParam("uuid") final String uuidStr, InputStream is)
           throws IOException, ClassNotFoundException {
      ObjectInputStream ois = new ObjectInputStream(is);
      ViewCoordinate    vc  = (ViewCoordinate) ois.readObject();

      BdbSingleton.get().putViewCoordinate(vc);

      return Response.created(uriInfo.getAbsolutePath()).build();
   }

   //~--- get methods ---------------------------------------------------------

   @GET
   @Path("{uuid}")
   @Produces("application/bdb")
   public StreamingOutput getViewCoordinate(@PathParam("uuid") final String uuidStr) throws IOException {
      return new StreamingOutput() {
         @Override
         public void write(OutputStream output) throws IOException, WebApplicationException {
            ObjectOutputStream oos = new ObjectOutputStream(output);

            oos.writeObject(BdbSingleton.get().getViewCoordinate(UUID.fromString(uuidStr)));
         }
      };
   }

   @GET
   @Path("")
   @Produces("application/bdb")
   public StreamingOutput getViewCoordinates() throws IOException {
      return new StreamingOutput() {
         @Override
         public void write(OutputStream output) throws IOException, WebApplicationException {
            ObjectOutputStream oos = new ObjectOutputStream(output);

            oos.writeObject(BdbSingleton.get().getViewCoordinates());
         }
      };
   }
}
