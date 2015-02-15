package org.ihtsdo.otf.tcc.api.metadata.binding;

import java.util.UUID;

import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;

public class SnomedRelType {

	public static ConceptSpec FINDING_SITE = 
		new ConceptSpec("Finding site", 
				UUID.fromString("3a6d919d-6c25-3aae-9bc3-983ead83a928"));
	
	public static ConceptSpec IS_A = 
		new ConceptSpec("Is a (attribute)", 
						UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25"));
	
}
