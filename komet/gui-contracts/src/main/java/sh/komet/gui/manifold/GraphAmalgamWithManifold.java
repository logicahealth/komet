package sh.komet.gui.manifold;

import javafx.beans.property.SimpleObjectProperty;
import sh.isaac.api.observable.coordinate.ObservableManifoldCoordinate;
import sh.isaac.api.tree.TaxonomyAmalgam;

public class GraphAmalgamWithManifold extends TaxonomyAmalgam {

    SimpleObjectProperty<ObservableManifoldCoordinate> observableManifoldCoordinateProperty = new SimpleObjectProperty<>();
    public GraphAmalgamWithManifold(ObservableManifoldCoordinate manifoldCoordinate, boolean includeDefiningTaxonomy) {
        super(manifoldCoordinate, includeDefiningTaxonomy);
        observableManifoldCoordinateProperty.set(manifoldCoordinate);
    }

    public ObservableManifoldCoordinate getObservableManifoldCoordinate() {
        return observableManifoldCoordinateProperty.get();
    }

    public SimpleObjectProperty<ObservableManifoldCoordinate> observableManifoldCoordinateProperty() {
        return observableManifoldCoordinateProperty;
    }

    public void setObservableManifoldCoordinate(ObservableManifoldCoordinate observableManifoldCoordinate) {
        this.observableManifoldCoordinateProperty.set(observableManifoldCoordinate);
    }
}
