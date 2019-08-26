package sh.komet.gui.contract.preferences;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;

import java.util.Collection;
import java.util.List;


public interface WindowPreferencesItem {
    void save();
    void revert();
    StringProperty getWindowName();
    IsaacPreferences getPreferenceNode();
    SimpleDoubleProperty xLocationProperty();
    SimpleDoubleProperty yLocationProperty();
    SimpleDoubleProperty heightProperty();
    SimpleDoubleProperty widthProperty();
    PersonaItem getPersonaItem();
    void setPersonaItem(PersonaItem personaItem);
    ObservableList<ConceptSpecification> getNodesList(int paneIndex);
    SimpleBooleanProperty enableLeftPaneProperty();
    SimpleBooleanProperty enableCenterPaneProperty();
    SimpleBooleanProperty enableRightPaneProperty();
    boolean isPaneEnabled(int paneIndex);
}
