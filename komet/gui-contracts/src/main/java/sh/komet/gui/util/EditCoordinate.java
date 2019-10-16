package sh.komet.gui.util;

import org.apache.commons.lang.Validate;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.observable.coordinate.ObservableEditCoordinate;
import sh.isaac.model.coordinate.EditCoordinateImpl;
import sh.isaac.model.observable.coordinate.ObservableEditCoordinateImpl;

public class EditCoordinate {

    private static final EditCoordinateImpl EDIT_COORDINATE = new EditCoordinateImpl(TermAux.UNINITIALIZED_COMPONENT_ID.getNid(),
            TermAux.UNINITIALIZED_COMPONENT_ID.getNid(),
            TermAux.UNINITIALIZED_COMPONENT_ID.getNid());

    private static final ObservableEditCoordinate OBSERVABLE_EDIT_COORDINATE = new ObservableEditCoordinateImpl(EDIT_COORDINATE);

    public static ObservableEditCoordinate get() {
        Validate.notNull(OBSERVABLE_EDIT_COORDINATE);
        return OBSERVABLE_EDIT_COORDINATE;
    }
}
