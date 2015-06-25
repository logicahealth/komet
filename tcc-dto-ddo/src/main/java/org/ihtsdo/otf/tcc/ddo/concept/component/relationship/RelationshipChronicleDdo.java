package org.ihtsdo.otf.tcc.ddo.concept.component.relationship;

//~--- non-JDK imports --------------------------------------------------------

import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.ochre.api.relationship.RelationshipVersionAdaptor;
import javafx.beans.property.SimpleObjectProperty;

import org.ihtsdo.otf.tcc.ddo.ComponentReference;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.ComponentChronicleDdo;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement()
public class RelationshipChronicleDdo
        extends ComponentChronicleDdo<RelationshipVersionDdo, RelationshipVersionAdaptor> {
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

   public RelationshipChronicleDdo(TaxonomyCoordinate ss, ConceptChronicleDdo concept,
                                  ObjectChronology<? extends RelationshipVersionAdaptor> another)
           throws IOException, ContradictionException {
      super(ss, concept, another);
      RelationshipVersionAdaptor primordialVersion = another.getVersionList().get(0);
      this.originReferenceProperty.set(
          new ComponentReference(primordialVersion.getOriginSequence(), 
              ss.getStampCoordinate(), ss.getLanguageCoordinate()));
      this.destinationReferenceProperty.set(
          new ComponentReference(primordialVersion.getDestinationSequence(), 
              ss.getStampCoordinate(), ss.getLanguageCoordinate()));
   }

   //~--- methods -------------------------------------------------------------

   public SimpleObjectProperty<ComponentReference> destinationReferenceProperty() {
      return destinationReferenceProperty;
   }

   @Override
   protected RelationshipVersionDdo makeVersion(TaxonomyCoordinate ss, RelationshipVersionAdaptor version)
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
