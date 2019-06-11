/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
package sh.isaac.convert.mojo.rxnorm;

import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugins.annotations.Parameter;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.util.RecursiveDelete;
import sh.isaac.api.util.UuidT3Generator;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.convert.directUtils.DirectConverter;
import sh.isaac.convert.directUtils.DirectConverterBaseMojo;
import sh.isaac.convert.directUtils.DirectWriteHelper;
import sh.isaac.convert.mojo.rxnorm.rrf.RXNCONSO;
import sh.isaac.convert.mojo.rxnorm.rrf.RXNSAT;
import sh.isaac.converters.sharedUtils.sql.TableDefinition;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.converters.sharedUtils.umlsUtils.AbbreviationExpansion;
import sh.isaac.converters.sharedUtils.umlsUtils.RRFDatabaseHandle;
import sh.isaac.converters.sharedUtils.umlsUtils.Relationship;
import sh.isaac.converters.sharedUtils.umlsUtils.UMLSFileReader;
import sh.isaac.converters.sharedUtils.umlsUtils.rrf.REL;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.model.semantic.types.DynamicStringImpl;
import sh.isaac.model.semantic.types.DynamicUUIDImpl;
import sh.isaac.pombuilder.converter.ConverterOptionParam;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;

/**
 * {@link RxNormImportHK2Direct}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@PerLookup
@Service
public class RxNormImportHK2Direct extends DirectConverterBaseMojo implements DirectConverter
{
	private final String cpcRefsetConceptKey = "Current Prescribable Content";
	private final String sourceMetadata = "Source Vocabulary Metadata";
	private final String sabs = "RxNorm Source Vocabularies";
	private final String allCUIConcepts = "All RxNorm CUI Concepts";
	private final String RRFMetadata = "RRF Attributes";
	private final String tablePrefix = "RXN";
	private final String sctSab = "SNOMEDCT_US";
	private final String RXNORM_TERMINOLOGY_NAME = "RxNorm";

	private final HashMap<String, Relationship> nameToRel = new HashMap<>();

	private final HashMap<String, UUID> semanticTypes = new HashMap<>();
	private final HashSet<UUID> loadedRels = new HashSet<>();
	private final HashSet<UUID> skippedRels = new HashSet<>();

	private final HashSet<String> mapToIsa = new HashSet<>();
	// FULLY_QUALIFIED_NAME, true or false - true for rel only, false for a rel and association representation
	private final HashMap<Long, UUID> sctIdToUUID = new HashMap<>(); // A map of real (found) SCTIDs to their concept UUID
	private final HashMap<String, Long> cuiToSCTID = new HashMap<>(); // Map CUI to SCTID for the real sctIds to UUIDs found above
	private final AtomicInteger skippedRelForNotMatchingCUIFilter = new AtomicInteger();

	// Format to parse 01/28/2010
	private final SimpleDateFormat dateParse = new SimpleDateFormat("MM/dd/yyyy");

	private HashMap<String, UUID> sTypes;
	private HashMap<String, UUID> suppress;
	private HashMap<String, UUID> sourceRestrictionLevels;
	private RRFDatabaseHandle db;
	private HashMap<String, AbbreviationExpansion> abbreviationExpansions;
	private UUID allCUIRefsetConcept;
	private UUID cpcRefsetConcept;

	private PreparedStatement semanticTypeStatement, descSat, cuiRelStatementForward, cuiRelStatementBackward, satRelStatement, hasTTYType;
	private HashSet<String> allowedCUIsForSABs;
	private boolean linkSnomedCT;
	private long defaultTime;
	private boolean initialConfigRun = false;

	/**
	 * An optional list of TTY types which should be included. If left blank, we create concepts from all CUI's that are in the SAB RxNorm. If
	 * provided, we only create concepts where the RxCUI has an entry with a TTY that matches one of the TTY's provided here
	 */
	@Parameter(required = false)
	protected List<String> ttyRestriction;

	/**
	 * An optional list of SABs which should be included. We always include the SAB RXNORM. Use this parameter to specify others to include. If
	 * SNOMEDCT_US is included, then a snomed CT ibdf file must be present - snomed CT is not loaded from RxNorm, but rather, linked to the
	 * provided SCT IBDF file.
	 */
	@Parameter(required = false)
	protected List<String> sabsToInclude;

	/**
	 * This constructor is for maven and HK2 and should not be used at runtime. You should
	 * get your reference of this class from HK2, and then call the {@link #configure(File, Path, String, StampCoordinate)} method on it.
	 */
	protected RxNormImportHK2Direct()
	{
		//for HK2
	}

	@Override
	public ConverterOptionParam[] getConverterOptions()
	{
		return new RxNormConfigOptions().getConfigOptions();
	}

	@Override
	public void setConverterOption(String internalName, String... values)
	{
		if (internalName.equals(getConverterOptions()[0].getInternalName()))
		{
			//0 is ttyRestriction
			ttyRestriction = Arrays.asList(values);
		}
		else if (internalName.equals(getConverterOptions()[1].getInternalName()))
		{
			//1 is sabsToInclude
			sabsToInclude = Arrays.asList(values);
		}
		else
		{
			throw new RuntimeException("Unsupported converter option " + internalName);
		}
	}

	/**
	 * If this was constructed via HK2, then you must call the configure method prior to calling {@link #convertContent()}
	 * If this was constructed via the constructor that takes parameters, you do not need to call this.
	 * 
	 * @see sh.isaac.convert.directUtils.DirectConverter#configure(java.io.File, java.io.File, java.lang.String,
	 *      sh.isaac.api.coordinate.StampCoordinate)
	 */
	@Override
	public void configure(File outputDirectory, Path inputFolder, String converterSourceArtifactVersion, StampCoordinate stampCoordinate)
	{
		this.outputDirectory = outputDirectory;
		this.inputFileLocationPath = inputFolder;
		this.converterSourceArtifactVersion = converterSourceArtifactVersion;
		this.converterUUID = new ConverterUUID(UuidT5Generator.PATH_ID_FROM_FS_DESC, false);
		this.readbackCoordinate = stampCoordinate == null ? StampCoordinates.getDevelopmentLatest() : stampCoordinate;
		
		if (this.outputDirectory == null)
		{
			try
			{
				this.outputDirectory = Files.createTempDirectory("rxnormLoadTemp").toFile();
				log.info("Using temp space in {}", this.outputDirectory.getAbsolutePath());
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		//You can set this on low memory systems, if necessary to reduce the footprint.  
		//Also, play with the SABs and tty types in the ibdf pom config.
		//this.converterUUID.setUUIDMapState(false);
	}

	@Override
	public SupportedConverterTypes[] getSupportedTypes()
	{
		return new SupportedConverterTypes[] { SupportedConverterTypes.RXNORM };
	}

	@Override
	protected Path[] getIBDFFilesToPreload() throws IOException
	{
		initialConfig();
		//There currently is no mechanism to automatically pre-load a required IBDF file if you are running the  converter live.
		//for now, if you are running live, you just have to manually load the reqs first

		// We need to make queries of an SCT DB for part of this load, pull them in, pre-load.
		final AtomicReference<Path> ibdfFile = new AtomicReference<>();
		Files.walk(inputFileLocationPath.resolve("ibdf"), new FileVisitOption[] {}).forEach(path -> {
			if (linkSnomedCT && path.toString().toLowerCase().endsWith(".ibdf"))
			{
				if (ibdfFile.get() != null)
				{
					throw new RuntimeException("Only expected to find one ibdf file in the folder " + inputFileLocationPath.resolve("ibdf").normalize());
				}
				ibdfFile.set(path);
			}
		});

		if (linkSnomedCT && ibdfFile.get() == null)
		{
			throw new IOException("Failed to locate the ibdf file in " + inputFileLocationPath.resolve("ibdf"));
		}
		return ibdfFile.get() != null ? new Path[] { ibdfFile.get() } : new Path[0];
	}

	@Override
	protected Collection<VersionType> getIBDFSkipTypes()
	{
		return Arrays.asList(new VersionType[] { VersionType.DESCRIPTION, VersionType.COMPONENT_NID, VersionType.LOGIC_GRAPH });
	}

	@Override
	protected boolean IBDFPreloadActiveOnly()
	{
		return false;
	}
	
	private void initialConfig()
	{
		if (initialConfigRun)
		{
			return;
		}
		// Cleanup the sabsToInclude list - do this before anything else, to linkSnomedCT gets set prior to the IBDF preload
		final HashSet<String> temp = new HashSet<>();

		if (this.sabsToInclude != null)
		{
			this.sabsToInclude.forEach((s) -> {
				temp.add(s.toUpperCase());
				log.info("sabsToInclude: " + s.toUpperCase());
			});
		}

		temp.add("RXNORM");

		if (temp.contains(this.sctSab))
		{
			this.linkSnomedCT = true;
			temp.remove(this.sctSab);
		}
		else
		{
			this.linkSnomedCT = false;
		}

		this.sabsToInclude = new ArrayList<>();
		this.sabsToInclude.addAll(temp);
		initialConfigRun = true;
	}

	/**
	 * @see sh.isaac.convert.directUtils.DirectConverterBaseMojo#convertContent(Consumer, BiConsumer))
	 * @see DirectConverter#convertContent(Consumer, BiConsumer))
	 */
	@Override
	public void convertContent(Consumer<String> statusUpdates, BiConsumer<Double, Double> progressUpdate) throws IOException
	{
		try
		{
			Get.identifierService().optimizeForOutOfOrderLoading();
			initialConfig();
			init();
			this.semanticTypeStatement = this.db.getConnection().prepareStatement("select TUI, ATUI, CVF from RXNSTY where RXCUI = ?");

			// we always grab the description type NDC if present, even if NDC doesn't come from a SAB we are including.
			this.descSat = this.db.getConnection()
					.prepareStatement("select * from RXNSAT where RXCUI = ? and RXAUI = ? and (" + createSabQueryPart("", false) + " or ATN='NDC')");

			// UMLS and RXNORM do different things with rels - UMLS never has null CUI's, while RxNorm always has null CUI's (when AUI is specified)
			// Also need to join back to MRCONSO to make sure that the target concept is one that we will load with the SAB filter in place.
			this.cuiRelStatementForward = this.db.getConnection()
					.prepareStatement("SELECT distinct r.RXCUI1, r.RXAUI1, r.STYPE1, r.REL, r.RXCUI2, r.RXAUI2, r.STYPE2, "
							+ "r.RELA, r.RUI, r.SRUI, r.SAB, r.SL, r.DIR, r.RG, r.SUPPRESS, r.CVF from RXNREL as r, RXNCONSO "
							+ "WHERE RXCUI2 = ? and RXAUI2 is null and " + createSabQueryPart("r.", this.linkSnomedCT) + " and r.RXCUI1 = RXNCONSO.RXCUI and "
							+ createSabQueryPart("RXNCONSO.", this.linkSnomedCT));
			this.cuiRelStatementBackward = this.db.getConnection()
					.prepareStatement("SELECT distinct r.RXCUI1, r.RXAUI1, r.STYPE1, r.REL, r.RXCUI2, r.RXAUI2, r.STYPE2, "
							+ "r.RELA, r.RUI, r.SRUI, r.SAB, r.SL, r.DIR, r.RG, r.SUPPRESS, r.CVF from RXNREL as r, RXNCONSO "
							+ "WHERE RXCUI1 = ? and RXAUI1 is null and " + createSabQueryPart("r.", this.linkSnomedCT) + " and r.RXCUI2 = RXNCONSO.RXCUI and "
							+ createSabQueryPart("RXNCONSO.", this.linkSnomedCT));

			int cuiCounter = 0;
			final HashSet<String> skippedCUIForNotMatchingCUIFilter;
			final ArrayList<RXNCONSO> conceptData;

			try (Statement statement = this.db.getConnection().createStatement())
			{
				StringBuilder ttyRestrictionQuery = new StringBuilder();
				if (ttyRestriction != null && ttyRestriction.size() > 0)
				{
					ttyRestrictionQuery.append(" and (");
					for (String s : ttyRestriction)
					{
						ttyRestrictionQuery.append("TTY = '");
						ttyRestrictionQuery.append(s);
						ttyRestrictionQuery.append("' or ");
						log.info("ttyRestriction: " + s);
					}
					ttyRestrictionQuery.setLength(ttyRestrictionQuery.length() - " or ".length());
					ttyRestrictionQuery.append(")");
				}

				this.allowedCUIsForSABs = new HashSet<>();

				try (ResultSet rs = statement
						.executeQuery("select RXCUI from RXNCONSO where " + createSabQueryPart("", this.linkSnomedCT) + " " + ttyRestrictionQuery);)
				{
					while (rs.next())
					{
						this.allowedCUIsForSABs.add(rs.getString("RXCUI"));
					}
				}

				try (ResultSet rs = statement.executeQuery("select RXCUI, LAT, RXAUI, SAUI, SCUI, SAB, TTY, CODE, STR, SUPPRESS, CVF from RXNCONSO " + "where "
						+ createSabQueryPart("", this.linkSnomedCT) + " order by RXCUI"))
				{
					skippedCUIForNotMatchingCUIFilter = new HashSet<>();
					conceptData = new ArrayList<>();

					while (rs.next())
					{
						final RXNCONSO current = new RXNCONSO(rs);

						if (!this.allowedCUIsForSABs.contains(current.rxcui))
						{
							skippedCUIForNotMatchingCUIFilter.add(current.rxcui);
							continue;
						}

						if ((conceptData.size() > 0) && !conceptData.get(0).rxcui.equals(current.rxcui))
						{
							processCUIRows(conceptData);

							if (cuiCounter % 100 == 0)
							{
								showProgress();
							}

							cuiCounter++;

							if (cuiCounter % 10000 == 0)
							{
								advanceProgressLine();
								log.info("Processed " + cuiCounter + " CUIs creating " + dwh.getLoadStats().getConceptCount() + " concepts");
							}

							conceptData.clear();
						}

						conceptData.add(current);
					}
					advanceProgressLine();
				}
			}

			// process last
			processCUIRows(conceptData);
			log.info("Processed " + cuiCounter + " CUIs creating " + dwh.getLoadStats().getConceptCount() + " concepts");
			log.info("Skipped " + skippedCUIForNotMatchingCUIFilter.size() + " concepts for not containing the desired TTY");
			log.info("Skipped " + this.skippedRelForNotMatchingCUIFilter + " relationships for linking to a concept we didn't include");
			this.semanticTypeStatement.close();
			this.descSat.close();
			this.cuiRelStatementForward.close();
			this.cuiRelStatementBackward.close();
			finish();
		}
		catch (Exception e)
		{
			throw new IOException(e);
		}
		finally
		{
			if (this.db != null)
			{
				try
				{
					this.db.shutdown();
				}
				catch (final SQLException e)
				{
					throw new RuntimeException(e);
				}
			}
		}
	}

	/**
	 * Adds the relationships.
	 *
	 * @param concept the concept
	 * @param relationships the relationships
	 * @return the array list
	 * @throws SQLException the SQL exception
	 * @throws PropertyVetoException the property veto exception
	 */
	private ArrayList<UUID> addRelationships(UUID concept, List<REL> relationships) throws SQLException, PropertyVetoException
	{
		final ArrayList<UUID> parents = new ArrayList<>();

		for (final REL relationship : relationships)
		{
			relationship.setSourceUUID(concept);

			if (relationship.getSourceAUI() == null)
			{
				if (this.cuiToSCTID.get(relationship.getTargetCUI()) != null)
				{
					if (this.cuiToSCTID.get(relationship.getSourceCUI()) != null)
					{
						// Both source and target are concepts we are linking from SCT. Don't load the rell.
						continue;
					}

					// map to existing target SCT concept
					relationship.setTargetUUID(this.sctIdToUUID.get(this.cuiToSCTID.get(relationship.getTargetCUI())));
				}
				else
				{
					// must be a concept we are creating
					relationship.setTargetUUID(createCUIConceptUUID(relationship.getTargetCUI(), true));
				}
			}
			else
			{
				throw new RuntimeException("don't yet handle AUI associations");

				// relationship.setTargetUUID(createCuiSabCodeConceptUUID(relationship.getRxNormTargetCUI(),
				// relationship.getTargetSAB(), relationship.getTargetCode()));
			}

			// We currently don't check the properties on the (duplicate) inverse rels to make sure they are all present - we assume that they
			// created the inverse relationships as an exact copy of the primary rel direction. So, just checking the first rel from our dupe list is
			// good enough
			if (isRelPrimary(relationship.getRel(), relationship.getRela()))
			{
				// This can happen when the reverse of the rel equals the rel... sib/sib
				if (relCheckIsRelLoaded(relationship))
				{
					continue;
				}

				final UUID relTypeAsRel = dwh.getRelationshipType(((relationship.getRela() == null) ? relationship.getRel() : relationship.getRela()));
				final UUID relTypeAsAssn = dwh.getAssociationType(((relationship.getRela() == null) ? relationship.getRel() : relationship.getRela()));
				UUID r;

				if (relTypeAsRel != null)
				{
					parents.add(relationship.getTargetUUID());
					continue;
				}
				else if (relTypeAsAssn != null)
				{
					//not using the convenience method, because I need to pass in the rel UUID
					r = dwh.makeDynamicSemantic(relTypeAsAssn, concept, new DynamicData[] { new DynamicUUIDImpl(relationship.getTargetUUID()) },
							defaultTime, ((relationship.getRui() != null) ? converterUUID.createNamespaceUUIDFromString("RUI:" + relationship.getRui()) : null));
				}
				else
				{
					throw new RuntimeException("Unexpected rel handling");
				}

				// Add the annotations
				final HashSet<String> addedRUIs = new HashSet<>();

				if (StringUtils.isNotBlank(relationship.getRela())) // we already used rela - annotate with rel.
				{
					UUID genericType = (dwh.getAssociationType(relationship.getRel()) == null)
							? dwh.getRelationshipType(relationship.getRel())
							: dwh.getAssociationType(relationship.getRel());
					boolean reversed = false;

					if ((genericType == null) && relationship.getRela().equals("mapped_from"))
					{
						// This is to handle non-sensical data in UMLS... they have no consistency in the generic rel they assign - sometimes RB, sometimes RN.
						// reverse it - currently, only an issue on 'mapped_from' rels - as the code in Relationship.java has some exceptions for this type.
						genericType = (dwh.getAssociationType(reverseRel(relationship.getRel())) == null)
								? dwh.getRelationshipType(reverseRel(relationship.getRel()))
								: dwh.getAssociationType(reverseRel(relationship.getRel()));
						reversed = true;
					}

					dwh.makeDynamicSemantic(genericType, r, new DynamicUUIDImpl(dwh.getOtherType(RRFMetadata, 
							(reversed ? "Generic rel type (inverse)" : "Generic rel type"))), defaultTime);
				}

				if (StringUtils.isNotBlank(relationship.getRui()))
				{
					if (!addedRUIs.contains(relationship.getRui()))
					{
						this.dwh.makeStringAnnotation(dwh.getOtherType(RRFMetadata, "RUI"), r, relationship.getRui(), defaultTime);
						addedRUIs.add(relationship.getRui());
						this.satRelStatement.clearParameters();
						this.satRelStatement.setString(1, relationship.getRui());

						final ArrayList<RXNSAT> satData;

						try (ResultSet nestedRels = this.satRelStatement.executeQuery())
						{
							satData = new ArrayList<>();

							while (nestedRels.next())
							{
								satData.add(new RXNSAT(nestedRels));
							}
						}

						processSAT(r, satData, null, relationship.getSab(), null, defaultTime);
					}
				}

				if (StringUtils.isNotBlank(relationship.getRg()))
				{
					this.dwh.makeStringAnnotation(dwh.getOtherType(RRFMetadata, "RG"), r, relationship.getRg(), defaultTime);
				}

				if (StringUtils.isNotBlank(relationship.getDir()))
				{
					this.dwh.makeStringAnnotation(dwh.getOtherType(RRFMetadata, "DIR"), r, relationship.getDir(), defaultTime);
				}

				if (StringUtils.isNotBlank(relationship.getSuppress()))
				{
					dwh.makeDynamicSemantic(dwh.getOtherType(RRFMetadata, "SUPPRESS"), r, 
							new DynamicUUIDImpl(this.suppress.get(relationship.getSuppress())), defaultTime);
				}

				if (StringUtils.isNotBlank(relationship.getCvf()))
				{
					if (relationship.getCvf().equals("4096"))
					{
						dwh.makeDynamicRefsetMember(cpcRefsetConcept, r, defaultTime);
					}
					else
					{
						throw new RuntimeException("Unexpected value in RXNSAT cvf column '" + relationship.getCvf() + "'");
					}
				}

				relCheckLoadedRel(relationship);
			}
			else
			{
				if (this.cuiToSCTID.containsKey(relationship.getSourceCUI()))
				{
					// this is telling us there was a relationship from an SCT concept, to a RXNorm concept, but because we are
					// not processing sct concept CUIs, we will never process this one in the forward direction.
					// For now, don't put it in the skip list.
					// Perhaps, in the future, we create a stub SCT concept, and create this association to the RxNorm concept
					// but not now.
				}
				else
				{
					relCheckSkippedRel(relationship);
				}
			}
		}

		return parents;
	}

	/**
	 * Check relationships.
	 */
	private void checkRelationships()
	{
		// if the inverse relationships all worked properly, skipped should be empty when loaded is subtracted from it.
		this.loadedRels.forEach((uuid) -> {
			this.skippedRels.remove(uuid);
		});

		if (this.skippedRels.size() > 0)
		{
			//TODO need to fix whatever bug is going on here
			log.error("Relationship design error - " + this.skippedRels.size() + " were skipped that should have been loaded");
		}
		else
		{
			log.info("Yea! - no missing relationships!");
		}
	}

	/**
	 * Creates the CUI concept UUID.
	 *
	 * @param cui the cui
	 * @return the uuid
	 */
	private UUID createCUIConceptUUID(String cui, boolean assignNid)
	{
		UUID temp = converterUUID.createNamespaceUUIDFromString("CUI:" + cui, true);
		if (assignNid)
		{
			Get.identifierService().assignNid(temp);
		}
		return temp;
	}

	/**
	 * Creates the sab query part.
	 *
	 * @param tablePrefix the table prefix
	 * @param includeSCT the include SCT
	 * @return the string
	 */
	private String createSabQueryPart(String tablePrefix, boolean includeSCT)
	{
		final StringBuffer sb = new StringBuffer();

		sb.append("(");
		this.sabsToInclude.forEach((s) -> {
			sb.append(tablePrefix).append("SAB='").append(s).append("' OR ");
		});

		if (includeSCT)
		{
			sb.append(tablePrefix).append("SAB='" + this.sctSab + "' OR ");
		}

		sb.setLength(sb.length() - 4);
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Finish.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws SQLException the SQL exception
	 */
	private void finish() throws IOException, SQLException
	{
		checkRelationships();
		this.satRelStatement.close();
		this.hasTTYType.close();

		dwh.processTaxonomyUpdates();
		Get.taxonomyService().notifyTaxonomyListenersToRefresh();

		log.info("Load Statistics");

		for (String line : dwh.getLoadStats().getSummary())
		{
			log.info(line);
		}

		// this could be removed from final release. Just added to help debug editor problems.
		if (outputDirectory != null)
		{
			if (outputDirectory.getName().toString().startsWith("rxnormLoadTemp"))
			{
				//temp folder we created during a runtime import.  delete.
				log.info("Cleaning up temp folder {}", this.outputDirectory);
				RecursiveDelete.delete(outputDirectory);
			}
			else
			{
				log.info("Dumping UUID Debug File");
				converterUUID.dump(outputDirectory, "rxnormUuid");
			}
		}
		converterUUID.clearCache();
	}

	/**
	 * If sabList is null or empty, no sab filtering is done.
	 *
	 * @throws Exception the exception
	 */
	private void init() throws Exception
	{
		final String fileNameDatePortion = loadDatabase();
		final SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyy");
		defaultTime = sdf.parse(fileNameDatePortion).getTime();

		this.abbreviationExpansions = AbbreviationExpansion.load(getClass().getResourceAsStream("/RxNormAbbreviationsExpansions.txt"));
		this.mapToIsa.add("isa");
		this.mapToIsa.add("inverse_isa");

		// not translating this one to isa for now
		// mapToIsa.add("CHD");
		this.mapToIsa.add("tradename_of");
		this.mapToIsa.add("has_tradename");

		//Right now, we are configured for the CPT grouping modules nid
		dwh = new DirectWriteHelper(TermAux.USER.getNid(), MetaData.RXNORM_MODULES____SOLOR.getNid(), MetaData.DEVELOPMENT_PATH____SOLOR.getNid(),
				converterUUID, RXNORM_TERMINOLOGY_NAME, false);

		setupModule(RXNORM_TERMINOLOGY_NAME, MetaData.RXNORM_MODULES____SOLOR.getPrimordialUuid(), Optional.of("http://www.nlm.nih.gov/research/umls/rxnorm"), defaultTime);

		//Set up our metadata hierarchy
		dwh.makeMetadataHierarchy(true, true, false, true, true, true, defaultTime);

		loadMetaData();

		log.info("Metadata load stats");
		for (String line : dwh.getLoadStats().getSummary())
		{
			log.info(line);
		}

		dwh.clearLoadStats();
		log.info("Loading content");

		this.satRelStatement = this.db.getConnection().prepareStatement(
				"select * from " + this.tablePrefix + "SAT where RXAUI" + "= ? and STYPE='RUI' and " + createSabQueryPart("", this.linkSnomedCT));
		this.hasTTYType = this.db.getConnection()
				.prepareStatement("select count (*) as count from RXNCONSO where rxcui=? and TTY=? and " + createSabQueryPart("", this.linkSnomedCT));

		if (this.linkSnomedCT)
		{
			prepareSCTMaps();
		}
	}

	/**
	 * Returns the date portion of the file name - so from 'RxNorm_full_09022014.zip' it returns 09022014
	 *
	 * @return the string
	 * @throws Exception the exception
	 */
	private String loadDatabase() throws Exception
	{
		// Set up the DB for loading the temp data
		final AtomicReference<String> toReturn = new AtomicReference<String>();

		// Read the RRF file directly from the source zip file - need to find the zip first, to get the date out of the file name.
		final AtomicReference<Path> file = new AtomicReference<>();
		Files.walk(inputFileLocationPath, new FileVisitOption[] {}).forEach(path ->
		{
			if (path.getFileName() != null && path.getFileName().toString().toLowerCase().startsWith("rxnorm_full_") 
					&& path.getFileName().toString().toLowerCase().endsWith(".zip"))
			{
				if (file.get() != null)
				{
					throw new RuntimeException("Only expected to find one zip file in the folder " + inputFileLocationPath.normalize());
				}
				file.set(path);
				toReturn.set(path.getFileName().toString().substring("rxnorm_full_".length()));
				toReturn.set(toReturn.get().substring(0, toReturn.get().length() - 4));
			}
		});

		if (file.get() == null)
		{
			throw new IOException("Failed to locate the zip file in " + inputFileLocationPath);
		}

		this.db = new RRFDatabaseHandle();

		final File dbFile = new File(this.outputDirectory, "rrfDB.h2.db");
		final boolean createdNew = this.db.createOrOpenDatabase(new File(this.outputDirectory, "rrfDB"));

		if (!createdNew)
		{
			log.info("Using existing database.  To load from scratch, delete the file '" + dbFile.getCanonicalPath() + ".*'");
		}
		else
		{
			// RxNorm doesn't give us the UMLS tables that define the table definitions, so I put them into an XML file.
			final HashMap<String, TableDefinition> tables = this.db
					.loadTableDefinitionsFromXML(RxNormImportHK2Direct.class.getResourceAsStream("/rxnorm/RxNormTableDefinitions.xml"));

			ZipInputStream zis = new ZipInputStream(Files.newInputStream(file.get(), StandardOpenOption.READ));
			ZipEntry ze = zis.getNextEntry();
			
			while (ze != null && tables.size() > 0)
			{
				String tableName = ze.getName();
				if (tableName.lastIndexOf('/') > 0)
				{
					tableName = tableName.substring(tableName.lastIndexOf('/') + 1, tableName.length());
				}
				if (tableName.endsWith(".RRF"))
				{
					tableName = tableName.substring(0, tableName.length() - 4);
				}
				
				if (tables.containsKey(tableName))
				{
					UMLSFileReader umlsReader = new UMLSFileReader(new BufferedReader(new InputStreamReader(zis, "UTF-8")));
					this.db.loadDataIntoTable(tables.remove(tableName), umlsReader, (message) -> log.info(message));
				}
				ze = zis.getNextEntry();
			}
			
			zis.close();
			if (tables.size() > 0)
			{
				log.error("Missing" + Arrays.toString(tables.keySet().toArray(new String[0])));
				throw new RuntimeException("Failed to find data for " + tables.size() + " tables!");
			}

			try ( // Build some indexes to support the queries we will run
					Statement s = this.db.getConnection().createStatement())
			{
				log.info("Creating indexes");
				showProgress();
				s.execute("CREATE INDEX conso_rxcui_index ON RXNCONSO (RXCUI)");
				showProgress();
				s.execute("CREATE INDEX conso_rxaui_index ON RXNCONSO (RXAUI)");
				showProgress();
				s.execute("CREATE INDEX sat_rxcui_aui_index ON RXNSAT (RXCUI, RXAUI)");
				showProgress();
				s.execute("CREATE INDEX sat_aui_index ON RXNSAT (RXAUI)");
				showProgress();
				s.execute("CREATE INDEX sty_rxcui_index ON RXNSTY (RXCUI)");
				showProgress();
				s.execute("CREATE INDEX sty_tui_index ON RXNSTY (TUI)");
				showProgress();
				s.execute("CREATE INDEX rel_rxcui2_index ON RXNREL (RXCUI2, RXAUI2)");
				showProgress();
				s.execute("CREATE INDEX rel_rxaui2_index ON RXNREL (RXCUI1, RXAUI1)");
				showProgress();
				s.execute("CREATE INDEX rel_rela_rel_index ON RXNREL (RELA, REL)"); // helps with rel metadata
				showProgress();
				s.execute("CREATE INDEX rel_sab_index ON RXNREL (SAB)"); // helps with rel metadata
				advanceProgressLine();
			}

			log.info("DB Setup complete");
		}

		return toReturn.get();
	}

	/**
	 * Load meta data.
	 *
	 * @throws Exception the exception
	 */
	private void loadMetaData() throws Exception
	{
		cpcRefsetConcept = dwh.makeRefsetTypeConcept(null, cpcRefsetConceptKey, null, null, defaultTime);
		allCUIRefsetConcept = dwh.makeRefsetTypeConcept(null, allCUIConcepts, null, null, defaultTime);

		// from http://www.nlm.nih.gov/research/umls/rxnorm/docs/2013/rxnorm_doco_full_2013-2.html#s12_8
		dwh.makeOtherMetadataRootNode(sourceMetadata, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(sourceMetadata), null, "Versioned CUI", "VCUI", null, 
				"CUI of the versioned SRC concept for a source", 
				DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(sourceMetadata), null, "Root CUI", "RCUI", null, 
				"CUI of the root SRC concept for a source", 
				DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(sourceMetadata), null, "Versioned Source Abbreviation", "VSAB", null, 
				"The versioned source abbreviation_ for a source, e.g., NDDF_2004_11_03", 
				DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(sourceMetadata), null, "Root Source Abbreviation", "RSAB", null, 
				"The root source abbreviation_, for a source e.g. NDDF", 
				DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(sourceMetadata), null, "Source Official Name", "SON", null, "The official name for a source", 
				DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(sourceMetadata), null, "Source Family", "SF", null, "The Source Family for a source", 
				DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(sourceMetadata), null, "Source Version", "SVER", null, "The source version, e.g., 2001", 
				DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(sourceMetadata), null, "Meta Start Date", "VSTART", null, 
				"The date a source became active, e.g., 2001_04_03", 
				DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(sourceMetadata), null, "Meta End Date", "VEND", null, 
				"The date a source ceased to be active, e.g., 2001_05_10", 
				DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(sourceMetadata), null, "Meta Insert Version", "IMETA", null, 
				"The version of the Metathesaurus a source first appeared, e.g., 2001AB", 
				DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(sourceMetadata), null, "Meta Remove Version", "RMETA", null, 
				"The version of the Metathesaurus a source was removed, e.g., 2001AC", 
				DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(sourceMetadata), null, "Source License Contact", "SLC", null, 
				"The source license contact information. A semi-colon separated list containing the following fields: Name; Title; Organization; Address 1; Address 2; City; State or Province; Country; Zip or Postal Code; Telephone; Contact Fax; Email; URL", 
				DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(sourceMetadata), null, "Source Content Contact", "SCC", null, 
				"The source content contact information. A semi-colon separated list containing the following fields: Name; Title; Organization; Address 1; Address 2; City; State or Province; Country; Zip or Postal Code; Telephone; Contact Fax; Email; URL", 
				DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(sourceMetadata), null, "Source Restriction Level", "SRL", null, 
				"0,1,2,3,4 - explained in the License Agreement.", 
				DynamicDataType.UUID, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(sourceMetadata), null, "Term Frequency", "TFR", null, 
				"The number of terms for this source in RXNCONSO.RRF, e.g., 12343 (not implemented yet)", 
				DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(sourceMetadata), null, "CUI Frequency", "CFR", null, 
				"The number of CUIs associated with this source, e.g., 10234 (not implemented yet)", 
				DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(sourceMetadata), null, "Context Type", "CXTY", null, 
				"The type of relationship label (Section 2.4.2 of UMLS Reference Manual)", 
				DynamicDataType.UUID, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(sourceMetadata), null, "Term Type List", "TTYL", null, 
				"Term type list from source, e.g., MH,EN,PM,TQ", 
				DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(sourceMetadata), null, "Attribute Name List", "ATNL", null, 
				"The attribute name list, e.g., MUI,RN,TH,...", 
				DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(sourceMetadata), null, "Language", "LAT", null, 
				"The language of the terms in the source", 
				DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(sourceMetadata), null, "Character Encoding", "CENC", null, 
				"Character set as specified by the IANA official names for character assignments http://www.iana.org/assignments/character-sets", 
				DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(sourceMetadata), null, "Current Version", "CURVER", null, 
				"A Y or N flag indicating whether or not this row corresponds to the current version of the named source", 
				DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(sourceMetadata), null, "Source in Subset", "SABIN", null, 
				"A Y or N flag indicating whether or not this row is represented in the current MetamorphoSys subset. Initially always Y where CURVER is Y, but later is recomputed by MetamorphoSys.", 
				DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(sourceMetadata), null, "Source short name", "SSN", null, 
				"The short name of a source as used by the NLM Knowledge Source Server.", 
				DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(sourceMetadata), null, "Source citation", "SCIT", null, 
				"Citation information for a source. A semi-colon separated list containing the following fields: Author(s); Author(s) address; Author(s) organization; Editor(s); Title; Content Designator; Medium Designator; Edition; Place of Publication; Publisher; Date of Publication/copyright; Date of revision; Location; Extent; Series; Availability Statement (URL); Language; Notes", 
				DynamicDataType.STRING, null, defaultTime);

		dwh.makeOtherMetadataRootNode(RRFMetadata, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(RRFMetadata), null, "General Rel Type", null, null, null, 
				DynamicDataType.UUID, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(RRFMetadata), null, "Inverse General Rel Type", null, null, null,  
				DynamicDataType.UUID, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(RRFMetadata), null, "Snomed Code", null, null, null, 
				DynamicDataType.UUID, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(RRFMetadata), null, "Inverse Snomed Code", null, null, null, 
				DynamicDataType.UUID, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(RRFMetadata), null, "Source asserted atom identifier", "SAUI", null, null, 
				DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(RRFMetadata), null, "Source asserted concept identifier", "SCUI", null, null, 
				DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(RRFMetadata), null, "Source Vocabulary", "SAB", null, null, 
				DynamicDataType.UUID, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(RRFMetadata), null, "Suppress", "SUPPRESS", null, null, 
				DynamicDataType.UUID, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(RRFMetadata), null, "Term Type Class", "tty_class", null, 
				"The name of the column in RXNCONSO.RRF or RXNREL.RRF that contains the identifier to which the attribute is attached, e.g., CUI, AUI.", 
				DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(RRFMetadata), null, "STYPE", null, null, 
				"The name of the column in RXNCONSO.RRF or RXNREL.RRF that contains the identifier to which the attribute is attached, e.g., CUI, AUI.", 
				DynamicDataType.UUID, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(RRFMetadata), null, "STYPE1", null, null, 
				"The name of the column in RXNCONSO.RRF that contains the identifier used for the first concept or first atom in source of the relationship (e.g., 'AUI' or 'CUI')", 
				DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(RRFMetadata), null, "STYPE2", null, null, 
				"The name of the column in RXNCONSO.RRF that contains the identifier used for the second concept or second atom in the source of the relationship (e.g., 'AUI' or 'CUI')",
				DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(RRFMetadata), null, "Source Asserted Attribute Identifier", "SATUI", null, 
				"Source asserted attribute identifier (optional - present if it exists)", 
				DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(RRFMetadata), null, "Semantic Type tree number", "STN", null, null, 
				DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(RRFMetadata), null, "Semantic Type", "STY", null, null, 
				DynamicDataType.UUID, null, defaultTime);
		// note - this is undocumented in RxNorm - used on the STY table - description_ comes from UMLS
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(RRFMetadata), null, "Content View Flag", "CVF", null, 
				"Bit field used to flag rows included in Content View.", 
				DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(RRFMetadata), null, "URI", null, null, null, 
				DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(RRFMetadata), null, "RG", null, null, "Machine generated and unverified indicator", 
				DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(RRFMetadata), null, "Generic rel type", null, null, "Generic rel type for this relationship", 
				DynamicDataType.UUID, null, defaultTime);
		//Stupid hack to mark as ID
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(RRFMetadata), null, "RXAUI", "Unique identifier for atom", null, "(RxNorm Atom Id)", 
				DynamicDataType.UNKNOWN, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(RRFMetadata), null, "RXCUI", "RxNorm Concept ID", null, "RxNorm Unique identifier for concept", 
				DynamicDataType.UNKNOWN, null, defaultTime);
		
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(RRFMetadata), null, "TUI", "Unique identifier of Semantic Type", null, 
				"Machine generated and unverified indicator", DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(RRFMetadata), null, "RUI", "Unique identifier for Relationship", null, 
				"Machine generated and unverified indicator", DynamicDataType.STRING, null, defaultTime);
		dwh.makeOtherTypeConcept(dwh.getOtherMetadataRootType(RRFMetadata), null, "ATUI", "Unique identifier for attribute", null, 
				"Machine generated and unverified indicator", DynamicDataType.STRING, null, defaultTime);
		
		dwh.linkToExistingAttributeTypeConcept(MetaData.CODE____SOLOR, defaultTime, readbackCoordinate);

		// Attributes from MRDoc
		// dynamically add more attributes from *DOC
		{
			log.info("Creating attribute types");
			// extra logic at the end to keep NDC's from any sab when processing RXNorm
			try (Statement s = this.db.getConnection().createStatement(); // extra logic at the end to keep NDC's from any sab when processing RXNorm
					ResultSet rs = s.executeQuery(
							"SELECT VALUE, TYPE, EXPL from " + this.tablePrefix + "DOC where DOCKEY = 'ATN' and VALUE in (select distinct ATN from "
									+ this.tablePrefix + "SAT" + " where " + createSabQueryPart("", false) + " or ATN='NDC')"))
			{
				while (rs.next())
				{
					final String abbreviation = rs.getString("VALUE");
					final String type = rs.getString("TYPE");
					final String expansion = rs.getString("EXPL");

					if (!type.equals("expanded_form"))
					{
						throw new RuntimeException("Unexpected type in the attribute data within DOC: '" + type + "'");
					}

					String altName = null;
					String description = null;

					if (expansion.length() > 30)
					{
						description = expansion;
					}
					else
					{
						altName = expansion;
					}

					final AbbreviationExpansion ae = this.abbreviationExpansions.get(abbreviation);

					if (abbreviation.equals("VUID"))
					{
						dwh.linkToExistingAttributeTypeConcept(MetaData.VUID____SOLOR, defaultTime, readbackCoordinate);
					}
					else if (ae == null)
					{
						log.info("No Abbreviation Expansion found for " + abbreviation + " using FSN: " + abbreviation + "  Alt:" + altName + " description:"
								+ description);
						dwh.makeAttributeTypeConcept(converterUUID.createNamespaceUUIDFromStrings(abbreviation, "ATN"), 
								abbreviation, null, altName, description, false, DynamicDataType.STRING, null, defaultTime);
					}
					else
					{
						dwh.makeAttributeTypeConcept(converterUUID.createNamespaceUUIDFromStrings(ae.getExpansion(), "ATN"), 
								ae.getExpansion(), null, ae.getAbbreviation(), ae.getDescription(), false, DynamicDataType.STRING, null, defaultTime);
					}
				}
			}
		}

		// description types
		{
			log.info("Creating description_ types");
			dwh.makeDescriptionTypeConcept(null, "Inverse FQN", null, null, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, defaultTime);
			dwh.makeDescriptionTypeConcept(null, "Inverse Synonym", null, null, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, defaultTime);
			dwh.makeDescriptionTypeConcept(null, "Inverse Description", null, null, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), null, defaultTime);
			

			final PreparedStatement ps;

			try (Statement s = this.db.getConnection().createStatement())
			{
				ResultSet usedDescTypes;

				usedDescTypes = s.executeQuery("select distinct TTY from RXNCONSO WHERE " + createSabQueryPart("", false));
				ps = this.db.getConnection().prepareStatement("select TYPE, EXPL from " + this.tablePrefix + "DOC where DOCKEY='TTY' and VALUE=?");

				while (usedDescTypes.next())
				{
					final String tty = usedDescTypes.getString(1);

					ps.setString(1, tty);

					String expandedForm;
					final HashSet<String> classes;

					try (ResultSet descInfo = ps.executeQuery())
					{
						expandedForm = null;
						classes = new HashSet<>();

						while (descInfo.next())
						{
							final String type = descInfo.getString("TYPE");
							final String expl = descInfo.getString("EXPL");

							switch (type)
							{
								case "expanded_form":
									if (expandedForm != null)
									{
										throw new RuntimeException("Expected name to be null!");
									}

									expandedForm = expl;
									break;

								case "tty_class":
									classes.add(expl);
									break;

								default :
									throw new RuntimeException("Unexpected type in DOC for '" + tty + "'");
							}
						}
					}

					ps.clearParameters();

					UUID descType = null;
					final AbbreviationExpansion ae = this.abbreviationExpansions.get(tty);

					if (ae == null)
					{
						log.error("No Abbreviation Expansion found for " + tty + " using fsn: " + tty + " alt: " + expandedForm);
						descType = dwh.makeDescriptionTypeConcept(converterUUID.createNamespaceUUIDFromStrings(tty, "TTY"), 
								tty, expandedForm, null, getCoreType(tty, classes), null, defaultTime);  
					}
					else
					{
						descType = dwh.makeDescriptionTypeConcept(converterUUID.createNamespaceUUIDFromStrings(ae.getExpansion(), "TTY"), 
								ae.getExpansion(), ae.getAbbreviation(), null, getCoreType(ae.getExpansion(), classes), 
								null, defaultTime); 
						if (StringUtils.isNotBlank(ae.getDescription()))
						{
							dwh.makeDescriptionEnNoDialect(descType, ae.getDescription(), MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), 
									Status.ACTIVE, defaultTime);
						}
					}

					for (final String tty_class : classes)
					{
						dwh.makeStringAnnotation(dwh.getOtherType(RRFMetadata, "tty_class"), descType, tty_class, defaultTime);
					}
				}

				usedDescTypes.close();
			}

			ps.close();
		}
		loadRelationshipMetadata();

		// STYPE values
		this.sTypes = new HashMap<>();
		{
			log.info("Creating STYPE types");

			try (Statement s = this.db.getConnection().createStatement();
					ResultSet rs = s.executeQuery("SELECT DISTINCT VALUE, TYPE, EXPL FROM " + this.tablePrefix + "DOC where DOCKEY like 'STYPE%'"))
			{
				while (rs.next())
				{
					final String sType = rs.getString("VALUE");
					final String type = rs.getString("TYPE");
					final String name = rs.getString("EXPL");

					if (!type.equals("expanded_form"))
					{
						throw new RuntimeException("Unexpected type in the attribute data within DOC: '" + type + "'");
					}

					UUID sTypeUUID = dwh.getOtherType(RRFMetadata, "STYPE");
					
					final UUID c = dwh.makeConceptEnNoDialect(
							converterUUID.createNamespaceUUIDFromString(sTypeUUID + ":" + name), 
							name, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), 
							new UUID[] {sTypeUUID}, Status.ACTIVE, defaultTime);
					dwh.makeDescriptionEnNoDialect(c, sType, MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), Status.ACTIVE, defaultTime);

					this.sTypes.put(name, c);
					this.sTypes.put(sType, c);
				}
			}
		}
		this.suppress = xDocLoaderHelper("SUPPRESS", "Suppress", false, dwh.getOtherType(RRFMetadata, "SUPPRESS"));

		// Not yet loading co-occurrence data yet, so don't need these yet.
		// xDocLoaderHelper("COA", "Attributes of co-occurrence", false);
		// xDocLoaderHelper("COT", "Type of co-occurrence", true);
		final HashMap<String, UUID> contextTypes = xDocLoaderHelper("CXTY", "Context Type", false, 
				dwh.getOtherType(sourceMetadata, "CXTY")); 

		// not yet loading mappings - so don't need this yet
		// xDocLoaderHelper("FROMTYPE", "Mapping From Type", false);
		// xDocLoaderHelper("TOTYPE", "Mapping To Type", false);
		// MAPATN - not yet used in UMLS
		// Handle the languages
		// Not actually doing anythign with these at the moment, we just map to metadata languages.
		{
			try (Statement s = this.db.getConnection().createStatement();
					ResultSet rs = s.executeQuery("SELECT * from " + this.tablePrefix + "DOC where DOCKEY = 'LAT' and VALUE in (select distinct LAT from "
							+ this.tablePrefix + "CONSO where " + createSabQueryPart("", false) + ")"))
			{
				while (rs.next())
				{
					final String abbreviation = rs.getString("VALUE");
					final String type = rs.getString("TYPE");

					// String expansion = rs.getString("EXPL");
					if (!type.equals("expanded_form"))
					{
						throw new RuntimeException("Unexpected type in the language data within DOC: '" + type + "'");
					}

					if (abbreviation.equals("ENG") || abbreviation.equals("SPA"))
					{
						// use official ISAAC languages
						if (abbreviation.equals("ENG") || abbreviation.equals("SPA"))
						{
							// We can map these onto metadata types.
						}
						else
						{
							throw new RuntimeException("unsupported language");
						}
					}
				}
			}
		}

		// And Source Restriction Levels
		{
			log.info("Creating Source Restriction Level types");
			this.sourceRestrictionLevels = new HashMap<>();

			try (PreparedStatement ps = this.db.getConnection()
					.prepareStatement("SELECT VALUE, TYPE, EXPL from " + this.tablePrefix + "DOC where DOCKEY=? ORDER BY VALUE"))
			{
				ps.setString(1, "SRL");

				final ResultSet rs = ps.executeQuery();
				String value = null;
				String description = null;
				String uri = null;

				// Two entries per SRL, read two rows, create an entry.
				while (rs.next())
				{
					String type = rs.getString("TYPE");
					String expl = rs.getString("EXPL");

					switch (type)
					{
						case "expanded_form":
							description = expl;
							break;

						case "uri":
							uri = expl;
							break;

						default :
							throw new RuntimeException("oops");
					}

					if (value == null)
					{
						value = rs.getString("VALUE");
					}
					else
					{
						if (!value.equals(rs.getString("VALUE")))
						{
							throw new RuntimeException("oops");
						}

						if ((description == null) || (uri == null))
						{
							throw new RuntimeException("oops");
						}

						UUID SRLConcept = dwh.getOtherType(sourceMetadata, "SRL");
						
						final UUID c = dwh.makeConceptEnNoDialect(
								converterUUID.createNamespaceUUIDFromString(SRLConcept + ":" + value), 
								value, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), 
								new UUID[] {SRLConcept}, Status.ACTIVE, defaultTime);
						dwh.makeDescriptionEnNoDialect(c, description, MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), Status.ACTIVE, defaultTime);
						
						this.sourceRestrictionLevels.put(value, c);
						dwh.makeStringAnnotation(dwh.getOtherType(RRFMetadata, "URI"), c, uri, defaultTime);
						type = null;
						expl = null;
						value = null;
					}
				}

				rs.close();
			}
		}

		// And Source vocabularies
		final PreparedStatement getSABMetadata = this.db.getConnection()
				.prepareStatement("Select * from " + this.tablePrefix + "SAB where (VSAB = ? or (RSAB = ? and CURVER='Y' ))");

		{
			log.info("Creating Source Vocabulary types");
			final HashSet<String> sabList = new HashSet<>();
			sabList.addAll(this.sabsToInclude);

			Statement s = this.db.getConnection().createStatement();
			ResultSet rs = s.executeQuery("select distinct SAB from RXNSAT where ATN='NDC'");

			while (rs.next())
			{
				sabList.add(rs.getString("SAB"));
			}

			rs.close();
			s.close();
			
			UUID sabsNode = dwh.makeOtherMetadataRootNode(sabs, defaultTime);

			for (final String currentSab : sabList)
			{
				s = this.db.getConnection().createStatement();
				rs = s.executeQuery(
						"SELECT SON from " + this.tablePrefix + "SAB WHERE (VSAB='" + currentSab + "' or (RSAB='" + currentSab + "' and CURVER='Y'))");

				if (rs.next())
				{
					final String son = rs.getString("SON");
					UUID cr = dwh.makeOtherTypeConcept(sabsNode, null, son, currentSab, null, null, DynamicDataType.STRING, null, defaultTime);

					try
					{
						// lookup the other columns for the row with this newly added RSAB terminology
						getSABMetadata.setString(1, (son == null) ? currentSab : son);
						getSABMetadata.setString(2, (son == null) ? currentSab : son);

						try (ResultSet rs2 = getSABMetadata.executeQuery())
						{
							if (rs2.next())
							{
								for (final String metadataProperty : dwh.getOtherTypes(dwh.getOtherMetadataRootType(sourceMetadata)))
								{
									final String columnName = metadataProperty;
									final String columnValue = rs2.getString(columnName);

									if (columnValue == null)
									{
										continue;
									}

									switch (columnName)
									{
										case "SRL":
											dwh.makeDynamicSemantic(dwh.getOtherType(sourceMetadata, metadataProperty),
													cr, new DynamicUUIDImpl(this.sourceRestrictionLevels.get(columnValue)), defaultTime);
											break;

										case "CXTY":
											dwh.makeDynamicSemantic(dwh.getOtherType(sourceMetadata, "CXTY"),
													cr, new DynamicUUIDImpl(contextTypes.get(columnValue)), defaultTime);
											break;

										default :
											dwh.makeStringAnnotation(dwh.getOtherType(sourceMetadata, metadataProperty), 
													cr, columnValue, defaultTime);
											break;
									}
								}
							}

							if (rs2.next())
							{
								throw new RuntimeException("Too many sabs for '" + currentSab + "' - Perhaps you should be using versioned sabs!");
							}
						}
					}
					catch (final SQLException e)
					{
						throw new RuntimeException("Error loading *SAB", e);
					}
				}
				else
				{
					log.error("Too few SABs for '" + currentSab + "' - perhaps you need to use versioned SABs.");
				}

				if (rs.next())
				{
					throw new RuntimeException("Too many SABs for '" + currentSab + "' - perhaps you need to use versioned SABs.");
				}

				rs.close();
				s.close();
			}
			getSABMetadata.close();
		}

		// And semantic types
		{
			log.info("Creating semantic types");

			try (Statement s = this.db.getConnection().createStatement();
					ResultSet rs = s.executeQuery("SELECT distinct TUI, STN, STY from " + this.tablePrefix + "STY"))
			{
				while (rs.next())
				{
					final String tui = rs.getString("TUI");
					final String stn = rs.getString("STN");
					final String sty = rs.getString("STY");
					final UUID c = dwh.makeConceptEnNoDialect(converterUUID.createNamespaceUUIDFromString(
							dwh.getOtherType(RRFMetadata, "STY") + ":" + sty),
							sty, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), 
							new UUID[] {dwh.getOtherType(RRFMetadata, "STY")}, Status.ACTIVE, defaultTime);

					this.semanticTypes.put(tui, c);
					dwh.makeStringAnnotation(dwh.getOtherType(RRFMetadata, "TUI"), c, tui, defaultTime);
					dwh.makeStringAnnotation(dwh.getOtherType(RRFMetadata, "STN"), c, stn, defaultTime);
				}
			}
		}
	}

	/**
	 * Load relationship metadata.
	 *
	 * @throws Exception the exception
	 */
	private void loadRelationshipMetadata() throws Exception
	{
		log.info("Creating relationship types");

		// Both of these get added as extra attributes on the relationship definition
		final HashMap<String, ArrayList<String>> snomedCTRelaMappings = new HashMap<>(); // Maps something like 'has_specimen_source_morphology' to '118168003' (may be more than one
																						// target SCT code)
		final HashMap<String, String> snomedCTRelMappings = new HashMap<>(); // Maps something like '118168003' to 'RO'

		Statement s = this.db.getConnection().createStatement();

		// get the inverses of first, before the expanded forms
		ResultSet rs = s
				.executeQuery("SELECT DOCKEY, VALUE, TYPE, EXPL FROM " + this.tablePrefix + "DOC where DOCKEY ='REL' or DOCKEY = 'RELA' order by TYPE DESC ");

		while (rs.next())
		{
			final String dockey = rs.getString("DOCKEY");
			final String value = rs.getString("VALUE");
			final String type = rs.getString("TYPE");
			final String expl = rs.getString("EXPL");

			if (value == null)
			{
				continue; // don't need this one
			}

			switch (type)
			{
				case "snomedct_rela_mapping":
					ArrayList<String> targetSCTIDs = snomedCTRelaMappings.get(expl);

					if (targetSCTIDs == null)
					{
						targetSCTIDs = new ArrayList<>();
						snomedCTRelaMappings.put(expl, targetSCTIDs);
					}

					targetSCTIDs.add(value);
					break;

				case "snomedct_rel_mapping":
					snomedCTRelMappings.put(value, expl);
					break;

				default :
					Relationship rel = this.nameToRel.get(value);

					if (rel == null)
					{
						if (type.endsWith("_inverse"))
						{
							rel = this.nameToRel.get(expl);

							if (rel == null)
							{
								rel = new Relationship(dockey.equals("RELA"));
								this.nameToRel.put(value, rel);
								this.nameToRel.put(expl, rel);
							}
							else
							{
								throw new RuntimeException("shouldn't happen due to query order");
							}
						}
						else
						{
							// only cases where there is no inverse
							rel = new Relationship(dockey.equals("RELA"));
							this.nameToRel.put(value, rel);
						}
					}

					switch (type)
					{
						case "expanded_form":
							rel.addDescription(value, expl);
							break;

						case "rela_inverse":
						case "rel_inverse":
							rel.addRelInverse(value, expl);
							break;

						default :
							throw new RuntimeException("Oops");
					}

					break;
			}
		}

		rs.close();
		s.close();

		final HashSet<String> actuallyUsedRelsOrRelas = new HashSet<>();

		for (final Entry<String, ArrayList<String>> x : snomedCTRelaMappings.entrySet())
		{
			if (!this.nameToRel.containsKey(x.getKey()))
			{
				// metamorphosys doesn't seem to remove these when the sct rel types aren't included - just silently remove them
				// unless it seems that they should map.
				// may_be_a appears to be a bug in RxNorm 2013-12-02. silently ignore...
				// TODO see if they fix it in the future, make this check version specific?
				// seems to be getting worse... now it fails to remove 'has_life_circumstance' too in 2014AA, and a few others.
				// Changing to a warning.
				log.error("Warning - The 'snomedct_rela_mapping' '" + x.getKey() + "' does not have a corresponding REL entry!  Skipping");

				// if (!x.getKey().equals("may_be_a") && !x.getKey().equals("has_life_circumstance"))
				// {
				// throw new RuntimeException("ERROR - No rel for " + x.getKey() + ".");
				// }
				x.getValue().forEach((sctId) -> {
					snomedCTRelMappings.remove(sctId);
				});
			}
			else
			{
				x.getValue().stream().map((sctid) -> {
					this.nameToRel.get(x.getKey()).addSnomedCode(x.getKey(), sctid);
					return sctid;
				}).map((sctid) -> snomedCTRelMappings.remove(sctid)).filter((relType) -> (relType != null)).map((relType) -> {
					this.nameToRel.get(x.getKey()).addRelType(x.getKey(), relType);
					return relType;
				}).forEachOrdered((relType) -> {
					// Shouldn't need this, but there are some cases where the metadata is inconsistent - with how it is actually used.
					actuallyUsedRelsOrRelas.add(relType);
				});
			}
		}

		if (snomedCTRelMappings.size() > 0)
		{
			snomedCTRelMappings.entrySet().forEach((x) -> {
				log.error(x.getKey() + ":" + x.getValue());
			});
			throw new RuntimeException("oops - still have (things listed above)");
		}

		s = this.db.getConnection().createStatement();
		rs = s.executeQuery("select distinct REL, RELA from " + this.tablePrefix + "REL where " + createSabQueryPart("", this.linkSnomedCT));

		while (rs.next())
		{
			actuallyUsedRelsOrRelas.add(rs.getString("REL"));

			if (rs.getString("RELA") != null)
			{
				actuallyUsedRelsOrRelas.add(rs.getString("RELA"));
			}
		}

		rs.close();
		s.close();

		final HashSet<Relationship> uniqueRels = new HashSet<>(this.nameToRel.values());

		// Sort the generic relationships first, these are needed when processing primary
		final ArrayList<Relationship> sortedRels = new ArrayList<>(uniqueRels);

		Collections.sort(sortedRels, (o1, o2) -> {
			if (o1.getIsRela() && !o2.getIsRela())
			{
				return 1;
			}

			if (o2.getIsRela() && !o1.getIsRela())
			{
				return -1;
			}

			return 0;
		});

		for (final Relationship r : sortedRels)
		{
			r.setSwap(this.db.getConnection(), this.tablePrefix);

			if (!actuallyUsedRelsOrRelas.contains(r.getFQName()) && !actuallyUsedRelsOrRelas.contains(r.getInverseFQName()))
			{
				continue;
			}

			final boolean associationAsRel = this.mapToIsa.contains(r.getFQName());
			UUID p = dwh.makeAssociationTypeConcept(null, ((r.getAltName() == null) ? r.getFQName() : r.getAltName()),
					((r.getAltName() == null) ? null : r.getFQName()), null, r.getDescription(),
					((r.getInverseAltName() == null) ? r.getInverseFQName() : r.getInverseAltName()), null, null,
					associationAsRel ? Arrays.asList(
							new UUID[] { dwh.getRelationTypesNode().get(), MetaData.RELATIONSHIP_TYPE_IN_SOURCE_TERMINOLOGY____SOLOR.getPrimordialUuid() })
							: null,
					defaultTime);

			// associations already handle inverse names
			if (r.getInverseAltName() != null)
			{
				dwh.makeDescriptionEnNoDialect(p, r.getInverseFQName(), dwh.getDescriptionType("Inverse FQN"), Status.ACTIVE, defaultTime);
			}

			if (r.getInverseFQName() != null)
			{
				dwh.makeDescriptionEnNoDialect(p, r.getInverseFQName(), dwh.getDescriptionType("Inverse Synonym"), Status.ACTIVE, defaultTime);
			}

			if (r.getInverseDescription() != null)
			{
				dwh.makeDescriptionEnNoDialect(p, r.getInverseDescription(), dwh.getDescriptionType("Inverse Description"), Status.ACTIVE, defaultTime);
			}

			if (r.getRelType() != null)
			{
				final Relationship generalRel = this.nameToRel.get(r.getRelType());

				dwh.makeDynamicSemantic(dwh.getOtherType(RRFMetadata, "General Rel Type"), p, new DynamicUUIDImpl(dwh.getAssociationType(generalRel.getFQName())),
						defaultTime);
			}

			if (r.getInverseRelType() != null)
			{
				final Relationship generalRel = this.nameToRel.get(r.getInverseRelType());

				dwh.makeDynamicSemantic(dwh.getOtherType(RRFMetadata, "Inverse General Rel Type"), p,
						new DynamicUUIDImpl(dwh.getAssociationType(generalRel.getFQName())), defaultTime);
			}

			r.getRelSnomedCode().forEach((sctCode) -> {
				dwh.makeDynamicSemantic(dwh.getOtherType(RRFMetadata, "Snomed Code"), p, new DynamicUUIDImpl(UuidT3Generator.fromSNOMED(sctCode)), defaultTime);
			});
			r.getInverseRelSnomedCode().forEach((sctCode) -> {
				dwh.makeDynamicSemantic(dwh.getOtherType(RRFMetadata, "Inverse Snomed Code"), p, new DynamicUUIDImpl(UuidT3Generator.fromSNOMED(sctCode)),
						defaultTime);
			});
		}
	}

	/**
	 * Make description type.
	 *
	 * @param fqName the fully qualified name
	 * @param altName the alt name
	 * @param description the description
	 * @param tty_classes the tty classes
	 * @return the property
	 */
	private UUID getCoreType(String name, final Set<String> tty_classes)
	{
		// The current possible classes are:
		// preferred
		// obsolete
		// entry_term
		// hierarchical
		// synonym
		// attribute
		// abbreviation
		// expanded
		// other

		if (name.equals("FN"))
		{
			return MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid();
		}
		else
		{
			return MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid();
		}
		//TODO are their definitions?
		//TODO someday, we may re-introduce a ranking mechanism for these description types, which can be used at runtime.  Look back in history 
		//at the old loader, to see how we used to rank these.
	}

	/**
	 * Prepare SCT maps.
	 *
	 * @throws SQLException the SQL exception
	 */
	private void prepareSCTMaps() throws SQLException
	{
		Get.assemblageService().getSemanticNidsFromAssemblage(MetaData.SCTID____SOLOR.getNid()).stream().forEach(semantic -> {
			final LatestVersion<StringVersion> lv = ((SemanticChronology) Get.assemblageService().getSemanticChronology(semantic))
					.getLatestVersion(StampCoordinates.getDevelopmentLatest());
			final StringVersion ss = lv.get();
			final Long sctId = Long.parseLong(ss.getString());
			final UUID conceptUUID = Get.identifierService().getUuidPrimordialForNid(ss.getReferencedComponentNid());

			this.sctIdToUUID.put(sctId, conceptUUID);
		});
		log.info("Read SCTID -> UUID mappings for " + this.sctIdToUUID.size() + " items");

		try (ResultSet rs = this.db.getConnection().createStatement().executeQuery("SELECT DISTINCT RXCUI, CODE from RXNCONSO where SAB='" + this.sctSab + "'"))
		{
			while (rs.next())
			{
				final String cui = rs.getString("RXCUI");
				final long sctid = Long.parseLong(rs.getString("CODE"));

				if (this.sctIdToUUID.containsKey(sctid))
				{
					this.cuiToSCTID.put(cui, sctid);
				}
			}
		}

		log.info("Read CUI -> SCTID mappings for " + this.cuiToSCTID.size() + " items");
	}

	/**
	 * Process CUI rows.
	 *
	 * @param conceptData the concept data
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws SQLException the SQL exception
	 * @throws PropertyVetoException the property veto exception
	 */
	private void processCUIRows(ArrayList<RXNCONSO> conceptData) throws IOException, SQLException, PropertyVetoException
	{
		final String rxCui = conceptData.get(0).rxcui;
		final HashSet<String> uniqueTTYs = new HashSet<>();
		final HashSet<String> uniqueSABs = new HashSet<>();
		
		// Activate the concept if any description is active
		Status conceptState = Status.INACTIVE;
		long conceptTime = Long.MAX_VALUE;

		//ensure all the same CUI, gather the TTYs involved
		for (RXNCONSO row : conceptData)
		{
			try
			{
				uniqueTTYs.add(row.tty);
				uniqueSABs.add(row.sab);
				if (!row.rxcui.equals(rxCui))
				{
					throw new RuntimeException("Oops");
				}
				
				//Find the time and status on the description to use for the concept
				if (row.sab.equals(this.sctSab))
				{
					continue;
				}

				// Add attributes from SAT table
				this.descSat.clearParameters();
				this.descSat.setString(1, rxCui);
				this.descSat.setString(2, row.rxaui);

				try (ResultSet rs = this.descSat.executeQuery())
				{
					boolean descriptionActive = true;
					while (rs.next())
					{
						final RXNSAT current = new RXNSAT(rs);
						if ("RXN_OBSOLETED".equals(current.atn))
						{
							descriptionActive = false;
						}
						if ("RXN_ACTIVATED".equals(current.atn))
						{
							try
							{
								final long time = this.dateParse.parse(current.atv).getTime();
								if (time < conceptTime)
								{
									conceptTime = time;
								}
							}
							catch (final ParseException e)
							{
								throw new RuntimeException("Can't parse date?");
							}
						}
					}
					if (descriptionActive)
					{
						conceptState = Status.ACTIVE;
					}
				}
			}
			catch (RuntimeException e)
			{
				log.error("Failed gathering ttys for RXNCONSO row: " + row);
				throw e;
			}
		}

		UUID cuiConcept;
		boolean isSCT = false;

		if ((uniqueSABs.size() == 1) && uniqueSABs.iterator().next().equals(this.sctSab))
		{
			// This is a SCT only concept - we don't want to create it. But we might need to put some relationships or associations here.
			final String sctId = conceptData.get(0).code;

			if (sctId == null)
			{
				throw new RuntimeException("Unexpected");
			}

			isSCT = true;
			Long temp = Long.parseLong(sctId);
			if (sctIdToUUID.get(temp) == null)
			{
				throw new RuntimeException("Unexpected failure mapping SCTID " + sctId + " to a UUID");
			}
			cuiConcept = sctIdToUUID.get(temp);

			// Add the RxCUI UUID - 
			Get.identifierService().addUuidForNid(createCUIConceptUUID(rxCui, false), Get.identifierService().getNidForUuids(cuiConcept));
			// TODO need to look at what else I should be grabbing - the RXCUI for example should be attached. What else?
		}
		else
		{
			// just creating the reference here, with the UUID - because we don't know if it should be active or inactive yet.
			// create the real concept later.
			cuiConcept = createCUIConceptUUID(rxCui, true);
			dwh.makeConcept(cuiConcept, conceptState, conceptTime);
			dwh.makeBrittleStringAnnotation(dwh.getOtherType(RRFMetadata, "RXCUI"), cuiConcept, rxCui, defaultTime);

			final HashSet<String> conceptSabs = new HashSet<>();
			final HashSet<String> uniqueUMLSCUI = new HashSet<>();
			final BiFunction<String, String, Boolean> uniqueCUIGather = (atn, atv) -> {
				// Pull these up to the concept.
				if ("UMLSCUI".equals(atn))
				{
					uniqueUMLSCUI.add(atv);
					return true;
				}

				return false;
			};

			for (final RXNCONSO atom : conceptData)
			{
				if (atom.sab.equals(this.sctSab))
				{
					continue;
				}

				// Add attributes from SAT table
				this.descSat.clearParameters();
				this.descSat.setString(1, rxCui);
				this.descSat.setString(2, atom.rxaui);

				final ArrayList<RXNSAT> satData;
				boolean disableDescription;
				Long descriptionTime;

				try (ResultSet rs = this.descSat.executeQuery())
				{
					satData = new ArrayList<>();
					disableDescription = false;
					descriptionTime = null;

					while (rs.next())
					{
						final RXNSAT current = new RXNSAT(rs);

						satData.add(current);

						if ("RXN_OBSOLETED".equals(current.atn))
						{
							disableDescription = true;
						}

						if ("RXN_ACTIVATED".equals(current.atn))
						{
							try
							{
								final long time = this.dateParse.parse(current.atv).getTime();
								descriptionTime = time;
							}
							catch (final ParseException e)
							{
								throw new RuntimeException("Can't parse date?");
							}
						}
					}
				}

				if (descriptionTime == null)
				{
					descriptionTime = conceptTime < defaultTime ? conceptTime : defaultTime;
				}
				
				UUID desc = dwh.makeDescription(converterUUID.createNamespaceUUIDFromStrings(cuiConcept.toString(), atom.rxaui),
						cuiConcept, atom.str, dwh.getDescriptionType(atom.tty), MetaData.ENGLISH_LANGUAGE____SOLOR.getPrimordialUuid(), 
						MetaData.NOT_APPLICABLE____SOLOR.getPrimordialUuid(), 
						disableDescription ? Status.INACTIVE : Status.ACTIVE, descriptionTime, null, null);
				
//				final ValuePropertyPairWithSAB desc = new ValuePropertyPairWithSAB(atom.str, this.ptDescriptions.getProperty(atom.tty), atom.sab, satData);

				dwh.makeBrittleStringAnnotation(dwh.getOtherType(RRFMetadata, "RXAUI"), desc, atom.rxaui, descriptionTime);
				
				dwh.makeDynamicSemantic(dwh.getOtherType(RRFMetadata, "SAB"), desc, new DynamicUUIDImpl(dwh.getOtherType(sabs, atom.sab)), descriptionTime);

				if (StringUtils.isNotBlank(atom.code) && !atom.code.equals("NOCODE"))
				{
					dwh.makeBrittleStringAnnotation(MetaData.CODE____SOLOR.getPrimordialUuid(), desc, atom.code, descriptionTime);
				}

				if (StringUtils.isNotBlank(atom.saui))
				{
					dwh.makeStringAnnotation(dwh.getOtherType(RRFMetadata, "SAUI"), desc, atom.saui, descriptionTime);
				}

				if (StringUtils.isNotBlank(atom.scui))
				{
					dwh.makeStringAnnotation(dwh.getOtherType(RRFMetadata, "SCUI"), desc, atom.scui, descriptionTime);
				}

				if (StringUtils.isNotBlank(atom.suppress))
				{
					dwh.makeDynamicSemantic(dwh.getOtherType(RRFMetadata, "SUPPRESS"), desc, new DynamicUUIDImpl(this.suppress.get(atom.suppress)), descriptionTime);
				}

				if (StringUtils.isNotBlank(atom.cvf))
				{
					if (atom.cvf.equals("4096"))
					{
						this.dwh.makeDynamicRefsetMember(cpcRefsetConcept, desc, descriptionTime);
					}
					else
					{
						throw new RuntimeException("Unexpected value in RXNCONSO cvf column '" + atom.cvf + "'");
					}
				}

				if (!atom.lat.equals("ENG"))
				{
					log.error("Non-english lang settings not handled yet!");
				}

				conceptSabs.add(atom.sab);
				processSAT(desc, satData, null, atom.sab, uniqueCUIGather, descriptionTime);
			}

			// pulling up the UMLS CUIs.
			// uniqueUMLSCUI is populated during processSAT
			for (String cui : uniqueUMLSCUI)
			{
				dwh.makeStringAnnotation(dwh.getAttributeType("UMLSCUI"), cuiConcept, cui, conceptTime);
			}

			try
			{
				// there are no attributes in rxnorm without an AUI.
				dwh.makeDynamicRefsetMember(this.allCUIRefsetConcept, cuiConcept, conceptTime);
			}
			catch (RuntimeException e)
			{
				if (isSCT && e.toString().contains("duplicate UUID"))
				{
					//ok - this can happen due to multiple merges onto an existing SCT concept
				}
				else
				{
					throw e;
				}
			}

			// add semantic types
			this.semanticTypeStatement.clearParameters();
			this.semanticTypeStatement.setString(1, rxCui);

			final ResultSet rs = this.semanticTypeStatement.executeQuery();

			processSemanticTypes(cuiConcept, rs);

			if (conceptTime < 0)
			{
				throw new RuntimeException("oops");
			}
		}

		final HashSet<UUID> parents = new HashSet<>();

		this.cuiRelStatementForward.clearParameters();
		this.cuiRelStatementForward.setString(1, rxCui);
		parents.addAll(addRelationships(cuiConcept, REL.read(null, this.cuiRelStatementForward.executeQuery(), true, this.allowedCUIsForSABs,
				this.skippedRelForNotMatchingCUIFilter, true, (string -> reverseRel(string)))));
		this.cuiRelStatementBackward.clearParameters();
		this.cuiRelStatementBackward.setString(1, rxCui);
		parents.addAll(addRelationships(cuiConcept, REL.read(null, this.cuiRelStatementBackward.executeQuery(), false, this.allowedCUIsForSABs,
				this.skippedRelForNotMatchingCUIFilter, true, (string -> reverseRel(string)))));

		// Have to add multiple parents at once, no place to keep all the other details. Load those as associations for now.
		if (parents.size() > 0)
		{
			dwh.makeParentGraph(cuiConcept, parents, Status.ACTIVE, conceptTime);
		}
	}

