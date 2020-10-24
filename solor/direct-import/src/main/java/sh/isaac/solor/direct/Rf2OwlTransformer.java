package sh.isaac.solor.direct;

import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.model.ModelGet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Semaphore;

public class Rf2OwlTransformer extends TimedTaskWithProgressTracker<Void> implements PersistTaskResult {

    private static final int WRITE_PERMITS = Runtime.getRuntime().availableProcessors() * 2;
    // TODO consider replacing readSemaphore with TaskCountManager
    protected final Semaphore writeSemaphore = new Semaphore(WRITE_PERMITS);
    final int transformSize = 10240;
    final IdentifierService identifierService = ModelGet.identifierService();
    private final ImportType importType;
    private final Transaction transaction;

    public Rf2OwlTransformer(ImportType importType, Transaction transaction) {
        this.importType = importType;
        this.transaction = transaction;
        updateTitle("Converting RF2 OWL to expressions " + importType);
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() throws Exception {
        try {
            setStartTime();
            updateMessage("Computing stated OWL expressions...");
            int conceptAssemblageNid = TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid();
            int owlAssemblageNid = Get.nidForUuids(UUID.fromString("9a119252-b2da-3e62-8767-706558be8e4b"));
            

            addToTotalWork(4);
            completedUnitOfWork();

            List<TransformationGroup> statedTransformList = new ArrayList<>();

            updateMessage("Transforming stated OWL expressions...");
            Get.conceptService().getConceptNidStream(conceptAssemblageNid, false).forEach((conceptNid) -> {

                ImmutableIntSet owlNids = Get.assemblageService().getSemanticNidsForComponentFromAssemblage(conceptNid, owlAssemblageNid);

                TransformationGroup tg = new TransformationGroup(conceptNid, owlNids.toArray(), PremiseType.STATED);
                statedTransformList.add(tg);

                if (statedTransformList.size() == transformSize) {
                    List<TransformationGroup> listForTask = new ArrayList<>(statedTransformList);
                    OwlTransformerAndWriter transformer = new OwlTransformerAndWriter(transaction, listForTask, writeSemaphore, this.importType, getStartTime());
                    Get.executor().submit(transformer);
                    statedTransformList.clear();
                }
            });
            // pickup any items remaining in the list.
            OwlTransformerAndWriter remainingStatedtransformer = new OwlTransformerAndWriter(transaction, statedTransformList, writeSemaphore, this.importType, getStartTime());
            Get.executor().submit(remainingStatedtransformer);


            completedUnitOfWork();

            writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
            transaction.commit("Finishing owl transformation").get();
            completedUnitOfWork();
            updateMessage("Completed transformation");

            return null;
        } finally {
            Get.taxonomyService().notifyTaxonomyListenersToRefresh();
            Get.activeTasks().remove(this);
        }
    }
}
