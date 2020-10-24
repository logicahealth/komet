package sh.isaac.solor.rf2.exporters.refsets;

import java.util.concurrent.Semaphore;
import java.util.stream.IntStream;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;
import sh.isaac.api.component.semantic.version.DynamicVersion;
import sh.isaac.api.component.semantic.version.LongVersion;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.component.semantic.version.brittle.*;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicNid;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicUUID;
import sh.isaac.api.statement.constraints.MeasureConstraints;
import sh.isaac.solor.rf2.config.RF2Configuration;
import sh.isaac.solor.rf2.exporters.RF2AbstractExporter;
import sh.isaac.solor.rf2.utility.RF2ExportHelper;

public class RF2RefsetExporter extends RF2AbstractExporter {

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

            this.intStream.forEach(nid -> {
                super.clearLineOutput();
                super.incrementProgressCount();

                switch (this.rf2Configuration.getRf2ReleaseType()){

                    case FULL:
                        Get.assemblageService().getSemanticChronology(nid).getVersionList().stream().forEach(version -> writeSemantic((SemanticVersion)version));
                        break;
                    case SNAPSHOT:
                        LatestVersion<SemanticVersion> lv = Get.assemblageService().getSemanticChronology(nid)
                            .getLatestVersion(this.rf2ExportHelper.getManifoldCoordinate().getVertexStampFilter());
                        if (lv.isPresent()) {
                            writeSemantic(lv.get());
                        }
                        break;
                    default :
                        throw new RuntimeException("Unsupported case " + this.rf2Configuration.getRf2ReleaseType());
                }
                super.writeToFile();
                super.tryAndUpdateProgressTracker();
            });

