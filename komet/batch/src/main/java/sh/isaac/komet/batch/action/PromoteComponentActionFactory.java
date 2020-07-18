package sh.isaac.komet.batch.action;

import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.komet.gui.control.property.ViewProperties;

import javax.inject.Singleton;

@Service
@Singleton
public class PromoteComponentActionFactory implements ActionFactory {
    public static final String PROMOTE_COMPONENT = "Promote component";

    @Override
    public String getActionName() {
        return PROMOTE_COMPONENT;
    }

    @Override
    public ActionItem makeActionItem(ManifoldCoordinate manifoldForDisplay) {
        PromoteComponentAction action = new PromoteComponentAction();
        action.setupForGui(manifoldForDisplay);
        return action;
    }
}
