package sh.isaac.komet.batch.action;

import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.komet.gui.control.property.ViewProperties;

import javax.inject.Singleton;

import static sh.isaac.komet.batch.action.InactivateComponent.INACTIVATE_COMPONENT;


@Service
@Singleton
public class InactivateComponentFactory implements ActionFactory {

    @Override
    public String getActionName() {
        return INACTIVATE_COMPONENT;
    }

    @Override
    public ActionItem makeActionItem(ManifoldCoordinate manifoldForDisplay) {
        InactivateComponent item = new InactivateComponent();
        item.setupForGui(manifoldForDisplay);
        return item;
    }
}

