package org.ihtsdo.otf.tcc.api.metadata.binding;

import java.util.UUID;

import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;

public class ConceptInactivationType {
	
	public static ConceptSpec AMBIGUOUS_CONCEPT = 
		new ConceptSpec("Ambiguous concept (inactive concept)", 
						UUID.fromString("5adbed85-55d8-3304-a404-4bebab660fff"));
	
	public static ConceptSpec DUPLICATE_CONCEPT = 
		new ConceptSpec("Duplicate concept (inactive concept)", 
						UUID.fromString("a5db42d4-6d94-33b7-92e7-d4a1d0f0d814"));
	
	public static ConceptSpec ERRONEOUS_CONCEPT = 
		new ConceptSpec("Erroneous concept (inactive concept)", 
						UUID.fromString("d4227098-db7a-331e-8f00-9d1e27626fc5"));
	
	public static ConceptSpec LIMITED_STATUS_CONCEPT = 
		new ConceptSpec("Limited status concept (inactive concept)", 
						UUID.fromString("0c7b717a-3e41-372b-be57-621befb9b3ee"));
	
	public static ConceptSpec MOVED_ELSEWHERE = 
		new ConceptSpec("Moved elsewhere (inactive concept)", 
						UUID.fromString("e730d11f-e155-3482-a423-9637db3bc1a2"));
	
	public static ConceptSpec OUTDATED_CONCEPT = 
		new ConceptSpec("Outdated concept (inactive concept)", 
						UUID.fromString("d8a42cc5-05dd-3fcf-a1f7-62856e38874a"));
	
	public static ConceptSpec REASON_NOT_STATED_CONCEPT = 
		new ConceptSpec("Reason not stated concept (inactive concept)", 
						UUID.fromString("a0db7e17-c6b2-3acc-811d-8a523274e869"));
}

