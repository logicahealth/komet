package org.ihtsdo.otf.tcc.api.spec;

import java.io.IOException;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;

public class SpecFactory {

    public static ConceptSpec get(ConceptChronicleBI concept, ViewCoordinate vc) throws IOException {
        ConceptVersionBI cv = Ts.get().getConceptVersion(vc, concept.getNid());
        try {
            return new ConceptSpec(cv.getDescriptionsActive().iterator().next().getText(),
                    concept.getPrimordialUuid());
        } catch (ContradictionException ex) {
            for (DescriptionChronicleBI desc : concept.getDescriptions()) {
                for (DescriptionVersionBI dv : desc.getVersions(vc)) {
                    return new ConceptSpec(dv.getText(),
                            concept.getPrimordialUuid());
                }
            }
            throw new IOException("No current description for: " + cv);
        }
    }

    public static ConceptSpec get(ConceptVersionBI concept) throws IOException {
        try {
            return new ConceptSpec(concept.getDescriptionsActive().iterator().next().getText(),
                    concept.getPrimordialUuid());
        } catch (ContradictionException ex) {
            return new ConceptSpec(
                    concept.getDescriptions().iterator().next().getVersions().iterator().next().getText(),
                    concept.getPrimordialUuid());
        }
    }

    public static DescriptionSpec get(DescriptionVersionBI desc, ViewCoordinate vc) throws IOException {
        if (desc != null && desc.getUUIDs() != null) {
            DescriptionSpec ds = new DescriptionSpec(desc.getUUIDs().toArray(new UUID[]{}),
                    get(Ts.get().getConcept(desc.getConceptNid()), vc),
                    get(Ts.get().getConcept(desc.getTypeNid()), vc),
                    desc.getText());
            ds.setLangText(desc.getText());
            return ds;
        } else {
            return null;
        }
    }
}
