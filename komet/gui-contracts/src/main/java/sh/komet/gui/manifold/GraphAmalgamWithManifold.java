package sh.komet.gui.manifold;

import javafx.beans.property.SimpleObjectProperty;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.tree.TaxonomyAmalgam;

public class GraphAmalgamWithManifold extends TaxonomyAmalgam {
    SimpleObjectProperty<Manifold> manifoldProperty = new SimpleObjectProperty<>();

    public GraphAmalgamWithManifold(ManifoldCoordinate manifoldCoordinate, boolean includeDefiningTaxonomy, Manifold manifold) {
        super(manifoldCoordinate, includeDefiningTaxonomy);
        this.manifoldProperty.set(manifold);
    }

    public Manifold getManifold() {
        return manifoldProperty.get();
    }

    public SimpleObjectProperty<Manifold> manifoldProperty() {
        return manifoldProperty;
    }

    public void setManifold(Manifold manifoldProperty) {
        this.manifoldProperty.set(manifoldProperty);
    }
}
