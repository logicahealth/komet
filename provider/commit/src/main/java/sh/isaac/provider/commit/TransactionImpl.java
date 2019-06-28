package sh.isaac.provider.commit;

import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.Get;
import sh.isaac.api.ProgressTracker;
import sh.isaac.api.alert.AlertCategory;
import sh.isaac.api.alert.AlertObject;
import sh.isaac.api.alert.AlertType;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.commit.ChangeChecker;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.commit.CommitTask;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.transaction.Transaction;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TODO add record of if component passed tests and is ready for commit (an optimization with possible side effects).
 */
public class TransactionImpl implements Transaction, Comparable<Transaction> {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LogManager.getLogger();

    ConcurrentHashMap<Integer, TransactionsForPath> children = new ConcurrentHashMap<>();

    final UUID transactionId;
    final ChangeCheckerMode changeCheckerMode;

    public TransactionImpl(ChangeCheckerMode changeCheckerMode) {
        this.transactionId = UUID.randomUUID();
        this.changeCheckerMode = changeCheckerMode;
    }

    private TransactionImpl(UUID uuid, ChangeCheckerMode changeCheckerMode) {
        this.transactionId = uuid;
        this.changeCheckerMode = changeCheckerMode;
    }

    @Override
    public boolean containsTransactionId(UUID transactionId) {
        if (this.transactionId.equals(transactionId)) {
            return true;
        }
        for (TransactionsForPath child: children.values()) {
            if (child.transactionId.equals(transactionId)) {
                return true;
            }
        }
        return false;
    }

    public Collection<? extends TransactionImpl> getChildren() {
        return children.values();
    }

    @Override
    public UUID getTransactionId() {
        return transactionId;
    }

    public TransactionImpl.TransactionsForPath addStampToTransaction(int stampSequence) {
        int pathNid = Get.stampService().getPathNidForStamp(stampSequence);
        children.computeIfAbsent(pathNid, pathNidKey -> new TransactionsForPath(this.changeCheckerMode, pathNidKey)).addStampToTransaction(stampSequence);
        return children.get(stampSequence);
    }

    @Override
    public Set<Integer> getStampsForTransaction() {
        HashSet<Integer> stampsForTransaction = new HashSet<>();
        for (Transaction childTransaction: children.values()) {
            stampsForTransaction.addAll(childTransaction.getStampsForTransaction());
        }
        return Collections.unmodifiableSet(stampsForTransaction);
    }

    @Override
    public void addVersionToTransaction(Version v) {
        children.computeIfAbsent(v.getPathNid(), pathNidKey -> new TransactionsForPath(this.changeCheckerMode, pathNidKey)).addComponentNidToTransaction(v.getNid());
    }

    @Override
    public Set<Integer> getComponentNidsForTransaction() {
        Set<Integer> componentNids = new HashSet<>();
        for (Transaction childTransaction: children.values()) {
            componentNids.addAll(childTransaction.getComponentNidsForTransaction());
        }
        return Collections.unmodifiableSet(componentNids);
    }

    @Override
    public int getCheckCountForTransaction() {
        int checkCount = 0;

        for (Transaction childTransaction: children.values()) {
            checkCount += childTransaction.getCheckCountForTransaction();
        }
        return checkCount;
    }

