package sh.isaac.api.coordinate;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.eclipse.collections.impl.map.mutable.primitive.MutableIntObjectMapFactoryImpl;
import org.eclipse.collections.impl.set.mutable.primitive.MutableIntSetFactoryImpl;
import org.jvnet.hk2.annotations.Service;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.inject.Singleton;
import sh.isaac.api.Get;
import sh.isaac.api.LanguageCoordinateService;
import sh.isaac.api.StaticIsaacCache;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.commit.ChronologyChangeListener;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DynamicVersion;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicUUID;
import sh.isaac.api.constants.DynamicConstants;

//Even though this class is static, needs to be a service, so that the reset() gets fired at appropriate times.
@Service
@Singleton
public class Coordinates implements StaticIsaacCache {

    private static final Logger LOG = LogManager.getLogger();
    
    private static ChronologyChangeListener ccl;

    private static final Cache<Integer, int[]> LANG_EXPAND_CACHE = Caffeine.newBuilder().maximumSize(100).build();
    
    public static class  Edit {
        public static EditCoordinateImmutable Default() {
            return EditCoordinateImmutable.make(
            TermAux.USER.getNid(), TermAux.SOLOR_OVERLAY_MODULE.getNid(),
                TermAux.DEVELOPMENT_PATH.getNid(),
                TermAux.SOLOR_OVERLAY_MODULE.getNid()
            );
        }
    }

    public static class Logic {
        public static LogicCoordinateImmutable ElPlusPlus() {
            return LogicCoordinateImmutable.make(TermAux.SNOROCKET_CLASSIFIER,
                    TermAux.EL_PLUS_PLUS_LOGIC_PROFILE,
                    TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE,
                    TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE,
                    TermAux.SOLOR_CONCEPT_ASSEMBLAGE,
                    TermAux.EL_PLUS_PLUS_DIGRAPH,
                    TermAux.SOLOR_ROOT);
        }
    }

    public static class Language {
        /**
         * A coordinate that completely ignores language - descriptions ranked by this coordinate will only be ranked by
         * description type and module preference.  This coordinate is primarily useful as a fallback coordinate for the final
         * {@link LanguageCoordinate#getNextPriorityLanguageCoordinate()} in a chain
         *
         * See {@link LanguageCoordinateService#getSpecifiedDescription(StampFilter, List, LanguageCoordinate)}
         * @param regularNameOnly if true,  only return regularname.  If false, prefer regular name, but will
         *     return a FQN or definition if regular name isn't available. 
         * @return the language coordinate
         *
         */
        public static LanguageCoordinateImmutable AnyLanguageRegularName(boolean regularNameOnly) {
            return LanguageCoordinateImmutable.make(
                    TermAux.LANGUAGE,
                    regularNameOnly ? IntLists.immutable.of(expandDescriptionTypePreferenceList(null, TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid()))
                        : IntLists.immutable.of(expandDescriptionTypePreferenceList(null, TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid(),
                            TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid(), TermAux.DEFINITION_DESCRIPTION_TYPE.getNid())),
                    IntLists.immutable.empty(),
                    IntLists.immutable.empty()
            );
        }

        /**
         * A coordinate that completely ignores language - descriptions ranked by this coordinate will only be ranked by
         * description type and module preference.  This coordinate is primarily useful as a fallback coordinate for the final
         * {@link LanguageCoordinate#getNextPriorityLanguageCoordinate()} in a chain
         *
         * See {@link LanguageCoordinateService#getSpecifiedDescription(StampFilter, List, LanguageCoordinate)}
         * @param fqnOnly if true,  only return fully qualified name.  If false, prefer fully qualified name, but will
         *     return a regular name or definition if fqn name isn't available. 
         * @return the language coordinate
         */
        public static LanguageCoordinateImmutable AnyLanguageFullyQualifiedName(boolean fqnOnly) {
            return LanguageCoordinateImmutable.make(
                    TermAux.LANGUAGE,
                    fqnOnly ? IntLists.immutable.of(expandDescriptionTypePreferenceList(null, TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid()))
                            : IntLists.immutable.of(expandDescriptionTypePreferenceList(null, TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid(),
                                TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid(), TermAux.DEFINITION_DESCRIPTION_TYPE.getNid())),
                    IntLists.immutable.empty(),
                    IntLists.immutable.empty()
            );
        }

