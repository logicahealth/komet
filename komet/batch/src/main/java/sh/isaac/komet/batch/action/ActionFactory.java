package sh.isaac.komet.batch.action;

import javafx.scene.Node;
import org.jvnet.hk2.annotations.Contract;
import sh.isaac.api.observable.coordinate.ObservableManifoldCoordinate;

@Contract
public interface ActionFactory {

    default Node getActionIcon() {
        return null;
    }

    String getActionName();

    ActionItem makeActionItem(ObservableManifoldCoordinate manifoldForDisplay);
}
