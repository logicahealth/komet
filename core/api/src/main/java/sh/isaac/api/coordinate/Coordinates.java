package sh.isaac.api.coordinate;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import sh.isaac.api.LanguageCoordinateService;
import sh.isaac.api.bootstrap.TermAux;

import java.util.List;

public class Coordinates {
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
         *     In this case, it will prefer regular name, but will
         *     return a FQN or definition if regular name isn't available.  If you
         *     only want a particlar type, make a different coordinate with only the types you want in the description type list.
         *
         */
        public static LanguageCoordinateImmutable AnyLanguageRegularName() {
            return LanguageCoordinateImmutable.make(
                    TermAux.LANGUAGE,
                    IntLists.immutable.of(TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid(),
                            TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid(), TermAux.DEFINITION_DESCRIPTION_TYPE.getNid()),
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
         * In this case, it will prefer fully qualified names, of arbitrary language  but will return descriptions of any description
         * type. If you
         * only want a particlar type, make a different coordinate with only the types you want in the description type list.
         */
        public static LanguageCoordinateImmutable AnyLanguageFullyQualifiedName() {
            return LanguageCoordinateImmutable.make(
                    TermAux.LANGUAGE,
                    IntLists.immutable.of(TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid(),
                            TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid(), TermAux.DEFINITION_DESCRIPTION_TYPE.getNid()),
                    IntLists.immutable.empty(),
                    IntLists.immutable.empty()
            );
        }

        /**
         * A coordinate that completely ignores language - descriptions ranked by this coordinate will only be ranked by
         * description type and module preference.  This coordinate is primarily useful as a fallback coordinate for the final
         * {@link LanguageCoordinate#getNextProrityLanguageCoordinate()} in a chain
         *
         * See {@link LanguageCoordinateService#getSpecifiedDescription(StampFilter, List, LanguageCoordinate)}
         * @param defOnly - if true, will only return definition types (or extended types) - if false, will fall back to regular name types,
         *     then FQN types.
         *
         * @return a coordinate that prefers definitions, of arbitrary language.
         * type
         */
        public static LanguageCoordinateImmutable AnyLanguageDefinition() {
            return LanguageCoordinateImmutable.make(
                    TermAux.LANGUAGE,
                    IntLists.immutable.of(TermAux.DEFINITION_DESCRIPTION_TYPE.getNid(), TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid(),
                            TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid()),
                    IntLists.immutable.empty(),
                    IntLists.immutable.empty()
            );
        }

        public static LanguageCoordinateImmutable UsEnglishFullyQualifiedName() {
            return LanguageCoordinateImmutable.make(
                    TermAux.ENGLISH_LANGUAGE.getNid(),
                    IntLists.immutable.of(TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid(),
                            TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid()),
                    IntLists.immutable.of(TermAux.US_DIALECT_ASSEMBLAGE.getNid(), TermAux.GB_DIALECT_ASSEMBLAGE.getNid()),
                    IntLists.immutable.of(TermAux.SCT_CORE_MODULE.getNid(), TermAux.SOLOR_OVERLAY_MODULE.getNid(), TermAux.SOLOR_MODULE.getNid()),
                    AnyLanguageFullyQualifiedName()
            );
        }

        public static LanguageCoordinateImmutable UsEnglishPreferredName() {
            return LanguageCoordinateImmutable.make(
                    TermAux.ENGLISH_LANGUAGE.getNid(),
                    IntLists.immutable.of(TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid(),
                            TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid()),
                    IntLists.immutable.of(TermAux.US_DIALECT_ASSEMBLAGE.getNid(), TermAux.GB_DIALECT_ASSEMBLAGE.getNid()),
                    IntLists.immutable.of(TermAux.SCT_CORE_MODULE.getNid(), TermAux.SOLOR_OVERLAY_MODULE.getNid(), TermAux.SOLOR_MODULE.getNid()),
                    AnyLanguageRegularName()
            );
        }

        public static LanguageCoordinateImmutable GbEnglishFullyQualifiedName() {
            return LanguageCoordinateImmutable.make(
                    TermAux.ENGLISH_LANGUAGE.getNid(),
                    IntLists.immutable.of(TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid(),
                            TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid()),
                    IntLists.immutable.of(TermAux.GB_DIALECT_ASSEMBLAGE.getNid(),
                            TermAux.US_DIALECT_ASSEMBLAGE.getNid()),
                    IntLists.immutable.of(TermAux.SCT_CORE_MODULE.getNid(), TermAux.SOLOR_OVERLAY_MODULE.getNid(), TermAux.SOLOR_MODULE.getNid()),
                    AnyLanguageFullyQualifiedName()
            );
        }

        public static LanguageCoordinateImmutable GbEnglishPreferredName() {
            return LanguageCoordinateImmutable.make(
                    TermAux.ENGLISH_LANGUAGE.getNid(),
                    IntLists.immutable.of(TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid(),
                            TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid()),
                    IntLists.immutable.of(TermAux.GB_DIALECT_ASSEMBLAGE.getNid(),
                            TermAux.US_DIALECT_ASSEMBLAGE.getNid()),
                    IntLists.immutable.of(TermAux.SCT_CORE_MODULE.getNid(), TermAux.SOLOR_OVERLAY_MODULE.getNid(), TermAux.SOLOR_MODULE.getNid()),
                    AnyLanguageRegularName()
            );
        }

        public static LanguageCoordinateImmutable SpanishFullyQualifiedName() {
            return LanguageCoordinateImmutable.make(
                    TermAux.SPANISH_LANGUAGE.getNid(),
                    IntLists.immutable.of(TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid(),
                            TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid()),
                    IntLists.immutable.of(TermAux.SPANISH_LATIN_AMERICA_DIALECT_ASSEMBLAGE.getNid()),
                    IntLists.immutable.of(TermAux.SCT_CORE_MODULE.getNid(), TermAux.SOLOR_OVERLAY_MODULE.getNid(), TermAux.SOLOR_MODULE.getNid()),
                    // Adding next priority language coordinate to be available for testing, and fallback.
                    UsEnglishFullyQualifiedName()
            );
        }

        public static LanguageCoordinateImmutable SpanishPreferredName() {
            return LanguageCoordinateImmutable.make(
                    TermAux.SPANISH_LANGUAGE.getNid(),
                    IntLists.immutable.of(TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid(),
                            TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid()),
                    IntLists.immutable.of(TermAux.SPANISH_LATIN_AMERICA_DIALECT_ASSEMBLAGE.getNid()),
                    IntLists.immutable.of(TermAux.SCT_CORE_MODULE.getNid(), TermAux.SOLOR_OVERLAY_MODULE.getNid(), TermAux.SOLOR_MODULE.getNid()),
                    // Adding next priority language coordinate to be available for testing, and fallback.
                    UsEnglishFullyQualifiedName()
            );
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

    public static class Digraph {
        public static final NavigationCoordinateImmutable Stated() {
            return NavigationCoordinateImmutable.makeStated();
        }
        public static final NavigationCoordinateImmutable Inferred() {
            return NavigationCoordinateImmutable.makeInferred();
        }
    }

    public static class Manifold {
        public static final ManifoldCoordinateImmutable DevelopmentInferredRegularNameSort() {
            return ManifoldCoordinateImmutable.makeInferred(
                    Path.Development().getStampFilter(),
                    Language.UsEnglishPreferredName(),
                    Logic.ElPlusPlus());
        }
        public static final ManifoldCoordinateImmutable DevelopmentStatedRegularNameSort() {
            return ManifoldCoordinateImmutable.makeStated(
                    Path.Development().getStampFilter(),
                    Language.UsEnglishPreferredName(),
                    Logic.ElPlusPlus());
        }
    }

}
