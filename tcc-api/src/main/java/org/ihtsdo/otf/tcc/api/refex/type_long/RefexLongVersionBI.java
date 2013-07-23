package org.ihtsdo.otf.tcc.api.refex.type_long;

import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;

public interface RefexLongVersionBI <A extends RefexLongAnalogBI<A>>
        extends RefexVersionBI<A> {
    
     long getLong1();

}
