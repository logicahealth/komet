package sh.isaac.solor.rf2;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.observable.semantic.version.*;
import sh.isaac.api.observable.semantic.version.brittle.*;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.UuidT3Generator;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.semantic.version.DynamicImpl;
import sh.isaac.solor.ExportConfiguration;
import sh.komet.gui.manifold.Manifold;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import static sh.isaac.api.chronicle.VersionType.*;

public class RF2ExportRefsetReader extends TimedTaskWithProgressTracker<List<String>> {

    private final RF2ExportHelper rf2ExportHelper;
    private final List<Chronology> chronologies;
    private final Semaphore readSemaphore;
    private final Manifold manifold;
    private final String assemblageName;

    public RF2ExportRefsetReader(List<Chronology> chronologies, Semaphore readSemaphore, Manifold manifold, ExportConfiguration exportConfiguration) {
        this.chronologies = chronologies;
        this.readSemaphore = readSemaphore;
        this.manifold = manifold;
        this.assemblageName = exportConfiguration.getMessage();
        this.rf2ExportHelper = new RF2ExportHelper(this.manifold);

        readSemaphore.acquireUninterruptibly();

        updateTitle("Reading " + this.assemblageName + " assemblage batch of size: " + chronologies.size());
        updateMessage("Processing batch of " + this.assemblageName + " concepts for RF2 Export");
        addToTotalWork(chronologies.size());
        Get.activeTasks().add(this);
    }

    @Override
    protected List<String> call() {
        ArrayList<String> returnList = new ArrayList<>();

        try{

            for(Chronology chronology : this.chronologies){

                String refsetID = this.rf2ExportHelper.getIdString(Get.concept(chronology.getAssemblageNid()));

                //Some semantics are referring to other semantics, not just directly to concepts
                String referenceComponentID = this.rf2ExportHelper
                        .getIdString(((SemanticChronology)chronology).getReferencedComponentNid());

                StringBuilder refsetRow = this.rf2ExportHelper.getRF2CommonElements(chronology)
                        .append(refsetID + "\t")
                        .append(referenceComponentID + "\t");

                switch (chronology.getVersionType()) {
                    case MEMBER:
                    case CONCEPT:
                        break;
                    case Nid1_Int2:
                        Observable_Nid1_Int2_Version observable_nid1_int2_version =
                                ((LatestVersion<Observable_Nid1_Int2_Version>)
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(chronology.getNid()))
                                        .get();
                        refsetRow.append(this.rf2ExportHelper.getIdString(observable_nid1_int2_version.getNid1()) + "\t")
                                .append(observable_nid1_int2_version.getInt2() + "\t");
                        break;
                    case Nid1_Nid2:
                        Observable_Nid1_Nid2_Version observable_nid1_nid2_version =
                                ((LatestVersion<Observable_Nid1_Nid2_Version>)
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(chronology.getNid()))
                                        .get();
                        refsetRow.append(this.rf2ExportHelper.getIdString(observable_nid1_nid2_version.getNid1()) + "\t")
                                .append(this.rf2ExportHelper.getIdString(observable_nid1_nid2_version.getNid2()) + "\t");
                        break;
                    case Nid1_Str2:
                        Observable_Nid1_Str2_Version observable_nid1_str2_version =
                                ((LatestVersion<Observable_Nid1_Str2_Version>)
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(chronology.getNid()))
                                        .get();
                        refsetRow.append(this.rf2ExportHelper.getIdString(observable_nid1_str2_version.getNid1()) + "\t")
                                .append(observable_nid1_str2_version.getStr2() + "\t");
                        break;
                    case Str1_Str2:
                        Observable_Str1_Str2_Version observable_str1_str2_version =
                                ((LatestVersion<Observable_Str1_Str2_Version>)
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(chronology.getNid()))
                                        .get();
                        refsetRow.append(observable_str1_str2_version.getStr1() + "\t")
                                .append(observable_str1_str2_version.getStr2() + "\t");
                        break;
                    case Nid1_Nid2_Str3:
                        Observable_Nid1_Nid2_Str3_Version observable_nid1_nid2_str3_version =
                                ((LatestVersion<Observable_Nid1_Nid2_Str3_Version>)
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(chronology.getNid()))
                                        .get();

                        refsetRow.append(this.rf2ExportHelper.getIdString(observable_nid1_nid2_str3_version.getNid1()) + "\t")
                                .append(this.rf2ExportHelper.getIdString(observable_nid1_nid2_str3_version.getNid2()) + "\t")
                                .append(observable_nid1_nid2_str3_version.getStr3() + "\t");
                        break;
                    case Nid1_Nid2_Int3:
                        Observable_Nid1_Nid2_Int3_Version observable_nid1_nid2_int3_version =
                                ((LatestVersion<Observable_Nid1_Nid2_Int3_Version>)
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(chronology.getNid()))
                                        .get();
                        refsetRow.append(this.rf2ExportHelper.getIdString(observable_nid1_nid2_int3_version.getNid1()) + "\t")
                                .append(this.rf2ExportHelper.getIdString(observable_nid1_nid2_int3_version.getNid2()) + "\t")
                                .append(observable_nid1_nid2_int3_version.getInt3() + "\t");
                        break;
                    case Str1_Nid2_Nid3_Nid4:
                        Observable_Str1_Nid2_Nid3_Nid4_Version observable_str1_nid2_nid3_nid4_version =
                                ((LatestVersion<Observable_Str1_Nid2_Nid3_Nid4_Version>)
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(chronology.getNid()))
                                        .get();
                        refsetRow.append(observable_str1_nid2_nid3_nid4_version.getStr1() + "\t")
                                .append(this.rf2ExportHelper.getIdString(observable_str1_nid2_nid3_nid4_version.getNid2()) + "\t")
                                .append(this.rf2ExportHelper.getIdString(observable_str1_nid2_nid3_nid4_version.getNid3()) + "\t")
                                .append(this.rf2ExportHelper.getIdString(observable_str1_nid2_nid3_nid4_version.getNid4()) + "\t");
                        break;
                    case Str1_Str2_Nid3_Nid4:
                        Observable_Str1_Str2_Nid3_Nid4_Version observable_str1_str2_nid3_nid4_version =
                                ((LatestVersion<Observable_Str1_Str2_Nid3_Nid4_Version>)
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(chronology.getNid()))
                                        .get();
                        refsetRow.append(observable_str1_str2_nid3_nid4_version.getStr1() + "\t")
                                .append(observable_str1_str2_nid3_nid4_version.getStr2() + "\t")
                                .append(this.rf2ExportHelper.getIdString(observable_str1_str2_nid3_nid4_version.getNid3()) + "\t")
                                .append(this.rf2ExportHelper.getIdString(observable_str1_str2_nid3_nid4_version.getNid4()) + "\t");
                        break;
                    case Str1_Str2_Nid3_Nid4_Nid5:
                        Observable_Str1_Str2_Nid3_Nid4_Nid5_Version observable_str1_str2_nid3_nid4_nid5_version =
                                ((LatestVersion<Observable_Str1_Str2_Nid3_Nid4_Nid5_Version>)
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(chronology.getNid()))
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
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(chronology.getNid()))
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
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(chronology.getNid()))
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
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(chronology.getNid()))
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
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(chronology.getNid()))
                                        .get();
                        refsetRow.append(observableLongVersion.getLongValue() + "\t");
                        break;
                    case STRING:
                        ObservableStringVersion observableStringVersion =
                                ((LatestVersion<ObservableStringVersion>)
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(chronology.getNid()))
                                        .get();
                        refsetRow.append(observableStringVersion.getString() + "\t");
                        break;
                    case DYNAMIC:

