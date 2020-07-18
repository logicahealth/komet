package sh.isaac.komet.batch.action;

import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.komet.gui.control.property.ViewProperties;

import javax.inject.Singleton;

import static sh.isaac.komet.batch.action.ReplaceAllInExpression.REPLACE_ALL_IN_EXPRESSION;

@Service
@Singleton
public class ReplaceAllInExpressionFactory implements ActionFactory {

    @Override
    public String getActionName() {
        return REPLACE_ALL_IN_EXPRESSION;
    }

    @Override
    public ActionItem makeActionItem(ManifoldCoordinate manifoldForDisplay) {
        ReplaceAllInExpression item = new ReplaceAllInExpression();
        item.setupForGui(manifoldForDisplay);
        return item;
    }
}
