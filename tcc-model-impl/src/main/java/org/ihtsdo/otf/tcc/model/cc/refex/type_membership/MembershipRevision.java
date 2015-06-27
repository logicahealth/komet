package org.ihtsdo.otf.tcc.model.cc.refex.type_membership;

//~--- non-JDK imports --------------------------------------------------------

import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_member.RefexMemberAnalogBI;
import org.ihtsdo.otf.tcc.dto.component.refex.type_member.TtkRefexRevision;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMemberVersion;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexRevision;

public class MembershipRevision extends RefexRevision<MembershipRevision, MembershipMember> 
    implements RefexMemberAnalogBI<MembershipRevision> {
   public MembershipRevision() {
      super();
   }

   public MembershipRevision(int statusAtPositionNid, MembershipMember primoridalMember) {
      super(statusAtPositionNid, primoridalMember);
   }

   public MembershipRevision(TtkRefexRevision eVersion, MembershipMember member) throws IOException {
      super(eVersion, member);
   }


   public MembershipRevision(Status status, long time, int authorNid, int moduleNid, int pathNid,
                             MembershipMember primoridalMember) {
      super(status, time, authorNid, moduleNid, pathNid, primoridalMember);
   }

   protected MembershipRevision(Status status, long time, int authorNid, int moduleNid, int pathNid,
                                MembershipRevision another) {
      super(status, time, authorNid, moduleNid, pathNid, another.primordialComponent);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {

      //
   }

    @Override
   protected void addSpecProperties(RefexCAB rcs) {

      // no fields to add...
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (MembershipRevision.class.isAssignableFrom(obj.getClass())) {
         return super.equals(obj);
      }

      return false;
   }

   @Override
   public MembershipRevision makeAnalog() {
      return new MembershipRevision(getStatus(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(),  this);
   }

   @Override
   public MembershipRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatus(status);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

         return this;
      }

      MembershipRevision newR = new MembershipRevision(status, time, authorNid, moduleNid, pathNid, this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public boolean readyToWriteRefsetRevision() {
      return true;
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuilder buf = new StringBuilder();

      buf.append(this.getClass().getSimpleName()).append(":{");
      buf.append(super.toString());

      return buf.toString();
   }


   //~--- get methods ---------------------------------------------------------

    @Override
   protected RefexType getTkRefsetType() {
      return RefexType.MEMBER;
   }

   @Override
   public Optional<MembershipMemberVersion> getVersion(ViewCoordinate c) throws ContradictionException {
      Optional<RefexMemberVersion<MembershipRevision, MembershipMember>> temp =  ((MembershipMember) primordialComponent).getVersion(c);
      return Optional.ofNullable(temp.isPresent() ? (MembershipMemberVersion)temp.get() : null);
   }

   @Override
   public List<MembershipMemberVersion> getVersions() {
      return ((MembershipMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<MembershipRevision>> getVersions(ViewCoordinate c) {
      return ((MembershipMember) primordialComponent).getVersions(c);
   }

}
