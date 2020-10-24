package sh.isaac.solor.sof;

import org.apache.mahout.math.set.OpenIntHashSet;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.collections.IntSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.externalizable.DataWriterService;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.model.ChronologyImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


public class SofExporter extends TimedTaskWithProgressTracker<Void> {


    private final OpenIntHashSet moduleSet = new OpenIntHashSet();
    private final OpenIntHashSet pathSet = new OpenIntHashSet();
    private final File exportFile;
    long workToDo = 0;
    AtomicLong workDone = new AtomicLong();
    AtomicInteger conceptExportCount = new AtomicInteger();
    AtomicInteger semanticExportCount = new AtomicInteger();

    public SofExporter(IntSet moduleSet, IntSet pathSet, File exportFile) {
        moduleSet.stream().forEach(nid -> this.moduleSet.add(nid));
        pathSet.stream().forEach(nid -> this.pathSet.add(nid));
        this.exportFile = exportFile;
        Get.activeTasks().add(this);
    }

    public SofExporter(Collection<ConceptSpecification> moduleSet, Collection<ConceptSpecification> pathSet, File exportFile) {
        moduleSet.stream().forEach(specification -> this.moduleSet.add(specification.getNid()));
        pathSet.stream().forEach(specification -> this.pathSet.add(specification.getNid()));
        this.exportFile = exportFile;
        Get.activeTasks().add(this);
    }

    ConceptProxy watchConcept = new ConceptProxy("Coombs test, indirect, titer (HDx)", UUID.fromString("dee1ab39-8613-5ac9-94fe-e4cfb1b8388f"));

    @Override
    protected Void call() throws Exception {
        try {
            updateMessage("Counting concepts");
            workToDo = Get.conceptService().getConceptChronologyStream(true).count();
            LOG.info("Concept count: " + workToDo);
            updateMessage("Counting semantics");
            long semanticCount = Get.assemblageService().getSemanticChronologyStream(true).count();
            LOG.info("Semantic count: " + semanticCount);
            workToDo = workToDo + semanticCount;

            addToTotalWork(workToDo);
            updateMessage("Exporting concepts");


            DataWriterService writer = Get.binaryDataWriter(exportFile.toPath());
            Get.conceptService().getConceptChronologyStream(false).forEach(conceptChronology -> {
                if (watchConcept.getNid() == conceptChronology.getNid()) {
                    LOG.info("Found watch: " + conceptChronology);
                }
                conditionalWrite(writer, conceptChronology);
                updateProgress();
            });

            updateMessage("Exporting semantics");
            Get.assemblageService().getSemanticChronologyStream(false).forEach(semanticChronology -> {
                if (semanticChronology.getReferencedComponentNid() == watchConcept.getNid()) {
                    LOG.info("Found watch: " + semanticChronology);
                }
                List<Version> versions = semanticChronology.getVersionList();
                conditionalWrite(writer, semanticChronology);
                updateProgress();
            });
            writer.flush();
            writer.close();
            updateMessage("Export complete");
            LOG.info("Exported " + conceptExportCount + " concepts, " + semanticExportCount + " semantics. ");
            return null;
        } finally {
            Get.activeTasks().remove(this);
        }
    }

    private void conditionalWrite(DataWriterService writer, Chronology chronology) {
        List<Version> versions = chronology.getVersionList();
        List<Version> versionsToWrite = new ArrayList<>();

        for (Version version: versions) {
            if (moduleSet.isEmpty() || moduleSet.contains(version.getModuleNid())) {
                if (pathSet.isEmpty() || pathSet.contains(version.getPathNid())) {
                    versionsToWrite.add(version);
                }
            }
        }
        if (!versionsToWrite.isEmpty()) {
            ((ChronologyImpl) chronology).setVersions(versionsToWrite);
            switch (chronology.getIsaacObjectType()) {
                case CONCEPT:
                    conceptExportCount.incrementAndGet();
                    break;
                case SEMANTIC:
                    semanticExportCount.incrementAndGet();
                    break;
            }
            writer.put(chronology);
        }
    }

    private void updateProgress() {
        workDone.incrementAndGet();
        if (workDone.get() % 1000 == 0) {
            completedUnitsOfWork(1000);
        }
    }
}
