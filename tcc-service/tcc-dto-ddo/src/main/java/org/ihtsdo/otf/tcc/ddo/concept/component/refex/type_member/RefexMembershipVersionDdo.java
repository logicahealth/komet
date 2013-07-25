package org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_member;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexVersionDdo;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;


public class RefexMembershipVersionDdo
        extends RefexVersionDdo<RefexMembershipChronicleDdo, RefexMembershipVersionDdo> {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public RefexMembershipVersionDdo() {
      super();
   }

   public RefexMembershipVersionDdo(RefexMembershipChronicleDdo chronicle, TerminologySnapshotDI ss,
                                   RefexVersionBI another)
           throws IOException, ContradictionException {
      super(chronicle, ss, another);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>ERefsetVersion</tt> object, and contains the same values, field by field,
    * as this <tt>ERefsetVersion</tt>.
    *
    * @param obj the object to compare with.
    * @return <code>true</code> if the objects are the same;
    *         <code>false</code> otherwise.
    */
   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (RefexMembershipVersionDdo.class.isAssignableFrom(obj.getClass())) {
         return super.equals(obj);
      }

      return false;
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
}
