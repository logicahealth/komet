package sh.isaac.api.transaction;

import javafx.concurrent.Task;
import sh.isaac.api.ProgressTracker;
import sh.isaac.api.alert.AlertObject;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.commit.ChangeChecker;
import sh.isaac.api.commit.CommitTask;
import sh.isaac.api.observable.ObservableVersion;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

public interface Transaction {

    UUID getTransactionId();

    boolean containsTransactionId(UUID transactionId);

    Set<Integer> getStampsForTransaction();

    void addVersionToTransaction(Version v);

    Set<Integer> getComponentNidsForTransaction();

    int getCheckCountForTransaction();

    boolean readyToCommit(ConcurrentSkipListSet<ChangeChecker> checkers,
                          ConcurrentSkipListSet<AlertObject> alertCollection,
                          ProgressTracker tracker);

    CommitTask commit();

    CommitTask commit(String comment);

    CommitTask commit(String comment, ConcurrentSkipListSet<AlertObject> alertCollection);

    CommitTask commitObservableVersions(String commitComment, ObservableVersion... versionsToCommit);

    Task<Void> cancel();
}
