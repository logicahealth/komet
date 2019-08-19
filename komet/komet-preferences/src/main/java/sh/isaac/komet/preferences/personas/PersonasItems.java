package sh.isaac.komet.preferences.personas;

import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.preferences.KometPreferencesController;
import sh.isaac.komet.preferences.ParentPanel;
import sh.isaac.komet.preferences.window.WindowPreferencePanel;
import sh.komet.gui.manifold.Manifold;

import java.util.UUID;
import java.util.prefs.BackingStoreException;

import static sh.isaac.komet.preferences.PreferenceGroup.Keys.GROUP_NAME;

/**
 * 2019-07-26
 * aks8m - https://github.com/aks8m
 */
public class PersonasItems extends ParentPanel {
    public static UUID DEFAULT_PERSONA = UUID.fromString("5b8bfe7f-aad2-4895-ae01-d4b8b0e67861");

    public PersonasItems(IsaacPreferences preferencesNode, Manifold manifold, KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "Personas"), manifold, kpc);
        if (!initialized()) {
            IsaacPreferences personaPreferences = addChild(UUID.randomUUID().toString(), PersonasItemPanel.class);
            PersonasItemPanel personasItemPanel = new PersonasItemPanel(personaPreferences, manifold, kpc);
            personasItemPanel.setPersonaUuid(DEFAULT_PERSONA);
            personasItemPanel.nameProperty().set("KOMET persona");
            personasItemPanel.save();
        }
        revertFields();
        save();
    }

    @Override
    protected Class getChildClass() {
        return PersonasItemPanel.class;
    }

    @Override
    protected void saveFields() throws BackingStoreException {

    }

    @Override
    protected void revertFields() {
        // nothing to revert
    }
}
