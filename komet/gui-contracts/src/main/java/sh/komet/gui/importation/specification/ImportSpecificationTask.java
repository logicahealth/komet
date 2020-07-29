package sh.komet.gui.importation.specification;

import sh.isaac.api.Get;
import sh.isaac.api.classifier.ClassifierService;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.solor.ContentProvider;
import sh.isaac.solor.direct.*;
import sh.komet.gui.control.property.ViewProperties;

import java.util.List;
import java.util.concurrent.Future;

/**
 * 2019-05-15
 * aks8m - https://github.com/aks8m
 */
public class ImportSpecificationTask extends TimedTaskWithProgressTracker<Void> implements PersistTaskResult {

    final ViewProperties viewProperties;
    final List<ContentProvider> entriesToImport;
    private final boolean isRF2RelationshipTransformed;
    private final boolean isLoincExpressionToConceptTransformed;
    private final boolean isLoincExpresstionToNavConceptsTransformed;
    private final boolean isClassfied;
    private final Transaction transaction;


    public ImportSpecificationTask(Transaction transaction,
                                   ViewProperties viewProperties, List<ContentProvider> entriesToImport,
                                   boolean isRF2RelationshipTransformed,
                                   boolean isLoincExpressionToConceptTransformed,
                                   boolean isLoincExpresstionToNavConceptsTransformed,
                                   boolean isClassfied) {
        this.transaction = transaction;
        this.viewProperties = viewProperties;
        this.entriesToImport = entriesToImport;
        this.isRF2RelationshipTransformed = isRF2RelationshipTransformed;
        this.isLoincExpressionToConceptTransformed = isLoincExpressionToConceptTransformed;
        this.isLoincExpresstionToNavConceptsTransformed = isLoincExpresstionToNavConceptsTransformed;
        this.isClassfied = isClassfied;

        updateTitle("Run Import Specifications");

        addToTotalWork(6);
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() throws Exception {
        try {
            completedUnitOfWork();
            updateMessage("Importing new content...");
            DirectImporter importer = new DirectImporter(transaction, null, entriesToImport);
            Future<?> importTask = Get.executor().submit(importer);
            importTask.get();
            completedUnitOfWork();

            if(this.isRF2RelationshipTransformed) {
                updateMessage("Transforming to SOLOR...");
                Rf2RelationshipTransformer transformer = new Rf2RelationshipTransformer(null);//TODO: Import Selected from the SNOMED Specificaiton
                Future<?> transformTask = Get.executor().submit(transformer);
                transformTask.get();
            }
            completedUnitOfWork();

            if(this.isLoincExpressionToConceptTransformed) {
                updateMessage("Convert LOINC expressions...");
                LoincExpressionToConcept convertLoinc = new LoincExpressionToConcept(transaction);
                Future<?> convertLoincTask = Get.executor().submit(convertLoinc);
                convertLoincTask.get();
            }
            completedUnitOfWork();

            if(this.isLoincExpresstionToNavConceptsTransformed) {
                updateMessage("Adding navigation concepts...");
                LoincExpressionToNavConcepts addNavigationConcepts = new LoincExpressionToNavConcepts(transaction, viewProperties.getManifoldCoordinate());
                Future<?> addNavigationConceptsTask = Get.executor().submit(addNavigationConcepts);
                addNavigationConceptsTask.get();
            }
            completedUnitOfWork();

            if(this.isClassfied) {
                updateMessage("Classifying new content...");
                ClassifierService classifierService = Get.logicService().getClassifierService(viewProperties.getManifoldCoordinate());
                Future<?> classifyTask = classifierService.classify();
                classifyTask.get();
            }
            completedUnitOfWork();

            return null;
        } finally {
            this.done();
            Get.taxonomyService().notifyTaxonomyListenersToRefresh();
            Get.activeTasks().remove(this);
        }
    }
}
