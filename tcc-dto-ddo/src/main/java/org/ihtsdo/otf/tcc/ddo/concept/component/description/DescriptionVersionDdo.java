package org.ihtsdo.otf.tcc.ddo.concept.component.description;

//~--- non-JDK imports --------------------------------------------------------
import gov.vha.isaac.ochre.api.LanguageCoordinateService;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

import org.ihtsdo.otf.tcc.ddo.ComponentReference;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import javax.xml.bind.annotation.XmlRootElement;
import org.ihtsdo.otf.tcc.ddo.concept.component.TypedComponentVersionDdo;

@XmlRootElement
public class DescriptionVersionDdo extends TypedComponentVersionDdo<DescriptionChronicleDdo, DescriptionVersionDdo> {

    public static final long serialVersionUID = 1;
    
    private static LanguageCoordinateService languageCoordinateService = 
            LookupService.getService(LanguageCoordinateService.class);
    //~--- fields --------------------------------------------------------------
    protected SimpleBooleanProperty initialCaseSignificantProperty = new SimpleBooleanProperty(this,
            "initialCaseSignificant");
    protected SimpleStringProperty languageProperty = new SimpleStringProperty(this, "language");
    protected SimpleStringProperty textProperty = new SimpleStringProperty(this, "text");

    //~--- constructors --------------------------------------------------------
    public DescriptionVersionDdo() {
        super();
    }

    public DescriptionVersionDdo(DescriptionChronicleDdo chronicle, TaxonomyCoordinate ss,
            DescriptionSememe another)
            throws IOException, ContradictionException {
        super(chronicle, ss, another);
        this.initialCaseSignificantProperty.set(languageCoordinateService.conceptIdToCaseSignificance(another.getCaseSignificanceConceptSequence()));
        this.languageProperty.set(languageCoordinateService.conceptIdToIso639(another.getLanguageConceptSequence()));
        this.textProperty.set(another.getText());
        this.typeReferenceProperty.set(new ComponentReference(another.getDescriptionTypeConceptSequence(), 
                ss.getStampCoordinate(), ss.getLanguageCoordinate()));
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is {@code true}
     * if and only if the argument is not {@code null}, is a
     * {@code EDescriptionVersion} object, and contains the same values, field
     * by field, as this {@code EDescriptionVersion}.
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

        if (DescriptionVersionDdo.class.isAssignableFrom(obj.getClass())) {
            DescriptionVersionDdo another = (DescriptionVersionDdo) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare initialCaseSignificantProperty
            if (this.initialCaseSignificantProperty != another.initialCaseSignificantProperty) {
                return false;
            }

            // Compare languageProperty
            if (!this.languageProperty.equals(another.languageProperty)) {
                return false;
            }

            // Compare textProperty
            if (!this.textProperty.equals(another.textProperty)) {
                return false;
            }

            // Compare typeUuid
            if (!this.typeReferenceProperty.equals(another.typeReferenceProperty)) {
                return false;
            }

            // Compare their parents
            return super.equals(obj);
        }

        return false;
    }

    public SimpleBooleanProperty initialCaseSignificantProperty() {
        return initialCaseSignificantProperty;
    }

    public SimpleStringProperty languageProperty() {
        return languageProperty;
    }

    public SimpleStringProperty textProperty() {
        return textProperty;
    }

    /**
     * Returns a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(this.getClass().getSimpleName()).append(": ");
        buff.append(" ics:");
        buff.append(this.initialCaseSignificantProperty.get());
        buff.append(" lang:");
        buff.append("'").append(this.languageProperty.get()).append("'");
        buff.append(" text:");
        buff.append("'").append(this.textProperty.get()).append("'");
        buff.append(super.toString());

        return buff.toString();
    }

    //~--- get methods ---------------------------------------------------------

    /*
     * (non-Javadoc)
     *
     * @see org.ihtsdo.etypes.I_DescribeExternally#getLang()
     */
    public String getLanguage() {
        return languageProperty.get();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ihtsdo.etypes.I_DescribeExternally#getText()
     */
    public String getText() {
        return textProperty.get();
    }


    /*
     * (non-Javadoc)
     *
     * @see org.ihtsdo.etypes.I_DescribeExternally#isInitialCaseSignificant()
     */
    public boolean isInitialCaseSignificant() {
        return initialCaseSignificantProperty.get();
    }

    public void setInitialCaseSignificant(boolean initialCaseSignificant) {
        this.initialCaseSignificantProperty.set(initialCaseSignificant);
    }

    public void setLanguage(String lang) {
        this.languageProperty.set(lang);
    }

    public void setText(String text) {
        this.textProperty.set(text);
    }
}
