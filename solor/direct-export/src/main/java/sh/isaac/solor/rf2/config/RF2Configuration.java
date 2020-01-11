package sh.isaac.solor.rf2.config;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import org.apache.commons.text.WordUtils;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.TaxonomySnapshot;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUsageDescription;
import sh.isaac.api.util.UuidT3Generator;
import sh.isaac.model.configuration.LanguageCoordinates;
import sh.isaac.model.semantic.DynamicUsageDescriptionImpl;
import sh.isaac.solor.rf2.utility.PreExportUtility;
import sh.isaac.solor.rf2.utility.RF2ExportHelper;

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
    
    
    /**
     * TODO 
     * One should take a look at {@link DynamicDataType#translateSCTIDMetadata} to see how SCTIDs are translated to types during import, 
     * These export rules below are likely not correct - we should probably be tracking the actual translations per refset, so they can be 
     * exported again correctly.  
     * As an example, there are  at least 6 different SCTIDs that are translated into our "String" field.  The only one we are writing back out
     * is parsableString, which obviously isn't correct.
     */
    public static final String REFERENCED_COMPONENT = "449608002";
    private static final String CONCEPT_TYPE_COMPONENT = "900000000000461009";
    public static final String SIGNED_INTEGER = "900000000000477005";
    public static final String PARSABLE_STRING = "707000009";
    private static final String REFSET_DESCRIPTOR_REFSET = "900000000000456007";


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
            this.isDescriptorAssemblage = assemblageNid == Get.concept(UuidT3Generator.fromSNOMED(REFSET_DESCRIPTOR_REFSET)).getNid();
        }

        this.noTreeTaxonomySnapshot = noTreeTaxonomySnapshot;

        setFilePath(assemblageNid, assemblageFQN);
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

                Get.concept(assemblageNid).getVersionList().stream().forEach(version -> {
                    this.refsetDescriptorDefinitions.add(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder) 
                            + REFERENCED_COMPONENT + "\t" + CONCEPT_TYPE_COMPONENT + "\t0\r\n");
                    DynamicUsageDescription dud = DynamicUsageDescriptionImpl.mockOrRead(assemblageNid);
                    
                    //Build the header row for our data columns
                    StringBuilder header = new StringBuilder();
                    header.append(rf2FileType.getFileHeader());
                    for (DynamicColumnInfo dci : dud.getColumnInfo()) {
                        switch (dci.getColumnDataType()) {
                            case INTEGER:
                            case LONG:
                            case STRING:
                            case BOOLEAN:
                            case DOUBLE:
                            case FLOAT:
                                header.append(dci.getColumnName());
                                header.append("\t");
                            //I'm not entirely sure if it is better to use the name componentId for these, or if we should use the actual semantic column name...
                            case NID:
                            case UUID:
                                header.append("componentId\t");
                                break;
                            case ARRAY:
                            case BYTEARRAY:
                            case POLYMORPHIC:
                            case UNKNOWN:
                            default :
                                throw new RuntimeException("Unexpected type! " + dci.getColumnDataType());
                        }
                        
                        //header row handled, now add the descriptor row for this column
                        
                        StringBuilder descriptor = new StringBuilder();
                        descriptor.append(createDescriptorBaseString(version, assemblageNid, descriptorStringBuilder));
                        //TODO I'm not sure this idString thing really does the right thing here... 
                        //Add the 'attributeDescription'
                        descriptor.append(rf2ExportHelper.getIdString(Get.nidForUuids(dci.getColumnDescriptionConcept())));
                        descriptor.append("\t");
                        //Add the 'attributeType'
                        switch (dci.getColumnDataType()) {  //Note, these aren't likely correct, we should be specifying more specific types.  
                            //See input mapping in {@link DynamicDataType#translateSCTIDMetadata}
                            case INTEGER:
                                descriptor.append(SIGNED_INTEGER);
                                descriptor.append("\t");
                                break;
                            case NID:
                            case UUID:
                                descriptor.append(CONCEPT_TYPE_COMPONENT);
                                descriptor.append("\t");
                                break;
                            case LONG:  //Not sure we want to treat longs as strings, but that is what they do in the file naming, below...
                            case STRING:
                                descriptor.append(PARSABLE_STRING);
                                descriptor.append("\t");
                                break;
                            case BOOLEAN:
                            case DOUBLE:
                            case FLOAT:
                            case ARRAY:
                            case BYTEARRAY:
                            case POLYMORPHIC:
                            case UNKNOWN:
                            default :
                                throw new RuntimeException("Unexpected type! " + dci.getColumnDataType());
                        }
                        //Add the attributeOrder
                        descriptor.append(dci.getColumnOrder() + 1);
                        descriptor.append("\r\n");
                        this.refsetDescriptorDefinitions.add(descriptor.toString());
                    }
                    
                    if (header.charAt(header.length() - 1) == '\t')
                    {
                        header.setLength(header.length() - 1);
                    }
                    this.fileHeader = header.toString();

                });
            }
        }
    }

    /**
     * Creates the first 6 columns of https://confluence.ihtsdotools.org/display/DOCRELFMT/5.2.11+Reference+Set+Descriptor
     * with a trailing tab
     */
    private String createDescriptorBaseString(Version version, int assemblageNid, StringBuilder stringBuilder){

        stringBuilder.setLength(0);

        //TODO why on earth is this exporting random UUIDs?  Thats useless.....
        return stringBuilder
                .append(UUID.randomUUID().toString() + "\t")
                .append(rf2ExportHelper.getTimeString(version) + "\t")
                .append(rf2ExportHelper.getActiveString(version) + "\t")
                .append(rf2ExportHelper.getIdString(version.getModuleNid()) + "\t")
                .append(REFSET_DESCRIPTOR_REFSET + "\t")
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
            default :
                throw new RuntimeException("Unsupported RF2 File Type " + this.rf2FileType);
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

    private void setFilePath(int assemblageNid, String assemblageFQN){
        StringBuilder pattern = new StringBuilder();

        DynamicUsageDescription dud = DynamicUsageDescriptionImpl.mockOrRead(assemblageNid);
        
        for (DynamicColumnInfo dci : dud.getColumnInfo()) {
            switch (dci.getColumnDataType()) {  
                case INTEGER:
                    pattern.append("i");
                    break;
                case NID:
                case UUID:
                    pattern.append("c");
                    break;
                case LONG:  //Not sure we want to treat longs as strings, but that is what they do in the file naming, below...
                case STRING:
                    pattern.append("s");
                    break;
                case BOOLEAN:
                case DOUBLE:
                case FLOAT:
                case ARRAY:
                case BYTEARRAY:
                case POLYMORPHIC:
                case UNKNOWN:
                default :
                    throw new RuntimeException("Unexpected type! " + dci.getColumnDataType());
            }
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
                        .replace("PATTERN", pattern.toString())
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
