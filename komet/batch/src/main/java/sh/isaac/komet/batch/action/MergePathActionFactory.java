package sh.isaac.komet.batch.action;

import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.observable.coordinate.ObservableManifoldCoordinate;

import jakarta.inject.Singleton;

@Service
@Singleton
public class MergePathActionFactory implements ActionFactory {
    public static final String MERGE_PATH = "Merge path";

    @Override
    public String getActionName() {
        return MERGE_PATH;
    }

    @Override
    public ActionItem makeActionItem(ObservableManifoldCoordinate manifoldForAction) {
        MergePath action = new MergePath();
        action.setupForGui(manifoldForAction);
        return action;
    }
}
