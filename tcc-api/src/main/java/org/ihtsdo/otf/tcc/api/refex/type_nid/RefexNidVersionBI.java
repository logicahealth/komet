package org.ihtsdo.otf.tcc.api.refex.type_nid;

import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;

public interface RefexNidVersionBI <A extends RefexNidAnalogBI<A>> 
        extends RefexVersionBI<A> {
    
     int getNid1();

}
