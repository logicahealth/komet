package org.ihtsdo.otf.tcc.api.refex;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;

public interface RefexChronicleBI<A extends RefexAnalogBI<A>>
        extends ComponentChronicleBI<RefexVersionBI<A>> {

    /**
     * Assemblage an assembled collection of objects. Used instead of
     * RefexExtensionId because of confusion with the component the Refex
     * extends, or the ReferencedComponentId.
     * @return the nid that identifies the Assemblage. 
     */
    int getAssemblageNid();

    /**
     *
     * @return 
     * @deprecated use getAssemblageNid()
     */
    @Deprecated
    int getRefexExtensionNid();

    int getReferencedComponentNid();

    RefexType getRefexType();
}
