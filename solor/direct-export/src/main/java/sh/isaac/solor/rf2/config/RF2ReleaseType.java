package sh.isaac.solor.rf2.config;

public enum RF2ReleaseType {

    FULL("Full"),
    SNAPSHOT("Snapshot"),
//    DELTA("Delta");
    ;

    private String releaseTypeName;

    RF2ReleaseType(String releaseTypeName){
        this.releaseTypeName = releaseTypeName;
    }

    @Override
    public String toString() {
        return this.releaseTypeName;
    }
}
