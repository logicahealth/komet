package org.ihtsdo.otf.tcc.api.metadata.binding;

import java.util.UUID;

import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;

public class Taxonomies {

	public static ConceptSpec WB_AUX = 
		new ConceptSpec("Terminology Auxiliary concept", 
				UUID.fromString("f4d2fabc-7e96-3b3a-a348-ae867ba74029"));
	
	public static ConceptSpec REFSET_AUX = 
		new ConceptSpec("Refset Auxiliary Concept", 
				UUID.fromString("1c698388-c309-3dfa-96f0-86248753fac5"));
	
	public static ConceptSpec QUEUE_TYPE = 
		new ConceptSpec("Queue Type", 
				UUID.fromString("fb78d89e-6953-3456-8903-8ee9d25539bc"));
	
	public static ConceptSpec SNOMED = 
		new ConceptSpec("SNOMED CT Concept", 
				         UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"));

        public static ConceptSpec SNOMED_ROLE_ROOT = 
		new ConceptSpec("Concept model attribute (attribute)", 
				         UUID.fromString("6155818b-09ed-388e-82ce-caa143423e99"));
        
}
