package gov.vha.isaac.ochre.model.logic.node;

import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.model.logic.LogicExpressionOchreImpl;
import gov.vha.isaac.ochre.model.logic.Node;
import org.apache.mahout.math.list.ShortArrayList;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by kec on 12/6/14.
 */
public abstract class ConnectorNode extends AbstractNode {

    private final ShortArrayList childIndices;

    public ConnectorNode(LogicExpressionOchreImpl logicGraphVersion, DataInputStream dataInputStream) throws IOException {
        super(logicGraphVersion, dataInputStream);
        short childrenSize = dataInputStream.readShort();
        childIndices = new ShortArrayList(childrenSize);
        for (int index = 0; index < childrenSize; index++) {
            childIndices.add(dataInputStream.readShort());
        }
    }

    public ConnectorNode(LogicExpressionOchreImpl logicGraphVersion, AbstractNode... children) {
        super(logicGraphVersion);
        childIndices = new ShortArrayList(children.length);
        for (AbstractNode child : children) {
            childIndices.add(child.getNodeIndex());
        }
    }

    public ConnectorNode(AbstractNode another) {
        super(another);
        childIndices = new ShortArrayList(another.getChildren().length);
        for (AbstractNode child : another.getChildren()) {
            childIndices.add(child.getNodeIndex());
        }
    }

    @Override
    protected void writeData(DataOutput dataOutput, DataTarget dataTarget) throws IOException {
        super.writeData(dataOutput, dataTarget);
        dataOutput.writeShort(childIndices.size());
        for (short value : childIndices.elements()) {
            dataOutput.writeShort(value);
        }
    }

    /**
     * 
     * @return a sorted array of child <code>Node</code>s.
     */
    @Override
    public AbstractNode[] getChildren() {
        AbstractNode[] childNodes = new AbstractNode[childIndices.size()];
        for (int i = 0; i < childNodes.length; i++) {
            childNodes[i] = (AbstractNode) logicGraphVersion.getNode(childIndices.get(i));
        }
        Arrays.sort(childNodes);
        return childNodes;
    }

    @Override
    public void addChildren(Node... children) {
        for (Node child : children) {
            childIndices.add(child.getNodeIndex());
        }
    }

    @Override
    protected int compareFields(Node o) {
        return 0;
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
        if (childIndices != null && !childIndices.isEmpty()) {
            if (childIndices.size() == 1) {
                return "->" + childIndices
                        + super.toString();
            }
            return "->" + childIndices
                    + super.toString();
        }
        return "";
    }
}
