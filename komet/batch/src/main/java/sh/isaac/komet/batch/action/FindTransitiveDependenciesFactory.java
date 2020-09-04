package sh.isaac.komet.batch.action;

import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.observable.coordinate.ObservableManifoldCoordinate;

import jakarta.inject.Singleton;

@Service
@Singleton
public class FindTransitiveDependenciesFactory implements ActionFactory {

    public static final String FIND_TRANSITIVE_DEPENDENCIES = "Find transitive dependencies";

    @Override
    public String getActionName() {
        return FIND_TRANSITIVE_DEPENDENCIES;
    }

    @Override
    public ActionItem makeActionItem(ObservableManifoldCoordinate manifoldForDisplay) {
        FindTransitiveDependencies item = new FindTransitiveDependencies();
        item.setupForGui(manifoldForDisplay);
        return item;
    }
}