//	private enum RANKED_DESCRIPTION_TYPES
//	{
//		PIN,  //name for a precise ingredient
//		IN,  //Name for an ingredient
//		MIN,  //name for a multi-ingredient
//		SCD,  //semantic clinical drug
//		BN,  //   Fully-specified drug brand name that can not be prescribed
//		SBD, //Semantic branded drug
//		DF,  //Dose Form
//		BPCK,  //branded drug delivery device
//		GPCK,  //generic drug delivery device
//		DFG,  //Dose Form Group
//		PSN,  //prescribable Names
//		SBDC,  //Semantic Branded Drug Component
//		SCDC,  //Semantic Drug Component
//		SBDF,  //Semantic branded drug and form 
//		SCDF,  //Semantic clinical drug and form 
//		SBDG, // Semantic branded drug group
//		SCDG, //semantic clinical drug group
//		BD,  //Fully-specified drug brand name that can be prescribed
//		DP,  //drug product
//		CD,  //Clinical Drug
//		CP,  //ICPC component process (in original...
//		CDC,  //Clinical drug name in concatenated format (NDDF), Clinical drug name (NDFRT
//		SU,  //Active Substance
//		UNRANKED
//	}

	/**
	 * Process SAT.
	 *
	 * @param itemToAnnotate the item to annotate
	 * @param satRows the sat rows
	 * @param itemCode the item code
	 * @param itemSab the item sab
	 * @param skipCheck the skip check
	 * @throws SQLException the SQL exception
	 * @throws PropertyVetoException the property veto exception
	 */
	private void processSAT(UUID itemToAnnotate, List<RXNSAT> satRows, String itemCode, String itemSab, BiFunction<String, String, Boolean> skipCheck, 
			long time) throws SQLException, PropertyVetoException
	{
		for (final RXNSAT rxnsat : satRows)
		{
			if (skipCheck != null)
			{
				if (skipCheck.apply(rxnsat.atn, rxnsat.atv))
				{
					continue;
				}
			}

			// for some reason, ATUI isn't always provided - don't know why. must gen differently in those cases...
			UUID stringAttrUUID;
			final UUID refsetUUID = dwh.getAttributeType(rxnsat.atn);

			if (rxnsat.atui != null)
			{
				stringAttrUUID = converterUUID.createNamespaceUUIDFromString("ATUI" + rxnsat.atui);
			}
			else
			{
				// need to put the aui in here, to keep it unique, as each AUI frequently specs the same CUI
				stringAttrUUID = converterUUID.createNamespaceUUIDFromStrings(itemToAnnotate.toString(), rxnsat.rxaui, rxnsat.atv, refsetUUID.toString());
			}
			
			UUID attribute; 

			if (refsetUUID == MetaData.VUID____SOLOR.getPrimordialUuid())
			{
				attribute = dwh.makeBrittleStringAnnotation(refsetUUID, itemToAnnotate, rxnsat.atv, time);
			}
			else
			{
				attribute = dwh.makeDynamicSemantic(refsetUUID, itemToAnnotate, new DynamicData[] { new DynamicStringImpl(rxnsat.atv) }, Status.ACTIVE,
					time, stringAttrUUID);
			}

			if (StringUtils.isNotBlank(rxnsat.atui))
			{
				dwh.makeStringAnnotation(dwh.getOtherType(RRFMetadata, "ATUI"), attribute, rxnsat.atui, time);
			}

			if (StringUtils.isNotBlank(rxnsat.stype))
			{
				dwh.makeDynamicSemantic(dwh.getOtherType(RRFMetadata, "STYPE"), attribute, new DynamicUUIDImpl(this.sTypes.get(rxnsat.stype)), time);
			}

			if (StringUtils.isNotBlank(rxnsat.code) && StringUtils.isNotBlank(itemCode) && !rxnsat.code.equals(itemCode))
			{
				throw new RuntimeException("oops");
			}

			if (StringUtils.isNotBlank(rxnsat.satui))
			{
				dwh.makeStringAnnotation(dwh.getAttributeType("SATUI"), attribute, rxnsat.satui, time);
			}

			// only load the sab if it is different than the sab of the item we are putting this attribute on
			if (StringUtils.isNotBlank(rxnsat.sab) && !rxnsat.sab.equals(itemSab))
			{
				dwh.makeDynamicSemantic(dwh.getOtherType(RRFMetadata, "SAB"), attribute, new DynamicUUIDImpl(dwh.getOtherType(sabs, itemSab)), time);
			}

			if (StringUtils.isNotBlank(rxnsat.suppress))
			{
				dwh.makeDynamicSemantic(dwh.getOtherType(RRFMetadata, "SUPPRESS"), attribute, new DynamicUUIDImpl(this.suppress.get(rxnsat.suppress)), time);
			}

			if (StringUtils.isNotBlank(rxnsat.cvf))
			{
				if (rxnsat.cvf.equals("4096"))
				{
					this.dwh.makeDynamicRefsetMember(this.cpcRefsetConcept, attribute, time);
				}
				else
				{
					throw new RuntimeException("Unexpected value in RXNSAT cvf column '" + rxnsat.cvf + "'");
				}
			}
		}
	}

	/**
	 * Process semantic types.
	 *
	 * @param concept the concept
	 * @param rs the rs
	 * @throws SQLException the SQL exception
	 */
	private void processSemanticTypes(UUID concept, ResultSet rs) throws SQLException
	{
		while (rs.next())
		{
			// try
			// {
			final UUID annotation = dwh.makeDynamicSemantic(dwh.getOtherType(RRFMetadata, "STY"), concept,
					new DynamicUUIDImpl(this.semanticTypes.get(rs.getString("TUI"))), defaultTime);

			if (rs.getString("ATUI") != null)
			{
				dwh.makeStringAnnotation(dwh.getOtherType(RRFMetadata, "ATUI"), annotation, rs.getString("ATUI"), defaultTime);
			}

			if (rs.getObject("CVF") != null) // might be an int or a string
			{
				dwh.makeStringAnnotation(dwh.getOtherType(RRFMetadata, "CVF"), annotation, rs.getString("CVF"), defaultTime);
			}
		}

		rs.close();
	}

	/**
	 * Rel check is rel loaded.
	 *
	 * @param rel the rel
	 * @return true, if successful
	 */
	private boolean relCheckIsRelLoaded(REL rel)
	{
		return this.loadedRels.contains(rel.getRelHash());
	}

	/**
	 * Rel check loaded rel.
	 *
	 * @param rel the rel
	 */
	private void relCheckLoadedRel(REL rel)
	{
		this.loadedRels.add(rel.getRelHash());
		this.skippedRels.remove(rel.getRelHash());
	}

	/**
	 * Call this when a rel wasn't added because the rel was listed with the inverse name, rather than the primary name.
	 *
	 * @param rel the rel
	 */
	private void relCheckSkippedRel(REL rel)
	{
		this.skippedRels.add(rel.getInverseRelHash(string -> this.nameToRel.get(string)));
	}

	/**
	 * Reverse rel.
	 *
	 * @param eitherRelType the either rel type
	 * @return the string
	 */
	private String reverseRel(String eitherRelType)
	{
		if (eitherRelType == null)
		{
			return null;
		}

		final Relationship r = this.nameToRel.get(eitherRelType);

		if (r.getFQName().equals(eitherRelType))
		{
			return r.getInverseFQName();
		}
		else if (r.getInverseFQName().equals(eitherRelType))
		{
			return r.getFQName();
		}
		else
		{
			throw new RuntimeException("gak");
		}
	}

	/**
	 * X doc loader helper.
	 *
	 * @param dockey the dockey
	 * @param niceName the nice name
	 * @param loadAsDefinition the load as definition
	 * @param parent the parent
	 * @return the hash map
	 * @throws Exception the exception
	 * 
	 *             Note - may return null, if there were no instances of the requested data
	 */
	private HashMap<String, UUID> xDocLoaderHelper(String dockey, String niceName, boolean loadAsDefinition, UUID parent) throws Exception
	{
		final HashMap<String, UUID> result = new HashMap<>();

		log.info("Creating '" + niceName + "' types");
		{
			try (Statement s = this.db.getConnection().createStatement();
					ResultSet rs = s.executeQuery("SELECT VALUE, TYPE, EXPL FROM " + this.tablePrefix + "DOC where DOCKEY='" + dockey + "'"))
			{
				while (rs.next())
				{
					final String value = rs.getString("VALUE");
					final String type = rs.getString("TYPE");
					final String name = rs.getString("EXPL");

					if (value == null)
					{
						// there is a null entry, don't care about it.
						continue;
					}

					if (!type.equals("expanded_form"))
					{
						throw new RuntimeException("Unexpected type in the attribute data within DOC: '" + type + "'");
					}

					final UUID created = dwh.makeConceptEnNoDialect(
							converterUUID.createNamespaceUUIDFromString(parent + ":" + (loadAsDefinition ? value : name)), (loadAsDefinition ? value : name),
							MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), new UUID[] { parent }, Status.ACTIVE, defaultTime);

					if (!loadAsDefinition)
					{
						dwh.makeDescriptionEnNoDialect(created, value, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getPrimordialUuid(), Status.ACTIVE,
								defaultTime);
					}
					result.put((loadAsDefinition ? value : name), created);

					if (!loadAsDefinition)
					{
						result.put(value, created);
					}
				}
			}
		}

		if (result.isEmpty())
		{
			// This can happen, depending on what is included during the metamorphosys run
			log.info("No entries found for '" + niceName + "' - skipping");
			return null;
		}

		return result;
	}

	/**
	 * Checks if rel primary.
	 *
	 * @param relName the rel name
	 * @param relaName the rela name
	 * @return true, if rel primary
	 */
	private boolean isRelPrimary(String relName, String relaName)
	{
		if (relaName != null)
		{
			return this.nameToRel.get(relaName).getFQName().equals(relaName);
		}
		else
		{
			return this.nameToRel.get(relName).getFQName().equals(relName);
		}
	}
}
