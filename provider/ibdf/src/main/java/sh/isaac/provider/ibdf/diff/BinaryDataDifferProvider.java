/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.provider.ibdf.diff;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvnet.hk2.annotations.Service;

import com.cedarsoftware.util.io.JsonWriter;

import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.commit.CommitService;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.externalizable.BinaryDataDifferService;
import sh.isaac.api.externalizable.BinaryDataReaderQueueService;
import sh.isaac.api.externalizable.BinaryDataReaderService;
import sh.isaac.api.externalizable.DataWriterService;
import sh.isaac.api.externalizable.IsaacExternalizable;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.externalizable.json.JsonDataWriterService;
import sh.isaac.api.util.UuidT5Generator;


/**
 * Routines enabling the examination of two ibdf files containing two distinct
 * versions of the same terminology and identifies the new/inactivated/modified
 * content between the two versions.
 * 
 * Once identified, a new changeset file may be generated containing these
 * changes. This file can then be imported into an existing database containing
 * the old version of the terminology. This will upgrade it to the new
 * terminology.
 * 
 * {@link BinaryDataDifferService}
 *
 * @author <a href="mailto:jefron@westcoastinformatics.com">Jesse Efron</a>
 */
@Service(name = "binary data differ")
@Singleton
// TODO [DAN 3] there are some serious thread-safety issues in this class, also stop/start issues.  also, why singleton?
public class BinaryDataDifferProvider implements BinaryDataDifferService {
	private final Logger log = LogManager.getLogger();
	private BinaryDataDifferProviderUtility diffUtil;

	// Stream hack
	private int conceptCount, semanticCount, itemCount, aliasCount, commentCount;

	// Analysis File Readers/Writers
	private String inputAnalysisDir;
	private String comparisonAnalysisDir;
	private String deltaIbdfPath;
	private UUID moduleUuid;
	private final String textInputFileName = "bothVersions.txt";
	private final String jsonFullComparisonFileName = "allChangedComponents.json";
	private final String textFullComparisonFileName = "allChangedComponents.txt";

	// Changeset File Writer
	private DataWriterService componentCSWriter = null;

	HashSet<Integer> skippedItems = new HashSet<>();

	public BinaryDataDifferProvider() {
		// For HK2
		log.info("binary data differ constructed");
	}

	@PostConstruct
	private void startMe() {
		log.info("Starting BinaryDataDifferProvider.");
	}

	@PreDestroy
	private void stopMe() {
		log.info("Stopping BinaryDataDifferProvider.");
	}

