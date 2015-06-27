package org.ihtsdo.otf.tcc.dto.component.refex.type_member;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.dto.component.TtkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;

public class TtkRefexRevision extends TtkRevision {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public TtkRefexRevision() {
      super();
   }
   public TtkRefexRevision(RefexVersionBI another) throws IOException {
      super(another);
   }


   public TtkRefexRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   //~--- methods -------------------------------------------------------------
   @Override
   protected final void addUuidReferencesForRevisionComponent(Collection<UUID> references) {
       // nothing to add
   }
   /**
    * Compares this object to the specified object. The result is {@code true}
    * if and only if the argument is not {@code null}, is a
    * {@code ERefsetVersion} object, and contains the same values, field by field,
    * as this {@code ERefsetVersion}.
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

      if (TtkRefexRevision.class.isAssignableFrom(obj.getClass())) {
         return super.equals(obj);
      }

      return false;
   }

   @Override
   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
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
   }
}
