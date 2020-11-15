package sh.isaac.api.importers;

import java.util.List;

public interface ConverterInfo {

    /**
     * In order to execute a conversion of the specified type, you must also provide dependencies for each of the listed
     * Source artifact identifiers.
     * <p>
     * This is used during IBDF CONVERSION
     *
     * @return the artifact dependencies
     */
    List<String> getArtifactDependencies();

    /**
     * Note that the artifactID may include a wildcard ('*') for some, such as SCT_EXTENSION - note - this is the pattern
     * for the source artifact upload, not the artifact id related to the converter.
     * <p>
     * This is used during SOURCE UPLOAD
     *
     * @return the artifact id
     */
    String getArtifactId();

    /**
     * Not for PRISME.
     *
     * @return the converter artifact id
     */
    String getConverterArtifactId();

    /**
     * Not for PRISME.
     *
     * @return the converter group id
     */
    String getConverterGroupId();

    /**
     * Not for PRISME.
     *
     * @return the converter mojo name
     */
    String getConverterMojoName();

    /**
     * Not for PRISME.
     *
     * @return the converter output artifact id
     */
    String getConverterOutputArtifactId();

    /**
     * In order to execute a conversion of the specified type, you must also provide dependencies for each of the listed
     * IBDF artifact identifiers.
     * <p>
     * This is used during IBDF CONVERSION
     *
     * @return the IBDF dependencies
     */
    List<String> getIBDFDependencies();

    /**
     * Not for PRISME.
     *
     * @return the license information
     */
    String[] getLicenseInformation();

    /**
     * Not for PRISME (but you can use it if you want).
     *
     * @return the nice name
     */
    String getNiceName();

    /**
     * Not for PRISME.
     *
     * @return the notice information
     */
    String[] getNoticeInformation();

    /**
     * This is used during SOURCE UPLOAD
     *
     * @return The descriptive text to provide to the end user to meet the regexp requirements given by {@link #getSourceVersionRegExpValidator()}
     */
    String getSourceVersionDescription();

    /**
     * The regular expression that should be satisfied for the version number given to the uploaded source artifact(s).  The value provided to
     * the {@link SrcUploadCreator#createSrcUploadConfiguration(SupportedConverterTypes, String, String, List, String, String, char[], String,
     * String, String, java.io.File, boolean, boolean)}
     * for the 'version' parameter should meet this regexp.
     * <p>
     * This is used during SOURCE UPLOAD
     *
     * @return the regular expression
     */
    String getSourceVersionRegExpValidator();

    /**
     * The information describing the files that an end user must upload into the system to allow the execution of a particular converter.
     * <p>
     * This is used during SOURCE UPLOAD
     *
     * @return the upload file info
     */
    List<UploadFileInfo> getUploadFileInfo();
}
