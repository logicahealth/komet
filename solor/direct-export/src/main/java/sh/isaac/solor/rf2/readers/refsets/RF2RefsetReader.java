package sh.isaac.solor.rf2.readers.refsets;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.observable.semantic.version.ObservableComponentNidVersion;
import sh.isaac.api.observable.semantic.version.ObservableLongVersion;
import sh.isaac.api.observable.semantic.version.ObservableStringVersion;
import sh.isaac.api.observable.semantic.version.brittle.*;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.solor.rf2.config.RF2Configuration;
import sh.isaac.solor.rf2.utility.RF2ExportHelper;
import sh.komet.gui.manifold.Manifold;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class RF2RefsetReader extends TimedTaskWithProgressTracker<List<String>> {

    private final RF2ExportHelper rf2ExportHelper;
    private final List<Chronology> chronologies;
    private final Semaphore readSemaphore;
    private final Manifold manifold;

    public RF2RefsetReader(List<Chronology> chronologies, Semaphore readSemaphore, Manifold manifold, String message) {
        this.chronologies = chronologies;
        this.readSemaphore = readSemaphore;
        this.manifold = manifold;
        this.rf2ExportHelper = new RF2ExportHelper(this.manifold);

        readSemaphore.acquireUninterruptibly();

        updateTitle("Reading " + message + " assemblage batch of size: " + chronologies.size());
        updateMessage("Processing batch of " + message + " concepts for RF2 Export");
        addToTotalWork(chronologies.size());
        Get.activeTasks().add(this);
    }

    @Override
    protected List<String> call() {
        ArrayList<String> returnList = new ArrayList<>();

        try{

            for(Chronology semanticChronology : this.chronologies){

                StringBuilder refsetRow = new StringBuilder();

                String refsetID = this.rf2ExportHelper.getIdString(Get.concept(semanticChronology.getAssemblageNid()));
                String referenceComponentID = this.rf2ExportHelper
                        .getIdString(((SemanticChronology)semanticChronology).getReferencedComponentNid());
                int stampNid = rf2ExportHelper.getSnapshotService()
                        .getObservableSemanticVersion(semanticChronology.getNid()).getStamps().findFirst().getAsInt();

                refsetRow.append(semanticChronology.getPrimordialUuid().toString() + "\t")
                        .append(this.rf2ExportHelper.getTimeString(stampNid) + "\t")
                        .append(this.rf2ExportHelper.getActiveString(stampNid) + "\t")
                        .append(this.rf2ExportHelper.getModuleString(stampNid) + "\t")
                        .append(refsetID + "\t")
                        .append(referenceComponentID + "\t");

                switch (semanticChronology.getVersionType()) {
                    case MEMBER:
                    case CONCEPT:
                        break;
                    case Nid1_Int2:
                        Observable_Nid1_Int2_Version observable_nid1_int2_version =
                                ((LatestVersion<Observable_Nid1_Int2_Version>)
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(semanticChronology.getNid()))
                                        .get();
                        refsetRow.append(this.rf2ExportHelper.getIdString(observable_nid1_int2_version.getNid1()) + "\t")
                                .append(observable_nid1_int2_version.getInt2() + "\t");
                        break;
                    case Nid1_Nid2:
                        Observable_Nid1_Nid2_Version observable_nid1_nid2_version =
                                ((LatestVersion<Observable_Nid1_Nid2_Version>)
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(semanticChronology.getNid()))
                                        .get();
                        refsetRow.append(this.rf2ExportHelper.getIdString(observable_nid1_nid2_version.getNid1()) + "\t")
                                .append(this.rf2ExportHelper.getIdString(observable_nid1_nid2_version.getNid2()) + "\t");
                        break;
                    case Nid1_Str2:
                        Observable_Nid1_Str2_Version observable_nid1_str2_version =
                                ((LatestVersion<Observable_Nid1_Str2_Version>)
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(semanticChronology.getNid()))
                                        .get();
                        refsetRow.append(this.rf2ExportHelper.getIdString(observable_nid1_str2_version.getNid1()) + "\t")
                                .append(observable_nid1_str2_version.getStr2() + "\t");
                        break;
                    case Str1_Str2:
                        Observable_Str1_Str2_Version observable_str1_str2_version =
                                ((LatestVersion<Observable_Str1_Str2_Version>)
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(semanticChronology.getNid()))
                                        .get();
                        refsetRow.append(observable_str1_str2_version.getStr1() + "\t")
                                .append(observable_str1_str2_version.getStr2() + "\t");
                        break;
                    case Nid1_Nid2_Str3:
                        Observable_Nid1_Nid2_Str3_Version observable_nid1_nid2_str3_version =
                                ((LatestVersion<Observable_Nid1_Nid2_Str3_Version>)
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(semanticChronology.getNid()))
                                        .get();

                        refsetRow.append(this.rf2ExportHelper.getIdString(observable_nid1_nid2_str3_version.getNid1()) + "\t")
                                .append(this.rf2ExportHelper.getIdString(observable_nid1_nid2_str3_version.getNid2()) + "\t")
                                .append(observable_nid1_nid2_str3_version.getStr3() + "\t");
                        break;
                    case Nid1_Nid2_Int3:
                        Observable_Nid1_Nid2_Int3_Version observable_nid1_nid2_int3_version =
                                ((LatestVersion<Observable_Nid1_Nid2_Int3_Version>)
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(semanticChronology.getNid()))
                                        .get();
                        refsetRow.append(this.rf2ExportHelper.getIdString(observable_nid1_nid2_int3_version.getNid1()) + "\t")
                                .append(this.rf2ExportHelper.getIdString(observable_nid1_nid2_int3_version.getNid2()) + "\t")
                                .append(observable_nid1_nid2_int3_version.getInt3() + "\t");
                        break;
                    case Str1_Nid2_Nid3_Nid4:
                        Observable_Str1_Nid2_Nid3_Nid4_Version observable_str1_nid2_nid3_nid4_version =
                                ((LatestVersion<Observable_Str1_Nid2_Nid3_Nid4_Version>)
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(semanticChronology.getNid()))
                                        .get();
                        refsetRow.append(observable_str1_nid2_nid3_nid4_version.getStr1() + "\t")
                                .append(this.rf2ExportHelper.getIdString(observable_str1_nid2_nid3_nid4_version.getNid2()) + "\t")
                                .append(this.rf2ExportHelper.getIdString(observable_str1_nid2_nid3_nid4_version.getNid3()) + "\t")
                                .append(this.rf2ExportHelper.getIdString(observable_str1_nid2_nid3_nid4_version.getNid4()) + "\t");
                        break;
                    case Str1_Str2_Nid3_Nid4:
                        Observable_Str1_Str2_Nid3_Nid4_Version observable_str1_str2_nid3_nid4_version =
                                ((LatestVersion<Observable_Str1_Str2_Nid3_Nid4_Version>)
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(semanticChronology.getNid()))
                                        .get();
                        refsetRow.append(observable_str1_str2_nid3_nid4_version.getStr1() + "\t")
                                .append(observable_str1_str2_nid3_nid4_version.getStr2() + "\t")
                                .append(this.rf2ExportHelper.getIdString(observable_str1_str2_nid3_nid4_version.getNid3()) + "\t")
                                .append(this.rf2ExportHelper.getIdString(observable_str1_str2_nid3_nid4_version.getNid4()) + "\t");
                        break;
                    case Str1_Str2_Nid3_Nid4_Nid5:
                        Observable_Str1_Str2_Nid3_Nid4_Nid5_Version observable_str1_str2_nid3_nid4_nid5_version =
                                ((LatestVersion<Observable_Str1_Str2_Nid3_Nid4_Nid5_Version>)
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(semanticChronology.getNid()))
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
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(semanticChronology.getNid()))
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
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(semanticChronology.getNid()))
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
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(semanticChronology.getNid()))
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
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(semanticChronology.getNid()))
                                        .get();
                        refsetRow.append(observableLongVersion.getLongValue() + "\t");
                        break;
                    case STRING:
                        ObservableStringVersion observableStringVersion =
                                ((LatestVersion<ObservableStringVersion>)
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(semanticChronology.getNid()))
                                        .get();
                        refsetRow.append(observableStringVersion.getString() + "\t");
                        break;
                    case DYNAMIC:

                        break;
                    case LOINC_RECORD:
                        ObservableLoincVersion observableLoincVersion =
                                ((LatestVersion<ObservableLoincVersion>)
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(semanticChronology.getNid()))
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
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(semanticChronology.getNid()))
                                        .get();
                        refsetRow.append(this.rf2ExportHelper.getIdString(observableComponentNidVersion.getComponentNid()) + "\t");
                        break;
                }

                completedUnitOfWork();
                returnList.add(refsetRow.append("\r").toString());
            }


        }finally {
            this.readSemaphore.release();
            Get.activeTasks().remove(this);
        }

        return returnList;
    }
}
