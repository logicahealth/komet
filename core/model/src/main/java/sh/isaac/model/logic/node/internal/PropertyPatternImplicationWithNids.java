package sh.isaac.model.logic.node.internal;

import org.roaringbitmap.RoaringBitmap;
import sh.isaac.api.DataTarget;
import sh.isaac.api.Get;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.logic.LogicalExpressionImpl;
import sh.isaac.model.logic.node.AbstractLogicNode;
import sh.isaac.model.logic.node.external.PropertyPatternImplicationNodeWithUuids;

import java.util.Arrays;
import java.util.UUID;


/**
 * Created by kec on 12/9/14.
 */
public final class PropertyPatternImplicationWithNids
        extends AbstractLogicNode {

    int[] propertyPattern;
    int propertyImplication;


    //~--- constructors --------------------------------------------------------

    /**
     * Instantiates a new PropertyChain node with sequences.
     *
     * @param externalForm the external form
     */
    public PropertyPatternImplicationWithNids(PropertyPatternImplicationNodeWithUuids externalForm) {
        super(externalForm);
        UUID[] externalPattern = externalForm.getPropertyPattern();
        this.propertyPattern = new int[externalPattern.length];
        for (int i = 0; i > externalPattern.length; i++) {
            this.propertyPattern[i] = Get.identifierService().getNidForUuids(externalPattern[i]);
        }
        this.propertyImplication = Get.identifierService().getNidForUuids(externalForm.getPropertyImplication());
    }

    /**
     * Instantiates a new PropertyChain node with sequences.
     *
     * @param logicGraphVersion the logic graph version
     * @param dataInputStream the data input stream
     */

    public PropertyPatternImplicationWithNids(LogicalExpressionImpl logicGraphVersion,
                                              ByteArrayDataBuffer dataInputStream) {
        super(logicGraphVersion, dataInputStream);
        this.propertyPattern = dataInputStream.getNidArray();
        this.propertyImplication = dataInputStream.getNid();
    }

    /**
     * Instantiates a new PropertyChain node with sequences.
     *
     * @param logicGraphVersion the logic graph version
     * @param propertyPattern the property pattern to match
     * @param propertyImplication the property implication if the pattern is matched.
     */
    public PropertyPatternImplicationWithNids(LogicalExpressionImpl logicGraphVersion,
                                              int[] propertyPattern,
                                              int propertyImplication) {
        super(logicGraphVersion);
        this.propertyPattern = propertyPattern;
        this.propertyImplication = propertyImplication;
    }

    //~--- methods -------------------------------------------------------------


    /**
     * Adds the concepts referenced by node.
     *
     * @param conceptNidSet the concept nid set
     */
    @Override
    public void addConceptsReferencedByNode(RoaringBitmap conceptNidSet) {
        super.addConceptsReferencedByNode(conceptNidSet);
        conceptNidSet.add(propertyImplication);
        for (int nid: this.propertyPattern) {
            conceptNidSet.add(nid);
        }
    }

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

        final PropertyPatternImplicationWithNids that = (PropertyPatternImplicationWithNids) o;

        if (this.propertyImplication != that.propertyImplication) {
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

        result = 31 * result + Integer.hashCode(this.propertyImplication);

        result = 31 * result + Arrays.hashCode(this.propertyPattern);
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
        sb.append("] pattern: '");
        for (int i = 0; i < this.propertyPattern.length; i++) {
            sb.append(Get.conceptDescriptionText(this.propertyPattern[i]));
            if (i < this.propertyPattern.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("'; implication: ");
        sb.append(Get.conceptDescriptionText(this.propertyImplication));
        sb.append(super.toString(nodeIdSuffix));

        return sb.toString();
    }

    private enum VARIABLES {
        X, Y, Z, A, B, C, D, E, F, G, H, I, J, K, L, M
    }

    @Override
    public String toSimpleString() {
         // If X "located in" Y and Y "part of" Z then X "located in" Z, for example a disease located in a part is located in the whole.

        StringBuilder sb = new StringBuilder("if  ");

        for (int i = 0; i < propertyPattern.length; i++) {
            // X
            sb.append(VARIABLES.values()[i]);
            // "located in"
            sb.append(" \"");
            sb.append(Get.defaultCoordinate().getPreferredDescriptionText(propertyPattern[i]));
            sb.append("\" ");
            // Y
            sb.append(VARIABLES.values()[i + 1]);
            sb.append("\n");
            if (i + 1 < propertyPattern.length) {
                sb.append("and ");
            }
         }

        sb.append("then ");
        sb.append(VARIABLES.values()[0]);
        sb.append(" \"");
        sb.append(Get.defaultCoordinate().getPreferredDescriptionText(propertyImplication));
        sb.append("\" ");
        sb.append(VARIABLES.values()[propertyPattern.length]);
        return sb.toString();
    }
    @Override
    public void addToBuilder(StringBuilder builder) {
        //TODO consider removing this method...
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
                final PropertyPatternImplicationNodeWithUuids externalForm = new PropertyPatternImplicationNodeWithUuids(this);

                externalForm.writeNodeData(dataOutput, dataTarget);
                break;

            case INTERNAL:
                dataOutput.putNidArray(this.propertyPattern);
                dataOutput.putNid(this.propertyImplication);
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
                Arrays.toString(this.propertyPattern) + this.propertyImplication);
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

    public int[] getPropertyPattern() {
        return propertyPattern;
    }

    public void setPropertyPattern(int[] propertyPattern) {
        this.propertyPattern = propertyPattern;
    }

    public int getPropertyImplication() {
        return propertyImplication;
    }

    public void setPropertyImplication(int propertyImplication) {
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
        PropertyPatternImplicationWithNids other = (PropertyPatternImplicationWithNids) o;
        int comparison = Integer.compare(this.propertyImplication, other.propertyImplication);
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

