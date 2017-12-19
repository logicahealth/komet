package sh.komet.gui.search.simple;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import org.controlsfx.control.IndexedCheckModel;
import sh.isaac.api.Get;
import sh.isaac.api.TaxonomySnapshotService;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.query.clauses.DescriptionLuceneMatch;

import sh.komet.gui.manifold.Manifold;

/**
 * @author aks8m
 */
public class SimpleSearchService extends Service<NidSet> {

    private final SimpleStringProperty luceneQuery = new SimpleStringProperty();
    private final SimpleObjectProperty<SearchComponentStatus> searchComponentStatus  = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<IndexedCheckModel<SimpleSearchController.CustomCheckListItem>> searchableParents = new SimpleObjectProperty<>();
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
                    TaxonomySnapshotService taxonomySnapshot = Get.taxonomyService().getSnapshot(getManifold());

                    runLuceneDescriptionQuery(results);
                    NidSet allowedConceptNids = findAllKindOfConcepts(results, taxonomySnapshot);
                    filterAllSemanticsBasedOnReferencedConcepts(results, allowedConceptNids, filteredValues, taxonomySnapshot);

                    results.clear();
                    results.addAll(filteredValues);
                }

                return results;
            }

            private void runLuceneDescriptionQuery(NidSet results){
                updateProgress(computeProgress(PROGRESS_INCREMENT_VALUE), PROGRESS_MAX_VALUE);
                descriptionLuceneMatch.setManifoldCoordinate(getManifold());
                descriptionLuceneMatch.setParameterString(getLuceneQuery());
                results.addAll(descriptionLuceneMatch.computePossibleComponents(null));
            }

            private NidSet findAllKindOfConcepts(NidSet results, TaxonomySnapshotService taxonomySnapshot){
                NidSet allowedConceptNids = new NidSet();

                try {
                    if (results.size() > 500) {

                        for (int allowedParentNid : getSearchableParents().asArray()) {
                            System.out.println(allowedParentNid);
                            NidSet kindOfSet = taxonomySnapshot.getKindOfConceptNidSet(allowedParentNid);

                            allowedConceptNids.addAll(kindOfSet);

                            updateProgress(
                                    computeProgress(PROGRESS_INCREMENT_VALUE
                                            / getSearchableParents().asArray().length)
                                    , PROGRESS_MAX_VALUE );
                        }
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }

                if(allowedConceptNids.isEmpty())
                    updateProgress(computeProgress(PROGRESS_INCREMENT_VALUE), PROGRESS_MAX_VALUE);

                return allowedConceptNids;
            }

            private void filterAllSemanticsBasedOnReferencedConcepts(NidSet results, NidSet allowedConceptNids
                    , NidSet filteredValues, TaxonomySnapshotService taxonomySnapshot){

                if(results.isEmpty())
                    updateProgress(computeProgress(PROGRESS_INCREMENT_VALUE), PROGRESS_MAX_VALUE );

                for (int descriptionNid : results.asArray()) {
                    updateProgress(
                            computeProgress(PROGRESS_INCREMENT_VALUE / results.asArray().length)
                            , PROGRESS_MAX_VALUE );

                    LatestVersion<DescriptionVersion> description = Get.assemblageService()
                            .getSemanticChronology(descriptionNid)
                            .getLatestVersion(getManifold());

                    if (!description.isPresent()) {
                        continue;
                    }

                    DescriptionVersion descriptionVersion = description.get();
                    int                conceptNid         = descriptionVersion.getReferencedComponentNid();

                    if (getSearchComponentStatus() != SearchComponentStatus.DONT_CARE) {
                        boolean active = Get.conceptActiveService().isConceptActive(conceptNid, getManifold());

                        if (!getSearchComponentStatus().filter(active)) {
                            continue;
                        }
                    }

                    if (!getSearchableParents().isEmpty()) {
                        if (!allowedConceptNids.isEmpty()) {
                            if (!allowedConceptNids.contains(conceptNid)) {
                                continue;
                            }
                        } else {
                            boolean allowedParentFound = false;

                            for (int allowedParentNid : getSearchableParents().asArray()) {
                                if (taxonomySnapshot.isKindOf(conceptNid, allowedParentNid)) {
                                    allowedParentFound = true;
                                    break;
                                }
                            }

                            if (!allowedParentFound) {
                                continue;
                            }
                        }
                    }
                    filteredValues.add(descriptionNid);
                }
            }

            private double computeProgress(double incrementValue){
                if(PROGRESS_CURRENT == 0){
                    PROGRESS_CURRENT = incrementValue;
                }else{
                    PROGRESS_CURRENT += incrementValue;
                }
                return PROGRESS_CURRENT;
            }
        };
    }

    public SimpleStringProperty luceneQueryProperty() {
        return luceneQuery;
    }

    public SimpleObjectProperty<SearchComponentStatus> searchComponentStatusProperty() {
        return searchComponentStatus;
    }

    private NidSet getSearchableParents() {
        NidSet searchableParentsNidSet = new NidSet();
        this.searchableParentsProperty().get().getCheckedIndices()
                .forEach(index -> searchableParentsNidSet.add(this.searchableParents.get().getItem(index).getNID()));
        return searchableParentsNidSet;
    }

    public SimpleObjectProperty<IndexedCheckModel<SimpleSearchController.CustomCheckListItem>> searchableParentsProperty() {
        return searchableParents;
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

    private SearchComponentStatus getSearchComponentStatus() {
        return searchComponentStatus.get();
    }



}