package sh.isaac.api.navigation;

import org.jvnet.hk2.annotations.Contract;
import sh.isaac.api.coordinate.ManifoldCoordinate;

@Contract
public interface NavigationService {
    Navigator getNavigator(ManifoldCoordinate mc);
}
