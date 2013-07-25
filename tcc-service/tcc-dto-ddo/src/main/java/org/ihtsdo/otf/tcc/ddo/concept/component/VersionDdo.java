package org.ihtsdo.otf.tcc.ddo.concept.component;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.SimpleObjectProperty;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.id.IdBI;
import org.ihtsdo.otf.tcc.ddo.ComponentReference;
import org.ihtsdo.otf.tcc.ddo.TimeReference;
import org.ihtsdo.otf.tcc.ddo.concept.component.attribute.ConceptAttributesVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.description.DescriptionVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.identifier.IdentifierDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.media.MediaVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_boolean.RefexBooleanVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp.RefexCompVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_int.RefexIntVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_long.RefexLongVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_member.RefexMembershipVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_string.RefexStringVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.relationship.RelationshipVersionDdo;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.Serializable;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlSeeAlso;
import org.ihtsdo.otf.tcc.api.coordinate.Status;

@XmlSeeAlso( {
   ConceptAttributesVersionDdo.class, DescriptionVersionDdo.class, IdentifierDdo.class,
   RelationshipVersionDdo.class, MediaVersionDdo.class, RefexCompVersionDdo.class, DescriptionVersionDdo.class,
   RefexLongVersionDdo.class, RefexMembershipVersionDdo.class, RefexBooleanVersionDdo.class,
   RefexStringVersionDdo.class, RefexIntVersionDdo.class
})
public abstract class VersionDdo implements Serializable {
   private static final long                          serialVersionUID    = 1;
   public static UUID                                 unspecifiedUserUuid =
      UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c");
   private ComponentReference                       authorReference;
   private SimpleObjectProperty<ComponentReference> authorReferenceProperty;
   private TimeReference                                     fxTime;
   private SimpleObjectProperty<TimeReference>               fxTimeProperty;
   private ComponentReference                       moduleReference;
   private SimpleObjectProperty<ComponentReference> moduleReferenceProperty;
   private ComponentReference                       pathReference;
   private SimpleObjectProperty<ComponentReference> pathReferenceProperty;
   private Status                                     status;
   private SimpleObjectProperty<Status>               statusProperty;
   private UUID                                       viewCoordinateUuid;

   public VersionDdo() {
      super();
   }

   public VersionDdo(TerminologySnapshotDI ss, ComponentVersionBI another)
           throws IOException, ContradictionException {
      super();
      status                  = another.getStatus();
      fxTime                  = new TimeReference(another.getTime());
      authorReference         = new ComponentReference(ss.getConceptForNid(another.getAuthorNid()));
      moduleReference         = new ComponentReference(ss.getConceptForNid(another.getModuleNid()));
      pathReference           = new ComponentReference(ss.getConceptForNid(another.getPathNid()));
      viewCoordinateUuid = ss.getViewCoordinate().getVcUuid();
      assert status != null: "status is null";
      assert fxTime != null: "fxTime is null";
      assert authorReference != null: "authorReference is null";
      assert moduleReference != null: "moduleReference is null";
      assert pathReference != null: "pathReference is null";
      assert viewCoordinateUuid != null: "viewCoordinateUuid is null";
   }

   public VersionDdo(TerminologySnapshotDI ss, IdBI id) throws IOException, ContradictionException {
      super();
      status                  = id.getStatus();
      fxTime                  = new TimeReference(id.getTime());
      authorReference         = new ComponentReference(ss.getConceptVersion(id.getAuthorNid()));
      moduleReference         = new ComponentReference(ss.getConceptVersion(id.getPathNid()));
      pathReference           = new ComponentReference(ss.getConceptVersion(id.getModuleNid()));
      this.viewCoordinateUuid = ss.getViewCoordinate().getVcUuid();
   }

   public SimpleObjectProperty<ComponentReference> authorReferenceProperty() {
      if (authorReferenceProperty == null) {
         authorReferenceProperty = new SimpleObjectProperty<>(this, "authorReference", authorReference);
      }

      return authorReferenceProperty;
   }

   public SimpleObjectProperty<TimeReference> fxTimeProperty() {
      if (fxTimeProperty == null) {
         fxTimeProperty = new SimpleObjectProperty<>(this, "time", fxTime);
      }

      return fxTimeProperty;
   }

