package org.ihtsdo.otf.tcc.api.refex2;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex2.data.RefexDataBI;

/**
 * 
 * {@link RefexChronicleBI}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public interface RefexChronicleBI<A extends RefexAnalogBI<A>> extends ComponentChronicleBI<RefexVersionBI<A>>
{
	/**
	 * Assemblage an assembled collection of objects. Used to identify the Refex that this item is a member of.
	 * Used instead of RefexExtensionId because of confusion with the component the Refex extends, or the ReferencedComponentId.
	 * 
	 * @return the nid that identifies the Refex that this component is a member of.
	 */
	int getAssemblageNid();

	/**
	 * @return The nid of the component that is a member of the Refex specified by {@link #getAssemblageNid()}
	 */
	int getReferencedComponentNid();

	/**
	 *
	 * @return
	 * @deprecated use getAssemblageNid()
	 */
	@Deprecated
	int getRefexExtensionNid();

	RefexType getRefexType();

	/**
	 * @return The nid of the component that defines the columns of data used in this Refex, and provides a description
	 * for the purpose of this Refex linkage.
	 * See {@link #getRefexUsageDescription()} for a much more useful (but more expensive) convenience method.
	 */
	int getRefexUsageDescriptorNid();

	/**
	 * A convenience method that reads the concept referenced in {@link #getColumnDescriptorNid()} and
	 * returns the actual column information that is contained within that concept.
	 * 
	 * @return
	 */
	RefexUsageDescriptionBI getRefexUsageDescription();

	/**
	 * @return All of the data columns that are part of this Refex. See {@link #getData(int)}.
	 * May be empty, will not be null.
	 */
	RefexDataBI[] getData();

	/**
	 * The type and data (if any) in the specified column of the Refex.
	 * 
	 * @param columnNumber
	 * @return The RefexMemberBI which contains the type and data (if any) for the specified column
	 * @throws IndexOutOfBoundsException
	 */
	RefexDataBI getData(int columnNumber) throws IndexOutOfBoundsException;
	
}
