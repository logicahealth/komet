package sh.komet.fx.stage.spreadsheet;

import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.api.util.UuidT5Generator;

import java.io.File;
import java.util.UUID;

public class SocImporter2010 extends SpreadsheetImporter {
    private static int MAJOR_GROUP = 0;
    private static int MINOR_GROUP = 1;
    private static int BROAD_GROUP = 2;
    private static int DETAILED_OCCUPATION = 3;
    private static int DESCRIPTION = 4;
    private UUID socCodeAssemblageUuid;

    private UUID majorGroupUuid;
    private UUID minorGroupUuid;
    private UUID broadGroupUuid;
    private UUID detailedOccupationUuid;

    public SocImporter2010() {
        super("Standard occupational classification", "Occupations",
                new File("/Users/kec/CDC-NIOSH ODI I&O/soc_structure_2010.txt"),
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
    }

    @Override
    void processLine(String[] fields, int stampSequence, Transaction transaction) {
        if (!fields[MAJOR_GROUP].trim().isBlank()) {
            majorGroup(fields, stampSequence, transaction);
        } else if (!fields[MINOR_GROUP].trim().isBlank()) {
            minorGroup(fields, stampSequence, transaction);
        } else if (!fields[BROAD_GROUP].trim().isBlank()) {
            broadGroup(fields, stampSequence, transaction);
        } else if (!fields[DETAILED_OCCUPATION].trim().isBlank()) {
            detailedOccupation(fields, stampSequence, transaction);
        }
    }


    private void majorGroup(String[] fields, int stampSequence, Transaction transaction) {
        majorGroupUuid = UuidT5Generator.get(this.sourceUuid, fields[MAJOR_GROUP].trim());
        String conceptName = fields[DESCRIPTION].trim();
        addConcept(majorGroupUuid, stampSequence);
        addParents(majorGroupUuid, stampSequence, transaction, this.topConceptUuid);
        addFullyQualifiedName(conceptName + " (major " + fields[MAJOR_GROUP].trim() +
                ")", majorGroupUuid, stampSequence);
        addPreferredName(conceptName, majorGroupUuid, stampSequence);
        addStringSemantic(socCodeAssemblageUuid, majorGroupUuid, fields[MAJOR_GROUP].trim(), stampSequence);
    }

    private void minorGroup(String[] fields, int stampSequence, Transaction transaction) {
        minorGroupUuid = UuidT5Generator.get(this.sourceUuid, fields[MINOR_GROUP].trim());
        String conceptName = fields[DESCRIPTION].trim();
        addConcept(minorGroupUuid, stampSequence);
        addParents(minorGroupUuid, stampSequence, transaction, this.majorGroupUuid);
        addFullyQualifiedName(conceptName + " (minor " + fields[MINOR_GROUP].trim() +
                ")", minorGroupUuid, stampSequence);
        addPreferredName(conceptName, minorGroupUuid, stampSequence);
        addStringSemantic(socCodeAssemblageUuid, minorGroupUuid, fields[MINOR_GROUP].trim(), stampSequence);
    }

    private void broadGroup(String[] fields, int stampSequence, Transaction transaction) {
        broadGroupUuid = UuidT5Generator.get(this.sourceUuid, fields[BROAD_GROUP].trim());
        String conceptName = fields[DESCRIPTION].trim();
        addConcept(broadGroupUuid, stampSequence);
        addParents(broadGroupUuid, stampSequence, transaction, this.minorGroupUuid);
        addFullyQualifiedName(conceptName + " (broad " + fields[BROAD_GROUP].trim() +
                ")", broadGroupUuid, stampSequence);
        addPreferredName(conceptName, broadGroupUuid, stampSequence);
        addStringSemantic(socCodeAssemblageUuid, broadGroupUuid, fields[BROAD_GROUP].trim(), stampSequence);
    }

    private void detailedOccupation(String[] fields, int stampSequence, Transaction transaction) {
        detailedOccupationUuid = UuidT5Generator.get(this.sourceUuid, fields[DETAILED_OCCUPATION].trim());
        String conceptName = fields[DESCRIPTION].trim();
        addConcept(detailedOccupationUuid, stampSequence);
        addParents(detailedOccupationUuid, stampSequence, transaction, this.broadGroupUuid);
        addFullyQualifiedName(conceptName + " (detailed " + fields[DETAILED_OCCUPATION].trim() +
                ")", detailedOccupationUuid, stampSequence);
        addPreferredName(conceptName, detailedOccupationUuid, stampSequence);
        addStringSemantic(socCodeAssemblageUuid, detailedOccupationUuid, fields[DETAILED_OCCUPATION].trim(), stampSequence);
    }
}
