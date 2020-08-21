package sh.isaac.komet.batch.action;

import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.observable.coordinate.ObservableManifoldCoordinate;

import javax.inject.Singleton;

@Service
@Singleton
public class FilterRf2ZipFactory implements ActionFactory {
    public static final String FILTER_ZIP = "Filter RF2 zip file";

    @Override
    public String getActionName() {
        return FILTER_ZIP;
    }

    @Override
    public ActionItem makeActionItem(ObservableManifoldCoordinate manifoldForAction) {
        FilterRf2Zip action = new FilterRf2Zip();
        action.setupForGui(manifoldForAction);
        return action;
    }
}