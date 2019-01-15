package sh.isaac.solor.rf2.readers.core;

import org.apache.commons.lang.ArrayUtils;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.observable.semantic.version.ObservableDescriptionVersion;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.solor.rf2.config.RF2Configuration;
import sh.isaac.solor.rf2.utility.RF2ExportHelper;
import sh.isaac.solor.rf2.utility.RF2FileWriter;
import sh.komet.gui.manifold.Manifold;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.stream.Stream;

public class RF2DescriptionReader extends TimedTaskWithProgressTracker<Void> {

    private final RF2ExportHelper rf2ExportHelper;
    private final Stream<? extends Chronology> streamPage;
    private final Semaphore readSemaphore;
    private final Manifold manifold;
    private final RF2Configuration rf2Configuration;
    private final RF2FileWriter rf2FileWriter;

    public RF2DescriptionReader(Stream streamPage, Semaphore readSemaphore, Manifold manifold,
                                RF2Configuration rf2Configuration, long pageSize) {
        this.streamPage = streamPage;
        this.readSemaphore = readSemaphore;
        this.manifold = manifold;
        this.rf2Configuration = rf2Configuration;
        rf2ExportHelper = new RF2ExportHelper(this.manifold);
        this.rf2FileWriter = new RF2FileWriter();

        readSemaphore.acquireUninterruptibly();

        updateTitle("Reading " + this.rf2Configuration.getMessage() + " batch of size: " + pageSize);
        updateMessage("Processing batch of descriptions for RF2 Export");
        addToTotalWork(pageSize + 1);
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() throws Exception {
        ArrayList<Byte[]> writeBytes = new ArrayList<>();

        try{

            this.streamPage
                    .forEach(chronology -> {
                        writeBytes.add(ArrayUtils.toObject(rf2ExportHelper.getRF2CommonElements(chronology)
                                .append(rf2ExportHelper.getIdString(Get.concept(((SemanticChronology) chronology).getReferencedComponentNid())) + "\t") //conceptId
                                .append(getLanguageCode(chronology) + "\t") //languageCode
                                .append(getTypeId(chronology) + "\t") //typeId
                                .append(getTerm(chronology) + "\t") //term
                                .append(getCaseSignificanceId(chronology)) //caseSignificanceId
                                .append("\r")
                                .toString().getBytes(Charset.forName("UTF-8"))));
                        completedUnitOfWork();

                    });

            updateTitle("Writing " + rf2Configuration.getMessage() + " RF2 file");
            updateMessage("Writing to " + rf2Configuration.getFilePath());

            rf2FileWriter.writeToFile(writeBytes, this.rf2Configuration);

            completedUnitOfWork();


        }finally {
            this.readSemaphore.release();
            Get.activeTasks().remove(this);
        }

        return null;
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
