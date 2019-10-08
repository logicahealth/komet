/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



package sh.isaac.pombuilder.converter;

import java.io.IOException;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import javafx.util.Pair;

import sh.isaac.pombuilder.FileUtil;
import sh.isaac.pombuilder.upload.SrcUploadCreator;

/**
 * {@link SupportedConverterTypes}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public enum SupportedConverterTypes
{
	//(?i) and (?-i) constructs are not supported in JavaScript (they are in Ruby)
	LOINC("loinc-src-data", ".*$", 
			"A typical LOINC version number is '2.59'.  The version numbers should be used directly from LOINC.  There are no enforced restrictions on the format.",
			new String[] {}, new String[] {}, new UploadFileInfo[] {
			new UploadFileInfo("", "https://loinc.org/downloads/loinc", 
					"LOINC_2.54_Text.zip",
					"The primary LOINC file is the 'LOINC Table File' in the csv format'.  This should be a zip file that contains a file named 'loinc.csv'."
					+ "  Additionally, the zip file may (optionally) contain 'map_to.csv' and 'source_organization.csv'."
					+ "  The zip file must contain 'text' within its name.", ".*text.*\\.zip$", true),
			new UploadFileInfo("", "https://loinc.org/downloads/files/loinc-multiaxial-hierarchy", 
					"LOINC_2.54_MULTI-AXIAL_HIERARCHY.zip",
					"The Multiaxial Hierarchy file is a zip file that contains a file named *multi-axial_hierarchy.csv.  The zip file containing the multiaxial hierarchy"
					+ " must contain 'multi-axial_hierarchy' within its name", ".*multi\\-axial_hierarchy.*\\.zip$", true),
			new UploadFileInfo("", "https://loinc.org/downloads/loinc",
					"LOINC_ReleaseNotes.txt",
					"The LOINC Release Notes file must be included for recent versions of LOINC.", ".*releasenotes\\.txt$", true)
	}, "loinc-ibdf", "convert-loinc-to-ibdf", "LOINC", 
			new String[] {"shared/licenses/loinc.xml"}, 
			new String[] {"shared/noticeAdditions/loinc-NOTICE-addition.txt"}),
	
	LOINC_TECH_PREVIEW("loinc-src-data-tech-preview", ".*$", 
			"A typical LOINC tech preview version number is '2015.08.01'.  The version numbers should be used directly from LOINC.  There are no enforced restrictions "
			+ "on the format.", 
			new String[] {"loinc-src-data"}, new String[] {"rf2-ibdf-sct"}, new UploadFileInfo[] {
			new UploadFileInfo("", "https://www.nlm.nih.gov/healthit/snomedct/international.html",
					"SnomedCT_LOINC_AlphaPhase3_INT_20160401.zip",
					"  The expected file is the RF2 release (NOT the Human Readable release nor the OWL release). "
					+ "The file must be a zip file, which ends with .zip", ".*\\.zip$", true)
	}, "loinc-ibdf-tech-preview", "convert-loinc-tech-preview-to-ibdf", "LOINC Tech Preview", 
			new String[] {"shared/licenses/loinc.xml", "shared/licenses/sct.xml"}, 
			new String[] {"shared/noticeAdditions/loinc-tech-preview-NOTICE-addition.txt", "shared/noticeAdditions/loinc-NOTICE-addition.txt", "shared/noticeAdditions/rf2-sct-NOTICE-addition.txt"}),
	
	SCT("rf2-src-data-sct", "\\d{8}.*$", 
			"A typical Snomed version number is '20170131' or '20170131T120000'.  The value here should be the same as the version number in the name of the uploaded "
			+ "zip file.  This requires a 4 digit year, 2 digit month, 2 digit day.  Any values can be appended after the 8 digits.",
			new String[] {}, new String[] {}, new UploadFileInfo[] {
			new UploadFileInfo("", "https://www.nlm.nih.gov/healthit/snomedct/international.html",
					"SnomedCT_RF2Release_INT_20160131.zip",
					"The expected file is the RF2 release zip file.  The filename must end with .zip, and must contain the release date in the Snomed standard"
					+ " naming convention (4 digit year, 2 digit month, 2 digit day).",
					".*_\\d{8}.*\\.zip$", true)
	}, "rf2-ibdf-sct", "convert-RF2-direct-to-ibdf", "SnomedCT", 
			new String[] {"shared/licenses/sct.xml"},
			new String[] {"shared/noticeAdditions/rf2-sct-NOTICE-addition.txt"}),
	
	SCT_EXTENSION("rf2-src-data-*-extension", "\\d{8}.*$", 
			"A typical Snomed extension version number is '20170131' or '20170131T120000'.  The value here should be the same as the version number in the name of the uploaded "
			+ "zip file.  This requires a 4 digit year, 2 digit month, 2 digit day.  Any values can be appended after the 8 digits.",
			new String[] {}, new String[] {"rf2-ibdf-sct"}, new UploadFileInfo[] {
			new UploadFileInfo("Snomed Extensions come from a variety of sources.  Note that the NLM has choosen to stop advertising the download links to the "
					+ " US Extension, but still publishes it.  The current download pattern is now: "
					+ "https://download.nlm.nih.gov/mlb/utsauth/USExt/SnomedCT_USExtensionRF2_Production_YYYYMMDDTHHMMSS.zip",
					"",
					"SnomedCT_USExtensionRF2_Production_20170301T120000.zip",
					"The expected file is the RF2 release zip file.  The filename must end with .zip, and must contain the release date in the Snomed standard"
					+ " naming convention (4 digit year, 2 digit month, 2 digit day) - it also now also accepts the new naming convention with T and 2 digits each "
					+ "of hour, minute and second.",
					".*_\\d{8}.*\\.zip$", true)
	}, "rf2-ibdf-", "convert-RF2-direct-to-ibdf", "SnomedCT Extension", 
			new String[] {"shared/licenses/sct.xml"},
			new String[] {"shared/noticeAdditions/rf2-sct-NOTICE-addition.txt"}),
	
	VHAT("vhat-src-data", "\\d{4}\\.\\d{2}\\.\\d{2}.*$", 
			"A typical VHAT version number is '2017.05.04' - which by convention, is the date that the content was exported from VETs."
			+ "  The first 10 characters of this string must follow the date format YYYY.MM.DD.  After the first 10 characters, there are no enforced restrictions"
			+ " on the format of this value.", 
			new String[] {}, new String[] {}, new UploadFileInfo[] {
			new UploadFileInfo("VHAT content is typically exported from a VETs system.  ", "",
					"VHAT.xml",
					"Any XML file that is valid per the VETs TerminologyData.xsd schema.  The file name is ignored", 
					".*\\.xml$", true)
	}, "vhat-ibdf", "convert-VHAT-to-ibdf", "VHAT", 
			new String[] {""}, 
			new String[] {""}),
	
	RXNORM("rxnorm-src-data", ".*$", 
			"A typical RxNorm version number is '2017.04.03'.  This value should come directly from the RxNorm release information, however, by convention, is"
			+ " reformatted to yyyy.mm.dd for better sorting in the artifact storage system.  There are no enforced restrictions on the format of this value.", 
			new String[] {}, new String[] {"rf2-ibdf-sct"}, new UploadFileInfo[] {
			new UploadFileInfo("", "https://www.nlm.nih.gov/research/umls/rxnorm/docs/rxnormfiles.html", 
					"RxNorm_full_06062016.zip",
					"The file must be a zip file, which starts with 'rxnorm_full' and ends with .zip", "rxnorm_full.*\\.zip$", true)
	}, "rxnorm-ibdf", "convert-rxnorm-to-ibdf", "RxNorm", 
			new String[] {"shared/licenses/rxnorm.xml"}, 
			new String[] {"shared/noticeAdditions/rxnorm-NOTICE-addition.txt"}),
//	RXNORM_SOLOR("rxnorm-src-data", new String[] {}, new String[] {"rf2-ibdf-sct"}, new UploadFileInfo[] {
//			new UploadFileInfo("", "https://www.nlm.nih.gov/research/umls/rxnorm/docs/rxnormfiles.html", 
//					"RxNorm_full_06062016.zip",
//					"The file must be a zip file, which starts with 'rxnorm_full' and ends with .zip", "rxnorm_full.*\\.zip$", true)
//	}, "rxnorm-ibdf-solor", "convert-rxnorm-solor-to-ibdf", "RxNorm Solor", 
//			new String[] {"shared/licenses/rxnorm.xml"}, 
//			new String[] {"shared/noticeAdditions/rxnorm-NOTICE-addition.txt"}),
	
	HL7v3("hl7v3-src-data", ".*$", 
			"A typical HL7v3 version number is '2.47.7'.  This value should come directly from the release information.  There are no enforced restrictions on the "
			+ "format of this value", 
			new String[] {}, new String[] {}, new UploadFileInfo[] {
		new UploadFileInfo("", "http://gforge.hl7.org/gf/project/design-repos/frs/?action=FrsReleaseBrowse&frs_package_id=30", 
				"hl7-rimRepos-2.47.7.zip",
				"The file must be a zip file, which should have 'rimRepos' in the file name and end with '.zip'.  This uploaded zip file" +
				 " MUST contain a file that has 'DEFN=UV=VO' in the file name, and ends with .coremif", ".*rim.*\\.zip$", true)
	}, "hl7v3-ibdf", "convert-hl7v3-to-ibdf", "HL7v3", 
			new String[] {"shared/licenses/hl7v3.xml"}, 
			new String[] {"shared/noticeAdditions/hl7v3-NOTICE-addition.txt"}),
	
	NUCC("nucc-src-data", ".*$", 
			"A typical NUCC version number is '17.0'.  This value should come directly from the release information.  There are no enforced restrictions on the format "
			+ " of this value.", 
			new String[] {}, new String[] {}, new UploadFileInfo[] {
			new UploadFileInfo("", "http://www.nucc.org/index.php/code-sets-mainmenu-41/provider-taxonomy-mainmenu-40/csv-mainmenu-57",
					"nucc_taxonomy_170.csv",
					"The file name is ignored - it just needs to be a csv file which ends with .csv.", 
					".*\\.csv$", true)
	}, "nucc-ibdf", "convert-NUCC-to-ibdf", "National Uniform Claim Committee", 
			new String[] {""}, // Cannot find explicit license statement at nucc.org (perhaps AMA?)
			new String[] {""}), // No explicit copyright notice text found to use
	CVX("cvx-src-data", ".*$", 
			"A typical CVX version number is '2017-02-06'.  This value can either be the date the file is downloaded from the source website, or the newest "
			+ "'Last Updated Date' from the downloaded content.  There are no enforced restrictions on the format of this value." , 
			new String[] {}, new String[] {}, new UploadFileInfo[] {
			new UploadFileInfo("", "https://www2a.cdc.gov/vaccines/iis/iisstandards/vaccines.asp?rpt=cvx",
					"cvx.xml",
					"The file name is ignored - it just needs to be an xml file which ends with .xml.  Download the 'XML-new format' type, " + 
					"and store it into a file with the extension .xml.  The recommended version to use for the source upload is YYYY-MM-DD of the download.", 
					".*\\.xml$", true)
	}, "cvx-ibdf", "convert-CVX-to-ibdf", "Current Vaccines Administered", 
			new String[] {""}, // No explicit license statement CDC, other than inter-governmental aggreements would be issued
			new String[] {""}), // No explicit copyright notice text found to use
	
	MVX("mvx-src-data", ".*$", 
			"A typical MVX version number is '2017-02-06'.  This value can either be the date the file is downloaded from the source website, or the newest "
			+ "'Last Updated Date' from the downloaded content.  There are no enforced restrictions on the format of this value." , 
			new String[] {}, new String[] {}, new UploadFileInfo[] {
			new UploadFileInfo("", "https://www2a.cdc.gov/vaccines/iis/iisstandards/vaccines.asp?rpt=mvx",
					"mvx.xml",
					"The file name is ignored - it just needs to be an xml file which ends with .xml.  Download the 'XML-new format' type, " + 
					"and store it into a file with the extension .xml.  The recommended version to use for the source upload is YYYY-MM-DD of the download.", 
					".*\\.xml$", true)
	}, "mvx-ibdf", "convert-MVX-to-ibdf", "Manufacturers of Vaccines", 
			new String[] {""}, // No explicit license statement CDC, other than inter-governmental aggreements would be issued
			new String[] {""}), // No explicit copyright notice text found to use
	
	CPT("cpt-src-data", "\\d{4}$", 
			"A typical CPT version number is '2017'.  This value must be the 4 digit year of the release date of the content.", 
			new String[] {}, new String[] {}, new UploadFileInfo[] {
			new UploadFileInfo("CPT is licensed, and is only available to a licensed user.  The VA has a license, but in practice, has purchased a copy for ease of access.  "
					+ "See Jazz 355069", 
					"https://commerce.ama-assn.org/store/catalog/productDetail.jsp?product_id=prod2680002&navAction=push",
					"cpt.zip",
					"The file name is ignored - it just needs to be a zip file which ends in .zip.  The zip file must contain "
					+ "LONGULT.txt, MEDU.txt and SHORTU.txt", 
					".*\\.zip$", true)
	}, "cpt-ibdf", "convert-CPT-to-ibdf", "Current Procedural Terminology", 
			new String[] {"shared/licenses/cpt.xml"},
			new String[] {"shared/noticeAdditions/cpt-NOTICE-addition.txt"}),
	
	ICD10_CM("icd10-src-data-cm", "\\d{4}$", 
			"A typical ICD10 CM version number is '2017'.  This value must be the 4 digit year of the release date of the content.", 
			new String[] {}, new String[] {}, new UploadFileInfo[] {
			new UploadFileInfo("", "https://www.cms.gov/Medicare/Coding/ICD10", 
					"2017-ICD10-Code-Descriptions.zip",
					"The file must be a zip file, which should be downloaded from the 'YYYY ICD-10-CM and GEMs'"
					+ " section, have 'Code-Descriptions' in the file name and end with '.zip'.  This uploaded zip file"
					+ " MUST contain a file that has 'order_YYYY' in the file name, and ends with .txt", 
					".*\\d{4}.*\\.zip$", true)
	}, "icd10-ibdf-cm", "convert-ICD10-to-ibdf", "International Classification of Diseases, Tenth Revision, Clinical Modification ", 
			new String[] {""},  // Cannot find license text from cms.gov or documentation
			new String[] {""}), // Cannot find copyright notice from cms.gov or documentation
		
	ICD10_PCS("icd10-src-data-pcs", "\\d{4}$", 
			"A typical ICD10 PCS version number is '2017'.  This value must be the 4 digit year of the release date of the content.", 
			new String[] {}, new String[] {}, new UploadFileInfo[] {
			new UploadFileInfo("", "https://www.cms.gov/Medicare/Coding/ICD10", 
					"2017-PCS-Long-Abbrev-Titles.zip",
					"The file must be a zip file, which should be downloaded from the 'YYYY ICD-10-PCS and GEMs'"
					+ " section, have 'Long-Abbrev-Titles' or 'Order-File' in the file name and end with '.zip'.  This uploaded zip file"
					+ " MUST contain a file that has 'order_YYYY' in the file name, and ends with .txt", 
					".*\\d{4}.*\\.zip$", true)
	}, "icd10-ibdf-pcs", "convert-ICD10-to-ibdf", "International Classification of Diseases, Tenth Revision, Procedure Coding System", 
			new String[] {""}, // Cannot find license text from cms.gov or documentation
			new String[] {""}), // Cannot find copyright notice from cms.gov or documentation
	
	SOPT("sopt-src-data", ".*$", 
			"A typical SOPT version number is 'cdc-v4-phdsc-v7'.  There are no enforced restrictions on the format of this value.", 
			new String[] {}, new String[] {}, new UploadFileInfo[] {
			new UploadFileInfo("", "https://phinvads.cdc.gov/vads/ViewValueSet.action?oid=2.16.840.1.114222.4.11.3591", 
					"ValueSet_PHVS_SourceOfPaymentTypology_PHDSC_V4_20170425-004232.zip",
					"The actual source is here, http://www.phdsc.org/standards/payer-typology.asp#archives, but the zipped xls format that we require"
					+ " is found here: https://phinvads.cdc.gov/vads/ViewValueSet.action?oid=2.16.840.1.114222.4.11.3591 - the available zipped xls download "
					+ " must contain the letters PHDSC and should contain a zip file with 1 or more xls files - where one of the xls files contains the letters "
					+ " 'PHDSC' in the file name.  Also note that the versioning is very confusing, because while PHDSC is currently at version 7, the CDC "
					+ "  releases it as version 4.  We recommend using the version naming pattern of 'cdc-v4-phdsc-v7' to help remove confusion.", 
					".*PHDSC.*\\.zip$", true)
	}, "sopt-ibdf", "convert-SOPT-to-ibdf", "Source of Payment Typology", 
			new String[] {"shared/licenses/sopt.xml"},
			new String[] {"shared/noticeAdditions/sopt-NOTICE-addition.txt"}),
	
	BEVON("bevon-src-data", ".*$", 
			"A typical bevon version number is '0.8'.  There are no enforced restrictions on the format of this value.", 
			new String[] {}, new String[] {}, new UploadFileInfo[] {
			new UploadFileInfo("", "https://github.com/jgkim/bevon", 
					"https://raw.githubusercontent.com/jgkim/bevon/master/0.8/ttl",
					"The file name is ignored - it just needs to be a turtle formatted file which ends with .ttl.", 
					".*.ttl$", true)
	}, "bevon-ibdf", "convert-turtle-to-ibdf", "Beverage Ontology", 
			new String[] {"shared/licenses/bevon.xml"},
			new String[] {"shared/noticeAdditions/bevon-NOTICE-addition.txt"}), 
	
	FHIR("fhir-src-data-*", ".*$", 
				"FHIR terminology version numbers are typically numeric, but no patterns are enforced at present.",
				new String[] {}, new String[] {}, new UploadFileInfo[] {
				new UploadFileInfo("FHIR terminologies come from a variety of locations.  The core definitions can be found at https://www.hl7.org/fhir/downloads.html, "
						+ "the core file is definitions.xml.zip.  You may upload individual XML files, or zip files with multiple xml files.  Use the extension type " 
						+ "field to specify the specific terminology, or a general name like 'core-definitions'",
						"https://www.hl7.org/fhir/downloads.html",
						"definitions.xml.zip",
						"The file name is ignored.  If it is an XML file, it needs to match the FHIR V4 XML schema.  If it is a zip file, this will process any found "
						+ "XML file as a FHIR formatted file.",
						".*\\.(zip|xml)$", true)
		}, "fhir-ibdf-", "convert-FHIR-to-ibdf", "FHIR Terminology", 
				new String[] {"shared/licenses/fhir.xml"},
				new String[] {"shared/noticeAdditions/fhir-NOTICE-addition.txt"}),
	;
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
	 * @param sourceUploadGroupId
	 * @param niceName
	 * @param licenseFilePaths
	 * @param noticeFilePaths
	 */
	private SupportedConverterTypes(String artifactId, String srcVersionRegExpValidator, String srcVersionDescription, String[] artifactSourceDependencies,
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
					licenseInformation[i] = FileUtil.readFile(licenseFilePaths[i]);
				}
			}
			for (int i = 0; i < noticeFilePaths.length; i++) {
				if (StringUtils.isBlank(noticeFilePaths[i])) {
					noticeInformation[i] = "";
				} else {
					noticeInformation[i] = FileUtil.readFile(noticeFilePaths[i]);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	//~--- get methods ---------------------------------------------------------

	/**
	 * In order to execute a conversion of the specified type, you must also provide dependencies for each of the listed
	 * Source artifact identifiers.
	 *
	 * This is used during IBDF CONVERSION
	 *
	 * @return the artifact dependencies
	 */
	public List<String> getArtifactDependencies() {
		return this.artifactSrcDependencies;
	}

	/**
	 * Note that the artifactID may include a wildcard ('*') for some, such as SCT_EXTENSION - note - this is the pattern
	 * for the source artifact upload, not the artifact id related to the converter.
	 *
	 * This is used during SOURCE UPLOAD
	 *
	 * @return the artifact id
	 */
	public String getArtifactId() {
		return this.srcArtifactId;
	}

	/**
	 * Not for PRISME.
	 *
	 * @return the converter artifact id
	 */
	protected String getConverterArtifactId() {
		return this.converterArtifactId;
	}

	/**
	 * Not for PRISME.
	 *
	 * @return the converter group id
	 */
	protected String getConverterGroupId() {
		return this.converterGroupId;
	}

	/**
	 * Not for PRISME.  
	 *
	 * @return the converter mojo name
	 */
	public String getConverterMojoName() {
		return this.converterMojoName;
	}

	/**
	 * Not for PRISME.
	 *
	 * @return the converter output artifact id
	 */
	public String getConverterOutputArtifactId() {
		return this.converterOutputArtifactId;
	}

	/**
	 * In order to execute a conversion of the specified type, you must also provide dependencies for each of the listed
	 * IBDF artifact identifiers.
	 *
	 * This is used during IBDF CONVERSION
	 *
	 * @return the IBDF dependencies
	 */
	public List<String> getIBDFDependencies() {
		return this.artifactIBDFDependencies;
	}

	/**
	 * Not for PRISME.
	 *
	 * @return the license information
	 */
	public String[] getLicenseInformation() {
		return this.licenseInformation;
	}

	/**
	 * Not for PRISME (but you can use it if you want).
	 *
	 * @return the nice name
	 */
	public String getNiceName() {
		return this.niceName;
	}

	/**
	 * Not for PRISME.
	 *
	 * @return the notice information
	 */
	public String[] getNoticeInformation() {
		return this.noticeInformation;
	}

	/**
	 * 
	 * This is used during SOURCE UPLOAD
	 * @return The descriptive text to provide to the end user to meet the regexp requirements given by {@link #getSourceVersionRegExpValidator()}  
	 */
	public String getSourceVersionDescription()
	{
		return srcVersionDescription;
	}
	
	/**
	 * The regular expression that should be satisfied for the version number given to the uploaded source artifact(s).  The value provided to 
	 * the {@link SrcUploadCreator#createSrcUploadConfiguration(SupportedConverterTypes, String, String, List, String, String, char[], String, 
	 * String, String, java.io.File, boolean, boolean)}
	 * for the 'version' parameter should meet this regexp.
	 * 
	 * This is used during SOURCE UPLOAD
	 * @return the regular expression 
	 */
	public String getSourceVersionRegExpValidator()
	{
		return srcVersionRegExpValidator;
	}

	/**
	 * The information describing the files that an end user must upload into the system to allow the execution of a particular converter.
	 *
	 * This is used during SOURCE UPLOAD
	 *
	 * @return the upload file info
	 */
	public List<UploadFileInfo> getUploadFileInfo() {
		return this.uploadFileInfo;
	}
	
	/**
	 * Find the converter type that would be used to process the specified source artifact
	 * @param srcArtifactId that artifactId of a sdo source file
	 * @return the type, or null
	 */
	public static SupportedConverterTypes findBySrcArtifactId(String srcArtifactId) {
		return findConverterTypeAndExtensionBySrcArtifactId(srcArtifactId).getKey();
	}

	/**
	 * @param srcArtifactId that artifactId of a sdo source file
	 * @return a pair, where the key, is the type of the converter that supports it, and the value 
	 * is the extension name that should be used to replace wildcard '*-extension' portion when building
	 * an IBDF file by processing this source artifact id. 
	 */
	public static Pair<SupportedConverterTypes, String> findConverterTypeAndExtensionBySrcArtifactId(String srcArtifactId)
	{
		for (SupportedConverterTypes sct : SupportedConverterTypes.values()) {
			if (sct.getArtifactId().equals(srcArtifactId)) {
				return new Pair<>(sct, "");
			}
			else if (sct.getArtifactId().contains("*")) {
				String[] parts = sct.getArtifactId().split("\\*");
				if (srcArtifactId.startsWith(parts[0]) && (parts.length == 1 || srcArtifactId.endsWith(parts[1]))) {
					return new Pair<>(sct, srcArtifactId.substring(parts[0].length(), srcArtifactId.length()));
				}
			}
		}
		return null;
	}
	
	public static SupportedConverterTypes findByIBDFArtifactId(String ibdfArtifactId) {
		for (SupportedConverterTypes sct : SupportedConverterTypes.values()) {
			if (sct.getConverterOutputArtifactId().equals(ibdfArtifactId)) {
				return sct;
			}
			else if (sct.getArtifactId().contains("*")) {
				 String[] parts = sct.getArtifactId().split("\\*");
				 if (ibdfArtifactId.startsWith(sct.getConverterOutputArtifactId()) && ibdfArtifactId.endsWith(parts[1])) {
					 return sct;
				 }
			 }
		}
		return null;
	}

	/**
	 * @param mojoName
	 * @return the matching enum, or null
	 */
	public static SupportedConverterTypes findByMojoName(String mojoName)
	{
		for (SupportedConverterTypes sct : SupportedConverterTypes.values()) {
				if (sct.getConverterMojoName().equals(mojoName)) {
					return sct;
				}
			}
			return null;
	}
}
