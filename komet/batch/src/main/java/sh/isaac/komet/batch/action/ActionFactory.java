package sh.isaac.komet.batch.action;

import javafx.scene.Node;
import org.jvnet.hk2.annotations.Contract;
import sh.komet.gui.manifold.Manifold;

@Contract
public interface ActionFactory {

    default Node getActionIcon() {
        return null;
    }

    String getActionName();

    ActionItem makeActionItem(Manifold manifoldForDisplay);
}
