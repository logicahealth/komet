package org.ihtsdo.otf.tcc.datastore;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentBI;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetItrBI;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.ihtsdo.otf.tcc.api.nid.ConcurrentBitSet;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.datastore.id.NidCNidMapBdb;
import org.ihtsdo.otf.tcc.datastore.temp.AceLog;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.cs.ChangeSetPolicy;
import org.ihtsdo.otf.tcc.api.cs.ChangeSetWriterThreading;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.model.cc.P;
import org.ihtsdo.otf.tcc.model.cc.attributes.ConceptAttributes;
import org.ihtsdo.otf.tcc.model.cc.attributes.ConceptAttributesRevision;
import org.ihtsdo.otf.tcc.model.cc.change.BdbCommitSequence;
import org.ihtsdo.otf.tcc.model.cc.change.LastChange;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.model.cc.description.Description;
import org.ihtsdo.otf.tcc.model.cc.description.DescriptionRevision;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexRevision;
import org.ihtsdo.otf.tcc.model.cc.relationship.Relationship;
import org.ihtsdo.otf.tcc.model.cc.relationship.RelationshipRevision;
import org.ihtsdo.otf.tcc.model.cs.ChangeSetWriterHandler;
import org.ihtsdo.otf.tcc.api.thread.NamedThreadFactory;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;
import org.ihtsdo.tcc.model.index.service.DescriptionIndexer;

public class BdbCommitManager {

    private static final int PERMIT_COUNT = 50;
    public static String pluginRoot = "plugins";
    private static final AtomicInteger writerCount = new AtomicInteger(0);
    private static boolean writeChangeSets = true;
    private static NativeIdSetBI uncommittedCNidsNoChecks = new ConcurrentBitSet();
    private static NativeIdSetBI uncommittedCNids = new ConcurrentBitSet();
    private static boolean performCreationTests = true;
    private static boolean performCommitTests = true;
    private static long lastDoUpdate = Long.MIN_VALUE;
    private static long lastCommit = Bdb.gVersion.incrementAndGet();
    private static long lastCancel = Integer.MIN_VALUE;
    private static Semaphore dbWriterPermit = new Semaphore(PERMIT_COUNT);
    private static ThreadGroup commitManagerThreadGroup =
            new ThreadGroup("commit manager threads");
    private static ExecutorService changeSetWriterService;
    private static ExecutorService dbWriterService;
    protected static DescriptionIndexer descIndexer;
    /**
     * <p> listeners </p>
     */
    private static ICommitListener[] listeners = new ICommitListener[0];

    //~--- static initializers -------------------------------------------------
    static {
        reset();
        descIndexer = Hk2Looker.get().getService(DescriptionIndexer.class);
    }

