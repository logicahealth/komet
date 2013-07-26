package org.ihtsdo.otf.tcc.api.workflow;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;

public interface WorkflowHandlerBI {
	public Collection<? extends WorkflowHistoryJavaBeanBI> getAvailableWorkflowActions(ConceptVersionBI concept, ViewCoordinate vc) throws IOException, ContradictionException;

	public List<UUID> getAllAvailableWorkflowActionUids(); 

	public boolean hasAction(Collection<? extends WorkflowHistoryJavaBeanBI> beans, ConceptSpec action) throws IOException, ContradictionException;

	public boolean isActiveAction(Collection<? extends WorkflowHistoryJavaBeanBI> possibleActions, UUID action);
}
