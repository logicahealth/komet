package sh.isaac.komet.batch.action;

import sh.isaac.api.Status;

public class UpdateStatusAction extends Action {
    Status newStatusValue;

    public UpdateStatusAction(int assemblageNid, int fieldNid, Status newStatusValue) {
        super(assemblageNid, fieldNid);
        this.newStatusValue = newStatusValue;
    }

    @Override
    protected Status getStatusUpdate() {
        return newStatusValue;
    }

    public Status getNewStatusValue() {
        return newStatusValue;
    }

    public void setNewStatusValue(Status newStatusValue) {
        this.newStatusValue = newStatusValue;
    }
}
