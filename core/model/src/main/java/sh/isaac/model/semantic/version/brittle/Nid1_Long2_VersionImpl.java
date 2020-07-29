package sh.isaac.model.semantic.version.brittle;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.brittle.Nid1_Long2_Version;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.AbstractVersionImpl;

/**
 *
 * @author kec
 */
public class Nid1_Long2_VersionImpl
        extends AbstractVersionImpl
        implements Nid1_Long2_Version {
    int nid1 = Integer.MAX_VALUE;
    long long2 = Long.MIN_VALUE;

    //~--- constructors --------------------------------------------------------

    public Nid1_Long2_VersionImpl(SemanticChronology container, int stampSequence) {
        super(container, stampSequence);
    }

    public Nid1_Long2_VersionImpl(SemanticChronology container,
                                 int stampSequence, ByteArrayDataBuffer data) {
        super(container, stampSequence);
        this.nid1 = data.getNid();
        this.long2 = data.getLong();
    }

    public Nid1_Long2_VersionImpl(Nid1_Long2_VersionImpl other,
                                  int stampSequence) {
        super(other.getChronology(), stampSequence);
        this.nid1 = other.getNid1();
        this.long2 = other.getLong2();
    }
    /**
     * Write version data.
     *
     * @param data the data
     */
    @Override
    public void writeVersionData(ByteArrayDataBuffer data) {
        super.writeVersionData(data);
        data.putNid(this.nid1);
        data.putLong(this.long2);
    }
    //~--- methods -------------------------------------------------------------

    public <V extends Version> V setupAnalog(int stampSequence) {
        SemanticChronologyImpl chronologyImpl = (SemanticChronologyImpl) this.chronicle;
        final Nid1_Long2_VersionImpl newVersion = new Nid1_Long2_VersionImpl(this, stampSequence);
        newVersion.setNid1(this.nid1);
        newVersion.setLong2(this.long2);

        chronologyImpl.addVersion(newVersion);
        return (V) newVersion;
    }

    @Override
    protected boolean deepEquals3(AbstractVersionImpl other) {
        return editDistance3(other, 0) == 0;
    }

    @Override
    protected int editDistance3(AbstractVersionImpl other, int editDistance) {
        Nid1_Long2_VersionImpl another = (Nid1_Long2_VersionImpl) other;
        if (this.nid1 != another.nid1) {
            editDistance++;
        }
        if (this.long2 != another.long2) {
            editDistance++;
        }

        return editDistance;
    }

    //~--- get methods ---------------------------------------------------------

    @Override
    public long getLong2() {
        return long2;
    }

    //~--- set methods ---------------------------------------------------------

    @Override
    public void setLong2(long long2) {
        this.long2 = long2;
    }

    //~--- get methods ---------------------------------------------------------

    @Override
    public int getNid1() {
        return nid1;
    }

    //~--- set methods ---------------------------------------------------------

    @Override
    public void setNid1(int nid1) {
        this.nid1 = nid1;
    }

    /**
     * To string.
     *
     * @param builder the builder
     * @return the string builder
     */
    @Override
    public StringBuilder toString(StringBuilder builder) {
        builder.append(" ")
                .append("{Component: ").append(Get.getTextForComponent(nid1))
                .append(", Long: ").append(long2).append(" ")
                .append(Get.stampService()
                        .describeStampSequence(this.getStampSequence())).append("}");
        return builder;
    }


}

