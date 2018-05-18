package sh.komet.gui.exportation;

public enum ExportType {

    RF2("SNOMED CT Release Format 2 (RF2)"),
    SRF("SOLOR Release Format (SRF)");

    private final String fullyQualifiedName;

    ExportType(String fullyQualifiedName){
        this.fullyQualifiedName = fullyQualifiedName;
    }


    @Override
    public String toString() {
        return this.fullyQualifiedName;
    }
}
