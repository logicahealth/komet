package sh.isaac.provider.commit;

import javafx.concurrent.Task;
import org.apache.mahout.math.map.OpenIntIntHashMap;
import sh.isaac.api.Get;
import sh.isaac.api.alert.AlertCategory;
import sh.isaac.api.alert.AlertObject;
import sh.isaac.api.alert.AlertType;
import sh.isaac.api.collections.StampSequenceSet;
import sh.isaac.api.commit.*;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

public class TransactionCommitTask extends CommitTask {

    //~--- fields --------------------------------------------------------------

    /** The commit comment. */
    final String commitComment;

    final CommitProvider commitProvider;

    /** The checkers. */
    private final ConcurrentSkipListSet<ChangeChecker> checkers;

    private final ConcurrentSkipListSet<AlertObject> alertCollection;

    /** The pending stamps for commit. */
    private final TransactionImpl transaction;

    private final Instant commitTime;

    //~--- constructors --------------------------------------------------------

    /**
     * Instantiates a new global commit task.
     *
     * @param commitComment the commit comment
     * @param checkers the checkers
     * @param alertCollection the alert collection
     * @param transaction the transaction
     */
    private TransactionCommitTask(String commitComment,
                                  CommitProvider commitProvider,
                             ConcurrentSkipListSet<ChangeChecker> checkers,
                             ConcurrentSkipListSet<AlertObject> alertCollection,
                             TransactionImpl transaction, Instant commitTime) {
        this.commitComment = commitComment;
        this.commitProvider = commitProvider;
        this.checkers               = checkers;
        this.alertCollection = alertCollection;
        this.transaction = transaction;
        this.commitTime = commitTime;
        addToTotalWork(transaction.getCheckCountForTransaction());
        updateTitle("Commit");
        updateMessage(commitComment);
        //LOG.info("Spawning CommitTask " + taskSequenceId);
        Get.activeTasks().add(this);
    }

    /**
     * Execute the commit task
     *
     * @return the optional
     * @throws Exception the exception
     */
    @Override
    protected Optional<CommitRecord> call()
            throws Exception {
        try {
            // need to track
            if (!this.transaction.readyToCommit(this.checkers, this.alertCollection, this)) {
                return Optional.empty();
            }
            Task<Void> stampCommitTask = Get.stampService().commit(this.transaction, commitTime.toEpochMilli());
            stampCommitTask.get();


            if (!transaction.getStampsForTransaction().isEmpty()) {
                if (this.commitComment != null) {
                    transaction.getStampsForTransaction().stream().forEach((stamp) -> commitProvider.addComment(stamp, this.commitComment));
                }
                final CommitRecord commitRecord = new CommitRecord(commitTime,
                        StampSequenceSet.of(transaction.getStampsForTransaction()),
                        new OpenIntIntHashMap(),
                        transaction.getComponentNidsForTransaction(),
                        this.commitComment);

                this.commitProvider.handleCommitNotification(commitRecord);
                return Optional.of(commitRecord);
            }
            else{
                this.alertCollection.add(new AlertObject("nothing to commit", "Nothing was found to commit", AlertType.INFORMATION, AlertCategory.COMMIT));
                return Optional.empty();
            }
        } catch (final Exception e1) {
            LOG.error("Unexpected commit failure", e1);
            throw new RuntimeException("Commit Failure of commit with message " + this.commitComment, e1);
        } finally {
            CommitProvider.getPendingTransactions().remove(transaction);
            Get.activeTasks().remove(this);
            this.commitProvider.getPendingCommitTasks().remove(this);
            //LOG.info("Finished CommitTask " + taskSequenceId);
        }
    }

    /**
     * Construct a task to perform a global commit.  The task is already executed / running when this method returns.
     *
     * @param commitComment the commit comment
     * @param checkers the checkers
     * @param transaction the transaction
     * @param commitProvider the commit provider
     * @param commitTime the time to record for the commited records...
     * @return a CommitTaskGlobal3, where calling get() on the task will return an optional - if populated, the commit was successfully handled.
     * If the get() returns an Optional.empty(), then the commit failed the change checkers.  Calling {@link TransactionCommitTask#getAlerts()} will
     * provide the details on the failed change checkers.
     */

    public static TransactionCommitTask get(
            String commitComment,
            CommitProvider commitProvider,
            ConcurrentSkipListSet<ChangeChecker> checkers,
            ConcurrentSkipListSet<AlertObject> alertCollection,
            TransactionImpl transaction, Instant commitTime) {
        final TransactionCommitTask task = new TransactionCommitTask(
                commitComment,
                commitProvider,
                checkers,
                alertCollection,
                transaction, commitTime);
        commitProvider.getPendingCommitTasks().add(task);
        Get.activeTasks().add(task);
        try {
            Get.workExecutors().getExecutor().execute(task);
            return task;
        } catch (Exception e) {
            Get.activeTasks().remove(task);
            throw e;
        }
    }
}
