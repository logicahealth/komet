package sh.komet.gui.exportation;

public enum ExportComponentType {

    CONCEPT("SOLOR Concept"),
    DESCRIPTION("SOLOR Description");

    private final String fullyQualifiedName;

    ExportComponentType(String fullyQualifiedName){
        this.fullyQualifiedName = fullyQualifiedName;
    }


    @Override
    public String toString() {
        return this.fullyQualifiedName;
    }
}
