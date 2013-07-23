package org.ihtsdo.otf.tcc.api.refex;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;

public interface RefexChronicleBI<A extends RefexAnalogBI<A>>
        extends ComponentChronicleBI<RefexVersionBI<A>> {
   int getRefexExtensionNid();

   int getReferencedComponentNid();
   
   RefexType getRefexType();
}
