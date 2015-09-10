package gov.va.oia.terminology.converters.sharedUtils.umlsUtils;

import java.beans.PropertyVetoException;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import org.apache.maven.plugin.logging.Log;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;
import org.ihtsdo.otf.tcc.dto.component.TtkComponentChronicle;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.TtkRefexDynamicMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.relationship.TtkRelationshipChronicle;
import gov.va.oia.terminology.converters.sharedUtils.ConsoleUtil;
import gov.va.oia.terminology.converters.sharedUtils.ConverterBaseMojo;
import gov.va.oia.terminology.converters.sharedUtils.EConceptUtility;
import gov.va.oia.terminology.converters.sharedUtils.EConceptUtility.DescriptionType;
import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.BPT_Annotations;
import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.BPT_Associations;
import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.BPT_ContentVersion;
import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.BPT_MemberRefsets;
import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.BPT_Relations;
import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.ConceptCreationNotificationListener;
import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.Property;
import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.PropertyAssociation;
import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.PropertyType;
import gov.va.oia.terminology.converters.sharedUtils.stats.ConverterUUID;
import gov.va.oia.terminology.converters.sharedUtils.umlsUtils.propertyTypes.PT_Descriptions;
import gov.va.oia.terminology.converters.sharedUtils.umlsUtils.propertyTypes.PT_Refsets;
import gov.va.oia.terminology.converters.sharedUtils.umlsUtils.propertyTypes.PT_Relationship_Metadata;
import gov.va.oia.terminology.converters.sharedUtils.umlsUtils.propertyTypes.PT_SAB_Metadata;
import gov.va.oia.terminology.converters.sharedUtils.umlsUtils.rrf.REL;
import gov.vha.isaac.metadata.source.IsaacMetadataAuxiliaryBinding;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import gov.vha.isaac.ochre.util.UuidT3Generator;

public abstract class RRFBaseConverterMojo extends ConverterBaseMojo
{
	protected HashMap<String, String> terminologyCodeRefsetPropertyName_ = new HashMap<>();
	protected PropertyType ptUMLSAttributes_;
	protected HashMap<String, PropertyType> ptTermAttributes_ = new HashMap<>();
	protected PT_Refsets ptUMLSRefsets_;
	protected HashMap<String, BPT_MemberRefsets> ptRefsets_ = new HashMap<>();
	protected BPT_ContentVersion ptContentVersion_;
	protected PropertyType ptSTypes_;
	protected PropertyType ptSuppress_;
	protected PropertyType ptLanguages_;
	protected PropertyType ptSourceRestrictionLevels_;
	protected PropertyType ptSABs_;
	protected HashMap<String, PropertyType> ptDescriptions_ = new HashMap<>();;
	protected HashMap<String, BPT_Relations> ptRelationships_ = new HashMap<>();;
	protected HashMap<String, BPT_Associations> ptAssociations_ = new HashMap<>();;
	protected PropertyType ptRelationshipMetadata_;
	public HashMap<String, Relationship> nameToRel_ = new HashMap<>();
	protected HashMap<String, UUID> semanticTypes_ = new HashMap<>();
	protected EConceptUtility eConcepts_;
	protected RRFDatabaseHandle db_;
	protected String tablePrefix_;
	//sabQueryString is only populated if they provided a filter list
	protected String sabQueryString_ = "";
	protected HashSet<String> sabsInDB_ = new HashSet<>();
	public boolean isRxNorm;
	
	private HashMap<String, UUID> rootConcepts_ = new HashMap<>();
	protected TtkConceptChronicle umlsRootConcept_ = null;
	
	protected UUID metaDataRoot_;
	
	PreparedStatement satRelStatement_;
	
	private HashSet<UUID> loadedRels_ = new HashSet<>();
	private HashSet<UUID> skippedRels_ = new HashSet<>();
	
	private HashMap<String, HashMap<String, HashMap<String, AtomicInteger>>> mappingRelCounters_ = new HashMap<>();
	private HashMap<String, AbbreviationExpansion> abbreviationExpansions;
	
	private HashSet<String> mapToIsa = new HashSet<>();
	
	//disabled debug code
	//protected HashSet<UUID> conceptUUIDsUsedInRels_ = new HashSet<>();
	//protected HashSet<UUID> conceptUUIDsCreated_ = new HashSet<>();
	
