package sh.isaac.api.observable.coordinate;

import sh.isaac.api.coordinate.StampFilterTemplateImmutable;
import sh.isaac.api.coordinate.StampFilterTemplateProxy;

public interface ObservableStampFilterTemplate
        extends StampFilterTemplateProxy, ObservableCoordinate<StampFilterTemplateImmutable>, StampFilterTemplateProperties {

}
