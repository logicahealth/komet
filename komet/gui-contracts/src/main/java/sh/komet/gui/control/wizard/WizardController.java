package sh.komet.gui.control.wizard;

import java.util.Map;

/**
 * 8/28/19
 * aks8m - https://github.com/aks8m
 */
public interface WizardController {

    /**
     * Set the Map representation of data to be used during initializaiton, use, and conclusion of a user
     * interacting with a wizard view.
     * @param wizardDataMap
     */
    void setWizardData(Map<WizardDataTypes, Object> wizardDataMap);

    /**
     * Get the wizard view's current values in it's data map. This is used as inital input when creating the wizard
     * view but also is updated via the view's controller during user interaction.
     * @return wizardDataMap
     */
    Map<WizardDataTypes, Object> getWizardData();

    /**
     * Set the type of wizard the WizardController is functioning as
     * @param wizardType
     */
    void setWizardType(WizardType wizardType);

    /**
     * Get the type of wizard the WizardController is functioning as
     * @return WizardType Enumeration
     */
    WizardType getWizardType();
}