   public static CharSequence informAboutUuid(UUID uuid) {
      if (uuid == null) {
         return "null";
      }

      if (Ts.get() == null) {
         return uuid.toString();
      }

      StringBuilder sb = new StringBuilder();

      if (Ts.get().hasUuid(uuid)) {
         try {
            int nid  = Ts.get().getNidForUuids(uuid);
            int cNid = Ts.get().getConceptNidForNid(nid);

            if (cNid == nid) {
               ConceptChronicleBI cc = Ts.get().getConcept(cNid);

               sb.append("'");
               sb.append(cc.toUserString());
               sb.append("' ");
               sb.append(cNid);
               sb.append(" ");
            } else {
               ComponentBI component = Ts.get().getComponent(nid);

               sb.append("comp: '");

               if (component != null) {
                  sb.append(component.toUserString());
               } else {
                  sb.append("null");
               }

               sb.append("' ");
               sb.append(nid);
               sb.append(" ");
            }
         } catch (IOException ex) {
            Logger.getLogger(VersionDdo.class.getName()).log(Level.SEVERE, null, ex);
         }
      }

      sb.append(uuid.toString());

      return sb;
   }

   public SimpleObjectProperty<ComponentReference> moduleReferenceProperty() {
      if (moduleReferenceProperty == null) {
         moduleReferenceProperty = new SimpleObjectProperty<>(this, "moduleReference", moduleReference);
      }

      return moduleReferenceProperty;
   }

   public SimpleObjectProperty<ComponentReference> pathReferenceProperty() {
      if (pathReferenceProperty == null) {
         pathReferenceProperty = new SimpleObjectProperty<>(this, "pathReference", pathReference);
      }

      return pathReferenceProperty;
   }

   /*
    * (non-Javadoc)
    *
    * @see org.ihtsdo.etypes.I_VersionExternal#getPathReference()
    */
   public SimpleObjectProperty<Status> statusReferenceProperty() {
      if (statusProperty == null) {
         statusProperty = new SimpleObjectProperty<>(this, "status", status);
      }

      return statusProperty;
   }

   /**
    * Returns a string representation of the object.
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(" s:");
      buff.append(getStatus());
      buff.append(" t: ");
      buff.append(getFxTime());
      buff.append(" a:");
      buff.append(getAuthorReference());
      buff.append(" m:");
      buff.append(getModuleReference());
      buff.append(" p:");
      buff.append(getPathReference());

      return buff.toString();
   }

   public ComponentReference getAuthorReference() {
      return (authorReferenceProperty == null)
             ? authorReference
             : authorReferenceProperty.get();
   }

   public TimeReference getFxTime() {
      return (fxTimeProperty == null)
             ? fxTime
             : fxTimeProperty.get();
   }

   public ComponentReference getModuleReference() {
      return (moduleReferenceProperty == null)
             ? moduleReference
             : moduleReferenceProperty.get();
   }

   public ComponentReference getPathReference() {
      return (pathReferenceProperty == null)
             ? pathReference
             : pathReferenceProperty.get();
   }
   public Status getStatus() {
      return (statusProperty == null)
             ? status
             : statusProperty.get();
   }

   public UUID getViewCoordinateUuid() {
      return viewCoordinateUuid;
   }

   public void setAuthorReference(ComponentReference authorReference) {
      if (authorReferenceProperty == null) {
         this.authorReference = authorReference;
      } else {
         authorReferenceProperty.set(authorReference);
      }
   }

   public void setFxTime(TimeReference fxTime) {
      if (fxTimeProperty == null) {
         this.fxTime = fxTime;
      } else {
         fxTimeProperty.set(fxTime);
      }
   }

   public void setModuleReference(ComponentReference moduleReference) {
      if (moduleReferenceProperty == null) {
         this.moduleReference = moduleReference;
      } else {
         moduleReferenceProperty.set(moduleReference);
      }
   }

   public void setPathReference(ComponentReference pathReference) {
      if (pathReferenceProperty == null) {
         this.pathReference = pathReference;
      } else {
         pathReferenceProperty.set(pathReference);
      }
   }

   public void setStatus(Status status) {
      if (statusProperty == null) {
         this.status = status;
      } else {
         statusProperty.set(status);
      }
   }

   public void setViewCoordinateUuid(UUID viewCoordinateUuid) {
       assert viewCoordinateUuid != null;
      this.viewCoordinateUuid = viewCoordinateUuid;
   }
}
