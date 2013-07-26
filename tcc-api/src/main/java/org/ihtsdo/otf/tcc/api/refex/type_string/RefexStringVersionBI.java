package org.ihtsdo.otf.tcc.api.refex.type_string;

import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;

public interface RefexStringVersionBI <A extends RefexStringAnalogBI<A>>
        extends RefexVersionBI<A> {
    
     String getString1();

}
