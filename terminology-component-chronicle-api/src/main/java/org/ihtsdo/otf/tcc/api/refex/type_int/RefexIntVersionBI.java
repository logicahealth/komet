package org.ihtsdo.otf.tcc.api.refex.type_int;

import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;

public interface RefexIntVersionBI <A extends RefexIntAnalogBI<A>>
        extends RefexVersionBI<A> {
    
     int getInt1();

}
