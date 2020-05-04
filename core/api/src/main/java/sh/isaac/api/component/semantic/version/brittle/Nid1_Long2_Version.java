package sh.isaac.api.component.semantic.version.brittle;

import sh.isaac.api.chronicle.VersionType;
/**
 *
 * @author kec
 */
public interface Nid1_Long2_Version
        extends BrittleVersion {

    int getNid1();
    long getLong2();

    void setNid1(int nid);
    void setLong2(long value);

    @Override
    default VersionType getSemanticType() {
        return VersionType.Nid1_Long2;
    }

    @Override
    default BrittleDataTypes[] getFieldTypes() {
        return new BrittleDataTypes[] {
                BrittleDataTypes.NID,
                BrittleDataTypes.LONG};
    }

    @Override
    default Object[] getDataFields() {
        Object[] temp = new Object[] {
                getNid1(),
                getLong2()};
        if (getFieldTypes().length != temp.length) {
            throw new RuntimeException("Mispecified brittle!");
        }
        return temp;
    }
}
