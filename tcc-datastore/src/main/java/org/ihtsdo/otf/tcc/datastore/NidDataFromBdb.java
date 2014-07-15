package org.ihtsdo.otf.tcc.datastore;

import org.ihtsdo.otf.tcc.api.thread.NamedThreadFactory;
import org.ihtsdo.otf.tcc.datastore.temp.AceLog;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptDataFetcherI;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.ref.Reference;
import java.util.concurrent.*;

public class NidDataFromBdb implements ConceptDataFetcherI {

    public static enum REF_TYPE {

        SOFT, WEAK
    };
    private static REF_TYPE refType = REF_TYPE.SOFT;
    private Future<byte[]> readOnlyFuture;
    private Future<byte[]> readWriteFuture;
    private int nid;
    private Reference<byte[]> readOnlyBytes;
    private byte[] readWriteBytes;
    private static ThreadGroup nidDataThreadGroup = new ThreadGroup("nid data threads");
    private static ExecutorService executorPool;

    static {
        resetExecutorPool();
    }

    public static void resetExecutorPool() {
        executorPool = Executors.newFixedThreadPool(
                Math.min(6, Runtime.getRuntime().availableProcessors() + 1),
                new NamedThreadFactory(nidDataThreadGroup,
                "Nid data service"));
    }

    public static void close() {
        if (executorPool != null) {
            AceLog.getAppLog().info("Shutting down NidDataFromBdb executor pool.");
            executorPool.shutdown();
            AceLog.getAppLog().info("Awaiting termination of NidDataFromBdb executor pool.");
            try {
                executorPool.awaitTermination(90, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                AceLog.getAppLog().warning(e.toString());
            }
            AceLog.getAppLog().info("Termination NidDataFromBdb executor pool.");
        }
        executorPool = null;
    }

    public NidDataFromBdb(int nid) {
        super();
        this.nid = nid;
        if (executorPool != null) {
            readOnlyFuture = executorPool.submit(new GetNidData(nid, Bdb.getConceptDb().getReadOnly()));
            readWriteFuture = executorPool.submit(new GetNidData(nid, Bdb.getConceptDb().getReadWrite()));
        }
    }

    @Override
    public synchronized void reset() {
        readWriteBytes = null;
        readWriteFuture = null;
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.db.bdb.ConceptDataFetcherI#getMutableBytes()
     */
    @Override
    public synchronized byte[] getMutableBytes() throws IOException {
        if (readWriteBytes == null) {
            if (readWriteFuture == null) {
                readWriteFuture = executorPool.submit(new GetNidData(nid, Bdb.getConceptDb().getReadWrite()));
                return getMutableBytes();
            }
            try {
                readWriteBytes = readWriteFuture.get();
                readWriteFuture = null;
            } catch (InterruptedException | ExecutionException e) {
                throw new IOException(e);
            }
        }
        return readWriteBytes;
    }


    /* (non-Javadoc)
     * @see org.ihtsdo.db.bdb.ConceptDataFetcherI#getMutableDataStream()
     */
    @Override
    public synchronized DataInputStream getMutableInputStream() throws IOException {
        return new DataInputStream(new ByteArrayInputStream(getMutableBytes()));
    }

    @Override
    public boolean isPrimordial() throws IOException {
        return getMutableBytes().length == 0;
    }
}
