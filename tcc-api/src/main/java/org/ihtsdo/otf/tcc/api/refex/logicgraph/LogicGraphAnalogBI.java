/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.otf.tcc.api.refex.logicgraph;

import java.beans.PropertyVetoException;
import org.ihtsdo.otf.tcc.api.refex.RefexAnalogBI;

/**
 *
 * @author kec
 * @param <A>
 */
public interface LogicGraphAnalogBI<A extends LogicGraphAnalogBI<A>>
        extends RefexAnalogBI<A>, LogicGraphVersionBI<A> {

    /**
     * Sets the array of bytes based on the given
     * {@code logicGraphBytes} for this array .
     *
     * @param logicGraphBytes the array of byte array to be associated with this member
     * @throws PropertyVetoException if the new value is not valid
     */
    void setLogicGraphBytes(byte[][] logicGraphBytes) throws PropertyVetoException;
}