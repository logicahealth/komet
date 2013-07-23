package org.ihtsdo.otf.tcc.chronicle.cc.component;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.chronicle.cc.Position;
import org.ihtsdo.otf.tcc.api.AnalogBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.AnalogGeneratorBI;
import org.ihtsdo.otf.tcc.api.coordinate.PositionBI;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.ihtsdo.otf.tcc.chronicle.cc.P;
import org.ihtsdo.otf.tcc.chronicle.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.api.time.TimeHelper;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.id.IdBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.text.SimpleDateFormat;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.api.coordinate.Status;

public abstract class Revision<V extends Revision<V, C>, C extends ConceptComponent<V, C>>
        implements ComponentVersionBI, AnalogBI, AnalogGeneratorBI<V> {
   protected static final Logger  logger         = Logger.getLogger(ConceptComponent.class.getName());
   public static SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");

   //~--- fields --------------------------------------------------------------

   public C   primordialComponent;
   public int stamp;

   //~--- constructors --------------------------------------------------------

   public Revision() {
      super();
   }

   public Revision(int stamp, C primordialComponent) {
      super();
      assert primordialComponent != null;
      assert stamp != 0;
      this.stamp            = stamp;
      this.primordialComponent = primordialComponent;
      primordialComponent.clearVersions();
      assert stamp != Integer.MAX_VALUE;
      this.primordialComponent.getEnclosingConcept().modified();
   }

   public Revision(TupleInput input, C conceptComponent) {
      this(input.readInt(), conceptComponent);
      conceptComponent.clearVersions();
      assert stamp != 0;
   }

   public Revision(Status status, long time, int authorNid, int moduleNid, int pathNid,
                   C primordialComponent) {
      this.stamp = P.s.getStamp(status, time, authorNid, moduleNid, pathNid);
      assert stamp != 0;
      this.primordialComponent = primordialComponent;
      primordialComponent.clearVersions();
      assert primordialComponent != null;
      assert stamp != Integer.MAX_VALUE;
      this.primordialComponent.getEnclosingConcept().modified();
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean addAnnotation(@SuppressWarnings("rawtypes") RefexChronicleBI annotation)
           throws IOException {
      return primordialComponent.addAnnotation(annotation);
   }

   abstract protected void addComponentNids(Set<Integer> allNids);

   @Override
   public boolean addLongId(Long longId, int authorityNid, org.ihtsdo.otf.tcc.api.coordinate.Status status, EditCoordinate ec, long time) {
      return primordialComponent.addLongId(longId, authorityNid, status, ec, time);
   }

   protected String assertionString() {
      try {
         return P.s.getConcept(primordialComponent.enclosingConceptNid).toLongString();
      } catch (IOException ex) {
         Logger.getLogger(ConceptComponent.class.getName()).log(Level.SEVERE, null, ex);
      }

      return toString();
   }

   @SuppressWarnings("unchecked")
   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (Revision.class.isAssignableFrom(obj.getClass())) {
         Revision<V, C> another = (Revision<V, C>) obj;

         if (this.stamp == another.stamp) {
            return true;
         }
      }

      return false;
   }

   @Override
   public final int hashCode() {
      return Hashcode.compute(primordialComponent.nid);
   }

   public boolean makeAdjudicationAnalogs(EditCoordinate ec, ViewCoordinate vc) throws IOException {
      return primordialComponent.makeAdjudicationAnalogs(ec, vc);
   }

   /**
    * 1. Analog, an object, concept or situation which in some way
    *    resembles a different situation
    * 2. Analogy, in language, a comparison between concepts
    * @param statusNid
    * @param pathNid
    * @param time
    * @return
    */
   @Override
   public abstract V makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid);

   protected void modified() {
      if (primordialComponent != null) {
         primordialComponent.modified();
      }
   }

   public final boolean readyToWrite() {
      assert primordialComponent != null : assertionString();
      assert stamp != Integer.MAX_VALUE : assertionString();
      assert(stamp > 0) || (stamp == -1);

      return true;
   }

   public abstract boolean readyToWriteRevision();

   @Override
   public boolean stampIsInRange(int min, int max) {
      return (stamp >= min) && (stamp <= max);
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuffer buf = new StringBuffer();

      buf.append(" stamp:");
      buf.append(stamp);

      try {
         buf.append(" s:").append(getStatus());
         buf.append(" t: ");
         buf.append(TimeHelper.formatDate(getTime()));
         buf.append(" a:");
         ConceptComponent.addNidToBuffer(buf, getAuthorNid());
         buf.append(" m:");
         ConceptComponent.addNidToBuffer(buf, getModuleNid());
         buf.append(" p:");
         ConceptComponent.addNidToBuffer(buf, getPathNid());
         buf.append(" ");
         buf.append(getTime());
      } catch (Throwable e) {
         buf.append(" !!! Invalid sapNid. Cannot compute path, time, status. !!! ");
         buf.append(e.getLocalizedMessage());
      }

      buf.append(" };");

      return buf.toString();
   }

   @Override
   public abstract String toUserString();

   @Override
   public String toUserString(TerminologySnapshotDI snapshot) throws IOException, ContradictionException {
      return toUserString();
   }

   /**
    * Test method to check to see if two objects are equal in all respects.
    * @param another
    * @return either a zero length String, or a String containing a
    * description of the validation failures.
    * @throws IOException
    */
   public String validate(Revision<?, ?> another) throws IOException {
      assert another != null;

      StringBuilder buf = new StringBuilder();

      if (this.stamp != another.stamp) {
         buf.append("\t\tRevision.sapNid not equal: \n\t\t\tthis.sapNid = ").append(this.stamp).append(
             "\n\t\t\tanother.sapNid = ").append(another.stamp).append("\n");
      }

      if (!this.primordialComponent.equals(another.primordialComponent)) {
         buf.append(
             "\t\tRevision.primordialComponent not equal: " + "\n\t\t\tthis.primordialComponent = ").append(
             this.primordialComponent).append("\n\t\t\tanother.primordialComponent = ").append(
             another.primordialComponent).append("\n");
      }

      return buf.toString();
   }

   @Override
   public boolean versionsEqual(ViewCoordinate vc1, ViewCoordinate vc2, Boolean compareAuthoring) {
      return primordialComponent.versionsEqual(vc1, vc2, compareAuthoring);
   }

   protected abstract void writeFieldsToBdb(TupleOutput output);

   public final void writeRevisionBdb(TupleOutput output) {
      output.writeInt(stamp);
      writeFieldsToBdb(output);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public Collection<? extends IdBI> getAdditionalIds() {
      return primordialComponent.getAdditionalIds();
   }

   @Override
   public Collection<? extends IdBI> getAllIds() {
      return primordialComponent.getAllIds();
   }

   @Override
   public Set<Integer> getAllNidsForVersion() throws IOException {
      HashSet<Integer> allNids = new HashSet<>();

      allNids.add(primordialComponent.nid);
      allNids.add(getAuthorNid());
      allNids.add(getPathNid());
      addComponentNids(allNids);

      return allNids;
   }

   public Set<Integer> getAllStamps() throws IOException {
      return primordialComponent.getAllStamps();
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getAnnotations() {
      return primordialComponent.getAnnotations();
   }

   @Override
   public int getAuthorNid() {
      return P.s.getAuthorNidForStamp(stamp);
   }

   @Override
   public ComponentChronicleBI getChronicle() {
      return (ComponentChronicleBI) primordialComponent;
   }

   @Override
   public int getConceptNid() {
      return primordialComponent.enclosingConceptNid;
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getAnnotationsActive(ViewCoordinate xyz)
           throws IOException {
      return primordialComponent.getAnnotationsActive(xyz);
   }

   @Override
   public <T extends RefexVersionBI<?>> Collection<T> getAnnotationsActive(ViewCoordinate xyz,
           Class<T> cls)
           throws IOException {
      return primordialComponent.getAnnotationsActive(xyz, cls);
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getAnnotationsActive(ViewCoordinate xyz,
           int refexNid)
           throws IOException {
      return primordialComponent.getAnnotationsActive(xyz, refexNid);
   }

   @Override
   public <T extends RefexVersionBI<?>> Collection<T> getAnnotationsActive(ViewCoordinate xyz,
           int refexNid, Class<T> cls)
           throws IOException {
      return primordialComponent.getAnnotationsActive(xyz, refexNid, cls);
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getRefexMembersActive(ViewCoordinate xyz, int refsetNid)
           throws IOException {
      return primordialComponent.getRefexMembersActive(xyz, refsetNid);
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getRefexMembersActive(ViewCoordinate xyz) throws IOException {
      return primordialComponent.getRefexMembersActive(xyz);
   }

   public ConceptChronicle getEnclosingConcept() {
      return primordialComponent.getEnclosingConcept();
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getRefexMembersInactive(ViewCoordinate xyz) throws IOException {
      return getChronicle().getRefexMembersInactive(xyz);
   }

   @Override
   public int getModuleNid() {
      return P.s.getModuleNidForStamp(stamp);
   }

   @Override
   public final int getNid() {
      return primordialComponent.getNid();
   }

   @Override
   public int getPathNid() {
      return P.s.getPathNidForStamp(stamp);
   }

   @Override
   public PositionBI getPosition() throws IOException {
      return new Position(getTime(), P.s.getPath(getPathNid()));
   }

   public Set<PositionBI> getPositions() throws IOException {
      return primordialComponent.getPositions();
   }

   @Override
   public UUID getPrimordialUuid() {
      return primordialComponent.getPrimordialUuid();
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getRefexMembers(int refsetNid) throws IOException {
      return primordialComponent.getRefexMembers(refsetNid);
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getRefexes() throws IOException {
      return primordialComponent.getRefexes();
   }

   @Override
   public int getStamp() {
      return stamp;
   }

   public final int getStatusAtPositionNid() {
      return stamp;
   }

   @Override
   public Status getStatus() {
      return P.s.getStatusForStamp(stamp);
   }

   @Override
   public long getTime() {
      return P.s.getTimeForStamp(stamp);
   }

   @Override
   public final List<UUID> getUUIDs() {
      return primordialComponent.getUUIDs();
   }

   public abstract IntArrayList getVariableVersionNids();

   public final C getVersioned() {
      return primordialComponent;
   }

   @Override
   public boolean hasCurrentAnnotationMember(ViewCoordinate xyz, int refsetNid) throws IOException {
      return primordialComponent.hasCurrentAnnotationMember(xyz, refsetNid);
   }

   @Override
   public boolean hasCurrentRefexMember(ViewCoordinate xyz, int refsetNid) throws IOException {
      return primordialComponent.hasCurrentRefexMember(xyz, refsetNid);
   }

   @Override
   public boolean isBaselineGeneration() {
      return stamp <= P.s.getMaxReadOnlyStamp();
   }

   @Override
   public boolean isUncommitted() {
      return getTime() == Long.MAX_VALUE;
   }

    @Override
    public boolean isActive() throws IOException {
        return getStatus() == Status.ACTIVE;
    }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setAuthorNid(int authorNid) {
      if (getTime() != Long.MAX_VALUE) {
         throw new UnsupportedOperationException("Cannot change status if time != Long.MAX_VALUE; "
                 + "Use makeAnalog instead.");
      }

      if (authorNid != getPathNid()) {
         this.stamp = P.s.getStamp(getStatus(), Long.MAX_VALUE, authorNid, getModuleNid(),
                                         getPathNid());
         modified();
      }
   }

   @Override
   public final void setModuleNid(int moduleNid) {
      if (getTime() != Long.MAX_VALUE) {
         throw new UnsupportedOperationException("Cannot change status if time != Long.MAX_VALUE; "
                 + "Use makeAnalog instead.");
      }

      try {
         this.stamp = P.s.getStamp(getStatus(), Long.MAX_VALUE, getAuthorNid(), moduleNid,
                                         getPathNid());
      } catch (Exception e) {
         throw new RuntimeException();
      }

      modified();
   }

   @Override
   public final void setNid(int nid) throws PropertyVetoException {
      throw new PropertyVetoException("nid", null);
   }

   @Override
   public final void setPathNid(int pathId) {
      if (getTime() != Long.MAX_VALUE) {
         throw new UnsupportedOperationException("Cannot change status if time != Long.MAX_VALUE; "
                 + "Use makeAnalog instead.");
      }

      this.stamp = P.s.getStamp(getStatus(), Long.MAX_VALUE, getAuthorNid(), getModuleNid(), pathId);
   }

   public void setStatusAtPosition(Status status, long time, int authorNid, int moduleNid, int pathNid) {
      this.stamp = P.s.getStamp(status, time, authorNid, moduleNid, pathNid);
      modified();
   }

   @Override
   public final void setStatus(org.ihtsdo.otf.tcc.api.coordinate.Status nid) {
      if (getTime() != Long.MAX_VALUE) {
         throw new UnsupportedOperationException("Cannot change status if time != Long.MAX_VALUE; "
                 + "Use makeAnalog instead.");
      }

      try {
         this.stamp = P.s.getStamp(nid, Long.MAX_VALUE, getAuthorNid(), getModuleNid(),
                                         getPathNid());
      } catch (Exception e) {
         throw new RuntimeException();
      }

      modified();
   }

   @Override
   public final void setTime(long time) {
      if (getTime() != Long.MAX_VALUE) {
         throw new UnsupportedOperationException("Cannot change status if time != Long.MAX_VALUE; "
                 + "Use makeAnalog instead.");
      }

      if (time != getTime()) {
         try {
            this.stamp = P.s.getStamp(getStatus(), time, getAuthorNid(), getModuleNid(),
                                            getPathNid());
         } catch (Exception e) {
            throw new RuntimeException();
         }

         modified();
      }
   }
}