	@Override
	public Map<ChangeType, List<IsaacExternalizable>> computeDelta(
			Map<IsaacObjectType, Set<IsaacExternalizable>> oldContentMap,
			Map<IsaacObjectType, Set<IsaacExternalizable>> newContentMap) {
		List<IsaacExternalizable> addedComponents = new ArrayList<>();
		List<IsaacExternalizable> retiredComponents = new ArrayList<>();
		List<IsaacExternalizable> changedComponents = new ArrayList<>();
		CommitService commitService = Get.commitService();

		final int activeStampSeq = createStamp(Status.ACTIVE);
		final int inactiveStampSeq = createStamp(Status.INACTIVE);

		// Find existing
		for (IsaacObjectType type : IsaacObjectType.values()) {
			Set<UUID> matchedSet = new HashSet<UUID>();
			if (type != IsaacObjectType.CONCEPT && type != IsaacObjectType.SEMANTIC) {
				// Given using the IsaacObjectType.values()
				// collection, ensure only handling the supported types
				continue;
			}

			// Search for modified components
			for (IsaacExternalizable oldComp : oldContentMap.get(type)) {
				Chronology oldCompChron = (Chronology) oldComp;
				for (IsaacExternalizable newComp : newContentMap.get(type)) {
					Chronology newCompChron = (Chronology) newComp;

					if (oldCompChron.getPrimordialUuid().equals(newCompChron.getPrimordialUuid())) {
						matchedSet.add(oldCompChron.getPrimordialUuid());

						try {
							IsaacExternalizable modifiedComponents = diffUtil.diff(oldCompChron, newCompChron,
									activeStampSeq, type);
							if (modifiedComponents != null) {
								changedComponents.add(modifiedComponents);
							}
						} catch (Exception e) {
							log.error("Failed ON type: " + type + " on component: " + oldCompChron.getPrimordialUuid());
							e.printStackTrace();
						}

						continue;
					}
				}
			}

			// Add oldComps not in matchedSet
			for (IsaacExternalizable oldComp : oldContentMap.get(type)) {
				if (!matchedSet.contains(((Chronology) oldComp).getPrimordialUuid())) {
					diffUtil.addNewInactiveVersion(oldComp, oldComp.getIsaacObjectType(), inactiveStampSeq);
					retiredComponents.add(oldComp);
				}
			}

			// Add newComps not in matchedSet
			for (IsaacExternalizable newComp : newContentMap.get(type)) {
				if (!matchedSet.contains(((Chronology) newComp).getPrimordialUuid())) {

					IsaacExternalizable addedComp = diffUtil.diff(null, (Chronology) newComp, activeStampSeq,
							type);

					if (addedComp != null) {
						addedComponents.add(addedComp);
						commitService.importNoChecks(addedComp);
					}
				}
			}
			commitService.postProcessImportNoChecks();

		} // Close Type Loop

		Map<ChangeType, List<IsaacExternalizable>> retMap = new HashMap<>();
		retMap.put(ChangeType.NEW_COMPONENTS, addedComponents);
		retMap.put(ChangeType.RETIRED_COMPONENTS, retiredComponents);
		retMap.put(ChangeType.MODIFIED_COMPONENTS, changedComponents);

		return retMap;
	}

	@Override
	public void generateDeltaIbdfFile(Map<ChangeType, List<IsaacExternalizable>> changedComponents) throws IOException {
		componentCSWriter = Get.binaryDataWriter(new File(deltaIbdfPath).toPath());

		for (ChangeType key : changedComponents.keySet()) {
			for (IsaacExternalizable c : changedComponents.get(key)) {
				componentCSWriter.put(c);
			}
		}

		componentCSWriter.close();
	}

	@Override
	public void createAnalysisFiles(Map<IsaacObjectType, Set<IsaacExternalizable>> oldContentMap,
			Map<IsaacObjectType, Set<IsaacExternalizable>> newContentMap,
			Map<ChangeType, List<IsaacExternalizable>> changedComponents) {
		try {
			// Handle Input Files
			if (oldContentMap != null) {
				generateInputAnalysisFile(oldContentMap, "OLD", "oldVersion.json");
			} else {
				log.info("oldContentMap empty so not writing json/text Input files for old content");
			}

			if (newContentMap != null) {
				generateInputAnalysisFile(newContentMap, "New", "newVersion.json");
			} else {
				log.info("newContentMap empty so not writing json/text Input files for new content");
			}

			// Handle Comparison Files
			if (changedComponents != null) {
				generateComparisonAnalysisFile(changedComponents);
				writeChangeSetForVerification();
			} else {
				log.info("changedComponents empty so not writing json/text Output files");
			}

		} catch (IOException e) {
			log.error(
					"Failed in creating analysis files (not in processing the content written to the analysis files)");
		}
	}

