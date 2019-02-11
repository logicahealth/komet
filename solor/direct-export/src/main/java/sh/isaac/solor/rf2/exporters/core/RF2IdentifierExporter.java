package sh.isaac.solor.rf2.exporters.core;

import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.observable.semantic.version.ObservableLongVersion;
import sh.isaac.api.observable.semantic.version.ObservableStringVersion;
import sh.isaac.api.observable.semantic.version.brittle.ObservableLoincVersion;
import sh.isaac.solor.rf2.config.RF2Configuration;
import sh.isaac.solor.rf2.exporters.RF2DefaultExporter;
import sh.isaac.solor.rf2.utility.RF2ExportHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.IntStream;

public class RF2IdentifierExporter extends RF2DefaultExporter {

    private final RF2ExportHelper rf2ExportHelper;
    private final IntStream intStream;
    private final Semaphore readSemaphore;

    public RF2IdentifierExporter(RF2Configuration rf2Configuration, RF2ExportHelper rf2ExportHelper, IntStream intStream, Semaphore readSemaphore) {
        super(rf2Configuration);
        this.rf2ExportHelper = rf2ExportHelper;
        this.intStream = intStream;
        this.readSemaphore = readSemaphore;

        readSemaphore.acquireUninterruptibly();
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() {

        List<Integer> retroNIds = new ArrayList<>();
        retroNIds.add(MetaData.SOLOR_CONCEPT____SOLOR.getNid());
        retroNIds.add(MetaData.METADATA____SOLOR.getNid());
        retroNIds.add(MetaData.BODY_STRUCTURE____SOLOR.getNid());
        retroNIds.add(MetaData.EVENT____SOLOR.getNid());
        retroNIds.add(MetaData.FORCE____SOLOR.getNid());
        retroNIds.add(MetaData.MEDICATION____SOLOR.getNid());
        retroNIds.add(MetaData.PHENOMENON____SOLOR.getNid());
        retroNIds.add(MetaData.ORGANISM____SOLOR.getNid());
        retroNIds.add(MetaData.OBJECT____SOLOR.getNid());
        retroNIds.add(MetaData.PROCEDURE____SOLOR.getNid());
        retroNIds.add(MetaData.SPECIMEN____SOLOR.getNid());
        retroNIds.add(MetaData.SUBSTANCE____SOLOR.getNid());
        retroNIds.add(MetaData.HEALTH_CONCEPT____SOLOR.getNid());
        retroNIds.add(MetaData.UNCATEGORIZED_PHENOMENON____SOLOR.getNid());
        retroNIds.add(MetaData.FINDING____SOLOR.getNid());
        retroNIds.add(MetaData.OBSERVATION____SOLOR.getNid());

        try{

            this.intStream
                    .forEach(nid -> {

                        switch (Get.identifierService().getObjectTypeForComponent(nid)){
                            case CONCEPT:

                                final StringBuilder conceptUUIDStringBuilder = new StringBuilder();
                                int conceptSTAMPNid = rf2ExportHelper.getSnapshotService()
                                        .getObservableConceptVersion(nid).getStamps().findFirst().getAsInt();

                                super.writeStringToFile(
                                        conceptUUIDStringBuilder.append("900000000000002006" + "\t")
                                                .append(Get.concept(nid).getPrimordialUuid().toString() + "\t")
                                                .append(rf2ExportHelper.getTimeString(conceptSTAMPNid) + "\t")
                                                .append(rf2ExportHelper.getActiveString(conceptSTAMPNid) + "\t")
                                                .append(rf2ExportHelper.getModuleString(conceptSTAMPNid) + "\t")
                                                .append(rf2ExportHelper.getIdString(nid))
                                                .append("\r")
                                                .toString()
                                );

                                if(rf2ExportHelper.isMetaDataConcept(nid) || retroNIds.contains(nid))
                                    super.writeStringToFile(
                                            conceptUUIDStringBuilder.append(rf2ExportHelper.getIdString(MetaData.RETRO_ID____SOLOR.getNid()) + "\t")
                                                    .append(Get.concept(nid).getPrimordialUuid().toString() + "\t")
                                                    .append(rf2ExportHelper.getTimeString(conceptSTAMPNid) + "\t")
                                                    .append(rf2ExportHelper.getActiveString(conceptSTAMPNid) + "\t")
                                                    .append(rf2ExportHelper.getModuleString(conceptSTAMPNid) + "\t")
                                                    .append(rf2ExportHelper.getIdString(nid))
                                                    .append("\r")
                                                    .toString());

                                break;
                            case SEMANTIC:

                                final StringBuilder semanticUUIDStringBuilder = new StringBuilder();
                                int semanticSTAMPNid = rf2ExportHelper.getSnapshotService().
                                        getObservableSemanticVersion(nid).getStamps().findFirst().getAsInt();

                                super.writeStringToFile(
                                        semanticUUIDStringBuilder.append("900000000000002006" + "\t")
                                                .append(Get.assemblageService().getSemanticChronology(nid).getPrimordialUuid().toString() + "\t")
                                                .append(rf2ExportHelper.getTimeString(semanticSTAMPNid) + "\t")
                                                .append(rf2ExportHelper.getActiveString(semanticSTAMPNid) + "\t")
                                                .append(rf2ExportHelper.getModuleString(semanticSTAMPNid) + "\t")
                                                .append(rf2ExportHelper.getIdString(nid))
                                                .append("\r")
                                                .toString()
                                );

                                switch (Get.assemblageService().getSemanticChronology(nid).getVersionType()){
                                    case LOINC_RECORD:
                                        final StringBuilder loincRecordStringBuilder = new StringBuilder();
                                        ObservableLoincVersion observableLoincVersion =
                                                ((LatestVersion<ObservableLoincVersion>)
                                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(nid))
                                                        .get();
                                        super.writeStringToFile(
                                                loincRecordStringBuilder.append(rf2ExportHelper.getIdString(MetaData.LOINC_ID_ASSEMBLAGE____SOLOR.getNid()) + "\t")
                                                        .append(observableLoincVersion.getLoincNum() + "\t")
                                                        .append(rf2ExportHelper.getTimeString(semanticSTAMPNid) + "\t")
                                                        .append(rf2ExportHelper.getActiveString(semanticSTAMPNid) + "\t")
                                                        .append(rf2ExportHelper.getModuleString(semanticSTAMPNid) + "\t")
                                                        .append(rf2ExportHelper.getIdString(Get.assemblageService().getSemanticChronology(nid).getReferencedComponentNid()))
                                                        .append("\r")
                                                        .toString()
                                        );

                                        break;
                                    case LONG:
                                        final StringBuilder longStringBuilder = new StringBuilder();
                                        ObservableLongVersion observableLongVersion =
                                                ((LatestVersion<ObservableLongVersion>)
                                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(nid))
                                                        .get();
                                        super.writeStringToFile(
                                                longStringBuilder.append(rf2ExportHelper.getIdString(rf2ExportHelper.getIndentifierAssemblageConceptNID(nid))+ "\t")
                                                        .append(observableLongVersion.getLongValue() + "\t")
                                                        .append(rf2ExportHelper.getTimeString(semanticSTAMPNid) + "\t")
                                                        .append(rf2ExportHelper.getActiveString(semanticSTAMPNid) + "\t")
                                                        .append(rf2ExportHelper.getModuleString(semanticSTAMPNid) + "\t")
                                                        .append(rf2ExportHelper.getIdString(Get.assemblageService().getSemanticChronology(nid).getReferencedComponentNid()))
                                                        .append("\r")
                                                        .toString()
                                        );

                                        break;
                                    case STRING:
                                        final StringBuilder stringStringBuilder = new StringBuilder();
                                        ObservableStringVersion observableStringVersion =
                                                ((LatestVersion<ObservableStringVersion>)
                                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(nid))
                                                        .get();
                                        super.writeStringToFile(
                                                stringStringBuilder.append(rf2ExportHelper.getIndentifierAssemblageConceptNID(nid)+ "\t")
                                                        .append(observableStringVersion.getString() + "\t")
                                                        .append(rf2ExportHelper.getTimeString(semanticSTAMPNid) + "\t")
                                                        .append(rf2ExportHelper.getActiveString(semanticSTAMPNid) + "\t")
                                                        .append(rf2ExportHelper.getModuleString(semanticSTAMPNid) + "\t")
                                                        .append(rf2ExportHelper.getIdString(Get.assemblageService().getSemanticChronology(nid).getReferencedComponentNid()))
                                                        .append("\r")
                                                        .toString()
                                        );

                                        break;
                                }
                                break;
                        }

                    });

        }finally {
            this.readSemaphore.release();
            Get.activeTasks().remove(this);
        }

        return null;
    }
}
