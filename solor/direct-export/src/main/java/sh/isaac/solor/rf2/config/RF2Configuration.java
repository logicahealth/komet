package sh.isaac.solor.rf2.config;

import org.apache.commons.text.WordUtils;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.TaxonomySnapshot;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.util.UuidT3Generator;
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
    private RF2FileType rf2FileType;
    private String message;
    private LocalDateTime localDateTime;
    private Supplier<IntStream> intStreamSupplier;
    private final String parentDirectory;
    private final String exportDirectory;
    private final String zipDirectory;
    private static HashMap<Integer, Integer[]> refsetDescriptorHeaders = new HashMap<>();
    private final List<String> refsetDescriptorDefinitions = new ArrayList<>();
    private boolean isDescriptorAssemblage = false;
    private long exportCount = 0;
    private final TaxonomySnapshot noTreeTaxonomySnapshot;
    private final RF2ReleaseType rf2ReleaseType;
    private final RF2ExportHelper rf2ExportHelper;


    public RF2Configuration(RF2FileType rf2FileType, RF2ReleaseType rf2ReleaseType, LocalDateTime localDateTime, File exportDirectory,
                            TaxonomySnapshot noTreeTaxonomySnapshot, RF2ExportHelper rf2ExportHelper) {
        this.rf2FileType = rf2FileType;
        this.rf2ReleaseType = rf2ReleaseType;
        this.localDateTime = localDateTime;
        this.fileHeader = rf2FileType.getFileHeader();
        this.message = rf2FileType.getMessage();
        this.parentDirectory = "/SnomedCT_SolorRF2_PRODUCTION_TIME1/"
                .replace("TIME1", DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").format(this.localDateTime));
        this.exportDirectory = exportDirectory.toString();
        this.rf2ExportHelper = rf2ExportHelper;
        this.zipDirectory = this.exportDirectory + this.parentDirectory.substring(0, this.parentDirectory.length() -1) + ".zip";

        this.noTreeTaxonomySnapshot = noTreeTaxonomySnapshot;

        setIntStreamSupplier(0);
        setFilePath();
    }

    public RF2Configuration(RF2FileType rf2ConfigType, RF2ReleaseType rf2ReleaseType, LocalDateTime localDateTime, int languageNid,
                            File exportDirectory, PreExportUtility preExportUtility, boolean isDescriptorAssemblagePresent,
                            TaxonomySnapshot noTreeTaxonomySnapshot, RF2ExportHelper rf2ExportHelper) {
        this.rf2FileType = rf2ConfigType;
        this.rf2ReleaseType = rf2ReleaseType;
        this.localDateTime = localDateTime;
        this.fileHeader = rf2ConfigType.getFileHeader();
        this.message = rf2ConfigType.getMessage() + " - " + LanguageCoordinates.conceptNidToIso639(languageNid);
        this.parentDirectory = "/SnomedCT_SolorRF2_PRODUCTION_TIME1/"
                .replace("TIME1", DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").format(this.localDateTime));
        this.exportDirectory = exportDirectory.toString();
        this.rf2ExportHelper = rf2ExportHelper;
        this.zipDirectory = this.exportDirectory + this.parentDirectory.substring(0, this.parentDirectory.length() -1) + ".zip";

        if(isDescriptorAssemblagePresent) {
            refsetDescriptorHeaders = preExportUtility.generateRefsetDescriptorHeaders();
        }

        this.noTreeTaxonomySnapshot = noTreeTaxonomySnapshot;

        setIntStreamSupplier(languageNid);
        setFilePath(languageNid);
        setFileHeader(languageNid);
    }

    public RF2Configuration(RF2FileType rf2FileType, RF2ReleaseType rf2ReleaseType, LocalDateTime localDateTime, int assemblageNid,
                            String assemblageFQN, VersionType versionType, File exportDirectory,
                            PreExportUtility preExportUtility, boolean isDescriptorAssemblagePresent,
                            TaxonomySnapshot noTreeTaxonomySnapshot, RF2ExportHelper rf2ExportHelper) {
        this.rf2FileType = rf2FileType;
        this.rf2ReleaseType = rf2ReleaseType;
        this.localDateTime = localDateTime;
        this.parentDirectory = "/SnomedCT_SolorRF2_PRODUCTION_TIME1/"
                .replace("TIME1", DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").format(this.localDateTime));
        this.exportDirectory = exportDirectory.toString();
        this.rf2ExportHelper = rf2ExportHelper;
        this.zipDirectory = this.exportDirectory + this.parentDirectory.substring(0, this.parentDirectory.length() -1) + ".zip";
        this.message = rf2FileType.getMessage() + " " + assemblageFQN;

        if(isDescriptorAssemblagePresent) {
            refsetDescriptorHeaders = preExportUtility.generateRefsetDescriptorHeaders();
            this.isDescriptorAssemblage = assemblageNid == Get.concept(UuidT3Generator.fromSNOMED("900000000000456007")).getNid();
        }

        this.noTreeTaxonomySnapshot = noTreeTaxonomySnapshot;

        setFilePath(versionType, assemblageFQN);
        setIntStreamSupplier(assemblageNid);
        setFileHeader(assemblageNid);
    }

    private void setFileHeader(int assemblageNid){

        if(refsetDescriptorHeaders.containsKey(assemblageNid)){
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(this.rf2FileType.getFileHeader());
            Integer[] tempColumns = refsetDescriptorHeaders.get(assemblageNid);

            for(int i = 1; i < tempColumns.length; i++){
                if(tempColumns[i] == null)
                    continue;
                stringBuilder.append(formatHeaderName(Get.concept(tempColumns[i]).getFullyQualifiedName().replace(" ", "")).toLowerCase())
                        .append("\t");
            }

            this.fileHeader = stringBuilder.toString().trim() + "\r\n";
        }else{

            if(this.rf2FileType.equals(RF2FileType.LANGUAGE_REFSET)) {
                this.fileHeader = rf2FileType.getFileHeader();
            }else {

                final StringBuilder descriptorStringBuilder = new StringBuilder();

                final String integerFieldDescription = this.rf2ExportHelper.getIdString(MetaData.INTEGER_FIELD____SOLOR.getNid());
                final String stringFieldDescription = this.rf2ExportHelper.getIdString(MetaData.STRING_FIELD____SOLOR.getNid());
                final String longFieldDescription = this.rf2ExportHelper.getIdString(MetaData.LONG_FIELD____SOLOR.getNid());

                Get.concept(assemblageNid).getVersionList().stream()
                        .forEach(version -> {

                            switch (Get.assemblageService().getVersionTypeForAssemblage(assemblageNid)) {
                                case MEMBER:
                                    this.fileHeader = rf2FileType.getFileHeader().trim() + "\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t0\r\n");
                                    break;
                                case COMPONENT_NID:
                                    this.fileHeader = rf2FileType.getFileHeader() + "componentId\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t0\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t1\r\n");
                                    break;
                                case STRING:
                                    this.fileHeader = rf2FileType.getFileHeader() + "stringValue\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t0\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + stringFieldDescription + "\t" + "707000009\t1\r\n");
                                    break;
                                case LONG:
                                    this.fileHeader = rf2FileType.getFileHeader() + "longValue\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t0\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + longFieldDescription + "\t" + "707000009\t1\r\n");
                                    break;
                                case Nid1_Int2:
                                    this.fileHeader = rf2FileType.getFileHeader() + "componentId\tintegerValue\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t0\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t1\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + integerFieldDescription + "\t" + "900000000000477005\t2\r\n");
                                    break;
                                case Nid1_Nid2:
                                    this.fileHeader = rf2FileType.getFileHeader() + "componentId\tcomponentId\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t0\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t1\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t2\r\n");
                                    break;
                                case Nid1_Str2:
                                    this.fileHeader = rf2FileType.getFileHeader() + "componentId\tstringValue\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t0\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t1\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + stringFieldDescription + "\t" + "707000009\t2\r\n");
                                    break;
                                case Str1_Str2:
                                    this.fileHeader = rf2FileType.getFileHeader() + "stringValue\tstringValue\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t0\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + stringFieldDescription + "\t" + "707000009\t1\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + stringFieldDescription + "\t" + "707000009\t2\r\n");
                                    break;
                                case Nid1_Nid2_Int3:
                                    this.fileHeader = rf2FileType.getFileHeader() + "componentId\tcomponentId\tintegerValue\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t0\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t1\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t2\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + integerFieldDescription + "\t" + "900000000000477005\t3\r\n");
                                    break;
                                case Nid1_Nid2_Str3:
                                    this.fileHeader = rf2FileType.getFileHeader() + "componentId\tcomponentId\tstringValue\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t0\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t1\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t2\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + stringFieldDescription + "\t" + "707000009\t3\r\n");
                                    break;
                                case Str1_Nid2_Nid3_Nid4:
                                    this.fileHeader = rf2FileType.getFileHeader() + "stringValue\tcomponentId\tcomponentId\tcomponentId\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t0\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + stringFieldDescription + "\t" + "707000009\t1\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t2\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t3\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t4\r\n");
                                    break;
                                case Str1_Str2_Nid3_Nid4:
                                    this.fileHeader = rf2FileType.getFileHeader() + "stringValue\tstringValue\tcomponentId\tcomponentId\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t0\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + stringFieldDescription + "\t" + "707000009\t1\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + stringFieldDescription + "\t" + "707000009\t2\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t3\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t4\r\n");
                                    break;
                                case Str1_Str2_Nid3_Nid4_Nid5:
                                    this.fileHeader = rf2FileType.getFileHeader() + "stringValue\tstringValue\tcomponentId\tcomponentId\tcomponentId\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t0\r");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + stringFieldDescription + "\t" + "707000009\t1\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + stringFieldDescription + "\t" + "707000009\t2\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t3\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t4\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t5\r\n");
                                    break;
                                case Nid1_Int2_Str3_Str4_Nid5_Nid6:
                                    this.fileHeader = rf2FileType.getFileHeader() + "componentId\tintegerValue\tstringValue\tstringValue\tcomponentId\tcomponentId\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t0\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t1\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + integerFieldDescription + "\t" + "900000000000477005\t2\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + stringFieldDescription + "\t" + "707000009\t3\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + stringFieldDescription + "\t" + "707000009\t4\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t5\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t6\r\n");
                                    break;
                                case Int1_Int2_Str3_Str4_Str5_Nid6_Nid7:
                                    this.fileHeader = rf2FileType.getFileHeader() + "integerValue\tintegerValue\tstringValue\tstringValue\tstringValue\tcomponentId\tcomponentId\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t0\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + integerFieldDescription + "\t" + "900000000000477005\t1\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + integerFieldDescription + "\t" + "900000000000477005\t2\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + stringFieldDescription + "\t" + "707000009\t3\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + stringFieldDescription + "\t" + "707000009\t4\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + stringFieldDescription + "\t" + "707000009\t5\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t6\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t7\r\n");
                                    break;
                                case Str1_Str2_Str3_Str4_Str5_Str6_Str7:
                                    this.fileHeader = rf2FileType.getFileHeader() + "stringValue\tstringValue\tstringValue\tstringValue\tstringValue\tstringValue\tstringValue\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t0\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + stringFieldDescription + "\t" + "707000009\t1\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + stringFieldDescription + "\t" + "707000009\t2\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + stringFieldDescription + "\t" + "707000009\t3\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + stringFieldDescription + "\t" + "707000009\t4\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + stringFieldDescription + "\t" + "707000009\t5\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + stringFieldDescription + "\t" + "707000009\t6\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + stringFieldDescription + "\t" + "707000009\t7\r\n");
                                    break;
                                case LOINC_RECORD:
                                    this.fileHeader = rf2FileType.getFileHeader() + "loincNum\tcomponent\tproperty\ttimeAspect\tsystem\tscaleType\tmethodType\tstatus\tshortName\tlongCommonName\r\n";
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + "449608002\t900000000000461009\t0\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + stringFieldDescription + "\t" + "707000009\t1\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + stringFieldDescription + "\t" + "707000009\t2\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + stringFieldDescription + "\t" + "707000009\t3\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + stringFieldDescription + "\t" + "707000009\t4\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + stringFieldDescription + "\t" + "707000009\t5\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + stringFieldDescription + "\t" + "707000009\t6\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + stringFieldDescription + "\t" + "707000009\t7\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + stringFieldDescription + "\t" + "707000009\t8\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + stringFieldDescription + "\t" + "707000009\t9\r\n");
                                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) + stringFieldDescription + "\t" + "707000009\t10\r\n");
                                    break;
                            }
                        });
            }
        }
    }

    private String createDescriptorBaseString(Version version, int assemblageNid, StringBuilder stringBuilder){

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

        final IdentifierService identifierService = Get.identifierService();

        switch (this.rf2FileType){
            case CONCEPT:
            case IDENTIFIER:

                this.intStreamSupplier = () -> Get.conceptService().getConceptNidStream();
                this.exportCount = Get.conceptService().getConceptCount();

                break;
            case DESCRIPTION:


                this.intStreamSupplier = () -> Arrays.stream(
                        this.noTreeTaxonomySnapshot
                                .getTaxonomyChildConceptNids(MetaData.LANGUAGE____SOLOR.getNid()))
                        .flatMap(nid -> Get.assemblageService().getSemanticNidStream(nid));

                Arrays.stream(
                        this.noTreeTaxonomySnapshot
                        .getTaxonomyChildConceptNids(MetaData.LANGUAGE____SOLOR.getNid()))
                        .forEach(nid -> this.exportCount += identifierService.getNidsForAssemblage(nid).count());

                break;
            case RELATIONSHIP:

                this.intStreamSupplier = () -> Get.assemblageService().getSemanticNidStream(TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getNid());
                this.exportCount = identifierService.getNidsForAssemblage(TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE.getNid()).count();

                break;
            case STATED_RELATIONSHIP:

                this.intStreamSupplier = () -> Get.assemblageService().getSemanticNidStream(TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE.getNid());
                this.exportCount = identifierService.getNidsForAssemblage(TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE.getNid()).count();

                break;

            case LANGUAGE_REFSET:
            case REFSET:

                this.intStreamSupplier = () -> Get.assemblageService().getSemanticNidStream(assemblageNid);
                this.exportCount = identifierService.getNidsForAssemblage(assemblageNid).count();

                break;
        }
    }

    private void setFilePath(){

        this.filePath = Paths.get(this.exportDirectory + this.parentDirectory +
                this.rf2FileType.getFilePath()
                        .replace("TIME1", DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").format(localDateTime))
                        .replace("TIME2", DateTimeFormatter.ofPattern("uuuuMMdd").format(localDateTime))
                        .replace("RELEASETYPE", this.rf2ReleaseType.toString()));
    }

    private void setFilePath(int languageNid){

        this.filePath = Paths.get(this.exportDirectory + this.parentDirectory +  this.rf2FileType.getFilePath()
                .replace("TIME1", DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").format(localDateTime))
                .replace("TIME2", DateTimeFormatter.ofPattern("uuuuMMdd").format(localDateTime))
                .replace("LANGUAGE1", LanguageCoordinates.conceptNidToIso639(languageNid))
                .replace("RELEASETYPE", this.rf2ReleaseType.toString()));
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
                this.exportDirectory + this.parentDirectory + this.rf2FileType.getFilePath()
                        .replace("TIME1", DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").format(localDateTime))
                        .replace("TIME2", DateTimeFormatter.ofPattern("uuuuMMdd").format(localDateTime))
                        .replace("PATTERN", pattern)
                        .replace("SUMMARY", formattedFQNForFileName)
                        .replace("RELEASETYPE", this.rf2ReleaseType.toString()));
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

    public RF2FileType getRf2FileType() {
        return rf2FileType;
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

    public long getExportCount(){
        return this.exportCount;
    }

    public TaxonomySnapshot getNoTreeTaxonomySnapshot() {
        return noTreeTaxonomySnapshot;
    }

    public RF2ReleaseType getRf2ReleaseType() {
        return rf2ReleaseType;
    }
}
