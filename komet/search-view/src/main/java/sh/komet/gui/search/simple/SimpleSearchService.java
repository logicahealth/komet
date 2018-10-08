package sh.komet.gui.search.simple;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;

import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.query.clauses.DescriptionLuceneMatch;
import sh.isaac.provider.query.search.CompositeSearchResult;
import sh.isaac.provider.query.search.SearchHandle;
import sh.isaac.provider.query.search.SearchHandler;

import sh.komet.gui.manifold.Manifold;
import sh.isaac.api.TaxonomySnapshot;

/**
 * @author aks8m
 */
public class SimpleSearchService extends Service<NidSet> {

    protected static final Logger LOG = LogManager.getLogger();

    private final SimpleStringProperty luceneQuery = new SimpleStringProperty();
    private final SimpleListProperty<Integer> parentNids = new SimpleListProperty<>();
    private final DescriptionLuceneMatch descriptionLuceneMatch = new DescriptionLuceneMatch();
    private Manifold manifold;
    private final double PROGRESS_MAX_VALUE = 100;
    private final double PROGRESS_INCREMENT_VALUE = 33.333; //Hard Coded based on Current Filter Algorithm (3 parts)
    private double PROGRESS_CURRENT = 0;

    @Override
    protected Task<NidSet> createTask() {
        return new Task<NidSet>() {
            @Override
            protected NidSet call() {

                PROGRESS_CURRENT = 0;
                final NidSet results = new NidSet();

                if (!getLuceneQuery().isEmpty()) {

                    final NidSet filteredValues = new NidSet();
                    TaxonomySnapshot taxonomySnapshot = Get.taxonomyService().getSnapshot(getManifold());

                    runLuceneDescriptionQuery(results);
                    NidSet allowedConceptNids = findAllKindOfConcepts(results, taxonomySnapshot);
                    filterAllSemanticsBasedOnReferencedConcepts(results, allowedConceptNids, filteredValues, taxonomySnapshot);

                    results.clear();
                    results.addAll(filteredValues);
                }

                return results;
            }

            private void runLuceneDescriptionQuery(NidSet results) {
                updateProgress(computeProgress(PROGRESS_INCREMENT_VALUE), PROGRESS_MAX_VALUE);
                descriptionLuceneMatch.setManifoldCoordinate(getManifold());
                String queryString = getLuceneQuery();
                // Special handling to remove check digit from LOINC code query. 
                if (queryString.charAt(queryString.length() -2) == '-') {
                    queryString = queryString.substring(0, queryString.length() -2);
                }
                
                
                descriptionLuceneMatch.setParameterString(queryString);
                results.addAll(descriptionLuceneMatch.computePossibleComponents(null));
//                if (results.isEmpty()) {
                if (true) {
                    
                    try {
                        CountDownLatch searchComplete = new CountDownLatch(1);
                        SearchHandle ssh = SearchHandler.searchIdentifiers(queryString,
                                null,
                                ((searchHandle) -> {
                                    try {
                                        for (CompositeSearchResult result : searchHandle.getResults()) {
                                            ConceptChronology containingConcept = result.getContainingConcept();
                                            for (SemanticChronology description : containingConcept.getConceptDescriptionList()) {
                                                results.add(description.getNid());
                                            }
                                        }
                                    } catch (Exception ex) {
                                        LOG.error(ex.toString(), ex);
                                    } finally {
                                        searchComplete.countDown();
                                    }
                                }),
                                null, null, true, manifold, false, null,
                                null, 10);
                        
                        searchComplete.await();
                    } catch (InterruptedException ex) {
                        LOG.error(ex.getLocalizedMessage(), ex);
                    }
                }

            }

            private NidSet findAllKindOfConcepts(NidSet results, TaxonomySnapshot taxonomySnapshot) {
                NidSet allowedConceptNids = new NidSet();

                try {
                    if (results.size() > 500) {

                        for (int allowedParentNid : getParentNids()) {
                            System.out.println(allowedParentNid);
                            NidSet kindOfSet = taxonomySnapshot.getKindOfConceptNidSet(allowedParentNid);

                            allowedConceptNids.addAll(kindOfSet);

                            updateProgress(
                                    computeProgress(PROGRESS_INCREMENT_VALUE
                                            / getParentNids().size()),
                                    PROGRESS_MAX_VALUE);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (allowedConceptNids.isEmpty()) {
                    updateProgress(computeProgress(PROGRESS_INCREMENT_VALUE), PROGRESS_MAX_VALUE);
                }

                return allowedConceptNids;
            }

            private void filterAllSemanticsBasedOnReferencedConcepts(NidSet results, NidSet allowedConceptNids,
                    NidSet filteredValues, TaxonomySnapshot taxonomySnapshot) {

                if (results.isEmpty()) {
                    updateProgress(computeProgress(PROGRESS_INCREMENT_VALUE), PROGRESS_MAX_VALUE);
                }

                for (int componentNid : results.asArray()) {
                    updateProgress(
                            computeProgress(PROGRESS_INCREMENT_VALUE / results.asArray().length),
                            PROGRESS_MAX_VALUE);

                    switch (Get.identifierService().getObjectTypeForComponent(componentNid)) {
                        case CONCEPT:
                            // ignore for now
                            break;
                        case SEMANTIC:
                            SemanticChronology semanticChronology = Get.assemblageService()
                                    .getSemanticChronology(componentNid);
                            switch (semanticChronology.getVersionType()) {
                                case DESCRIPTION:
                                    handleDescription(semanticChronology, allowedConceptNids, taxonomySnapshot, filteredValues);
                                    break;
                                case STRING:
                                    // TODO SHORT TERM: Find a description for the concept or description associated
                                    // with the identifier...
                                    // TODO LONG Term: display the object that matched in the result list...
                                    Optional<? extends Chronology> optionalChronology
                                            = Get.identifiedObjectService().getChronology(semanticChronology.getReferencedComponentNid());
                                    if (optionalChronology.isPresent()) {
                                        Chronology chronology = optionalChronology.get();
                                        switch (chronology.getVersionType()) {
                                            case CONCEPT:
                                                ConceptChronology concept = (ConceptChronology) chronology;
                                                for (SemanticChronology descriptionChronology : concept.getConceptDescriptionList()) {
                                                    filteredValues.add(descriptionChronology.getNid());
                                                }
                                                break;
                                            case DESCRIPTION:
                                                filteredValues.add(chronology.getNid());
                                                break;
                                        }
                                    }
                                    LOG.info("Search found: " + semanticChronology);
                                default:
                                // ignore for now. 
                            }
                            break;
                    }
                }

            }

            protected void handleDescription(SemanticChronology semanticChronology, NidSet allowedConceptNids, TaxonomySnapshot taxonomySnapshot, NidSet filteredValues) {
                LatestVersion<DescriptionVersion> description = semanticChronology.getLatestVersion(getManifold());
                if (!description.isPresent()) {
                    return;
                }
                DescriptionVersion descriptionVersion = description.get();
                int conceptNid = descriptionVersion.getReferencedComponentNid();
                if (!getParentNids().isEmpty()) {
                    if (!allowedConceptNids.isEmpty()) {
                        if (!allowedConceptNids.contains(conceptNid)) {
                            return;
                        }
                    } else {
                        boolean allowedParentFound = false;
                        for (int allowedParentNid : getParentNids()) {
                            if (taxonomySnapshot.isKindOf(conceptNid, allowedParentNid)) {
                                allowedParentFound = true;
                                break;
                            }
                        }
                        if (!allowedParentFound) {
                            return;
                        }
                    }
                }
                filteredValues.add(semanticChronology.getNid());
            }

            private double computeProgress(double incrementValue) {
                if (PROGRESS_CURRENT == 0) {
                    PROGRESS_CURRENT = incrementValue;
                } else {
                    PROGRESS_CURRENT += incrementValue;
                }
                return PROGRESS_CURRENT;
            }
        };
    }

    public SimpleStringProperty luceneQueryProperty() {
        return luceneQuery;
    }

    private Manifold getManifold() {
        return this.manifold;
    }

    public void setManifold(Manifold manifold) {
        this.manifold = manifold;
    }

    private String getLuceneQuery() {
        return luceneQuery.get();
    }

    private ObservableList<Integer> getParentNids() {
        return parentNids.get();
    }

    public SimpleListProperty<Integer> parentNidsProperty() {
        return parentNids;
    }

}
