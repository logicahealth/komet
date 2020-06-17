package sh.isaac.model.observable.coordinate;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StampPath;
import sh.isaac.api.coordinate.StampPositionImmutable;
import sh.isaac.api.observable.coordinate.ObservableStampPath;
import sh.isaac.model.observable.override.ListPropertyWithOverride;
import sh.isaac.model.observable.override.ObjectPropertyWithOverride;
import sh.isaac.model.observable.override.SetPropertyWithOverride;

public class ObservableStampPathWithOverride
        extends ObservableStampPathBase {

    //~--- constructors --------------------------------------------------------

    private ObservableStampPathWithOverride(ObservableStampPath stampPath, String coordinateName) {
        super(stampPath, coordinateName);
    }

    private ObservableStampPathWithOverride(ObservableStampPath stampPath) {
        super(stampPath, "Overridden stamp path");
    }

    public static ObservableStampPathWithOverride make(ObservableStampPath stampPath) {
        return new ObservableStampPathWithOverride(stampPath);
    }

    @Override
    protected ListProperty<StampPositionImmutable> makePathOriginsAsListProperty(StampPath stampPath) {
        ObservableStampPath stampPathObservable = (ObservableStampPath) stampPath;
        return new ListPropertyWithOverride<>(stampPathObservable.pathOriginsAsListPropertyProperty(), this);
    }

    @Override
    protected SetProperty<StampPositionImmutable> makePathOriginsProperty(StampPath stampPath) {
        ObservableStampPath stampPathObservable = (ObservableStampPath) stampPath;
        return new SetPropertyWithOverride<>(stampPathObservable.pathOriginsProperty(), this);
    }

    @Override
    protected ObjectProperty<ConceptSpecification> makePathConceptProperty(StampPath stampPath) {
        ObservableStampPath stampPathObservable = (ObservableStampPath) stampPath;
        return new ObjectPropertyWithOverride<>(stampPathObservable.pathConceptProperty(), this);
    }
}
