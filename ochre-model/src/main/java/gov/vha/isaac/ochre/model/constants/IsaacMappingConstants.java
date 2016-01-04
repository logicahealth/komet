package gov.vha.isaac.ochre.model.constants;

import java.util.UUID;
import gov.vha.isaac.ochre.api.MetadataConceptConstant;
import gov.vha.isaac.ochre.api.MetadataConceptConstantGroup;
import gov.vha.isaac.ochre.api.MetadataDynamicSememeConstant;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeValidatorType;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeUUIDImpl;

public class IsaacMappingConstants
{
	//This is just used as salt for generating other UUIDs
	public static final MetadataConceptConstant MAPPING_NAMESPACE = new MetadataConceptConstant("mapping namespace", 
		UUID.fromString("9b93f811-7b66-5024-bebf-6a7019743e88"),
		"A concept used to hold the UUID used as the namespace ID generation when creating mappings") {};
		
	public static final MetadataConceptConstant DYNAMIC_SEMEME_COLUMN_MAPPING_PURPOSE = new MetadataConceptConstant("mapping purpose", 
		UUID.fromString("e5de9548-35b9-5e3b-9968-fd9c0a665b51"),
		"Stores the editor stated purpose of the mapping set") {};
		
	//These next 3 don't have to be public - just want the hierarchy created during the DB build
	private static final MetadataConceptConstant broader = new MetadataConceptConstant("Broader Than", 
		UUID.fromString("c1068428-a986-5c12-9583-9b2d3a24fdc6")) {};
	
	private static final MetadataConceptConstant exact = new MetadataConceptConstant("Exact", 
		UUID.fromString("8aa6421d-4966-5230-ae5f-aca96ee9c2c1")) {};
	
	private static final MetadataConceptConstant narrower = new MetadataConceptConstant("Narrower Than", 
		UUID.fromString("250d3a08-4f28-5127-8758-e8df4947f89c")) {};
		
	public static final MetadataConceptConstantGroup MAPPING_QUALIFIERS = new MetadataConceptConstantGroup("mapping qualifiers", 
		UUID.fromString("83204ca8-bd51-530c-af04-5edbec04a7c6"), 
		"Stores the editor selected mapping qualifier") 
		{
			{
				addChild(broader);
				addChild(exact);
				addChild(narrower);
			}
		};
		
	//These next two don't need to be public, just want them in the hierarchy
	private static final MetadataConceptConstant pending = new MetadataConceptConstant("Pending", 
		UUID.fromString("d481125e-b8ca-537c-b688-d09d626e5ff9")) {};
		
	private static final MetadataConceptConstant reviewed = new MetadataConceptConstant("Reviewed", 
		UUID.fromString("45b49b0d-e2d2-5a27-a08d-8f79856b6307")) {};
		
	public static final MetadataConceptConstantGroup MAPPING_STATUS = new MetadataConceptConstantGroup("mapping status type", 
		UUID.fromString("f4523b36-3714-5d0e-999b-edb8f21dc0fa"), 
		"Stores the editor selected status of the mapping set or mapping instance") 
		{
			{
				addChild(pending);
				addChild(reviewed);
			}
		};
	public static final MetadataConceptConstantGroup MAPPING_METADATA = new MetadataConceptConstantGroup("mapping metadata", 
		UUID.fromString("9b5de306-e582-58e3-a23a-0dbf49cbdfe7")) 
		{
			{
				addChild(MAPPING_NAMESPACE);
				addChild(MAPPING_QUALIFIERS);
				addChild(MAPPING_STATUS);
			}
		};
		
	public static final MetadataDynamicSememeConstant DYNAMIC_SEMEME_MAPPING_SEMEME_TYPE = new MetadataDynamicSememeConstant("Mapping Sememe Type", 
		UUID.fromString("aa4c75a1-fc69-51c9-88dc-a1a1c7f84e01"),
		"A Sememe used to specify how user-created mapping Sememes are structured", 
			new DynamicSememeColumnInfo[] {
				new DynamicSememeColumnInfo(0, MAPPING_STATUS.getUUID(), DynamicSememeDataType.UUID, null, false, 
					new DynamicSememeValidatorType[] {DynamicSememeValidatorType.IS_KIND_OF},
					new DynamicSememeUUIDImpl[] {new DynamicSememeUUIDImpl(MAPPING_STATUS.getUUID())}),
				new DynamicSememeColumnInfo(1, DYNAMIC_SEMEME_COLUMN_MAPPING_PURPOSE.getUUID(), DynamicSememeDataType.STRING, 
						null, false)},
			null) {}; 
}
