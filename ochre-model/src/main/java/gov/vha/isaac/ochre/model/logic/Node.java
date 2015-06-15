package gov.vha.isaac.ochre.model.logic;

import gov.vha.isaac.ochre.api.DataTarget;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Created by kec on 12/9/14.
 */
public interface Node {

    NodeSemantic getNodeSemantic();

    Node[] getChildren();
    
    default Stream<Node> getChildStream() {
        return Arrays.stream(getChildren());
    };

    byte[] getBytes(DataTarget dataTarget);

    short getNodeIndex();

    void setNodeIndex(short nodeIndex);

    void addChildren(Node... children);
}
