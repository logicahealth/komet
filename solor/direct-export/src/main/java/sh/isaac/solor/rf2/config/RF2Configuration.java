package sh.isaac.solor.rf2.config;

import org.apache.commons.text.WordUtils;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.observable.semantic.version.brittle.Observable_Nid1_Nid2_Int3_Version;
import sh.isaac.api.observable.semantic.version.brittle.Observable_Nid1_Nid2_Str3_Version;
import sh.isaac.api.util.UuidT3Generator;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.configuration.LanguageCoordinates;
import sh.isaac.solor.rf2.utility.PreExportUtility;
import sh.isaac.solor.rf2.utility.RF2ExportHelper;
import sh.komet.gui.manifold.Manifold;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class RF2Configuration {

    private String fileHeader;
    private Path filePath;
    private RF2ConfigType rf2ConfigType;
    private String message;
    private LocalDateTime localDateTime;
    private Supplier<IntStream> intStreamSupplier;
    private final String parentDirectory;
    private final String exportDirectory;
    private final String zipDirectory;
    private final Manifold manifold;
    private static HashMap<Integer, Integer[]> refsetDescriptorHeaders = new HashMap<>();
    private final List<String> refsetDescriptorDefinitions = new ArrayList<>();
    private boolean isDescriptorAssemblage = false;

    public RF2Configuration(RF2ConfigType rf2ConfigType, LocalDateTime localDateTime, File exportDirectory,
                            Manifold manifold) {
        this.rf2ConfigType = rf2ConfigType;
        this.localDateTime = localDateTime;
        this.fileHeader = rf2ConfigType.getFileHeader();
        this.message = rf2ConfigType.getMessage();
        this.parentDirectory = "/SnomedCT_SolorRF2_PRODUCTION_TIME1/"
                .replace("TIME1", DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").format(this.localDateTime));
        this.exportDirectory = exportDirectory.toString();
        this.manifold = manifold;
        this.zipDirectory = this.exportDirectory + this.parentDirectory.substring(0, this.parentDirectory.length() -1) + ".zip";

        setIntStreamSupplier(0);
        setFilePath();
    }

    public RF2Configuration(RF2ConfigType rf2ConfigType, LocalDateTime localDateTime, int languageNid,
                            File exportDirectory, Manifold manifold, PreExportUtility preExportUtility, boolean isDescriptorAssemblagePresent) {
        this.rf2ConfigType = rf2ConfigType;
        this.localDateTime = localDateTime;
        this.fileHeader = rf2ConfigType.getFileHeader();
        this.message = rf2ConfigType.getMessage() + " - " + LanguageCoordinates.conceptNidToIso639(languageNid);
        this.parentDirectory = "/SnomedCT_SolorRF2_PRODUCTION_TIME1/"
                .replace("TIME1", DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").format(this.localDateTime));
        this.exportDirectory = exportDirectory.toString();
        this.manifold = manifold;
        this.zipDirectory = this.exportDirectory + this.parentDirectory.substring(0, this.parentDirectory.length() -1) + ".zip";

        if(isDescriptorAssemblagePresent) {
            refsetDescriptorHeaders = preExportUtility.generateRefsetDescriptorHeaders();
        }

        setIntStreamSupplier(languageNid);
        setFilePath(languageNid);
        setFileHeader(languageNid);
    }

    public RF2Configuration(RF2ConfigType rf2ConfigType, LocalDateTime localDateTime, int assemblageNid,
                            String assemblageFQN, VersionType versionType, File exportDirectory, Manifold manifold,
                            PreExportUtility preExportUtility, boolean isDescriptorAssemblagePresent) {
        this.rf2ConfigType = rf2ConfigType;
        this.localDateTime = localDateTime;
        this.parentDirectory = "/SnomedCT_SolorRF2_PRODUCTION_TIME1/"
                .replace("TIME1", DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").format(this.localDateTime));
        this.exportDirectory = exportDirectory.toString();
        this.manifold = manifold;
        this.zipDirectory = this.exportDirectory + this.parentDirectory.substring(0, this.parentDirectory.length() -1) + ".zip";
        this.message = rf2ConfigType.getMessage() + " " + assemblageFQN;

        if(isDescriptorAssemblagePresent) {
            refsetDescriptorHeaders = preExportUtility.generateRefsetDescriptorHeaders();
            this.isDescriptorAssemblage = assemblageNid == Get.concept(UuidT3Generator.fromSNOMED("900000000000456007")).getNid();
        }

        setFilePath(versionType, assemblageFQN);
        setIntStreamSupplier(assemblageNid);
        setFileHeader(assemblageNid);
    }

    private void setFileHeader(int assemblageNid){

        if(refsetDescriptorHeaders.containsKey(assemblageNid)){
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(this.rf2ConfigType.getFileHeader());
            Integer[] tempColumns = refsetDescriptorHeaders.get(assemblageNid);

            for(int i = 1; i < tempColumns.length; i++){
                if(tempColumns[i] == null)
                    continue;
                stringBuilder.append(formatHeaderName(Get.concept(tempColumns[i]).getFullyQualifiedName().replace(" ", "")).toLowerCase())
                        .append("\t");
            }

            String dynamicHeader = stringBuilder.toString().substring(0, stringBuilder.length() -2);


            this.fileHeader = dynamicHeader + "\r\n";
        }else{

            if(this.rf2ConfigType.equals(RF2ConfigType.LANGUAGE_REFSET)) {
                this.fileHeader = rf2ConfigType.getFileHeader();
            }else {

                final RF2ExportHelper rf2ExportHelper = new RF2ExportHelper(manifold);
                final StringBuilder descriptorStringBuilder = new StringBuilder();

                final String integerFieldDescription = rf2ExportHelper.getIdString(MetaData.INTEGER_FIELD____SOLOR.getNid());
                final String stringFieldDescription = rf2ExportHelper.getIdString(MetaData.STRING_FIELD____SOLOR.getNid());
                final String longFieldDescription = rf2ExportHelper.getIdString(MetaData.LONG_FIELD____SOLOR.getNid());

                Get.concept(assemblageNid).getVersionList().stream()
                        .forEach(version -> {

                            switch (Get.assemblageService().getVersionTypeForAssemblage(assemblageNid)) {
                                case MEMBER:
                                    this.fileHeader = rf2ConfigType.getFileHeader().trim() + "\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t0\r\n");
                                    break;
                                case COMPONENT_NID:
                                    this.fileHeader = rf2ConfigType.getFileHeader() + "componentId\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t0\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t1\r\n");
                                    break;
                                case STRING:
                                    this.fileHeader = rf2ConfigType.getFileHeader() + "stringValue\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t0\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + stringFieldDescription + "\t" + "707000009\t1\r\n");
                                    break;
                                case LONG:
                                    this.fileHeader = rf2ConfigType.getFileHeader() + "longValue\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t0\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + longFieldDescription + "\t" + "707000009\t1\r\n");
                                    break;
                                case Nid1_Int2:
                                    this.fileHeader = rf2ConfigType.getFileHeader() + "componentId\tintegerValue\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t0\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t1\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + integerFieldDescription + "\t" + "900000000000477005\t2\r\n");
                                    break;
                                case Nid1_Nid2:
                                    this.fileHeader = rf2ConfigType.getFileHeader() + "componentId\tcomponentId\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t0\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t1\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t2\r\n");
                                    break;
                                case Nid1_Str2:
                                    this.fileHeader = rf2ConfigType.getFileHeader() + "componentId\tstringValue\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t0\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t1\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + stringFieldDescription + "\t" + "707000009\t2\r\n");
                                    break;
                                case Str1_Str2:
                                    this.fileHeader = rf2ConfigType.getFileHeader() + "stringValue\tstringValue\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t0\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + stringFieldDescription + "\t" + "707000009\t1\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + stringFieldDescription + "\t" + "707000009\t2\r\n");
                                    break;
                                case Nid1_Nid2_Int3:
                                    this.fileHeader = rf2ConfigType.getFileHeader() + "componentId\tcomponentId\tintegerValue\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t0\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t1\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t2\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + integerFieldDescription + "\t" + "900000000000477005\t3\r\n");
                                    break;
                                case Nid1_Nid2_Str3:
                                    this.fileHeader = rf2ConfigType.getFileHeader() + "componentId\tcomponentId\tstringValue\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t0\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t1\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t2\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + stringFieldDescription + "\t" + "707000009\t3\r\n");
                                    break;
                                case Str1_Nid2_Nid3_Nid4:
                                    this.fileHeader = rf2ConfigType.getFileHeader() + "stringValue\tcomponentId\tcomponentId\tcomponentId\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t0\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + stringFieldDescription + "\t" + "707000009\t1\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t2\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t3\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t4\r\n");
                                    break;
                                case Str1_Str2_Nid3_Nid4:
                                    this.fileHeader = rf2ConfigType.getFileHeader() + "stringValue\tstringValue\tcomponentId\tcomponentId\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t0\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + stringFieldDescription + "\t" + "707000009\t1\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + stringFieldDescription + "\t" + "707000009\t2\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t3\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t4\r\n");
                                    break;
                                case Str1_Str2_Nid3_Nid4_Nid5:
                                    this.fileHeader = rf2ConfigType.getFileHeader() + "stringValue\tstringValue\tcomponentId\tcomponentId\tcomponentId\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t0\r");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + stringFieldDescription + "\t" + "707000009\t1\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + stringFieldDescription + "\t" + "707000009\t2\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t3\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t4\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t5\r\n");
                                    break;
                                case Nid1_Int2_Str3_Str4_Nid5_Nid6:
                                    this.fileHeader = rf2ConfigType.getFileHeader() + "componentId\tintegerValue\tstringValue\tstringValue\tcomponentId\tcomponentId\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t0\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t1\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + integerFieldDescription + "\t" + "900000000000477005\t2\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + stringFieldDescription + "\t" + "707000009\t3\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + stringFieldDescription + "\t" + "707000009\t4\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t5\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t6\r\n");
                                    break;
                                case Int1_Int2_Str3_Str4_Str5_Nid6_Nid7:
                                    this.fileHeader = rf2ConfigType.getFileHeader() + "integerValue\tintegerValue\tstringValue\tstringValue\tstringValue\tcomponentId\tcomponentId\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t0\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + integerFieldDescription + "\t" + "900000000000477005\t1\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + integerFieldDescription + "\t" + "900000000000477005\t2\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + stringFieldDescription + "\t" + "707000009\t3\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + stringFieldDescription + "\t" + "707000009\t4\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + stringFieldDescription + "\t" + "707000009\t5\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t6\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t7\r\n");
                                    break;
                                case Str1_Str2_Str3_Str4_Str5_Str6_Str7:
                                    this.fileHeader = rf2ConfigType.getFileHeader() + "stringValue\tstringValue\tstringValue\tstringValue\tstringValue\tstringValue\tstringValue\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t0\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + stringFieldDescription + "\t" + "707000009\t1\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + stringFieldDescription + "\t" + "707000009\t2\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + stringFieldDescription + "\t" + "707000009\t3\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + stringFieldDescription + "\t" + "707000009\t4\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + stringFieldDescription + "\t" + "707000009\t5\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + stringFieldDescription + "\t" + "707000009\t6\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + stringFieldDescription + "\t" + "707000009\t7\r\n");
                                    break;
                                case LOINC_RECORD:
                                    this.fileHeader = rf2ConfigType.getFileHeader() + "loincNum\tcomponent\tproperty\ttimeAspect\tsystem\tscaleType\tmethodType\tstatus\tshortName\tlongCommonName\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + "449608002\t900000000000461009\t0\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + stringFieldDescription + "\t" + "707000009\t1\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + stringFieldDescription + "\t" + "707000009\t2\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + stringFieldDescription + "\t" + "707000009\t3\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + stringFieldDescription + "\t" + "707000009\t4\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + stringFieldDescription + "\t" + "707000009\t5\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + stringFieldDescription + "\t" + "707000009\t6\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + stringFieldDescription + "\t" + "707000009\t7\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + stringFieldDescription + "\t" + "707000009\t8\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + stringFieldDescription + "\t" + "707000009\t9\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder, rf2ExportHelper) + stringFieldDescription + "\t" + "707000009\t10\r\n");
                                    break;
                            }
                        });
            }
        }
    }

    private String createDescriptorBaseString(Version version, int assemblageNid, StringBuilder stringBuilder, RF2ExportHelper rf2ExportHelper){

        stringBuilder.setLength(0);

        return stringBuilder
                .append(UUID.randomUUID().toString() + "\t")
                .append(rf2ExportHelper.getTimeString(version) + "\t")
                .append(rf2ExportHelper.getActiveString(version) + "\t")
                .append(rf2ExportHelper.getIdString(version.getModuleNid()) + "\t")
                .append("900000000000456007" + "\t")
                .append(rf2ExportHelper.getIdString(assemblageNid) + "\t").toString();
    }

    private String formatHeaderName(String originalHeader){
        return originalHeader.replace(originalHeader.substring(originalHeader.indexOf("("), originalHeader.indexOf(")") + 1),"");
    }

    private void setIntStreamSupplier(int assemblageNid){

        switch (this.rf2ConfigType){
            case CONCEPT:
            case IDENTIFIER:
                this.intStreamSupplier = () -> Get.conceptService().getConceptNidStream();
                break;
            case DESCRIPTION:
                this.intStreamSupplier = () -> Arrays.stream(
                        Get.taxonomyService().getSnapshot(manifold)
                                .getTaxonomyChildConceptNids(MetaData.LANGUAGE____SOLOR.getNid()))
                        .flatMap(nid -> Get.assemblageService().getSemanticNidStream(nid));
                break;
            case RELATIONSHIP:
                this.intStreamSupplier = () -> Get.assemblageService().getSemanticNidStream(TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getNid());
                break;
            case STATED_RELATIONSHIP:
                this.intStreamSupplier = () -> Get.assemblageService().getSemanticNidStream(TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE.getNid());
                break;
            case TRANSITIVE_CLOSURE:
                this.intStreamSupplier = () -> Get.assemblageService().getSemanticNidStream(TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getNid());
                break;
            case VERSIONED_TRANSITIVE_CLOSURE:
                this.intStreamSupplier = () -> Get.assemblageService().getSemanticNidStream(TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getNid());
                break;
            case LANGUAGE_REFSET:
            case REFSET:
                this.intStreamSupplier = () -> Get.assemblageService().getSemanticNidStream(assemblageNid);
                break;
        }
    }

    private void setFilePath(){

        this.filePath = Paths.get(this.exportDirectory + this.parentDirectory +
                this.rf2ConfigType.getFilePath()
                .replace("TIME1", DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").format(localDateTime))
                .replace("TIME2", DateTimeFormatter.ofPattern("uuuuMMdd").format(localDateTime)));
    }

    private void setFilePath(int languageNid){

        this.filePath = Paths.get(this.exportDirectory + this.parentDirectory +  this.rf2ConfigType.getFilePath()
                .replace("TIME1", DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").format(localDateTime))
                .replace("TIME2", DateTimeFormatter.ofPattern("uuuuMMdd").format(localDateTime))
                .replace("LANGUAGE1", LanguageCoordinates.conceptNidToIso639(languageNid)));
    }

    private void setFilePath(VersionType versionType, String assemblageFQN){
        String pattern = "";

        switch (versionType){
            case MEMBER:
                break;
            case COMPONENT_NID:
                pattern = "c";
                break;
            case STRING:
            case LONG:
                pattern = "s";
                break;
            case Nid1_Int2:
                pattern = "ci";
                break;
            case Nid1_Nid2:
                pattern = "cc";
                break;
            case Nid1_Str2:
                pattern = "cs";
                break;
            case Str1_Str2:
                pattern = "ss";
                break;
            case Nid1_Nid2_Int3:
                pattern = "cci";
                break;
            case Nid1_Nid2_Str3:
                pattern = "ccs";
                break;
            case Str1_Nid2_Nid3_Nid4:
                pattern = "sccc";
                break;
            case Str1_Str2_Nid3_Nid4:
                pattern = "sscc";
                break;
            case Str1_Str2_Nid3_Nid4_Nid5:
                pattern = "ssccc";
                break;
            case Nid1_Int2_Str3_Str4_Nid5_Nid6:
                pattern = "cisscc";
                break;
            case Int1_Int2_Str3_Str4_Str5_Nid6_Nid7:
                pattern = "iissscc";
                break;
            case Str1_Str2_Str3_Str4_Str5_Str6_Str7:
                pattern = "sssssss";
                break;
            case LOINC_RECORD:
                pattern = "ssssssssss";
                break;
        }

        String formattedFQNForFileName = WordUtils.capitalizeFully(assemblageFQN);
        if(assemblageFQN.contains("(") && assemblageFQN.contains(")"))
            formattedFQNForFileName = formattedFQNForFileName
                    .replace(formattedFQNForFileName.substring(formattedFQNForFileName.indexOf("("),
                            formattedFQNForFileName.indexOf(")") + 1),"")
                    .replace(" ", "");
        if(formattedFQNForFileName.contains("ReferenceSet"))
            formattedFQNForFileName = formattedFQNForFileName.replace("ReferenceSet","");

        this.filePath = Paths.get(
                this.exportDirectory + this.parentDirectory + this.rf2ConfigType.getFilePath()
                .replace("TIME1", DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").format(localDateTime))
                .replace("TIME2", DateTimeFormatter.ofPattern("uuuuMMdd").format(localDateTime))
                .replace("PATTERN", pattern)
                .replace("SUMMARY", formattedFQNForFileName));
    }

    public Path getFilePath(){
        return this.filePath;
    }

    public String getZipDirectory() {
        return zipDirectory;
    }

    public String getFileHeader() {
        return fileHeader;
    }

    public RF2ConfigType getRf2ConfigType() {
        return rf2ConfigType;
    }

    public String getMessage() {
        return this.message;
    }

    public IntStream getIntStream() {
        return this.intStreamSupplier.get();
    }

    public List<String> getRefsetDescriptorDefinitions() {
        return refsetDescriptorDefinitions;
    }

    public boolean isDescriptorAssemblage() {
        return isDescriptorAssemblage;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }
}
