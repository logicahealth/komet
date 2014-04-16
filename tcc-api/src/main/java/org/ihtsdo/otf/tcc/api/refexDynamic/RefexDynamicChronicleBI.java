package org.ihtsdo.otf.tcc.api.refexDynamic;

import org.ihtsdo.otf.tcc.api.blueprint.RefexDynamicCAB;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicUsageDescription;

/**
 * 
 * {@link RefexDynamicChronicleBI}
 *
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public interface RefexDynamicChronicleBI<A extends RefexDynamicVersionBI<A>> extends ComponentChronicleBI<RefexDynamicVersionBI<A>> {
    /**
     * Assemblage is an assembled collection of objects. Used to identify the Refex that this item is a 
     * member of.  Used instead of RefexExtensionId because of confusion with the component the 
     * Refex Extends, or the ReferencedComponentId.
     * 
     * Note that in the new RefexDynanamicAPI - this linked concept must contain the column definitions 
     * for using this concept as a Refex container.  See the class description of {@link RefexDynamicUsageDescription}
     * for more details on the requirements.
     * 
     * @return the nid that identifies the Refex that this component is a member of.
     */
    public int getAssemblageNid();

    /**
     * @return The nid of the component that is a member of the Refex specified
     *         by {@link #getAssemblageNid()}
     */
    public int getReferencedComponentNid();

    /**
     * A convenience method that reads the concept referenced in
     * {@link #getAssemblageNid()} and returns the actual column
     * information that is contained within that concept.
     */
    public RefexDynamicUsageDescription getRefexUsageDescription();

}
