package org.ihtsdo.otf.tcc.chronicle.cc.concept;


public interface I_BindConceptComponents {

	public ConceptChronicle getEnclosingConcept();

	public void setupBinder(ConceptChronicle enclosingConcept);

}