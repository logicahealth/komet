package org.ihtsdo.otf.tcc.api.workflow;

import java.io.IOException;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;

public interface WorkflowHistoryJavaBeanBI {
	public void setConcept(UUID id);
	public void setWorkflowId(UUID id);
	public void setPath(UUID id);
	public void setModeler(UUID id);
	public void setState(UUID id);
	public void setAction(UUID id);
	public void setFSN(String desc);
	public void setEffectiveTime(Long t);
	public void setWorkflowTime(Long t);
	public void setRxMemberId(int id);
	public void setAutoApproved(boolean b);
	public void setOverridden(boolean b);
	
	public UUID getConcept();
	public UUID getWorkflowId();
	public UUID getPath();
	public UUID getModeler();
	public UUID getState();
	public UUID getAction();
	public String getFSN();
	public Long getEffectiveTime();
	public Long getWorkflowTime();
	public boolean getAutoApproved();
	public boolean getOverridden();
	public int getRxMemberId();
	
	public String getStateForTitleBar(ViewCoordinate coordinate) throws IOException;
	public String getModelerForTitleBar(ViewCoordinate coordinate) throws IOException;
	
	public String toString();
}
