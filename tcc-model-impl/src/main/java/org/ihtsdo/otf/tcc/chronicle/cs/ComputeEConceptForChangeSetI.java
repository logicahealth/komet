package org.ihtsdo.otf.tcc.chronicle;

import java.io.IOException;

import org.ihtsdo.otf.tcc.chronicle.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;

public interface ComputeEConceptForChangeSetI {

	public TtkConceptChronicle getEConcept(ConceptChronicle c) throws IOException;

}