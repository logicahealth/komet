package sh.isaac.komet.batch.action;

import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.observable.ObservableVersion;

public class UpdateComponentAction extends Action {

    ConceptSpecification newComponent;

    public UpdateComponentAction(int assemblageNid, int fieldNid) {
        super(assemblageNid, fieldNid);
    }
    public UpdateComponentAction() {
        super(TermAux.ANY_ASSEMBLAGE.getNid(), ObservableVersion.PROPERTY_INDEX.PATH.getSpec().getNid());
    }

    public ConceptSpecification getNewComponent() {
        return newComponent;
    }

    public void setNewComponent(ConceptSpecification newComponent) {
        this.newComponent = newComponent;
    }
}
