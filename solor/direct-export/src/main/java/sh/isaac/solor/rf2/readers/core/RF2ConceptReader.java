package sh.isaac.solor.rf2.readers.core;

import org.apache.commons.lang.ArrayUtils;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.solor.rf2.config.RF2Configuration;
import sh.isaac.solor.rf2.utility.RF2ExportHelper;
import sh.isaac.solor.rf2.utility.RF2FileWriter;
import sh.komet.gui.manifold.Manifold;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.stream.Stream;

public class RF2ConceptReader extends TimedTaskWithProgressTracker<Void> {

    private final RF2ExportHelper rf2ExportHelper;
    private final Stream<? extends Chronology> streamPage;
    private final Semaphore readSemaphore;
    private final Manifold manifold;
    private final RF2Configuration rf2Configuration;
    private final RF2FileWriter rf2FileWriter = new RF2FileWriter();

    public RF2ConceptReader(Stream streamPage, Semaphore readSemaphore, Manifold manifold,
                            RF2Configuration rf2Configuration, long pageSize) {
        this.streamPage = streamPage;
        this.readSemaphore = readSemaphore;
        this.manifold = manifold;
        this.rf2Configuration = rf2Configuration;

        rf2ExportHelper = new RF2ExportHelper(this.manifold);

        readSemaphore.acquireUninterruptibly();

        updateTitle("Reading " + this.rf2Configuration.getMessage() + " batch of size: " + pageSize);
        updateMessage("Processing batch of concepts for RF2 Export");
        addToTotalWork(pageSize + 1);
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() {
        final ArrayList<Byte[]> writeBytes = new ArrayList<>();

        try{

            this.streamPage
                    .forEach(chronology -> {
                        writeBytes.add(ArrayUtils.toObject(
                                this.rf2ExportHelper.getRF2CommonElements(chronology)
                                .append(getConceptPrimitiveOrSufficientDefinedSCTID(chronology.getNid()))
                                .append("\r")
                                .toString().getBytes(Charset.forName("UTF-8"))));
                        completedUnitOfWork();
                    });

//            this.streamPage
//                    .flatMap(chronology -> chronology.getVersionList().stream())
//                    .forEach(version -> {
//                        writeBytes.add(ArrayUtils.toObject(this.rf2ExportHelper.getRF2CommonElements(version)
//                            .append(getConceptPrimitiveOrSufficientDefinedSCTID(version.getNid()))
//                            .append("\r")
//                            .toString().getBytes(Charset.forName("UTF-8"))));
//                        completedUnitOfWork();
//                    });

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

    private String getConceptPrimitiveOrSufficientDefinedSCTID(int conceptNid) {
        //primitive vs sufficiently
        //If the stated definition contains a sufficient set, it is sufficiently defined, if not it is primitive.
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
