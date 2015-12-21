package gov.vha.isaac.ochre.logic.csiro.classify;

import au.csiro.ontology.Ontology;
import au.csiro.ontology.classification.IReasoner;
import au.csiro.ontology.model.Axiom;
import au.csiro.snorocket.core.SnorocketReasoner;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;
import org.apache.mahout.math.set.OpenIntHashSet;

import java.util.BitSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Created by kec on 12/16/14.
 */

// TODO move to CSIRO specific module
// Create a classifier service...
public class Classify {
    public static void execute(BitSet conceptSequences, OpenIntHashSet roleSequences,
                               OpenIntHashSet neverGroupRoleSequences, int roleGroupConceptSequence) {
        Stream<LogicalExpressionOchreImpl> logicGraphStream = null;


        AxiomCollector axiomCollector = new AxiomCollector(conceptSequences, roleSequences,
                neverGroupRoleSequences, roleGroupConceptSequence);
        Set<Axiom> axioms = logicGraphStream.collect(axiomCollector);

        // Create a classifier and classify the axioms
        IReasoner r = new SnorocketReasoner();
        r.loadAxioms(axioms);
        r = r.classify();

        // Get only the taxonomy
        Ontology res = r.getClassifiedOntology();

    }
}
