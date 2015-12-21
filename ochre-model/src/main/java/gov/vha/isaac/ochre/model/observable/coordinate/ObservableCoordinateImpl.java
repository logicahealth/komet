package gov.vha.isaac.ochre.model.observable.coordinate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kec on 7/24/15.
 */
public class ObservableCoordinateImpl {
    private final List<Object> listenerReferences = new ArrayList<>();
    protected void addListenerReference(Object listener) {
        listenerReferences.add(listener);
    }

}
