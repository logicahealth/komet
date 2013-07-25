package org.ihtsdo.otf.tcc.api.nid;

import java.io.IOException;

public interface NativeIdSetItrBI {
    /**
     * Returns the current native id.
     * <p>
     * This is invalid until {@link #next()} is called for the first time.
     */
    public int nid();

    /**
     * Moves to the next identifier in the set. Returns true, iff
     * there is such a nid.
     */
    public boolean next() throws IOException;

}
