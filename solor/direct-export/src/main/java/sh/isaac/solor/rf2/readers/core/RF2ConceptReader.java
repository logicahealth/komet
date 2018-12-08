package sh.isaac.solor.rf2.readers.core;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.solor.rf2.utility.RF2ExportHelper;
import sh.komet.gui.manifold.Manifold;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;

public class RF2ConceptReader extends TimedTaskWithProgressTracker<List<String>> {

    private final RF2ExportHelper rf2ExportHelper;
    private final List<Chronology> chronologies;
    private final Semaphore readSemaphore;
    private final Manifold manifold;

    public RF2ConceptReader(List<Chronology> chronologies, Semaphore readSemaphore, Manifold manifold, String message) {
        this.chronologies = chronologies;
        this.readSemaphore = readSemaphore;
        this.manifold = manifold;
        rf2ExportHelper = new RF2ExportHelper(this.manifold);

        readSemaphore.acquireUninterruptibly();

        updateTitle("Reading " + message + " batch of size: " + chronologies.size());
        updateMessage("Processing batch of concepts for RF2 Export");
        addToTotalWork(chronologies.size());
        Get.activeTasks().add(this);
    }

    @Override
    protected List<String> call() {
        ArrayList<String> returnList = new ArrayList<>();

        try{

            for(Chronology chronology : chronologies){
                returnList.add(this.rf2ExportHelper.getRF2CommonElements(chronology)
                        .append(getConceptPrimitiveOrSufficientDefinedSCTID(chronology.getNid()))
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

    private String getConceptPrimitiveOrSufficientDefinedSCTID(int conceptNid) {
        //primitive vs sufficiently
        //If the stated definition contains a sufficient set, it is sufficently defined, if not it is primitive.
        Optional<LogicalExpression> conceptExpression = this.manifold.getLogicalExpression(conceptNid, PremiseType.STATED);

        if (!conceptExpression.isPresent()) {
            return "900000000000074008"; //This seems to happen e.g. SOLOR Concept & SOLOR Concept (SOLOR)
        }else{
            if(conceptExpression.get().contains(NodeSemantic.SUFFICIENT_SET)){
                return "900000000000073002"; //sufficiently defined SCTID
            }else{
                return "900000000000074008"; //primitive SCTID
            }
        }
    }
}
