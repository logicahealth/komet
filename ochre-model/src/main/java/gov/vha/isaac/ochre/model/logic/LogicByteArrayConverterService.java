/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.model.logic;



import gov.vha.isaac.ochre.api.DataSource;
import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.api.logic.LogicalExpressionByteArrayConverter;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author kec
 */
@Service 
public class LogicByteArrayConverterService implements LogicalExpressionByteArrayConverter {

    @Override
    public byte[][] convertLogicGraphForm(byte[][] logicGraphBytes, DataTarget dataTarget) {
        LogicalExpressionOchreImpl logicGraph;
        switch (dataTarget) {
            case EXTERNAL:
                logicGraph = new LogicalExpressionOchreImpl(logicGraphBytes, DataSource.INTERNAL);
                break;
            case INTERNAL:
                logicGraph = new LogicalExpressionOchreImpl(logicGraphBytes, DataSource.EXTERNAL);
                break;
            default:
                throw new UnsupportedOperationException("Can't handle: " + dataTarget);
        }
        
        return logicGraph.getData(dataTarget);
    }
}
