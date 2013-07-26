package org.ihtsdo.otf.tcc.ddo.concept.component.identifier;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.ddo.ComponentReference;
import org.ihtsdo.otf.tcc.ddo.concept.component.VersionDdo;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.id.IdBI;
import org.ihtsdo.otf.tcc.api.id.LongIdBI;
import org.ihtsdo.otf.tcc.api.id.StringIdBI;
import org.ihtsdo.otf.tcc.api.id.UuidIdBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.xml.bind.annotation.XmlSeeAlso;

@XmlSeeAlso({ IdentifierLongDdo.class, IdentifierStringDdo.class, IdentifierUuidDdo.class })
public abstract class IdentifierDdo extends VersionDdo {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   protected ComponentReference authorityRef;

   //~--- constructors --------------------------------------------------------

   public IdentifierDdo() {
      super();
   }

   public IdentifierDdo(TerminologySnapshotDI ss, IdBI id) throws IOException, ContradictionException {
      super(ss, id);
      this.authorityRef = new ComponentReference(ss.getConceptVersion(id.getAuthorityNid()));
   }

   //~--- methods -------------------------------------------------------------

   public static IdentifierDdo convertId(TerminologySnapshotDI ss, IdBI id)
           throws IOException, ContradictionException {
      Object denotation = id.getDenotation();

      switch (IDENTIFIER_PART_TYPES.getType(denotation.getClass())) {
      case LONG :
         return new IdentifierLongDdo(ss, (LongIdBI) id);

      case STRING :
         return new IdentifierStringDdo(ss, (StringIdBI) id);

      case UUID :
         return new IdentifierUuidDdo(ss, (UuidIdBI) id);

      default :
         throw new UnsupportedOperationException();
      }
   }

   /**
    * Returns a string representation of the object.
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(" authority:");
      buff.append(this.authorityRef);
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   //~--- get methods ---------------------------------------------------------

   public ComponentReference getAuthorityRef() {
      return authorityRef;
   }

   public abstract Object getDenotation();

   public abstract IDENTIFIER_PART_TYPES getIdType();

   //~--- set methods ---------------------------------------------------------

   public void setAuthorityRef(ComponentReference authorityRef) {
      this.authorityRef = authorityRef;
   }

   public abstract void setDenotation(Object denotation);
}
