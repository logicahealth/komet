package org.ihtsdo.otf.tcc.dto.component;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.id.IdBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.uuid.UuidFactory;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;
import org.ihtsdo.otf.tcc.dto.component.identifier.IDENTIFIER_PART_TYPES;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifier;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifierLong;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifierString;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifierUuid;
import org.ihtsdo.otf.tcc.dto.component.refex.TtkRefexAbstractMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_array_of_bytearray.TtkRefexArrayOfByteArrayMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_boolean.TtkRefexBooleanMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_int.TtkRefexIntMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_long.TtkRefexLongMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_member.TtkRefexMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_string.TtkRefexStringMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid.TtkRefexUuidMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_boolean.TtkRefexUuidBooleanMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_float.TtkRefexUuidFloatMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_int.TtkRefexUuidIntMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_long.TtkRefexUuidLongMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_string.TtkRefexUuidStringMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid.TtkRefexUuidUuidMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_string.TtkRefexUuidUuidStringMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid.TtkRefexUuidUuidUuidMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_float.TtkRefexUuidUuidUuidFloatMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_int.TtkRefexUuidUuidUuidIntMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_long.TtkRefexUuidUuidUuidLongMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid_string.TtkRefexUuidUuidUuidStringMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.transformer.ComponentFields;
import org.ihtsdo.otf.tcc.dto.component.transformer.ComponentTransformerBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;

import javax.xml.bind.annotation.*;

