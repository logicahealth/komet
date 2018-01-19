package sh.isaac.api.component.semantic.version.brittle;

import sh.isaac.api.component.semantic.version.SemanticVersion;

public interface BrittleVersion extends SemanticVersion {

	public enum BrittleDataTypes {STRING, NID, INTEGER};
	
	/**
	 * Return the type and order of data fields that will be returned by {@link #getDataFields()}
	 * @return
	 */
	public BrittleDataTypes[] getFieldTypes();
	
	/**
	 * Return all data columns in this version, with null padding for blank fields, so that the returned
	 * object array matches in size and type the definition of {@link #getFieldTypes()}
	 * @return
	 */
	public Object[] getDataFields();
}
