package sh.isaac.solor.direct;

public enum GenomicComponentType {
    VARIANT_CONCEPT("Variant concept", "gov.nih.nlm.ncbi.variant."),
    GENE_CONCEPT("Gene concept", "gov.nih.nlm.ncbi.gene."),
    GENE_SNOMED_REL("Gene to SNOMED CT relationshp", "gov.nih.nlm.ncbi.rel."),
    VARIANT_GENE_REL("Variant to Gene relationship", "gov.nih.nlm.ncbi.rel."),
    VARIANT_DESC("Variant description", "gov.nih.nlm.ncbi.description."),
    GENE_DESC("Gene description", "gov.nih.nlm.ncbi.description.")
    ;


    private String nameString;
    private String namespaceString;

    GenomicComponentType(String nameString, String namespaceString) {
        this.nameString = nameString;
        this.namespaceString = namespaceString;
    }

    public String getNamespaceString() {
        return namespaceString;
    }

    @Override
    public String toString() {
        return this.nameString;
    }
}
