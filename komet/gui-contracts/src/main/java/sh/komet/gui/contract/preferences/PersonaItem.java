package sh.komet.gui.contract.preferences;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import sh.isaac.api.component.concept.ConceptSpecification;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 2019-07-22
 * aks8m - https://github.com/aks8m
 */
public interface PersonaItem {
    void save();
    void revert();

    UUID getPersonaUuid();
    void setPersonaUuid(UUID personaUuid);

    SimpleStringProperty nameProperty();

    SimpleStringProperty instanceNameProperty();

    SimpleBooleanProperty enableLeftPaneProperty();

    SimpleBooleanProperty enableCenterPaneProperty();

    SimpleBooleanProperty enableRightPaneProperty();

    SimpleListProperty<ConceptSpecification> leftPaneOptionsProperty();

    SimpleListProperty<ConceptSpecification> centerPaneOptionsProperty();

    SimpleListProperty<ConceptSpecification> rightPaneOptionProperty();

    SimpleListProperty<ConceptSpecification> leftPaneDefaultsProperty();

    SimpleListProperty<ConceptSpecification> centerPaneDefaultsProperty();

    SimpleListProperty<ConceptSpecification> rightPaneDefaultsProperty();

    WindowPreferences createNewWindowPreferences();

    /**
     * If pane is enabled, but getAllowedOptionsForPane is empty, it should
     * be treated as a wildcard, where all options are allowed.
     * @param paneIndex
     * @return The set of allowed options for a pane, or empty if a wildcard (all options are allowed).
     */
    Set<ConceptSpecification> getAllowedOptionsForPane(int paneIndex);

    List<ConceptSpecification> getDefaultItemsForPane(int paneIndex);

    boolean isPaneEnabled(int paneIndex);
}
