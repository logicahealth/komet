package gov.vha.isaac.ochre.impl.utility;

import static gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder.And;
import static gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder.NecessarySet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvnet.hk2.annotations.Service;

import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.bootstrap.TermAux;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.api.commit.ChangeCheckerMode;
import gov.vha.isaac.ochre.api.component.concept.ConceptBuilder;
import gov.vha.isaac.ochre.api.component.concept.ConceptBuilderService;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptSpecification;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.component.concept.description.DescriptionBuilder;
import gov.vha.isaac.ochre.api.component.concept.description.DescriptionBuilderService;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.ComponentNidSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.LogicGraphSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeColumnUtility;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeUsageDescription;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeUtility;
import gov.vha.isaac.ochre.api.constants.DynamicSememeConstants;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.identity.StampedVersion;
import gov.vha.isaac.ochre.api.index.IndexServiceBI;
import gov.vha.isaac.ochre.api.index.SearchResult;
import gov.vha.isaac.ochre.api.logic.LogicNode;
import gov.vha.isaac.ochre.api.logic.LogicalExpression;
import gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder;
import gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilderService;
import gov.vha.isaac.ochre.api.logic.NodeSemantic;
import gov.vha.isaac.ochre.api.util.NumericUtils;
import gov.vha.isaac.ochre.api.util.UUIDUtil;
import gov.vha.isaac.ochre.model.configuration.EditCoordinates;
import gov.vha.isaac.ochre.model.configuration.LanguageCoordinates;
import gov.vha.isaac.ochre.model.configuration.LogicCoordinates;
import gov.vha.isaac.ochre.model.configuration.StampCoordinates;
import gov.vha.isaac.ochre.model.coordinate.StampCoordinateImpl;
import gov.vha.isaac.ochre.model.sememe.DynamicSememeUsageDescriptionImpl;
import gov.vha.isaac.ochre.model.sememe.version.StringSememeImpl;

@Service
@Singleton
public class Frills implements DynamicSememeColumnUtility {

	private static Logger log = LogManager.getLogger();

