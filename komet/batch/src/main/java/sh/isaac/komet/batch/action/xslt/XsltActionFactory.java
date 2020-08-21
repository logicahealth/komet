package sh.isaac.komet.batch.action.xslt;

import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.observable.coordinate.ObservableManifoldCoordinate;
import sh.isaac.komet.batch.action.ActionFactory;
import sh.isaac.komet.batch.action.ActionItem;

import javax.inject.Singleton;

@Service
@Singleton
public class XsltActionFactory implements ActionFactory {
    public static final String XSLT_TRANSFORM = "XSLT transform";

    @Override
    public String getActionName() {
        return XSLT_TRANSFORM;
    }

    @Override
    public ActionItem makeActionItem(ObservableManifoldCoordinate manifoldForAction) {
        XsltAction action = new XsltAction();
        action.setupForGui(manifoldForAction);
        return action;
    }
}