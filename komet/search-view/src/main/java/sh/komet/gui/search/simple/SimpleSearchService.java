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
import sh.isaac.api.component.semantic.SemanticChronology;
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

    @Override
    protected Task<NidSet> createTask() {
        return new Task<NidSet>() {
            @Override
            protected NidSet call() {

                final NidSet results = new NidSet();

                if (!getParameter().isEmpty()) {
                    descriptionLuceneMatch.setManifoldCoordinate(getManifold());
                    descriptionLuceneMatch.setParameterString(getParameter());
                    results.addAll(descriptionLuceneMatch.computePossibleComponents(null));

                    final NidSet filteredValues = new NidSet();

                    // Get a combined set of allowed concepts...
                    TaxonomySnapshotService taxonomySnapshot = Get.taxonomyService().getSnapshot(getManifold());

                    // if the result set is small, it will be faster to use the isKindOf method call, rather than pre-computing
                    // all allowed concepts as the kindOfSequenceSet would. You can play with changing this number to compare
                    // performance choices.
                    NidSet allowedConceptNids = null;

                    try {
                        if (results.size() > 500) {
                            allowedConceptNids = new NidSet();

                            for (int allowedParentNid : getSearchableParents().asArray()) {
                                System.out.println(allowedParentNid);
                                NidSet kindOfSet = taxonomySnapshot.getKindOfConceptNidSet(allowedParentNid);

                                allowedConceptNids.addAll(kindOfSet);
                            }
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                    // I assume this is a description nid...
                    double progressCount = 0;
                    for (int descriptionNid : results.asArray()) {

                        SemanticChronology descriptionChronology = Get.assemblageService()
                                                                      .getSemanticChronology(descriptionNid);
                        LatestVersion<DescriptionVersion> description =
                            descriptionChronology.getLatestVersion(getManifold());

                        // TODO, this step probably filters out inactive descriptions, which is not always what we do.
                        // The stamp coordinate would have to have both active and inactive status values, but I don't want to mess with the
                        // manifold here, save that for the FLOWR query to get right.
                        if (!description.isPresent()) {

                            // move on to the next one...
                            continue;
                        }

                        DescriptionVersion descriptionVersion = description.get();
                        int                conceptNid         = descriptionVersion.getReferencedComponentNid();

                        if (getSearchComponentStatus() != SearchComponentStatus.DONT_CARE) {
                            boolean active = Get.conceptActiveService().isConceptActive(conceptNid, getManifold());

                            if (!getSearchComponentStatus().filter(active)) {

                                // move on to the next one...
                                continue;
                            }
                        }

                        if (!getSearchableParents().isEmpty()) {
                            if (allowedConceptNids != null) {
                                if (!allowedConceptNids.contains(conceptNid)) {

                                    // move on to the next one...
                                    continue;
                                }
                            } else {
                                boolean allowedParentFound = false;

                                for (int allowedParentNid : getSearchableParents().asArray()) {
                                    if (taxonomySnapshot.isKindOf(conceptNid, allowedParentNid)) {
                                        allowedParentFound = true;

                                        // break the allowedParents loop
                                        break;
                                    }
                                }

                                if (!allowedParentFound) {

                                    // move on to the next one.
                                    continue;
                                }
                            }
                        }

                        if(progressCount % 5 == 0) {
                           super.updateProgress(progressCount, results.size());
                        }

                        progressCount++;
                        filteredValues.add(descriptionNid);
                    }

                    results.clear();
                    results.addAll(filteredValues);
                }

                return results;
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

    private String getParameter() {
        return luceneQuery.get();
    }

    private SearchComponentStatus getSearchComponentStatus() {
        return searchComponentStatus.get();
    }

}