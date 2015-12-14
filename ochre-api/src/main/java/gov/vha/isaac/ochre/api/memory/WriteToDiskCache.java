package gov.vha.isaac.ochre.api.memory;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by kec on 4/10/15.
 */
public class WriteToDiskCache {

    private static final int WRITE_INTERVAL_IN_MS = 15000;

    static final Thread writerThread;

    static ConcurrentSkipListSet<MemoryManagedReference> cacheSet = new ConcurrentSkipListSet<>();

    static {
        writerThread = new Thread(new WriteToDiskRunnable(), "WriteToDiskCache thread");
        writerThread.setDaemon(true);
    }

    public static class WriteToDiskRunnable implements Runnable {

        @Override
        public void run() {
            while (true) {
                Optional<MemoryManagedReference> optionalReference = cacheSet.stream().
                        filter((memoryManagedReference) -> {
                            if (memoryManagedReference.get() == null) {
                                cacheSet.remove(memoryManagedReference);
                                return false;
                            }

                            return memoryManagedReference.hasUnwrittenUpdate();
                        }).max((o1, o2) -> {
                            if (o1.msSinceLastUnwrittenUpdate() > o2.msSinceLastUnwrittenUpdate()) {
                                return 1;
                            }
                            if (o1.msSinceLastUnwrittenUpdate() < o2.msSinceLastUnwrittenUpdate()) {
                                return -1;
                            }
                            return 0;
                        });
                boolean written = false;
                if (optionalReference.isPresent()) {
                    written = true;
                    MemoryManagedReference ref = (MemoryManagedReference) optionalReference.get();
                    if (ref.msSinceLastUnwrittenUpdate() > WRITE_INTERVAL_IN_MS) {
                        ref.write();
                    }
                }
                if (!written) {
                    try {
                        writerThread.wait(WRITE_INTERVAL_IN_MS);
                    } catch (InterruptedException e) {
                        // continue work
                    }
                }
            }
        }
    }

    public static void addToCache(MemoryManagedReference newRef) {
        cacheSet.add(newRef);
    }

    public static void flushAndClearCache() {
        cacheSet.stream().
                forEach((memoryManagedReference) -> {
                    
                    cacheSet.remove(memoryManagedReference);
                    while (memoryManagedReference.hasUnwrittenUpdate()) {
                        memoryManagedReference.clear();
                        memoryManagedReference.write();
                    }
                });
    }
}
