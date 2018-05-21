package sh.komet.gui.exportation;

public enum ExportFormatType {

    RF2("SNOMED CT Release Format 2 (RF2)"),
    SRF("SOLOR Release Format (SRF)");

    private final String fullyQualifiedName;

    ExportFormatType(String fullyQualifiedName){
        this.fullyQualifiedName = fullyQualifiedName;
    }


    @Override
    public String toString() {
        return this.fullyQualifiedName;
    }
}