	/**
	 * If sabList is null or empty, no sab filtering is done. 
	 */
	protected void init(String tablePrefix, PropertyType attributes, Collection<String> sabList, List<String> additionalRootConcepts, 
			Collection<String> relsToMapToIsA, long defaultTime) throws Exception
	{
		clearTargetFiles();
		tablePrefix_ = tablePrefix;
		isRxNorm = tablePrefix_.equals("RXN");

		ptUMLSAttributes_ = attributes;
		
		abbreviationExpansions = AbbreviationExpansion.load(
				getClass().getResourceAsStream(isRxNorm ? "/RxNormAbbreviationsExpansions.txt" : "/UMLSAbbreviationExpansions.txt"));
		
		if (relsToMapToIsA != null)
		{
			mapToIsa.addAll(relsToMapToIsA);
		}
		mapToIsa.add("isa");
		//not translating this one to isa for now
		//		mapToIsa.add("CHD");
		
		if (sabList != null && sabList.size() > 0)
		{
			sabQueryString_ += " (";
			for (String sab : sabList)
			{
				validateSab(sab);
				sabQueryString_ += "SAB='" + sab + "' or ";
				sabsInDB_.add(sab);
			}
			sabQueryString_ = sabQueryString_.substring(0, sabQueryString_.length() - 4);
			sabQueryString_ += ")";
		}
		else if (!isRxNorm)
		{
			Statement s = db_.getConnection().createStatement();
			ResultSet rs = s.executeQuery("Select distinct SAB from MRRANK");
			while (rs.next())
			{
				String sab = rs.getString("SAB");
				sabsInDB_.add(sab);
			}
			rs.close();
			
			rs = s.executeQuery("Select distinct SAB from MRSAT");
			while (rs.next())
			{
				String sab = rs.getString("SAB");
				sabsInDB_.add(sab);
			}
			rs.close();
			
			rs = s.executeQuery("Select distinct SAB from MRREL");
			while (rs.next())
			{
				String sab = rs.getString("SAB");
				sabsInDB_.add(sab);
			}
			rs.close();
			s.close();
		}

		File binaryOutputFile = new File(outputDirectory, "RRF-" + tablePrefix + ".jbin");

		dos_ = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(binaryOutputFile)));
		//TODO this would be the wrong module for UMLS terminologies - if we load UMLS again, update this to change module per term.
		eConcepts_ = new EConceptUtility(IsaacMetadataAuxiliaryBinding.RXNORM.getPrimodialUuid(), dos_, defaultTime);

		metaDataRoot_ = ConverterUUID.createNamespaceUUIDFromString("metadata");
		eConcepts_.createAndStoreMetaDataConcept(metaDataRoot_, (isRxNorm ? "RxNorm" : "UMLS") + " RRF Metadata", 
				IsaacMetadataAuxiliaryBinding.ISAAC_ROOT.getPrimodialUuid(), null, dos_);

		if (!isRxNorm)
		{
			//Create the UMLS hierarchy root concept
			umlsRootConcept_ = eConcepts_.createConcept("UMLS Root Concepts");
			ConsoleUtil.println("Root concept FSN is 'UMLS Root Concepts' and the UUID is " + umlsRootConcept_.getPrimordialUuid());
			//We write this later, so callers can still add attributes to it.
		}
		
		loadMetaData(additionalRootConcepts);

		ConsoleUtil.println("Metadata Statistics");
		for (String s : eConcepts_.getLoadStats().getSummary())
		{
			ConsoleUtil.println(s);
		}

		eConcepts_.clearLoadStats();
		satRelStatement_ = db_.getConnection().prepareStatement("select * from " + tablePrefix_ + "SAT where " + (isRxNorm ? "RXAUI" : "METAUI") 
				+ "= ? and STYPE='RUI' " + (sabQueryString_.length() > 0 ? "and" + sabQueryString_ : ""));
	}

	private void validateSab(String sab) throws Exception
	{
		Statement s = db_.getConnection().createStatement();
		ResultSet rs = s.executeQuery("Select SAB from " + tablePrefix_ + "CONSO where SAB = '" + sab + "' limit 1");
		if (!rs.next())
		{
			rs.close();
			//Check MRREL
			rs = s.executeQuery("Select SAB from " + tablePrefix_ + "REL where SAB = '" + sab + "' limit 1");
			if (!rs.next())
			{
				throw new Exception("Invalid sabFilter '" + sab + "'.  Perhaps you have mixed up VSABs and RSABs?");
			}
		}
		rs.close();
		s.close();
	}
	
	protected void finish() throws IOException, SQLException
	{
		checkRelationships();
		satRelStatement_.close();
		eConcepts_.storeRefsetConcepts(ptUMLSRefsets_, dos_);
		for (BPT_MemberRefsets r : ptRefsets_.values())
		{
			eConcepts_.storeRefsetConcepts(r, dos_);
		}
		if (umlsRootConcept_ != null)
		{
			umlsRootConcept_.writeExternal(dos_);
		}
		dos_.close();
		ConsoleUtil.println("Load Statistics");
		for (String s : eConcepts_.getLoadStats().getSummary())
		{
			ConsoleUtil.println(s);
		}
		
		if (!isRxNorm)
		{
			ConsoleUtil.println("Cross-Terminology Mapping Statistics");
			summarizeMappingRels();
		}
		
		//disabled debug code
		//conceptUUIDsUsedInRels_.removeAll(conceptUUIDsCreated_);
		//if (conceptUUIDsUsedInRels_.size() > 0)
		//{
		//	for (UUID uuid : conceptUUIDsUsedInRels_)
		//	{
		//		ConsoleUtil.printErrorln("ERROR!  Didn't create concept: " + uuid.toString());
		//	}
		//}

		// this could be removed from final release. Just added to help debug editor problems.
		ConsoleUtil.println("Dumping UUID Debug File");
		ConverterUUID.dump(outputDirectory, (isRxNorm ? "RxNorm" : "UMLS") + "UUID");
		ConsoleUtil.writeOutputToFile(new File(outputDirectory, "ConsoleOutput.txt").toPath());
	}
	
	private void clearTargetFiles()
	{
		new File(outputDirectory, (isRxNorm ? "RxNorm" : "UMLS") + "UUIDDebugMap.txt").delete();
		new File(outputDirectory, "ConsoleOutput.txt").delete();
		new File(outputDirectory, "RRF.jbin").delete();
	}
	
	protected abstract void loadCustomMetaData() throws Exception;
	protected abstract void addCustomRefsets(BPT_MemberRefsets refset) throws Exception;
	
	private void loadMetaData(List<String> additionalRootConcepts) throws Exception
	{
		ptUMLSRefsets_ = new PT_Refsets(isRxNorm ? "RxNorm RRF" : "UMLS");
		ptContentVersion_ = new BPT_ContentVersion();
		final PropertyType sourceMetadata = new PT_SAB_Metadata();
		ptRelationshipMetadata_ = new PT_Relationship_Metadata();

		//don't load ptContentVersion_ yet - custom code might add to it
		eConcepts_.loadMetaDataItems(Arrays.asList(ptUMLSRefsets_, sourceMetadata, ptRelationshipMetadata_, ptUMLSAttributes_),
				metaDataRoot_, dos_);
		
		loadTerminologySpecificMetadata();
		
		//STYPE values
		ptSTypes_= new PropertyType("STYPEs", true, DynamicSememeDataType.STRING){};
		{
			ConsoleUtil.println("Creating STYPE types");
			ptSTypes_.indexByAltNames();
			Statement s = db_.getConnection().createStatement();
			ResultSet rs = s.executeQuery("SELECT DISTINCT VALUE, TYPE, EXPL FROM " + tablePrefix_ + "DOC where DOCKEY like 'STYPE%'");
			while (rs.next())
			{
				String sType = rs.getString("VALUE");
				String type = rs.getString("TYPE");
				String name = rs.getString("EXPL");

				if (!type.equals("expanded_form"))
				{
					throw new RuntimeException("Unexpected type in the attribute data within DOC: '" + type + "'");
				}				
				
				ptSTypes_.addProperty(name, null, sType, null);
			}
			//T ODO maybe a bug in metamorphosys?  I know at one point, I didn't have these.. but they exist now... ignore
			//ptSTypes_.addProperty("Concept identifier", null, "CUI", null);
			//ptSTypes_.addProperty("Source asserted descriptor identifier", null, "SDUI", null);
			rs.close();
			s.close();
		}
		eConcepts_.loadMetaDataItems(ptSTypes_, metaDataRoot_, dos_);
		
		
		ptSuppress_=  xDocLoaderHelper("SUPPRESS", "Suppress", false);
		
		//Not yet loading co-occurrence data yet, so don't need these yet.
		//xDocLoaderHelper("COA", "Attributes of co-occurrence", false);
		//xDocLoaderHelper("COT", "Type of co-occurrence", true);  
		
		final PropertyType contextTypes = xDocLoaderHelper("CXTY", "Context Type", false);
		
		//not yet loading mappings - so don't need this yet
		//xDocLoaderHelper("FROMTYPE", "Mapping From Type", false);  
		//xDocLoaderHelper("TOTYPE", "Mapping To Type", false);  
		//MAPATN - not yet used in UMLS
		
		// Handle the languages
		{
			ConsoleUtil.println("Creating language types");
			ptLanguages_ = new PropertyType("Languages", true, DynamicSememeDataType.STRING){};
			Statement s = db_.getConnection().createStatement();
			ResultSet rs = s.executeQuery("SELECT * from " + tablePrefix_ + "DOC where DOCKEY = 'LAT' and VALUE in (select distinct LAT from " 
					+ tablePrefix_ + "CONSO " + (sabQueryString_.length() > 0 ? " where " + sabQueryString_ : "") + ")");
			while (rs.next())
			{
				String abbreviation = rs.getString("VALUE");
				String type = rs.getString("TYPE");
				String expansion = rs.getString("EXPL");

				if (!type.equals("expanded_form"))
				{
					throw new RuntimeException("Unexpected type in the language data within DOC: '" + type + "'");
				}

				Property p = ptLanguages_.addProperty(abbreviation, expansion, null);

				if (abbreviation.equals("ENG") || abbreviation.equals("SPA"))
				{
					// use official WB SCT languages
					if (abbreviation.equals("ENG"))
					{
						p.setWBPropertyType(UUID.fromString("c0836284-f631-3c86-8cfc-56a67814efab"));
					}
					else if (abbreviation.equals("SPA"))
					{
						p.setWBPropertyType(UUID.fromString("03615ef2-aa56-336d-89c5-a1b5c4cee8f6"));
					}
					else
					{
						throw new RuntimeException("oops");
					}
				}
			}
			rs.close();
			s.close();
			eConcepts_.loadMetaDataItems(ptLanguages_, metaDataRoot_, dos_);
		}
		
		// And Source Restriction Levels
		{
			ConsoleUtil.println("Creating Source Restriction Level types");
			ptSourceRestrictionLevels_ = new PropertyType("Source Restriction Levels", true, DynamicSememeDataType.UUID){};
			PreparedStatement ps = db_.getConnection().prepareStatement("SELECT VALUE, TYPE, EXPL from " + tablePrefix_ + "DOC where DOCKEY=? ORDER BY VALUE");
			ps.setString(1, "SRL");
			ResultSet rs = ps.executeQuery();
			
			String value = null;
			String description = null;
			String uri = null;
			
			//Two entries per SRL, read two rows, create an entry.
			
			while (rs.next())
			{
				String type = rs.getString("TYPE");
				String expl = rs.getString("EXPL");
				
				if (type.equals("expanded_form"))
				{
					description = expl;
				}
				else if (type.equals("uri"))
				{
					uri = expl;
				}
				else
				{
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
					
					if (description == null || uri == null)
					{
						throw new RuntimeException("oops");
					}
					
					Property p = ptSourceRestrictionLevels_.addProperty(value, null, description);
					final String temp = uri;
					p.registerConceptCreationListener(new ConceptCreationNotificationListener()
					{
						@Override
						public void conceptCreated(Property property, TtkConceptChronicle concept)
						{
							eConcepts_.addStringAnnotation(concept, temp, ptUMLSAttributes_.getProperty("URI").getUUID(), Status.ACTIVE);
						}
					});
					type = null;
					expl = null;
					value = null;
				}
			}
			rs.close();
			ps.close();

			eConcepts_.loadMetaDataItems(ptSourceRestrictionLevels_, metaDataRoot_, dos_);
		}

		// And Source vocabularies
		final PreparedStatement getSABMetadata = db_.getConnection().prepareStatement("Select * from " + tablePrefix_ + "SAB where (VSAB = ? or (RSAB = ? and CURVER='Y' ))");
		{
			ConsoleUtil.println("Creating Source Vocabulary types");
			ptSABs_ = new PropertyType("Source Vocabularies", true, DynamicSememeDataType.STRING){};
			ptSABs_.indexByAltNames();
			
			HashSet<String> sabList = new HashSet<>();
			sabList.addAll(sabsInDB_);
			
			Statement s = db_.getConnection().createStatement();
			ResultSet rs;
			if (isRxNorm)
			{
				rs = s.executeQuery("select distinct SAB from RXNSAT where ATN='NDC'");
				while (rs.next())
				{
					sabList.add(rs.getString("SAB"));
				}
				rs.close();
				s.close();
			}
			
			for (String currentSab : sabList)
			{
				s = db_.getConnection().createStatement();
				rs = s.executeQuery("SELECT SON from " + tablePrefix_ + "SAB WHERE (VSAB='" + currentSab + "' or (RSAB='" + currentSab + "' and CURVER='Y'))");
				if (rs.next())
				{
					String son = rs.getString("SON");

					Property p = ptSABs_.addProperty(son, null, currentSab, null);
					p.registerConceptCreationListener(new ConceptCreationNotificationListener()
					{
						@Override
						public void conceptCreated(Property property, TtkConceptChronicle concept)
						{
							try
							{
								//lookup the other columns for the row with this newly added RSAB terminology
								getSABMetadata.setString(1, property.getSourcePropertyAltName() == null ? property.getSourcePropertyNameFSN() : property.getSourcePropertyAltName());
								getSABMetadata.setString(2, property.getSourcePropertyAltName() == null ? property.getSourcePropertyNameFSN() : property.getSourcePropertyAltName());
								ResultSet rs2 = getSABMetadata.executeQuery();
								if (rs2.next())  //should be only one result
								{
									for (Property metadataProperty : sourceMetadata.getProperties())
									{
										String columnName = metadataProperty.getSourcePropertyAltName() == null ? metadataProperty.getSourcePropertyNameFSN() 
												: metadataProperty.getSourcePropertyAltName();
										String columnValue = rs2.getString(columnName);
										if (columnValue == null)
										{
											continue;
										}
										if (columnName.equals("SRL"))
										{
											eConcepts_.addUuidAnnotation(concept, ptSourceRestrictionLevels_.getProperty(columnValue).getUUID(),
													metadataProperty.getUUID());
										}
										else if (columnName.equals("CXTY"))
										{
											eConcepts_.addUuidAnnotation(concept, contextTypes.getProperty(columnValue).getUUID(),
													metadataProperty.getUUID());
										}
										else
										{
											eConcepts_.addStringAnnotation(concept, columnValue, metadataProperty.getUUID(), Status.ACTIVE);
										}
									}
								}
								if (rs2.next())
								{
									throw new RuntimeException("Too many sabs.  Perhaps you should be using versioned sabs!");
								}
								rs2.close();
							}
							catch (SQLException e)
							{
								throw new RuntimeException("Error loading *SAB", e);
							}
						}
					});
				}
				else
				{
					throw new RuntimeException("Too few? SABs - perhaps you need to use versioned SABs.");
				}
				if (rs.next())
				{
					throw new RuntimeException("Too many SABs for '" + currentSab  + "' - perhaps you need to use versioned SABs.");
				}
				rs.close();
				s.close();
			}
			eConcepts_.loadMetaDataItems(ptSABs_, metaDataRoot_, dos_);
			getSABMetadata.close();
		}

		// And semantic types
		{
			ConsoleUtil.println("Creating semantic types");
			PropertyType ptSemanticTypes = new PropertyType("Semantic Types", true, DynamicSememeDataType.UUID){};
			Statement s = db_.getConnection().createStatement();
			ResultSet rs = s.executeQuery("SELECT distinct TUI, STN, STY from " + tablePrefix_+ "STY");
			while (rs.next())
			{
				final String tui = rs.getString("TUI");
				final String stn = rs.getString("STN");
				String sty = rs.getString("STY");

				Property p = ptSemanticTypes.addProperty(sty);
				semanticTypes_.put(tui, p.getUUID());
				p.registerConceptCreationListener(new ConceptCreationNotificationListener()
				{
					@Override
					public void conceptCreated(Property property, TtkConceptChronicle concept)
					{
						eConcepts_.addStringAnnotation(concept, tui, ptUMLSAttributes_.getProperty("TUI").getUUID(), Status.ACTIVE);
						eConcepts_.addStringAnnotation(concept, stn, ptUMLSAttributes_.getProperty("STN").getUUID(), Status.ACTIVE);
					}
				});
			}
			rs.close();
			s.close();

			eConcepts_.loadMetaDataItems(ptSemanticTypes, metaDataRoot_, dos_);
		}
		
		loadCustomMetaData();
		eConcepts_.loadMetaDataItems(ptContentVersion_, metaDataRoot_, dos_);
		
		findRootConcepts(additionalRootConcepts);
	}
	
	/*
	 * Note - may return null, if there were no instances of the requested data
	 */
	protected PropertyType xDocLoaderHelper(String dockey, String niceName, boolean loadAsDefinition) throws Exception
	{
		ConsoleUtil.println("Creating '" + niceName + "' types");
		PropertyType pt = new PropertyType(niceName, true, DynamicSememeDataType.UUID) {};
		{
			if (!loadAsDefinition)
			{
				pt.indexByAltNames();
			}
			Statement s = db_.getConnection().createStatement();
			ResultSet rs = s.executeQuery("SELECT VALUE, TYPE, EXPL FROM " + tablePrefix_ + "DOC where DOCKEY='" + dockey + "'");
			while (rs.next())
			{
				String value = rs.getString("VALUE");
				String type = rs.getString("TYPE");
				String name = rs.getString("EXPL");
				
				if (value == null)
				{
					//there is a null entry, don't care about it.
					continue;
				}

				if (!type.equals("expanded_form"))
				{
					throw new RuntimeException("Unexpected type in the attribute data within DOC: '" + type + "'");
				}				
				
				pt.addProperty((loadAsDefinition ? value : name), null, (loadAsDefinition ? null : value), (loadAsDefinition ? name : null));
			}
			rs.close();
			s.close();
		}
		if (pt.getProperties().size() == 0)
		{
			//This can happen, depending on what is included during the metamorphosys run
			ConsoleUtil.println("No entries found for '" + niceName + "' - skipping");
			return null;
		}
		eConcepts_.loadMetaDataItems(pt, metaDataRoot_, dos_);
		return pt;
	}
	
	private void loadTerminologySpecificMetadata() throws Exception
	{
		UUID mainNamespace = ConverterUUID.getNamespace();
		
		for (String sab : sabsInDB_)
		{
			UUID termSpecificMetadataRoot;
			String terminologyName;
			
			ConsoleUtil.println("Setting up metadata for " + sab);
			
			if (isRxNorm)
			{
				terminologyName = "RxNorm";
				//Change to a different namespace, so property types that are repeated in the metadata don't collide.
				ConverterUUID.configureNamespace(ConverterUUID.createNamespaceUUIDFromString(eConcepts_.moduleUuid_, terminologyName + ".metadata"));
			}
			else
			{
				Statement s = db_.getConnection().createStatement();
				ResultSet rs = s.executeQuery("Select SSN from MRSAB where RSAB ='" + sab + "' or VSAB='" + sab + "'");
				if (rs.next())
				{
					terminologyName = rs.getString("SSN");
				}
				else
				{
					throw new RuntimeException("Can't find name for " + sab);
				}

				//Change to a different namespace, so property types that are repeated in various UMLS terminologies don't collide.
				ConverterUUID.configureNamespace(ConverterUUID.createNamespaceUUIDFromString(eConcepts_.moduleUuid_, terminologyName + ".metadata"));
			}
			termSpecificMetadataRoot = ConverterUUID.createNamespaceUUIDFromString("metadata");
			//If we have an item with 'special' snomed handling, hide it down under the UMLS RRF metadata - as we don't load any concepts related 
			//to this terminology - just pulling relationships.
			eConcepts_.createAndStoreMetaDataConcept(termSpecificMetadataRoot, terminologyName + " Metadata", 
					(specialHandling(sab) ? metaDataRoot_ : IsaacMetadataAuxiliaryBinding.ISAAC_ROOT.getPrimodialUuid()), null, dos_);
			
			//dynamically add more attributes from *DOC
			{
				ConsoleUtil.println("Creating attribute types");
				PropertyType annotations = new BPT_Annotations() {};
				annotations.indexByAltNames();
				
				Statement s = db_.getConnection().createStatement();
				//extra logic at the end to keep NDC's from any sab when processing RXNorm
				ResultSet rs = s.executeQuery("SELECT VALUE, TYPE, EXPL from " + tablePrefix_ + "DOC where DOCKEY = 'ATN' and VALUE in (select distinct ATN from " 
						+ tablePrefix_ + "SAT" + " where SAB='" + sab + "'" + (isRxNorm ? " or ATN='NDC'" : "") + ")");
				while (rs.next())
				{
					String abbreviation = rs.getString("VALUE");
					String type = rs.getString("TYPE");
					String expansion = rs.getString("EXPL");
	
					if (!type.equals("expanded_form"))
					{
						throw new RuntimeException("Unexpected type in the attribute data within DOC: '" + type + "'");
					}
	
					String preferredName = null;
					String description = null;
					if (expansion.length() > 30)
					{
						description = expansion;
					}
					else
					{
						preferredName = expansion;
					}
					
					AbbreviationExpansion ae = abbreviationExpansions.get(abbreviation);
					if (ae == null)
					{
						ConsoleUtil.printErrorln("No Abbreviation Expansion found for " + abbreviation);
						annotations.addProperty(abbreviation, preferredName, description);
					}
					else
					{
						annotations.addProperty(ae.getExpansion(), null, ae.getAbbreviation(), ae.getDescription());
					}
				}
				if (isRxNorm)
				{
					annotations.addProperty("UMLSAUI");  //TODO bug in RxNorm - This property should be in RXNDOC, but it is currently missing - bug in the data  
				}
				rs.close();
				s.close();
				
				if (annotations.getProperties().size() > 0)
				{
					eConcepts_.loadMetaDataItems(annotations, termSpecificMetadataRoot, dos_);
				}
				ptTermAttributes_.put(sab, annotations);
			}
			
			
			// And Descriptions
			{
				ConsoleUtil.println("Creating description_ types");
				PropertyType descriptions = new PT_Descriptions(terminologyName);
				descriptions.indexByAltNames();
				Statement s = db_.getConnection().createStatement();
				ResultSet usedDescTypes;
				if (isRxNorm )
				{
					usedDescTypes = s.executeQuery("select distinct TTY from RXNCONSO WHERE SAB='" + sab + "'");
				}
				else
				{
					usedDescTypes = s.executeQuery("select distinct TTY from MRRANK WHERE SAB='" + sab + "'");
				}

				PreparedStatement ps = db_.getConnection().prepareStatement("select TYPE, EXPL from " + tablePrefix_ + "DOC where DOCKEY='TTY' and VALUE=?");

				while (usedDescTypes.next())
				{
					String tty = usedDescTypes.getString(1);
					ps.setString(1, tty);
					ResultSet descInfo = ps.executeQuery();

					String expandedForm = null;
					final HashSet<String> classes = new HashSet<>();

					while (descInfo.next())
					{
						String type = descInfo.getString("TYPE");
						String expl = descInfo.getString("EXPL");
						if (type.equals("expanded_form"))
						{
							if (expandedForm != null)
							{
								throw new RuntimeException("Expected name to be null!");
							}
							expandedForm = expl;
						}
						else if (type.equals("tty_class"))
						{
							classes.add(expl);
						}
						else
						{
							throw new RuntimeException("Unexpected type in DOC for '" + tty + "'");
						}
					}
					descInfo.close();
					ps.clearParameters();
					
					Property p = null;
					AbbreviationExpansion ae = abbreviationExpansions.get(tty);
					if (ae == null)
					{
						ConsoleUtil.printErrorln("No Abbreviation Expansion found for " + tty);
						p = makeDescriptionType(tty, expandedForm, null, null, classes);
					}
					else
					{
						p = makeDescriptionType(ae.getExpansion(), null, ae.getAbbreviation(), ae.getDescription(), classes);
					}
					
					descriptions.addProperty(p);
					p.registerConceptCreationListener(new ConceptCreationNotificationListener()
					{
						@Override
						public void conceptCreated(Property property, TtkConceptChronicle concept)
						{
							for (String tty_class : classes)
							{
								eConcepts_.addStringAnnotation(concept, tty_class, ptUMLSAttributes_.getProperty("tty_class").getUUID(), Status.ACTIVE);
							}
						}
					});
					
				}
				usedDescTypes.close();
				s.close();
				ps.close();
				
				ptDescriptions_.put(sab, descriptions);
				allDescriptionsCreated(sab);
				
				if (descriptions.getProperties().size() > 0)
				{
					eConcepts_.loadMetaDataItems(descriptions, termSpecificMetadataRoot, dos_);
				}
			}
			
			//Make a refset
			BPT_MemberRefsets refset = new BPT_MemberRefsets(terminologyName);
			terminologyCodeRefsetPropertyName_.put(sab, "All " + terminologyName + " Concepts");
			refset.addProperty(terminologyCodeRefsetPropertyName_.get(sab));
			addCustomRefsets(refset);
			ptRefsets_.put(sab, refset);
			eConcepts_.loadMetaDataItems(refset, termSpecificMetadataRoot, dos_);
			
			loadRelationshipMetadata(terminologyName, sab, termSpecificMetadataRoot);
		}
		//Go back to the primary namespace
		ConverterUUID.configureNamespace(mainNamespace);
	}
	
	private void findRootConcepts(List<String> additionalRoots) throws IOException, SQLException
	{
		if (isRxNorm)
		{
			return;
		}
		Statement statement = db_.getConnection().createStatement();
		ResultSet rs = statement.executeQuery("select * from MRHIER where PAUI is null");
		HashMap<String, UUID> sabRoots = new HashMap<>();
		while (rs.next())
		{
			String sab = rs.getString("SAB");
			UUID parent = sabRoots.get(sab);
			
			if (parent == null)
			{
				parent = ptSABs_.getProperty(sab).getUUID();
				//This concept was already created, so I can't add a rel to it - create a new concept with the same UUID, let the WB merge them.
				TtkConceptChronicle tempConcept = eConcepts_.createConcept(parent);
				eConcepts_.addRelationship(tempConcept, umlsRootConcept_.getPrimordialUuid());
				tempConcept.writeExternal(dos_);
			}
			
			rootConcepts_.put(rs.getString("CUI") + ":" + rs.getString("AUI"), parent);
		}
		rs.close();
		
		try
		{
			if (additionalRoots != null)
			{
				for (String s : additionalRoots)
				{
					String[] temp = s.split("\\|");
					String sab = temp[0];
					String cui = temp[1];
					String aui = temp[2];
					
					UUID parent = sabRoots.get(sab);
					
					if (parent == null)
					{
						parent = ptSABs_.getProperty(sab).getUUID();
						//This concept was already created, so I can't add a rel to it - create a new concept with the same UUID, let the WB merge them.
						TtkConceptChronicle tempConcept = eConcepts_.createConcept(parent);
						eConcepts_.addRelationship(tempConcept, umlsRootConcept_.getPrimordialUuid());
						tempConcept.writeExternal(dos_);
					}
					rootConcepts_.put(cui+ ":" + aui, parent);
				}
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException("Couldn't parse the provided 'additionalRoots' - must be a 'SAB|CUI|AUI' triple", e);
		}
	}
	
	/**
	 * Returns the UUID of the concept this 'root' concept should link to, or null, if none.
	 */
	protected UUID isRootConcept(String cui, String aui)
	{
		return rootConcepts_.get(cui + ":" + aui);
	}
	
	/**
	 * Implementer needs to add an entry to ptDescriptions_ with the data provided...
	 */
	protected abstract Property makeDescriptionType(String fsnName, String preferredName, String altName, String description, final Set<String> tty_classes);
	
	/**
	 * Called just before the description_ set is actually created as eConcepts (in case the implementor needs to rank them all together)
	 */
	protected abstract void allDescriptionsCreated(String sab) throws Exception;
	
	/**
	 * @param customHandle - allow custom handling of particular items.  Pass null or an empty list if no custom handling is required.
	 * Otherwise, pass a fuction that accepts two strings - it will be fed atn (attribute name) and atv (attribute value) - function should
	 * return true, if the value is considered handled, and should not have 'normal' handling by the default code.  Else, return false,
	 * and this code will behave as if no function was passed.
	 */
	protected abstract void processSAT(TtkComponentChronicle<?, ?> itemToAnnotate, ResultSet rs, String itemCode, String itemSab, 
			List<BiFunction<String, String, Boolean>> customHandle) throws SQLException, PropertyVetoException;
	
	private void loadRelationshipMetadata(String terminologyName, String sab, UUID terminologyMetadataRoot) throws Exception
	{
		ConsoleUtil.println("Creating relationship types");
		//Both of these get added as extra attributes on the relationship definition
		HashMap<String, ArrayList<String>> snomedCTRelaMappings = new HashMap<>(); //Maps something like 'has_specimen_source_morphology' to '118168003' (may be more than one target SCT code)
		HashMap<String, String> snomedCTRelMappings = new HashMap<>();  //Maps something like '118168003' to 'RO'
		
		nameToRel_ = new HashMap<>();
		
		Statement s = db_.getConnection().createStatement();
		//get the inverses of first, before the expanded forms
		ResultSet rs = s.executeQuery("SELECT DOCKEY, VALUE, TYPE, EXPL FROM " + tablePrefix_ + "DOC where DOCKEY ='REL' or DOCKEY = 'RELA' order by TYPE DESC ");
		while (rs.next())
		{
			String dockey = rs.getString("DOCKEY");
			String value = rs.getString("VALUE");
			String type = rs.getString("TYPE");
			String expl = rs.getString("EXPL");
			if (value == null)
			{
				continue;  //don't need this one
			}
			
			if (type.equals("snomedct_rela_mapping"))
			{
				ArrayList<String> targetSCTIDs = snomedCTRelaMappings.get(expl);
				if (targetSCTIDs == null)
				{
					targetSCTIDs = new ArrayList<String>();
					snomedCTRelaMappings.put(expl, targetSCTIDs);
				}
				targetSCTIDs.add(value);
			}
			else if (type.equals("snomedct_rel_mapping"))
			{
				snomedCTRelMappings.put(value, expl);
			}
			else
			{
				Relationship rel = nameToRel_.get(value);
				if (rel == null)
				{
					if (type.endsWith("_inverse"))
					{
						rel = nameToRel_.get(expl);
						if (rel == null)
						{
							rel = new Relationship(dockey.equals("RELA"));
							nameToRel_.put(value, rel);
							nameToRel_.put(expl, rel);
						}
						else
						{
							throw new RuntimeException("shouldn't happen due to query order");
						}
					}
					else
					{
						//only cases where there is no inverse
						rel = new Relationship(dockey.equals("RELA"));
						nameToRel_.put(value, rel);
					}
				}
				
				if (type.equals("expanded_form"))
				{
					rel.addDescription(value, expl);
				}
				else if (type.equals("rela_inverse") || type.equals("rel_inverse"))
				{
					rel.addRelInverse(value, expl);
				}
				else
				{
					throw new RuntimeException("Oops");
				}
			}
		}
		
		rs.close();
		s.close();
		
		HashSet<String> actuallyUsedRelsOrRelas = new HashSet<>();
		
		for (Entry<String, ArrayList<String>> x : snomedCTRelaMappings.entrySet())
		{
			if (!nameToRel_.containsKey(x.getKey()))
			{
				//metamorphosys doesn't seem to remove these when the sct rel types aren't included - just silently remove them 
				//unless it seems that they should map.
				if (isRxNorm || sab.startsWith("SNOMEDCT"))
				{
					//may_be_a appears to be a bug in RxNorm 2013-12-02.  silently ignore...
					//TODO see if they fix it in the future, make this check version specific?
					//seems to be getting worse... now it fails to remove 'has_life_circumstance' too in 2014AA, and a few others.
					//Changing to a warning.
					ConsoleUtil.printErrorln("Warning - The 'snomedct_rela_mapping' '" + x.getKey() + "' does not have a corresponding REL entry!  Skipping");
//					if (!x.getKey().equals("may_be_a") && !x.getKey().equals("has_life_circumstance"))
//					{
//						throw new RuntimeException("ERROR - No rel for " + x.getKey() + ".");
//					}
				}
				for (String sctId : x.getValue())
				{
					snomedCTRelMappings.remove(sctId);
				}
			}
			else
			{
				for (String sctid : x.getValue())
				{
					nameToRel_.get(x.getKey()).addSnomedCode(x.getKey(), sctid);
					String relType = snomedCTRelMappings.remove(sctid);
					if (relType != null)
					{
						nameToRel_.get(x.getKey()).addRelType(x.getKey(), relType);
						//Shouldn't need this, but there are some cases where the metadata is inconsistent - with how it is actually used.
						actuallyUsedRelsOrRelas.add(relType);
					}
				}
			}
		}
		
		if (snomedCTRelMappings.size() > 0)
		{
			for (Entry<String, String> x : snomedCTRelMappings.entrySet())
			{
				ConsoleUtil.printErrorln(x.getKey() + ":" + x.getValue());
			}
			throw new RuntimeException("oops - still have (things listed above)");
			
		}
		
		final BPT_Relations relationships = new BPT_Relations(terminologyName) {};  
		relationships.indexByAltNames();
		final BPT_Associations associations = new BPT_Associations(terminologyName) {};
		associations.indexByAltNames();
		
		s = db_.getConnection().createStatement();
		rs = s.executeQuery("select distinct REL, RELA from " + tablePrefix_ + "REL where SAB='" + sab + "'");
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
		
		HashSet<Relationship> uniqueRels = new HashSet<>(nameToRel_.values());
		for (final Relationship r : uniqueRels)
		{
			r.setSwap(db_.getConnection(), tablePrefix_);
			
			if (!actuallyUsedRelsOrRelas.contains(r.getFSNName()) && !actuallyUsedRelsOrRelas.contains(r.getInverseFSNName()))
			{
				continue;
			}
			
			Property p;
			if (mapToIsa.contains(r.getFSNName()))
			{
				p = new Property((r.getAltName() == null ? r.getFSNName() : r.getAltName()), null, (r.getAltName() == null ? null : r.getFSNName()),
						r.getDescription(), EConceptUtility.isARelUuid_);  //map to isA
				relationships.addProperty(p);  //conveniently, the only thing we will treat as relationships are things mapped to isa.
			}
			else
			{
				p = new PropertyAssociation(null, (r.getAltName() == null ? r.getFSNName() : r.getAltName()), 
						(r.getAltName() == null ? null : r.getFSNName()), (r.getInverseAltName() == null ? r.getInverseFSNName() : r.getInverseAltName()),
						r.getDescription(), false);
				associations.addProperty(p);
			}
			
			p.registerConceptCreationListener(new ConceptCreationNotificationListener()
			{
				@Override
				public void conceptCreated(Property property, TtkConceptChronicle concept)
				{
					//associations already handle inverse names 
					if (!(property instanceof PropertyAssociation) && r.getInverseFSNName() != null)
					{
						eConcepts_.addDescription(concept, (r.getInverseAltName() == null ? r.getInverseFSNName() : r.getInverseAltName()), DescriptionType.FSN, 
								false, ptDescriptions_.get(sab).getProperty("Inverse FSN").getUUID(),
								ptDescriptions_.get(sab).getProperty("Inverse FSN").getPropertyType().getPropertyTypeReferenceSetUUID(), Status.ACTIVE);
					}
					
					if (r.getAltName() != null)
					{
						//Need to create this UUID to be different than forward name, in case forward and reverse are identical (like 'RO')
						UUID descUUID = ConverterUUID.createNamespaceUUIDFromStrings(concept.getPrimordialUuid().toString(), r.getInverseFSNName(), 
								DescriptionType.SYNONYM.name(), "false", "inverse");
						//Yes, this looks funny, no its not a copy/paste error.  We swap the FSN and alt names for... it a long story.  42.
						eConcepts_.addDescription(concept, descUUID, r.getInverseFSNName(), DescriptionType.SYNONYM, false, 
								ptDescriptions_.get(sab).getProperty("Inverse Synonym").getUUID(),
								ptDescriptions_.get(sab).getProperty("Inverse Synonym").getPropertyType().getPropertyTypeReferenceSetUUID(), Status.ACTIVE);
					}
					
					if (r.getInverseDescription() != null)
					{
						eConcepts_.addDescription(concept, r.getInverseDescription(), DescriptionType.DEFINITION, true, 
								ptDescriptions_.get(sab).getProperty("Inverse Description").getUUID(),
								ptDescriptions_.get(sab).getProperty("Inverse Description").getPropertyType().getPropertyTypeReferenceSetUUID(), Status.ACTIVE);
					}
					
					if (r.getRelType() != null)
					{
						Relationship generalRel = nameToRel_.get(r.getRelType());
						
						eConcepts_.addUuidAnnotation(concept, (mapToIsa.contains(generalRel.getFSNName()) ? relationships.getProperty(generalRel.getFSNName()) : 
							associations.getProperty(generalRel.getFSNName())).getUUID(), ptRelationshipMetadata_.getProperty("General Rel Type").getUUID());
					}
					
					if (r.getInverseRelType() != null)
					{
						Relationship generalRel = nameToRel_.get(r.getInverseRelType());
						
						eConcepts_.addUuidAnnotation(concept, (mapToIsa.contains(generalRel.getFSNName()) ? relationships.getProperty(generalRel.getFSNName()) : 
							associations.getProperty(generalRel.getFSNName())).getUUID(), 
								ptRelationshipMetadata_.getProperty("Inverse General Rel Type").getUUID());
					}
					
					for (String sctCode : r.getRelSnomedCode())
					{
						eConcepts_.addUuidAnnotation(concept, UuidT3Generator.fromSNOMED(sctCode), 
								ptRelationshipMetadata_.getProperty("Snomed Code").getUUID());
					}
					
					for (String sctCode : r.getInverseRelSnomedCode())
					{
						eConcepts_.addUuidAnnotation(concept, UuidT3Generator.fromSNOMED(sctCode), 
								ptRelationshipMetadata_.getProperty("Inverse Snomed Code").getUUID());
					}
				}
			});
		}
		
		if (relationships.getProperties().size() > 0)
		{
			eConcepts_.loadMetaDataItems(relationships, terminologyMetadataRoot, dos_);
		}
		ptRelationships_.put(sab, relationships);
		if (associations.getProperties().size() > 0)
		{
			eConcepts_.loadMetaDataItems(associations, terminologyMetadataRoot, dos_);
		}
		ptAssociations_.put(sab, associations);
	}
	
	protected void processSemanticTypes(TtkConceptChronicle concept, ResultSet rs) throws SQLException
	{
		while (rs.next())
		{
			TtkRefexDynamicMemberChronicle annotation = eConcepts_.addUuidAnnotation(concept, semanticTypes_.get(rs.getString("TUI")), ptUMLSAttributes_.getProperty("STY").getUUID());
			if (rs.getString("ATUI") != null)
			{
				eConcepts_.addStringAnnotation(annotation, rs.getString("ATUI"), ptUMLSAttributes_.getProperty("ATUI").getUUID(), Status.ACTIVE);
			}

			if (rs.getObject("CVF") != null)  //might be an int or a string
			{
				eConcepts_.addStringAnnotation(annotation, rs.getString("CVF"), ptUMLSAttributes_.getProperty("CVF").getUUID(), Status.ACTIVE);
			}
		}
		rs.close();
	}
	
	/**
	 * Add the attribute value(s) for each given type, with nested attributes linking to the AUI(s) that they came from.  
	 */
	protected void loadGroupStringAttributes(TtkComponentChronicle<?, ?> component, UUID annotationRefset, HashMap<UUID, HashMap<String, HashSet<String>>> values, boolean skipNestedAUIs)
	{
		for (Entry<UUID, HashMap<String, HashSet<String>>> dataType : values.entrySet())
		{
			for (Entry<String, HashSet<String>> valueAui : dataType.getValue().entrySet())
			{
				String value = valueAui.getKey();
				TtkRefexDynamicMemberChronicle attribute = eConcepts_.addStringAnnotation(component, value, dataType.getKey(), Status.ACTIVE);
				if (!skipNestedAUIs)
				{
					for (String aui : valueAui.getValue())
					{
						eConcepts_.addStringAnnotation(attribute, aui, annotationRefset, Status.ACTIVE);
					}
				}
			}
		}
	}
	
	/**
	 * Add the attribute value(s) for each given type, with nested attributes linking to the AUI(s) that they came from.  
	 */
	protected void loadGroupUUIDAttributes(TtkComponentChronicle<?, ?> component, UUID annotationRefset, HashMap<UUID, HashMap<UUID, HashSet<String>>> values, boolean skipNestedAUIs)
	{
		for (Entry<UUID, HashMap<UUID, HashSet<String>>> dataType : values.entrySet())
		{
			for (Entry<UUID, HashSet<String>> valueAui : dataType.getValue().entrySet())
			{
				UUID value = valueAui.getKey();
				TtkRefexDynamicMemberChronicle attribute = eConcepts_.addUuidAnnotation(component, value, dataType.getKey());
				if (!skipNestedAUIs)
				{
					for (String aui : valueAui.getValue())
					{
						eConcepts_.addStringAnnotation(attribute, aui, annotationRefset, Status.ACTIVE);
					}
				}
			}
		}
	}
	
	protected void addAttributeToGroup(HashMap<UUID, HashMap<String, HashSet<String>>> group, UUID typeForColName, String value, String aui)
	{
		if (value == null)
		{
			return;
		}
		HashMap<String, HashSet<String>> colData = group.get(typeForColName);
		if (colData == null)
		{
			colData = new HashMap<>();
			group.put(typeForColName, colData);
		}
		HashSet<String> auis = colData.get(value);
		if (auis == null)
		{
			auis = new HashSet<>();
			colData.put(value, auis);
		}
		if (aui != null)
		{
			auis.add(aui);
		}
	}
	
	protected void addAttributeToGroup(HashMap<UUID, HashMap<UUID, HashSet<String>>> group, UUID typeForColName, UUID value, String aui)
	{
		if (value == null)
		{
			return;
		}
		HashMap<UUID, HashSet<String>> colData = group.get(typeForColName);
		if (colData == null)
		{
			colData = new HashMap<>();
			group.put(typeForColName, colData);
		}
		HashSet<String> auis = colData.get(value);
		if (auis == null)
		{
			auis = new HashSet<>();
			colData.put(value, auis);
		}
		if (aui != null)
		{
			auis.add(aui);
		}
	}
	
	protected UUID createCUIConceptUUID(String cui)
	{
		return ConverterUUID.createNamespaceUUIDFromString("CUI:" + cui, true);
	}
	
	protected UUID createCuiSabCodeConceptUUID(String cui, String sab, String code)
	{
		return ConverterUUID.createNamespaceUUIDFromString("CODE:" + cui + ":" + sab + ":" + code, true);
	}
	
	/**
	 * @throws SQLException
	 * @throws PropertyVetoException 
	 */
	protected void addRelationships(TtkConceptChronicle concept, List<REL> relationships) throws SQLException, PropertyVetoException
	{
		HashMap<UUID, List<REL>> uniqueRels = new HashMap<>();
		
		//preprocess - set up the source and target UUIDs for this rel, so we can identify duplicates.
		//Note - the duplicates we are detecting here are rels that point to the same WB code concept, that occur 
		//due to the way that we combine AUI's.  This is not detecting duplicates caused by forward/reverse rels in the UMLS.
		for (REL relationship : relationships)
		{
			relationship.setSourceUUID(concept.getPrimordialUuid());
			
			if (!relationship.hasSnomedSpecialHandling())
			{
				if (relationship.getSourceAUI() == null)
				{
					relationship.setTargetUUID(createCUIConceptUUID(relationship.getTargetCUI()));
				}
				else
				{
					relationship.setTargetUUID(createCuiSabCodeConceptUUID((isRxNorm ? relationship.getRxNormTargetCUI() : relationship.getTargetCUI()), 
							relationship.getTargetSAB(), relationship.getTargetCode()));
				}
			}
			
			List<REL> rels = uniqueRels.get(relationship.getRelHash());
			if (rels == null)
			{
				rels = new ArrayList<REL>(4);
				uniqueRels.put(relationship.getRelHash(), rels);
			}
			rels.add(relationship);
		}
		
		for (List<REL> duplicateRels : uniqueRels.values())
		{
			//We currently don't check the properties on the (duplicate) inverse rels to make sure they are all present - we assume that they 
			//created the inverse relationships as an exact copy of the primary rel direction.  So, just checking the first rel from our dupe list is good enough
			if (isRelPrimary(duplicateRels.get(0).getRel(), duplicateRels.get(0).getRela()))
			{
				//This can happen when the reverse of the rel equals the rel... sib/sib
				if (relCheckIsRelLoaded(duplicateRels.get(0)))
				{
					continue;
				}
				
				Property relType = ptAssociations_.get(duplicateRels.get(0).getSab()).getProperty(duplicateRels.get(0).getRela() == null ? 
						duplicateRels.get(0).getRel() : duplicateRels.get(0).getRela()) == null ? 
								ptRelationships_.get(duplicateRels.get(0).getSab()).getProperty(
										(duplicateRels.get(0).getRela() == null ? duplicateRels.get(0).getRel() : duplicateRels.get(0).getRela())) : 
								ptAssociations_.get(duplicateRels.get(0).getSab()).getProperty(
										(duplicateRels.get(0).getRela() == null ? duplicateRels.get(0).getRel() : duplicateRels.get(0).getRela()));
				
				TtkRelationshipChronicle r;
				
				if (relType.getWBTypeUUID() == null)
				{
					r = eConcepts_.addRelationship(concept, (duplicateRels.get(0).getRui() != null ? ConverterUUID.createNamespaceUUIDFromString("RUI:" + duplicateRels.get(0).getRui()) : null),
							duplicateRels.get(0).getTargetUUID(), relType.getUUID(), null, null, null);
				}
				else  //need to swap out to the wb rel type (usually, isa)
				{
					r = eConcepts_.addRelationship(concept, (duplicateRels.get(0).getRui() != null ? ConverterUUID.createNamespaceUUIDFromString("RUI:" + duplicateRels.get(0).getRui()) : null),
							duplicateRels.get(0).getTargetUUID(), relType.getWBTypeUUID(), relType.getUUID(), relType.getPropertyType().getPropertyTypeReferenceSetUUID(), null);
				}
				
				if (!isRxNorm)
				{
					//stats gathering, nothing more
					countMappingRel(duplicateRels.get(0).getSourceSAB(), duplicateRels.get(0).getTargetSAB(), relType.getSourcePropertyNameFSN());
				}
				
				//disabled debug code
				//conceptUUIDsUsedInRels_.add(concept.getPrimordialUuid());
				//conceptUUIDsUsedInRels_.add(duplicateRels.get(0).getTargetUUID());
				
				HashMap<UUID, HashMap<String, HashSet<String>>> stringAttributes = new HashMap<>();
				HashMap<UUID, HashMap<UUID, HashSet<String>>> uuidAttributes = new HashMap<>();
				
				//Now we have created a single relationship for this REL - iterate all of the duplicate definitions of this rel to pick up all of the unique annotations
				//that we need.
				HashSet<String> addedRUIs = new HashSet<>();
				for (REL dupeRel : duplicateRels)
				{
					if (dupeRel.getRela() != null)  //we already used rela - annotate with rel.
					{
						Property genericType = ptAssociations_.get(dupeRel.getSab()).getProperty(dupeRel.getRel()) == null ? 
								ptRelationships_.get(dupeRel.getSab()).getProperty(dupeRel.getRel()) :
									ptAssociations_.get(dupeRel.getSab()).getProperty(dupeRel.getRel());
						boolean reversed = false;
						if (genericType == null && dupeRel.getRela().equals("mapped_from"))
						{
							//This is to handle non-sensical data in UMLS... they have no consistency in the generic rel they assign - sometimes RB, sometimes RN.
							//reverse it - currently, only an issue on 'mapped_from' rels - as the code in Relationship.java has some exceptions for this type.
							genericType = ptAssociations_.get(dupeRel.getSab()).getProperty(reverseRel(dupeRel.getRel())) == null ? 
									ptRelationships_.get(dupeRel.getSab()).getProperty(reverseRel(dupeRel.getRel())) :
										ptAssociations_.get(dupeRel.getSab()).getProperty(reverseRel(dupeRel.getRel()));
							reversed = true;
						}
						addAttributeToGroup(uuidAttributes, ptUMLSAttributes_.getProperty(reversed ? "Generic rel type (inverse)" : "Generic rel type").getUUID(),
								genericType.getUUID(), dupeRel.getSourceTargetAnnotationLabel());
					}
					if (dupeRel.getRui() != null)
					{
						if (!addedRUIs.contains(dupeRel.getRui()))
						{
							eConcepts_.addStringAnnotation(r, dupeRel.getRui(), ptUMLSAttributes_.getProperty("RUI").getUUID(), Status.ACTIVE);
							addedRUIs.add(dupeRel.getRui());
							satRelStatement_.clearParameters();
							satRelStatement_.setString(1, dupeRel.getRui());
							ResultSet nestedRels = satRelStatement_.executeQuery();
							processSAT(r, nestedRels, null, dupeRel.getSab(), null);
						}
					}
					if (!isRxNorm && dupeRel.getSrui() != null)
					{
						addAttributeToGroup(stringAttributes, ptUMLSAttributes_.getProperty("SRUI").getUUID(), dupeRel.getSrui(), dupeRel.getSourceTargetAnnotationLabel());
					}
					
					if (!isRxNorm)
					{
						//always rxnorm for rxnorm, don't bother loading.
						addAttributeToGroup(uuidAttributes, ptUMLSAttributes_.getProperty("SAB").getUUID(),
								ptSABs_.getProperty(dupeRel.getSab()).getUUID(), dupeRel.getSourceTargetAnnotationLabel());
					}
					if (!isRxNorm && dupeRel.getSl() != null && !dupeRel.getSl().equals(dupeRel.getSab()))  //I don't  think this ever actually happens
					{
						addAttributeToGroup(uuidAttributes, ptUMLSAttributes_.getProperty("SL").getUUID(),
								ptSABs_.getProperty(dupeRel.getSl()).getUUID(), dupeRel.getSourceTargetAnnotationLabel());
					}
					if (dupeRel.getRg() != null)
					{
						addAttributeToGroup(stringAttributes, ptUMLSAttributes_.getProperty("RG").getUUID(), dupeRel.getRg(), dupeRel.getSourceTargetAnnotationLabel());
					}
					if (dupeRel.getDir() != null)
					{
						addAttributeToGroup(stringAttributes, ptUMLSAttributes_.getProperty("DIR").getUUID(), dupeRel.getDir(), dupeRel.getSourceTargetAnnotationLabel());
					}
					if (dupeRel.getSuppress() != null)
					{
						addAttributeToGroup(uuidAttributes, ptUMLSAttributes_.getProperty("SUPPRESS").getUUID(),
								ptSuppress_.getProperty(dupeRel.getSuppress()).getUUID(), dupeRel.getSourceTargetAnnotationLabel());
					}
					
					//Add an attribute that says which relationship this attribute came from (can't use RUI, as it isn't provided consistently)
					if (dupeRel.getSourceTargetAnnotationLabel() != null)
					{
						eConcepts_.addStringAnnotation(r, dupeRel.getSourceTargetAnnotationLabel(), ptRelationshipMetadata_.getProperty("sAUI & tAUI").getUUID(), Status.ACTIVE);
					}
					
				}

				loadGroupStringAttributes(r, ptRelationshipMetadata_.getProperty("sAUI & tAUI").getUUID(), stringAttributes, false);
				loadGroupUUIDAttributes(r, ptRelationshipMetadata_.getProperty("sAUI & tAUI").getUUID(), uuidAttributes, false);
				processRelCVFAttributes(r, duplicateRels);
				
				relCheckLoadedRel(duplicateRels.get(0));
			}
			else
			{
				relCheckSkippedRel(duplicateRels.get(0));
			}
		}
	}
	
	//This is overridden by RXNorm, which handles it differently
	protected void processRelCVFAttributes(TtkRelationshipChronicle r, List<REL> duplicateRelationships)
	{
		HashMap<UUID, HashMap<String, HashSet<String>>> stringAttributes = new HashMap<>();
		for (REL dupeRel : duplicateRelationships)
		{
			if (dupeRel.getCvf() != null)
			{
				addAttributeToGroup(stringAttributes, ptUMLSAttributes_.getProperty("CVF").getUUID(), dupeRel.getCvf(), dupeRel.getSourceTargetAnnotationLabel());
			}
		}
		loadGroupStringAttributes(r, ptRelationshipMetadata_.getProperty("sAUI & tAUI").getUUID(), stringAttributes, false);
	}
	
	//This is overridden by UMLS, which treats Snomed special in some cases
	protected boolean specialHandling(String sab)
	{
		return false;
	}
	
	private boolean isRelPrimary(String relName, String relaName)
	{
		if (relaName != null)
		{
			return nameToRel_.get(relaName).getFSNName().equals(relaName);
		}
		else
		{
			return nameToRel_.get(relName).getFSNName().equals(relName);
		}
	}
	
	public String reverseRel(String eitherRelType)
	{
		if (eitherRelType == null)
		{
			return null;
		}
		Relationship r = nameToRel_.get(eitherRelType);
		if (r.getFSNName().equals(eitherRelType))
		{
			return r.getInverseFSNName();
		}
		else if (r.getInverseFSNName().equals(eitherRelType))
		{
			return r.getFSNName();
		}
		else
		{
			throw new RuntimeException("gak");
		}
		
	}
	
	private void relCheckLoadedRel(REL rel)
	{
		loadedRels_.add(rel.getRelHash());
		skippedRels_.remove(rel.getRelHash());
	}
	
	private boolean relCheckIsRelLoaded(REL rel)
	{
		return loadedRels_.contains(rel.getRelHash());
	}

	/**
	 * Call this when a rel wasn't added because the rel was listed with the inverse name, rather than the primary name. 
	 */
	private void relCheckSkippedRel(REL rel)
	{
		skippedRels_.add(rel.getInverseRelHash(this));
	}
	
	private void checkRelationships()
	{
		//if the inverse relationships all worked properly, skipped should be empty when loaded is subtracted from it.
		for (UUID uuid : loadedRels_)
		{
			skippedRels_.remove(uuid);
		}
		
		if (skippedRels_.size() > 0)
		{
			ConsoleUtil.printErrorln("Relationship design error - " +  skippedRels_.size() + " were skipped that should have been loaded");
		}
		else
		{
			ConsoleUtil.println("Yea! - no missing relationships!");
		}
	}
	
	@Override
	public Log getLog()
	{
		// noop
		return null;
	}

	@Override
	public void setLog(Log arg0)
	{
		//noop
	}
	
	private void countMappingRel(String sourceSAB, String targetSAB, String relName)
	{
		String source = sourceSAB == null ? "<CUI>" : sourceSAB;
		String target = targetSAB == null ? "<CUI>" : targetSAB;

		if (source.equals(target) && !source.equals("<CUI>"))
		{
			return;
		}
		HashMap<String, HashMap<String, AtomicInteger>> targetToType = mappingRelCounters_.get(source);
		if (targetToType == null)
		{
			targetToType = new HashMap<>();
			mappingRelCounters_.put(source, targetToType);
		}
		HashMap<String, AtomicInteger> typeToCount = targetToType.get(target);
		if (typeToCount == null)
		{
			typeToCount = new HashMap<>();
			targetToType.put(target, typeToCount);
		}
		
		AtomicInteger count = typeToCount.get(relName);
		if (count == null)
		{
			count = new AtomicInteger(0);
			typeToCount.put(relName, count);
		}
		count.incrementAndGet();
	}
	
	protected void summarizeMappingRels()
	{
		for (Entry<String, HashMap<String, HashMap<String, AtomicInteger>>> counter : mappingRelCounters_.entrySet())
		{
			String source = counter.getKey();
			for (Entry<String, HashMap<String, AtomicInteger>> targets : counter.getValue().entrySet())
			{
				String target = targets.getKey();
				for (Entry<String, AtomicInteger> types : targets.getValue().entrySet())
				{
					ConsoleUtil.println("Source - " + source + " - Target - " +  target + " - Type - " + types.getKey() + " - " +  types.getValue().get());
				}
			}
		}
	}
}
