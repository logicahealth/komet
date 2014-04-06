package org.ihtsdo.otf.tcc.api.refex4;

import java.io.IOException;

import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex4.blueprint.Refex4CAB;

/**
 * 
 * {@link RefexVersionBI}
 *
 * @author kec
 */
public interface RefexVersionBI<A extends RefexVersionBI<A>> extends ComponentVersionBI, RefexChronicleBI<A> {
    /**
     * @param viewCoordinate
     *            the view coordinate specifying which version of the
     *            description to make a blueprint of
     * @param idDirective
     * @param refexDirective
     * @return the refex blueprint, which can be constructed to create a
     *         <code>RefexChronicleBI</code>
     * @throws IOException
     *             signals that an I/O exception has occurred
     * @throws ContradictionException
     *             if more than one version of the description was returned for
     *             the specified view coordinate
     * @throws InvalidCAB
     *             if the any of the values in blueprint to make are invalid
     * @see org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint
     */
    @Override
    Refex4CAB makeBlueprint(ViewCoordinate viewCoordinate, IdDirective idDirective, RefexDirective refexDirective)
            throws IOException, InvalidCAB, ContradictionException;

    /**
     * Method description
     *
     *
     * @param another
     *
     * @return
     */
    boolean refexFieldsEqual(RefexVersionBI<?> another);

}
