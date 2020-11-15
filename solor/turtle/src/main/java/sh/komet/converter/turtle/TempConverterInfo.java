package sh.komet.converter.turtle;

import org.apache.commons.lang3.StringUtils;
import sh.isaac.api.importers.ConverterInfo;
import sh.isaac.api.importers.UploadFileInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class TempConverterInfo implements ConverterInfo {
    private String srcArtifactId;
    private String srcVersionRegExpValidator;
    private String srcVersionDescription;
    private List<String> artifactSrcDependencies;
    private List<String> artifactIBDFDependencies;
    private List<UploadFileInfo> uploadFileInfo;

    /*
     * If we were really clever, we would pull this from an options file published with the converter itself.
     * unfortunately, that gets tricky, because the user needs to populate these when they are uploading, without necessarily knowing what particular
     * version of the converter will execute against this uploaded content.  So, will hardcode them here for now, and developers will have to manually
     * update these if the patterns change in the future.
     */
    protected final String converterGroupId = "sh.isaac.misc";
    protected final String converterArtifactId = "importers-mojos";
    private String converterOutputArtifactId;
    private String converterMojoName;  //Must match the value from the mojo - aka - @ Mojo( name = "convert-loinc-to-ibdf", defaultPhase... used as the goal in the pom.
    private String niceName;
    private String[] licenseInformation;
    private String[] noticeInformation;


    /**
     * @param artifactId
     * @param srcVersionRegExpValidator
     * @param srcVersionDescription
     * @param artifactSourceDependencies
     * @param artifactIBDFDependencies
     * @param uploadFileInfo
     * @param converterOutputArtifactId
     * @param converterMojoName
     * @param niceName
     * @param licenseFilePaths
     * @param noticeFilePaths
     */
    public TempConverterInfo(String artifactId, String srcVersionRegExpValidator, String srcVersionDescription, String[] artifactSourceDependencies,
                                    String[] artifactIBDFDependencies, UploadFileInfo[] uploadFileInfo, String converterOutputArtifactId, String converterMojoName,
                                    String niceName, String[] licenseFilePaths, String[] noticeFilePaths) {
        this.srcArtifactId = artifactId;
        this.srcVersionRegExpValidator = srcVersionRegExpValidator;
        this.srcVersionDescription = srcVersionDescription;
        this.artifactSrcDependencies = Arrays.asList(artifactSourceDependencies);
        this.artifactIBDFDependencies = Arrays.asList(artifactIBDFDependencies);
        this.uploadFileInfo = Arrays.asList(uploadFileInfo);
        this.converterOutputArtifactId = converterOutputArtifactId;
        this.converterMojoName = converterMojoName;
        this.niceName = niceName;
        this.licenseInformation = new String[licenseFilePaths.length];
        this.noticeInformation = new String[noticeFilePaths.length];
        try {
            for (int i = 0; i < licenseFilePaths.length; i++) {
                if (StringUtils.isBlank(licenseFilePaths[i])) {
                    licenseInformation[i] = "";
                } else {
                    licenseInformation[i] = Files.readString(new File(licenseFilePaths[i]).toPath());
                }
            }
            for (int i = 0; i < noticeFilePaths.length; i++) {
                if (StringUtils.isBlank(noticeFilePaths[i])) {
                    noticeInformation[i] = "";
                } else {
                    noticeInformation[i] = Files.readString(new File(noticeFilePaths[i]).toPath());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    //~--- get methods ---------------------------------------------------------

    @Override
    public List<String> getArtifactDependencies() {
        return this.artifactSrcDependencies;
    }

    @Override
    public String getArtifactId() {
        return this.srcArtifactId;
    }

    @Override
    public String getConverterArtifactId() {
        return this.converterArtifactId;
    }

    @Override
    public String getConverterGroupId() {
        return this.converterGroupId;
    }

    @Override
    public String getConverterMojoName() {
        return this.converterMojoName;
    }

    @Override
    public String getConverterOutputArtifactId() {
        return this.converterOutputArtifactId;
    }

    @Override
    public List<String> getIBDFDependencies() {
        return this.artifactIBDFDependencies;
    }

    @Override
    public String[] getLicenseInformation() {
        return this.licenseInformation;
    }

    @Override
    public String getNiceName() {
        return this.niceName;
    }

    @Override
    public String[] getNoticeInformation() {
        return this.noticeInformation;
    }

    @Override
    public String getSourceVersionDescription()
    {
        return srcVersionDescription;
    }

    @Override
    public String getSourceVersionRegExpValidator()
    {
        return srcVersionRegExpValidator;
    }

    @Override
    public List<UploadFileInfo> getUploadFileInfo() {
        return this.uploadFileInfo;
    }
}
