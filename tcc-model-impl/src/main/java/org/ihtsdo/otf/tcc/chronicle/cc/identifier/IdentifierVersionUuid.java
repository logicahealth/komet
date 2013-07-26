package org.ihtsdo.otf.tcc.chronicle.cc.identifier;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import java.io.IOException;

import org.ihtsdo.otf.tcc.chronicle.cc.component.ConceptComponent.IDENTIFIER_PART_TYPES;
import org.ihtsdo.otf.tcc.api.id.UuidIdBI;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifierUuid;

//~--- JDK imports ------------------------------------------------------------

import java.util.UUID;
import org.ihtsdo.otf.tcc.api.coordinate.Status;

public class IdentifierVersionUuid extends IdentifierVersion implements UuidIdBI {
   private long lsb;
   private long msb;

   //~--- constructors --------------------------------------------------------

   public IdentifierVersionUuid() {
      super();
   }

   public IdentifierVersionUuid(TtkIdentifierUuid idv) throws IOException {
      super(idv);
      msb = idv.getDenotation().getMostSignificantBits();
      lsb = idv.getDenotation().getLeastSignificantBits();
   }

   public IdentifierVersionUuid(TupleInput input) {
      super(input);
      msb = input.readLong();
      lsb = input.readLong();
   }

   public IdentifierVersionUuid(Status status, long time, int authorNid, int moduleNid,
           int pathNid, int authorityNid, UUID uuid) {
      super(status, time, authorNid, moduleNid, pathNid, authorityNid);
      msb = uuid.getMostSignificantBits();
      lsb = uuid.getLeastSignificantBits();
   }

   public IdentifierVersionUuid(IdentifierVersionUuid another, Status status, long time,
           int authorNid, int moduleNid, int pathNid) {
      super(status, time, authorNid, moduleNid, pathNid, another.authorityNid);
      msb = another.msb;
      lsb = another.lsb;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (IdentifierVersionUuid.class.isAssignableFrom(obj.getClass())) {
         IdentifierVersionUuid another = (IdentifierVersionUuid) obj;

         return (this.msb == another.msb) && (this.lsb == another.lsb) && super.equals(another);
      }

      return false;
   }

   @Override
   public int hashCode() {
      int hash = 3;

      hash = 97 * hash + (int) (this.msb ^ (this.msb >>> 32));

      return hash;
   }
   @Override
   public final boolean readyToWriteIdentifier() {
      return true;
   }

   /*
    * (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuilder buf = new StringBuilder();

      buf.append(this.getClass().getSimpleName()).append(": ");
      buf.append("uuid:").append(getUuid());
      buf.append(" ");
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   protected void writeSourceIdToBdb(TupleOutput output) {
      output.writeLong(msb);
      output.writeLong(lsb);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public UUID getDenotation() {
      return getUuid();
   }

   @Override
   public IDENTIFIER_PART_TYPES getType() {
      return IDENTIFIER_PART_TYPES.UUID;
   }

   public UUID getUuid() {
      return new UUID(msb, lsb);
   }

}