	private void generateComparisonAnalysisFile(Map<ChangeType, List<IsaacExternalizable>> changedComponents)
			throws IOException {
		try (FileWriter allChangesTextWriter = new FileWriter(comparisonAnalysisDir + textFullComparisonFileName);
				JsonDataWriterService allChangesJsonWriter = new JsonDataWriterService(
						new File(comparisonAnalysisDir + jsonFullComparisonFileName));) {

			for (ChangeType key : changedComponents.keySet()) {
				int counter = 1;

				FileWriter changeTypeWriter = new FileWriter(comparisonAnalysisDir + key + "_File.txt");

				try {
					List<IsaacExternalizable> components = changedComponents.get(key);

					allChangesJsonWriter.put("\n\n\n\t\t\t**** " + key.toString() + " ****");
					allChangesTextWriter.write("\n\n\n\t\t\t**** " + key.toString() + " ****");

					for (IsaacExternalizable c : components) {
						String componentType;
						if (c.getIsaacObjectType() == IsaacObjectType.CONCEPT) {
							componentType = "Concept";
						} else {
							componentType = "Semantic";
						}

						String componentToWrite = "---- " + key.toString() + " " + componentType + " #" + counter++
								+ "   " + ((Chronology) c).getPrimordialUuid() + " ----\n";

						// Print Header
						allChangesJsonWriter.put(componentToWrite);
						allChangesTextWriter.write("\n\n\n\t\t\t" + componentToWrite);
						changeTypeWriter.write("\n\n\n\t\t\t" + componentToWrite);

						// Print Value (JSON Working TXT has issues)
						allChangesJsonWriter.put(c);

						try {
							changeTypeWriter.write(c.toString() + "\n\n\n");
						} catch (Exception e) {

						}

						try {
							allChangesTextWriter.write(c.toString());
						} catch (Exception e) {

						}
					}
				} catch (IOException e) {
					log.error("Failure processing changes of type " + key.toString());
				} finally {
					changeTypeWriter.close();
				}
			}
		}

	}

	/**
	 * Set up all the boilerplate stuff.
	 * 
	 * Create a stamp in current database... create seq... then when
	 * serializing, point it
	 * 
	 * @param state
	 *            - state or null (for current)
	 * @param time
	 *            - time or null (for default)
	 */
	private int createStamp(Status state) {
		return LookupService.getService(StampService.class).getStampSequence(state, diffUtil.getNewImportDate(),
				TermAux.USER.getNid(), // Author
				LookupService.getService(IdentifierService.class).getNidForUuids(moduleUuid), // Module
				TermAux.DEVELOPMENT_PATH.getNid()); // Path
	}

	@Override
	public void initialize(String comparisonAnalysisDir, String inputAnalysisDir, String deltaIbdfPathFile,
			Boolean generateAnalysisFiles, boolean diffOnStatus, boolean diffOnTimestamp, boolean diffOnAuthor,
			boolean diffOnModule, boolean diffOnPath, String importDate, String moduleToCreate) {
		diffUtil = new BinaryDataDifferProviderUtility(diffOnStatus, diffOnTimestamp, diffOnAuthor, diffOnModule,
				diffOnPath);
		diffUtil.setNewImportDate(importDate);

		this.moduleUuid = UuidT5Generator.get(UuidT5Generator.PATH_ID_FROM_FS_DESC, moduleToCreate);

		this.inputAnalysisDir = inputAnalysisDir;
		this.comparisonAnalysisDir = comparisonAnalysisDir;
		this.deltaIbdfPath = deltaIbdfPathFile;

		if (generateAnalysisFiles) {
			File f = new File(inputAnalysisDir);
			deleteDirectoryFiles(f);
			f.mkdirs();

			f = new File(comparisonAnalysisDir);
			deleteDirectoryFiles(f);
			f.mkdirs();
		}
	}

