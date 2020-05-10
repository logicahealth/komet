package sh.komet.gui.util;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.StaticIsaacCache;
import sh.komet.gui.contract.preferences.PersonaChangeListener;
import sh.komet.gui.contract.preferences.PersonaItem;

import javax.inject.Singleton;
import java.util.HashSet;

@Service
@Singleton
public class PersonaChangeListeners implements StaticIsaacCache {
    private static ObservableSet<PersonaChangeListener> PERSONA_CHANGE_LISTENERS = FXCollections.observableSet(new HashSet<>());

    public static void addPersonaChangeListener(PersonaChangeListener listener) {
        PERSONA_CHANGE_LISTENERS.add(listener);
    }
    public static void removePersonaChangeListener(PersonaChangeListener listener) {
        PERSONA_CHANGE_LISTENERS.remove(listener);
    }
    public static void firePersonaChanged(PersonaItem personaItem, boolean active) {
        for (PersonaChangeListener personaChangeListener: PERSONA_CHANGE_LISTENERS) {
            personaChangeListener.personaChanged(personaItem, active);
        }
    }
    @Override
    public void reset() {
        Platform.runLater(() -> {
            PERSONA_CHANGE_LISTENERS.clear();
        });

    }
}
