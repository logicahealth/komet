
/**
 *
 */
package org.ihtsdo.otf.tcc.chronicle.cc.concept;

import com.sleepycat.bind.tuple.TupleInput;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import org.ihtsdo.otf.tcc.chronicle.cc.NidPair;
import org.ihtsdo.otf.tcc.chronicle.cc.P;
import org.ihtsdo.otf.tcc.chronicle.cc.component.AnnotationIndexBinder;
import org.ihtsdo.otf.tcc.chronicle.cc.component.AnnotationStyleBinder;
import org.ihtsdo.otf.tcc.chronicle.cc.component.DataVersionBinder;
import org.ihtsdo.otf.tcc.chronicle.cc.description.Description;
import org.ihtsdo.otf.tcc.chronicle.cc.lucene.LuceneManager;
import org.ihtsdo.otf.tcc.chronicle.cc.media.Media;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.chronicle.cc.relationship.Relationship;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;

/**
 * File format:<br>
 *
 * @author kec
 *
 */
public abstract class ConceptDataManager implements I_ManageConceptData {

   /**
    * When the number of refset members are greater than this value, use a map
    * for looking up members instead of iterating through a list.
    */
   protected static int useMemberMapThreshold = 15;

   //~--- fields --------------------------------------------------------------

   protected long                lastChange         = Long.MIN_VALUE;
   protected long                lastWrite          = Long.MIN_VALUE;
   protected long                lastExtinctRemoval = Long.MIN_VALUE;
   protected ConceptChronicle             enclosingConcept;
   protected ConceptDataFetcherI nidData;

   //~--- constructors --------------------------------------------------------

   public ConceptDataManager(ConceptDataFetcherI nidData) throws IOException {
      super();
      this.nidData            = nidData;
      this.lastChange         = getDataVersion();
      this.lastWrite          = this.lastChange;
      this.lastExtinctRemoval = P.s.getSequence();
   }

   //~--- methods -------------------------------------------------------------

   /*
    * (non-Javadoc)
    *
    * @see
    * org.ihtsdo.db.bdb.concept.I_ManageConceptData#add(org.ihtsdo.db.bdb.concept
    * .component.description.Description)
    */
   @Override
   public void add(Description desc) throws IOException {
      getDescriptions().addDirect(desc);
      getDescNids().add(desc.nid);
      modified();
   }

   /*
    * (non-Javadoc)
    *
    * @see
    * org.ihtsdo.db.bdb.concept.I_ManageConceptData#add(org.ihtsdo.db.bdb.concept
    * .component.image.Image)
    */
   @Override
   public void add(Media img) throws IOException {
      getImages().addDirect(img);
      getImageNids().add(img.nid);
      modified();
   }

   /*
    * (non-Javadoc)
    *
    * @see
    * org.ihtsdo.db.bdb.concept.I_ManageConceptData#add(org.ihtsdo.db.bdb.concept
    * .component.relationship.Relationship)
    */
   @Override
   public void add(Relationship rel) throws IOException {
      getSourceRels().add(rel);
      getSrcRelNids().add(rel.nid);
      modified();
   }

   protected abstract void addToMemberMap(RefexMember<?, ?> refsetMember);

   protected long checkFormatAndVersion(TupleInput input) throws UnsupportedEncodingException {
      input.mark(128);

      int  formatVersion = input.readInt();
      long dataVersion   = input.readLong();

      if (formatVersion != OFFSETS.CURRENT_FORMAT_VERSION) {
         throw new UnsupportedEncodingException("No support for format version: " + formatVersion);
      }

      input.reset();

      return dataVersion;
   }

   @Override
   public void modified() {
      lastChange = P.s.incrementAndGetSequence();
   }

   @Override
   public void modified(long sequence) {
      lastChange = sequence;
   }

   void processNewDesc(Description e) throws IOException {
      assert e.nid != 0 : "descNid is 0: " + this;
      getDescNids().add(e.nid);
      LuceneManager.addUncommittedDescNid(e.nid);
      modified();
   }

   void processNewImage(Media img) throws IOException {
      assert img.nid != 0 : "imgNid is 0: " + this;
      getImageNids().add(img.nid);
      modified();
   }

