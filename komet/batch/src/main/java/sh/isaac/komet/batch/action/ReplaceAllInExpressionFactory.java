package sh.isaac.komet.batch.action;

import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.observable.coordinate.ObservableManifoldCoordinate;

import jakarta.inject.Singleton;

import static sh.isaac.komet.batch.action.ReplaceAllInExpression.REPLACE_ALL_IN_EXPRESSION;

@Service
@Singleton
public class ReplaceAllInExpressionFactory implements ActionFactory {

    @Override
    public String getActionName() {
        return REPLACE_ALL_IN_EXPRESSION;
    }

    @Override
    public ActionItem makeActionItem(ObservableManifoldCoordinate manifoldForDisplay) {
        ReplaceAllInExpression item = new ReplaceAllInExpression();
        item.setupForGui(manifoldForDisplay);
        return item;
    }
}
