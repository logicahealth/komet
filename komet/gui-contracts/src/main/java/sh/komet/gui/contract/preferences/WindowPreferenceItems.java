package sh.komet.gui.contract.preferences;

import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import sh.isaac.api.component.concept.ConceptSpecification;

import java.util.List;

public interface WindowPreferenceItems {
    StringProperty getWindowName();

    ObservableList<ConceptSpecification> getLeftTabPanelSpecs();
    ObservableList<ConceptSpecification> getCenterTabPanelSpecs();
    ObservableList<ConceptSpecification> getRightTabPanelSpecs();
}
