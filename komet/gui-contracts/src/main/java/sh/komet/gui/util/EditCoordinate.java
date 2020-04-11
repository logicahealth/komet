package sh.komet.gui.util;

import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.coordinate.EditCoordinateImmutable;
import sh.isaac.api.observable.coordinate.ObservableEditCoordinate;
import sh.isaac.model.observable.coordinate.ObservableEditCoordinateImpl;

public class EditCoordinate {

    private static final EditCoordinateImmutable EDIT_COORDINATE = EditCoordinateImmutable.make(TermAux.UNINITIALIZED_COMPONENT_ID.getNid(),
            TermAux.UNINITIALIZED_COMPONENT_ID.getNid(),
            TermAux.UNINITIALIZED_COMPONENT_ID.getNid());

    private static final ObservableEditCoordinate OBSERVABLE_EDIT_COORDINATE = new ObservableEditCoordinateImpl(EDIT_COORDINATE);

    public static ObservableEditCoordinate get() {
        return OBSERVABLE_EDIT_COORDINATE;
    }
}
