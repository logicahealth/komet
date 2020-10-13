package sh.isaac.provider.elk;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.elk.exceptions.ElkException;
import org.semanticweb.elk.loading.AxiomLoader;
import org.semanticweb.elk.owl.interfaces.ElkClass;
import org.semanticweb.elk.owl.parsing.Owl2ParserFactory;
import org.semanticweb.elk.reasoner.Reasoner;
import org.semanticweb.elk.reasoner.ReasonerFactory;
import org.semanticweb.elk.reasoner.config.ReasonerConfiguration;
import org.semanticweb.elk.reasoner.taxonomy.model.Taxonomy;
import sh.isaac.api.Get;
import sh.isaac.api.SingleAssemblageSnapshot;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.classifier.ClassifierResults;
import sh.isaac.api.classifier.ClassifierService;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.ManifoldCoordinateImmutable;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.task.TimedTask;

public class ElkClassifier implements ClassifierService {
    private final ManifoldCoordinateImmutable manifoldCoordinateImmutable;
    private static final int BATCH_LENGTH = 100;
    private static final Logger LOG = LogManager.getLogger();

    public ElkClassifier(ManifoldCoordinateImmutable manifoldCoordinateImmutable) {
        this.manifoldCoordinateImmutable = manifoldCoordinateImmutable;
    }

    @Override
    public TimedTask<ClassifierResults> classify() {
        SingleAssemblageSnapshot<LogicGraphVersion> snapshot = Get.assemblageService().getSingleAssemblageSnapshot(TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE, LogicGraphVersion.class, manifoldCoordinateImmutable.getViewStampFilter());
        ElkLoader loader = new ElkLoader(snapshot.getLatestSemanticVersionsFromAssemblage(), BATCH_LENGTH);

        // create reasoner
        ReasonerFactory reasoningFactory = new ReasonerFactory();

        ReasonerConfiguration configuration = ReasonerConfiguration
                .getConfiguration();
        Reasoner reasoner = reasoningFactory.createReasoner(loader,
                configuration);

        // Classify the ontology.
        
        try {
            Taxonomy<ElkClass> taxonomy = reasoner.getTaxonomyQuietly();
            LOG.info("getTaxonomyQuietly complete: " + taxonomy.getTopNode());
        } catch (ElkException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public TimedTask<ClassifierResults> classify(boolean cycleCheck) {
        return null;
    }

    @Override
    public TimedTask<Integer> getConceptNidForExpression(LogicalExpression expression, ManifoldCoordinate manifoldCoordinate) {
        return null;
    }
}
