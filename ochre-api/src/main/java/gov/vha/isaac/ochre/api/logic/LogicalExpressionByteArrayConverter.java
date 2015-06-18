/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api.logic;

import gov.vha.isaac.ochre.api.DataTarget;
import org.jvnet.hk2.annotations.Contract;

/**
 *
 * @author kec
 */
@Contract
public interface LogicalExpressionByteArrayConverter {
    byte[][] convertLogicGraphForm(byte[][] logicGraphBytes, DataTarget dataTarget);
}
