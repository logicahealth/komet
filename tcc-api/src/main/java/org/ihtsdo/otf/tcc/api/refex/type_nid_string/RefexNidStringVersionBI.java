package org.ihtsdo.otf.tcc.api.refex.type_nid_string;

import org.ihtsdo.otf.tcc.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_string.RefexStringVersionBI;

public interface RefexNidStringVersionBI <A extends RefexNidStringAnalogBI<A>>
    extends RefexNidVersionBI<A>, RefexStringVersionBI<A> {
     
}
