package org.ihtsdo.otf.tcc.api.coordinate;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface PathBI {
   public String toHtmlString() throws IOException;

   //~--- get methods ---------------------------------------------------------

   public int getConceptNid();

   /**
    * Get all origins and origin of origins, etc., for this path.
    */
   public Set<? extends PositionBI> getInheritedOrigins();

   public PathBI getMatchingPath(int pathNid);

   /**
    * Similar to {@link #getInheritedOrigins()} however superseded origins
    * (where there is more than one origin for the same path but with an
    * earlier version) will be excluded.
    */
   public Set<? extends PositionBI> getNormalisedOrigins();

   public Collection<? extends PositionBI> getOrigins();

   public List<UUID> getUUIDs();
}
