package org.ihtsdo.otf.tcc.ddo.concept.component.relationship;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.SimpleObjectProperty;

import org.ihtsdo.otf.tcc.ddo.ComponentReference;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.ComponentChronicleDdo;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement()
public class RelationshipChronicleDdo
        extends ComponentChronicleDdo<RelationshipVersionDdo, RelationshipVersionBI<?>> {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   protected SimpleObjectProperty<ComponentReference> destinationReferenceProperty =
      new SimpleObjectProperty<>(this, "destination");
   protected SimpleObjectProperty<ComponentReference> originReferenceProperty =
      new SimpleObjectProperty<>(this, "origin");

   //~--- constructors --------------------------------------------------------

   public RelationshipChronicleDdo() {
      super();
   }

   public RelationshipChronicleDdo(TerminologySnapshotDI ss, ConceptChronicleDdo concept,
                                  RelationshipChronicleBI another)
           throws IOException, ContradictionException {
      super(ss, concept, another.getPrimordialVersion());
      this.originReferenceProperty.set(
          new ComponentReference(ss.getConceptVersion(another.getOriginNid())));
      this.destinationReferenceProperty.set(
          new ComponentReference(ss.getConceptVersion(another.getDestinationNid())));
   }

   //~--- methods -------------------------------------------------------------

   public SimpleObjectProperty<ComponentReference> destinationReferenceProperty() {
      return destinationReferenceProperty;
   }

   @Override
   protected RelationshipVersionDdo makeVersion(TerminologySnapshotDI ss, RelationshipVersionBI version)
           throws IOException, ContradictionException {
      return new RelationshipVersionDdo(this, ss, version);
   }

   public SimpleObjectProperty<ComponentReference> originReferenceProperty() {
      return originReferenceProperty;
   }

   //~--- get methods ---------------------------------------------------------

   public ComponentReference getDestinationReference() {
      return destinationReferenceProperty.get();
   }

   public ComponentReference getOriginReference() {
      return originReferenceProperty.get();
   }

   //~--- set methods ---------------------------------------------------------

   public void setDestinationReference(ComponentReference destinationReference) {
      this.destinationReferenceProperty.set(destinationReference);
   }

   public void setOriginReference(ComponentReference originReference) {
      this.originReferenceProperty.set(originReference);
   }
}
