package gov.vha.isaac.ochre.model.logic.node;

import gov.vha.isaac.ochre.api.DataTarget;
import gov.vha.isaac.ochre.model.logic.LogicalExpressionOchreImpl;
import gov.vha.isaac.ochre.api.logic.Node;
import org.apache.mahout.math.list.ShortArrayList;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by kec on 12/6/14.
 */
public abstract class ConnectorNode extends AbstractNode {

    private final ShortArrayList childIndices;

    public ConnectorNode(LogicalExpressionOchreImpl logicGraphVersion, DataInputStream dataInputStream) throws IOException {
        super(logicGraphVersion, dataInputStream);
        short childrenSize = dataInputStream.readShort();
        childIndices = new ShortArrayList(childrenSize);
        for (int index = 0; index < childrenSize; index++) {
            childIndices.add(dataInputStream.readShort());
        }
    }

    public ConnectorNode(LogicalExpressionOchreImpl logicGraphVersion, AbstractNode... children) {
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
     * @return a sorted array of child <code>Node</code>s.
     */
    @Override
    public AbstractNode[] getChildren() {
        AbstractNode[] childNodes = new AbstractNode[childIndices.size()];
        for (int i = 0; i < childNodes.length; i++) {
            childNodes[i] = (AbstractNode) logicGraphVersion.getNode(childIndices.get(i));
        }
        return childNodes;
    }

    @Override
    public void addChildren(Node... children) {
        for (Node child : children) {
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
        if (childIndices != null && !childIndices.isEmpty()) {
            return "➞" + childIndices
                    + super.toString();
        }
        return "";
    }
    
    @Override
    protected int compareFields(Node o) {
        // node semantic is already determined to be the same...
        int comparison = compareNodeFields(o);
        if (comparison != 0) {
            return comparison;
        }
        ConnectorNode other = (ConnectorNode) o;
        if (this.childIndices.size() != other.childIndices.size()) {
            return Integer.compare(this.childIndices.size(), other.childIndices.size());
        }
        for (int i = 0; i < this.childIndices.size(); i++) {
            comparison = logicGraphVersion.getNode(childIndices.get(i)).compareTo(logicGraphVersion.getNode(other.childIndices.get(i)));
            if (comparison != 0) {
                return comparison;
            }
        }
        return 0;
    }
    
    protected abstract int compareNodeFields(Node o);

}
