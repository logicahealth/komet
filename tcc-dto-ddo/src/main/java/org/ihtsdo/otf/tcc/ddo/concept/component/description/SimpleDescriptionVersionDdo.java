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

/*AKF: Is there way to get only certain parts of a DDO back? 
I'm not sure how many times we will see a use case like this, 
but it seems better to not have make new DDO classes each time.
*/
    public SimpleDescriptionVersionDdo(DescriptionChronicleDdo chronicle, TerminologySnapshotDI ss,
            DescriptionVersionBI dv, ConceptVersionBI cv) throws IOException, ContradictionException {
        super(ss, dv);
        this.textProperty.set(dv.getText());
        this.languageProperty.set(dv.getLang());
        this.typeNid = dv.getTypeNid();
        this.dv = dv;
//AKF: changed from cv.getPreferredDescription().equals(dv) to account for more than one preferred term (e.g. US and GB)        
        if (cv.getDescriptionsPreferredActive().contains(dv)) {
            Collection<? extends RefexVersionBI<?>> usRefexMembers = dv.getRefexMembersActive(cv.getViewCoordinate(), Snomed.US_LANGUAGE_REFEX.getNid());
            Collection<? extends RefexVersionBI<?>> gbRefexMembers = dv.getRefexMembersActive(cv.getViewCoordinate(), Snomed.GB_LANGUAGE_REFEX.getNid());
//AKF: forgot that most of the time the PT is for both US and GB
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

//AKF: I would say "effectiveTime" to keep with the standard naming
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
