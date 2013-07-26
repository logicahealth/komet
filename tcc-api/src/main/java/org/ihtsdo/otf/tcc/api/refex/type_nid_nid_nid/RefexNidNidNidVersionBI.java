package org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid;

import org.ihtsdo.otf.tcc.api.refex.type_nid_nid.RefexNidNidVersionBI;

public interface RefexNidNidNidVersionBI <A extends RefexNidNidNidAnalogBI<A>> 
    extends RefexNidNidVersionBI<A> {
     int getNid3();

}
