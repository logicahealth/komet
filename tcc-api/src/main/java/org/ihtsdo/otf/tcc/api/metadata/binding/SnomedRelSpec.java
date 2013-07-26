package org.ihtsdo.otf.tcc.api.metadata.binding;

import org.ihtsdo.otf.tcc.api.spec.RelSpec;

public class SnomedRelSpec {
	public static RelSpec FINDING_SITE = 
		new RelSpec(Taxonomies.SNOMED, 
					SnomedRelType.FINDING_SITE, 
					Snomed.BODY_STRUCTURE);

}
