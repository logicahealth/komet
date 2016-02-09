package gov.vha.isaac.ochre.model.logic.node;

import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.api.logic.LogicNode;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;
import org.apache.mahout.math.list.ShortArrayList;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by kec on 12/6/14.
 */
public abstract class ConnectorNode extends AbstractLogicNode {

    private final ShortArrayList childIndices;

    public ConnectorNode(LogicalExpressionOchreImpl logicGraphVersion, DataInputStream dataInputStream) throws IOException {
        super(logicGraphVersion, dataInputStream);
        short childrenSize = dataInputStream.readShort();
        childIndices = new ShortArrayList(childrenSize);
        for (int index = 0; index < childrenSize; index++) {
            childIndices.add(dataInputStream.readShort());
        }
    }

    public ConnectorNode(LogicalExpressionOchreImpl logicGraphVersion, AbstractLogicNode... children) {
        super(logicGraphVersion);
        childIndices = new ShortArrayList(children.length);
        for (AbstractLogicNode child : children) {
            childIndices.add(child.getNodeIndex());
        }
    }

    public ConnectorNode(AbstractLogicNode another) {
        super(another);
        childIndices = new ShortArrayList(another.getChildren().length);
        for (AbstractLogicNode child : another.getChildren()) {
            childIndices.add(child.getNodeIndex());
        }
    }
    
    @Override
    public final void sort() {
        childIndices.mergeSortFromTo(0, childIndices.size()-1, 
            (short o1, short o2) 
                -> logicGraphVersion.getNode(o1).compareTo(logicGraphVersion.getNode(o2)));
    }

    @Override
    protected void writeData(DataOutput dataOutput, DataTarget dataTarget) throws IOException {
        sort();
        super.writeData(dataOutput, dataTarget);
        dataOutput.writeShort(childIndices.size());
        for (short value : childIndices.elements()) {
            dataOutput.writeShort(value);
        }
    }

    /**
     * 
     * @return a sorted array of child <code>LogicNode</code>s.
     */
    @Override
    public AbstractLogicNode[] getChildren() {
        AbstractLogicNode[] childNodes = new AbstractLogicNode[childIndices.size()];
        for (int i = 0; i < childNodes.length; i++) {
            childNodes[i] = (AbstractLogicNode) logicGraphVersion.getNode(childIndices.get(i));
        }
        return childNodes;
    }

    @Override
    public void addChildren(LogicNode... children) {
        for (LogicNode child : children) {
            childIndices.add(child.getNodeIndex());
        }
        sort();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + childIndices.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return toString("");
    }
    
    @Override
    public String toString(String nodeIdSuffix) {
        if (childIndices != null && !childIndices.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            builder.append("➞[");
            childIndices.forEach((index) -> {
                builder.append(index);
                builder.append(nodeIdSuffix);
                builder.append(", ");
                return true;
            });
            builder.deleteCharAt(builder.length() -1);
            builder.deleteCharAt(builder.length() -1);
            builder.append("]");
            return builder.toString();
        }
        return "";
    }
    
    @Override
    protected int compareFields(LogicNode o) {
        // node semantic is already determined to be the same...
        return compareNodeFields(o);
    }
    
    protected abstract int compareNodeFields(LogicNode o);

}
