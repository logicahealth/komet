package sh.isaac.api.coordinate;

public interface StampBranchProxy extends StampBranch {

    StampBranch getStampBranch();

    @Override
    default long getBranchOriginTime() {
        return getStampBranch().getBranchOriginTime();
    }

    @Override
    default int getPathOfBranchNid() {
        return getStampBranch().getPathOfBranchNid();
    }

    @Override
    default StampBranchImmutable toStampBranchImmutable() {
        return null;
    }
}
