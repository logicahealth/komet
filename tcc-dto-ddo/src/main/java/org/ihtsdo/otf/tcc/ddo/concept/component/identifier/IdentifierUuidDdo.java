package org.ihtsdo.otf.tcc.ddo.concept.component.identifier;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.SimpleObjectProperty;

import org.ihtsdo.otf.tcc.ddo.ComponentReference;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.id.UuidIdBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.UUID;


public class IdentifierUuidDdo extends IdentifierDdo {
   public static final long serialVersionUID = 1;
   public static UUID       generatedUuid    = UUID.fromString("2faa9262-8fb2-11db-b606-0800200c9a66");

   //~--- fields --------------------------------------------------------------

   protected SimpleObjectProperty<UUID> denotationProperty = new SimpleObjectProperty<>(this, "denotation");

   //~--- constructors --------------------------------------------------------

   public IdentifierUuidDdo() {
      super();
   }

   public IdentifierUuidDdo(TerminologySnapshotDI ss, UUID denotation)
           throws IOException, ContradictionException {
      super();
      this.denotationProperty.set(denotation);
      this.authorityRef = new ComponentReference(ss.getConceptVersion(generatedUuid));
   }

   public IdentifierUuidDdo(TerminologySnapshotDI ss, UuidIdBI id) throws IOException, ContradictionException {
      super(ss, id);
      denotationProperty.set(id.getDenotation());
   }

   //~--- methods -------------------------------------------------------------

   public SimpleObjectProperty<UUID> denotationProperty() {
      return denotationProperty;
   }

   /**
    * Returns a string representation of the object.
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(this.getClass().getSimpleName()).append(": ");
      buff.append(" denotation:");
      buff.append(this.denotationProperty.get());
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public UUID getDenotation() {
      return denotationProperty.get();
   }

   @Override
   public IDENTIFIER_PART_TYPES getIdType() {
      return IDENTIFIER_PART_TYPES.UUID;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setDenotation(Object denotation) {
      this.denotationProperty.set((UUID) denotation);
   }
}
