package org.ihtsdo.otf.tcc.api.refex2;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;

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

}
