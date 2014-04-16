package org.ihtsdo.otf.tcc.api.refexDynamic;

import java.io.IOException;
import javax.naming.InvalidNameException;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDynamicCAB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;

/**
 * 
 * {@link RefexDynamicVersionBI}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public interface RefexDynamicVersionBI<A extends RefexDynamicVersionBI<A>> extends ComponentVersionBI, RefexDynamicChronicleBI<A> {
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
    RefexDynamicCAB makeBlueprint(ViewCoordinate viewCoordinate, IdDirective idDirective, RefexDirective refexDirective)
            throws IOException, InvalidCAB, ContradictionException;

    /**
     * Check to see if the data field of another object are equal to this one.
     * This is a 'deep' check - all aspects of each element of the RefexDynamicDataBI must be 
     * equal for this to return true.
     */
    boolean dataFieldsEqual(RefexDynamicDataBI[] otherData);
    
    /**
     * @return All of the data columns that are part of this Refex. See
     *         {@link #getData(int)}. May be empty, will not be null.
     */
    RefexDynamicDataBI[] getData();

    /**
     * The type and data (if any) in the specified column of the Refex.
     * 
     * @param columnNumber
     * @return The RefexMemberBI which contains the type and data (if any) for
     *         the specified column
     * @throws IndexOutOfBoundsException
     */
    RefexDynamicDataBI getData(int columnNumber) throws IndexOutOfBoundsException;
    
    /**
     * The type and data (if any) in the specified column of the Refex.
     * 
     * @param columnName
     * @return The RefexMemberBI which contains the type and data (if any) for
     *         the specified column
     * @throws IndexOutOfBoundsException
     */
    RefexDynamicDataBI getData(String columnName) throws InvalidNameException;

}
