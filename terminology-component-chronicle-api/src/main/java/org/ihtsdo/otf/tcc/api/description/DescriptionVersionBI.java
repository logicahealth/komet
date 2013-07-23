package org.ihtsdo.otf.tcc.api.description;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.AnalogGeneratorBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.chronicle.TypedComponentVersionBI;
import org.ihtsdo.otf.tcc.api.blueprint.DescriptionCAB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.regex.Pattern;

/**
 * Interface description
 *
 *
 * @param <A>
 *
 * @version        Enter version here..., 13/04/30
 * @author         Enter your name here...    
 */
public interface DescriptionVersionBI<A extends DescriptionAnalogBI>
        extends TypedComponentVersionBI, DescriptionChronicleBI, AnalogGeneratorBI<A> {

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
   public DescriptionCAB makeBlueprint(ViewCoordinate vc, IdDirective idDirective,
       RefexDirective refexDirective)
           throws IOException, ContradictionException, InvalidCAB;

   /**
    * Method description
    *
    *
    * @param p
    *
    * @return
    */
   public boolean matches(Pattern p);

   /**
    * Method description
    *
    *
    * @return
    */
   public String getLang();

   /**
    * Method description
    *
    *
    * @return
    */
   public String getText();

   /**
    * Method description
    *
    *
    * @return
    */
   public boolean isInitialCaseSignificant();
}
