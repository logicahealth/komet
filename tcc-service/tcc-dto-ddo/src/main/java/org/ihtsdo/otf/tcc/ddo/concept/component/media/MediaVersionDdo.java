package org.ihtsdo.otf.tcc.ddo.concept.component.media;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.SimpleStringProperty;

import org.ihtsdo.otf.tcc.ddo.ComponentReference;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.media.MediaVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.xml.bind.annotation.XmlTransient;
import org.ihtsdo.otf.tcc.ddo.concept.component.ComponentVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.TypedComponentVersionDdo;

public class MediaVersionDdo extends TypedComponentVersionDdo<MediaChronicleDdo, MediaVersionDdo>  {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   private SimpleStringProperty textDescriptionProperty = new SimpleStringProperty(this, "textDescription");

   //~--- constructors --------------------------------------------------------

   public MediaVersionDdo() {
      super();
   }

   public MediaVersionDdo(MediaChronicleDdo chronicle, TerminologySnapshotDI ss, MediaVersionBI another)
           throws IOException, ContradictionException {
      super(chronicle, ss, another);
      this.textDescriptionProperty.set(another.getTextDescription());
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>EImageVersion</tt> object, and contains the same values, field by field,
    * as this <tt>EImageVersion</tt>.
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

      if (MediaVersionDdo.class.isAssignableFrom(obj.getClass())) {
         MediaVersionDdo another = (MediaVersionDdo) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare textDescriptionProperty
         if (!this.textDescriptionProperty.equals(another.textDescriptionProperty)) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   public SimpleStringProperty textDescriptionProperty() {
      return textDescriptionProperty;
   }

   /**
    * Returns a string representation of the object.
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(this.getClass().getSimpleName()).append(": ");
      buff.append(" desc:");
      buff.append("'").append(this.textDescriptionProperty.get()).append("' ");
      buff.append(super.toString());

      return buff.toString();
   }

   //~--- get methods ---------------------------------------------------------

   public String getTextDescription() {
      return textDescriptionProperty.get();
   }

   //~--- set methods ---------------------------------------------------------

   public void setTextDescription(String textDescription) {
      this.textDescriptionProperty.set(textDescription);
   }
   
   @XmlTransient
   public byte[] getDataBytes() {
      return chronicle.dataBytes;
   }
   
   @XmlTransient
   public String getFormat() {
      return chronicle.format;
   }

}
