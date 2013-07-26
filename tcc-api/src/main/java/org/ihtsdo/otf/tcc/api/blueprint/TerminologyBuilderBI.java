package org.ihtsdo.otf.tcc.api.blueprint;

import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import java.io.IOException;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptAttributeAB;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptCB;
import org.ihtsdo.otf.tcc.api.blueprint.DescriptionCAB;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.MediaCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RelationshipCAB;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.media.MediaChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;

public interface TerminologyBuilderBI {

    /**
     *  
     * @param res
     * @return A <code>RefexChronicleBI</code> if the <code>blueprint</code> 
     * regardless of if the RefexChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    RefexChronicleBI<?> construct(RefexCAB blueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     *  This method incurs an extra cost to determine if a current version already meets the specification. 
     * @param res
     * @return A <code>RefexChronicleBI</code> if the <code>blueprint</code> 
     * regardless of if the RefexChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    RefexChronicleBI<?> constructIfNotCurrent(RefexCAB blueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     *  
     * @param res
     * @return A <code>RelationshipChronicleBI</code> if the <code>blueprint</code> 
     * regardless of if the RelationshipChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    RelationshipChronicleBI construct(RelationshipCAB blueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     *  This method incurs an extra cost to determine if a current version already meets the specification. 
     * @param res
     * @return A <code>RelationshipChronicleBI</code> if the 
     * <code>blueprint</code> regardless of if the RelationshipChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    RelationshipChronicleBI constructIfNotCurrent(RelationshipCAB blueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     *  
     * @param res
     * @return A <code>DescriptionChronicleBI</code> if the <code>blueprint</code> 
     * regardless of if the DescriptionChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    DescriptionChronicleBI construct(DescriptionCAB blueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     *  This method incurs an extra cost to determine if a current version already meets the specification. 
     * @param res
     * @return A <code>DescriptionChronicleBI</code> if the
     * <code>blueprint</code> regardless of if the DescriptionChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    DescriptionChronicleBI constructIfNotCurrent(DescriptionCAB blueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     *  
     * @param res
     * @return A <code>MediaChronicleBI</code> if the <code>blueprint</code> 
     * regardless of if the MediaChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    MediaChronicleBI construct(MediaCAB blueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     *  This method incurs an extra cost to determine if a current version already meets the specification. 
     * @param res
     * @return A <code>MediaChronicleBI</code> if the <code>blueprint</code> 
     *         regardless of if the MediaChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    MediaChronicleBI constructIfNotCurrent(MediaCAB blueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     *  
     * @param res
     * @return A <code>ConceptChronicleBI</code> if the <code>blueprint</code> 
     *          regardless of if the ConceptChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    ConceptChronicleBI construct(ConceptCB blueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     *  This method incurs an extra cost to determine if a current version 
     *  already meets the specification. 
     * @param res
     * @return A <code>ConceptChronicleBI</code> if the <code>blueprint</code> 
     *          regardless of if the ConceptChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    ConceptChronicleBI constructIfNotCurrent(ConceptCB blueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     *  
     * @param res
     * @return A <code>ConceptAttributeChronicleBI</code> if the <code>blueprint</code> regardless of if the ConceptAttributeChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    ConceptAttributeChronicleBI construct(ConceptAttributeAB blueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     *  This method incurs an extra cost to determine if a current version already meets the specification. 
     * @param res
     * @return A <code>ConceptAttributeChronicleBI</code> if the <code>blueprint</code> regardless of if the ConceptAttributeChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    ConceptAttributeChronicleBI constructIfNotCurrent(ConceptAttributeAB blueprint) throws IOException, InvalidCAB, ContradictionException;

    EditCoordinate getEditCoordinate();
}