	public static Map<String, Object> getIdInfo(UUID id) {
		return getIdInfo(id.toString());
	}
	public static Map<String, Object> getIdInfo(int id) {
		return getIdInfo(Integer.toString(id));
	}
	public static Map<String, Object> getIdInfo(String id) {
		Map<String, Object> idInfo = new HashMap<>();

		Long sctId = null;
		Integer seq = null;
		Integer nid = null;
		UUID[] uuids = null;
		ObjectChronologyType typeOfPassedId = null;

		try {
			Optional<Integer> intId = NumericUtils.getInt(id);
			if (intId.isPresent())
			{
				// id interpreted as the id of the referenced component
				if (intId.get() > 0) {
					seq = intId.get();
					nid = Get.identifierService().getConceptNid(seq);
				} else if (intId.get() < 0) {
					nid = intId.get();
					seq = Get.identifierService().getConceptSequence(intId.get());
				}

				if (nid != null) {
					typeOfPassedId = Get.identifierService().getChronologyTypeForNid(nid);
					uuids = Get.identifierService().getUuidArrayForNid(nid);
				}
			}
			else
			{
				Optional<UUID> uuidId = UUIDUtil.getUUID(id);
				if (uuidId.isPresent())
				{
					// id interpreted as the id of either a sememe or a concept
					nid = Get.identifierService().getNidForUuids(uuidId.get());
					typeOfPassedId = Get.identifierService().getChronologyTypeForNid(nid);

					switch (typeOfPassedId) {
					case CONCEPT: {
						seq = Get.identifierService().getConceptSequenceForUuids(uuidId.get());
						break;
					}
					case SEMEME: {
						seq = Get.identifierService().getSememeSequenceForUuids(uuidId.get());
						break;
					}
					case UNKNOWN_NID:
					default:
					}
				}
			}

			if (nid != null) {
				idInfo.put("DESC", Get.conceptDescriptionText(nid));
				if (typeOfPassedId == ObjectChronologyType.CONCEPT) {
					Optional<Long> optSctId = Frills.getSctId(nid, StampCoordinates.getDevelopmentLatest());
					if (optSctId.isPresent()) {
						sctId = optSctId.get();
						
						idInfo.put("DEVLATEST_SCTID", sctId);
					}
				}
			}
		} catch (Exception e) {
			log.warn("Problem getting idInfo for \"" + id + "\". Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
		}
		idInfo.put("PASSED_ID", id);
		idInfo.put("SEQ", seq);
		idInfo.put("NID", nid);
		idInfo.put("UUIDs", Arrays.toString(uuids));
		idInfo.put("TYPE", typeOfPassedId);

		return idInfo;
	}
	/**
	 * @param lgs The LogicGraphSememe containing the logic graph data
	 * @return true if the corresponding concept is fully defined, otherwise returns false (for primitive concepts)
	 * 
	 * Things that are defined with a SUFFICIENT_SET are defined.
	 * Things that are defined with a NECESSARY_SET are primitive.
	 */
	public static <T extends LogicGraphSememe<T>> boolean isConceptFullyDefined(LogicGraphSememe<T> lgs) {
		LogicalExpression le = lgs.getLogicalExpression();
		LogicNode rootNode = le.getRoot();
		return rootNode.getChildren()[0].getNodeSemantic() == NodeSemantic.SUFFICIENT_SET;
	}

	/**
	 * @param id The int sequence or NID of the Concept for which the logic graph is requested
	 * @param stated boolean indicating stated vs inferred definition chronology should be used
	 * @return An Optional containing a LogicGraphSememe SememeChronology
	 */
	public static Optional<SememeChronology<? extends LogicGraphSememe<?>>> getLogicGraphChronology(int id, boolean stated)
	{
		log.debug("Getting " + (stated ? "stated" : "inferred") + " logic graph chronology for " + Frills.getIdInfo(id));
		Optional<SememeChronology<? extends SememeVersion<?>>> defChronologyOptional = stated ? Get.statedDefinitionChronology(id) : Get.inferredDefinitionChronology(id);
		if (defChronologyOptional.isPresent())
		{
			log.debug("Got " + (stated ? "stated" : "inferred") + " logic graph chronology for " + Frills.getIdInfo(id));

			@SuppressWarnings("unchecked")
			SememeChronology<? extends LogicGraphSememe<?>> sememeChronology = (SememeChronology<? extends LogicGraphSememe<?>>)defChronologyOptional.get();

			return Optional.of(sememeChronology);
		} else {
			log.warn("NO " + (stated ? "stated" : "inferred") + " logic graph chronology for " + Frills.getIdInfo(id));

			return Optional.empty();
		}
	}
	/**
	 * @param logicGraphSememeChronology The SememeChronology<? extends LogicGraphSememe<?>> chronology for which the logic graph version is requested
	 * @param stampCoordinate StampCoordinate to be used for selecting latest version
	 * @return An Optional containing a LogicGraphSememe SememeChronology
	 */
	public static Optional<LatestVersion<LogicGraphSememe<?>>> getLogicGraphVersion(SememeChronology<? extends LogicGraphSememe<?>> logicGraphSememeChronology, StampCoordinate stampCoordinate)
	{
		log.debug("Getting logic graph sememe for " + Frills.getIdInfo(logicGraphSememeChronology.getReferencedComponentNid()));

		@SuppressWarnings({ "unchecked", "rawtypes" })
		Optional<LatestVersion<LogicGraphSememe<?>>> latest = ((SememeChronology)logicGraphSememeChronology).getLatestVersion(LogicGraphSememe.class, stampCoordinate);
		if (latest.isPresent()) {
			log.debug("Got logic graph sememe for " + Frills.getIdInfo(logicGraphSememeChronology.getReferencedComponentNid()));
		} else {
			log.warn("NO logic graph sememe for " + Frills.getIdInfo(logicGraphSememeChronology.getReferencedComponentNid()));
		}
		return latest;
	}
	/**
	 *
	 * Determine if Chronology has nested sememes
	 *
	 * @param chronology
	 * @return true if there is a nested sememe, false otherwise
	 */
	public static boolean hasNestedSememe(ObjectChronology<?> chronology) {
		return !chronology.getSememeList().isEmpty();
	}

	/**
	 * Find the SCTID for a component (if it has one)
	 *
	 * @param componentNid
	 * @param stamp - optional - if not provided uses default from config
	 * service
	 * @return the id, if found, or empty (will not return null)
	 */
	public static Optional<Long> getSctId(int componentNid, StampCoordinate stamp) {
		try {
			Optional<LatestVersion<StringSememeImpl>> sememe = Get.sememeService().getSnapshot(StringSememeImpl.class,
					stamp == null ? Get.configurationService().getDefaultStampCoordinate() : stamp)
					.getLatestSememeVersionsForComponentFromAssemblage(componentNid,
							MetaData.SNOMED_INTEGER_ID.getConceptSequence()).findFirst();
			if (sememe.isPresent()) {
				return Optional.of(Long.parseLong(sememe.get().value().getString()));
			}
		} catch (Exception e) {
			log.error("Unexpected error trying to find SCTID for nid " + componentNid, e);
		}
		return Optional.empty();
	}

	/**
	 * Determine if a particular description sememe is flagged as preferred IN
	 * ANY LANGUAGE. Returns false if there is no acceptability sememe.
	 *
	 * @param descriptionSememeNid
	 * @param stamp - optional - if not provided, uses default from config
	 * service
	 * @throws RuntimeException If there is unexpected data (incorrectly)
	 * attached to the sememe
	 */
	public static boolean isDescriptionPreferred(int descriptionSememeNid, StampCoordinate stamp) throws RuntimeException {
		AtomicReference<Boolean> answer = new AtomicReference<>();

		//Ignore the language annotation... treat preferred in any language as good enough for our purpose here...
		Get.sememeService().getSememesForComponent(descriptionSememeNid).forEach(nestedSememe
				-> {
			if (nestedSememe.getSememeType() == SememeType.COMPONENT_NID) {
				@SuppressWarnings({"rawtypes", "unchecked"})
				Optional<LatestVersion<ComponentNidSememe>> latest = ((SememeChronology) nestedSememe).getLatestVersion(ComponentNidSememe.class,
						stamp == null ? Get.configurationService().getDefaultStampCoordinate() : stamp);

				if (latest.isPresent()) {
					if (latest.get().value().getComponentNid() == MetaData.PREFERRED.getNid()) {
						if (answer.get() != null && answer.get() != true) {
							throw new RuntimeException("contradictory annotations about preferred status!");
						}
						answer.set(true);
					} else if (latest.get().value().getComponentNid() == MetaData.ACCEPTABLE.getNid()) {
						if (answer.get() != null && answer.get() != false) {
							throw new RuntimeException("contradictory annotations about preferred status!");
						}
						answer.set(false);
					} else {
						throw new RuntimeException("Unexpected component nid!");
					}

				}
			}
		});
		if (answer.get() == null) {
			log.warn("Description nid {} does not have an acceptability sememe!", descriptionSememeNid);
			return false;
		}
		return answer.get();
	}

	/**
	 * Returns a Map correlating each dialect sequence for a passed
	 * descriptionSememeId with its respective acceptability nid (preferred vs
	 * acceptable)
	 *
	 * @param descriptionSememeNid
	 * @param stamp - optional - if not provided, uses default from config
	 * service
	 * @throws RuntimeException If there is inconsistent data (incorrectly)
	 * attached to the sememe
	 */
	public static Map<Integer, Integer> getAcceptabilities(int descriptionSememeNid, StampCoordinate stamp) throws RuntimeException {
		Map<Integer, Integer> dialectSequenceToAcceptabilityNidMap = new ConcurrentHashMap<>();

		Get.sememeService().getSememesForComponent(descriptionSememeNid).forEach(nestedSememe
				-> {
			if (nestedSememe.getSememeType() == SememeType.COMPONENT_NID) {
				int dialectSequence = nestedSememe.getAssemblageSequence();

				@SuppressWarnings({"rawtypes", "unchecked"})
				Optional<LatestVersion<ComponentNidSememe>> latest = ((SememeChronology) nestedSememe).getLatestVersion(ComponentNidSememe.class,
						stamp == null ? Get.configurationService().getDefaultStampCoordinate() : stamp);

				if (latest.isPresent()) {
					if (latest.get().value().getComponentNid() == MetaData.PREFERRED.getNid()
							|| latest.get().value().getComponentNid() == MetaData.ACCEPTABLE.getNid()) {
						if (dialectSequenceToAcceptabilityNidMap.get(dialectSequence) != null
								&& dialectSequenceToAcceptabilityNidMap.get(dialectSequence) != latest.get().value().getComponentNid()) {
							throw new RuntimeException("contradictory annotations about acceptability!");
						} else {
							dialectSequenceToAcceptabilityNidMap.put(dialectSequence, latest.get().value().getComponentNid());
						}
					} else {
						UUID uuid = null;
						String componentDesc = null;
						try {
							Optional<UUID> uuidOptional = Get.identifierService().getUuidPrimordialForNid(latest.get().value().getComponentNid());
							if (uuidOptional.isPresent()) {
								uuid = uuidOptional.get();
							}
							Optional<LatestVersion<DescriptionSememe<?>>> desc = Get.conceptService().getSnapshot(StampCoordinates.getDevelopmentLatest(), LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate()).getDescriptionOptional(latest.get().value().getComponentNid());
							componentDesc = desc.isPresent() ? desc.get().value().getText() : null;
						} catch (Exception e) {
							// NOOP
						}

						log.warn("Unexpected component " + componentDesc + " (uuid=" + uuid + ", nid=" + latest.get().value().getComponentNid() + ")");
						//throw new RuntimeException("Unexpected component " + componentDesc + " (uuid=" + uuid + ", nid=" + latest.get().value().getComponentNid() + ")");
						//dialectSequenceToAcceptabilityNidMap.put(dialectSequence, latest.get().value().getComponentNid());
					}
				}
			}
		});
		return dialectSequenceToAcceptabilityNidMap;
	}

	/**
	 * Convenience method to extract the latest version of descriptions of the
	 * requested type
	 *
	 * @param conceptNid The concept to read descriptions for
	 * @param descriptionType expected to be one of
	 * {@link MetaData#SYNONYM} or
	 * {@link MetaData#FULLY_SPECIFIED_NAME} or
	 * {@link MetaData#DEFINITION_DESCRIPTION_TYPE}
	 * @param stamp - optional - if not provided gets the default from the
	 * config service
	 * @return the descriptions - may be empty, will not be null
	 */
	public static List<DescriptionSememe<?>> getDescriptionsOfType(int conceptNid, ConceptSpecification descriptionType,
			StampCoordinate stamp) {
		ArrayList<DescriptionSememe<?>> results = new ArrayList<>();
		Get.sememeService().getSememesForComponent(conceptNid)
				.forEach(descriptionC
						-> {
					if (descriptionC.getSememeType() == SememeType.DESCRIPTION) {
						@SuppressWarnings({"unchecked", "rawtypes"})
						Optional<LatestVersion<DescriptionSememe<?>>> latest = ((SememeChronology) descriptionC).getLatestVersion(DescriptionSememe.class,
								stamp == null ? Get.configurationService().getDefaultStampCoordinate() : stamp);
						if (latest.isPresent()) {
							DescriptionSememe<?> ds = latest.get().value();
							if (ds.getDescriptionTypeConceptSequence() == descriptionType.getConceptSequence()) {
								results.add(ds);
							}
						}
					} else {
						log.warn("Description attached to concept nid {} is not of the expected type!", conceptNid);
					}
				});
		return results;
	}

	public static Optional<Integer> getNidForSCTID(long sctID) {
		IndexServiceBI si = LookupService.get().getService(IndexServiceBI.class, "sememe indexer");
		if (si != null) {
			//force the prefix algorithm, and add a trailing space - quickest way to do an exact-match type of search
			List<SearchResult> result = si.query(sctID + " ", true,
					new Integer[] {MetaData.SNOMED_INTEGER_ID.getConceptSequence()}, 5, Long.MIN_VALUE);
			if (result.size() > 0) {
				return Optional.of(Get.sememeService().getSememe(result.get(0).getNid()).getReferencedComponentNid());
			}
		} else {
			log.warn("Sememe Index not available - can't lookup SCTID");
		}
		return Optional.empty();
	}

	/**
	 * Convenience method to return sequences of a distinct set of modules in
	 * which versions of an ObjectChronology have been defined
	 *
	 * @param chronology The ObjectChronology
	 * @return sequences of a distinct set of modules in which versions of an
	 * ObjectChronology have been defined
	 */
	public static Set<Integer> getAllModuleSequences(ObjectChronology<? extends StampedVersion> chronology) {
		Set<Integer> moduleSequences = new HashSet<>();
		for (StampedVersion version : chronology.getVersionList()) {
			moduleSequences.add(version.getModuleSequence());
		}

		return Collections.unmodifiableSet(moduleSequences);
	}

	public static StampCoordinate makeStampCoordinateAnalogVaryingByModulesOnly(StampCoordinate existingStampCoordinate, int requiredModuleSequence, int... optionalModuleSequences) {
		ConceptSequenceSet moduleSequenceSet = new ConceptSequenceSet();
		moduleSequenceSet.add(requiredModuleSequence);
		if (optionalModuleSequences != null) {
			for (int seq : optionalModuleSequences) {
				moduleSequenceSet.add(seq);
			}
		}

		EnumSet<State> allowedStates = EnumSet.allOf(State.class);
		allowedStates.addAll(existingStampCoordinate.getAllowedStates());
		StampCoordinate newStampCoordinate = new StampCoordinateImpl(
				existingStampCoordinate.getStampPrecedence(),
				existingStampCoordinate.getStampPosition(),
				moduleSequenceSet, allowedStates);

		return newStampCoordinate;
	}

	public static void refreshIndexes() {
		LookupService.get().getAllServiceHandles(IndexServiceBI.class).forEach(index
				-> {
			//Making a query, with long.maxValue, causes the index to refresh itself, and look at the latest updates, if there have been updates.
			index.getService().query("hi", null, 1, Long.MAX_VALUE);
		});
	}
	
	/**
	 * Get isA children of a concept.  Does not return the requested concept in any circumstance.
	 * @param conceptSequence The concept to look at
	 * @param recursive recurse down from the concept
	 * @param leafOnly only return leaf nodes
	 * @return the set of concept sequence ids that represent the children
	 */
	public static Set<Integer> getAllChildrenOfConcept(int conceptSequence, boolean recursive, boolean leafOnly)
	{
		Set<Integer> temp = getAllChildrenOfConcept(new HashSet<Integer>(), conceptSequence, recursive, leafOnly);
		if (leafOnly && temp.size() == 1)
		{
			temp.remove(conceptSequence);
		}
		return temp;
	}
	
	/**
	 * Recursively get Is a children of a concept.  May inadvertenly return the requested starting sequence when leafOnly is true, and 
	 * there are no children.
	 */
	private static Set<Integer> getAllChildrenOfConcept(Set<Integer> handledConceptSequenceIds, int conceptSequence, boolean recursive, boolean leafOnly)
	{
		Set<Integer> results = new HashSet<>();
		
		// This both prevents infinite recursion and avoids processing or returning of duplicates
		if (handledConceptSequenceIds.contains(conceptSequence)) {
			return results;
		}

		AtomicInteger count = new AtomicInteger();
		IntStream children = Get.taxonomyService().getTaxonomyChildSequences(conceptSequence);

		children.forEach((conSequence) ->
		{
			count.getAndIncrement();
			if (!leafOnly)
			{
				results.add(conSequence);
			}
			if (recursive)
			{
				results.addAll(getAllChildrenOfConcept(handledConceptSequenceIds, conSequence, recursive, leafOnly));
			}
		});
		
		
		if (leafOnly && count.get() == 0)
		{
			results.add(conceptSequence);
		}
		handledConceptSequenceIds.add(conceptSequence);
		return results;
	}
	
	/**
	 * Create a new concept using the provided columnName and columnDescription values which is suitable 
	 * for use as a column descriptor within {@link DynamicSememeUsageDescription}.
	 * 
	 * The new concept will be created under the concept {@link DynamicSememeConstants#DYNAMIC_SEMEME_COLUMNS}
	 * 
	 * A complete usage pattern (where both the refex assemblage concept and the column name concept needs
	 * to be created) would look roughly like this:
	 * 
	 * DynamicSememeUtility.createNewDynamicSememeUsageDescriptionConcept(
	 *	 "The name of the Sememe", 
	 *	 "The description of the Sememe",
	 *	 new DynamicSememeColumnInfo[]{new DynamicSememeColumnInfo(
	 *		 0,
	 *		 DynamicSememeColumnInfo.createNewDynamicSememeColumnInfoConcept(
	 *			 "column name",
	 *			 "column description"
	 *			 )
	 *		 DynamicSememeDataType.STRING,
	 *		 new DynamicSememeStringImpl("default value")
	 *		 )}
	 *	 )
	 * 
	 * //TODO [REFEX] figure out language details (how we know what language to put on the name/description
	 * @throws RuntimeException 
	 */
	
	public static ConceptChronology<? extends ConceptVersion<?>> createNewDynamicSememeColumnInfoConcept(String columnName, String columnDescription) 
			throws RuntimeException
	{
		if (columnName == null || columnName.length() == 0 || columnDescription == null || columnDescription.length() == 0)
		{
			throw new RuntimeException("Both the column name and column description are required");
		}
		
		ConceptBuilderService conceptBuilderService = LookupService.getService(ConceptBuilderService.class);
		conceptBuilderService.setDefaultLanguageForDescriptions(MetaData.ENGLISH_LANGUAGE);
		conceptBuilderService.setDefaultDialectAssemblageForDescriptions(MetaData.US_ENGLISH_DIALECT);
		conceptBuilderService.setDefaultLogicCoordinate(LogicCoordinates.getStandardElProfile());

		DescriptionBuilderService descriptionBuilderService = LookupService.getService(DescriptionBuilderService.class);
		LogicalExpressionBuilder defBuilder = LookupService.getService(LogicalExpressionBuilderService.class).getLogicalExpressionBuilder();

		NecessarySet(And(ConceptAssertion(Get.conceptService().getConcept(DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMNS.getNid()), defBuilder)));

		LogicalExpression parentDef = defBuilder.build();

		ConceptBuilder builder = conceptBuilderService.getDefaultConceptBuilder(columnName, null, parentDef);

		DescriptionBuilder<?, ?> definitionBuilder = descriptionBuilderService.getDescriptionBuilder(columnName, builder,
						MetaData.SYNONYM,
						MetaData.ENGLISH_LANGUAGE);

		definitionBuilder.setPreferredInDialectAssemblage(MetaData.US_ENGLISH_DIALECT);
		builder.addDescription(definitionBuilder);
		
		definitionBuilder = descriptionBuilderService.getDescriptionBuilder(columnDescription, builder, MetaData.DEFINITION_DESCRIPTION_TYPE,
				MetaData.ENGLISH_LANGUAGE);
		definitionBuilder.setPreferredInDialectAssemblage(MetaData.US_ENGLISH_DIALECT);
		builder.addDescription(definitionBuilder);

		ConceptChronology<? extends ConceptVersion<?>> newCon = builder.build(EditCoordinates.getDefaultUserMetadata(), ChangeCheckerMode.ACTIVE, new ArrayList<>());

		Get.commitService().addUncommitted(newCon);

		Get.commitService().commit("creating new dynamic sememe column: " + columnName);
		return newCon;
	}
	
	/**
	 * See {@link DynamicSememeUsageDescription} for the full details on what this builds.
	 * 
	 * Does all the work to create a new concept that is suitable for use as an Assemblage Concept for a new style Dynamic Sememe.
	 * 
	 * The concept will be created under the concept {@link DynamicSememeConstants#DYNAMIC_SEMEME_ASSEMBLAGES} if a parent is not specified
	 * 
	 * //TODO [REFEX] figure out language details (how we know what language to put on the name/description
	 * @param sememePreferredTerm - The preferred term for this refex concept that will be created.
	 * @param sememeDescription - A user friendly string the explains the overall intended purpose of this sememe (what it means, what it stores)
	 * @param columns - The column information for this new refex.  May be an empty list or null.
	 * @param parentConceptNidOrSequence  - optional - if null, uses {@link DynamicSememeConstants#DYNAMIC_SEMEME_ASSEMBLAGES}
	 * @param referencedComponentRestriction - optional - may be null - if provided - this restricts the type of object referenced by the nid or 
	 * UUID that is set for the referenced component in an instance of this sememe.  If {@link ObjectChronologyType#UNKNOWN_NID} is passed, it is ignored, as 
	 * if it were null.
	 * @param referencedComponentSubRestriction - optional - may be null - subtype restriction for {@link ObjectChronologyType#SEMEME} restrictions
	 * @return a reference to the newly created sememe item
	 */
	public static DynamicSememeUsageDescription createNewDynamicSememeUsageDescriptionConcept(String sememeFSN, String sememePreferredTerm, 
			String sememeDescription, DynamicSememeColumnInfo[] columns, Integer parentConceptNidOrSequence, ObjectChronologyType referencedComponentRestriction,
			SememeType referencedComponentSubRestriction)
	{

		ConceptBuilderService conceptBuilderService = LookupService.getService(ConceptBuilderService.class);
		conceptBuilderService.setDefaultLanguageForDescriptions(MetaData.ENGLISH_LANGUAGE);
		conceptBuilderService.setDefaultDialectAssemblageForDescriptions(MetaData.US_ENGLISH_DIALECT);
		conceptBuilderService.setDefaultLogicCoordinate(LogicCoordinates.getStandardElProfile());

		DescriptionBuilderService descriptionBuilderService = LookupService.getService(DescriptionBuilderService.class);
		LogicalExpressionBuilder defBuilder = LookupService.getService(LogicalExpressionBuilderService.class).getLogicalExpressionBuilder();

		ConceptChronology<?> parentConcept =  Get.conceptService().getConcept(parentConceptNidOrSequence == null ? 
				DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSEMBLAGES.getNid() 
				: parentConceptNidOrSequence);
		
		NecessarySet(And(ConceptAssertion(parentConcept, defBuilder)));

		LogicalExpression parentDef = defBuilder.build();

		ConceptBuilder builder = conceptBuilderService.getDefaultConceptBuilder(sememeFSN, null, parentDef);

		DescriptionBuilder<?, ?> definitionBuilder = descriptionBuilderService.getDescriptionBuilder(sememePreferredTerm, builder,
						MetaData.SYNONYM,
						MetaData.ENGLISH_LANGUAGE);
		definitionBuilder.setPreferredInDialectAssemblage(MetaData.US_ENGLISH_DIALECT);
		builder.addDescription(definitionBuilder);
		
		ConceptChronology<? extends ConceptVersion<?>> newCon = builder.build(EditCoordinates.getDefaultUserMetadata(), ChangeCheckerMode.ACTIVE, new ArrayList<>());
		Get.commitService().addUncommitted(newCon);
		
		{
			//Set up the dynamic sememe 'special' definition
			definitionBuilder = descriptionBuilderService.getDescriptionBuilder(sememeDescription, builder, MetaData.DEFINITION_DESCRIPTION_TYPE,
					MetaData.ENGLISH_LANGUAGE);
			definitionBuilder.setPreferredInDialectAssemblage(MetaData.US_ENGLISH_DIALECT);
			@SuppressWarnings("rawtypes")
			SememeChronology definitonSememe = (SememeChronology) definitionBuilder.build(EditCoordinates.getDefaultUserMetadata(), ChangeCheckerMode.ACTIVE);
			Get.commitService().addUncommitted(definitonSememe);
			
			SememeChronology<? extends SememeVersion<?>> sememe = Get.sememeBuilderService().getDynamicSememeBuilder(definitonSememe.getNid(), 
					DynamicSememeConstants.get().DYNAMIC_SEMEME_DEFINITION_DESCRIPTION.getSequence(), null).build(EditCoordinates.getDefaultUserMetadata(), ChangeCheckerMode.ACTIVE);
			Get.commitService().addUncommitted(sememe);
		}

		if (columns != null)
		{
			//Ensure that we process in column order - we don't always keep track of that later - we depend on the data being stored in the right order.
			TreeSet<DynamicSememeColumnInfo> sortedColumns = new TreeSet<>(Arrays.asList(columns));
			
			for (DynamicSememeColumnInfo ci : sortedColumns)
			{
				DynamicSememeData[] data = LookupService.getService(DynamicSememeUtility.class).configureDynamicSememeDefinitionDataForColumn(ci);

				SememeChronology<? extends SememeVersion<?>> sememe = Get.sememeBuilderService().getDynamicSememeBuilder(newCon.getNid(), 
						DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getSequence(), data)
					.build(EditCoordinates.getDefaultUserMetadata(), ChangeCheckerMode.ACTIVE);
				Get.commitService().addUncommitted(sememe);
			}
		}
		
		DynamicSememeData[] data = LookupService.getService(DynamicSememeUtility.class).
				configureDynamicSememeRestrictionData(referencedComponentRestriction, referencedComponentSubRestriction);
		
		if (data != null)
		{
			SememeChronology<? extends SememeVersion<?>> sememe = Get.sememeBuilderService().getDynamicSememeBuilder(newCon.getNid(), 
					DynamicSememeConstants.get().DYNAMIC_SEMEME_REFERENCED_COMPONENT_RESTRICTION.getSequence(), data)
				.build(EditCoordinates.getDefaultUserMetadata(), ChangeCheckerMode.ACTIVE);
			Get.commitService().addUncommitted(sememe);
		}

		Get.commitService().commit("creating new dynamic sememe assemblage: " + sememeFSN);
		return new DynamicSememeUsageDescriptionImpl(newCon.getNid());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String[] readDynamicSememeColumnNameDescription(UUID columnDescriptionConcept)
	{
		String columnName = null;
		String columnDescription = null;
		String fsn = null;
		String acceptableSynonym = null;
		String acceptableDefinition = null;
		try
		{
			ConceptChronology<? extends ConceptVersion<?>> cc = Get.conceptService().getConcept(columnDescriptionConcept);
			for (SememeChronology<? extends DescriptionSememe<?>> dc : cc.getConceptDescriptionList())
			{
				if (columnName != null && columnDescription != null)
				{
					break;
				}
				
				@SuppressWarnings("rawtypes")
				Optional<LatestVersion<DescriptionSememe<?>>> descriptionVersion = ((SememeChronology)dc)
						.getLatestVersion(DescriptionSememe.class, Get.configurationService().getDefaultStampCoordinate());
				
				if (descriptionVersion.isPresent())
				{
					DescriptionSememe<?> d = descriptionVersion.get().value();
					if (d.getDescriptionTypeConceptSequence() == TermAux.FULLY_SPECIFIED_DESCRIPTION_TYPE.getConceptSequence())
					{
						fsn = d.getText();
					}
					else if (d.getDescriptionTypeConceptSequence() == TermAux.SYNONYM_DESCRIPTION_TYPE.getConceptSequence())
					{
						if (Frills.isDescriptionPreferred(d.getNid(), null))
						{
							columnName = d.getText();
						}
						else
						{
							acceptableSynonym = d.getText();
						}
					}
					else if (d.getDescriptionTypeConceptSequence() == TermAux.DEFINITION_DESCRIPTION_TYPE.getConceptSequence())
					{
						if (Frills.isDescriptionPreferred(d.getNid(), null))
						{
							columnDescription = d.getText();
						}
						else
						{
							acceptableDefinition = d.getText();
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			log.warn("Failure reading DynamicSememeColumnInfo '" + columnDescriptionConcept + "'", e);
		}
		if (columnName == null)
		{
			log.warn("No preferred synonym found on '" + columnDescriptionConcept + "' to use " + "for the column name - using FSN");
			columnName = (fsn == null ? "ERROR - see log" : fsn);
		}
		
		if (columnDescription == null && acceptableDefinition != null)
		{
			columnDescription = acceptableDefinition;
		}
		
		if (columnDescription == null && acceptableSynonym != null)
		{
			columnDescription = acceptableSynonym;
		}
		
		if (columnDescription == null)
		{
			log.info("No preferred or acceptable definition or acceptable synonym found on '" 
					+ columnDescriptionConcept + "' to use for the column description- re-using the the columnName, instead.");
			columnDescription = columnName;
		}
		return new String[] {columnName, columnDescription};
	}
}