        /**
         * A coordinate that completely ignores language - descriptions ranked by this coordinate will only be ranked by
         * description type and module preference.  This coordinate is primarily useful as a fallback coordinate for the final
         * {@link LanguageCoordinate#getNextPriorityLanguageCoordinate()} in a chain
         *
         * See {@link LanguageCoordinateService#getSpecifiedDescription(StampFilter, List, LanguageCoordinate)}
         * @param defOnly if true,  only return definition name.  If false, prefer definition name, but will
         *     return a regular name or fqn if definition name isn't available. 
         * @return a coordinate that prefers definitions, of arbitrary language.
         * type
         */
        public static LanguageCoordinateImmutable AnyLanguageDefinition(boolean defOnly) {
            return LanguageCoordinateImmutable.make(
                    TermAux.LANGUAGE,
                    defOnly ? IntLists.immutable.of(expandDescriptionTypePreferenceList(null, TermAux.DEFINITION_DESCRIPTION_TYPE.getNid()))
                            : IntLists.immutable.of(expandDescriptionTypePreferenceList(null, TermAux.DEFINITION_DESCRIPTION_TYPE.getNid(),
                                TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid(), TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid())),
                    IntLists.immutable.empty(),
                    IntLists.immutable.empty()
            );
        }

        /**
         * @return US English language coordinate, preferring FQNs, but allowing regular names, if no FQN is found.
         */
        public static LanguageCoordinateImmutable UsEnglishFullyQualifiedName() {
            return LanguageCoordinateImmutable.make(
                    TermAux.ENGLISH_LANGUAGE.getNid(),
                    IntLists.immutable.of(expandDescriptionTypePreferenceList(null, TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid(),
                            TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid())),
                    IntLists.immutable.of(TermAux.US_DIALECT_ASSEMBLAGE.getNid(), TermAux.GB_DIALECT_ASSEMBLAGE.getNid()),
                    IntLists.immutable.of(TermAux.SCT_CORE_MODULE.getNid(), TermAux.SOLOR_OVERLAY_MODULE.getNid(), TermAux.SOLOR_MODULE.getNid()),
                    AnyLanguageFullyQualifiedName(false)
            );
        }

        /**
         * @return US English language coordinate, preferring regular name, but allowing FQN names is no regular name is found
         */
        public static LanguageCoordinateImmutable UsEnglishRegularName() {
            return LanguageCoordinateImmutable.make(
                    TermAux.ENGLISH_LANGUAGE.getNid(),
                    IntLists.immutable.of(expandDescriptionTypePreferenceList(null, TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid(),
                            TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid())),
                    IntLists.immutable.of(TermAux.US_DIALECT_ASSEMBLAGE.getNid(), TermAux.GB_DIALECT_ASSEMBLAGE.getNid()),
                    IntLists.immutable.of(TermAux.SCT_CORE_MODULE.getNid(), TermAux.SOLOR_OVERLAY_MODULE.getNid(), TermAux.SOLOR_MODULE.getNid()),
                    AnyLanguageRegularName(false)
            );
        }

        public static LanguageCoordinateImmutable GbEnglishFullyQualifiedName() {
            return LanguageCoordinateImmutable.make(
                    TermAux.ENGLISH_LANGUAGE.getNid(),
                    IntLists.immutable.of(expandDescriptionTypePreferenceList(null, TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid(),
                            TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid())),
                    IntLists.immutable.of(TermAux.GB_DIALECT_ASSEMBLAGE.getNid(),
                            TermAux.US_DIALECT_ASSEMBLAGE.getNid()),
                    IntLists.immutable.of(TermAux.SCT_CORE_MODULE.getNid(), TermAux.SOLOR_OVERLAY_MODULE.getNid(), TermAux.SOLOR_MODULE.getNid()),
                    AnyLanguageFullyQualifiedName(false)
            );
        }

        public static LanguageCoordinateImmutable GbEnglishPreferredName() {
            return LanguageCoordinateImmutable.make(
                    TermAux.ENGLISH_LANGUAGE.getNid(),
                    IntLists.immutable.of(expandDescriptionTypePreferenceList(null, TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid(),
                            TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid())),
                    IntLists.immutable.of(TermAux.GB_DIALECT_ASSEMBLAGE.getNid(),
                            TermAux.US_DIALECT_ASSEMBLAGE.getNid()),
                    IntLists.immutable.of(TermAux.SCT_CORE_MODULE.getNid(), TermAux.SOLOR_OVERLAY_MODULE.getNid(), TermAux.SOLOR_MODULE.getNid()),
                    AnyLanguageRegularName(false)
            );
        }