	@Override
	public Map<IsaacObjectType, Set<IsaacExternalizable>> processInputIbdfFile(File versionFile)
			throws Exception {
		log.info("Processing file: " + versionFile.getAbsolutePath());
		BinaryDataReaderService reader = Get.binaryDataReader(versionFile.toPath());

		itemCount = 0;
		conceptCount = 0;
		semanticCount = 0;
		aliasCount = 0;
		commentCount = 0;

		Map<IsaacObjectType, Set<IsaacExternalizable>> retMap = new HashMap<>();
		retMap.put(IsaacObjectType.CONCEPT, new HashSet<IsaacExternalizable>());
		retMap.put(IsaacObjectType.SEMANTIC, new HashSet<IsaacExternalizable>());
		retMap.put(IsaacObjectType.STAMP_ALIAS, new HashSet<IsaacExternalizable>());
		retMap.put(IsaacObjectType.STAMP_COMMENT, new HashSet<IsaacExternalizable>());
		try {
			reader.getStream().forEach((object) -> {
				if (object != null) {
					itemCount++;

					try {
						if (object.getIsaacObjectType() == IsaacObjectType.CONCEPT) {
							conceptCount++;
							retMap.get(object.getIsaacObjectType()).add(object);
						} else if (object.getIsaacObjectType() == IsaacObjectType.SEMANTIC) {
							semanticCount++;
							retMap.get(object.getIsaacObjectType()).add(object);
						} else if (object.getIsaacObjectType() == IsaacObjectType.STAMP_ALIAS) {
							aliasCount++;
							retMap.get(object.getIsaacObjectType()).add(object);
						} else if (object.getIsaacObjectType() == IsaacObjectType.STAMP_COMMENT) {
							commentCount++;
							retMap.get(object.getIsaacObjectType()).add(object);
						} else {
							throw new UnsupportedOperationException("Unknown ochre object type: " + object);
						}
					} catch (Exception e) {
						log.error("Failure at " + conceptCount + " concepts, " + semanticCount + " semantics, ", e);
						Map<String, Object> args = new HashMap<>();
						args.put(JsonWriter.PRETTY_PRINT, true);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						JsonWriter json = new JsonWriter(baos, args);

						UUID primordial = null;
						if (object instanceof Chronology) {
							primordial = ((Chronology) object).getPrimordialUuid();
						}

						json.write(object);
						log.error("Failed on "
								+ (primordial == null ? ": "
										: "object with primoridial UUID " + primordial.toString() + ": ")
								+ baos.toString());
						json.close();

					}

					if (itemCount % 10000 == 0) {
						log.info("Still processing ibdf file.  Status: " + itemCount + " entries, " + "Loaded "
								+ conceptCount + " concepts, " + semanticCount + " semantics, " + aliasCount + " aliases, "
								+ commentCount + " comments");
					}
				}
			});
		} catch (Exception ex) {
			log.info("Exception during load: Loaded " + conceptCount + " concepts, " + semanticCount + " semantics, "
					+ aliasCount + " aliases, " + commentCount + " comments"
					+ (skippedItems.size() > 0 ? ", skipped for inactive " + skippedItems.size() : ""));
			throw new Exception(ex.getLocalizedMessage(), ex);
		}

		log.info("Finished processing ibdf file.  Results: " + itemCount + " entries, " + "Loaded " + conceptCount
				+ " concepts, " + semanticCount + " semantics, " + aliasCount + " aliases, " + commentCount + " comments");

		return retMap;
	}

	private void writeChangeSetForVerification() throws FileNotFoundException {
		int ic = 0;
		int cc = 0;
		int sc = 0;

		BinaryDataReaderQueueService reader = Get.binaryDataQueueReader(new File(deltaIbdfPath).toPath());
		BlockingQueue<IsaacExternalizable> queue = reader.getQueue();

		Map<String, Object> args = new HashMap<>();
		args.put(JsonWriter.PRETTY_PRINT, true);

		try (FileOutputStream fos = new FileOutputStream(new File(comparisonAnalysisDir + "verificationChanges.json"));
				JsonWriter verificationWriter = new JsonWriter(fos, args);) {

			while (!queue.isEmpty() || !reader.isFinished()) {
				IsaacExternalizable object = queue.poll(500, TimeUnit.MILLISECONDS);
				if (object != null) {
					ic++;
					try {
						if (object.getIsaacObjectType() == IsaacObjectType.CONCEPT) {
							cc++;
							verificationWriter.write(object);
						} else if (object.getIsaacObjectType() == IsaacObjectType.SEMANTIC) {
							sc++;
							verificationWriter.write(object);
						} else {
							throw new UnsupportedOperationException("Unknown ochre object type: " + object);
						}
					} catch (Exception e) {
						log.error("Failure at " + ic + " items " + cc + " concepts, " + sc + " semantics, ", e);
						args = new HashMap<>();
						args.put(JsonWriter.PRETTY_PRINT, true);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						JsonWriter json = new JsonWriter(baos, args);

						UUID primordial = null;
						if (object instanceof Chronology) {
							primordial = ((Chronology) object).getPrimordialUuid();
						}

						json.write(object);
						log.error("Failed on "
								+ (primordial == null ? ": "
										: "object with primoridial UUID " + primordial.toString() + ": ")
								+ baos.toString());
						json.close();

					}

					if (ic % 10000 == 0) {
						log.info("Read " + ic + " entries, " + "Loaded " + cc + " concepts, " + sc + " semantics, ");
					}
				}
			}

		} catch (Exception ex) {
			log.info("Loaded " + ic + " items, " + cc + " concepts, " + sc + " semantics, "
					+ (skippedItems.size() > 0 ? ", skipped for inactive " + skippedItems.size() : ""));
		}

		log.info("Finished with " + ic + " items, " + cc + " concepts, " + sc + " semantics, "
				+ (skippedItems.size() > 0 ? ", skipped for inactive " + skippedItems.size() : ""));

	}

