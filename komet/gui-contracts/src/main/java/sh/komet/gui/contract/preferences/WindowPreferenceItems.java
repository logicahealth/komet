package sh.komet.gui.contract.preferences;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;

import java.util.List;


public interface WindowPreferenceItems {
    void save();
    void revert();
    StringProperty getWindowName();
    IsaacPreferences getPreferenceNode();
    SimpleDoubleProperty xLocationProperty();
    SimpleDoubleProperty yLocationProperty();
    SimpleDoubleProperty heightProperty();
    SimpleDoubleProperty widthProperty();

}