        public static LanguageCoordinateImmutable SpanishFullyQualifiedName() {
            return LanguageCoordinateImmutable.make(
                    TermAux.SPANISH_LANGUAGE.getNid(),
                    IntLists.immutable.of(expandDescriptionTypePreferenceList(null, TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid(),
                            TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid())),
                    IntLists.immutable.of(TermAux.SPANISH_LATIN_AMERICA_DIALECT_ASSEMBLAGE.getNid()),
                    IntLists.immutable.of(TermAux.SCT_CORE_MODULE.getNid(), TermAux.SOLOR_OVERLAY_MODULE.getNid(), TermAux.SOLOR_MODULE.getNid()),
                    // Adding next priority language coordinate to be available for testing, and fallback.
                    UsEnglishFullyQualifiedName()
            );
        }

        public static LanguageCoordinateImmutable SpanishPreferredName() {
            return LanguageCoordinateImmutable.make(
                    TermAux.SPANISH_LANGUAGE.getNid(),
                    IntLists.immutable.of(expandDescriptionTypePreferenceList(null, TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid(),
                            TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid())),
                    IntLists.immutable.of(TermAux.SPANISH_LATIN_AMERICA_DIALECT_ASSEMBLAGE.getNid()),
                    IntLists.immutable.of(TermAux.SCT_CORE_MODULE.getNid(), TermAux.SOLOR_OVERLAY_MODULE.getNid(), TermAux.SOLOR_MODULE.getNid()),
                    // Adding next priority language coordinate to be available for testing, and fallback.
                    UsEnglishFullyQualifiedName()
            );
        }
        
        /**
         * Take in a list of the description type prefs, such as {@link TermAux#FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE}, {@link TermAux#REGULAR_NAME_DESCRIPTION_TYPE}
         * and include any non-core description types that are linked to these core types, in the right order, so that the LanguageCoordinates can include the 
         * non-core description types in the appropriate places when looking for descriptions.
         * @param descriptionTypePreferenceList the starting list - should only consist of core description types - 
         * {@link TermAux#FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE}, {@link TermAux#REGULAR_NAME_DESCRIPTION_TYPE}, {@link TermAux#DEFINITION_DESCRIPTION_TYPE} 
         * @param stampFilter - optional - if not provided, uses {@link sh.isaac.api.coordinate.Coordinates.Filter#DevelopmentLatestActiveOnly()}
         * @return the initial list, plus any equivalent non-core types in the appropriate order.  See {@link DynamicConstants#DYNAMIC_DESCRIPTION_CORE_TYPE}
         */
        public static int[] expandDescriptionTypePreferenceList(StampFilter stampFilter, int ... descriptionTypePreferenceList) {
            LOG.trace("Expand desription types requested");
            StampFilter filter = stampFilter == null ? Coordinates.Filter.DevelopmentLatestActiveOnly() : stampFilter;
            int requestKey = filter.hashCode();
            for (int nid : descriptionTypePreferenceList) {
                requestKey = 97 * requestKey + nid;
            }

            return LANG_EXPAND_CACHE.get(requestKey, keyAgain -> {
                long time = System.currentTimeMillis();

                if (ccl == null) {
                    ccl = new ChronologyChangeListener() {
                        UUID me = UUID.randomUUID();
                        {
                            Get.commitService().addChangeListener(this);
                        }

                        @Override
                        public void handleCommit(CommitRecord commitRecord) {
                            // ignore
                        }

                        @Override
                        public void handleChange(SemanticChronology sc) {
                            LANG_EXPAND_CACHE.invalidateAll();
                        }

                        @Override
                        public void handleChange(ConceptChronology cc) {
                            LANG_EXPAND_CACHE.invalidateAll();
                        }

                        @Override
                        public UUID getListenerUuid() {
                            return me;
                        }
                    };
                }
                MutableIntObjectMap<MutableIntSet> equivalentTypes = MutableIntObjectMapFactoryImpl.INSTANCE.empty();

                //Collect the mappings from core types -> non core types
                IntStream nids = Get.identifierService().getNidsForAssemblage(DynamicConstants.get().DYNAMIC_DESCRIPTION_CORE_TYPE.getNid(), false);
                nids.forEach(nid -> {
                    SemanticChronology sc = Get.assemblageService().getSemanticChronology(nid);
                    DynamicVersion dv = (DynamicVersion) sc.getLatestVersion(filter).get();
                    int coreType = Get.identifierService().getNidForUuids(((DynamicUUID) dv.getData(0)).getDataUUID());
                    MutableIntSet mapped = equivalentTypes.get(coreType);
                    if (mapped == null) {
                        mapped = MutableIntSetFactoryImpl.INSTANCE.empty();
                        equivalentTypes.put(coreType, mapped);
                    }
                    mapped.add(sc.getReferencedComponentNid());
                });

                if (equivalentTypes.isEmpty()) {
                    //this method is a noop
                    LOG.trace("Expanded description types call is a noop in {}ms", System.currentTimeMillis() - time);
                    return descriptionTypePreferenceList;
                }

                MutableIntList result = IntLists.mutable.empty();
                IntList startNids = IntLists.immutable.of(descriptionTypePreferenceList);
                for (int coreType : descriptionTypePreferenceList) {
                    if (!result.contains(coreType)) {
                        result.add(coreType);
                    }
                    MutableIntSet nonCoreTypes = equivalentTypes.get(coreType);
                    if (nonCoreTypes != null) {
                        nonCoreTypes.forEach(type -> {
                            if (!result.contains(type)) {
                                result.add(type);
                            }
                        });
                    }
                }
                LOG.info("Expanded language type list from {} to {} in {}ms", startNids, result, System.currentTimeMillis() - time);
                return result.toArray(new int[result.size()]);
            });
        }
    }

