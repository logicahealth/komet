package sh.isaac.api.component.semantic.version.brittle;

import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;

public interface BrittleVersion extends SemanticVersion {

	public enum BrittleDataTypes {
		STRING(DynamicDataType.STRING), 
		NID(DynamicDataType.NID), 
		INTEGER(DynamicDataType.INTEGER), 
		FLOAT(DynamicDataType.FLOAT), 
		BOOLEAN(DynamicDataType.BOOLEAN);
	
		private DynamicDataType mapTo;
		
		BrittleDataTypes(DynamicDataType mapTo)
		{
			this.mapTo = mapTo;
		}
		
		public DynamicDataType getDynamicColumnType()
		{
			return mapTo;
		}
	}
	
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
