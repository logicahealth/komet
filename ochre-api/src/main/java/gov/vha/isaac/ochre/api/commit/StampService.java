package gov.vha.isaac.ochre.api.commit;

import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import javafx.concurrent.Task;

import java.time.Instant;
import java.util.Map;
import java.util.stream.IntStream;
import org.jvnet.hk2.annotations.Contract;

/**
 * Created by kec on 1/2/16.
 */
@Contract
public interface StampService {

    /**
     * STAMP sequences start at 1, in part to ensure that uninitialized values
     * (a zero by default) are not treated as valid stamp sequences.
     */
    int FIRST_STAMP_SEQUENCE = 1;

    int getAuthorSequenceForStamp(int stampSequence);

    int getModuleSequenceForStamp(int stampSequence);

    int getPathSequenceForStamp(int stampSequence);

    State getStatusForStamp(int stampSequence);

    long getTimeForStamp(int stampSequence);

    boolean isNotCanceled(int stampSequence);

    boolean isUncommitted(int stampSequence);

    /**
     *
     * @param stampSequence a stamp sequence to create an analog of
     * @return a stampSequence with a State of {@link State#INACTIVE}, but the
     * same time, author, module, and path as the provided stamp sequence.
     */
    int getRetiredStampSequence(int stampSequence);

    /**
     *
     * @param stampSequence a stamp sequence to create an analog of
     * @return a stampSequence with a State of {@link State#ACTIVE}, but the
     * same time, author, module, and path as the provided stamp sequence.
     */
    int getActivatedStampSequence(int stampSequence);

    /**
     * An idempotent operation to return a sequence that uniquely identified by
     * this combination of status, time, author, module, and path (STAMP). If an
     * existing sequence has this combination, that existing sequence will be
     * returned. If no sequence has this combination, a new sequence will be
     * created and returned.
     *
     * @param status
     * @param time
     * @param authorSequence
     * @param moduleSequence
     * @param pathSequence
     * @return the stampSequence
     */
    int getStampSequence(State status, long time,
            int authorSequence, int moduleSequence, int pathSequence);

    /**
     *
     * @param stampSequence
     * @return a textual representation of the stamp sequence.
     */
    String describeStampSequence(int stampSequence);

    /**
     *
     * @param stampSequence
     * @return the Instant represented by this stampSequence
     */
    default Instant getInstantForStamp(int stampSequence) {
        return Instant.ofEpochMilli(getTimeForStamp(stampSequence));
    }

    /**
     * Use to compare if versions may be unnecessary duplicates. If their
     * content is equal, see if their stampSequences indicate a semantic
     * difference (change in status, module, or path).
     *
     * @param stampSequence1
     * @param stampSequence2
     * @return true if stampSequences are equal without considering the author
     * and time.
     */
    boolean stampSequencesEqualExceptAuthorAndTime(int stampSequence1, int stampSequence2);

    /**
     * @return an IntStream of all stamp sequences known to the commit service.
     */
    IntStream getStampSequences();

    /**
     * Used by the commit manager to get the pending stamps, so that there is a
     * definitive list if items in the commit. Should only be used by developers
     * creating their own commit service.
     *
     * @return
     */
    Map<UncommittedStamp, Integer> getPendingStampsForCommit();

    /**
     * Used to revert a commit in progress, i.e. a commit that failed because of
     * a data check error, or some other intervening circumstance. Not for use
     * (will not work) to undo a successful commit. Should only be used by
     * developers creating their own commit service.
     *
     * @param pendingStamps
     */
    void setPendingStampsForCommit(Map<UncommittedStamp, Integer> pendingStamps);

    /**
     * Used by the commit manger to cancel pending stamps for a particular
     * author. Should only be used by developers creating their own commit
     * service.
     *
     * @param authorSequence
     * @return
     */
    Task<Void> cancel(int authorSequence);
    
    /**
     * Used by the commit manger when committing a pending stamp. 
     * Should only be used by developers creating their own commit
     * service.
     * @param stamp
     * @param stampSequence 
     */
     void addStamp(Stamp stamp, int stampSequence);
}
