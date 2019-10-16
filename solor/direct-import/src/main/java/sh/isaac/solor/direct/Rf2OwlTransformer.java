package sh.isaac.solor.direct;

import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.model.ModelGet;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;

public class Rf2OwlTransformer extends TimedTaskWithProgressTracker<Void> implements PersistTaskResult {

    private static final int WRITE_PERMITS = Runtime.getRuntime().availableProcessors() * 2;
    protected final Semaphore writeSemaphore = new Semaphore(WRITE_PERMITS);
    final int transformSize = 10240;
    final IdentifierService identifierService = ModelGet.identifierService();
    private final ImportType importType;

    public Rf2OwlTransformer(ImportType importType) {
        this.importType = importType;
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
            Get.conceptService().getConceptNidStream(conceptAssemblageNid).forEach((conceptNid) -> {

                NidSet owlNids = Get.assemblageService().getSemanticNidsForComponentFromAssemblage(conceptNid, owlAssemblageNid);

                TransformationGroup tg = new TransformationGroup(conceptNid, owlNids.asArray(), PremiseType.STATED);
                statedTransformList.add(tg);

                if (statedTransformList.size() == transformSize) {
                    List<TransformationGroup> listForTask = new ArrayList<>(statedTransformList);
                    OwlTransformerAndWriter transformer = new OwlTransformerAndWriter(listForTask, writeSemaphore, this.importType, getStartTime());
                    Get.executor().submit(transformer);
                    statedTransformList.clear();
                }
            });
            // pickup any items remaining in the list.
            OwlTransformerAndWriter remainingStatedtransformer = new OwlTransformerAndWriter(statedTransformList, writeSemaphore, this.importType, getStartTime());
            Get.executor().submit(remainingStatedtransformer);


            completedUnitOfWork();

            writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
            completedUnitOfWork();
            updateMessage("Completed transformation");

            return null;
        } finally {
            Get.taxonomyService().notifyTaxonomyListenersToRefresh();
            Get.activeTasks().remove(this);
        }
    }
}
