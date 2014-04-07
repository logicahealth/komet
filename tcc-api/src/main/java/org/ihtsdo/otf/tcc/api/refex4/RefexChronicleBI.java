package org.ihtsdo.otf.tcc.api.refex4;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.refex4.data.RefexUsageDescriptionBI;

/**
 * 
 * {@link RefexChronicleBI}
 *
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public interface RefexChronicleBI<A extends RefexVersionBI<A>> extends ComponentChronicleBI<RefexVersionBI<A>> {
    /**
     * Assemblage an assembled collection of objects. Used to identify the Refex
     * that this item is a member of. Used instead of RefexExtensionId because
     * of confusion with the component the Refex extends, or the
     * ReferencedComponentId.
     * 
     * Note that in the new RefexAPI - this linked concept must contain the column definitions 
     * for using this concept as a Refex container.  The linked concept must define the combination of data
     * columns being used within this Refex. The referenced concept must contains a Refex extension of 
     * RefexDataColumn //TODO <concept> where the attached data is [int, String, String] where the int value is used to 
     * align the order with the data here, and the two string columns are used for the name and
     * description values of the column.
     * 
     * The referenced concept should also contain a description of type //TODO determine where the description contains 
     * the overall purpose of this Refex.
     * 
     * @return the nid that identifies the Refex that this component is a member
     *         of.
     */
    int getAssemblageNid();

    /**
     * @return The nid of the component that is a member of the Refex specified
     *         by {@link #getAssemblageNid()}
     */
    int getReferencedComponentNid();

    /**
     * A convenience method that reads the concept referenced in
     * {@link #getAssemblageNid()} and returns the actual column
     * information that is contained within that concept.
     * 
     * @return
     */
    RefexUsageDescriptionBI getRefexUsageDescription();

}
