package org.ihtsdo.otf.tcc.ddo.concept.component.description;

//~--- non-JDK imports --------------------------------------------------------
import java.io.IOException;
import java.util.Collection;
import javafx.beans.property.SimpleStringProperty;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

//~--- JDK imports ------------------------------------------------------------
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.ddo.TimeReference;
import org.ihtsdo.otf.tcc.ddo.concept.component.SimpleVersionDdo;

/**
 *
 * @author dylangrald
 */
@XmlRootElement
public class SimpleDescriptionVersionDdo extends SimpleVersionDdo {

    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    protected SimpleStringProperty textProperty = new SimpleStringProperty(this, "text");
    protected SimpleStringProperty languageProperty = new SimpleStringProperty(this, "language");
    protected SimpleStringProperty timeProperty = new SimpleStringProperty(this, "");
    protected SimpleStringProperty typeProperty = new SimpleStringProperty(this, "");
    @XmlTransient
    protected int typeNid;
    @XmlTransient
    protected ConceptVersionBI cv;
    @XmlTransient
    protected DescriptionVersionBI dv;
//~--- constructors --------------------------------------------------------

    public SimpleDescriptionVersionDdo() {
        super();
    }

    public SimpleDescriptionVersionDdo(DescriptionChronicleDdo chronicle, TerminologySnapshotDI ss,
            DescriptionVersionBI dv, ConceptVersionBI cv) throws IOException, ContradictionException {
        super(ss, dv);
        this.textProperty.set(dv.getText());
        this.languageProperty.set(dv.getLang());
        this.typeNid = dv.getTypeNid();
        this.dv = dv;
        if (cv.getDescriptionsPreferredActive().contains(dv)) {
            Collection<? extends RefexVersionBI<?>> usRefexMembers = dv.getRefexMembersActive(cv.getViewCoordinate(), Snomed.US_LANGUAGE_REFEX.getNid());
            Collection<? extends RefexVersionBI<?>> gbRefexMembers = dv.getRefexMembersActive(cv.getViewCoordinate(), Snomed.GB_LANGUAGE_REFEX.getNid());
            if (!usRefexMembers.isEmpty() && gbRefexMembers.isEmpty()) {
                this.typeProperty.set("Synonym pt:EN-US");
            } else if (!gbRefexMembers.isEmpty() && usRefexMembers.isEmpty()) {
                    this.typeProperty.set("Synonym pt:EN-GB");
            }else{
                this.typeProperty.set("Synonym pt:EN-US,EN-GB");
            }
        } else if (this.typeNid == Snomed.FULLY_SPECIFIED_DESCRIPTION_TYPE.getNid()) {
            this.typeProperty.set("Fully specified name");
        } else if (this.typeNid == Snomed.SYNONYM_DESCRIPTION_TYPE.getNid()) {
            this.typeProperty.set("Synonym");
        } else if (this.typeNid == Snomed.DEFINITION_DESCRIPTION_TYPE.getNid()) {
            this.typeProperty.set("Definition");
        }

        TimeReference tr = new TimeReference(dv.getTime());
        this.timeProperty.set(tr.getTimeText());

    }

    @XmlAttribute(name = "text")
    public String getTextProperty() {
        return textProperty.get();
    }

    @XmlElement(name = "language")
    public String getLanguageProperty() {
        return languageProperty.get();
    }

    @XmlElement(name = "dateCreated")
    public String getTimeProperty() {
        return timeProperty.get();
    }

    @XmlElement(name = "descriptionType")
    public String getTypeProperty() {
        return this.typeProperty.get();
    }

    public void setTextProperty(String text) {
        this.textProperty.set(text);
    }

    public void setLanguageProperty(String text) {
        this.languageProperty.set(text);
    }

    public void setTimeProperty(String time) {
        this.timeProperty.set(time);
    }

}
