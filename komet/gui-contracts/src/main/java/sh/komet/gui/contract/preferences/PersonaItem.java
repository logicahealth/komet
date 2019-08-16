package sh.komet.gui.contract.preferences;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import sh.isaac.api.component.concept.ConceptSpecification;

import java.util.UUID;

/**
 * 2019-07-22
 * aks8m - https://github.com/aks8m
 */
public interface PersonaItem {
    void save();
    void revert();

    UUID getPersonaUuid();

    SimpleStringProperty nameProperty();

    SimpleBooleanProperty enableLeftPaneProperty();

    SimpleBooleanProperty enableCenterPaneProperty();

    SimpleBooleanProperty enableRightPaneProperty();

    SimpleListProperty<ConceptSpecification> leftPaneOptionsProperty();

    SimpleListProperty<ConceptSpecification> centerPaneOptionsProperty();

    SimpleListProperty<ConceptSpecification> rightPaneOptionProperty();

    SimpleListProperty<ConceptSpecification> leftPaneDefaultsProperty();

    SimpleListProperty<ConceptSpecification> centerPaneDefaultsProperty();

    SimpleListProperty<ConceptSpecification> rightPaneDefaultsProperty();

    WindowPreferencesItem createNewWindowPreferences();


}
