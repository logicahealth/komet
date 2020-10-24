package sh.komet.fx.stage.spreadsheet;

import org.reactfx.util.Lists;
import sh.isaac.MetaData;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.api.util.UuidT5Generator;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

public class IndustryImporter extends SpreadsheetImporter {
    HashSet<UUID> conceptsWritten = new HashSet<>();
    UUID censusIndustryCodeAssemblageUuid;
    UUID industryCodeAssemblageUuid;

    public IndustryImporter() {
        super("PHVS Industry", "Industry",
                new File("/Users/kec/CDC-NIOSH ODI I&O/PHVS Industry CDC NAICS 2012 - V1.txt"),
                UUID.fromString("c44d850c-ff70-11ea-adc1-0242ac120002"), System.currentTimeMillis(), TermAux.USER,
                TermAux.SOLOR_OVERLAY_MODULE, TermAux.DEVELOPMENT_PATH);
    }

    @Override
    protected void addModelData(int stampSequence, Transaction transaction) {

        String industryCodeFqn = "NAICS industry code";
        String industryCodeName = "industry";
        industryCodeAssemblageUuid = UuidT5Generator.get(sourceUuid, industryCodeFqn);
        addConcept(industryCodeAssemblageUuid, stampSequence);
        addParents(industryCodeAssemblageUuid, stampSequence, transaction, TermAux.IDENTIFIER_SOURCE.getPrimordialUuid());
        addFullyQualifiedName(industryCodeFqn, industryCodeAssemblageUuid, stampSequence);
        addPreferredName(industryCodeName, industryCodeAssemblageUuid, stampSequence);

        String censusCode = "Census industry code";
        censusIndustryCodeAssemblageUuid = UuidT5Generator.get(sourceUuid, censusCode);
        addConcept(censusIndustryCodeAssemblageUuid, stampSequence);
        addParents(censusIndustryCodeAssemblageUuid, stampSequence, transaction, TermAux.IDENTIFIER_SOURCE.getPrimordialUuid());
        addFullyQualifiedName(censusCode, censusIndustryCodeAssemblageUuid, stampSequence);
    }

    @Override
    void processLine(String[] fields, int stampSequence, Transaction transaction) {
        String code = fields[0];
        UUID conceptUuid = UuidT5Generator.get(this.sourceUuid, code);
        String conceptName = fields[1];
        String duplicateConceptName = fields[2];
        if (conceptName.equals(duplicateConceptName) == false) {
            // They are always equal....
            System.out.println("Industry " + conceptName + " != " + duplicateConceptName);
        }
        String conceptDefinition = fields[3];

        String industryCode = fields[4];
        UUID industryUuid = UuidT5Generator.get(this.sourceUuid, industryCode);

        String industryConceptName = fields[5];

        String censusParentCode = fields[6];
        UUID censusParentUuid = UuidT5Generator.get(this.sourceUuid, censusParentCode);
        String censusConceptName = fields[7];
        String preferredName = fields[8];

        UUID censusConceptUuid = UuidT5Generator.get(this.sourceUuid, censusParentCode);
        if (!conceptsWritten.contains(censusConceptUuid)) {
            if (censusConceptName == null || censusConceptName.isBlank()) {
                LOG.error("Empty census concept name for: " + Arrays.toString(fields));
            } else {
                conceptsWritten.add(censusConceptUuid);
                addConcept(censusConceptUuid, stampSequence);
                addParents(censusConceptUuid, stampSequence, transaction, this.topConceptUuid);
                addFullyQualifiedName(censusConceptName + " (Census)", censusConceptUuid, stampSequence);
                addPreferredName(censusConceptName, censusConceptUuid, stampSequence);
                addStringSemantic(censusIndustryCodeAssemblageUuid, censusConceptUuid, censusParentCode, stampSequence);
            }
        }

        UUID socConceptUuid = UuidT5Generator.get(this.sourceUuid, industryCode);
        if (!conceptsWritten.contains(socConceptUuid)) {
            conceptsWritten.add(socConceptUuid);
            addConcept(socConceptUuid, stampSequence);
            addParents(socConceptUuid, stampSequence, transaction, censusParentUuid);
            addFullyQualifiedName(industryConceptName + " (NAICS)", industryUuid, stampSequence);
            addPreferredName(industryConceptName, socConceptUuid, stampSequence);
            addStringSemantic(industryCodeAssemblageUuid, socConceptUuid, industryCode, stampSequence);
        }

        if (!conceptsWritten.contains(conceptUuid)) {
            conceptsWritten.add(conceptUuid);
            addConcept(conceptUuid, stampSequence);
            addParents(conceptUuid, stampSequence, transaction, industryUuid);
            addFullyQualifiedName(conceptName, conceptUuid, stampSequence);
            addPreferredName(preferredName, conceptUuid, stampSequence);
            addDefinition(conceptDefinition, conceptUuid, stampSequence);
            addStringSemantic(MetaData.CODE____SOLOR.getPrimordialUuid(), conceptUuid, code, stampSequence);
            addStringSemantic(censusIndustryCodeAssemblageUuid, conceptUuid, censusParentCode, stampSequence);
            addStringSemantic(industryCodeAssemblageUuid, conceptUuid, industryCode, stampSequence);
        } else {
            System.out.println("Duplicate concept in file: " + code + ": " + conceptName);
        }
    }
}
