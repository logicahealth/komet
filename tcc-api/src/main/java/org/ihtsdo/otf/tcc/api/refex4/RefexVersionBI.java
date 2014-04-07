package org.ihtsdo.otf.tcc.api.refex4;

import java.io.IOException;

import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex4.blueprint.DynamicRefexCAB;
import org.ihtsdo.otf.tcc.api.refex4.data.RefexDataBI;

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
    DynamicRefexCAB makeBlueprint(ViewCoordinate viewCoordinate, IdDirective idDirective, RefexDirective refexDirective)
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
    
    /**
     * @return All of the data columns that are part of this Refex. See
     *         {@link #getData(int)}. May be empty, will not be null.
     */
    RefexDataBI[] getData();

    /**
     * The type and data (if any) in the specified column of the Refex.
     * 
     * @param columnNumber
     * @return The RefexMemberBI which contains the type and data (if any) for
     *         the specified column
     * @throws IndexOutOfBoundsException
     */
    RefexDataBI getData(int columnNumber) throws IndexOutOfBoundsException;
    
    /**
     * The type and data (if any) in the specified column of the Refex.
     * 
     * @param columnName
     * @return The RefexMemberBI which contains the type and data (if any) for
     *         the specified column
     * @throws IndexOutOfBoundsException
     */
    RefexDataBI getData(String columnName) throws IndexOutOfBoundsException;

}
