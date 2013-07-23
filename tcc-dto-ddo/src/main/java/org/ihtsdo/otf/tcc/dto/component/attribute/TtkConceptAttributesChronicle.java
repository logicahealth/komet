package org.ihtsdo.otf.tcc.dto.component.attribute;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeChronicleBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.dto.component.TtkComponentChronicle;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import org.ihtsdo.otf.tcc.dto.component.transformer.ComponentFields;
import org.ihtsdo.otf.tcc.dto.component.transformer.ComponentTransformerBI;

@XmlRootElement(name="attributes")
public class TtkConceptAttributesChronicle extends TtkComponentChronicle<TtkConceptAttributesRevision> {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   @XmlAttribute
   public boolean defined;

   //~--- constructors --------------------------------------------------------

   public TtkConceptAttributesChronicle() {
      super();
   }

   public TtkConceptAttributesChronicle(ConceptAttributeChronicleBI another) throws IOException {
      super(another.getPrimordialVersion());

      Collection<? extends ConceptAttributeVersionBI> versions = another.getVersions();
      Iterator<? extends ConceptAttributeVersionBI>   itr      = versions.iterator();
      ConceptAttributeVersionBI                       vers     = itr.next();

      this.defined = vers.isDefined();

      if (versions.size() > 1) {
         revisions = new ArrayList<>(versions.size() - 1);

         while (itr.hasNext()) {
            vers = itr.next();
            revisions.add(new TtkConceptAttributesRevision(vers));
         }
      }
      ViewCoordinate vc = null;
      int refexNid = Integer.MAX_VALUE;
      
      another.getAnnotationsActive(vc, refexNid, null);
   }

   public TtkConceptAttributesChronicle(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   public TtkConceptAttributesChronicle(TtkConceptAttributesChronicle another, ComponentTransformerBI transformer) {
      super(another, transformer);
      this.defined = transformer.transform(another.defined, another, ComponentFields.ATTRIBUTE_DEFINED);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>EConceptAttributes</tt> object, and contains the same values, field by field,
    * as this <tt>EConceptAttributes</tt>.
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

      if (TtkConceptAttributesChronicle.class.isAssignableFrom(obj.getClass())) {
         TtkConceptAttributesChronicle another = (TtkConceptAttributesChronicle) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare defined
         if (this.defined != another.defined) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   /**
    * Returns a hash code for this <code>EConceptAttributes</code>.
    *
    * @return a hash code value for this <tt>EConceptAttributes</tt>.
    */
   @Override
   public int hashCode() {
      return this.primordialUuid.hashCode();
   }

   @Override
   public TtkConceptAttributesChronicle makeTransform(ComponentTransformerBI transformer) {
      return new TtkConceptAttributesChronicle(this, transformer);
   }

   @Override
   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      defined = in.readBoolean();

      int versionCount = in.readInt();

      assert versionCount < 1024 : "Version count is: " + versionCount;

      if (versionCount > 0) {
         revisions = new ArrayList<>(versionCount);

         for (int i = 0; i < versionCount; i++) {
            revisions.add(new TtkConceptAttributesRevision(in, dataVersion));
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
      buff.append(" defined: ");
      buff.append(this.defined);
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   @Override
   public void writeExternal(DataOutput out) throws IOException {
      super.writeExternal(out);
      out.writeBoolean(defined);

      if (revisions == null) {
         out.writeInt(0);
      } else {
         assert revisions.size() < 1024 : "Version count is: " + revisions.size() + "\n\n" + this.toString();
         out.writeInt(revisions.size());

         for (TtkConceptAttributesRevision cav : revisions) {
            cav.writeExternal(out);
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public List<TtkConceptAttributesRevision> getRevisionList() {
      return revisions;
   }

   public boolean isDefined() {
      return defined;
   }

   //~--- set methods ---------------------------------------------------------

   public void setDefined(boolean defined) {
      this.defined = defined;
   }
}