    //~--- methods -------------------------------------------------------------
    public static void addUncommitted(ConceptChronicleBI igcd) {
        if (igcd == null) {
            return;
        }

        ConceptChronicle concept = (ConceptChronicle) igcd;

        LastChange.touch(concept);
        GlobalPropertyChange.firePropertyChange(TerminologyStoreDI.CONCEPT_EVENT.ADD_UNCOMMITTED, null, concept);

        if (concept.isUncommitted() == false) {
            removeUncommitted(concept);

            try {
                dbWriterPermit.acquire();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }

            dbWriterService.execute(new SetNidsForCid(concept));
            dbWriterService.execute(new ConceptWriter(concept));

            return;
        }

        concept.modified();

        try {
            uncommittedCNids.setMember(concept.getNid());
            dbWriterPermit.acquire();
            dbWriterService.execute(new SetNidsForCid(concept));
            dbWriterService.execute(new ConceptWriter(concept));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }



    public static void addUncommittedNoChecks(ConceptChronicleBI concept) {
        ConceptChronicle c = (ConceptChronicle) concept;

        c.modified();
        LastChange.touch(c);

        if (c.isUncommitted()) {
            uncommittedCNidsNoChecks.setMember(c.getNid());
         } else {
            c = (ConceptChronicle) concept;

            removeUncommitted(c);
        }

        try {
            writeUncommitted(c);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void cancel() {
        lastCancel = Bdb.gVersion.incrementAndGet();

        synchronized (uncommittedCNids) {
            synchronized (uncommittedCNidsNoChecks) {
                    try {
                        NativeIdSetItrBI uncommittedCNidsItr = uncommittedCNids.getIterator();
                        NativeIdSetItrBI uncommittedCNidsNoChecksItr = uncommittedCNidsNoChecks.getIterator();
                        Set<Integer> cNidSet = new HashSet<>();

                        while (uncommittedCNidsItr.next()) {
                            cNidSet.addAll(ConceptChronicle.get(uncommittedCNidsItr.nid()).getConceptNidsAffectedByCommit());

                            if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                                AceLog.getAppLog().fine(
                                        "Canceling on concept: "
                                        + P.s.getComponent(uncommittedCNidsItr.nid()).toUserString() + " UUID: "
                                        + P.s.getUuidsForNid(uncommittedCNidsItr.nid()).toString());
                            }
                        }

                        while (uncommittedCNidsNoChecksItr.next()) {
                            cNidSet.addAll(
                                    ConceptChronicle.get(uncommittedCNidsNoChecksItr.nid()).getConceptNidsAffectedByCommit());

                            if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                                AceLog.getAppLog().fine(
                                        "Canceling on concept: "
                                        + P.s.getComponent(uncommittedCNidsNoChecksItr.nid()).toUserString()
                                        + " UUID: "
                                        + P.s.getUuidsForNid(uncommittedCNidsNoChecksItr.nid()).toString());
                            }
                        }

                        LastChange.touchComponents(cNidSet);
                        Bdb.getStampDb().commit(Long.MIN_VALUE);
                        Bdb.getStampDb().commit(Long.MIN_VALUE);
                        handleCanceledConcepts(uncommittedCNids);
                        handleCanceledConcepts(uncommittedCNidsNoChecks);
                        uncommittedCNidsNoChecks.clear();
                        uncommittedCNids.clear();
                    } catch (IOException e1) {
                        AceLog.getAppLog().alertAndLogException(e1);
                    }
            }
        }
    }

    public static boolean commit(ChangeSetPolicy changeSetPolicy,
            ChangeSetWriterThreading changeSetWriterThreading) {

        boolean passedRelease = false;
        boolean performCommit = true;

        try {
            synchronized (uncommittedCNids) {
                synchronized (uncommittedCNidsNoChecks) {
                        NativeIdSetBI allUncommitted = new ConcurrentBitSet();
                        allUncommitted.or(uncommittedCNids);
                        allUncommitted.or(uncommittedCNidsNoChecks);
                        try {
                            GlobalPropertyChange.fireVetoableChange(TerminologyStoreDI.CONCEPT_EVENT.PRE_COMMIT, null, allUncommitted);
                        } catch (PropertyVetoException ex) {
                            return false;
                        }

                        if (performCreationTests) {
                            NativeIdSetItrBI uncommittedCNidItr = uncommittedCNids.getIterator();

                        if (performCommit) {
                            lastCommit = Bdb.gVersion.incrementAndGet();
                            if (Bdb.annotationConcepts != null) {
                                for (ConceptChronicle annotationConcept : Bdb.annotationConcepts) {
                                    dbWriterService.execute(new ConceptWriter(annotationConcept));
                                }
                                Bdb.annotationConcepts.clear();
                            }


                            while (uncommittedCNidItr.next()) {
                                
                                    int cnid = uncommittedCNidItr.nid();
                                    ConceptChronicle c = ConceptChronicle.get(cnid);

                                    c.modified(lastCommit);
                                
                            }

                            NativeIdSetItrBI uncommittedCNidItrNoChecks = uncommittedCNidsNoChecks.getIterator();

                            long commitTime = System.currentTimeMillis();
                            NidSetBI sapNidsFromCommit = Bdb.getStampDb().commit(commitTime);

                            if (writeChangeSets && (sapNidsFromCommit.size() > 0)) {
                                if (changeSetPolicy == null) {
                                    changeSetPolicy = ChangeSetPolicy.OFF;
                                }

                                if (changeSetWriterThreading == null) {
                                    changeSetWriterThreading = ChangeSetWriterThreading.SINGLE_THREAD;
                                }

                                switch (changeSetPolicy) {
                                    case COMPREHENSIVE:
                                    case INCREMENTAL:
                                    case MUTABLE_ONLY:
                                        uncommittedCNidsNoChecks.or(uncommittedCNids);

                                        if (uncommittedCNidsNoChecks.size() > 0) {
                                            ChangeSetWriterHandler handler =
                                                    new ChangeSetWriterHandler(uncommittedCNidsNoChecks, commitTime,
                                                    sapNidsFromCommit, changeSetPolicy.convert(),
                                                    changeSetWriterThreading);

                                            changeSetWriterService.execute(handler);
                                            passedRelease = true;
                                        }

                                        break;

                                    case OFF:
                                        break;

                                    default:
                                        throw new RuntimeException("Can't handle policy: " + changeSetPolicy);
                                }
                            }

                            notifyCommit();
                            uncommittedCNids.clear();
                            uncommittedCNidsNoChecks = Bdb.getConceptDb().getEmptyIdSet();
                            descIndexer.commitToLucene();
                        }
                        GlobalPropertyChange.firePropertyChange(TerminologyStoreDI.CONCEPT_EVENT.POST_COMMIT, null, allUncommitted);

                    }
                }
            }

            if (performCommit) {
                Bdb.sync();
                BdbCommitSequence.nextSequence();
            }
        } catch (Exception e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }


        if (performCommit) {
            return true;
        }

        return false;
    }

    public static boolean commit(ConceptChronicle c, ChangeSetPolicy changeSetPolicy,
            ChangeSetWriterThreading changeSetWriterThreading) {
        if ((uncommittedCNids.size() == 1) && (uncommittedCNidsNoChecks.size() == 1)
                && uncommittedCNids.isMember(c.getNid()) && uncommittedCNidsNoChecks.isMember(c.getNid())) {
            return commit(changeSetPolicy, changeSetWriterThreading);
        } else if ((uncommittedCNids.size() == 1) && (uncommittedCNidsNoChecks.isEmpty())
                && uncommittedCNids.isMember(c.getNid())) {
            return commit(changeSetPolicy, changeSetWriterThreading);
        } else if ((uncommittedCNids.isEmpty()) && (uncommittedCNidsNoChecks.size() == 1)
                && uncommittedCNidsNoChecks.isMember(c.getNid())) {
            return commit(changeSetPolicy, changeSetWriterThreading);
        }

        NativeIdSetBI allUncommitted = new ConcurrentBitSet();
        allUncommitted.setMember(c.getConceptNid());
        try {
            GlobalPropertyChange.fireVetoableChange(TerminologyStoreDI.CONCEPT_EVENT.PRE_COMMIT, null, allUncommitted);
        } catch (PropertyVetoException ex) {
            return false;
        }

        boolean performCommit = true;

        try {
            AceLog.getAppLog().info("Committing concept: " + c.toUserString() + " UUID: "
                    + P.s.getUuidsForNid(c.getNid()).toString());
            if (performCommit) {
                BdbCommitSequence.nextSequence();

                for (ConceptChronicle annotationConcept : Bdb.annotationConcepts) {
                    dbWriterService.execute(new ConceptWriter(annotationConcept));
                }

                Bdb.annotationConcepts.clear();

                long commitTime = System.currentTimeMillis();
                NidSetBI sapNidsFromCommit = c.setCommitTime(commitTime);
                NativeIdSetBI commitSet = new ConcurrentBitSet();

                commitSet.setMember(c.getNid());
                c.modified();
                Bdb.getConceptDb().writeConcept(c);



                if (writeChangeSets) {
                    if (changeSetPolicy == null) {
                        changeSetPolicy = ChangeSetPolicy.OFF;
                    }

                    if (changeSetWriterThreading == null) {
                        changeSetWriterThreading = ChangeSetWriterThreading.SINGLE_THREAD;
                    }

                    switch (changeSetPolicy) {
                        case COMPREHENSIVE:
                        case INCREMENTAL:
                        case MUTABLE_ONLY:
                            ChangeSetWriterHandler handler = new ChangeSetWriterHandler(commitSet, commitTime,
                                    sapNidsFromCommit, changeSetPolicy.convert(),
                                    changeSetWriterThreading);

                            changeSetWriterService.execute(handler);

                            break;

                        case OFF:
                            break;

                        default:
                            throw new RuntimeException("Can't handle policy: " + changeSetPolicy);
                    }
                }

                uncommittedCNids.andNot(commitSet);
                uncommittedCNidsNoChecks.andNot(commitSet);
                descIndexer.commitToLucene(c);
            }
        } catch (Exception e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }

        GlobalPropertyChange.firePropertyChange(TerminologyStoreDI.CONCEPT_EVENT.POST_COMMIT, null, allUncommitted);
 
        if (performCommit) {
            return true;
        }

        return false;
    }

    public static boolean forget(ConceptAttributeVersionBI attr) throws IOException {
        ConceptChronicle c = Bdb.getConcept(attr.getConceptNid());
      ConceptAttributes a = (ConceptAttributes) attr;

        if ((a.getTime() != Long.MAX_VALUE) && (a.getTime() != Long.MIN_VALUE)) {

            // Only need to forget additional versions;
            if (a.revisions != null) {
                synchronized (a.revisions) {
                    List<ConceptAttributesRevision> toRemove = new ArrayList<>();
                    Iterator<ConceptAttributesRevision> ri = a.revisions.iterator();

                    while (ri.hasNext()) {
                        ConceptAttributesRevision ar = ri.next();

                        if (ar.getTime() == Long.MAX_VALUE) {
                            toRemove.add(ar);
                        }
                    }

                    for (ConceptAttributesRevision r : toRemove) {
                        a.removeRevision(r);
                        r.stamp = -1;
                    }
                }
            }

            addUncommittedNoChecks(c);
        } else {
            a.primordialStamp = -1;

            return true;
        }

        return false;
    }


   public static void forget(DescriptionVersionBI desc) throws IOException {
      Description d = (Description) desc;
        ConceptChronicle c = Bdb.getConcept(d.getConceptNid());

        if (d.getTime() != Long.MAX_VALUE) {

            // Only need to forget additional versions;
            if (d.revisions == null) {
                throw new UnsupportedOperationException("Cannot forget a committed component.");
            } else {
                synchronized (d.revisions) {
                    List<DescriptionRevision> toRemove = new ArrayList<>();
                    Iterator<DescriptionRevision> di = d.revisions.iterator();

                    while (di.hasNext()) {
                        DescriptionRevision dr = di.next();

                        if (dr.getTime() == Long.MAX_VALUE) {
                            toRemove.add(dr);
                        }
                    }

                    for (DescriptionRevision tr : toRemove) {
                        d.removeRevision(tr);
                        tr.stamp = -1;
                    }
                }
            }
        } else {

            // have to forget "all" references to component...
            c.getDescriptions().remove(d);
            c.getData().getDescNids().remove(d.getNid());
            d.primordialStamp = -1;
        }

        c.modified();
        addUncommittedNoChecks(c);
    }

   @SuppressWarnings("unchecked")
   public static void forget(RefexChronicleBI extension) throws IOException {
      RefexMember m         = (RefexMember) extension;
      ConceptChronicle      c         = Bdb.getConcept(m.getRefexExtensionNid());
      ComponentBI  component = Bdb.getComponent(m.getReferencedComponentNid());

        if (component instanceof ConceptChronicle) {
            component = ((ConceptChronicle) component).getConceptAttributes();
        }

        ConceptComponent comp = (ConceptComponent) component;

        if (m.getTime() != Long.MAX_VALUE) {

            // Only need to forget additional versions;
            if (m.revisions == null) {
                throw new UnsupportedOperationException("Cannot forget a committed component.");
            } else {
                synchronized (m.revisions) {
                    List<RefexRevision<?, ?>> toRemove = new ArrayList<>();
                    Iterator<?> mi = m.revisions.iterator();

                    while (mi.hasNext()) {
                        RefexRevision<?, ?> mr = (RefexRevision<?, ?>) mi.next();

                        if (mr.getTime() == Long.MAX_VALUE) {
                            toRemove.add(mr);
                        }
                    }

                    for (RefexRevision tr : toRemove) {
                        m.removeRevision(tr);
                        tr.stamp = -1;
                    }
                }
            }
        } else {

            // have to forget "all" references to component...
            if (c.isAnnotationStyleRefex()) {
                comp.getAnnotationsMod().remove(m);
            } else {
                c.getRefsetMembers().remove(m);
                c.getData().getMemberNids().remove(m.getNid());
            }

            m.setSTAMP(-1);
        }


        c.modified();
        addUncommittedNoChecks(c);
    }


   public static void forget(ConceptChronicleBI concept) throws IOException {
      ConceptChronicle c = (ConceptChronicle) concept;

        c.cancel();
    }


   public static void forget(RelationshipVersionBI rel) throws IOException {
      ConceptChronicle      c = Bdb.getConcept(rel.getOriginNid());
      Relationship r = (Relationship) rel;

        if (r.getTime() != Long.MAX_VALUE) {

            // Only need to forget additional versions;
            if (r.revisions == null) {
                throw new UnsupportedOperationException("Cannot forget a committed component.");
            } else {
                synchronized (r.revisions) {
                    List<RelationshipRevision> toRemove = new ArrayList<>();
                    Iterator<RelationshipRevision> ri = r.revisions.iterator();

                    while (ri.hasNext()) {
                        RelationshipRevision rr = ri.next();

                        if (rr.getTime() == Long.MAX_VALUE) {
                            toRemove.add(rr);
                        }
                    }

                    for (RelationshipRevision tr : toRemove) {
                        r.removeRevision(tr);
                    }
                }
            }
        } else {

            // have to forget "all" references to component...
            c.getRelationshipsOutgoing().remove((Relationship) rel);
            c.getData().getSrcRelNids().remove(rel.getNid());
            r.primordialStamp = -1;
        }

        c.modified();
        addUncommittedNoChecks(c);
    }

    private static void handleCanceledConcepts(NativeIdSetBI uncommittedCNids2) throws IOException {
        NativeIdSetItrBI idItr = uncommittedCNids2.getIterator();

        while (idItr.next()) {
            try {
                ConceptChronicle c = ConceptChronicle.get(idItr.nid());

                if (c.isCanceled()) {
                    forget(c);
                }

                c.flushVersions();
                c.modified();
                c.setLastWrite(Bdb.gVersion.incrementAndGet());
            } catch (Exception ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }
    }

    private static void notifyCommit() {
        if ((listeners != null) && (listeners.length > 0)) {
            final CommitEvent event;

            event = new CommitEvent(uncommittedCNidsNoChecks);

            for (final ICommitListener listener : listeners) {
                try {
                    listener.afterCommit(event);
                } catch (final Exception exception) {

                    // @todo handle exception
                    exception.printStackTrace();
                }
            }
        }
    }

    /**
     * <p> notify the commit event </p>
     */
    private static void notifyShutdown() {
        if ((listeners != null) && (listeners.length > 0)) {
            for (final ICommitListener listener : listeners) {
                try {
                    listener.shutdown();
                } catch (final Exception exception) {

                    // @todo handle exception
                    exception.printStackTrace();
                }
            }
        }
    }

    public static void removeUncommitted(final ConceptChronicle concept) {
        if (uncommittedCNids.isMember(concept.getNid())) {
            uncommittedCNids.setNotMember(concept.getNid());
        }
    }

    public static void reset() {
        changeSetWriterService = Executors.newFixedThreadPool(1,
                new NamedThreadFactory(commitManagerThreadGroup, "Change set writer"));
        dbWriterService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
                new NamedThreadFactory(commitManagerThreadGroup, "Db writer"));
    }

    public static void resumeChangeSetWriters() {
        writeChangeSets = true;
    }

    public static void shutdown() throws InterruptedException {
        cancel();
        AceLog.getAppLog().info("Shutting down dbWriterService.");
        dbWriterService.shutdown();
        AceLog.getAppLog().info("Awaiting termination of dbWriterService.");
        dbWriterService.awaitTermination(90, TimeUnit.MINUTES);
        AceLog.getAppLog().info("Shutting down changeSetWriterService.");
        changeSetWriterService.shutdown();
        AceLog.getAppLog().info("Awaiting termination of changeSetWriterService.");
        changeSetWriterService.awaitTermination(90, TimeUnit.MINUTES);
        AceLog.getAppLog().info("BdbCommitManager is shutdown.");
        notifyShutdown();
    }

    public static void suspendChangeSetWriters() {
        writeChangeSets = false;
    }

    public static void waitTillWritesFinished() {
        if (writerCount.get() > 0) {
            try {
                dbWriterPermit.acquireUninterruptibly(PERMIT_COUNT);
            } finally {
                dbWriterPermit.release(PERMIT_COUNT);
            }
        }
    }

    private static void writeUncommitted(ConceptChronicle c) throws InterruptedException {
        if (c != null) {
            dbWriterPermit.acquire();
            dbWriterService.execute(new SetNidsForCid(c));
            dbWriterService.execute(new ConceptWriter(c));
        }
    }

    //~--- get methods ---------------------------------------------------------


    public static long getLastCancel() {
        return lastCancel;
    }

    public static long getLastCommit() {
        return lastCommit;
    }

    public static Set<ConceptChronicle> getUncommitted() {
        try {
            Set<ConceptChronicle> returnSet = new HashSet<>();
            NativeIdSetItrBI cNidItr = uncommittedCNids.getIterator();

            while (cNidItr.next()) {
                returnSet.add(ConceptChronicle.get(cNidItr.nid()));
            }

            cNidItr = uncommittedCNidsNoChecks.getIterator();

            while (cNidItr.next()) {
                returnSet.add(ConceptChronicle.get(cNidItr.nid()));
            }

            return returnSet;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isCheckCommitDataEnabled() {
        return performCommitTests;
    }

    public static boolean isCheckCreationDataEnabled() {
        return performCreationTests;
    }

    //~--- set methods ---------------------------------------------------------
    public static void setCheckCommitDataEnabled(boolean enabled) {
        performCommitTests = enabled;
    }

    public static void setCheckCreationDataEnabled(boolean enabled) {
        performCreationTests = enabled;
    }

    //~--- inner classes -------------------------------------------------------
    public static class AskToContinue implements Runnable {

        private boolean continueWithCommit;

        //~--- methods ----------------------------------------------------------
        @Override
        public void run() {
            int selection = JOptionPane.showConfirmDialog(new JFrame(), "Do you want to continue with commit?",
                    "Warnings Detected", JOptionPane.YES_NO_OPTION);

            continueWithCommit = selection == JOptionPane.YES_OPTION;
        }
    }

    private static class ConceptWriter implements Runnable {

        private ConceptChronicle c;

        //~--- constructors -----------------------------------------------------
        public ConceptWriter(ConceptChronicle c) {
            super();
            assert c.readyToWrite();
            this.c = c;
            writerCount.incrementAndGet();
        }

        //~--- methods ----------------------------------------------------------
        @Override
        public void run() {
            try {
                while (c.isUnwritten() && !c.isCanceled()) {
                    Bdb.getConceptDb().writeConcept(c);
                }
            } catch (Throwable e) {
                String exceptionStr = "Exception Writing: " + c.toLongString();
                Exception newEx = new Exception(exceptionStr, e);

                System.out.println(exceptionStr + "\n\n" + e.toString());
                AceLog.getAppLog().alertAndLogException(newEx);
            } finally {
                dbWriterPermit.release();
                writerCount.decrementAndGet();
            }
        }
    }

    private static class SetNidsForCid implements Runnable {

        ConceptChronicle concept;

        //~--- constructors -----------------------------------------------------
        public SetNidsForCid(ConceptChronicle concept) {
            super();
            this.concept = concept;
        }

        //~--- methods ----------------------------------------------------------
        @Override
        public void run() {
            try {
                Collection<Integer> nids = concept.getAllNids();
                NidCNidMapBdb nidCidMap = Bdb.getNidCNidMap();

                for (int nid : nids) {
                    nidCidMap.setCNidForNid(concept.getNid(), nid);
                }
            } catch (IOException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }
    }
}
