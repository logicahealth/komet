package sh.komet.fx.stage.spreadsheet;

import org.apache.lucene.index.Term;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.api.util.UuidT5Generator;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

public class OccupationImporter extends SpreadsheetImporter {
    HashSet<UUID> conceptsWritten = new HashSet<>();
    UUID censusCodeAssemblageUuid;
    UUID censusNameAssemblageUuid;
    UUID socCodeAssemblageUuid;

    public OccupationImporter() {
        super("PHVS Occupation", "Occupation",
                new File("/Users/kec/CDC-NIOSH ODI I&O/PHVS Occupation CDC ONET-SOC 2010 - V1.txt"),
                UUID.fromString("d7ae6216-fe1b-11ea-adc1-0242ac120002"), System.currentTimeMillis(), TermAux.USER,
        TermAux.SOLOR_OVERLAY_MODULE, TermAux.DEVELOPMENT_PATH);
    }

    @Override
    protected void addModelData(int stampSequence, Transaction transaction) {

        String socCodeFqn = "Standard occupation code";
        String socCodeName = "SOC";
        socCodeAssemblageUuid = UuidT5Generator.get(sourceUuid, socCodeFqn);
        addConcept(socCodeAssemblageUuid, stampSequence);
        addParents(socCodeAssemblageUuid, stampSequence, transaction, TermAux.IDENTIFIER_SOURCE.getPrimordialUuid());
        addFullyQualifiedName(socCodeFqn, socCodeAssemblageUuid, stampSequence);
        addPreferredName(socCodeName, socCodeAssemblageUuid, stampSequence);

        String censusName = "Census occupation name";
        censusNameAssemblageUuid = UuidT5Generator.get(sourceUuid, censusName);
        addConcept(censusNameAssemblageUuid, stampSequence);
        addParents(censusNameAssemblageUuid, stampSequence, transaction, MetaData.DESCRIPTION_ASSEMBLAGE____SOLOR.getPrimordialUuid());
        addFullyQualifiedName(censusName, censusNameAssemblageUuid, stampSequence);

        String censusCode = "Census occupation code";
        censusCodeAssemblageUuid = UuidT5Generator.get(sourceUuid, censusCode);
        addConcept(censusCodeAssemblageUuid, stampSequence);
        addParents(censusCodeAssemblageUuid, stampSequence, transaction, TermAux.IDENTIFIER_SOURCE.getPrimordialUuid());
        addFullyQualifiedName(censusCode, censusCodeAssemblageUuid, stampSequence);

    }

    @Override
    void processLine(String[] fields, int stampSequence, Transaction transaction) {
        String code = fields[0];
        UUID conceptUuid = UuidT5Generator.get(this.sourceUuid, code);
        String conceptName = fields[1];
        String duplicateConceptName = fields[2];
        if (conceptName.equals(duplicateConceptName) == false) {
            // They are always equal....
            System.out.println("Occupation " + conceptName + " != " + duplicateConceptName);
        }
        String conceptDefinition = fields[3];

        String socCode = fields[4];
        UUID socUuid = UuidT5Generator.get(this.sourceUuid, socCode);

        String socConceptName = fields[5];

        String censusParentCode = fields[6];
        UUID censusParentUuid = UuidT5Generator.get(this.sourceUuid, censusParentCode);
        String censusConceptName = fields[7];
        String preferredName = fields[8];

        UUID censusConceptUuid = UuidT5Generator.get(this.sourceUuid, censusParentCode);
//        if (!conceptsWritten.contains(censusConceptUuid)) {
//            conceptsWritten.add(censusConceptUuid);
//            addConcept(censusConceptUuid, stampSequence);
//            addParents(censusConceptUuid, stampSequence, transaction, this.topConceptUuid);
//            addFullyQualifiedName(censusConceptName + " (Census)", censusConceptUuid, stampSequence);
//            addPreferredName(censusConceptName, censusConceptUuid, stampSequence);
//            addStringSemantic(censusCodeAssemblageUuid, censusConceptUuid, censusParentCode, stampSequence);
//        }

        if (!conceptsWritten.contains(socUuid)) {
            conceptsWritten.add(socUuid);
            addConcept(socUuid, stampSequence);
            String socParent = socCode.substring(0, socCode.indexOf('.'));
            UUID socParentUuid = UuidT5Generator.get(this.sourceUuid, socParent);
            try {
                addParents(socUuid, stampSequence, transaction, socParentUuid);
            } catch (Throwable e) {
                LOG.error("Failed with SOC Parent: " + socParent + " " + Arrays.toString(fields));
            }
            addFullyQualifiedName(socConceptName + " (SOC " + socCode +
                    ")", socUuid, stampSequence);
            addPreferredName(socConceptName, socUuid, stampSequence);
            addStringSemantic(socCodeAssemblageUuid, socUuid, socCode, stampSequence);
        }

        if (!conceptsWritten.contains(conceptUuid)) {
            conceptsWritten.add(conceptUuid);
            addConcept(conceptUuid, stampSequence);
            try {
                addParents(conceptUuid, stampSequence, transaction, socUuid);
            } catch (Throwable e) {
                LOG.error("Failed with SOC: " + socCode + " " + Arrays.toString(fields));
            }
            addFullyQualifiedName(conceptName + " (" + code +
                    ")", conceptUuid, stampSequence);
            addPreferredName(preferredName, conceptUuid, stampSequence);
            addDefinition(conceptDefinition, conceptUuid, stampSequence);
            addStringSemantic(MetaData.CODE____SOLOR.getPrimordialUuid(), conceptUuid, code, stampSequence);
            addStringSemantic(censusCodeAssemblageUuid, conceptUuid, censusParentCode, stampSequence);
            addStringSemantic(censusNameAssemblageUuid, conceptUuid, censusConceptName, stampSequence);
            addStringSemantic(socCodeAssemblageUuid, conceptUuid, socCode, stampSequence);
        } else {
            System.out.println("Duplicate concept in file: " + code + ": " + conceptName);
        }

    }
}
