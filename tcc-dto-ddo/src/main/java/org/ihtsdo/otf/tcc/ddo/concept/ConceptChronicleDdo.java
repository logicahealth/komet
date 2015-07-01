package org.ihtsdo.otf.tcc.ddo.concept;

//~--- non-JDK imports --------------------------------------------------------
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptService;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.ochre.api.logic.LogicService;
import gov.vha.isaac.ochre.api.relationship.RelationshipVersionAdaptor;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.ddo.ComponentReference;
import org.ihtsdo.otf.tcc.ddo.concept.component.attribute.ConceptAttributesChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.description.DescriptionChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.description.DescriptionVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.relationship.RelationshipChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.relationship.RelationshipVersionDdo;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RefexPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RelationshipPolicy;

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
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexFactoryDdo;

/**
 * Property definition pattern from
 * https://wikis.oracle.com/display/OpenJDK/JavaFX+Property+Architecture using
 * "Basic Lazy With Default Value" example.
 *
 * @author kec
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement()
public class ConceptChronicleDdo implements Serializable {

    public static final String PADDING = "     ";
    public static final long serialVersionUID = 1;
    @XmlElementWrapper(name = "descriptionList")
    @XmlElement(name = "description")
    protected ObservableList<DescriptionChronicleDdo> _descriptions;
    @XmlElementWrapper(name = "destinationRelationshipList")
    @XmlElement(name = "destinationRelationship")
    protected ObservableList<RelationshipChronicleDdo> _destinationRelationships;
    @XmlElementWrapper(name = "originRelationshipList")
    @XmlElement(name = "originRelationship")
    protected ObservableList<RelationshipChronicleDdo> _originRelationships;
    @XmlElementWrapper(name = "refsetMemberList")
    @XmlElement(name = "refsetMember")
    protected ObservableList<RefexChronicleDdo<?, ?>> _refsetMembers;
    @XmlElement()
    protected ConceptAttributesChronicleDdo conceptAttributes;
    @XmlElement()
    protected ComponentReference conceptReference;
    @XmlTransient
    private SimpleObjectProperty<ObservableList<DescriptionChronicleDdo>> descriptions;
    @XmlTransient
    private SimpleObjectProperty<ObservableList<RelationshipChronicleDdo>> destinationRelationships;
    @XmlTransient
    private SimpleObjectProperty<ObservableList<RelationshipChronicleDdo>> originRelationships;
    @XmlElement()
    protected UUID primordialUuid;
    @XmlElement()
    protected UUID viewCoordinateUuid;
    @XmlElement()
    private RefexPolicy refexPolicy;
    @XmlTransient
    private SimpleObjectProperty<ObservableList<RefexChronicleDdo<?, ?>>> refsetMembers;
    @XmlElement()
    private RelationshipPolicy relationshipPolicy;

    public ConceptChronicleDdo() {
        super();
        _originRelationships
                = FXCollections.observableArrayList(new ArrayList<RelationshipChronicleDdo>(1));
        _destinationRelationships
                = FXCollections.observableArrayList(new ArrayList<RelationshipChronicleDdo>(1));
        _descriptions = FXCollections.observableArrayList(new ArrayList<DescriptionChronicleDdo>(1));
        _refsetMembers = FXCollections.observableArrayList(new ArrayList<RefexChronicleDdo<?, ?>>(0));
    }

    public ConceptChronicleDdo(TaxonomyCoordinate taxonomyCoordinate, ConceptChronology<?> concept,
            RefexPolicy refexPolicy, RelationshipPolicy relationshipPolicy) {
        try {
            this.refexPolicy = refexPolicy;
            this.relationshipPolicy = relationshipPolicy;
            this.viewCoordinateUuid = taxonomyCoordinate.getUuid();
            this.conceptReference = new ComponentReference(concept, taxonomyCoordinate);

            this.conceptAttributes
                    = new ConceptAttributesChronicleDdo(taxonomyCoordinate, this, concept);
            this.primordialUuid = conceptAttributes.getPrimordialComponentUuid();

            switch (relationshipPolicy) {
                case DESTINATION_RELATIONSHIPS:
                    _destinationRelationships
                            = FXCollections.observableArrayList(new ArrayList<RelationshipChronicleDdo>(0));
                    addDestinationRelationships(concept, taxonomyCoordinate);

                    break;

                case ORIGINATING_RELATIONSHIPS:
                    _originRelationships = FXCollections.observableArrayList(new ArrayList<RelationshipChronicleDdo>(0));
                    addOriginRelationships(concept, taxonomyCoordinate);

                    break;

                case ORIGINATING_AND_DESTINATION_RELATIONSHIPS:
                    addOriginRelationships(concept, taxonomyCoordinate);
                    addDestinationRelationships(concept, taxonomyCoordinate);

                    break;

                case ORIGINATING_AND_DESTINATION_TAXONOMY_RELATIONSHIPS:
                    addOriginTaxonomyRelationships(concept, taxonomyCoordinate);
                    addDestinationTaxonomyRelationships(concept, taxonomyCoordinate);

                    break;

                default:
                    throw new UnsupportedOperationException("Can't handle: " + relationshipPolicy);
            }

            _descriptions
                    = FXCollections.observableArrayList(new ArrayList<DescriptionChronicleDdo>(concept.getConceptDescriptionList().size()));

            for (SememeChronology<? extends DescriptionSememe> desc : concept.getConceptDescriptionList()) {
                DescriptionChronicleDdo dc = new DescriptionChronicleDdo(taxonomyCoordinate, this, desc);

                if (!dc.getVersions().isEmpty()) {
                    _descriptions.add(dc);
                }
            }

            if (((refexPolicy == RefexPolicy.ANNOTATION_MEMBERS_AND_REFSET_MEMBERS)
                    || (refexPolicy == RefexPolicy.REFEX_MEMBERS_AND_REFSET_MEMBERS))) {
                List<? extends SememeChronology<? extends SememeVersion>> members = concept.getSememeList();

                if (members != null) {
                    _refsetMembers = FXCollections.observableArrayList(new ArrayList<RefexChronicleDdo<?, ?>>(members.size()));

                    for (SememeChronology<? extends SememeVersion> m : members) {
                        Optional<RefexChronicleDdo<?, ?>> member = convertRefex(taxonomyCoordinate, m);

                        if (member.isPresent()) {
                            _refsetMembers.add(member.get());
                        } else {
                            throw new IOException("Could not convert refset member: " + m + "\nfrom refset: " + concept);
                        }
                    }
                }
            }
        } catch (ContradictionException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected final void addDestinationRelationships(ConceptChronology c, TaxonomyCoordinate taxonomyCoordinate)
            throws ContradictionException, IOException {
        Collection<SememeChronology<RelationshipVersionAdaptor>> relsIncoming = c.getRelationshipListWithConceptAsDestination();

        _destinationRelationships
                = FXCollections.observableArrayList(new ArrayList<RelationshipChronicleDdo>(relsIncoming.size()));

        for (SememeChronology<RelationshipVersionAdaptor> rel : relsIncoming) {
            if (rel != null) {
                RelationshipChronicleDdo fxc = new RelationshipChronicleDdo(taxonomyCoordinate, this, rel);

                if (!fxc.getVersions().isEmpty()) {
                    _destinationRelationships.add(fxc);
                }
            }
        }
    }

    protected final void addDestinationTaxonomyRelationships(ConceptChronology c, TaxonomyCoordinate taxonomyCoordinate)
            throws ContradictionException, IOException {
        List<SememeChronology<RelationshipVersionAdaptor>> relsIncoming = c.getRelationshipListWithConceptAsDestination();

        _destinationRelationships
                = FXCollections.observableArrayList(new ArrayList<RelationshipChronicleDdo>(relsIncoming.size()));

        int isaSequence = Snomed.IS_A.getSequence();
        for (SememeChronology<RelationshipVersionAdaptor> rel : relsIncoming) {
            Optional<LatestVersion<RelationshipVersionAdaptor>> optionalRelVersion = rel.getLatestVersion(RelationshipVersionAdaptor.class, taxonomyCoordinate.getStampCoordinate());
            if (optionalRelVersion.isPresent()) {
                LatestVersion<RelationshipVersionAdaptor> latestRelVersion = optionalRelVersion.get();
                if (latestRelVersion.value().getTypeSequence() == isaSequence) {
                    if (taxonomyCoordinate.getTaxonomyType() == latestRelVersion.value().getPremiseType()) {
                        ConceptChronology origin = Get.conceptService().getConcept(latestRelVersion.value().getOriginSequence());
                        ConceptChronicleDdo originDdo = new ConceptChronicleDdo(taxonomyCoordinate, origin, RefexPolicy.NONE, RelationshipPolicy.ORIGINATING_RELATIONSHIPS);
                        RelationshipChronicleDdo fxc = new RelationshipChronicleDdo(taxonomyCoordinate, originDdo, rel);
                        _destinationRelationships.add(fxc);
                    }
                }
            }
        }
    }

    protected final void addOriginRelationships(ConceptChronology c, TaxonomyCoordinate taxonomyCoordinate)
            throws ContradictionException, IOException {
        List<SememeChronology<RelationshipVersionAdaptor>> outgoingRels = c.getRelationshipListOriginatingFromConcept();
        _originRelationships = FXCollections.observableArrayList(
                new ArrayList<RelationshipChronicleDdo>(outgoingRels.size()));
        for (SememeChronology<? extends RelationshipVersionAdaptor> rel : outgoingRels) {
            RelationshipChronicleDdo fxc = new RelationshipChronicleDdo(taxonomyCoordinate, this, rel);
            if (!fxc.getVersions().isEmpty()) {
                _originRelationships.add(fxc);
            }
        }
    }

    protected final void addOriginTaxonomyRelationships(ConceptChronology c, TaxonomyCoordinate taxonomyCoordinate)
            throws ContradictionException, IOException {
        List<SememeChronology<RelationshipVersionAdaptor>> relsOutgoing = c.getRelationshipListOriginatingFromConcept();
        _originRelationships = FXCollections.observableArrayList(
                new ArrayList<RelationshipChronicleDdo>());

        int isaSequence = Snomed.IS_A.getSequence();

        for (SememeChronology<RelationshipVersionAdaptor> rel : relsOutgoing) {
            Optional<LatestVersion<RelationshipVersionAdaptor>> optionalRelVersion = rel.getLatestVersion(RelationshipVersionAdaptor.class, taxonomyCoordinate.getStampCoordinate());
            if (optionalRelVersion.isPresent()) {
                LatestVersion<RelationshipVersionAdaptor> latestRelVersion = optionalRelVersion.get();
                if (latestRelVersion.value().getTypeSequence() == isaSequence) {
                    if (taxonomyCoordinate.getTaxonomyType() == latestRelVersion.value().getPremiseType()) {
                        RelationshipChronicleDdo fxc = new RelationshipChronicleDdo(taxonomyCoordinate, this, rel);
                        _originRelationships.add(fxc);
                    }
                }
            }

        }
    }

    private Optional<RefexChronicleDdo<?, ?>> convertRefex(TaxonomyCoordinate taxonomyCoordinate, SememeChronology<?> m)
            throws IOException, ContradictionException {
        return convertRefex(taxonomyCoordinate, this, m);
    }

    public static Optional<RefexChronicleDdo<?, ?>> convertRefex(TaxonomyCoordinate taxonomyCoordinate, ConceptChronicleDdo concept,
            SememeChronology<?> m)
            throws IOException, ContradictionException {
        return RefexFactoryDdo.make(taxonomyCoordinate, concept, m);
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
     * Compares this object to the specified object. The result is {@code true}
     * if and only if the argument is not {@code null}, is a {@code EConcept}
     * object, and contains the same values, field by field, as this
     * {@code EConcept}.
     *
     * @param obj the object to compare with.
     * @return {@code true} if the objects are the same; {@code false}
     * otherwise.
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
     * Returns a hash code for this {@code EConcept}.
     *
     * @return a hash code value for this {@code EConcept}.
     */
    @Override
    public int hashCode() {
        return this.conceptAttributes.getPrimordialComponentUuid().hashCode();
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

        if (!getDescriptions().isEmpty() && !getDescriptions().get(0).getVersions().isEmpty()) {
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

    public void setViewCoordinateUuid(UUID viewCoordinateUuid) {
        this.viewCoordinateUuid = viewCoordinateUuid;
    }
}
