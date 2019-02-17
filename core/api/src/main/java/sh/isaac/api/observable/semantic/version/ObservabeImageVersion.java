package sh.isaac.api.observable.semantic.version;

import javafx.beans.property.ObjectProperty;
import sh.isaac.api.component.semantic.version.MutableImageVersion;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;

public interface ObservabeImageVersion extends ObservableSemanticVersion, MutableImageVersion {

    ObjectProperty<byte[]> imageDataProperty();

    @Override
    public ObservableSemanticChronology getChronology();

}
