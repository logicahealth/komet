package sh.komet.gui.contract.preferences;

import sh.isaac.api.preferences.IsaacPreferences;

import java.util.UUID;

public interface WindowsParentPreferences {
    IsaacPreferences addWindow(String windowName, UUID windowUuid);
}
