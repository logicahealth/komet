/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.otf.tcc.api.refex.logicgraph;

import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;

/**
 *
 * @author kec
 * @param <A>
 */
public interface LogicGraphVersionBI <A extends LogicGraphAnalogBI<A>>
        extends RefexVersionBI<A> {

    /**
     * Gets the array of byte array associated with this version of an array of
     * byte array refex member.
     *
     * @return the array of byte array with this refex member version
     */
    byte[][] getLogicGraphBytes();
    
    byte[][] getExternalLogicGraphBytes();

}
