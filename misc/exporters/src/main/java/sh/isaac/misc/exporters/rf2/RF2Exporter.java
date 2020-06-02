package sh.isaac.misc.exporters.rf2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.Zip;
import sh.isaac.misc.exporters.rf2.files.RF2File;
import sh.isaac.misc.exporters.rf2.files.RF2FileFetcher;
import sh.isaac.misc.exporters.rf2.files.RF2ReleaseStatus;
import sh.isaac.misc.exporters.rf2.files.RF2ReleaseType;
import sh.isaac.misc.exporters.rf2.files.RF2Scope;
import sh.isaac.misc.exporters.rf2.files.StandardFiles;
import sh.isaac.model.configuration.LogicCoordinates;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.utility.Frills;

public class RF2Exporter extends TimedTaskWithProgressTracker<File> 
{
	Logger log = LogManager.getLogger(RF2Exporter.class);
	
	final File outputFolder;
	final File packageFolder;
	final RF2FileFetcher ff;
	
	final boolean makeSnapshot;
	final boolean makeDelta;
	final boolean makeFull;
	final StampCoordinate coordinate;
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
	public RF2Exporter(StampCoordinate coordinate, boolean makeFull, boolean makeSnapshot, boolean makeDelta, Optional<Long> deltaChangedAfter,
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
		this.statedLogicGraphLookupNid = LogicCoordinates.getStandardElProfile().getStatedAssemblageNid();
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
							if (coordinate.getModuleNids().contains(allVersions.get(i).getModuleNid()) 
									&& coordinate.getStampPosition().getPathNid() == allVersions.get(i).getPathNid()
									&& coordinate.getStampPosition().getTime() > allVersions.get(i).getTime())
							{
								//In RF2, we can only write one version per unique day, so in the event some item is modified
								//more than once in a day, only export the newest edit.
								if ((i + 1) < allVersions.size())
								{
									//TODO [1] this is wrong, it needs to take into account the filter above
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
				}
				catch (Exception e)
				{
					throw new RuntimeException(e);
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


	private void writeConcept(RF2ReleaseType releaseType, ConceptVersion cv) throws IOException
	{
		RF2File file = ff.getFile(releaseType, StandardFiles.Concept.name());
		
		file.writeRow(new String[] {
				Frills.getSctId(cv.getNid(), coordinate)
					.orElseGet(() -> 
					{
						//TODO [1] create a mechanism to return / log RF2 creation errors with the RF2
						log.warn("No SCTID available on conept " + cv.getPrimordialUuid());
						return 0l;
					}) + "",
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
			return Frills.getSctId(nid, null).orElseGet(() ->
			{
				//TODO [1] proper handling of this
				log.error("Missing constant SCTID on {}", Get.identifiedObjectService().getChronology(nid));
				return 0l;
				
			}).toString();
		});
	}


	private String getActive(Status status)
	{
		return status.isActive() ? "1" : "0";
	}


	private String getModuleId(int moduleNid)
	{
		int typeNid = Frills.getTerminologyTypeForModule(moduleNid, StampCoordinates.getDevelopmentLatest());
		return getCachedSCTID(typeNid);
	}
}
