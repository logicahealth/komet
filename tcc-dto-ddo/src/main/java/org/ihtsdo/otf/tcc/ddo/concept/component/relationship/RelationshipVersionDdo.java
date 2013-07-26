package org.ihtsdo.otf.tcc.ddo.concept.component.relationship;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import org.ihtsdo.otf.tcc.ddo.ComponentReference;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import org.ihtsdo.otf.tcc.ddo.concept.component.TypedComponentVersionDdo;

public class RelationshipVersionDdo
        extends TypedComponentVersionDdo<RelationshipChronicleDdo, RelationshipVersionDdo> {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   protected SimpleIntegerProperty                      groupProperty                   = new SimpleIntegerProperty(this, "group");
   protected SimpleObjectProperty<ComponentReference> characteristicReferenceProperty =
      new SimpleObjectProperty<>(this, "characteristic");
   protected SimpleObjectProperty<ComponentReference> refinabilityReferenceProperty =
      new SimpleObjectProperty<>(this, "refinability");

   //~--- constructors --------------------------------------------------------

   public RelationshipVersionDdo() {
      super();
   }

   public RelationshipVersionDdo(RelationshipChronicleDdo chronicle, TerminologySnapshotDI ss,
                                RelationshipVersionBI rv)
           throws IOException, ContradictionException {
      super(chronicle, ss, rv);
      characteristicReferenceProperty.set(
          new ComponentReference(ss.getConceptVersion(rv.getCharacteristicNid())));
      refinabilityReferenceProperty.set(
          new ComponentReference(ss.getConceptVersion(rv.getRefinabilityNid())));
      groupProperty.set(rv.getGroup());
      typeReferenceProperty.set(new ComponentReference(ss.getConceptVersion(rv.getTypeNid())));
   }

   //~--- methods -------------------------------------------------------------

   public SimpleObjectProperty<ComponentReference> characteristicReferenceProperty() {
      return characteristicReferenceProperty;
   }

   public SimpleIntegerProperty groupProperty() {
      return groupProperty;
   }

   public SimpleObjectProperty<ComponentReference> refinabilityReferenceProperty() {
      return refinabilityReferenceProperty;
   }

   /**
    * Returns a string representation of the object.
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(this.getClass().getSimpleName()).append(": ");
      buff.append(" origin:");
      buff.append(this.getOriginReference());
      buff.append(" type:");
      buff.append(this.typeReferenceProperty.get());
      buff.append(" destination:");
      buff.append(this.getDestinationReference());
      buff.append(" grp:");
      buff.append(this.groupProperty.get());
      buff.append(" char:");
      buff.append(this.characteristicReferenceProperty.get());
      buff.append(" ref:");
      buff.append(this.refinabilityReferenceProperty.get());
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   //~--- get methods ---------------------------------------------------------

   public ComponentReference getCharacteristicReference() {
      return characteristicReferenceProperty.get();
   }

   public ComponentReference getDestinationReference() {
      return this.chronicle.getDestinationReference();
   }

   public ComponentReference getOriginReference() {
      return this.chronicle.getOriginReference();
   }

   public ComponentReference getRefinabilityReference() {
      return refinabilityReferenceProperty.get();
   }

   public int getRelGroup() {
      return groupProperty.get();
   }

   //~--- set methods ---------------------------------------------------------

   public void setCharacteristicReference(ComponentReference characteristicReference) {
      this.characteristicReferenceProperty.set(characteristicReference);
   }

   public void setRefinabilityReference(ComponentReference refinabilityReference) {
      this.refinabilityReferenceProperty.set(refinabilityReference);
   }

   public void setRelGroup(int relGroup) {
      this.groupProperty.set(relGroup);
   }
 }
