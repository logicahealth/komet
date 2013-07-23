package org.ihtsdo.otf.tcc.dto.component.description;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.dto.component.TtkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.UUID;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import org.ihtsdo.otf.tcc.dto.component.transformer.ComponentFields;
import org.ihtsdo.otf.tcc.dto.component.transformer.ComponentTransformerBI;

@XmlRootElement(name="description-revision")
public class TtkDescriptionRevision extends TtkRevision {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   @XmlAttribute
   public boolean initialCaseSignificant;
   @XmlAttribute
   public String  lang;
   @XmlAttribute
   public String  text;
   @XmlAttribute
   public UUID    typeUuid;

   //~--- constructors --------------------------------------------------------

   public TtkDescriptionRevision() {
      super();
   }

   public TtkDescriptionRevision(DescriptionVersionBI another) throws IOException {
      super(another);
      this.initialCaseSignificant = another.isInitialCaseSignificant();
      this.lang                   = another.getLang();
      this.text                   = another.getText();
      this.typeUuid               = Ts.get().getUuidPrimordialForNid(another.getTypeNid());
   }

   public TtkDescriptionRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   public TtkDescriptionRevision(TtkDescriptionRevision another, ComponentTransformerBI transformer) {
      super(another, transformer);
      this.initialCaseSignificant = transformer.transform(another.initialCaseSignificant, another, ComponentFields.DESCRIPTION_INITIAL_CASE_SIGNIFICANT);
      this.lang                   = transformer.transform(another.lang, another, ComponentFields.DESCRIPTION_LANGUAGE);
      this.text                   = transformer.transform(another.text, another, ComponentFields.DESCRIPTION_TEXT);
      this.typeUuid = transformer.transform(another.typeUuid, another, ComponentFields.DESCRIPTION_TYPE_UUID);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>EDescriptionVersion</tt> object, and contains the same values, field by field,
    * as this <tt>EDescriptionVersion</tt>.
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

      if (TtkDescriptionRevision.class.isAssignableFrom(obj.getClass())) {
         TtkDescriptionRevision another = (TtkDescriptionRevision) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare initialCaseSignificant
         if (this.initialCaseSignificant != another.initialCaseSignificant) {
            return false;
         }

         // Compare lang
         if (!this.lang.equals(another.lang)) {
            return false;
         }

         // Compare text
         if (!this.text.equals(another.text)) {
            return false;
         }

         // Compare typeUuid
         if (!this.typeUuid.equals(another.typeUuid)) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   @Override
   public TtkDescriptionRevision makeTransform(ComponentTransformerBI transformer) {
      return new TtkDescriptionRevision(this, transformer);
   }

   @Override
   public final void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      initialCaseSignificant = in.readBoolean();
      lang                   = in.readUTF();

      if (dataVersion < 7) {
         text = in.readUTF();
      } else {
         int textlength = in.readInt();

         if (textlength > 32000) {
            int    textBytesLength = in.readInt();
            byte[] textBytes       = new byte[textBytesLength];

            in.readFully(textBytes);
            text = new String(textBytes, "UTF-8");
         } else {
            text = in.readUTF();
         }
      }

      typeUuid = new UUID(in.readLong(), in.readLong());
   }

   /**
    * Returns a string representation of the object.
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(this.getClass().getSimpleName()).append(": ");
      buff.append(" ics:");
      buff.append(this.initialCaseSignificant);
      buff.append(" lang:");
      buff.append("'").append(this.lang).append("'");
      buff.append(" text:");
      buff.append("'").append(this.text).append("'");
      buff.append(" type:");
      buff.append(informAboutUuid(this.typeUuid));
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   @Override
   public void writeExternal(DataOutput out) throws IOException {
      super.writeExternal(out);
      out.writeBoolean(initialCaseSignificant);
      out.writeUTF(lang);
      out.writeInt(text.length());

      if (text.length() > 32000) {
         byte[] textBytes = text.getBytes("UTF-8");

         out.writeInt(textBytes.length);
         out.write(textBytes);
      } else {
         out.writeUTF(text);
      }

      out.writeLong(typeUuid.getMostSignificantBits());
      out.writeLong(typeUuid.getLeastSignificantBits());
   }

   //~--- get methods ---------------------------------------------------------

   /*
    * (non-Javadoc)
    *
    * @see org.ihtsdo.etypes.I_DescribeExternally#getLang()
    */
   public String getLang() {
      return lang;
   }

   /*
    * (non-Javadoc)
    *
    * @see org.ihtsdo.etypes.I_DescribeExternally#getText()
    */
   public String getText() {
      return text;
   }

   public UUID getTypeUuid() {
      return typeUuid;
   }

   /*
    * (non-Javadoc)
    *
    * @see org.ihtsdo.etypes.I_DescribeExternally#isInitialCaseSignificant()
    */
   public boolean isInitialCaseSignificant() {
      return initialCaseSignificant;
   }

   //~--- set methods ---------------------------------------------------------

   public void setInitialCaseSignificant(boolean initialCaseSignificant) {
      this.initialCaseSignificant = initialCaseSignificant;
   }

   public void setLang(String lang) {
      this.lang = lang;
   }

   public void setText(String text) {
      this.text = text;
   }

   public void setTypeUuid(UUID typeUuid) {
      this.typeUuid = typeUuid;
   }
}
