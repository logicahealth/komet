package sh.isaac.provider.commit;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import sh.isaac.api.transaction.Transaction;

import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

public class PendingTransactions {

    private static final ConcurrentSkipListSet<Transaction> concurrentPendingTransactionSet = new ConcurrentSkipListSet<>();

    private static final ObservableSet<Transaction> pendingTransactionObservableList = FXCollections.observableSet(new ConcurrentSkipListSet<>());

    private static final ObservableSet<Transaction> readOnlyPendingTransactionObservableList = FXCollections.unmodifiableObservableSet(pendingTransactionObservableList);

    private static final AtomicInteger pendingTransactionCount = new AtomicInteger();

    public static void addTransaction(Transaction transaction) {
        pendingTransactionCount.incrementAndGet();
        concurrentPendingTransactionSet.add(transaction);
        if (Platform.isFxApplicationThread()) {
            pendingTransactionObservableList.add(transaction);
        } else {
            Platform.runLater(() -> pendingTransactionObservableList.add(transaction));
        }
    }

    public static int getPendingTransactionCount() {
        return pendingTransactionCount.get();
    }

    public static void removeTransaction(Transaction transaction) {
        pendingTransactionCount.decrementAndGet();
        concurrentPendingTransactionSet.remove(transaction);
        if (Platform.isFxApplicationThread()) {
            pendingTransactionObservableList.remove(transaction);
        } else {
            Platform.runLater(() -> pendingTransactionObservableList.remove(transaction));
        }
    }

    public static ObservableSet<Transaction> getPendingTransactionList() {
        return readOnlyPendingTransactionObservableList;
    }
    protected static ConcurrentSkipListSet<Transaction> getConcurrentPendingTransactionList() {
        return concurrentPendingTransactionSet;
    }
}
