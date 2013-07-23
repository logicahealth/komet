package org.ihtsdo.otf.tcc.api.refex.type_nid_int;

import org.ihtsdo.otf.tcc.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_int.RefexIntVersionBI;

public interface RefexNidIntVersionBI <A extends RefexNidIntAnalogBI<A>>
    extends RefexNidVersionBI<A>, RefexIntVersionBI<A> {
     
}
