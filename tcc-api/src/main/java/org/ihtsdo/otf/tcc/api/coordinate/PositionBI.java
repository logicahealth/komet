package org.ihtsdo.otf.tcc.api.coordinate;

import java.util.Collection;
import java.util.Map;

public interface PositionBI {

    PathBI getPath();

    long getTime();

    boolean isSubsequentOrEqualTo(long time, int pathNid);

    boolean isAntecedentOrEqualTo(long time, int pathNid);

    boolean isAntecedentOrEqualTo(PositionBI another);

    boolean checkAntecedentOrEqualToOrigins(Collection<? extends PositionBI> origins);

    boolean isSubsequentOrEqualTo(PositionBI another);

    boolean equals(long time, int pathNid);

    @Deprecated
    Collection<? extends PositionBI> getAllOrigins();
    
    /**
     * Need to also enable "blocking" at positions... add getBarriers() ? that 
     * returns Collection<? extends PositionBI> , 
     * Need to support retirement of an intersection (standard refex
     * retirement?)
     * @return 
     */
    Map<Long, ? extends PositionBI> getIntersections();
    
    Collection<? extends PositionBI> getBarriers();

}
