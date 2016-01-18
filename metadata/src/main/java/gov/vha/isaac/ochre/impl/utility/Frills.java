package gov.vha.isaac.ochre.impl.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.chronicle.StampedVersion;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.api.component.concept.ConceptSpecification;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.ComponentNidSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.index.IndexServiceBI;
import gov.vha.isaac.ochre.api.index.SearchResult;
import gov.vha.isaac.ochre.model.configuration.LanguageCoordinates;
import gov.vha.isaac.ochre.model.configuration.StampCoordinates;
import gov.vha.isaac.ochre.model.coordinate.StampCoordinateImpl;
import gov.vha.isaac.ochre.model.sememe.version.StringSememeImpl;

public class Frills {

    private static Logger log = LogManager.getLogger();

    /**
     *
     * Determine if Chronology has nested sememes
     *
     * @param chronology
     * @return
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
        Get.sememeService().getSememesForComponentFromAssemblage(conceptNid, MetaData.DESCRIPTION_ASSEMBLAGE.getConceptSequence())
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
                    MetaData.SNOMED_INTEGER_ID.getConceptSequence(), 5, Long.MIN_VALUE);
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
}
