package sh.komet.gui.control.wizard;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

/**
 * 8/28/19
 * aks8m - https://github.com/aks8m
 */
public interface WizardView {

    /**
     * Set the Map representation of data to be used during initializaiton, use, and conclusion of a user
     * interacting with a wizard view.
     * @param wizardDataMap
     */
    void setWizardViewData(Map<String, Object> wizardDataMap);

    /**
     * Get the wizard view's current values in it's data map. This is used as inital input when creating the wizard
     * view but also is updated via the view's controller during user interaction.
     * @return wizardDataMap
     */
    Map<String, Object> getWizardViewData();

    /**
     * Get the fxml file that represents the javafx view configuration for this particular
     * @return Wizard View FXML path location
     */
    String getWizardViewFXML();

    /**
     *Get the UUID for this specific Wizard View
     * @return Wizard View UUID
     */
    UUID getWizardViewUUID();

}
