package gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe;

import java.util.UUID;

import org.jvnet.hk2.annotations.Contract;

@Contract
public interface DynamicSememeColumnUtility {
	/**
	 * A convenience method to read the values that should be used as the name and description for a data 
	 * column in a dynamic sememe from an existing concept
	 * @return an array of two strings, first entry name, seconde entry description
	 */
	public String[] readDynamicSememeColumnNameDescription(UUID columnDescriptionConcept);
}
