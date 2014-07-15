/**
 *
 */
package org.ihtsdo.otf.tcc.datastore.concept;

//~--- non-JDK imports --------------------------------------------------------
import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;


import org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.datastore.temp.AceLog;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptVersion;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.otf.tcc.api.concept.ConceptFetcherBI;

/**
 * Class description
 *
 *
 * @version Enter version here..., 13/04/25
 * @author Enter your name here...
 */
public class ParallelConceptIterator implements Callable<Boolean>, ConceptFetcherBI {

    static boolean verbose = true;
    /**
     * Field description
     */
    private int processedCount = 0;
    /**
     * Field description
     */
    private boolean stop = false;
    /**
     * Field description
     */
    private ProcessUnfetchedConceptDataBI processor;
    /**
     * Field description
     */
    private int first;
    /**
     * Field description
     */
    private int last;
    /**
     * Field description
     */
    private int countToProcess;

    /**
     * Field description
     */
    private Database mutable;

    /**
     * Field description
     */
    private int currentCNid;

    /**
     * Field description
     */
    private Cursor mutableCursor;
    /**
     * Field description
     */
    private DatabaseEntry aKey;
    /**
     * Field description
     */
    private DatabaseEntry roFoundData;
    /**
     * Field description
     */
    private DatabaseEntry mutableFoundData;
    /**
     * Field description
     */
    private Thread currentThread;
    /**
     * For reporting progress.
     */
    private ParallelConceptIteratorTask task;

    public void setTask(ParallelConceptIteratorTask task) {
        this.task = task;
    }

    /**
     * Enum description
     *
     */
    private enum FETCH {

        ONE, TWO, THREE
    };

    /**
     * Constructs ...
     *
     *
     * @param first
     * @param last
     * @param count
     * @param processor
     * @param mutable
     */
    public ParallelConceptIterator(int first, int last, int count, ProcessUnfetchedConceptDataBI processor,
            Database mutable) {
        super();
        this.first = first;
        this.last = last;
        this.countToProcess = count;
        this.processor = processor;
        this.mutable = mutable;
        aKey = new DatabaseEntry();
        aKey.setPartial(false);
        roFoundData = new DatabaseEntry();
        roFoundData.setPartial(false);
        mutableFoundData = new DatabaseEntry();
        mutableFoundData.setPartial(false);
        currentThread = Thread.currentThread();
    }

    /**
     * Method description
     *
     *
     * @param mutableCursor
     * @param mutableFoundKey
     * @param mutableFoundData
     *
     * @return
     */
    private int advanceCursor(Cursor mutableCursor, DatabaseEntry mutableFoundKey,
            DatabaseEntry mutableFoundData) {
        if (stop) {
            return Integer.MAX_VALUE;
        }

        int mutableKey;

        if (mutableCursor.getNext(mutableFoundKey, mutableFoundData, LockMode.READ_UNCOMMITTED)
                == OperationStatus.SUCCESS) {
            mutableKey = IntegerBinding.entryToInt(mutableFoundKey);
        } else {
            mutableKey = Integer.MAX_VALUE;
        }

        return mutableKey;
    }

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws Exception
     */
    @Override
    public Boolean call() throws Exception {
        if (task != null) {
            task.updateMessage("iterating");
            task.updateProgress(0, countToProcess);
        }
        CursorConfig cursorConfig = new CursorConfig();

        cursorConfig.setReadUncommitted(true);
        mutableCursor = mutable.openCursor(null, cursorConfig);

        int mutableKey = first;
        if (verbose) {
            System.out.println("Parallel concept iterator starting.\n" + " First: " + first + " last: "
                    + last + " mutableKey: " + mutableKey
                    + " processedCount: " + processedCount + " countToProcess: "
                    + countToProcess);
        }

        try {

            DatabaseEntry mutableFoundKey = new DatabaseEntry();

            IntegerBinding.intToEntry(mutableKey, mutableFoundKey);

            DatabaseEntry mutableFoundDataPartial = new DatabaseEntry();

            mutableFoundDataPartial.setPartial(true);
            mutableFoundDataPartial.setPartial(0, 0, true);
            mutableKey = setupCursor(mutableCursor, mutableFoundKey, mutableFoundDataPartial);

            while (((mutableKey <= last)) && processor.continueWork()) {

                    currentCNid = mutableKey;
                    processor.processUnfetchedConceptData(currentCNid, this);
                    processedCount++;
                    if (task != null) {
                        if (processedCount <= countToProcess) {
                            task.updateProgress(processedCount, countToProcess);
                        } else {
                            task.updateProgress(processedCount, processedCount);
                        }
                    }

                    if (mutableKey < last) {
                        mutableKey = advanceCursor(mutableCursor, mutableFoundKey, mutableFoundDataPartial);
                    } else {
                        mutableKey = Integer.MAX_VALUE;
                    }

            }

            if (verbose) {
                System.out.println("Parallel concept iterator finished.\n" + " First: " + first + " last: "
                        + last + " mutableKey: " + mutableKey
                        + " processedCount: " + processedCount + " countToProcess: "
                        + countToProcess);
            }
            if (task != null) {
                task.updateMessage("Finished. Processed: " + processedCount + " items");
                task.updateProgress(countToProcess, countToProcess);
            }
            return true;
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw new Exception(ex);
        } finally {

            mutableCursor.close();
        }
    }

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws Exception
     */
    @Override
    public ConceptChronicle fetch() throws Exception {
        return fetchThree();
    }

    /**
     * Method description
     *
     *
     * @param vc
     *
     * @return
     *
     * @throws Exception
     */
    @Override
    public ConceptVersion fetch(ViewCoordinate vc) throws Exception {
        ConceptChronicle c = fetch();

        if (c != null) {
            return c.getVersion(vc);
        }

        return null;
    }

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws IOException
     */
    private ConceptChronicle fetchThree() throws IOException {
        ConceptChronicle c = ConceptChronicle.getIfInMap(currentCNid);

        if (c != null) {
            return c;
        }

        mutableCursor.getCurrent(aKey, mutableFoundData, LockMode.READ_UNCOMMITTED);

        //return ConceptChronicle.get(currentCNid, new byte[0], mutableFoundData.getData());
        throw new UnsupportedOperationException();
    }


    /**
     * Method description
     *
     *
     * @param cursor
     * @param foundKey
     * @param foundData
     *
     * @return
     */
    private int setupCursor(Cursor cursor, DatabaseEntry foundKey, DatabaseEntry foundData) {
        int cNid;

        if (cursor.getSearchKeyRange(foundKey, foundData, LockMode.READ_UNCOMMITTED)
                == OperationStatus.SUCCESS) {
            cNid = IntegerBinding.entryToInt(foundKey);
        } else {
            cNid = Integer.MAX_VALUE;
        }

        return cNid;
    }

    /**
     * Method description
     *
     */
    public void stop() {
        this.stop = true;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public Thread getCurrentThread() {
        return currentThread;
    }

    /**
     * Method description
     *
     *
     * @param currentThread
     */
    public void setCurrentThread(Thread currentThread) {
        this.currentThread = currentThread;
    }
}
