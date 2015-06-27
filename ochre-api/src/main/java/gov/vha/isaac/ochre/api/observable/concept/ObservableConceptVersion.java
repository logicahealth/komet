package gov.vha.isaac.ochre.api.observable.concept;

import gov.vha.isaac.ochre.api.chronicle.StampedVersion;
import gov.vha.isaac.ochre.api.observable.ObservableVersion;

/**
 * Created by kec on 6/6/15.
 */
public interface ObservableConceptVersion extends ObservableVersion, StampedVersion {

    @Override
    ObservableConceptChronology<ObservableConceptVersion> getChronology(); 

}
