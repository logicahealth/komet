package org.ihtsdo.otf.tcc.ddo.concept.component;

//~--- non-JDK imports --------------------------------------------------------

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.identifier.IdentifierDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.identifier.IdentifierUuidDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexChronicleDdo;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.id.IdBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.Serializable;

import java.util.*;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;

public abstract class ComponentChronicleDdo<V extends ComponentVersionDdo, T extends ComponentVersionBI>
        implements Serializable {
   private static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   @XmlElementWrapper(name = "additionalIdList")
   @XmlElement(name = "additionalId")
   public ObservableList<IdentifierDdo> additionalIds =
      FXCollections.observableArrayList(new ArrayList<IdentifierDdo>(0));
   @XmlElementWrapper(name = "annotationList")
   @XmlElement(name = "annotation")
   public ObservableList<RefexChronicleDdo<?, ?>> refexes =
      FXCollections.observableArrayList(new ArrayList<RefexChronicleDdo<?, ?>>(0));
   private int               componentNid;
   @XmlTransient
   protected ConceptChronicleDdo       concept;
   private UUID              primordialComponentUuid;
   @XmlElementWrapper(name = "versionList")
   @XmlElement()
   private ObservableList<V> versions;

   //~--- constructors --------------------------------------------------------

   public ComponentChronicleDdo() {
      super();
      this.versions = FXCollections.observableArrayList(new ArrayList<V>(1));
   }

   public ComponentChronicleDdo(TerminologySnapshotDI ss, ConceptChronicleDdo concept, ComponentChronicleBI<T> another)
           throws IOException, ContradictionException {
      super();
      this.concept                 = concept;
      this.primordialComponentUuid = another.getPrimordialUuid();
      this.componentNid            = another.getNid();

      Collection<? extends IdBI> anotherAdditionalIds = another.getAdditionalIds();

      if (anotherAdditionalIds != null) {
         this.additionalIds =
            FXCollections.observableArrayList(new ArrayList<IdentifierDdo>(anotherAdditionalIds.size()));
         nextId:
         for (IdBI id : anotherAdditionalIds) {
            this.additionalIds.add((IdentifierDdo) IdentifierDdo.convertId(ss, id));
         }
      }

      processRefexes(ss, another);

      switch (concept.getVersionPolicy()) {
      case ACTIVE_VERSIONS :
         this.versions = FXCollections.observableArrayList(new ArrayList<V>(1));

         for (T v : another.getVersions(ss.getViewCoordinate())) {
            this.versions.add(makeVersion(ss, v));
         }
         
         for (RefexChronicleBI<?> annotation: another.getAnnotations()) {
             for (RefexVersionBI av: annotation.getVersions(ss.getViewCoordinate())) {
                 
             }
         }

         break;

      case LAST_VERSIONS :
         this.versions = FXCollections.observableArrayList(new ArrayList<V>(1));

         for (T v : another.getVersions(ss.getViewCoordinate().getVcWithAllStatusValues())) {
            this.versions.add(makeVersion(ss, v));
         }

         break;

      case ALL_VERSIONS :
         this.versions = FXCollections.observableArrayList(new ArrayList<V>(another.getVersions().size()));

         for (T v : another.getVersions()) {
            this.versions.add(makeVersion(ss, v));
         }

         break;

      default :
         throw new UnsupportedOperationException("Can't get versions for policy: "
                 + concept.getVersionPolicy());
      }
   }

   //~--- methods -------------------------------------------------------------

   public void beforeUnmarshal(Unmarshaller u, Object parent) {
      if (parent instanceof ConceptChronicleDdo) {
         this.concept = (ConceptChronicleDdo) parent;
      } else if (parent instanceof ComponentChronicleDdo) {
         this.concept = ((ComponentChronicleDdo) parent).getConcept();
      }
   }

   /**
    * Compares this object to the specified object. The result is <tt>true</tt> if and only if the argument
    * is not <tt>null</tt>, is a <tt>EComponent</tt> object, and contains the same values, field by field, as
    * this <tt>EComponent</tt>.
    *
    * @param obj the object to compare with.
    * @return <code>true</code> if the objects are the same; <code>false</code> otherwise.
    */
   @Override
   public final boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (ComponentChronicleDdo.class.isAssignableFrom(obj.getClass())) {
         ComponentChronicleDdo<V, T> another = (ComponentChronicleDdo<V, T>) obj;

         return this.primordialComponentUuid.equals(another.primordialComponentUuid);
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
   public final int hashCode() {
      return this.primordialComponentUuid.hashCode();
   }

   protected abstract V makeVersion(TerminologySnapshotDI ss, T version)
           throws IOException, ContradictionException;

   private void processRefexes(TerminologySnapshotDI ss, ComponentChronicleBI<T> another)
           throws IOException, ContradictionException {
      HashSet<RefexChronicleBI<?>> refexesToProcess = new HashSet<>();

      switch (getConcept().getRefexPolicy()) {
      case REFEX_MEMBERS :
      case REFEX_MEMBERS_AND_REFSET_MEMBERS :
         refexesToProcess.addAll(another.getRefexes());

         break;

      case ANNOTATION_MEMBERS :
      case ANNOTATION_MEMBERS_AND_REFSET_MEMBERS :
         refexesToProcess.addAll(another.getAnnotations());
      }

      for (RefexChronicleBI<?> r : refexesToProcess) {
            RefexChronicleDdo<?, ?> fxRefexMember = ConceptChronicleDdo.convertRefex(ss, concept, r);
            if (!fxRefexMember.getVersions().isEmpty()) {
                this.refexes.add(fxRefexMember);
            }
      }
   }

   /**
    * Returns a string representation of the object.
    */
    @Override
   public final String toString() {
      int depth = 1;

      if (this instanceof RefexChronicleDdo<?, ?>) {
         depth = 2;
      }

      StringBuilder buff = new StringBuilder();

      buff.append(this.getClass().getSimpleName()).append(": ");
      buff.append(" primordial:");
      buff.append(this.primordialComponentUuid);
      buff.append(" xtraIds:");
      buff.append(this.additionalIds);
      buff.append(super.toString());

      if ((refexes != null) && (refexes.size() > 0)) {
         buff.append("\n" + ConceptChronicleDdo.PADDING);

         for (int i = 0; i < depth; i++) {
            buff.append(ConceptChronicleDdo.PADDING);
         }

         buff.append("annotations:\n");

         for (RefexChronicleDdo m : this.refexes) {
            buff.append(ConceptChronicleDdo.PADDING);
            buff.append(ConceptChronicleDdo.PADDING);

            for (int i = 0; i < depth; i++) {
               buff.append(ConceptChronicleDdo.PADDING);
            }

            buff.append(m);
            buff.append("\n");
         }
      }

      if ((versions != null) && (versions.size() > 0)) {
         buff.append("\n" + ConceptChronicleDdo.PADDING + "revisions:\n");

         for (VersionDdo r : this.versions) {
            buff.append(ConceptChronicleDdo.PADDING);
            buff.append(ConceptChronicleDdo.PADDING);

            for (int i = 0; i < depth; i++) {
               buff.append(ConceptChronicleDdo.PADDING);
            }

            buff.append(r);
            buff.append("\n");
         }
      }

      return buff.toString();
   }

   //~--- get methods ---------------------------------------------------------

   public List<IdentifierDdo> getAdditionalIds() {
      return additionalIds;
   }

   public int getComponentNid() {
      return componentNid;
   }

   public ConceptChronicleDdo getConcept() {
      return concept;
   }

   public int getIdCount() {
      if (additionalIds == null) {
         return 1;
      }

      return additionalIds.size() + 1;
   }

   public UUID getPrimordialComponentUuid() {
      return primordialComponentUuid;
   }

   public List<RefexChronicleDdo<?, ?>> getRefexes() {
      return refexes;
   }

   public List<UUID> getUuids() {
      List<UUID> uuids = new ArrayList<>();

      uuids.add(primordialComponentUuid);

      if (additionalIds != null) {
         for (IdentifierDdo idv : additionalIds) {
            if (IdentifierUuidDdo.class.isAssignableFrom(idv.getClass())) {
               uuids.add((UUID) idv.getDenotation());
            }
         }
      }

      return uuids;
   }

   public int getVersionCount() {
      List<? extends VersionDdo> extraVersions = getVersions();

      if (extraVersions == null) {
         return 1;
      }

      return extraVersions.size() + 1;
   }

   public final List<V> getVersions() {
      return versions;
   }

   //~--- set methods ---------------------------------------------------------

   public void setAdditionalIds(ObservableList<IdentifierDdo> additionalIds) {
      this.additionalIds = additionalIds;
   }

   public void setComponentNid(int componentNid) {
      this.componentNid = componentNid;
   }

   public void setPrimordialComponentUuid(UUID primordialComponentUuid) {
      this.primordialComponentUuid = primordialComponentUuid;
   }

   public void setRefexes(ObservableList<RefexChronicleDdo<?, ?>> annotations) {
      this.refexes = annotations;
   }

   public void setVersions(ObservableList<V> versions) {
      this.versions = versions;
   }
}
