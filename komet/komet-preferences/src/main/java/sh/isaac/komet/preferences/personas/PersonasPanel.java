package sh.isaac.komet.preferences.personas;

import sh.isaac.api.preferences.IsaacPreferences;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.isaac.komet.preferences.ParentPanel;
import sh.komet.gui.manifold.Manifold;

import java.util.UUID;
import java.util.prefs.BackingStoreException;

import static sh.komet.gui.contract.preferences.PreferenceGroup.Keys.GROUP_NAME;

/**
 * 2019-07-26
 * aks8m - https://github.com/aks8m
 */
public class PersonasPanel extends ParentPanel {
    public static UUID DEFAULT_PERSONA = UUID.fromString("5b8bfe7f-aad2-4895-ae01-d4b8b0e67861");
    public static final String DEFAULT_PERSONA_NAME = "Default persona";

    public PersonasPanel(IsaacPreferences preferencesNode, Manifold manifold, KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "Personas"), manifold, kpc);
        if (!initialized()) {
            IsaacPreferences personaPreferences = addChild(DEFAULT_PERSONA.toString(), PersonaItemPanel.class);
            personaPreferences.put(GROUP_NAME, DEFAULT_PERSONA_NAME);
            PersonaItemPanel personaItemPanel = new PersonaItemPanel(personaPreferences, manifold, kpc);
            personaItemPanel.save();
        }
        revertFields();
        save();
    }

    @Override
    protected Class getChildClass() {
        return PersonaItemPanel.class;
    }

    @Override
    protected void saveFields() throws BackingStoreException {

    }

    @Override
    protected void revertFields() {
        // nothing to revert
    }
}
