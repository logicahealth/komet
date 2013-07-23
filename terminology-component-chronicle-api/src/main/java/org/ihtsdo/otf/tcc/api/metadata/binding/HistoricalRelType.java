package org.ihtsdo.otf.tcc.api.metadata.binding;

import java.util.UUID;

import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;

public class HistoricalRelType {

	public static ConceptSpec MAY_BE_A = 
		new ConceptSpec("MAY BE A (attribute)", 
						UUID.fromString("721dadc2-53a0-3ffa-8abd-80ff6aa87db2"));
	
	public static ConceptSpec MOVED_TO = 
		new ConceptSpec("MOVED TO (attribute)", 
						UUID.fromString("c3394436-568c-327a-9d20-4a258d65a936"));
	
	public static ConceptSpec REPLACED_BY = 
		new ConceptSpec("REPLACED BY (attribute)", 
						UUID.fromString("0b010f24-523b-3ae4-b3a2-ec1f425c8a85"));
	
	public static ConceptSpec SAME_AS = 
		new ConceptSpec("SAME AS (attribute)", 
						UUID.fromString("87594159-50f0-3b5f-aa4f-f6061c0ce497"));
	
	public static ConceptSpec WAS_A = 
		new ConceptSpec("WAS A (attribute)", 
						UUID.fromString("a1a598c0-7988-3c8e-9ba2-342f24de7c6b"));
	
	public static ConceptSpec[] getHistoricalTypes(){
		
	ConceptSpec[] historicalTypes = new ConceptSpec [5];
	
	historicalTypes[0] = MAY_BE_A;
	historicalTypes[1] = MOVED_TO;
	historicalTypes[2] = REPLACED_BY;
	historicalTypes[3] = SAME_AS;
	historicalTypes[4] = WAS_A;
	
	return historicalTypes;
	
	}
	
}