            if(this.rf2Configuration.isDescriptorAssemblage()){
                super.writeToFile(this.rf2Configuration.getRefsetDescriptorDefinitions());
            }

        } finally {
            this.readSemaphore.release();
            Get.activeTasks().remove(this);
        }

        return null;
    }
    
    private void writeSemantic(final SemanticVersion sv)
    {
        String refsetID = this.rf2ExportHelper.getIdString(sv.getAssemblageNid());
        String referenceComponentID = this.rf2ExportHelper.getIdString(sv.getReferencedComponentNid());
        super.outputToWrite
                .append(sv.getPrimordialUuid() + "\t")
                .append(this.rf2ExportHelper.getTimeString(sv) + "\t")
                .append(this.rf2ExportHelper.getActiveString(sv) + "\t")
                .append(this.rf2ExportHelper.getIdString(sv.getModuleNid()) + "\t")
                .append(refsetID + "\t")
                .append(referenceComponentID);

        switch (sv.getSemanticType()) {
            case MEMBER:
                break;
            case Nid1_Int2:
                super.outputToWrite
                        .append("\t" + this.rf2ExportHelper.getIdString(((Nid1_Int2_Version)sv).getNid1()) + "\t")
                        .append(((Nid1_Int2_Version)sv).getInt2());
                break;
            case Nid1_Long2:
                super.outputToWrite
                        .append("\t" + this.rf2ExportHelper.getIdString(((Nid1_Long2_Version)sv).getNid1()) + "\t")
                        .append(((Nid1_Long2_Version)sv).getLong2());
                break;
            case Nid1_Nid2:
                super.outputToWrite
                        .append("\t" + this.rf2ExportHelper.getIdString(((Nid1_Nid2_Version)sv).getNid1()) + "\t")
                        .append(this.rf2ExportHelper.getIdString(((Nid1_Nid2_Version)sv).getNid2()));
                break;
            case Nid1_Str2:
                super.outputToWrite
                        .append("\t" + this.rf2ExportHelper.getIdString(((Nid1_Str2_Version)sv).getNid1()) + "\t")
                        .append(((Nid1_Str2_Version)sv).getStr2());
                break;
            case Str1_Str2:
                super.outputToWrite
                        .append("\t" + ((Str1_Str2_Version)sv).getStr1() + "\t")
                        .append(((Str1_Str2_Version)sv).getStr2());
                break;
            case Nid1_Nid2_Str3:
                super.outputToWrite
                        .append("\t" + this.rf2ExportHelper.getIdString(((Nid1_Nid2_Str3_Version)sv).getNid1()) + "\t")
                        .append(this.rf2ExportHelper.getIdString(((Nid1_Nid2_Str3_Version)sv).getNid2()) + "\t")
                        .append(((Nid1_Nid2_Str3_Version)sv).getStr3());
                break;
            case Nid1_Nid2_Int3:
                super.outputToWrite
                        .append("\t" + this.rf2ExportHelper.getIdString(((Nid1_Nid2_Int3_Version)sv).getNid1()) + "\t")
                        .append(this.rf2ExportHelper.getIdString(((Nid1_Nid2_Int3_Version)sv).getNid2()) + "\t")
                        .append(((Nid1_Nid2_Int3_Version)sv).getInt3());
                break;
            case Str1_Nid2_Nid3_Nid4:
                super.outputToWrite
                        .append("\t" + ((Str1_Nid2_Nid3_Nid4_Version)sv).getStr1() + "\t")
                        .append(this.rf2ExportHelper.getIdString(((Str1_Nid2_Nid3_Nid4_Version)sv).getNid2()) + "\t")
                        .append(this.rf2ExportHelper.getIdString(((Str1_Nid2_Nid3_Nid4_Version)sv).getNid3()) + "\t")
                        .append(this.rf2ExportHelper.getIdString(((Str1_Nid2_Nid3_Nid4_Version)sv).getNid4()));
                break;
            case Str1_Str2_Nid3_Nid4:
                super.outputToWrite
                        .append("\t" + ((Str1_Str2_Nid3_Nid4_Version)sv).getStr1() + "\t")
                        .append(((Str1_Str2_Nid3_Nid4_Version)sv).getStr2() + "\t")
                        .append(this.rf2ExportHelper.getIdString(((Str1_Str2_Nid3_Nid4_Version)sv).getNid3()) + "\t")
                        .append(this.rf2ExportHelper.getIdString(((Str1_Str2_Nid3_Nid4_Version)sv).getNid4()));
                break;
            case Str1_Str2_Nid3_Nid4_Nid5:

                super.outputToWrite
                        .append("\t" + ((Str1_Str2_Nid3_Nid4_Nid5_Version)sv).getStr1() + "\t")
                        .append(((Str1_Str2_Nid3_Nid4_Nid5_Version)sv).getStr2() + "\t")
                        .append(this.rf2ExportHelper.getIdString(((Str1_Str2_Nid3_Nid4_Nid5_Version)sv).getNid3()) + "\t")
                        .append(this.rf2ExportHelper.getIdString(((Str1_Str2_Nid3_Nid4_Nid5_Version)sv).getNid4()) + "\t")
                        .append(this.rf2ExportHelper.getIdString(((Str1_Str2_Nid3_Nid4_Nid5_Version)sv).getNid5()));
                break;
            case Nid1_Int2_Str3_Str4_Nid5_Nid6:
                super.outputToWrite
                        .append("\t" + this.rf2ExportHelper.getIdString(((Nid1_Int2_Str3_Str4_Nid5_Nid6_Version)sv).getNid1()) + "\t")
                        .append(((Nid1_Int2_Str3_Str4_Nid5_Nid6_Version)sv).getInt2() + "\t")
                        .append(((Nid1_Int2_Str3_Str4_Nid5_Nid6_Version)sv).getStr3() + "\t")
                        .append(((Nid1_Int2_Str3_Str4_Nid5_Nid6_Version)sv).getStr4() + "\t")
                        .append(this.rf2ExportHelper.getIdString(((Nid1_Int2_Str3_Str4_Nid5_Nid6_Version)sv).getNid5()) + "\t")
                        .append(this.rf2ExportHelper.getIdString(((Nid1_Int2_Str3_Str4_Nid5_Nid6_Version)sv).getNid6()));
                break;
            case Int1_Int2_Str3_Str4_Str5_Nid6_Nid7:
                super.outputToWrite
                        .append("\t" + ((Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version)sv).getInt1() + "\t")
                        .append(((Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version)sv).getInt2() + "\t")
                        .append(((Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version)sv).getStr3() + "\t")
                        .append(((Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version)sv).getStr4() + "\t")
                        .append(((Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version)sv).getStr5() + "\t")
                        .append(this.rf2ExportHelper.getIdString(((Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version)sv).getNid6()) + "\t")
                        .append(this.rf2ExportHelper.getIdString(((Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version)sv).getNid7()));
                break;
            case Str1_Str2_Str3_Str4_Str5_Str6_Str7:
                super.outputToWrite
                        .append("\t" + ((Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version)sv).getStr1() + "\t")
                        .append(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version)sv).getStr2() + "\t")
                        .append(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version)sv).getStr3() + "\t")
                        .append(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version)sv).getStr4() + "\t")
                        .append(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version)sv).getStr5() + "\t")
                        .append(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version)sv).getStr6() + "\t")
                        .append(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version)sv).getStr7());
                break;
            case LONG:
                super.outputToWrite
                        .append("\t" + ((LongVersion)sv).getLongValue());
                break;
            case STRING:
                super.outputToWrite
                        .append("\t" + ((StringVersion)sv).getString());
                break;
            case DYNAMIC:
                DynamicData[] dd = ((DynamicVersion)sv).getData();
                for (DynamicData di : dd) {
                    switch (di.getDynamicDataType()) {
                        case BOOLEAN:
                        case DOUBLE:
                        case FLOAT:
                        case INTEGER:
                        case LONG:
                        case STRING:
                            super.outputToWrite.append("\t").append(di.dataToString());
                            break;
                        case NID:
                            super.outputToWrite.append("\t").append(this.rf2ExportHelper.getIdString(((DynamicNid)di).getDataNid()));
                            break;
                        case UUID:
                            super.outputToWrite.append("\t").append(this.rf2ExportHelper.getIdString(Get.identifierService().getNidForUuids(((DynamicUUID)di).getDataUUID())));
                            break;
                        case ARRAY:
                        case BYTEARRAY:
                        case POLYMORPHIC:
                        case UNKNOWN:
                        default :
                            throw new RuntimeException("Unsupported type for export! " + di.getDynamicDataType());
                        
                    }
                    
                }
                break;
            case COMPONENT_NID:
                super.outputToWrite
                        .append("\t" + this.rf2ExportHelper.getIdString(((ComponentNidVersion)sv).getComponentNid()));

            case MEASURE_CONSTRAINTS:
                super.outputToWrite
                    .append("\t" + ((MeasureConstraints)sv).getConstraintDescription() + "\t")
                    .append(((MeasureConstraints)sv).getConstraintDescription() + "\t")
                    .append(((MeasureConstraints)sv).getInitialLowerBound() + "\t")
                    .append(((MeasureConstraints)sv).getInitialUpperBound() + "\t")
                    .append(((MeasureConstraints)sv).getInitialIncludeUpperBound() + "\t")
                    .append(((MeasureConstraints)sv).getInitialIncludeLowerBound() + "\t")
                    .append(((MeasureConstraints)sv).getMinimumValue() + "\t")
                    .append(((MeasureConstraints)sv).getMaximumValue() + "\t")
                    .append(((MeasureConstraints)sv).getMinimumGranularity() + "\t")
                    .append(((MeasureConstraints)sv).getMaximumGranularity() + "\t")
                    .append(((MeasureConstraints)sv).showRange() + "\t")
                    .append(((MeasureConstraints)sv).showGranularity() + "\t")
                    .append(((MeasureConstraints)sv).showIncludeBounds() + "\t")
                    .append(this.rf2ExportHelper.getIdString(((MeasureConstraints)sv).getMeasureSemanticConstraintAssemblageNid()));
                break;
            case IMAGE:
            case DESCRIPTION:
            case LOGIC_GRAPH:
            case RF2_RELATIONSHIP:
            case CONCEPT:
            case UNKNOWN:
            default :
                throw new RuntimeException("Invalid / Unsupported data type passed into refset exporter: " + sv.getSemanticType());
        }
        super.outputToWrite.append("\r\n");
    }
}
