package sh.isaac.api.observable.semantic.version.brittle;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import sh.isaac.api.component.semantic.version.brittle.Nid1_Long2_Version;
import sh.isaac.api.observable.semantic.version.ObservableSemanticVersion;

/**
 *
 * @author kec
 */
public interface Observable_Nid1_Long2_Version
        extends ObservableSemanticVersion, Nid1_Long2_Version {
    IntegerProperty nid1Property();
    LongProperty long2Property();

}
