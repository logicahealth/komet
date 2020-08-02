package sh.isaac.api.navigation;

import sh.isaac.api.Status;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.ManifoldCoordinateImmutable;
import sh.isaac.api.coordinate.StampFilterImmutable;
import sh.isaac.api.coordinate.VertexSort;
import sh.isaac.api.snapshot.calculator.RelativePositionCalculator;

import java.util.EnumSet;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

public interface NavigationRecord {

    /**
     * Determine if the concept has a latest status within the allowed status values of the stamp coordinate.
     *
     * @param conceptNid           the concept nid
     * @param stampFilterImmutable the stamp coordinate
     * @return true, if the latest stamp as determined by the stamp coordinate is within the allowed
     * states of the stamp coordinate.
     */
    boolean conceptSatisfiesStamp(int conceptNid, StampFilterImmutable stampFilterImmutable);

    EnumSet<Status> getConceptStates(int conceptNid, StampFilterImmutable stampFilterImmutable);

    /**
     * Connection count.
     *
     * @return the int
     */
    int connectionCount();

    /**
     * Contains concept nid via type, ignoring coordinates
     *
     * @param conceptNid      the concept nid
     * @param typeSequenceSet the type sequence set
     * @param flags           the flags
     * @return true, if successful
     */
    boolean containsConceptNidViaType(int conceptNid, NidSet typeSequenceSet, int[] flags);

    /**
     * Contains concept nid via type.
     *
     * @param conceptNid      the concept nid
     * @param typeSequenceSet the type sequence set
     * @param mc              the tc
     * @return true, if successful
     */
    boolean containsConceptNidViaType(int conceptNid,
                                      NidSet typeSequenceSet,
                                      ManifoldCoordinate mc);

    /**
     * Contains concept nid via type.
     *
     * @param conceptNid   the concept nid
     * @param typeSequence the type sequence
     * @param mc           the tc
     * @return true, if successful
     */
    boolean containsConceptNidViaType(int conceptNid, int typeSequence, ManifoldCoordinate mc);

    /**
     * Contains concept nid via type.
     *
     * @param conceptNid      the concept nid
     * @param typeSequenceSet the type sequence set
     * @param mc              the tc
     * @param flags           the flags
     * @return true, if successful
     */
    boolean containsConceptNidViaType(int conceptNid,
                                      NidSet typeSequenceSet,
                                      ManifoldCoordinate mc,
                                      int[] flags);

    /**
     * Contains concept nid via type.
     *
     * @param conceptNid the concept nid
     * @param typeNid    the type nid
     * @param mc         the manifold coordinate
     * @param flags      the flags
     * @return true, if successful
     */
    boolean containsConceptNidViaType(int conceptNid,
                                      int typeNid,
                                      ManifoldCoordinate mc,
                                      int[] flags);

    /**
     * Contains sequence via type with flags, ignoring coordinates
     *
     * @param conceptNid the concept nid
     * @param typeNid    the type sequence
     * @param flags      the flags
     * @return true, if successful
     */
    boolean containsNidViaTypeWithFlags(int conceptNid, int typeNid, int[] flags);

    /**
     * Length.
     *
     * @return the int
     */
    int length();

    /**
     * Gets the concept nid stamp records.
     *
     * @param conceptNid the concept nid
     * @return the concept nid stamp records
     */
    Optional<? extends TypeStampNavigationRecords> getConceptNidStampRecords(int conceptNid);

    /**
     * Contains stamp of type with flags.
     *
     * @param typeNid Integer.MAX_VALUE is a wildcard and will match all types.
     * @param flags   the flags
     * @return true if found.
     */
    boolean containsStampOfTypeWithFlags(int typeNid, int[] flags);

    /**
     * Gets the concept nids for type, ignoring coordinates
     *
     * @param typeNid typeNid to match, or Integer.MAX_VALUE if a wildcard.
     * @return active concepts identified by their sequence value.
     */
    int[] getConceptNidsForType(int typeNid);

    /**
     * Gets the concept nids for type.
     *
     * @param typeSequence       typeNid to match, or Integer.MAX_VALUE if a wildcard.
     * @param manifoldCoordinate used to determine if a concept is active.
     * @return active concepts identified by their sequence value.
     * @deprecated method that uses RelativePositionCalculator is safer from a concurrent modification perspective.
     */
    int[] getConceptNidsForType(int typeSequence, ManifoldCoordinate manifoldCoordinate, IntFunction<int[]> taxonomyDataProvider);

    int[] getConceptNidsForType(int typeSequence, IntFunction<int[]> taxonomyDataProvider, int[] flags,
                                RelativePositionCalculator edgeComputer,
                                RelativePositionCalculator vertexComputer,
                                VertexSort sort,
                                ManifoldCoordinateImmutable digraph);

    /**
     * Gets the destination concept nids, ignoring all coordinates.
     *
     * @return the destination concept nids
     */
    IntStream getDestinationConceptSequences();

    /**
     * Gets the destination concept nids not of type.
     *
     * @param typeSet the type set
     * @param mc      the tc
     * @return the destination concept nids not of type
     */
    int[] getDestinationConceptNidsNotOfType(NidSet typeSet, ManifoldCoordinate mc);

    /**
     * Gets the destination concept nids of type, ignoring all coordinates
     *
     * @param typeSet the type set
     * @return the destination concept nids of type
     */
    IntStream getDestinationConceptNidsOfType(NidSet typeSet);

    /**
     * Gets the destination concept nids of type.
     *
     * @param typeSet the type set
     * @param mc      the mc
     * @return the destination concept nids of type
     */
    int[] getDestinationConceptNidsOfType(NidSet typeSet, ManifoldCoordinate mc);

    /**
     * Gets the destination concept nids of type.
     *
     * @param typeSet the type set
     * @param mc      the mc
     * @return the destination concept nids of type
     */
    boolean hasDestinationConceptNidsOfType(NidSet typeSet, ManifoldCoordinate mc);

    /**
     * Gets the parent concept nids, ignoring all coordinates
     *
     * @return the parent concept nids
     */
    IntStream getParentConceptSequences();

    /**
     * Gets the types for relationship.
     *
     * @param destinationId the destination id
     * @param mc            the tc
     * @return the types for relationship
     */
    int[] getTypesForRelationship(int destinationId, ManifoldCoordinate mc);
}
