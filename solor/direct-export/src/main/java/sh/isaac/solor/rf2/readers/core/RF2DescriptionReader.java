package sh.isaac.solor.rf2.readers.core;

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.observable.semantic.version.ObservableDescriptionVersion;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.solor.rf2.utility.RF2ExportHelper;
import sh.komet.gui.manifold.Manifold;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class RF2DescriptionReader extends TimedTaskWithProgressTracker<List<String>> {

    private final RF2ExportHelper rf2ExportHelper;
    private final List<Chronology> chronologies;
    private final Semaphore readSemaphore;
    private final Manifold manifold;

    public RF2DescriptionReader(List<Chronology> chronologies, Semaphore readSemaphore, Manifold manifold, String message) {
        this.chronologies = chronologies;
        this.readSemaphore = readSemaphore;
        this.manifold = manifold;
        rf2ExportHelper = new RF2ExportHelper(this.manifold);

        readSemaphore.acquireUninterruptibly();

        updateTitle("Reading " + message + " batch of size: " + chronologies.size());
        updateMessage("Processing batch of descriptions for RF2 Export");
        addToTotalWork(chronologies.size());
        Get.activeTasks().add(this);
    }

    @Override
    protected List<String> call() throws Exception {
        ArrayList<String> returnList = new ArrayList<>();

        try{

            for(Chronology chronology : this.chronologies) {
                returnList.add(
                        rf2ExportHelper.getRF2CommonElements(chronology)
                                .append(rf2ExportHelper.getIdString(Get.concept(((SemanticChronology) chronology).getReferencedComponentNid())) + "\t") //conceptId
                                .append(getLanguageCode(chronology) + "\t") //languageCode
                                .append(getTypeId(chronology) + "\t") //typeId
                                .append(getTerm(chronology) + "\t") //term
                                .append(getCaseSignificanceId(chronology)) //caseSignificanceId
                                .append("\r")
                                .toString());
                completedUnitOfWork();
            }


        }finally {
            this.readSemaphore.release();
            Get.activeTasks().remove(this);
        }

        return returnList;
    }

    private String getLanguageCode(Chronology chronology){

        int languageNID = chronology.getAssemblageNid();

        if(languageNID == TermAux.ENGLISH_LANGUAGE.getNid())
            return "en";
        else
            return "¯\\_(ツ)_/¯";

    }

    private String getTypeId(Chronology chronology){
        //Definition (core metadata concept)			-	900000000000550004
        //Fully specified name (core metadata concept)	-	900000000000003001
        //Synonym (core metadata concept) 				-	900000000000013009

        final int typeNid = ((LatestVersion<ObservableDescriptionVersion>)
                 rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(chronology.getNid())).get().getDescriptionTypeConceptNid();

        if(typeNid == TermAux.DEFINITION_DESCRIPTION_TYPE.getNid())
            return "900000000000550004";
        else if(typeNid == TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid())
            return "900000000000003001";
        else if(typeNid == TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid())
            return "900000000000013009";

        return "¯\\_(ツ)_/¯";
    }

    private String getTerm(Chronology chronology){
        return ((LatestVersion<ObservableDescriptionVersion>) rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(chronology.getNid())).get().getText();
    }

    private String getCaseSignificanceId(Chronology chronology){
        //Entire term case insensitive (core metadata concept)				-	900000000000448009
        //Entire term case sensitive (core metadata concept)				-	900000000000017005
        //Only initial character case insensitive (core metadata concept)	-	900000000000020002

        final int caseSigNid = ((LatestVersion<ObservableDescriptionVersion>)
                rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(chronology.getNid())).get().getCaseSignificanceConceptNid();


        if( caseSigNid == TermAux.DESCRIPTION_NOT_CASE_SENSITIVE.getNid())
            return "900000000000448009";
        else if(caseSigNid == TermAux.DESCRIPTION_CASE_SENSITIVE.getNid())
            return "900000000000017005";
        else if(caseSigNid == TermAux.DESCRIPTION_INITIAL_CHARACTER_SENSITIVE.getNid())
            return "900000000000020002";

        return "¯\\_(ツ)_/¯";
    }
}
