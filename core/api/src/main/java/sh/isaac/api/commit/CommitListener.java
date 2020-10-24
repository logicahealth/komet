package sh.isaac.api.commit;

import java.util.UUID;

public interface CommitListener {

    /**
     * Gets the listener uuid.
     *
     * @return a unique UUID for this listener.
     */
    UUID getListenerUuid();


    /**
     * Don't do work on or block the calling thread.
     * @param commitRecord a record of a successful commit.
     */
    void handleCommit(CommitRecord commitRecord);
}
