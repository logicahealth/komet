package org.ihtsdo.otf.tcc.ddo.concept;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.api.relationship.RelAssertionType;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.media.MediaChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.ddo.ComponentReference;
import org.ihtsdo.otf.tcc.ddo.concept.component.attribute.ConceptAttributesChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.description.DescriptionChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.description.DescriptionVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.media.MediaChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.relationship.RelationshipChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.relationship.RelationshipVersionDdo;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RefexPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RelationshipPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.VersionPolicy;

//~--- JDK imports ------------------------------------------------------------


import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;

import java.util.*;
import javax.xml.bind.JAXB;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexFactoryDdo;

/**
 * Property definition pattern from
 * https://wikis.oracle.com/display/OpenJDK/JavaFX+Property+Architecture
 * using "Basic Lazy With Default Value" example.
 * @author kec
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement()
public class ConceptChronicleDdo implements Serializable {
   public static final String                                            PADDING          = "     ";
   public static final long                                              serialVersionUID = 1;
   @XmlElementWrapper(name = "descriptionList")
   @XmlElement(name = "description")
   protected ObservableList<DescriptionChronicleDdo>                      _descriptions;
   @XmlElementWrapper(name = "destinationRelationshipList")
   @XmlElement(name = "destinationRelationship")
   protected ObservableList<RelationshipChronicleDdo>                     _destinationRelationships;
   @XmlElementWrapper(name = "mediaList")
   @XmlElement(name = "media")
   protected ObservableList<MediaChronicleDdo>                            _media;
   @XmlElementWrapper(name = "originRelationshipList")
   @XmlElement(name = "originRelationship")
   protected ObservableList<RelationshipChronicleDdo>                     _originRelationships;
   @XmlElementWrapper(name = "refsetMemberList")
   @XmlElement(name = "refsetMember")
   protected ObservableList<RefexChronicleDdo<?, ?>>                      _refsetMembers;
   @XmlElement()
   protected ConceptAttributesChronicleDdo                                conceptAttributes;
   @XmlElement()
   protected ComponentReference                                        conceptReference;
   @XmlTransient
   private SimpleObjectProperty<ObservableList<DescriptionChronicleDdo>>  descriptions;
   @XmlTransient
   private SimpleObjectProperty<ObservableList<RelationshipChronicleDdo>> destinationRelationships;
   @XmlTransient
   private SimpleObjectProperty<ObservableList<MediaChronicleDdo>>        media;
   @XmlTransient
   private SimpleObjectProperty<ObservableList<RelationshipChronicleDdo>> originRelationships;
   @XmlElement()
   protected UUID                                                        primordialUuid;
   @XmlElement()
   protected UUID                                                        viewCoordinateUuid;
   @XmlElement()
   private RefexPolicy                                                   refexPolicy;
   @XmlTransient
   private SimpleObjectProperty<ObservableList<RefexChronicleDdo<?, ?>>>  refsetMembers;
   @XmlElement()
   private RelationshipPolicy                                            relationshipPolicy;
   @XmlElement()
   private VersionPolicy                                                 versionPolicy;

   public ConceptChronicleDdo() {
      super();
      _originRelationships      =
         FXCollections.observableArrayList(new ArrayList<RelationshipChronicleDdo>(1));
      _destinationRelationships =
         FXCollections.observableArrayList(new ArrayList<RelationshipChronicleDdo>(1));
      _descriptions  = FXCollections.observableArrayList(new ArrayList<DescriptionChronicleDdo>(1));
      _media         = FXCollections.observableArrayList(new ArrayList<MediaChronicleDdo>(1));
      _refsetMembers = FXCollections.observableArrayList(new ArrayList<RefexChronicleDdo<?, ?>>(0));
   }

   public ConceptChronicleDdo(TerminologySnapshotDI ss, ConceptChronicleBI c, VersionPolicy versionPolicy,
                    RefexPolicy refexPolicy, RelationshipPolicy relationshipPolicy)
           throws IOException, ContradictionException {
      this.versionPolicy      = versionPolicy;
      this.refexPolicy        = refexPolicy;
      this.relationshipPolicy = relationshipPolicy;
      this.viewCoordinateUuid = ss.getViewCoordinate().getVcUuid();
      this.conceptReference   = new ComponentReference(c.getPrimordialUuid(), c.getNid(),
          ss.getConceptForNid(c.getNid()).getPreferredDescription().getText());
      this.conceptAttributes = new ConceptAttributesChronicleDdo(ss, this, c.getConceptAttributes());
      this.primordialUuid    = conceptAttributes.getPrimordialComponentUuid();

      switch (relationshipPolicy) {
      case DESTINATION_RELATIONSHIPS :
         _destinationRelationships =
            FXCollections.observableArrayList(new ArrayList<RelationshipChronicleDdo>(0));
         addDestinationRelationships(c, ss);

         break;

      case ORIGINATING_RELATIONSHIPS :
         _originRelationships = FXCollections.observableArrayList(new ArrayList<RelationshipChronicleDdo>(0));
         addOriginRelationships(c, ss);

         break;

      case ORIGINATING_AND_DESTINATION_RELATIONSHIPS :
         addOriginRelationships(c, ss);
         addDestinationRelationships(c, ss);

         break;

      case ORIGINATING_AND_DESTINATION_TAXONOMY_RELATIONSHIPS :
         addOriginTaxonomyRelationships(c, ss);
         addDestinationTaxonomyRelationships(c, ss);

         break;

      default :
         throw new UnsupportedOperationException("Can't handle: " + relationshipPolicy);
      }

      _descriptions =
         FXCollections.observableArrayList(new ArrayList<DescriptionChronicleDdo>(c.getDescriptions().size()));

      for (DescriptionChronicleBI desc : c.getDescriptions()) {
         DescriptionChronicleDdo dc = new DescriptionChronicleDdo(ss, this, desc);

         if (!dc.getVersions().isEmpty()) {
            _descriptions.add(dc);
         }
      }

      _media = FXCollections.observableArrayList(new ArrayList<MediaChronicleDdo>(c.getMedia().size()));

      for (MediaChronicleBI mediaChronicle : c.getMedia()) {
         MediaChronicleDdo tkMedia = new MediaChronicleDdo(ss, this, mediaChronicle);

         if (!tkMedia.getVersions().isEmpty()) {
            _media.add(tkMedia);
         }
      }

      if (((refexPolicy == RefexPolicy.ANNOTATION_MEMBERS_AND_REFSET_MEMBERS)
          || (refexPolicy == RefexPolicy.REFEX_MEMBERS_AND_REFSET_MEMBERS)) &&!c.isAnnotationStyleRefex()) {
         Collection<? extends RefexChronicleBI> members = c.getRefsetMembers();

         if (members != null) {
            _refsetMembers = FXCollections.observableArrayList(new ArrayList<RefexChronicleDdo<?,
                ?>>(members.size()));

            for (RefexChronicleBI m : members) {
               RefexChronicleDdo<?, ?> member = convertRefex(ss, m);

               if ((member != null) &&!member.getVersions().isEmpty()) {
                  _refsetMembers.add(member);
               } else {
                  throw new IOException("Could not convert refset member: " + m + "\nfrom refset: " + c);
               }
            }
         }
      }
   }

   protected final void addDestinationRelationships(ConceptChronicleBI c, TerminologySnapshotDI ss)
           throws ContradictionException, IOException {
      Collection<? extends RelationshipChronicleBI> relsIncoming = c.getRelationshipsIncoming();

      _destinationRelationships =
         FXCollections.observableArrayList(new ArrayList<RelationshipChronicleDdo>(relsIncoming.size()));

      for (RelationshipChronicleBI rel : relsIncoming) {
         RelationshipChronicleDdo fxc = new RelationshipChronicleDdo(ss, this, rel);

         if (!fxc.getVersions().isEmpty()) {
            _destinationRelationships.add(fxc);
         }
      }
   }

   protected final void addDestinationTaxonomyRelationships(ConceptChronicleBI c, TerminologySnapshotDI ss)
           throws ContradictionException, IOException {
      Collection<? extends RelationshipChronicleBI> relsIncoming = c.getRelationshipsIncoming();

      _destinationRelationships =
         FXCollections.observableArrayList(new ArrayList<RelationshipChronicleDdo>(relsIncoming.size()));

      int isaNid = ss.getViewCoordinate().getIsaNid();

NEXT_REL:
      for (RelationshipChronicleBI rel : relsIncoming) {
         RelationshipVersionBI relVersion = rel.getPrimordialVersion();

         switch (ss.getViewCoordinate().getRelationshipAssertionType()) {
         case INFERRED :
            if (!relVersion.isInferred()) {
               continue NEXT_REL;
            }

            break;

         case STATED :
            if (!relVersion.isStated()) {
               continue NEXT_REL;
            }

            break;

         case INFERRED_THEN_STATED :
            if (!relVersion.isInferred()) {
               continue NEXT_REL;
            }

            break;
         }

         boolean foundType = false;

         for (RelationshipVersionBI rv : rel.getVersions(ss.getViewCoordinate())) {
            if (isaNid == rv.getTypeNid()) {
               foundType = true;

               break;
            }
         }

         if (!foundType) {
            continue NEXT_REL;
         }

         RelationshipChronicleDdo fxc = new RelationshipChronicleDdo(ss, this, rel);

         _destinationRelationships.add(fxc);
      }

      if (_destinationRelationships.isEmpty()
          && (ss.getViewCoordinate().getRelationshipAssertionType()
              == RelAssertionType.INFERRED_THEN_STATED)) {
         for (RelationshipChronicleBI rel : relsIncoming) {
            RelationshipVersionBI relVersion = rel.getPrimordialVersion();

            if (relVersion.isStated()) {
               boolean foundType = false;

               for (RelationshipVersionBI rv : rel.getVersions(ss.getViewCoordinate())) {
                  if (isaNid != rv.getTypeNid()) {
                     foundType = true;

                     break;
                  }
               }

               if (foundType) {
                  RelationshipChronicleDdo fxc = new RelationshipChronicleDdo(ss, this, rel);

                  _destinationRelationships.add(fxc);
               }
            }
         }
      }
   }

   protected final void addOriginRelationships(ConceptChronicleBI c, TerminologySnapshotDI ss)
           throws ContradictionException, IOException {
      _originRelationships = FXCollections.observableArrayList(
         new ArrayList<RelationshipChronicleDdo>(c.getRelationshipsOutgoing().size()));

      for (RelationshipChronicleBI rel : c.getRelationshipsOutgoing()) {
         RelationshipChronicleDdo fxc = new RelationshipChronicleDdo(ss, this, rel);

         if (!fxc.getVersions().isEmpty()) {
            _originRelationships.add(fxc);
         }
      }
   }

   protected final void addOriginTaxonomyRelationships(ConceptChronicleBI c, TerminologySnapshotDI ss)
           throws ContradictionException, IOException {
      _originRelationships = FXCollections.observableArrayList(
         new ArrayList<RelationshipChronicleDdo>(c.getRelationshipsOutgoing().size()));

      int isaNid = ss.getViewCoordinate().getIsaNid();

      for (RelationshipChronicleBI rel : c.getRelationshipsOutgoing()) {
         RelationshipChronicleDdo          fxc      = new RelationshipChronicleDdo(ss, this, rel);
         ArrayList<RelationshipVersionDdo> toRemove = new ArrayList<>();

         for (RelationshipVersionDdo fxv : fxc.getVersions()) {
            if (isaNid != fxv.getTypeReference().getNid()) {
               toRemove.add(fxv);

               break;
            }

            switch (ss.getViewCoordinate().getRelationshipAssertionType()) {
            case INFERRED :
               if (!(fxv.getAuthorReference().getNid() == ss.getViewCoordinate().getClassifierNid())) {
                  toRemove.add(fxv);
               }

               break;

            case STATED :
               if (fxv.getAuthorReference().getNid() == ss.getViewCoordinate().getClassifierNid()) {
                  toRemove.add(fxv);
               }

               break;
            }
         }

         fxc.getVersions().removeAll(toRemove);

         if (!fxc.getVersions().isEmpty()) {
            _originRelationships.add(fxc);
         }
      }
   }

   private RefexChronicleDdo<?, ?> convertRefex(TerminologySnapshotDI ss, RefexChronicleBI<?> m)
           throws IOException, ContradictionException {
      return convertRefex(ss, this, m);
   }

   public static RefexChronicleDdo<?, ?> convertRefex(TerminologySnapshotDI ss, ConceptChronicleDdo concept,
       RefexChronicleBI<?> m)
           throws IOException, ContradictionException {
      return RefexFactoryDdo.make(ss, concept, m);
   }

   public ObjectProperty<ObservableList<DescriptionChronicleDdo>> descriptions() {
      if (descriptions == null) {
         descriptions = new SimpleObjectProperty<>(_descriptions);
      }

      return descriptions;
   }

   public SimpleObjectProperty<ObservableList<RelationshipChronicleDdo>> destinationRelationships() {
      if (destinationRelationships == null) {
         destinationRelationships = new SimpleObjectProperty<>(_destinationRelationships);
      }

      return destinationRelationships;
   }

   /**
    * Compares this object to the specified object. The result is <tt>true</tt> if and only if the argument
    * is not <tt>null</tt>, is a <tt>EConcept</tt> object, and contains the same values, field by field, as
    * this <tt>EConcept</tt>.
    *
    * @param obj the object to compare with.
    * @return
    * <code>true</code> if the objects are the same;
    * <code>false</code> otherwise.
    */
   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (ConceptChronicleDdo.class.isAssignableFrom(obj.getClass())) {
         ConceptChronicleDdo another = (ConceptChronicleDdo) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare ConceptAttributes
         if (this.conceptAttributes == null) {
            if (this.conceptAttributes != another.conceptAttributes) {
               return false;
            }
         } else if (!this.conceptAttributes.equals(another.conceptAttributes)) {
            return false;
         }

         // Compare Descriptions
         if (this._descriptions == null) {
            if (another._descriptions == null) {                    // Equal!
            } else if (another._descriptions.isEmpty()) {           // Equal!
            } else {
               return false;
            }
         } else if (!this._descriptions.equals(another._descriptions)) {
            return false;
         }

         // Compare Relationships
         if (this._originRelationships == null) {
            if (another._originRelationships == null) {             // Equal!
            } else if (another._originRelationships.isEmpty()) {    // Equal!
            } else {
               return false;
            }
         } else if (!this._originRelationships.equals(another._originRelationships)) {
            return false;
         }

         // Compare Images
         if (this._media == null) {
            if (another._media == null) {                           // Equal!
            } else if (another._media.isEmpty()) {                  // Equal!
            } else {
               return false;
            }
         } else if (!this._media.equals(another._media)) {
            return false;
         }

         // Compare Refset Members
         if (this._refsetMembers == null) {
            if (another._refsetMembers == null) {                   // Equal!
            } else if (another._refsetMembers.isEmpty()) {          // Equal!
            } else {
               return false;
            }
         } else if (!this._refsetMembers.equals(another._refsetMembers)) {
            return false;
         }

         // If none of the previous comparisons fail, the objects must be equal
         return true;
      }

      return false;
   }

   /**
    * Returns a hash code for this
    * <code>EConcept</code>.
    *
    * @return a hash code value for this <tt>EConcept</tt>.
    */
   @Override
   public int hashCode() {
      return this.conceptAttributes.getPrimordialComponentUuid().hashCode();
   }

   public ObjectProperty<ObservableList<MediaChronicleDdo>> media() {
      if (media == null) {
         media = new SimpleObjectProperty<>(_media);
      }

      return media;
   }

   public SimpleObjectProperty<ObservableList<RelationshipChronicleDdo>> originRelationships() {
      if (originRelationships == null) {
         originRelationships = new SimpleObjectProperty<>(_originRelationships);
      }

      return originRelationships;
   }

   public ObjectProperty<ObservableList<RefexChronicleDdo<?, ?>>> refsetMembers() {
      if (refsetMembers == null) {
         refsetMembers = new SimpleObjectProperty<>(_refsetMembers);
      }

      return refsetMembers;
   }

   public String toHtml() {
      StringBuilder sb = new StringBuilder();

      sb.append("<html>");
      sb.append("<head>");
      sb.append("<title>");
      sb.append(primordialUuid.toString());
      sb.append(" ");

      if (!getDescriptions().isEmpty() &&!getDescriptions().get(0).getVersions().isEmpty()) {
         sb.append(getDescriptions().get(0).getVersions().get(0).getText());
      }

      sb.append("</title>");
      sb.append("</head>");
      sb.append("<body>");
      sb.append(getHtmlFragment());
      sb.append("</body>");
      sb.append("</html>");

      return sb.toString();
   }

   /**
    * Returns a string representation of the object.
    */
   @Override
   public String toString() {
      return this.conceptReference.getText();
   }

   public String toXml() {
       StringWriter sw = new StringWriter();
       JAXB.marshal(this, sw);
       return sw.toString();
   }
   public ConceptAttributesChronicleDdo getConceptAttributes() {
      return conceptAttributes;
   }

   public ComponentReference getConceptReference() {
      return conceptReference;
   }

   private void getDescriptionTable(StringBuilder sb) {
      sb.append("<table>");
      sb.append("<tr>");
      sb.append("<th colspan=2 align=left>descriptions:</th>");
      sb.append("</tr>");
      sb.append("<tr>");
      sb.append("<th align=left>text:</th>");
      sb.append("<th align=left>type:</th>");
      sb.append("</tr>");

      for (DescriptionChronicleDdo fxdc : getDescriptions()) {
         for (DescriptionVersionDdo fxdv : fxdc.getVersions()) {
            sb.append("<tr>");
            sb.append("<td>");
            sb.append(fxdv.getText());
            sb.append("</td>");
            sb.append("<td>");
            sb.append(fxdv.getTypeReference().getHtmlFragment());
            sb.append("</td>");
            sb.append("</tr>");
         }
      }

      sb.append("</table>");
   }

   public ObservableList<DescriptionChronicleDdo> getDescriptions() {
      if (descriptions != null) {
         return descriptions.getValue();
      }

      if (_descriptions == null) {
         _descriptions = FXCollections.emptyObservableList();
      }

      return _descriptions;
   }

   private void getDestinationRelationshipTable(StringBuilder sb) {
      sb.append("<table>");
      sb.append("<tr>");
      sb.append("<th colspan=2 align=left>destination relationships:</th>");
      sb.append("</tr>");
      sb.append("<tr>");
      sb.append("<th align=left>concept:</th>");
      sb.append("<th align=left>type:</th>");
      sb.append("</tr>");

      for (RelationshipChronicleDdo fxrc : getDestinationRelationships()) {
         for (RelationshipVersionDdo fxrv : fxrc.getVersions()) {
            sb.append("<tr>");
            sb.append("<td>");
            sb.append(fxrv.getOriginReference().getHtmlFragment());
            sb.append("</td>");
            sb.append("<td>");
            sb.append(fxrv.getTypeReference().getHtmlFragment());
            sb.append("</td>");
            sb.append("</tr>");
         }
      }

      sb.append("</table>");
   }

   public ObservableList<RelationshipChronicleDdo> getDestinationRelationships() {
      if (destinationRelationships != null) {
         return destinationRelationships.get();
      }

      if (_destinationRelationships == null) {
         _destinationRelationships = FXCollections.emptyObservableList();
      }

      return _destinationRelationships;
   }

   public String getHtmlFragment() {
      StringBuilder sb = new StringBuilder();

      getDescriptionTable(sb);
      getOriginRelationshipTable(sb);
      getDestinationRelationshipTable(sb);

      return sb.toString();
   }

   public ObservableList<MediaChronicleDdo> getMedia() {
      if (media != null) {
         return media.get();
      }

      if (_media == null) {
         _media = FXCollections.emptyObservableList();
      }

      return _media;
   }

   private void getOriginRelationshipTable(StringBuilder sb) {
      sb.append("<table>");
      sb.append("<tr>");
      sb.append("<th colspan=2 align=left>origin relationships:</th>");
      sb.append("</tr>");
      sb.append("<tr>");
      sb.append("<th align=left>type:</th>");
      sb.append("<th align=left>concept:</th>");
      sb.append("</tr>");

      for (RelationshipChronicleDdo fxrc : getOriginRelationships()) {
         for (RelationshipVersionDdo fxrv : fxrc.getVersions()) {
            sb.append("<tr>");
            sb.append("<td>");
            sb.append(fxrv.getTypeReference().getHtmlFragment());
            sb.append("</td>");
            sb.append("<td>");
            sb.append(fxrv.getDestinationReference().getHtmlFragment());
            sb.append("</td>");
            sb.append("</tr>");
         }
      }

      sb.append("</table>");
   }

   public ObservableList<RelationshipChronicleDdo> getOriginRelationships() {
      if (originRelationships != null) {
         return originRelationships.get();
      }

      if (_originRelationships == null) {
         _originRelationships = FXCollections.emptyObservableList();
      }

      return _originRelationships;
   }

   public UUID getPrimordialUuid() {
      return primordialUuid;
   }

   public RefexPolicy getRefexPolicy() {
      return refexPolicy;
   }

   public ObservableList<RefexChronicleDdo<?, ?>> getRefsetMembers() {
      if (refsetMembers != null) {
         return refsetMembers.get();
      }

      if (_refsetMembers == null) {
         _refsetMembers = FXCollections.emptyObservableList();
      }

      return _refsetMembers;
   }

   public RelationshipPolicy getRelationshipPolicy() {
      return relationshipPolicy;
   }

   public VersionPolicy getVersionPolicy() {
      return versionPolicy;
   }

   public UUID getViewCoordinateUuid() {
      return viewCoordinateUuid;
   }

   public void setConceptAttributes(ConceptAttributesChronicleDdo conceptAttributes) {
      this.conceptAttributes = conceptAttributes;
   }

   public void setConceptReference(ComponentReference conceptReference) {
      this.conceptReference = conceptReference;
   }

   public void setDescriptions(List<DescriptionChronicleDdo> descriptions) {
      if (this.descriptions != null) {
         this.descriptions.setValue(FXCollections.observableArrayList(descriptions));
      } else {
         this._descriptions = FXCollections.observableArrayList(descriptions);
      }
   }

   public void setDestinationRelationships(ObservableList<RelationshipChronicleDdo> destinationRelationships) {
      if (this.destinationRelationships != null) {
         this.destinationRelationships.setValue(FXCollections.observableArrayList(destinationRelationships));
      } else {
         this._destinationRelationships = FXCollections.observableArrayList(destinationRelationships);
      }
   }

   public void setMedia(ObservableList<MediaChronicleDdo> media) {
      if (this.media != null) {
         this.media.setValue(FXCollections.observableArrayList(media));
      } else {
         this._media = FXCollections.observableArrayList(media);
      }
   }

   public void setOriginRelationships(List<RelationshipChronicleDdo> relationships) {
      if (this.originRelationships != null) {
         this.originRelationships.setValue(FXCollections.observableArrayList(relationships));
      } else {
         this._originRelationships = FXCollections.observableArrayList(relationships);
      }
   }

   public void setPrimordialUuid(UUID primordialUuid) {
      this.primordialUuid = primordialUuid;
   }

   public void setRefexPolicy(RefexPolicy refexPolicy) {
      this.refexPolicy = refexPolicy;
   }

   public void setRefsetMembers(List<RefexChronicleDdo<?, ?>> refsetMembers) {
      if (this.refsetMembers != null) {
         this.refsetMembers.setValue(FXCollections.observableArrayList(refsetMembers));
      } else {
         this._refsetMembers = FXCollections.observableArrayList(refsetMembers);
      }
   }

   public void setRelationshipPolicy(RelationshipPolicy relationshipPolicy) {
      this.relationshipPolicy = relationshipPolicy;
   }

   public void setVersionPolicy(VersionPolicy versionPolicy) {
      this.versionPolicy = versionPolicy;
   }

   public void setViewCoordinateUuid(UUID viewCoordinateUuid) {
      this.viewCoordinateUuid = viewCoordinateUuid;
   }
}