   void processNewRefsetMember(RefexMember<?, ?> refsetMember) throws IOException {
      assert refsetMember != null : "refsetMember is null: " + this;
      assert refsetMember.nid != 0 : "memberNid is 0: " + this;
      assert refsetMember.getReferencedComponentNid() != 0 : "componentNid is 0: " + this;
      assert refsetMember.enclosingConceptNid != 0 : "refsetNid is 0: " + this;

      if (!isAnnotationStyleRefex()) {
         getMemberNids().add(refsetMember.nid);
         addToMemberMap(refsetMember);
         modified();
         P.s.addXrefPair(refsetMember.getReferencedComponentNid(),
                         NidPair.getRefexNidMemberNidPair(refsetMember.getRefexExtensionNid(), refsetMember.getNid()));
      }
   }

   void processNewRel(Relationship rel) throws IOException {
      assert rel != null : "rel is null: " + this;
      assert rel.nid != 0 : "relNid is 0: " + this;
      assert rel.getTypeNid() != 0 : "relTypeNid is 0: " + this;
      assert P.s.getConceptForNid(rel.nid) != null :
             "No concept for component: " + rel.nid + "\nsourceConcept: "
             + this.enclosingConcept.toLongString() + "\ndestConcept: "
             + ConceptChronicle.get(rel.getDestinationNid()).toLongString();
      P.s.addRelOrigin(rel.getDestinationNid(), rel.getOriginNid());
      getSrcRelNids().add(rel.nid);
      modified();
   }

   @Override
   public void resetNidData() {
      this.nidData.reset();
   }

   @Override
   public String toString() {
      return enclosingConcept.toLongString();
   }

   //~--- get methods ---------------------------------------------------------

   /*
    * (non-Javadoc)
    *
    * @see org.ihtsdo.db.bdb.concept.I_ManageConceptData#getAllNids()
    */
   @Override
   public Collection<Integer> getAllNids() throws IOException {
      Collection<Integer> descNids   = getDescNids();
      Collection<Integer> srcRelNids = getSrcRelNids();
      Collection<Integer> imgNids    = getImageNids();
      Collection<Integer> memberNids = new ArrayList<>(0);

      if (!isAnnotationStyleSet()) {
         memberNids = getMemberNids();
      }

      int                size             = 1 + descNids.size() + srcRelNids.size() + imgNids.size() + memberNids.size();
      ArrayList<Integer> allContainedNids = new ArrayList<>(size);

      allContainedNids.add(enclosingConcept.getNid());
      assert enclosingConcept.getNid() != 0;
      assert !descNids.contains(0);
      allContainedNids.addAll(descNids);
      assert !srcRelNids.contains(0);
      allContainedNids.addAll(srcRelNids);
      assert !imgNids.contains(0);
      allContainedNids.addAll(imgNids);
      assert !memberNids.contains(0);
      allContainedNids.addAll(memberNids);

      return allContainedNids;
   }

   private long getDataVersion() throws IOException {
      TupleInput readOnlyInput = nidData.getReadOnlyTupleInput();
      long       dataVersion   = Long.MIN_VALUE;

      if (readOnlyInput.available() > 0) {
         dataVersion = checkFormatAndVersion(readOnlyInput);
      }

      TupleInput readWriteInput = nidData.getMutableTupleInput();

      if (readWriteInput.available() > 0) {
         dataVersion = checkFormatAndVersion(readWriteInput);
      }

      return dataVersion;
   }

   /*
    * (non-Javadoc)
    *
    * @see org.ihtsdo.db.bdb.concept.I_ManageConceptData#getDestRels()
    */
   @Override
   public List<Relationship> getDestRels() throws IOException {

      // Need to make sure there are no pending db writes prior calling this method.
      P.s.waitTillWritesFinished();
      return new ArrayList(P.s.getDestRels(enclosingConcept.getNid()));
   }