                        break;
                    case DESCRIPTION:
                        ObservableDescriptionVersion observableDescriptionVersion =
                                ((LatestVersion<ObservableDescriptionVersion>)
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(chronology.getNid()))
                                        .get();
                        refsetRow.append(observableDescriptionVersion.getText() + "\t");
                        break;
                    case LOGIC_GRAPH:
                        ObservableLogicGraphVersion observableLogicGraphVersion =
                                ((LatestVersion<ObservableLogicGraphVersion>)
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(chronology.getNid()))
                                        .get();
                        refsetRow.append(observableLogicGraphVersion.getLogicalExpression().toSimpleString() + "\t");
                        break;
                    case LOINC_RECORD:
                        ObservableLoincVersion observableLoincVersion =
                                ((LatestVersion<ObservableLoincVersion>)
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(chronology.getNid()))
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
                    case RF2_RELATIONSHIP:
                        ObservableRf2Relationship observableRf2Relationship =
                                ((LatestVersion<ObservableRf2Relationship>)
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(chronology.getNid()))
                                        .get();
                        refsetRow.append(this.rf2ExportHelper.getIdString(observableRf2Relationship.getDestinationNid()) + "\t")
                                .append(observableRf2Relationship.getRelationshipGroup() + "\t")
                                .append(this.rf2ExportHelper.getIdString(observableRf2Relationship.getTypeNid()) + "\t")
                                .append(this.rf2ExportHelper.getIdString(observableRf2Relationship.getCharacteristicNid()) + "\t")
                                .append(this.rf2ExportHelper.getIdString(observableRf2Relationship.getModifierNid()) + "\t");
                        break;
                    case COMPONENT_NID:
                        ObservableComponentNidVersion observableComponentNidVersion =
                                ((LatestVersion<ObservableComponentNidVersion>)
                                        rf2ExportHelper.getSnapshotService().getObservableSemanticVersion(chronology.getNid()))
                                        .get();
                        refsetRow.append(this.rf2ExportHelper.getIdString(observableComponentNidVersion.getComponentNid()) + "\t");
                        break;
                    case MEASURE_CONSTRAINTS:
                        //???
                        break;
                    case UNKNOWN:
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