/**
 * Class description
 *
 *
 * @param <V>
 *
 * @version        Enter version here..., 13/03/27
 * @author         Enter your name here...    
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class TtkComponentChronicle<V extends TtkRevision> extends TtkRevision {

   /** Field description */
   private static final long serialVersionUID = 1;

   /** Field description */
   @XmlElementWrapper(name = "additional-ids")
   @XmlElement(name = "id")
   public List<TtkIdentifier> additionalIds;

   /** Field description */
   @XmlElementWrapper(name = "annotations")
   @XmlElement(name = "refex")
   public List<TtkRefexAbstractMemberChronicle<?>> annotations;

   /** Field description */
   @XmlAttribute
   public UUID primordialUuid;

   /** Field description */
   @XmlElementWrapper(name = "revisions")
   @XmlElement(name = "revision")
   public List<V> revisions;

   /**
    * Constructs ...
    *
    */
   public TtkComponentChronicle() {
      super();
   }

   /**
    * Constructs ...
    *
    *
    * @param another
    *
    * @throws IOException
    */
   public TtkComponentChronicle(ComponentVersionBI another) throws IOException {
      super(another);

      Collection<? extends IdBI> anotherAdditionalIds = another.getAdditionalIds();

      if (anotherAdditionalIds != null) {
         this.additionalIds = new ArrayList<>(anotherAdditionalIds.size());
nextId:
         for (IdBI id : anotherAdditionalIds) {
            this.additionalIds.add((TtkIdentifier) TtkIdentifier.convertId(id));
         }
      }

      Collection<? extends RefexChronicleBI<?>> anotherAnnotations = another.getAnnotations();

      processAnnotations(anotherAnnotations);
      this.primordialUuid = another.getPrimordialUuid();
   }

   /**
    * Constructs ...
    *
    *
    * @param in
    * @param dataVersion
    *
    * @throws ClassNotFoundException
    * @throws IOException
    */
   public TtkComponentChronicle(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   /**
    * Constructs ...
    *
    *
    * @param another
    * @param transformer
    */
   public TtkComponentChronicle(TtkComponentChronicle<V> another, ComponentTransformerBI transformer) {
      super(another, transformer);

      if (another.additionalIds != null) {
         this.additionalIds = new ArrayList<>(another.additionalIds.size());

         for (TtkIdentifier id : another.additionalIds) {
            this.additionalIds.add((TtkIdentifier) id.makeTransform(transformer));
         }
      }

      if (another.annotations != null) {
         this.annotations = new ArrayList<>(another.annotations.size());

         for (TtkRefexAbstractMemberChronicle<?> r : another.annotations) {
            this.annotations.add((TtkRefexAbstractMemberChronicle<?>) r.makeTransform(transformer));
         }
      }

      this.primordialUuid = transformer.transform(another.primordialUuid, another,
          ComponentFields.PRIMORDIAL_UUID);

      if (another.revisions != null) {
         this.revisions = new ArrayList<>(another.revisions.size());

         for (V r : another.revisions) {
            this.revisions.add((V) r.makeTransform(transformer));
         }
      }
   }

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>EComponent</tt> object, and contains the same values, field by field,
    * as this <tt>EComponent</tt>.
    *
    * @param obj the object to compare with.
    * @return <code>true</code> if the objects are the same; <code>false</code>
    * otherwise.
    */
   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (TtkComponentChronicle.class.isAssignableFrom(obj.getClass())) {
         TtkComponentChronicle<?> another = (TtkComponentChronicle<?>) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare primordialComponentUuid
         if (!this.primordialUuid.equals(another.primordialUuid)) {
            return false;
         }

         // Compare additionalIdComponents
         if (this.additionalIds == null) {
            if (another.additionalIds == null) {             // Equal!
            } else if (another.additionalIds.isEmpty()) {    // Equal!
            } else {
               return false;
            }
         } else if (!this.additionalIds.equals(another.additionalIds)) {
            return false;
         }

         // Compare extraVersions
         if (this.revisions == null) {
            if (another.revisions == null) {                 // Equal!
            } else if (another.revisions.isEmpty()) {        // Equal!
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
    * Returns a hash code for this
    * <code>EComponent</code>.
    *
    * @return a hash code value for this <tt>EComponent</tt>.
    */
   @Override
   public int hashCode() {
      return Arrays.hashCode(new int[] { getPrimordialComponentUuid().hashCode(), status.hashCode(),
                                         pathUuid.hashCode(), (int) time, (int) (time >>> 32) });
   }

   /**
    * Method description
    *
    *
    * @param annotations
    *
    * @throws IOException
    */
   private void processAnnotations(Collection<? extends RefexChronicleBI<?>> annotations) throws IOException {
      if ((annotations != null) &&!annotations.isEmpty()) {
         this.annotations = new ArrayList<>(annotations.size());

         for (RefexChronicleBI<?> r : annotations) {
            this.annotations.add(TtkConceptChronicle.convertRefex(r));
         }
      }
   }

   /**
    * Method description
    *
    *
    * @param in
    * @param dataVersion
    *
    * @throws ClassNotFoundException
    * @throws IOException
    */
   @Override
   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      primordialUuid = new UUID(in.readLong(), in.readLong());

      short idVersionCount = in.readShort();

      assert idVersionCount < 500 : "idVersionCount is: " + idVersionCount;

      if (idVersionCount > 0) {
         additionalIds = new ArrayList<>(idVersionCount);

         for (int i = 0; i < idVersionCount; i++) {
            switch (IDENTIFIER_PART_TYPES.readType(in)) {
            case LONG :
               additionalIds.add(new TtkIdentifierLong(in, dataVersion));

               break;

            case STRING :
               additionalIds.add(new TtkIdentifierString(in, dataVersion));

               break;

            case UUID :
               additionalIds.add(new TtkIdentifierUuid(in, dataVersion));

               break;

            default :
               throw new UnsupportedOperationException();
            }
         }
      }

      short annotationCount = in.readShort();

      assert annotationCount < 5000 : "annotation count is: " + annotationCount;

      if (annotationCount > 0) {
         annotations = new ArrayList<>(annotationCount);

         for (int i = 0; i < annotationCount; i++) {
            RefexType type = RefexType.readType(in);

            switch (type) {
            case CID :
               annotations.add(new TtkRefexUuidMemberChronicle(in, dataVersion));

               break;

            case CID_CID :
               annotations.add(new TtkRefexUuidUuidMemberChronicle(in, dataVersion));

               break;

            case MEMBER :
               annotations.add(new TtkRefexMemberChronicle(in, dataVersion));

               break;

            case CID_CID_CID :
               annotations.add(new TtkRefexUuidUuidUuidMemberChronicle(in, dataVersion));

               break;

            case CID_CID_STR :
               annotations.add(new TtkRefexUuidUuidStringMemberChronicle(in, dataVersion));

               break;

            case INT :
               annotations.add(new TtkRefexIntMemberChronicle(in, dataVersion));

               break;

            case STR :
               annotations.add(new TtkRefexStringMemberChronicle(in, dataVersion));

               break;

            case CID_INT :
               annotations.add(new TtkRefexUuidIntMemberChronicle(in, dataVersion));

               break;

            case BOOLEAN :
               annotations.add(new TtkRefexBooleanMemberChronicle(in, dataVersion));

               break;

            case CID_FLOAT :
               annotations.add(new TtkRefexUuidFloatMemberChronicle(in, dataVersion));

               break;

            case CID_LONG :
               annotations.add(new TtkRefexUuidLongMemberChronicle(in, dataVersion));

               break;

            case CID_STR :
               annotations.add(new TtkRefexUuidStringMemberChronicle(in, dataVersion));

               break;

            case LONG :
               annotations.add(new TtkRefexLongMemberChronicle(in, dataVersion));

               break;

            case ARRAY_BYTEARRAY :
               annotations.add(new TtkRefexArrayOfByteArrayMemberChronicle(in, dataVersion));

               break;

            case CID_CID_CID_FLOAT :
               annotations.add(new TtkRefexUuidUuidUuidFloatMemberChronicle(in, dataVersion));

               break;

            case CID_CID_CID_INT :
               annotations.add(new TtkRefexUuidUuidUuidIntMemberChronicle(in, dataVersion));

               break;

            case CID_CID_CID_LONG :
               annotations.add(new TtkRefexUuidUuidUuidLongMemberChronicle(in, dataVersion));

               break;

            case CID_CID_CID_STRING :
               annotations.add(new TtkRefexUuidUuidUuidStringMemberChronicle(in, dataVersion));

               break;

            case CID_BOOLEAN :
               annotations.add(new TtkRefexUuidBooleanMemberChronicle(in, dataVersion));

               break;

            default :
               throw new UnsupportedOperationException("Can't handle refset type: " + type);
            }
         }
      }
   }

   /**
    * Returns a string representation of the object.
    *
    * @return
    */
   @Override
   public String toString() {
      int depth = 1;

      if (this instanceof TtkRefexAbstractMemberChronicle) {
         depth = 2;
      }

      StringBuilder buff = new StringBuilder();

      buff.append(" primordial:");
      buff.append(this.primordialUuid);
      buff.append(" xtraIds:");
      buff.append(this.additionalIds);
      buff.append(super.toString());

      if ((annotations != null) && (annotations.size() > 0)) {
         buff.append("\n" + TtkConceptChronicle.PADDING);

         for (int i = 0; i < depth; i++) {
            buff.append(TtkConceptChronicle.PADDING);
         }

         buff.append("annotations:\n");

         for (TtkRefexAbstractMemberChronicle m : this.annotations) {
            buff.append(TtkConceptChronicle.PADDING);
            buff.append(TtkConceptChronicle.PADDING);

            for (int i = 0; i < depth; i++) {
               buff.append(TtkConceptChronicle.PADDING);
            }

            buff.append(m);
            buff.append("\n");
         }
      }

      if ((revisions != null) && (revisions.size() > 0)) {
         buff.append("\n" + TtkConceptChronicle.PADDING + "revisions:\n");

         for (TtkRevision r : this.revisions) {
            buff.append(TtkConceptChronicle.PADDING);
            buff.append(TtkConceptChronicle.PADDING);

            for (int i = 0; i < depth; i++) {
               buff.append(TtkConceptChronicle.PADDING);
            }

            buff.append(r);
            buff.append("\n");
         }
      }

      return buff.toString();
   }

   /**
    * Method description
    *
    *
    * @param out
    *
    * @throws IOException
    */
   @Override
   public void writeExternal(DataOutput out) throws IOException {
      super.writeExternal(out);
      out.writeLong(primordialUuid.getMostSignificantBits());
      out.writeLong(primordialUuid.getLeastSignificantBits());

      if (additionalIds == null) {
         out.writeShort(0);
      } else {
         assert additionalIds.size() < 500 : "additionalIds is: " + additionalIds.size();
         out.writeShort(additionalIds.size());

         for (TtkIdentifier idv : additionalIds) {
            idv.getIdType().writeType(out);
            idv.writeExternal(out);
         }
      }

      if (annotations == null) {
         out.writeShort(0);
      } else {
         assert annotations.size() < 500 : "annotation count is: " + annotations.size();
         out.writeShort(annotations.size());

         for (TtkRefexAbstractMemberChronicle<?> r : annotations) {
            r.getType().writeType(out);
            r.writeExternal(out);
         }
      }
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public List<TtkIdentifier> getAdditionalIdComponents() {
      return additionalIds;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public List<TtkRefexAbstractMemberChronicle<?>> getAnnotations() {
      return annotations;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public List<TtkIdentifier> getEIdentifiers() {
      List<TtkIdentifier> ids;

      if (additionalIds != null) {
         ids = new ArrayList<>(additionalIds.size() + 1);
         ids.addAll(additionalIds);
      } else {
         ids = new ArrayList<>(1);
      }

      ids.add(new TtkIdentifierUuid(this.primordialUuid));

      return ids;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public int getIdComponentCount() {
      if (additionalIds == null) {
         return 1;
      }

      return additionalIds.size() + 1;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public UUID getPrimordialComponentUuid() {
      return primordialUuid;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public abstract List<? extends TtkRevision> getRevisionList();

   /**
    * Method description
    *
    *
    * @return
    */
   public List<V> getRevisions() {
      return revisions;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public List<UUID> getUuids() {
      List<UUID> uuids;

      if (additionalIds != null) {
         uuids = new ArrayList<>(additionalIds.size() + 1);
      } else {
         uuids = new ArrayList<>(1);
      }

      uuids.add(primordialUuid);

      if (additionalIds != null) {
         for (TtkIdentifier idv : additionalIds) {
            if (TtkIdentifierUuid.class.isAssignableFrom(idv.getClass())) {
               uuids.add((UUID) idv.getDenotation());
            }
         }
      }

      return uuids;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public int getVersionCount() {
      List<? extends TtkRevision> extraVersions = getRevisionList();

      if (extraVersions == null) {
         return 1;
      }

      return extraVersions.size() + 1;
   }

   /**
    * Method description
    *
    *
    * @param additionalIdComponents
    */
   public void setAdditionalIdComponents(List<TtkIdentifier> additionalIdComponents) {
      this.additionalIds = additionalIdComponents;
   }

   /**
    * Method description
    *
    *
    * @param annotations
    */
   public void setAnnotations(List<TtkRefexAbstractMemberChronicle<?>> annotations) {
      this.annotations = annotations;
   }

   /**
    * Method description
    *
    *
    * @param primordialComponentUuid
    */
   public void setPrimordialComponentUuid(UUID primordialComponentUuid) {
      this.primordialUuid = primordialComponentUuid;
   }

   /**
    * Method description
    *
    *
    * @param revisions
    */
   public void setRevisions(List<V> revisions) {
      this.revisions = revisions;
   }
}
