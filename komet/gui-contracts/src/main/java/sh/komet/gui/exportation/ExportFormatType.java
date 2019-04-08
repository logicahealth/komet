package sh.komet.gui.exportation;

public enum ExportFormatType {

    RF2("SNOMED CT Release Format 2 Compatible (RF2)"),
    SRF("Solor Relational Format (SRF)"),
    SOF("Solor Object Format (SOF)");

    private final String fullyQualifiedName;

    ExportFormatType(String fullyQualifiedName){
        this.fullyQualifiedName = fullyQualifiedName;
    }

    @Override
    public String toString() {
        return this.fullyQualifiedName;
    }
}
