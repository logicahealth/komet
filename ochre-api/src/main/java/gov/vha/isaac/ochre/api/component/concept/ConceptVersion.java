package gov.vha.isaac.ochre.api.component.concept;

import gov.vha.isaac.ochre.api.chronicle.StampedVersion;

/**
 * Created by kec on 6/6/15.
 * @param <V>
 */
public interface ConceptVersion<V extends ConceptVersion> extends StampedVersion {

    ConceptChronology<V> getChronology();

}
