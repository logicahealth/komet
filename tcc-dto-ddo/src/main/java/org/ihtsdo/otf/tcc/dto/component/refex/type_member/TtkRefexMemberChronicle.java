package org.ihtsdo.otf.tcc.dto.component.refex.type_member;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.dto.component.refex.TtkRefexAbstractMemberChronicle;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;

public class TtkRefexMemberChronicle extends TtkRefexAbstractMemberChronicle<TtkRefexRevision> {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public TtkRefexMemberChronicle() {
      super();
   }

   public TtkRefexMemberChronicle(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }
   public TtkRefexMemberChronicle(RefexChronicleBI another) throws IOException {
      super((RefexVersionBI) another.getPrimordialVersion());

      Collection<? extends RefexVersionBI> refexes   = another.getVersions();
      int                                              partCount = refexes.size();
      Iterator<? extends RefexVersionBI>   itr       = refexes.iterator();
      RefexVersionBI                       rv        = itr.next();

      if (partCount > 1) {
         revisions = new ArrayList<>(partCount - 1);

         while (itr.hasNext()) {
            rv = itr.next();
            revisions.add(new TtkRefexRevision(rv));
         }
      }
   }

   //~--- methods -------------------------------------------------------------

    @Override
    protected void addUuidReferencesForRefexRevision(Collection<UUID> references) {
        // nothing to do...
    }
   /**
    * Compares this object to the specified object. The result is {@code true}
    * if and only if the argument is not {@code null}, is a
    * {@code ERefsetMember} object, and contains the same values, field by field,
    * as this {@code ERefsetMember}.
    *
    * @param obj the object to compare with.
    * @return {@code true} if the objects are the same;
    *         {@code false} otherwise.
    */
   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (TtkRefexMemberChronicle.class.isAssignableFrom(obj.getClass())) {

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   /**
    * Returns a hash code for this {@code ERefsetMember}.
    *
    * @return a hash code value for this {@code ERefsetMember}.
    */
   @Override
   public int hashCode() {
      return this.primordialUuid.hashCode();
   }


   @Override
   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);

      int versionSize = in.readInt();

      if (versionSize > 0) {
         revisions = new ArrayList<>(versionSize);

         for (int i = 0; i < versionSize; i++) {
            revisions.add(new TtkRefexRevision(in, dataVersion));
         }
      }
   }

   /**
    * Returns a string representation of the object.
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(this.getClass().getSimpleName()).append(": ");
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   @Override
   public void writeExternal(DataOutput out) throws IOException {
      super.writeExternal(out);

      if (revisions == null) {
         out.writeInt(0);
      } else {
         out.writeInt(revisions.size());

         for (TtkRefexRevision rmv : revisions) {
            rmv.writeExternal(out);
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   public List<TtkRefexRevision> getRevisionList() {
      return revisions;
   }

   @Override
   public RefexType getType() {
      return RefexType.MEMBER;
   }
}
