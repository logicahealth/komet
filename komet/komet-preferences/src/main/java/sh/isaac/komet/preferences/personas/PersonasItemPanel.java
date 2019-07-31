package sh.isaac.komet.preferences.personas;

import javafx.beans.property.SimpleStringProperty;
import sh.isaac.MetaData;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.preferences.AbstractPreferences;
import sh.isaac.komet.preferences.KometPreferencesController;
import sh.komet.gui.contract.preferences.PersonaItem;
import sh.komet.gui.control.PropertySheetTextWrapper;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;

import java.util.Optional;
import java.util.prefs.BackingStoreException;

/**
 * 2019-07-22
 * aks8m - https://github.com/aks8m
 */
public class PersonasItemPanel extends AbstractPreferences implements PersonaItem {

    public enum Keys {
        PERSONA_NAME,
        PERSONA_TAXONOMY_CONFIGURATION_ID,
        PERSONA_WINDOW_CONFIGURATION_ID
    };

    private final SimpleStringProperty nameProperty
            = new SimpleStringProperty(this, MetaData.PERSONA_NAME____SOLOR.toExternalString());

    public PersonasItemPanel(IsaacPreferences preferencesNode, Manifold manifold, KometPreferencesController kpc) {
        super(preferencesNode, getGroupName(preferencesNode), manifold, kpc);
        nameProperty.set(groupNameProperty().get());
        nameProperty.addListener((observable, oldValue, newValue) -> {
            groupNameProperty().set(newValue);
        });
        revertFields();
        save();
        getItemList().add(new PropertySheetTextWrapper(manifold, nameProperty));

    }

    @Override
    protected void saveFields() throws BackingStoreException {

        Optional<String> oldItemName = getPreferencesNode().get(Keys.PERSONA_NAME);
        if (oldItemName.isPresent()) {
            FxGet.removeTaxonomyConfiguration(oldItemName.get());
        }

        getPreferencesNode().put(PersonasItemPanel.Keys.PERSONA_NAME, nameProperty.get());


    }

    @Override
    protected void revertFields(){
        this.nameProperty.set(getPreferencesNode().get(PersonasItemPanel.Keys.PERSONA_NAME, getGroupName()));

    }

    @Override
    public boolean showDelete() {
        return true;
    }

    private static String getGroupName(IsaacPreferences preferencesNode) {
        return preferencesNode.get(Keys.PERSONA_NAME, "Persona");
    }
}
