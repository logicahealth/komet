package org.ihtsdo.otf.tcc.dto.component.refex.type_int;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_int.RefexIntVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.dto.component.refex.TtkRefexAbstractMemberChronicle;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import javax.xml.bind.annotation.XmlAttribute;
import org.ihtsdo.otf.tcc.dto.component.transformer.ComponentFields;
import org.ihtsdo.otf.tcc.dto.component.transformer.ComponentTransformerBI;

public class TtkRefexIntMemberChronicle extends TtkRefexAbstractMemberChronicle<TtkRefexIntRevision> {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   @XmlAttribute
   public int int1;

   //~--- constructors --------------------------------------------------------

   public TtkRefexIntMemberChronicle() {
      super();
   }

   public TtkRefexIntMemberChronicle(RefexChronicleBI another) throws IOException {
      super((RefexVersionBI) another.getPrimordialVersion());

      TerminologyStoreDI                      ts        = Ts.get();
      Collection<? extends RefexIntVersionBI> refexes   = another.getVersions();
      int                                     partCount = refexes.size();
      Iterator<? extends RefexIntVersionBI>   itr       = refexes.iterator();
      RefexIntVersionBI                       rv        = itr.next();

      this.int1 = rv.getInt1();

      if (partCount > 1) {
         revisions = new ArrayList<>(partCount - 1);

         while (itr.hasNext()) {
            rv = itr.next();
            revisions.add(new TtkRefexIntRevision(rv));
         }
      }
   }

   public TtkRefexIntMemberChronicle(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   public TtkRefexIntMemberChronicle(TtkRefexIntMemberChronicle another, ComponentTransformerBI transformer) {
      super(another, transformer);
      this.int1 = transformer.transform(another.int1, another, ComponentFields.REFEX_INTEGER1);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>ERefsetIntMember</tt> object, and contains the same values, field by field,
    * as this <tt>ERefsetIntMember</tt>.
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

      if (TtkRefexIntMemberChronicle.class.isAssignableFrom(obj.getClass())) {
         TtkRefexIntMemberChronicle another = (TtkRefexIntMemberChronicle) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare intValue
         if (this.int1 != another.int1) {
            return false;
         }

         // Compare extraVersions
         if (this.revisions == null) {
            if (another.revisions == null) {             // Equal!
            } else if (another.revisions.isEmpty()) {    // Equal!
            } else {
               return false;
            }
         } else if (!this.revisions.equals(another.revisions)) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   /**
    * Returns a hash code for this <code>ERefsetIntMember</code>.
    *
    * @return a hash code value for this <tt>ERefsetIntMember</tt>.
    */
   @Override
   public int hashCode() {
      return this.primordialUuid.hashCode();
   }

   @Override
   public TtkRefexIntMemberChronicle makeTransform(ComponentTransformerBI transformer) {
      return new TtkRefexIntMemberChronicle(this, transformer);
   }

   @Override
   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      int1 = in.readInt();

      int versionSize = in.readInt();

      if (versionSize > 0) {
         revisions = new ArrayList<>(versionSize);

         for (int i = 0; i < versionSize; i++) {
            revisions.add(new TtkRefexIntRevision(in, dataVersion));
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
      buff.append(" int: ");
      buff.append(this.int1);
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   @Override
   public void writeExternal(DataOutput out) throws IOException {
      super.writeExternal(out);
      out.writeInt(int1);

      if (revisions == null) {
         out.writeInt(0);
      } else {
         out.writeInt(revisions.size());

         for (TtkRefexIntRevision rmv : revisions) {
            rmv.writeExternal(out);
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   public int getIntValue() {
      return int1;
   }

   @Override
   public List<TtkRefexIntRevision> getRevisionList() {
      return revisions;
   }

   @Override
   public RefexType getType() {
      return RefexType.INT;
   }

   //~--- set methods ---------------------------------------------------------

   public void setIntValue(int intValue) {
      this.int1 = intValue;
   }
}
