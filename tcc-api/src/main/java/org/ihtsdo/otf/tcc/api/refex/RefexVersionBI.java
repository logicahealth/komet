package org.ihtsdo.otf.tcc.api.refex;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.AnalogGeneratorBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
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
public interface RefexVersionBI<A extends RefexAnalogBI<A>>
        extends ComponentVersionBI, RefexChronicleBI<A>, AnalogGeneratorBI<A> {

   /**
    * @param viewCoordinate the view coordinate specifying which version of the
    * description to make a blueprint of
    * @param idDirective
    * @param refexDirective
    * @return the refex blueprint, which can be constructed to create a <code>RefexChronicleBI</code>
    * @throws IOException signals that an I/O exception has occurred
    * @throws ContradictionException if more than one version of the
    * description was returned for the specified view coordinate
    * @throws InvalidCAB if the any of the values in blueprint to make are
    * invalid
    * @see org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint
    */
   @Override
   RefexCAB makeBlueprint(ViewCoordinate viewCoordinate, IdDirective idDirective,
                          RefexDirective refexDirective)
           throws IOException, InvalidCAB, ContradictionException;

   /**
    * Method description
    *
    *
    * @param another
    *
    * @return
    */
   boolean refexFieldsEqual(RefexVersionBI another);
}
