/*
 * Copyright 2020 Mind Computing Inc, Sagebits LLC
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

package sh.isaac.misc.exporters.rf2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.MetaData;
import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.coordinate.Coordinates;
import sh.isaac.api.coordinate.StampFilter;
import sh.isaac.api.coordinate.StampFilterImmutable;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.Zip;
import sh.isaac.misc.exporters.rf2.files.RF2File;
import sh.isaac.misc.exporters.rf2.files.RF2FileFetcher;
import sh.isaac.misc.exporters.rf2.files.RF2ReleaseStatus;
import sh.isaac.misc.exporters.rf2.files.RF2ReleaseType;
import sh.isaac.misc.exporters.rf2.files.RF2Scope;
import sh.isaac.misc.exporters.rf2.files.StandardFiles;

import sh.isaac.utility.Frills;
import sh.isaac.utility.LanguageMap;

public class RF2Exporter extends TimedTaskWithProgressTracker<File> 
{
	Logger log = LogManager.getLogger(RF2Exporter.class);
	
	private final String ERROR_FILE_NAME = "Errors";
	
	final File outputFolder;
	final File packageFolder;
	final RF2FileFetcher ff;
	
	final boolean makeSnapshot;
	final boolean makeDelta;
	final boolean makeFull;
	final StampFilter coordinate;
	final int statedLogicGraphLookupNid;
	final AssemblageService as;
	final long deltaChangedAfter;
	
	final DateTimeFormatter effectiveTimeFormat = DateTimeFormatter.ofPattern("YYYYMMdd").withZone(ZoneId.systemDefault());
	
	final ConcurrentHashMap<Integer, String> sctidConstants = new ConcurrentHashMap<>();

	/**
	 * @param coordinate - specifies how to read the content to include.
	 * @param makeFull - create the 'Full' directory and contents
	 * @param makeSnapshot - create the 'Snapshot' directory and contents
	 * @param makeDelta - create the 'Delta' directory and contents (requires changedAfter)
	 * @param deltaChangedAfter - Timestamp to use as the start time for calculating the delta.
	 * @param product - Camel case short title sufficient to identify the product.  Used in the top folder name.  "FooExtension" for example
	 * @param scope - optional component of top folder name
	 * @param status - required component of top folder name
	 * @param countryNamespace 2 letter country code, or 7 digit namespace (or a combination of the two) used in the file name
	 * @param releaseDate - The package release date in YYYYMMDD format.
	 * @param releaseTime - The package release time in HHMMSS format.  Uses 120000 if not specified.
	 * @param releaseTimeZone - The package release time zone.  Uses 'Z' if not specified.
	 * @param versionDate - uses 'releaseDate' if not specified.  The YYYYMMDD value to use for individual files.
	 * @throws IOException 
	 */
	public RF2Exporter(StampFilter coordinate, boolean makeFull, boolean makeSnapshot, boolean makeDelta, Optional<Long> deltaChangedAfter,
			String product, Optional<RF2Scope> scope, RF2ReleaseStatus status, String countryNamespace, 
			String releaseDate, Optional<String> releaseTime, Optional<String> releaseTimeZone, Optional<String> versionDate) throws IOException
	{
		Path p = Paths.get(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
		outputFolder = p.toFile();
		outputFolder.mkdirs();
		if (!outputFolder.isDirectory())
		{
			throw new IOException("Don't seem to have access to the temp folder");
		}
		packageFolder = new File(outputFolder, "SnomedCT_" + product + (scope.isPresent() ? scope.get().toString() : "") + "RF2_" + status.toString() + "_" 
				+ releaseDate + "T" + releaseTime.orElse("120000") + releaseTimeZone.orElse("Z"));
		packageFolder.mkdir();
		
		this.makeDelta = makeDelta;
		this.makeSnapshot = makeSnapshot;
		this.makeFull = makeFull;
		this.coordinate = coordinate;
		this.statedLogicGraphLookupNid = Coordinates.Logic.ElPlusPlus().getStatedAssemblageNid();
		this.as = Get.assemblageService();
		this.deltaChangedAfter = (makeDelta ? deltaChangedAfter.get() : 0);
		
		ff = new RF2FileFetcher(packageFolder, countryNamespace, versionDate.orElse(releaseDate));
		log.debug("RF2Exporter launched - outputFolder: {}, packageFolder: {}, makeDelta: {}, makeSnapshot: {}, makeFull: {}, coordinate: {}, deltaChangedAfter: {}, {}",
				outputFolder.getAbsoluteFile().toString(), packageFolder.getAbsoluteFile().toString(), this.makeDelta, this.makeSnapshot, this.makeFull, 
				this.coordinate.toString(), this.deltaChangedAfter, ff.toString());
	}
	
	
	@Override
	public File call() throws Exception
	{
		try
		{
			log.debug("Writing concepts to RF2 structure");
			Get.conceptService().getConceptChronologyStream().parallel().forEach(cc ->
			{
				try
				{
					if (makeSnapshot)
					{
						LatestVersion<ConceptVersion> lv = cc.getLatestVersion(coordinate);
						if (lv.isPresent())
						{
							writeConcept(RF2ReleaseType.Snapshot, lv.get());
						}
					}
					if (makeFull || makeDelta)
					{
						List<ConceptVersion> allVersions = cc.getVersionList();
						
						Iterator<ConceptVersion> it = allVersions.iterator();
						while (it.hasNext())
						{
							ConceptVersion cv = it.next();
							if (!coordinate.getModuleNids().contains(cv.getModuleNid()) 
									|| coordinate.getStampPosition().getPathForPositionNid() != cv.getPathNid()
									|| coordinate.getStampPosition().getTime() <= cv.getTime())
							{
								//we don't write this one.
								it.remove();
							}
						}
						
						allVersions.sort(new Comparator<Version>()
						{
							@Override
							public int compare(Version o1, Version o2)
							{
								return Long.compare(o1.getTime(), o2.getTime());
							}
						});
						
						String next = null;
						for (int i = 0; i < allVersions.size(); i++)
						{
							//In RF2, we can only write one version per unique day, so in the event some item is modified
							//more than once in a day, only export the newest edit.
							if ((i + 1) < allVersions.size())
							{
								String current = next == null ? effectiveTimeFormat.format(Instant.ofEpochMilli(allVersions.get(i).getTime())) : next;
								next = effectiveTimeFormat.format(Instant.ofEpochMilli(allVersions.get(i + 1).getTime()));
								if (current.equals(next))
								{
									continue;
								}
							}
							if (makeFull)
							{
								writeConcept(RF2ReleaseType.Full, allVersions.get(i));
							}
							if (makeDelta)
							{
								if (allVersions.get(i).getTime() > deltaChangedAfter)
								{
									writeConcept(RF2ReleaseType.Delta, allVersions.get(i));
								}
							}
						}
					}
				}
				catch (Exception e)
				{
					log.error("Error writing concept " + cc + ":  ", e);
					try
					{
						ff.getLogFile(null, ERROR_FILE_NAME).writeLine("Error writing concept " + cc);
					}
					catch (IOException e1)
					{
						log.error("additional error while writin to rf2 error file", e1);
					}
				}
			});
			
			log.debug("Writing concepts to RF2 structure");
			Get.assemblageService().getSemanticChronologyStream().parallel().forEach(sc ->
			{
				try
				{
					if (makeSnapshot)
					{
						LatestVersion<SemanticVersion> lv = sc.getLatestVersion(coordinate);
						if (lv.isPresent())
						{
							writeSemantic(RF2ReleaseType.Snapshot, lv.get());
						}
					}
					if (makeFull || makeDelta)
					{
						List<SemanticVersion> allVersions = sc.getVersionList();
						
						Iterator<SemanticVersion> it = allVersions.iterator();
						while (it.hasNext())
						{
							SemanticVersion cv = it.next();
							if (!coordinate.getModuleNids().contains(cv.getModuleNid()) 
									|| coordinate.getStampPosition().getPathForPositionNid() != cv.getPathNid()
									|| coordinate.getStampPosition().getTime() <= cv.getTime())
							{
								//we don't write this one.
								it.remove();
							}
						}
						
						allVersions.sort(new Comparator<Version>()
						{
							@Override
							public int compare(Version o1, Version o2)
							{
								return Long.compare(o1.getTime(), o2.getTime());
							}
						});
						
						String next = null;
						for (int i = 0; i < allVersions.size(); i++)
						{
							//In RF2, we can only write one version per unique day, so in the event some item is modified
							//more than once in a day, only export the newest edit.
							if ((i + 1) < allVersions.size())
							{
								String current = next == null ? effectiveTimeFormat.format(Instant.ofEpochMilli(allVersions.get(i).getTime())) : next;
								next = effectiveTimeFormat.format(Instant.ofEpochMilli(allVersions.get(i + 1).getTime()));
								if (current.equals(next))
								{
									continue;
								}
							}
							if (makeFull)
							{
								writeSemantic(RF2ReleaseType.Full, allVersions.get(i));
							}
							if (makeDelta)
							{
								if (allVersions.get(i).getTime() > deltaChangedAfter)
								{
									writeSemantic(RF2ReleaseType.Delta, allVersions.get(i));
								}
							}
						}
					}
				}
				catch (Exception e)
				{
					log.error("Error writing semantic " + sc + ":  ", e);
					try
					{
						ff.getLogFile(null, ERROR_FILE_NAME).writeLine("Error writing semantic " +sc);
					}
					catch (IOException e1)
					{
						log.error("additional error while writin to rf2 error file", e1);
					}
				}
			});
			
			if (ff.getOpenFileCount() == 0)
			{
				log.info("Didn't export any content?");
				ff.writeTextFile("readme.txt", "No content was found to export with the provided criteria");
			}
			
			//Need to close prior to zip
			ff.closeAll();
			
			//TODO task progress updates throughout this class, also, tie to progress in zip portion here.
			Zip z = new Zip(new File(outputFolder, packageFolder.getName() + ".zip"), packageFolder);
			return z.addAllFilesInCommonRoot(true);
		}
		finally
		{
			ff.closeAll();
		}
	}


	private void writeSemantic(RF2ReleaseType releaseType, SemanticVersion semanticVersion) throws IOException
	{
		switch (semanticVersion.getSemanticType())
		{
			case COMPONENT_NID:
				//TODO[1] implement
				break;
			case DESCRIPTION:
				DescriptionVersion dv = (DescriptionVersion)semanticVersion;
				String descSCTID = getCachedSCTID(dv.getNid());
				String refConceptSCTID = getSCTID(semanticVersion.getReferencedComponentNid(), releaseType);
				String typeId = getSCTID(dv.getDescriptionTypeConceptNid(), releaseType);
				String caseSigId = getCachedSCTID(dv.getCaseSignificanceConceptNid());
				String languageCode = LanguageMap.conceptNidToIso639(dv.getLanguageConceptNid());
				RF2File descFile;
				if (dv.getDescriptionTypeConceptNid() == MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR.getNid())
				{
					descFile = ff.getTextDefinitionFile(releaseType, languageCode);
				}
				else 
				{
					descFile = ff.getDescriptionFile(releaseType, languageCode);
				}
				
				descFile.writeRow(new String[] {
						descSCTID,
						effectiveTimeFormat.format(Instant.ofEpochMilli(semanticVersion.getTime())),
						getActive(semanticVersion.getStatus()), 
						getModuleId(semanticVersion.getModuleNid()), 
						refConceptSCTID,
						languageCode,
						typeId,
						dv.getText(),
						caseSigId
				});
				
				break;
			case DYNAMIC:
				//TODO[1] implement
				break;
			case LOGIC_GRAPH:
				//TODO[1] implement
				break;
			case LONG:
				//TODO[1] implement
				break;
			case MEMBER:
				//TODO[1] implement
				break;
			case RF2_RELATIONSHIP:
				//TODO[1] implement
				break;
			case STRING:
				//TODO[1] implement
				break;
			case MEASURE_CONSTRAINTS:
			case IMAGE:
			case Int1_Int2_Str3_Str4_Str5_Nid6_Nid7:
			case Nid1_Int2:
			case Nid1_Int2_Str3_Str4_Nid5_Nid6:
			case Nid1_Nid2:
			case Nid1_Nid2_Int3:
			case Nid1_Nid2_Str3:
			case Nid1_Str2:
			case Str1_Nid2_Nid3_Nid4:
			case Str1_Str2:
			case Str1_Str2_Nid3_Nid4:
			case Str1_Str2_Nid3_Nid4_Nid5:
			case Str1_Str2_Str3_Str4_Str5_Str6_Str7:
			case CONCEPT:
			case UNKNOWN:
			default :
				log.debug("Unsupported semantic type being ignored by RF2 export");
				break;
			
		}
	}

	private void writeConcept(RF2ReleaseType releaseType, ConceptVersion cv) throws IOException
	{
		RF2File file = ff.getFile(releaseType, StandardFiles.Concept.name());
		
		Optional<Long> sctid = Frills.getSctId(cv.getNid(), coordinate);

		if (sctid.isEmpty())
		{
			ff.getLogFile(releaseType, ERROR_FILE_NAME).writeLine("No SCTID available on concept " + cv.getPrimordialUuid());
		}
		
		String id = sctid.isPresent() ? sctid.get().toString() : cv.getPrimordialUuid().toString();
		
		file.writeRow(new String[] {
				id,
				effectiveTimeFormat.format(Instant.ofEpochMilli(cv.getTime())),
				getActive(cv.getStatus()), 
				getModuleId(cv.getModuleNid()), 
				getDefinitionStatusId(cv)
				});
	}


	private String getDefinitionStatusId(ConceptVersion cv)
	{
		Optional<SemanticChronology> sc = as.getSemanticChronologyStreamForComponentFromAssemblage(cv.getNid(), statedLogicGraphLookupNid).findFirst();
		if (sc.isPresent())
		{
			//Was it defined at this time stamp...
			LatestVersion<LogicGraphVersion> lv = sc.get().getLatestVersion(coordinate.makeCoordinateAnalog(cv.getTime()));
			if (lv.isPresent())
			{
				if (Frills.isConceptFullyDefined(lv.get()))
				{
					getCachedSCTID(TermAux.SUFFICIENT_CONCEPT_DEFINITION.getNid());
				}
			}
		}
		return getCachedSCTID(TermAux.NECESSARY_BUT_NOT_SUFFICIENT_CONCEPT_DEFINITION.getNid());
	}
	
	/**
	 * DO NOT USE THIS FOR ALL SCTID LOOKUPS, only constants.
	 * @param nid
	 * @return
	 */
	private String getCachedSCTID(final int nid)
	{
		return sctidConstants.computeIfAbsent(nid, nidAgain -> 
		{
			return getSCTID(nid, null);
		});
	}
	
	private String getSCTID(final int nid, RF2ReleaseType rf2ReleaseType)
	{
		Optional<Long> sctid = Frills.getSctId(nid, coordinate);
		if (sctid.isEmpty())
		{
			UUID uuid = Get.identifierService().getUuidPrimordialForNid(nid);
			try
			{
				ff.getLogFile(rf2ReleaseType, ERROR_FILE_NAME).writeLine("No SCTID available on concept " + uuid);
			}
			catch (IOException e)
			{
				log.error("Unexpected error writing error to log", e);
			}
			return uuid.toString();
		}
		return sctid.get().toString();
	}



	private String getActive(Status status)
	{
		return status.isActive() ? "1" : "0";
	}


	private String getModuleId(int moduleNid)
	{
		int typeNid = Frills.getTerminologyTypeForModule(moduleNid, Coordinates.Filter.DevelopmentLatest());
		return getCachedSCTID(typeNid);
	}
}
