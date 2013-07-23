package org.ihtsdo.otf.tcc.api.refex.type_float;

import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;

public interface RefexFloatVersionBI <A extends RefexFloatAnalogBI<A>>
        extends RefexVersionBI<A> {
    
     float getFloat1();

}
