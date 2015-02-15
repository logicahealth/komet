package org.ihtsdo.otf.tcc.model.cs;

import java.io.IOException;

import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;

public interface ComputeEConceptForChangeSetI {

	public TtkConceptChronicle getEConcept(ConceptChronicle c) throws IOException;

}