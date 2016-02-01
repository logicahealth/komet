package gov.vha.isaac.ochre.api.constants;

import org.jvnet.hk2.annotations.Contract;

/**
 * ISAAC module level code can implement this class, and annotate with HK2 as a service, 
 * in order to have their constants automatically generated into the DB by the mojo by the 
 * {@link ExportTaxonomy} mojo
 * 
 *
 */

@Contract
public interface ModuleProvidedConstants {

	/**
	 * When providing concepts for this method, any top-level concept returned should have specified a parent 
	 * via a setParent(..) call.  Otherwise, it will be attached to the ISAAC root concept.
	 * 
	 * Concepts that are nested under a {@link MetadataConceptConstantGroup} will be created relative to the concept
	 * created at the top of the group (which should have setParent(...) specified)
	 * 
	 * DO NOT make a reference to the LookupService in a variable this is statically defined - this will break the 
	 * HK2 init routine!
	 * @return
	 */
	public MetadataConceptConstant[] getConstantsToCreate();
}
