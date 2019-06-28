/*
 * Copyright 2018 VetsEZ Inc, Sagebits LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.solor.direct;

import static java.time.temporal.ChronoField.INSTANT_SECONDS;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUtility;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.api.util.StringUtils;
import sh.isaac.api.util.UuidT3Generator;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.types.DynamicBooleanImpl;
import sh.isaac.model.semantic.types.DynamicDoubleImpl;
import sh.isaac.model.semantic.types.DynamicFloatImpl;
import sh.isaac.model.semantic.types.DynamicIntegerImpl;
import sh.isaac.model.semantic.types.DynamicLongImpl;
import sh.isaac.model.semantic.types.DynamicNidImpl;
import sh.isaac.model.semantic.types.DynamicStringImpl;
import sh.isaac.model.semantic.types.DynamicUUIDImpl;
import sh.isaac.model.semantic.version.DynamicImpl;

/**
 * An importer component that processes RF2 refsets into dynamic semantic refsets.
 * 
 * Much of the pattern / processing taking from {@link BrittleRefsetWriter}
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class DynamicRefsetWriter extends TimedTaskWithProgressTracker<Integer>
{
	private static final int REFSET_MEMBER_UUID = 0;
	private static final int EFFECTIVE_TIME_INDEX = 1;
	private static final int ACTIVE_INDEX = 2;  // 0 == false, 1 == true
	private static final int MODULE_SCTID_INDEX = 3;
	protected static final int ASSEMBLAGE_SCT_ID_INDEX = 4;
	private static final int REFERENCED_CONCEPT_SCT_ID_INDEX = 5;
	protected static final int VARIABLE_FIELD_START = 6;

	private final List<String[]> refsetRecords;
	private final Semaphore writeSemaphore;
	private final List<IndexBuilderService> indexers;
	private final ImportSpecification importSpecification;
	private final ImportType importType;
	private final AssemblageService assemblageService = Get.assemblageService();
	private final IdentifierService identifierService = Get.identifierService();
	private final StampService stampService = Get.stampService();
	private final HashSet<String> refsetsToIgnore = new HashSet<>();
	private final ConcurrentHashMap<Integer,Boolean> configuredDynamicSemantics;
	private final HashMap<String, ArrayList<DynamicColumnInfo>> dynamicColumnInfo;

	public DynamicRefsetWriter(List<String[]> semanticRecords, Semaphore writeSemaphore, String message, ImportSpecification importSpecification,
			ImportType importType, HashMap<String, ArrayList<DynamicColumnInfo>> refsetColumnInfo, ConcurrentHashMap<Integer,Boolean> configuredDynamicSemantics)
	{
		this.refsetRecords = semanticRecords;
		this.writeSemaphore = writeSemaphore;
		this.importSpecification = importSpecification;
		this.importType = importType;
		this.writeSemaphore.acquireUninterruptibly();
		this.dynamicColumnInfo = refsetColumnInfo;
		this.configuredDynamicSemantics = configuredDynamicSemantics;
		indexers = LookupService.get().getAllServices(IndexBuilderService.class);
		updateTitle("Importing semantic batch of size: " + semanticRecords.size());
		updateMessage(message);
		addToTotalWork(semanticRecords.size());
		Get.activeTasks().add(this);

		// TODO move these to an import preference...
		refsetsToIgnore.add("6011000124106"); // 6011000124106 | ICD-10-CM complex map reference set (foundation metadata concept)
		refsetsToIgnore.add("447563008"); // 447563008 | ICD-9-CM equivalence complex map reference set (foundation metadata concept)
		refsetsToIgnore.add("447569007"); // 447569007 | International Classification of Diseases, Ninth Revision, Clinical Modification reimbursement
											 // complex map reference set (foundation metadata concept)
		refsetsToIgnore.add("450993002"); // 450993002 | International Classification of Primary Care, Second edition complex map reference set (foundation metadata concept) |
		refsetsToIgnore.add("447562003"); // 447562003 | ICD-10 complex map reference set (foundation metadata concept)
		refsetsToIgnore.add("900000000000497000"); // 900000000000497000 | CTV3 simple map reference set (foundation metadata concept) |
		refsetsToIgnore.add("467614008"); // 467614008 | GMDN simple map reference set (foundation metadata concept)
		refsetsToIgnore.add("446608001"); // 446608001 | ICD-O simple map reference set (foundation metadata concept)
		refsetsToIgnore.add("711112009"); // 711112009 | ICNP diagnoses simple map reference set (foundation metadata concept)
		refsetsToIgnore.add("712505008"); // 712505008 | ICNP interventions simple map reference set (foundation metadata concept)
		refsetsToIgnore.add("900000000000498005"); // 900000000000498005 | SNOMED RT identifier simple map (foundation metadata concept)
		refsetsToIgnore.add("733900009"); // 733900009 | UCUM simple map reference set (foundation metadata concept)
		refsetsToIgnore.add("900000000000490003");  // 900000000000490003 | Description inactivation indicator attribute value reference set (foundation metadata concept) |
		refsetsToIgnore.add("900000000000489007");  // 900000000000489007 | Concept inactivation indicator attribute value reference set (foundation metadata concept)
		refsetsToIgnore.add("900000000000527005");  // 900000000000527005 | SAME AS association reference set (foundation metadata concept)
	}

	private void index(Chronology chronicle)
	{
		for (IndexBuilderService indexer : indexers)
		{
			indexer.indexNow(chronicle);
		}
	}

	int nidFromSctid(String sctid)
	{
		try
		{
			return identifierService.getNidForUuids(UuidT3Generator.fromSNOMED(sctid));
		}
		catch (NoSuchElementException e)
		{
			LOG.error("The SCTID {} was mapped to UUID {} but that UUID has not been loaded into the system", sctid, UuidT3Generator.fromSNOMED(sctid));
			throw e;
		}
	}

	@Override
	protected Integer call() throws Exception
	{
		try
		{
			int authorNid = Get.configurationService().getGlobalDatastoreConfiguration().getDefaultEditCoordinate().getAuthorNid();
			int pathNid = Get.configurationService().getGlobalDatastoreConfiguration().getDefaultEditCoordinate().getPathNid();
			List<String[]> noSuchElementList = new ArrayList<>();

			Transaction transaction = Get.commitService().newTransaction(ChangeCheckerMode.INACTIVE);
			boolean skippedAny = false;
			int skipped = 0;
			for (String[] refsetRecord : refsetRecords)
			{
				try
				{
					UUID referencedComponentUuid = UuidT3Generator.fromSNOMED(refsetRecord[REFERENCED_CONCEPT_SCT_ID_INDEX]);
					final Status state = Status.fromZeroOneToken(refsetRecord[ACTIVE_INDEX]);
					if (importType == ImportType.ACTIVE_ONLY)
					{
						if (state == Status.INACTIVE)
						{
							continue;
						}
						// if the referenced component not previously imported, may
						// have been inactive, so don't import.
						if (!identifierService.hasUuid(referencedComponentUuid))
						{
							if (!skippedAny)
							{
								skippedAny = true;
								StringBuilder builder = new StringBuilder();
								int assemblageNid = nidFromSctid(refsetRecord[ASSEMBLAGE_SCT_ID_INDEX]);
								builder.append("Skipping at least one record in: ");
								builder.append(Get.conceptDescriptionText(assemblageNid));
								builder.append("\n");
								builder.append(Arrays.toString(refsetRecord));
								LOG.warn(builder.toString());
							}
							skipped++;
							continue;
						}
					}
					if (refsetsToIgnore.contains(refsetRecord[ASSEMBLAGE_SCT_ID_INDEX]))
					{
						skipped++;
						continue;
					}

					UUID elementUuid = UUID.fromString(refsetRecord[REFSET_MEMBER_UUID]);
					int moduleNid = nidFromSctid(refsetRecord[MODULE_SCTID_INDEX]);
					int assemblageNid = nidFromSctid(refsetRecord[ASSEMBLAGE_SCT_ID_INDEX]);
					int referencedComponentNid = nidFromSctid(refsetRecord[REFERENCED_CONCEPT_SCT_ID_INDEX]);
					TemporalAccessor accessor = DateTimeFormatter.ISO_INSTANT.parse(DirectImporter.getIsoInstant(refsetRecord[EFFECTIVE_TIME_INDEX]));
					long time = accessor.getLong(INSTANT_SECONDS) * 1000;
					int versionStamp = stampService.getStampSequence(state, time, authorNid, moduleNid, pathNid);
					
					if (configuredDynamicSemantics.get(assemblageNid) == null)
					{
						//Need to stop the world, and configure it - this is the first time we have seen this assemblage.
						synchronized (configuredDynamicSemantics) {
							if (configuredDynamicSemantics.get(assemblageNid) == null)
							{
								// set up the dynamic semantic definition for this assemblage we haven't seen before
								// TODO would like to add a second parent to this concept into the metadata tree, but I don't think it knows how to
								// merge logic graphs well yet....
								//TODO this should be done with an edit coordinate the same as the refset concept, I suppose... I don't know how to find 
								//the coords of the refset concept that was specified during _this_ import, however...
								
								ArrayList<DynamicColumnInfo> dci = dynamicColumnInfo.get(refsetRecord[ASSEMBLAGE_SCT_ID_INDEX]);
								if (dci == null)
								{
									LOG.warn("Refset may be misconfigured, no construction information available from the der2_ccirefset_refsetdescriptor file." 
											+ "  This may be ok, if this is an extension that appends to an existing refset");
									//We can't do any futher config here.  If it wasn't configured, it should fail when the data validator checks the columns
									//against the spec.
								}
								else
								{
									// check if the spec from the file matches the file name...
									for (int i = 0; i < dci.size(); i++)
									{
										if (importSpecification.refsetBrittleTypes[i].getDynamicColumnType() != dci.get(i).getColumnDataType())
										{
											throw new RuntimeException("The name of the refset file " + importSpecification.contentProvider.getStreamSourceName()
													+ " does not match the column type information in the 'attributeType' column of the der2_ccirefset_refsetdescriptor file "
													+ " for 'attributeOrder' " + i);
										}
									}
									
									int[] assemblageStamps = Get.concept(assemblageNid).getVersionStampSequences();
									Arrays.sort(assemblageStamps);
									int stampSequence = assemblageStamps[assemblageStamps.length - 1];  //use the largest (newest) stamp on the concept, 
									//since we probably just loaded the concept....
									
									List<Chronology> items = LookupService.getService(DynamicUtility.class).configureConceptAsDynamicSemantic(transaction, assemblageNid,
											"DynamicDefinition for refset " + DirectImporter.trimZipName(importSpecification.contentProvider.getStreamSourceName()),
											dci.toArray(new DynamicColumnInfo[dci.size()]),
											null, null, stampSequence);
	
									for (Chronology c : items)
									{
										index(c);
										assemblageService.writeSemanticChronology((SemanticChronology)c);
									}
								}
								configuredDynamicSemantics.put(assemblageNid, true);
							}
						}
					}

					SemanticChronologyImpl refsetMemberToWrite = new SemanticChronologyImpl(this.importSpecification.streamType.getSemanticVersionType(),
							elementUuid, assemblageNid, referencedComponentNid);

					if (importSpecification.streamType != ImportStreamType.DYNAMIC)
					{
						throw new RuntimeException("Expected dynamic stream type");
					}
					
					DynamicData[] data = new DynamicData[importSpecification.refsetBrittleTypes == null ? 0 : importSpecification.refsetBrittleTypes.length];
					for (int i = 0; i < data.length; i++)
					{
						String readData = (refsetRecord[VARIABLE_FIELD_START + i] == null ? "" : refsetRecord[VARIABLE_FIELD_START + i].trim());
						
						if (StringUtils.isNotBlank(readData))
						{
							switch(importSpecification.refsetBrittleTypes[i].getDynamicColumnType())
							{
								case BOOLEAN:
									data[i] = new DynamicBooleanImpl(readData.equals("1"));
									break;
								case DOUBLE:
									data[i] = new DynamicDoubleImpl(Double.parseDouble(readData));
									break;
								case FLOAT:
									data[i] = new DynamicFloatImpl(Float.parseFloat(readData));
									break;
								case INTEGER:
									try
									{
										data[i] = new DynamicIntegerImpl(Integer.parseInt(readData));
									}
									catch (NumberFormatException e)
									{
										// for some silly reason, a whole bunch of refsets in the core SCT are 
										//defined with UNSIGNED integers as the data type.  Java 8 (kinda) supports
										//unsigned ints - so we will at least make an attempt to shove it in here.
										data[i] = new DynamicIntegerImpl(Integer.parseUnsignedInt(readData));
										LOG.warn("You won the prize!  You actually found an unsigned int: " + data[i] + " more testing needed now....");
									}
									break;
								case LONG:
									data[i] = new DynamicLongImpl(Long.parseLong(readData));
									break;
								case NID:
									data[i] = new DynamicNidImpl(nidFromSctid(readData));
									break;
								case STRING:
									data[i] = new DynamicStringImpl(readData);
									break;
								case UUID:
									data[i] = new DynamicUUIDImpl(UUID.fromString(readData));
									break;
								case UNKNOWN:
								case ARRAY:
								case POLYMORPHIC:
								case BYTEARRAY:
								default :
									throw new RuntimeException("Unsupported brittle type " + importSpecification.refsetBrittleTypes[i].getDynamicColumnType());
							}
						}
					}
					
					DynamicImpl dv = refsetMemberToWrite.createMutableVersion(versionStamp);
					dv.setData(data);
					index(refsetMemberToWrite);
					assemblageService.writeSemanticChronology(refsetMemberToWrite);
				}
				catch (NoSuchElementException ex)
				{
					noSuchElementList.add(refsetRecord);
				}
				completedUnitOfWork();
			}
			if (!noSuchElementList.isEmpty())
			{
				LOG.error("Continuing after import failed with no such element exception for these records: \n" + noSuchElementList.toString());
			}
			transaction.commit("Dynamic refset writer");
			return skipped;
		}
		finally
		{
			this.writeSemaphore.release();
			Get.activeTasks().remove(this);
		}
	}
}