   /*
    * (non-Javadoc)
    *
    * @see org.ihtsdo.db.bdb.concept.I_ManageConceptData#getDestRels()
    */
   @Override
   public List<Relationship> getDestRels(NidSetBI allowedTypes) throws IOException {

      // Need to make sure there are no pending db writes prior calling this method.
      P.s.waitTillWritesFinished();

      List<Relationship> destRels = new ArrayList<>();

      for (int originNid : P.s.getDestRelOriginNids(enclosingConcept.getNid(), allowedTypes)) {
         ConceptChronicle c = (ConceptChronicle) P.s.getConceptForNid(originNid);

         if (c != null) {
            for (Relationship r : c.getRelationshipsOutgoing()) {
               if (r != null && r.getDestinationNid() == enclosingConcept.getNid()) {
                   if (allowedTypes.contains(r.getTypeNid())) {
                       destRels.add(r);
                   } else {
                       for (RelationshipVersionBI rv: r.getVersions()) {
                           if (allowedTypes.contains(rv.getTypeNid())) {
                               destRels.add(r);
                               break;
                           }
                       }
                   }
                  
               }
            }
         }
      }

      return destRels;
   }

   public boolean getIsAnnotationStyleIndex() throws IOException {
      AnnotationIndexBinder binder        = AnnotationIndexBinder.getBinder();
      TupleInput            readOnlyInput = nidData.getReadOnlyTupleInput();
      boolean               isIndex       = false;

      if (readOnlyInput.available() > 0) {
         isIndex = binder.entryToObject(readOnlyInput);
      }

      TupleInput readWriteInput = nidData.getMutableTupleInput();

      if (readWriteInput.available() > 0) {
         isIndex = binder.entryToObject(readWriteInput);
      }

      return isIndex;
   }

   public boolean getIsAnnotationStyleRefset() throws IOException {
      AnnotationStyleBinder binder            = AnnotationStyleBinder.getBinder();
      TupleInput            readOnlyInput     = nidData.getReadOnlyTupleInput();
      boolean               isAnnotationStyle = false;

      if (readOnlyInput.available() > 0) {
         isAnnotationStyle = binder.entryToObject(readOnlyInput);
      }

      TupleInput readWriteInput = nidData.getMutableTupleInput();

      if (readWriteInput.available() > 0) {
         isAnnotationStyle = binder.entryToObject(readWriteInput);
      }

      return isAnnotationStyle;
   }

   @Override
   public long getLastChange() {
      return lastChange;
   }

   @Override
   public long getLastWrite() {
      return lastWrite;
   }

   /*
    * (non-Javadoc)
    *
    * @see org.ihtsdo.db.bdb.concept.I_ManageConceptData#getNid()
    */
   @Override
   public int getNid() {
      return enclosingConcept.getNid();
   }

   @Override
   public byte[] getReadOnlyBytes() throws IOException {
      return nidData.getReadOnlyBytes();
   }

   @Override
   public byte[] getReadWriteBytes() throws IOException {
      return nidData.getReadWriteBytes();
   }

   /*
    * (non-Javadoc)
    *
    * @see
    * org.ihtsdo.db.bdb.concept.I_ManageConceptData#getReadWriteDataVersion()
    */
   @Override
   public int getReadWriteDataVersion() throws InterruptedException, ExecutionException, IOException {
      DataVersionBinder binder = DataVersionBinder.getBinder();

      return binder.entryToObject(nidData.getMutableTupleInput());
   }

   @Override
   public TupleInput getReadWriteTupleInput() throws IOException {
      return nidData.getMutableTupleInput();
   }

   public abstract boolean hasComponent(int nid) throws IOException;

   public abstract boolean hasUncommittedComponents();

   @Override
   public boolean isPrimordial() throws IOException {
      return nidData.isPrimordial();
   }

   @Override
   public final boolean isUncommitted() {
      if (lastChange > P.s.getLastCommit()) {
         return hasUncommittedComponents();
      }

      return false;
   }