    @Override
    public boolean readyToCommit(ConcurrentSkipListSet<ChangeChecker> checkers,
                                 ConcurrentSkipListSet<AlertObject> alertCollection,
                                 ProgressTracker tracker) {
        AtomicBoolean ready = new AtomicBoolean(true);
        for (Transaction childTransaction: children.values()) {
            if (!childTransaction.readyToCommit(checkers, alertCollection, tracker)) {
                ready.set(false);
            }
        }
        return ready.get();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionImpl that = (TransactionImpl) o;
        return transactionId.equals(that.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }

    @Override
    public CommitTask commit() {
        return commit("", new ConcurrentSkipListSet<AlertObject>());
    }

    @Override
    public CommitTask commit(String comment) {
        return commit(comment, new ConcurrentSkipListSet<AlertObject>());
    }


    @Override
    public CommitTask commit(String comment, ConcurrentSkipListSet<AlertObject> alertCollection) {
        LOG.info("Committing transaction: " + this.getTransactionId());
        return Get.commitService().commit(this, comment, alertCollection);
    }

    @Override
    public Task<Void> cancel() {
        return ((CommitProvider) Get.commitService()).cancel(this);
    }

    public boolean containsComponent(int nid) {
        for (TransactionsForPath child: children.values()) {
            if (child.containsComponent(nid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int compareTo(Transaction o) {
        return this.getTransactionId().compareTo(o.getTransactionId());
    }

    public Transaction getTransactionForPath(int pathNid) {
        return children.computeIfAbsent(pathNid, pathNidKey -> new TransactionsForPath(this.changeCheckerMode, pathNidKey));
    }

    private class TransactionsForPath extends TransactionImpl {
        final ConcurrentSkipListSet<Integer> stamps = new ConcurrentSkipListSet<>();
        final ConcurrentSkipListSet<Integer> components = new ConcurrentSkipListSet<>();
        final int pathNid;

        public TransactionsForPath(ChangeCheckerMode changeCheckerMode, int pathNid) {
            super(changeCheckerMode);
            this.pathNid = pathNid;
        }

        public TransactionsForPath(UUID uuid, ChangeCheckerMode changeCheckerMode, int pathNid) {
            super(uuid, changeCheckerMode);
            this.pathNid = pathNid;
        }
        public boolean containsComponent(int nid) {
            if (components.contains(nid)) {
                return true;
            }
            for (TransactionsForPath child: children.values()) {
                if (child.containsComponent(nid)) {
                    return true;
                }
            }
            return false;
        }


        public TransactionImpl.TransactionsForPath addStampToTransaction(int stampSequence) {
            stamps.add(stampSequence);
            return this;
        }

        public void addComponentNidToTransaction(int componentNid) {
            components.add(componentNid);
        }
        public Set<Integer> getComponentNidsForTransaction() {
            Set<Integer> componentNids = new HashSet<>(components);
            for (Transaction childTransaction: children.values()) {
                componentNids.addAll(childTransaction.getComponentNidsForTransaction());
            }
            return Collections.unmodifiableSet(componentNids);
        }


        public int getCheckCountForTransaction() {
            int checkCount = 0;
            if (changeCheckerMode == ChangeCheckerMode.ACTIVE) {
                checkCount = checkCount + components.size();
            }
            for (Transaction childTransaction: children.values()) {
                checkCount = checkCount + childTransaction.getCheckCountForTransaction();
            }
            return checkCount;
        }

        public Set<Integer> getStampsForTransaction() {
            HashSet<Integer> stampsForTransaction = new HashSet<>(stamps);
            for (Transaction childTransaction: children.values()) {
                stampsForTransaction.addAll(childTransaction.getStampsForTransaction());
            }
            return Collections.unmodifiableSet(stampsForTransaction);
        }

        public boolean readyToCommit(ConcurrentSkipListSet<ChangeChecker> checkers,
                                     ConcurrentSkipListSet<AlertObject> alertCollection,
                                     ProgressTracker tracker) {
            AtomicBoolean ready = new AtomicBoolean(true);
            for (Transaction childTransaction: children.values()) {
                if (!childTransaction.readyToCommit(checkers, alertCollection, tracker)) {
                    ready.set(false);
                }
            }
            if (changeCheckerMode == ChangeCheckerMode.ACTIVE &! checkers.isEmpty()) {
                components.parallelStream().forEach(nid -> {
                    Optional<? extends Chronology> optionalChronology = Get.identifiedObjectService().getChronology(nid);
                    if (optionalChronology.isPresent()) {
                        Chronology chronology = optionalChronology.get();
                        for (ChangeChecker checker: checkers) {
                            Optional<AlertObject> optionalAlert = checker.check(chronology, pathNid, this);
                            if (optionalAlert.isPresent()) {
                                alertCollection.add(optionalAlert.get());
                                if (optionalAlert.get().getAlertType().preventsCheckerPass()) {
                                    ready.set(false);
                                }
                            }
                        }
                    } else {
                        // No chronology for id.
                        ready.set(false);
                        alertCollection.add(new AlertObject("No chronology for id",
                                "There is no chronology in the database associated with this identifier: " + nid,
                                AlertType.ERROR,
                                AlertCategory.COMMIT,
                                nid));
                    }
                    tracker.completedUnitOfWork();
                });
            }
            return ready.get();
        }
    }

    @Override
    public CommitTask commitObservableVersions(String commitComment, ObservableVersion... versionsToCommit) {
        return ((CommitProvider) Get.commitService()).commitObservableVersions(this, commitComment, versionsToCommit);
    }
}
