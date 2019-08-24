package sh.komet.gui.control.wizard.controller;

import sh.komet.gui.control.wizard.WizardType;

/**
 * 8/21/19
 * aks8m - https://github.com/aks8m
 */
public interface WizardModel<T, U> {

    U getModel();

    void initializeWizardModel(T initialWizardModel);

    WizardType getWizardType();
}