   @Override
   public final boolean isUnwritten() {
      return lastChange > lastWrite;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setLastWrite(long lastWrite) {
      this.lastWrite = Math.max(this.lastWrite, lastWrite);
   }

   //~--- inner classes -------------------------------------------------------

   public class AddDescriptionSet extends ConcurrentSkipListSet<Description> {
      private static final long serialVersionUID = 1L;

      //~--- constructors -----------------------------------------------------

      public AddDescriptionSet(Collection<? extends Description> c) {
         super(new ComponentComparator());

         for (Description d : c) {
            addDirect(d);
         }
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public boolean add(Description e) {
         try {
            boolean returnValue = super.add(e);

            processNewDesc(e);

            return returnValue;
         } catch (IOException e1) {
            throw new RuntimeException(e1);
         }
      }

      public final boolean addDirect(Description e) {
         return super.add(e);
      }
   }


   public class AddMediaSet extends ConcurrentSkipListSet<Media> {
      private static final long serialVersionUID = 1L;

      //~--- constructors -----------------------------------------------------

      public AddMediaSet(Collection<? extends Media> c) {
         super(new ComponentComparator());

         for (Media i : c) {
            addDirect(i);
         }
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public boolean add(Media e) {
         try {
            boolean returnValue = super.add(e);

            processNewImage(e);

            return returnValue;
         } catch (IOException e1) {
            throw new RuntimeException(e1);
         }
      }

      public final boolean addDirect(Media e) {
         return super.add(e);
      }
   }


   public class AddMemberSet extends ConcurrentSkipListSet<RefexMember<?, ?>> {
      private static final long serialVersionUID = 1L;

      //~--- constructors -----------------------------------------------------

      public AddMemberSet(Collection<? extends RefexMember<?, ?>> c) {
         super(new ComponentComparator());

         for (RefexMember m : c) {
            addDirect(m);
         }
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public boolean add(RefexMember<?, ?> e) {
         try {
            assert e != null : "Trying to add a null refset member to: " + this;

            boolean returnValue = super.add(e);

            processNewRefsetMember(e);

            return returnValue;
         } catch (IOException e1) {
            throw new RuntimeException(e1);
         }
      }

      public final boolean addDirect(RefexMember<?, ?> e) {
         return super.add(e);
      }
   }


   public class AddSrcRelSet extends ConcurrentSkipListSet<Relationship> {
      private static final long serialVersionUID = 1L;

      //~--- constructors -----------------------------------------------------

      public AddSrcRelSet(Collection<? extends Relationship> c) {
         super(new ComponentComparator());

         for (Relationship r : c) {
            add(r);
         }
      }
      public AddSrcRelSet(Collection<? extends Relationship> c, boolean addDirect) {
         super(new ComponentComparator());
         for (Relationship r : c) {
             if (addDirect) {
                 super.add(r);
             } else {
                add(r);
             }
         }
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public final boolean add(Relationship e) {
         try {
            assert e != null : "Relationship is null processing:\n" + this;

            boolean returnValue = super.add(e);

            processNewRel(e);

            return returnValue;
         } catch (IOException e1) {
            throw new RuntimeException(e1);
         }
      }
   }


   public class SetModifiedWhenChangeSet extends ConcurrentSkipListSet<NidPair> {

      /**
       *
       */
      private static final long serialVersionUID = 1L;

      //~--- constructors -----------------------------------------------------

      public SetModifiedWhenChangeSet() {
         super();
      }

      public SetModifiedWhenChangeSet(Collection<NidPair> c) {
         super(c);
      }

      public SetModifiedWhenChangeSet(NidPair[] toCopyIn) {
         super(Arrays.asList(toCopyIn));
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public boolean add(NidPair e) {
         boolean returnValue = super.add(e);

         modified();

         return returnValue;
      }

      @Override
      public boolean addAll(Collection<? extends NidPair> c) {
         boolean returnValue = super.addAll(c);

         modified();

         return returnValue;
      }

      @Override
      public void clear() {
         throw new UnsupportedOperationException();
      }

      public synchronized boolean forget(NidPair pair) {
         boolean removed = super.remove(pair);

         if (removed) {
            modified();
         }

         return removed;
      }

      @Override
      public boolean remove(Object o) {
         return forget((NidPair) o);
      }

      @Override
      public boolean removeAll(Collection<?> c) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean retainAll(Collection<?> c) {
         throw new UnsupportedOperationException();
      }
   }
}