    public static class Filter {

        public static StampFilterImmutable DevelopmentLatest() {
            return StampFilterImmutable.make(StatusSet.ACTIVE_AND_INACTIVE,
                    Position.LatestOnDevelopment(),
                    IntSets.immutable.empty());
        }

        public static StampFilterImmutable DevelopmentLatestActiveOnly() {
            return StampFilterImmutable.make(StatusSet.ACTIVE_ONLY,
                    Position.LatestOnDevelopment(),
                    IntSets.immutable.empty());
        }

        public static StampFilterImmutable MasterLatest() {
            return StampFilterImmutable.make(StatusSet.ACTIVE_AND_INACTIVE,
                    Position.LatestOnMaster(),
                    IntSets.immutable.empty());
        }

        public static StampFilterImmutable MasterLatestActiveOnly() {
            return StampFilterImmutable.make(StatusSet.ACTIVE_ONLY,
                    Position.LatestOnMaster(),
                    IntSets.immutable.empty());
        }
    }

    public static class Position {
        public static StampPositionImmutable LatestOnDevelopment() {
            return StampPositionImmutable.make(Long.MAX_VALUE, TermAux.DEVELOPMENT_PATH);
        }
        public static StampPositionImmutable LatestOnMaster() {
            return StampPositionImmutable.make(Long.MAX_VALUE, TermAux.MASTER_PATH);
        }
    }

    public static class Path {

        public static StampPathImmutable Master() {
            return StampPathImmutable.make(TermAux.MASTER_PATH, Sets.immutable.of(StampPositionImmutable.make(Long.MAX_VALUE, TermAux.PRIMORDIAL_PATH.getNid())));
        }

        public static StampPathImmutable Development() {
            return StampPathImmutable.make(TermAux.DEVELOPMENT_PATH, Sets.immutable.of(StampPositionImmutable.make(Long.MAX_VALUE, TermAux.PRIMORDIAL_PATH.getNid())));
        }
    }

    public static class Manifold {
        public static final ManifoldCoordinateImmutable DevelopmentInferredRegularNameSort() {
            return ManifoldCoordinateImmutable.makeInferred(
                    Path.Development().getStampFilter(),
                    Language.UsEnglishRegularName(),
                    Logic.ElPlusPlus(), Activity.DEVELOPING, Edit.Default());
        }
        public static final ManifoldCoordinateImmutable DevelopmentStatedRegularNameSort() {
            return ManifoldCoordinateImmutable.makeStated(
                    Path.Development().getStampFilter(),
                    Language.UsEnglishRegularName(),
                    Logic.ElPlusPlus(), Activity.DEVELOPING, Edit.Default());
        }
    }
    

    @Override
    public void reset() {
       LANG_EXPAND_CACHE.invalidateAll();
    }
}
