package sh.isaac.provider.elk;

import org.semanticweb.elk.loading.AxiomLoader;
import org.semanticweb.elk.loading.ElkLoadingException;
import org.semanticweb.elk.owl.interfaces.ElkAxiom;
import org.semanticweb.elk.owl.visitors.ElkAxiomProcessor;
import org.semanticweb.elk.util.concurrent.computation.InterruptMonitor;
import sh.isaac.api.DataSource;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.stream.VersionStream;
import sh.isaac.model.logic.LogicalExpressionImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.stream.Stream;

public class ElkLoader implements AxiomLoader, AxiomLoader.Factory {
    /**
     * a special batch to detect that all axioms are loaded
     */
    private static final ArrayList<ElkAxiom> POISON_BATCH = new ArrayList<ElkAxiom>(
            1);
    /**
     * the maximum number of axioms in the exchange batch
     */
    private final GraphToElkTranslator graphToElkTranslator = new GraphToElkTranslator();
    private final int batchLength;
    private final SynchronousQueue<ArrayList<ElkAxiom>> axiomExchanger;
    private boolean started;
    private volatile boolean finished;
    /**
     * the exception created if something goes wrong
     */
    protected volatile Exception exception;
    /**
     * the expression stream to obtain the axioms from
     */
    private final VersionStream<LogicGraphVersion> expressionStream;

    /**
     * the thread in which the expression translator is running
     */
    private final Thread translatorThread;

    public ElkLoader(VersionStream<LogicGraphVersion> expressionStream, int batchLength) {
        this.expressionStream = expressionStream;
        this.batchLength = batchLength;
        this.axiomExchanger = new SynchronousQueue<>();
        this.started = false;
        this.finished = false;
        this.exception = null;
        this.translatorThread = new Thread(new Translator(), "komet-to-elk-translator-thread");
    }

    @Override
    public synchronized void load(ElkAxiomProcessor axiomInserter,
                                  ElkAxiomProcessor axiomDeleter) throws ElkLoadingException {
        if (finished)
            return;

        if (!started) {
            translatorThread.start();
            started = true;
        }

        ArrayList<ElkAxiom> nextBatch;

        for (;;) {
            if (isInterrupted())
                break;
            try {
                nextBatch = axiomExchanger.take();
            } catch (InterruptedException e) {
                /*
                 * we don't know for sure why the thread was interrupted, so we
                 * need to obey; if interrupt was not relevant, the process will
                 * restart; we need to restore the interrupt status so that the
                 * called methods know that there was an interrupt
                 */
                Thread.currentThread().interrupt();
                break;
            }
            if (nextBatch == POISON_BATCH) {
                break;
            }
            for (int i = 0; i < nextBatch.size(); i++) {
                ElkAxiom axiom = nextBatch.get(i);
                axiomInserter.visit(axiom);
            }
        }
        if (exception != null) {
            throw new ElkLoadingException(exception);
        }
    }

    @Override
    public boolean isLoadingFinished() {
        return finished;
    }


    /**
     * The translator worker used to translate the axioms
     *
     *
     */
    private class Translator implements Runnable {
        @Override
        public void run() {
            try {
                AxiomInserter axiomInserter = new AxiomInserter(axiomExchanger);
                expressionStream.forEach(latestVersion -> {
                    List<ElkAxiom> elkAxioms = translate(latestVersion); // translate here...
                    try {
                        for (ElkAxiom axiom: elkAxioms) {
                            axiomInserter.visit(axiom);
                        }

                    } catch (InterruptedException e) {
                        finished = true;
                        exception = e;
                        throw new RuntimeException(e);
                    }
                });
            } catch (Throwable e) {
                exception = new ElkLoadingException(
                        "Cannot load the ontology!", e);
            } finally {
                finished = true;
                try {
                    axiomExchanger.put(POISON_BATCH);
                } catch (InterruptedException e) {
                    /*
                     * we don't know what is causing this but we need to obey;
                     * consistency of the computation for such interrupt is not
                     * guaranteed; restore the interrupt status and exit
                     */
                    Thread.currentThread().interrupt();
                }
            }
        }

        private List<ElkAxiom> translate(LatestVersion<LogicGraphVersion> latestVersion) {
            if (latestVersion.isPresent()) {
                LogicGraphVersion logicGraphVersion = latestVersion.get();
                if (logicGraphVersion.getReferencedComponentNid() >= 0) {
                    throw new IllegalStateException("Referenced component nid must be negative: " + logicGraphVersion.getReferencedComponentNid());
                }
                final LogicalExpressionImpl logicGraph = new LogicalExpressionImpl(logicGraphVersion.getGraphData(),
                        DataSource.INTERNAL);
                List<ElkAxiom> axiomList = graphToElkTranslator.translate(logicGraphVersion);

                return axiomList;
            }
            return new ArrayList<>(0);
        }
    }

    /**
     * A simple {@link ElkAxiomProcessor} that insert the parsed axioms into the
     * given queue
     *
     * @author "Yevgeny Kazakov"
     *
     */
    private class AxiomInserter {

        final private BlockingQueue<ArrayList<ElkAxiom>> axiomBuffer;

        /**
         * the next batch of axioms that should be filled
         */
        private ArrayList<ElkAxiom> nextBatch;

        AxiomInserter(BlockingQueue<ArrayList<ElkAxiom>> axiomBuffer) {
            this.axiomBuffer = axiomBuffer;
            nextBatch = new ArrayList<>(batchLength);
        }

        public void visit(ElkAxiom elkAxiom) throws InterruptedException {
            nextBatch.add(elkAxiom);
            if (nextBatch.size() == batchLength) {
                submitBatch();
                nextBatch = new ArrayList<>(batchLength);
            }
        }

        public void finish() throws InterruptedException {
            // submit the last partially filled batch
            submitBatch();
        }

        private void submitBatch() throws InterruptedException {
                axiomBuffer.put(nextBatch);
        }
    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean isInterrupted() {
        return false;
    }

    @Override
    public AxiomLoader getAxiomLoader(InterruptMonitor interrupter) {
        return this;
    }
}
