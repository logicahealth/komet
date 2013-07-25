package org.ihtsdo.otf.tcc.chronicle.cc.relationship.group;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.chronicle.cc.Position;
import org.ihtsdo.otf.tcc.chronicle.cc.P;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.api.coordinate.PositionBI;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.blueprint.CreateOrAmendBlueprint;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.id.IdBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.group.RelGroupChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.group.RelGroupVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;

public class RelGroupVersion implements RelGroupVersionBI {
   private long                time = Long.MIN_VALUE;
   private int                 authorNid;
   private ViewCoordinate      coordinate;
   private int                 moduleNid;
   private int                 pathNid;
   private RelGroupChronicleBI rg;
   private Status                 status;

   //~--- constructors --------------------------------------------------------

   public RelGroupVersion(RelGroupChronicleBI rg, ViewCoordinate coordinate) {
      assert rg != null;
      assert coordinate != null;
      this.rg         = rg;
      this.coordinate = new ViewCoordinate(UUID.randomUUID(), "RelGroupVersion temp", coordinate);
      setupLatest();
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean addAnnotation(RefexChronicleBI<?> annotation) throws IOException {
      return rg.addAnnotation(annotation);
   }

   @Override
   public boolean makeAdjudicationAnalogs(EditCoordinate ec, ViewCoordinate vc) throws Exception {
      return rg.makeAdjudicationAnalogs(ec, vc);
   }

   @Override
   public CreateOrAmendBlueprint makeBlueprint(ViewCoordinate vc, 
            IdDirective idDirective, RefexDirective refexDirective) throws IOException, ContradictionException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   private void setupLatest() {
      time = Long.MIN_VALUE;

      for (RelationshipVersionBI relV : getAllCurrentRelVersions()) {
         if (relV.getTime() > time) {
            time      = relV.getTime();
            authorNid = relV.getAuthorNid();
            pathNid   = relV.getPathNid();
            status = relV.getStatus();
            moduleNid = relV.getModuleNid();
         }
      }

      if (time == Long.MIN_VALUE) {
         for (RelationshipChronicleBI rel : getRels()) {
            for (RelationshipVersionBI relV : rel.getVersions(coordinate)) {
               if (relV.getTime() > time) {
                  time      = relV.getTime();
                  authorNid = relV.getAuthorNid();
                  pathNid   = relV.getPathNid();
                  status = relV.getStatus();
                  moduleNid = relV.getModuleNid();
               }
            }
         }
      }
   }

   @Override
   public boolean stampIsInRange(int min, int max) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public String toUserString() {
      StringBuilder buff = new StringBuilder();

      buff.append("Group: ");

      for (RelationshipChronicleBI rel : getRels()) {
         buff.append(rel.toUserString());
         buff.append("; ");
      }

      return buff.toString();
   }

   @Override
   public String toUserString(TerminologySnapshotDI snapshot) throws IOException, ContradictionException {
      return toUserString();
   }

   @Override
   public boolean versionsEqual(ViewCoordinate vc1, ViewCoordinate vc2, Boolean compareAuthoring) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public Collection<? extends IdBI> getAdditionalIds() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends RelationshipVersionBI> getAllCurrentRelVersions() {
      ArrayList<RelationshipVersionBI> results = new ArrayList<>();

      for (RelationshipChronicleBI relc : rg.getRels()) {
         if (coordinate != null) {
            for (RelationshipVersionBI rv : relc.getVersions(coordinate)) {
               if ((rv.getGroup() == rg.getRelGroup())
                       && coordinate.getAllowedStatus().contains(rv.getStatus())) {
                  results.add(rv);
               }
            }
         } else {
            for (RelationshipVersionBI rv : relc.getVersions()) {
               if (rv.getGroup() == rg.getRelGroup()) {
                  results.add(rv);
               }
            }
         }
      }

      return results;
   }

   @Override
   public Collection<? extends IdBI> getAllIds() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Set<Integer> getAllNidsForVersion() throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends RelationshipVersionBI> getAllRels() throws ContradictionException {
      ArrayList<RelationshipVersionBI> results = new ArrayList<>();

      for (RelationshipChronicleBI relc : rg.getRels()) {
         if (coordinate != null) {
            try {
               RelationshipVersionBI rv = relc.getVersion(coordinate.getVcWithAllStatusValues());

               if (rv != null) {
                  if (rv.getGroup() == rg.getRelGroup()) {
                     results.add(rv);
                  }
               }
            } catch (ContradictionException ex) {
               for (RelationshipVersionBI rv : relc.getVersions(coordinate.getVcWithAllStatusValues())) {
                  if (rv.getGroup() == rg.getRelGroup()) {
                     results.add(rv);
                  }
               }
            }
         } else {
            for (RelationshipVersionBI rv : relc.getVersions()) {
               if (rv.getGroup() == rg.getRelGroup()) {
                  results.add(rv);
               }
            }
         }
      }

      return results;
   }

   @Override
   public Set<Integer> getAllStamps() throws IOException {
      return rg.getAllStamps();
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getAnnotations() throws IOException {
      return rg.getAnnotations();
   }

   @Override
   public int getAuthorNid() {
      return authorNid;
   }

   @Override
   public ComponentChronicleBI getChronicle() {
      return rg;
   }

   @Override
   public int getConceptNid() {
      return rg.getConceptNid();
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getAnnotationsActive(ViewCoordinate xyz)
           throws IOException {
      return rg.getAnnotationsActive(xyz);
   }

   @Override
   public <T extends RefexVersionBI<?>> Collection<T> getAnnotationsActive(ViewCoordinate xyz,
           Class<T> cls)
           throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getAnnotationsActive(ViewCoordinate xyz,
           int refexNid)
           throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public <T extends RefexVersionBI<?>> Collection<T> getAnnotationsActive(ViewCoordinate xyz,
           int refexNid, Class<T> cls)
           throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getRefexMembersActive(ViewCoordinate xyz, int refsetNid)
           throws IOException {
      return rg.getRefexMembersActive(xyz, refsetNid);
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getRefexMembersActive(ViewCoordinate xyz) throws IOException {
      return rg.getRefexMembersActive(xyz);
   }

   @Override
   public Collection<? extends RelationshipVersionBI> getCurrentRels() throws ContradictionException {
      ArrayList<RelationshipVersionBI> results = new ArrayList<>();

      for (RelationshipChronicleBI relc : rg.getRels()) {
         if (coordinate != null) {
            RelationshipVersionBI rv = relc.getVersion(coordinate);

            if (rv != null) {
               if ((rv.getGroup() == rg.getRelGroup())
                       && coordinate.getAllowedStatus().contains(rv.getStatus())) {
                  results.add(rv);
               }
            }
         } else {
            for (RelationshipVersionBI rv : relc.getVersions()) {
               if (rv.getGroup() == rg.getRelGroup()) {
                  results.add(rv);
               }
            }
         }
      }

      return results;
   }

   @Override
   public ConceptChronicleBI getEnclosingConcept() {
      return rg.getEnclosingConcept();
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getRefexMembersInactive(ViewCoordinate xyz) throws IOException {
      return rg.getRefexMembersInactive(xyz);
   }

   @Override
   public int getModuleNid() {
      return moduleNid;
   }

   @Override
   public int getNid() {
      return rg.getNid();
   }

   @Override
   public int getPathNid() {
      return pathNid;
   }

   @Override
   public PositionBI getPosition() throws IOException {
      return new Position(getTime(), P.s.getPath(getPathNid()));
   }

   @Override
   public Set<PositionBI> getPositions() throws IOException {
      return rg.getPositions();
   }

   @Override
   public UUID getPrimordialUuid() {
      return rg.getPrimordialUuid();
   }

   @Override
   public RelGroupVersionBI getPrimordialVersion() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getRefexMembers(int refsetNid) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getRefexes() throws IOException {
      return rg.getRefexes();
   }

   @Override
   public int getRelGroup() {
      return rg.getRelGroup();
   }

   @Override
   public Collection<? extends RelationshipChronicleBI> getRels() {
      return rg.getRels();
   }

   @Override
   public int getStamp() {
      throw new UnsupportedOperationException();
   }

   @Override
   public Status getStatus() {
      return status;
   }

   @Override
   public long getTime() {
      return time;
   }

   @Override
   public List<UUID> getUUIDs() {
      return rg.getUUIDs();
   }

   @Override
   public RelGroupVersionBI getVersion(ViewCoordinate c) throws ContradictionException {
      return rg.getVersion(c);
   }

   @Override
   public List<RelGroupVersion> getVersions() {
      return Arrays.asList(new RelGroupVersion[] { new RelGroupVersion(this, null) });
   }

   @Override
   public Collection<? extends RelGroupVersionBI> getVersions(ViewCoordinate c) {
      return Arrays.asList(new RelGroupVersionBI[] { new RelGroupVersion(this, c) });
   }

   @Override
   public boolean hasCurrentAnnotationMember(ViewCoordinate xyz, int refsetNid) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public boolean hasCurrentRefexMember(ViewCoordinate xyz, int refsetNid) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public boolean isActive() throws IOException {
      return coordinate.getAllowedStatus().contains(status);
   }

   @Override
   public boolean isBaselineGeneration() {
      for (RelationshipChronicleBI rc : getRels()) {
         for (RelationshipVersionBI rv : rc.getVersions()) {
            if (!rv.isBaselineGeneration()) {
               return false;
            }
         }
      }

      return true;
   }

   @Override
   public boolean isUncommitted() {
      return false;
   }
}
