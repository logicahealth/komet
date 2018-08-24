package sh.isaac.solor.direct;

public enum GenomicConceptType {
    VARIANT("Variant"), GENE("Gene"), ALLELE("Allele"), GENE_SNOMED("Gene");


    private String nameString;

    GenomicConceptType(String nameString) {
        this.nameString = nameString;
    }

    @Override
    public String toString() {
        return this.nameString;
    }
}
