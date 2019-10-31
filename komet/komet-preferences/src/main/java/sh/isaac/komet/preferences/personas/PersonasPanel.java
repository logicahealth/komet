package sh.isaac.komet.preferences.personas;

import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.isaac.komet.preferences.ParentPanel;
import sh.komet.gui.manifold.Manifold;

import java.util.UUID;
import java.util.prefs.BackingStoreException;

import static sh.isaac.MetaData.CONCEPT_DETAILS_PANEL____SOLOR;
import static sh.komet.gui.contract.preferences.PreferenceGroup.Keys.GROUP_NAME;

/**
 * 2019-07-26
 * aks8m - https://github.com/aks8m
 */
public class PersonasPanel extends ParentPanel {
    //
    public static final UUID DEFAULT_PERSONA_UUID = UUID.fromString("5b8bfe7f-aad2-4895-ae01-d4b8b0e67861");
    public static final String DEFAULT_PERSONA_NAME = "Komet Window";
    //
    public static final UUID CLASSIFICATION_RESULTS_PERSONA_UUID = UUID.fromString("af0917cf-1814-4660-afbb-ae244a9271af");
    public static final String CLASSIFICATION_RESULTS_PERSONA_NAME = "Classification Results Window";

    //
    public static final UUID COMPOSITE_ACTION_PERSONA_UUID = UUID.fromString("08e49a1f-540e-484e-8dec-cedd7a9d1edd");
    public static final String COMPOSITE_ACTION_PERSONA_NAME = "Composite Action Window";

    public PersonasPanel(IsaacPreferences preferencesNode, Manifold manifold, KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "Personas"), manifold, kpc);
        if (!initialized()) {
            IsaacPreferences compositeActionPersonaPreferences = addChild(COMPOSITE_ACTION_PERSONA_UUID.toString(), PersonaItemPanel.class);
            compositeActionPersonaPreferences.put(GROUP_NAME, COMPOSITE_ACTION_PERSONA_NAME);
            PersonaItemPanel compositeAxtionResultsPersonaItemPanel = new PersonaItemPanel(compositeActionPersonaPreferences, manifold, kpc,
                    "Composite Action",
                    new ConceptSpecification[] {MetaData.COMPOSITE_ACTION_PANEL____SOLOR},
                    new ConceptSpecification[] {MetaData.COMPONENT_LIST_PANEL____SOLOR},
                    new ConceptSpecification[] {MetaData.CONCEPT_DETAILS_LIST_VIEW_LINKED_PANEL____SOLOR}
            );
            compositeAxtionResultsPersonaItemPanel.save();

            IsaacPreferences classificationResultsPersonaPreferences = addChild(CLASSIFICATION_RESULTS_PERSONA_UUID.toString(), PersonaItemPanel.class);
            classificationResultsPersonaPreferences.put(GROUP_NAME, CLASSIFICATION_RESULTS_PERSONA_NAME);
            PersonaItemPanel classificationResultsPersonaItemPanel = new PersonaItemPanel(classificationResultsPersonaPreferences, manifold, kpc,
                    "Classification Results",
                    new ConceptSpecification[] {MetaData.CLASSIFICATION_RESULTS_PANEL____SOLOR},
                    new ConceptSpecification[] {MetaData.CONCEPT_DETAILS_CLASSIFICATION_RESULTS_LINKED_PANEL____SOLOR},
                    new ConceptSpecification[] {MetaData.TAXONOMY_PANEL____SOLOR}
            );
            classificationResultsPersonaItemPanel.save();


            IsaacPreferences personaPreferences = addChild(DEFAULT_PERSONA_UUID.toString(), PersonaItemPanel.class);
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
