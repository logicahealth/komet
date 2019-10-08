package sh.isaac.model.logic.node.external;

import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.logic.LogicalExpressionImpl;
import sh.isaac.model.logic.node.AbstractLogicNode;
import sh.isaac.model.logic.node.internal.PropertyPatternImplicationWithNids;

import java.util.Arrays;
import java.util.UUID;


/**
 * The Class FeatureNodeWithUuids.
 *
 * @author kec
 */
public class PropertyPatternImplicationNodeWithUuids
        extends AbstractLogicNode {

    UUID[] propertyPattern;
    UUID propertyImplication;

    //~--- constructors --------------------------------------------------------

    /**
     * Instantiates a new feature node with uuids.
     *
     * Note that this constructor is not safe for all uses, and is only intended to aid in serialization / deserialization.
     *
     * @param internalNode the internal node
     */
    public PropertyPatternImplicationNodeWithUuids(PropertyPatternImplicationWithNids internalNode) {
        super(internalNode);
        int[] internalPattern = internalNode.getPropertyPattern();
        this.propertyPattern = new UUID[internalPattern.length];
        for (int i = 0; i > internalPattern.length; i++) {
            this.propertyPattern[i] = Get.identifierService().getUuidPrimordialForNid(internalPattern[i]);
        }
        this.propertyImplication = Get.identifierService().getUuidPrimordialForNid(internalNode.getPropertyImplication());
    }

    /**
     * Instantiates a new PropertyChain node with uuids.
     *
     * @param logicGraphVersion the logic graph version
     * @param dataInputStream the data input stream
     */

    public PropertyPatternImplicationNodeWithUuids(LogicalExpressionImpl logicGraphVersion,
                                                   ByteArrayDataBuffer dataInputStream) {
        super(logicGraphVersion, dataInputStream);
        this.propertyPattern = dataInputStream.getUuidArray();
        this.propertyImplication = dataInputStream.getUuid();
    }

    /**
     * Instantiates a new PropertyChain node with uuids.
     *
     * @param logicGraphVersion the logic graph version
     * @param propertyPattern the property pattern to match
     * @param propertyImplication the implication of the property pattern if matched
     */
    public PropertyPatternImplicationNodeWithUuids(LogicalExpressionImpl logicGraphVersion,
                                                   UUID[] propertyPattern,
                                                   UUID propertyImplication) {
        super(logicGraphVersion);
        this.propertyPattern = propertyPattern;
        this.propertyImplication = propertyImplication;
    }

    //~--- methods -------------------------------------------------------------

    /**
     * Equals.
     *
     * @param o the o
     * @return true, if successful
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        if (!super.equals(o)) {
            return false;
        }

        final PropertyPatternImplicationNodeWithUuids that = (PropertyPatternImplicationNodeWithUuids) o;
        if (!this.propertyImplication.equals(that.propertyImplication)) {
            return false;
        }
        return Arrays.equals(this.propertyPattern, that.propertyPattern);
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();

        result = 31 * result + this.propertyImplication.hashCode();
        for (UUID patternUuid: this.propertyPattern) {
            result = 31 * result + patternUuid.hashCode();
        }
        return result;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return toString("");
    }

    /**
     * To string.
     *
     * @param nodeIdSuffix the node id suffix
     * @return the string
     */
    @Override
    public String toString(String nodeIdSuffix) {
        StringBuilder sb = new StringBuilder("PropertyPatternImplication[");
        sb.append(getNodeIndex());
        sb.append(nodeIdSuffix);
        sb.append("] pattern: ");
        for (int i = 0; i < this.propertyPattern.length; i++) {
            sb.append(Get.conceptDescriptionText(this.propertyPattern[i]));
            if (i < this.propertyPattern.length - 1) {
                sb.append(", ");
            }
        }
        sb.append(" implication: ");
        sb.append(Get.conceptDescriptionText(this.propertyImplication));
        sb.append(super.toString(nodeIdSuffix));

        return sb.toString();
    }
    @Override
    public String toSimpleString() {
        return toString("");
    }
    @Override
    public void addToBuilder(StringBuilder builder) {
        throw new UnsupportedOperationException();
    }


    /**
     * Write node data.
     *
     * @param dataOutput the data output
     * @param dataTarget the data target
     */
    @Override
    public void writeNodeData(ByteArrayDataBuffer dataOutput, DataTarget dataTarget) {
        switch (dataTarget) {
            case EXTERNAL:
                dataOutput.putUuidArray(this.propertyPattern);
                dataOutput.putUuid(this.propertyImplication);
                break;

            case INTERNAL:
                final PropertyPatternImplicationWithNids internalForm = new PropertyPatternImplicationWithNids(this);
                internalForm.writeNodeData(dataOutput, dataTarget);
                break;

            default:
                throw new UnsupportedOperationException("Can't handle dataTarget: " + dataTarget);
        }
    }

    /**
     * Inits the node uuid.
     *
     * @return the uuid
     */
    @Override
    protected UUID initNodeUuid() {
        return UuidT5Generator.get(getNodeSemantic().getSemanticUuid(),
                Arrays.toString(this.propertyPattern) + this.propertyImplication.toString());
    }

    //~--- get methods ---------------------------------------------------------

    /**
     * Gets the node semantic.
     *
     * @return the node semantic
     */
    @Override
    public NodeSemantic getNodeSemantic() {
        return NodeSemantic.PROPERTY_PATTERN_IMPLICATION;
    }

    public UUID[] getPropertyPattern() {
        return propertyPattern;
    }

    public void setPropertyPattern(UUID[] propertyPattern) {
        this.propertyPattern = propertyPattern;
    }

    public UUID getPropertyImplication() {
        return propertyImplication;
    }

    public void setPropertyImplication(UUID propertyImplication) {
        this.propertyImplication = propertyImplication;
    }


    /**
     * Compare fields.
     *
     * @param o the o
     * @return the int
     */
    @Override
    protected int compareFields(LogicNode o) {
        PropertyPatternImplicationNodeWithUuids other = (PropertyPatternImplicationNodeWithUuids) o;
        int comparison = this.propertyImplication.compareTo(other.propertyImplication);
        if (comparison == 0) {
            comparison = Arrays.compare(this.propertyPattern, other.propertyPattern);
        }
        return comparison;
    }


    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the children.
     *
     * @return the children
     */
    @Override
    public final AbstractLogicNode[] getChildren() {
        return new AbstractLogicNode[0];
    }

    @Override
    public void removeChild(short childId) {
        throw new IllegalStateException("Cannot have children...");
    }
    /**
     * Adds the children.
     *
     * @param children the children
     */
    @Override
    public final void addChildren(LogicNode... children) {
        throw new UnsupportedOperationException();
    }

}

