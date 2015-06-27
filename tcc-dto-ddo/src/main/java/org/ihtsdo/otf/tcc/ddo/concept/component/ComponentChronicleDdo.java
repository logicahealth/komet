package org.ihtsdo.otf.tcc.ddo.concept.component;

//~--- non-JDK imports --------------------------------------------------------
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.chronicle.StampedVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexChronicleDdo;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;

//~--- JDK imports ------------------------------------------------------------
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

public abstract class ComponentChronicleDdo<V extends ComponentVersionDdo, T extends StampedVersion>
        implements Serializable {

    private static final long serialVersionUID = 1;
    private static final Logger log = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------
    @XmlElementWrapper(name = "annotationList")
    @XmlElement(name = "annotation")
    public ObservableList<RefexChronicleDdo<?, ?>> refexes
            = FXCollections.observableArrayList(new ArrayList<RefexChronicleDdo<?, ?>>(0));
    private int componentNid;
    @XmlTransient
    protected ConceptChronicleDdo concept;
    private UUID primordialComponentUuid;
    @XmlElementWrapper(name = "versionList")
    @XmlElement()
    private ObservableList<V> versions;

   //~--- constructors --------------------------------------------------------
    public ComponentChronicleDdo() {
        super();
        this.versions = FXCollections.observableArrayList(new ArrayList<V>(1));
    }

    public ComponentChronicleDdo(TaxonomyCoordinate taxonomyCoordinate, ConceptChronicleDdo concept, ObjectChronology<? extends T> another)
            throws IOException, ContradictionException {
        super();
        this.concept = concept;
        this.primordialComponentUuid = another.getPrimordialUuid();
        this.componentNid = another.getNid();

        processRefexes(taxonomyCoordinate, another);
        this.versions = FXCollections.observableArrayList(new ArrayList<V>(another.getVersions().size()));
        for (T v : another.getVersionList()) {
            this.versions.add(makeVersion(taxonomyCoordinate, v));
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
     * Compares this object to the specified object. The result is {@code true}
     * if and only if the argument is not {@code null}, is a {@code EComponent}
     * object, and contains the same values, field by field, as this
     * {@code EComponent}.
     *
     * @param obj the object to compare with.
     * @return {@code true} if the objects are the same; {@code false}
     * otherwise.
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
     * Returns a hash code for this {@code EComponent}.
     *
     * @return a hash code value for this {@code EComponent}.
     */
    @Override
    public final int hashCode() {
        return this.primordialComponentUuid.hashCode();
    }

    protected abstract V makeVersion(TaxonomyCoordinate taxonomyCoordinate, T version)
            throws IOException, ContradictionException;

    private void processRefexes(TaxonomyCoordinate taxonomyCoordinate, ObjectChronology<? extends T> another)
            throws IOException, ContradictionException {
        HashSet<SememeChronology<? extends SememeVersion>> sememesToProcess = new HashSet<>();

        switch (getConcept().getRefexPolicy()) {
            case REFEX_MEMBERS:
            case REFEX_MEMBERS_AND_REFSET_MEMBERS:
            case ANNOTATION_MEMBERS:
            case ANNOTATION_MEMBERS_AND_REFSET_MEMBERS:
                sememesToProcess.addAll(another.getSememeList());
                break;

            case NONE:
                //noop
                break;
            default:
                log.error("Unhandled case in process Refexes: " + getConcept().getRefexPolicy());
                break;
        }

        for (SememeChronology<?> r : sememesToProcess) {
            Optional<RefexChronicleDdo<?, ?>> optionalFxRefexMember
                    = ConceptChronicleDdo.convertRefex(taxonomyCoordinate, concept, r);
            if (optionalFxRefexMember.isPresent()) {
                RefexChronicleDdo<?, ?> fxRefexMember = optionalFxRefexMember.get();
                if (!fxRefexMember.getVersions().isEmpty()) {
                    this.refexes.add(fxRefexMember);
                }

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


    public int getComponentNid() {
        return componentNid;
    }

    public ConceptChronicleDdo getConcept() {
        return concept;
    }

    public int getIdCount() {
            return 1;
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