	private void generateInputAnalysisFile(Map<IsaacObjectType, Set<IsaacExternalizable>> contentMap,
			String version, String jsonInputFileName) throws IOException {

		int i = 1;

		try (FileWriter inputAnalysisTextWriter = new FileWriter(inputAnalysisDir + textInputFileName, true);
				JsonDataWriterService inputAnalysisJsonWriter = new JsonDataWriterService(
						new File(inputAnalysisDir + jsonInputFileName));) {

			inputAnalysisTextWriter.write("\n\n\n\n\n\n\t\t\t**** " + version + " LIST ****");
			inputAnalysisJsonWriter.put("\n\n\n\n\n\n\t\t\t**** " + version + " LIST ****");
			for (IsaacExternalizable component : contentMap.get(IsaacObjectType.CONCEPT)) {
				ConceptChronology cc = (ConceptChronology) component;
				inputAnalysisJsonWriter
						.put("#---- " + version + " Concept #" + i + "   " + cc.getPrimordialUuid() + " ----");
				inputAnalysisJsonWriter.put(cc);
				inputAnalysisTextWriter.write(
						"\n\n\n\t\t\t---- " + version + " Concept #" + i + "   " + cc.getPrimordialUuid() + " ----\n");
				inputAnalysisTextWriter.write(cc.toString());
				i++;
			}

			i = 1;

			for (IsaacExternalizable component : contentMap.get(IsaacObjectType.SEMANTIC)) {
				SemanticChronology se = (SemanticChronology) component;
				inputAnalysisJsonWriter
						.put("--- " + version + " Semantic #" + i + "   " + se.getPrimordialUuid() + " ----");
				inputAnalysisJsonWriter.put(se);

				try {
					inputAnalysisTextWriter.write("\n\n\n\t\t\t---- " + version + " Semantic #" + i + "   "
							+ se.getPrimordialUuid() + " ----\n");
					inputAnalysisTextWriter.write(se.toString());
				} catch (Exception e) {
					inputAnalysisTextWriter.write("Failure on TXT writing " + se.getVersionType() + " which is index: "
							+ i + " in writing *" + version + "* content to text file for analysis (UUID: "
							+ se.getPrimordialUuid() + ".");
				}
				i++;
			}
		} catch (Exception e) {
			log.error("Failure on writing index: " + i + " in writing *" + version
					+ "* content to text file for analysis.");
		}
	}

	// In case someone doesn't want to clean DB but still rerun operation to
	// gather some information, delete all comparison files
	private void deleteDirectoryFiles(File dir) {
		if (dir.isDirectory() == false) {
			return;
		}

		File[] listFiles = dir.listFiles();
		for (File file : listFiles) {
			file.delete();
		}

		// now directory is empty, so we can delete it
		dir.delete();
	}
}