package org.ihtsdo.otf.tcc.api.refex.type_boolean;

import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;

public interface RefexBooleanVersionBI <A extends RefexBooleanAnalogBI<A>>
        extends RefexVersionBI<A> {
    
     boolean getBoolean1();

}
