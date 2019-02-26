package sh.isaac.solor.rf2.exporters.refsets;

import sh.isaac.api.Get;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;
import sh.isaac.api.component.semantic.version.LongVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.component.semantic.version.brittle.*;
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

            final StringBuilder linesToWrite = new StringBuilder();

            this.intStream
                    .forEach(nid -> {

                        linesToWrite.setLength(0);

                        Get.assemblageService().getSemanticChronology(nid).getVersionList().stream()
                                .forEach(version -> {

                                    String refsetID = this.rf2ExportHelper.getIdString(version.getAssemblageNid());
                                    String referenceComponentID = this.rf2ExportHelper
                                            .getIdString(((SemanticChronology)version.getChronology()).getReferencedComponentNid());
                                    linesToWrite
                                            .append(version.getPrimordialUuid() + "\t")
                                            .append(this.rf2ExportHelper.getTimeString(version) + "\t")
                                            .append(this.rf2ExportHelper.getActiveString(version) + "\t")
                                            .append(this.rf2ExportHelper.getIdString(version.getModuleNid()) + "\t")
                                            .append(refsetID + "\t")
                                            .append(referenceComponentID + "\t");

                                    switch (version.getSemanticType()) {
                                        case MEMBER:
                                            break;
                                        case Nid1_Int2:

                                            linesToWrite
                                                    .append(this.rf2ExportHelper.getIdString(((Nid1_Int2_Version)version).getNid1()) + "\t")
                                                    .append(((Nid1_Int2_Version)version).getInt2());
                                            break;
                                        case Nid1_Nid2:

                                            linesToWrite
                                                    .append(this.rf2ExportHelper.getIdString(((Nid1_Nid2_Version)version).getNid1()) + "\t")
                                                    .append(this.rf2ExportHelper.getIdString(((Nid1_Nid2_Version)version).getNid2()));
                                            break;
                                        case Nid1_Str2:

                                            linesToWrite
                                                    .append(this.rf2ExportHelper.getIdString(((Nid1_Str2_Version)version).getNid1()) + "\t")
                                                    .append(((Nid1_Str2_Version)version).getStr2());
                                            break;
                                        case Str1_Str2:

                                            linesToWrite
                                                    .append(((Str1_Str2_Version)version).getStr1() + "\t")
                                                    .append(((Str1_Str2_Version)version).getStr2());
                                            break;
                                        case Nid1_Nid2_Str3:

                                            linesToWrite
                                                    .append(this.rf2ExportHelper.getIdString(((Nid1_Nid2_Str3_Version)version).getNid1()) + "\t")
                                                    .append(this.rf2ExportHelper.getIdString(((Nid1_Nid2_Str3_Version)version).getNid2()) + "\t")
                                                    .append(((Nid1_Nid2_Str3_Version)version).getStr3());
                                            break;
                                        case Nid1_Nid2_Int3:

                                            linesToWrite
                                                    .append(this.rf2ExportHelper.getIdString(((Nid1_Nid2_Int3_Version)version).getNid1()) + "\t")
                                                    .append(this.rf2ExportHelper.getIdString(((Nid1_Nid2_Int3_Version)version).getNid2()) + "\t")
                                                    .append(((Nid1_Nid2_Int3_Version)version).getInt3());
                                            break;
                                        case Str1_Nid2_Nid3_Nid4:

                                            linesToWrite
                                                    .append(((Str1_Nid2_Nid3_Nid4_Version)version).getStr1() + "\t")
                                                    .append(this.rf2ExportHelper.getIdString(((Str1_Nid2_Nid3_Nid4_Version)version).getNid2()) + "\t")
                                                    .append(this.rf2ExportHelper.getIdString(((Str1_Nid2_Nid3_Nid4_Version)version).getNid3()) + "\t")
                                                    .append(this.rf2ExportHelper.getIdString(((Str1_Nid2_Nid3_Nid4_Version)version).getNid4()));
                                            break;
                                        case Str1_Str2_Nid3_Nid4:

                                            linesToWrite
                                                    .append(((Str1_Str2_Nid3_Nid4_Version)version).getStr1() + "\t")
                                                    .append(((Str1_Str2_Nid3_Nid4_Version)version).getStr2() + "\t")
                                                    .append(this.rf2ExportHelper.getIdString(((Str1_Str2_Nid3_Nid4_Version)version).getNid3()) + "\t")
                                                    .append(this.rf2ExportHelper.getIdString(((Str1_Str2_Nid3_Nid4_Version)version).getNid4()));
                                            break;
                                        case Str1_Str2_Nid3_Nid4_Nid5:

                                            linesToWrite
                                                    .append(((Str1_Str2_Nid3_Nid4_Nid5_Version)version).getStr1() + "\t")
                                                    .append(((Str1_Str2_Nid3_Nid4_Nid5_Version)version).getStr2() + "\t")
                                                    .append(this.rf2ExportHelper.getIdString(((Str1_Str2_Nid3_Nid4_Nid5_Version)version).getNid3()) + "\t")
                                                    .append(this.rf2ExportHelper.getIdString(((Str1_Str2_Nid3_Nid4_Nid5_Version)version).getNid4()) + "\t")
                                                    .append(this.rf2ExportHelper.getIdString(((Str1_Str2_Nid3_Nid4_Nid5_Version)version).getNid5()));
                                            break;
                                        case Nid1_Int2_Str3_Str4_Nid5_Nid6:

                                            linesToWrite
                                                    .append(this.rf2ExportHelper.getIdString(((Nid1_Int2_Str3_Str4_Nid5_Nid6_Version)version).getNid1()) + "\t")
                                                    .append(((Nid1_Int2_Str3_Str4_Nid5_Nid6_Version)version).getInt2() + "\t")
                                                    .append(((Nid1_Int2_Str3_Str4_Nid5_Nid6_Version)version).getStr3() + "\t")
                                                    .append(((Nid1_Int2_Str3_Str4_Nid5_Nid6_Version)version).getStr4() + "\t")
                                                    .append(this.rf2ExportHelper.getIdString(((Nid1_Int2_Str3_Str4_Nid5_Nid6_Version)version).getNid5()) + "\t")
                                                    .append(this.rf2ExportHelper.getIdString(((Nid1_Int2_Str3_Str4_Nid5_Nid6_Version)version).getNid6()));
                                            break;
                                        case Int1_Int2_Str3_Str4_Str5_Nid6_Nid7:

                                            linesToWrite
                                                    .append(((Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version)version).getInt1() + "\t")
                                                    .append(((Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version)version).getInt2() + "\t")
                                                    .append(((Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version)version).getStr3() + "\t")
                                                    .append(((Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version)version).getStr4() + "\t")
                                                    .append(((Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version)version).getStr5() + "\t")
                                                    .append(this.rf2ExportHelper.getIdString(((Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version)version).getNid6()) + "\t")
                                                    .append(this.rf2ExportHelper.getIdString(((Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_Version)version).getNid7()));
                                            break;
                                        case Str1_Str2_Str3_Str4_Str5_Str6_Str7:

                                            linesToWrite
                                                    .append(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version)version).getStr1() + "\t")
                                                    .append(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version)version).getStr2() + "\t")
                                                    .append(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version)version).getStr3() + "\t")
                                                    .append(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version)version).getStr4() + "\t")
                                                    .append(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version)version).getStr5() + "\t")
                                                    .append(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version)version).getStr6() + "\t")
                                                    .append(((Str1_Str2_Str3_Str4_Str5_Str6_Str7_Version)version).getStr7());
                                            break;
                                        case LONG:

                                            linesToWrite
                                                    .append(((LongVersion)version).getLongValue());
                                            break;
                                        case STRING:

                                            linesToWrite
                                                    .append(((StringVersion)version).getString());
                                            break;
                                        case DYNAMIC:
                                            break;
                                        case LOINC_RECORD:

                                            linesToWrite
                                                    .append(((LoincVersion)version).getLoincNum() + "\t")
                                                    .append(((LoincVersion)version).getComponent() + "\t")
                                                    .append(((LoincVersion)version).getProperty() + "\t")
                                                    .append(((LoincVersion)version).getTimeAspect() + "\t")
                                                    .append(((LoincVersion)version).getSystem() + "\t")
                                                    .append(((LoincVersion)version).getScaleType() + "\t")
                                                    .append(((LoincVersion)version).getMethodType() + "\t")
                                                    .append(((LoincVersion)version).getLoincStatus() + "\t")
                                                    .append(((LoincVersion)version).getShortName() + "\t")
                                                    .append(((LoincVersion)version).getLongCommonName());

                                            break;
                                        case COMPONENT_NID:

                                            linesToWrite
                                                    .append(this.rf2ExportHelper.getIdString(((ComponentNidVersion)version).getComponentNid()));
                                            break;
                                    }
                                    linesToWrite.append("\r");
                                });

                        super.writeStringToFile(linesToWrite.toString());
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
