package sh.isaac.solor.rf2.exporters.refsets;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.observable.semantic.version.ObservableComponentNidVersion;
import sh.isaac.api.observable.semantic.version.ObservableLongVersion;
import sh.isaac.api.observable.semantic.version.ObservableStringVersion;
import sh.isaac.api.observable.semantic.version.brittle.*;
import sh.isaac.solor.rf2.config.RF2Configuration;
import sh.isaac.solor.rf2.exporters.RF2DefaultExporter;
import sh.isaac.solor.rf2.utility.RF2ExportHelper;

import java.util.concurrent.Semaphore;
import java.util.stream.IntStream;

public class RF2RefsetExporter extends RF2DefaultExporter {

    private final RF2ExportHelper rf2ExportHelper;
    private final IntStream intStream;
    private final Semaphore readSemaphore;
    private final RF2Configuration rf2Configuration;

    public RF2RefsetExporter(RF2Configuration rf2Configuration, RF2ExportHelper rf2ExportHelper, IntStream intStream, Semaphore readSemaphore) {
        super(rf2Configuration);
        this.rf2ExportHelper = rf2ExportHelper;
        this.intStream = intStream;
        this.readSemaphore = readSemaphore;
        this.rf2Configuration = rf2Configuration;

        readSemaphore.acquireUninterruptibly();
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() {

        try{

            this.intStream
                    .forEach(nid -> {
                        final StringBuilder refsetRow = new StringBuilder();

                        String refsetID = this.rf2ExportHelper.getIdString(Get.assemblageService().getSemanticChronology(nid).getAssemblageNid());
                        String referenceComponentID = this.rf2ExportHelper
                                .getIdString(Get.assemblageService().getSemanticChronology(nid).getReferencedComponentNid());
                        int stampNid = rf2ExportHelper.getSnapshotService()
                                .getObservableSemanticVersion(nid).getStamps().findFirst().getAsInt();

                        refsetRow.append(Get.assemblageService().getSemanticChronology(nid).getPrimordialUuid().toString() + "\t")
                                .append(this.rf2ExportHelper.getTimeString(stampNid) + "\t")
                                .append(this.rf2ExportHelper.getActiveString(stampNid) + "\t")
                                .append(this.rf2ExportHelper.getModuleString(stampNid) + "\t")
                                .append(refsetID + "\t")
                                .append(referenceComponentID + "\t");

                        switch (Get.assemblageService().getSemanticChronology(nid).getVersionType()) {
                            case MEMBER:
                                break;
                            case Nid1_Int2:
                                Observable_Nid1_Int2_Version observable_nid1_int2_version =
                                        ((LatestVersion<Observable_Nid1_Int2_Version>)
                                                rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(nid))
                                                .get();
                                refsetRow.append(this.rf2ExportHelper.getIdString(observable_nid1_int2_version.getNid1()) + "\t")
                                        .append(observable_nid1_int2_version.getInt2() + "\t");
                                break;
                            case Nid1_Nid2:
                                Observable_Nid1_Nid2_Version observable_nid1_nid2_version =
                                        ((LatestVersion<Observable_Nid1_Nid2_Version>)
                                                rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(nid))
                                                .get();
                                refsetRow.append(this.rf2ExportHelper.getIdString(observable_nid1_nid2_version.getNid1()) + "\t")
                                        .append(this.rf2ExportHelper.getIdString(observable_nid1_nid2_version.getNid2()) + "\t");
                                break;
                            case Nid1_Str2:
                                Observable_Nid1_Str2_Version observable_nid1_str2_version =
                                        ((LatestVersion<Observable_Nid1_Str2_Version>)
                                                rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(nid))
                                                .get();
                                refsetRow.append(this.rf2ExportHelper.getIdString(observable_nid1_str2_version.getNid1()) + "\t")
                                        .append(observable_nid1_str2_version.getStr2() + "\t");
                                break;
                            case Str1_Str2:
                                Observable_Str1_Str2_Version observable_str1_str2_version =
                                        ((LatestVersion<Observable_Str1_Str2_Version>)
                                                rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(nid))
                                                .get();
                                refsetRow.append(observable_str1_str2_version.getStr1() + "\t")
                                        .append(observable_str1_str2_version.getStr2() + "\t");
                                break;
                            case Nid1_Nid2_Str3:
                                Observable_Nid1_Nid2_Str3_Version observable_nid1_nid2_str3_version =
                                        ((LatestVersion<Observable_Nid1_Nid2_Str3_Version>)
                                                rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(nid))
                                                .get();

                                refsetRow.append(this.rf2ExportHelper.getIdString(observable_nid1_nid2_str3_version.getNid1()) + "\t")
                                        .append(this.rf2ExportHelper.getIdString(observable_nid1_nid2_str3_version.getNid2()) + "\t")
                                        .append(observable_nid1_nid2_str3_version.getStr3() + "\t");
                                break;
                            case Nid1_Nid2_Int3:
                                Observable_Nid1_Nid2_Int3_Version observable_nid1_nid2_int3_version =
                                        ((LatestVersion<Observable_Nid1_Nid2_Int3_Version>)
                                                rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(nid))
                                                .get();
                                refsetRow.append(this.rf2ExportHelper.getIdString(observable_nid1_nid2_int3_version.getNid1()) + "\t")
                                        .append(this.rf2ExportHelper.getIdString(observable_nid1_nid2_int3_version.getNid2()) + "\t")
                                        .append(observable_nid1_nid2_int3_version.getInt3() + "\t");
                                break;
                            case Str1_Nid2_Nid3_Nid4:
                                Observable_Str1_Nid2_Nid3_Nid4_Version observable_str1_nid2_nid3_nid4_version =
                                        ((LatestVersion<Observable_Str1_Nid2_Nid3_Nid4_Version>)
                                                rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(nid))
                                                .get();
                                refsetRow.append(observable_str1_nid2_nid3_nid4_version.getStr1() + "\t")
                                        .append(this.rf2ExportHelper.getIdString(observable_str1_nid2_nid3_nid4_version.getNid2()) + "\t")
                                        .append(this.rf2ExportHelper.getIdString(observable_str1_nid2_nid3_nid4_version.getNid3()) + "\t")
                                        .append(this.rf2ExportHelper.getIdString(observable_str1_nid2_nid3_nid4_version.getNid4()) + "\t");
                                break;
                            case Str1_Str2_Nid3_Nid4:
                                Observable_Str1_Str2_Nid3_Nid4_Version observable_str1_str2_nid3_nid4_version =
                                        ((LatestVersion<Observable_Str1_Str2_Nid3_Nid4_Version>)
                                                rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(nid))
                                                .get();
                                refsetRow.append(observable_str1_str2_nid3_nid4_version.getStr1() + "\t")
                                        .append(observable_str1_str2_nid3_nid4_version.getStr2() + "\t")
                                        .append(this.rf2ExportHelper.getIdString(observable_str1_str2_nid3_nid4_version.getNid3()) + "\t")
                                        .append(this.rf2ExportHelper.getIdString(observable_str1_str2_nid3_nid4_version.getNid4()) + "\t");
                                break;
                            case Str1_Str2_Nid3_Nid4_Nid5:
                                Observable_Str1_Str2_Nid3_Nid4_Nid5_Version observable_str1_str2_nid3_nid4_nid5_version =
                                        ((LatestVersion<Observable_Str1_Str2_Nid3_Nid4_Nid5_Version>)
                                                rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(nid))
                                                .get();
                                refsetRow.append(observable_str1_str2_nid3_nid4_nid5_version.getStr1() + "\t")
                                        .append(observable_str1_str2_nid3_nid4_nid5_version.getStr2() + "\t")
                                        .append(this.rf2ExportHelper.getIdString(observable_str1_str2_nid3_nid4_nid5_version.getNid3()) + "\t")
                                        .append(this.rf2ExportHelper.getIdString(observable_str1_str2_nid3_nid4_nid5_version.getNid4()) + "\t")
                                        .append(this.rf2ExportHelper.getIdString(observable_str1_str2_nid3_nid4_nid5_version.getNid5()) + "\t");
                                break;
                            case Nid1_Int2_Str3_Str4_Nid5_Nid6:
                                Observable_Nid1_Int2_Str3_Str4_Nid5_Nid6_Version observable_nid1_int2_str3_str4_nid5_nid6_version =
                                        ((LatestVersion<Observable_Nid1_Int2_Str3_Str4_Nid5_Nid6_Version>)
                                                rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(nid))
                                                .get();
                                refsetRow.append(this.rf2ExportHelper.getIdString(observable_nid1_int2_str3_str4_nid5_nid6_version.getNid1()) + "\t")
                                        .append(observable_nid1_int2_str3_str4_nid5_nid6_version.getInt2() + "\t")
                                        .append(observable_nid1_int2_str3_str4_nid5_nid6_version.getStr3() + "\t")
                                        .append(observable_nid1_int2_str3_str4_nid5_nid6_version.getStr4() + "\t")
                                        .append(this.rf2ExportHelper.getIdString(observable_nid1_int2_str3_str4_nid5_nid6_version.getNid5()) + "\t")
                                        .append(this.rf2ExportHelper.getIdString(observable_nid1_int2_str3_str4_nid5_nid6_version.getNid6()) + "\t");
                                break;
                            case Int1_Int2_Str3_Str4_Str5_Nid6_Nid7:
                                Observable_Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version observable_int1_int2_str3_str4_str5_nid6_nid7_version =
                                        ((LatestVersion<Observable_Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version>)
                                                rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(nid))
                                                .get();
                                refsetRow.append(observable_int1_int2_str3_str4_str5_nid6_nid7_version.getInt1() + "\t")
                                        .append(observable_int1_int2_str3_str4_str5_nid6_nid7_version.getInt2() + "\t")
                                        .append(observable_int1_int2_str3_str4_str5_nid6_nid7_version.getStr3() + "\t")
                                        .append(observable_int1_int2_str3_str4_str5_nid6_nid7_version.getStr4() + "\t")
                                        .append(observable_int1_int2_str3_str4_str5_nid6_nid7_version.getStr5() + "\t")
                                        .append(this.rf2ExportHelper.getIdString(observable_int1_int2_str3_str4_str5_nid6_nid7_version.getNid6()) + "\t")
                                        .append(this.rf2ExportHelper.getIdString(observable_int1_int2_str3_str4_str5_nid6_nid7_version.getNid7()) + "\t");
                                break;
                            case Str1_Str2_Str3_Str4_Str5_Str6_Str7:
                                Observable_Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version observable_str1_str2_str3_str4_str5_str6_str7_version =
                                        ((LatestVersion<Observable_Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version>)
                                                rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(nid))
                                                .get();
                                refsetRow.append(observable_str1_str2_str3_str4_str5_str6_str7_version.getStr1() + "\t")
                                        .append(observable_str1_str2_str3_str4_str5_str6_str7_version.getStr2() + "\t")
                                        .append(observable_str1_str2_str3_str4_str5_str6_str7_version.getStr3() + "\t")
                                        .append(observable_str1_str2_str3_str4_str5_str6_str7_version.getStr4() + "\t")
                                        .append(observable_str1_str2_str3_str4_str5_str6_str7_version.getStr5() + "\t")
                                        .append(observable_str1_str2_str3_str4_str5_str6_str7_version.getStr6() + "\t")
                                        .append(observable_str1_str2_str3_str4_str5_str6_str7_version.getStr7() + "\t");
                                break;
                            case LONG:
                                ObservableLongVersion observableLongVersion =
                                        ((LatestVersion<ObservableLongVersion>)
                                                rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(nid))
                                                .get();
                                refsetRow.append(observableLongVersion.getLongValue() + "\t");
                                break;
                            case STRING:
                                ObservableStringVersion observableStringVersion =
                                        ((LatestVersion<ObservableStringVersion>)
                                                rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(nid))
                                                .get();
                                refsetRow.append(observableStringVersion.getString() + "\t");
                                break;
                            case DYNAMIC:

                                break;
                            case LOINC_RECORD:
                                ObservableLoincVersion observableLoincVersion =
                                        ((LatestVersion<ObservableLoincVersion>)
                                                rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(nid))
                                                .get();
                                refsetRow.append(observableLoincVersion.getComponent() + "\t")
                                        .append(observableLoincVersion.getLoincNum() + "\t")
                                        .append(observableLoincVersion.getLongCommonName() + "\t")
                                        .append(observableLoincVersion.getMethodType() + "\t")
                                        .append(observableLoincVersion.getProperty() + "\t")
                                        .append(observableLoincVersion.getScaleType() + "\t")
                                        .append(observableLoincVersion.getShortName() + "\t")
                                        .append(observableLoincVersion.getLoincStatus() + "\t")
                                        .append(observableLoincVersion.getSystem() + "\t")
                                        .append(observableLoincVersion.getTimeAspect() + "\t");
                                break;
                            case COMPONENT_NID:
                                ObservableComponentNidVersion observableComponentNidVersion =
                                        ((LatestVersion<ObservableComponentNidVersion>)
                                                rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(nid))
                                                .get();
                                refsetRow.append(this.rf2ExportHelper.getIdString(observableComponentNidVersion.getComponentNid()) + "\t");
                                break;
                        }

                        super.writeStringToFile(refsetRow.append("\r").toString());
                    });

            //Write out the rf2configuration additional List of descriptor stuff
            if(this.rf2Configuration.isDescriptorAssemblage()){
                writeStringsToFile(this.rf2Configuration.getRefsetDescriptorDefinitions());
            }


        }finally {
            this.readSemaphore.release();
            Get.activeTasks().remove(this);
        }

        return null;
    }
}
