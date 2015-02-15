package org.ihtsdo.otf.tcc.api.conattr;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.AnalogGeneratorBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptAttributeAB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

/**
 * Interface description
 *
 *
 * @param <A>
 *
 * @version        Enter version here..., 13/04/30
 * @author         Enter your name here...    
 */
public interface ConceptAttributeVersionBI<A extends ConceptAttributeAnalogBI>
        extends ComponentVersionBI, ConceptAttributeChronicleBI, AnalogGeneratorBI<A> {

   /**
    * Method description
    *
    *
    * @param vc
    * @param idDirective
    * @param refexDirective
    *
    * @return
    *
    * @throws ContradictionException
    * @throws IOException
    * @throws InvalidCAB
    */
   @Override
   public ConceptAttributeAB makeBlueprint(ViewCoordinate vc, IdDirective idDirective,
       RefexDirective refexDirective)
           throws IOException, ContradictionException, InvalidCAB;

   /**
    * Method description
    *
    *
    * @return
    */
   public boolean isDefined();
}
