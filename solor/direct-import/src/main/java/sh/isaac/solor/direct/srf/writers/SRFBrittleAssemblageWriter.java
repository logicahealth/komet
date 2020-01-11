package sh.isaac.solor.direct.srf.writers;

import org.apache.mahout.math.Arrays;
import sh.isaac.api.*;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.UuidT3Generator;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.ComponentNidVersionImpl;
import sh.isaac.model.semantic.version.LongVersionImpl;
import sh.isaac.model.semantic.version.StringVersionImpl;
import sh.isaac.model.semantic.version.brittle.*;
import sh.isaac.solor.direct.DirectImporter;
import sh.isaac.solor.direct.ImportStreamType;
import sh.isaac.solor.direct.ImportType;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import static java.time.temporal.ChronoField.INSTANT_SECONDS;

/**
 * 2019-04-23
 * aks8m - https://github.com/aks8m
 */
public class SRFBrittleAssemblageWriter extends TimedTaskWithProgressTracker<Void> {

    private static final int SRF_ID_INDEX = 0;
    private static final int SRF_STATUS_INDEX = 1;
    private static final int SRF_TIME_INDEX = 2;
    private static final int SRF_AUTHOR_INDEX = 3;
    private static final int SRF_MODULE_INDEX = 4;
    private static final int SRF_PATH_INDEX = 5;
    private static final int SRF_ASSEMBLAGE_ID_INDEX = 6;
    private static final int SRF_REFERENCED_COMPONENT_ID_INDEX = 7;
    private static final int SRF_VARIABLE_FIELD_START = 7;

    private final List<String[]> refsetRecords;
    private final Semaphore writeSemaphore;
    private final List<IndexBuilderService> indexers;
    private final ImportType importType;
    private final AssemblageService assemblageService = Get.assemblageService();
    private final IdentifierService identifierService = Get.identifierService();
    private final StampService stampService = Get.stampService();
    private final ImportStreamType streamType;
    private final int VARIABLE_FIELD_START;

    public SRFBrittleAssemblageWriter(List<String[]> refsetRecords, Semaphore writeSemaphore,
                                      ImportStreamType streamType, String message, ImportType importType) {
        this.refsetRecords = refsetRecords;
        this.writeSemaphore = writeSemaphore;
        this.streamType = streamType;
        this.importType = importType;

        this.writeSemaphore.acquireUninterruptibly();
        this.VARIABLE_FIELD_START = SRF_VARIABLE_FIELD_START;
        indexers = LookupService.get().getAllServices(IndexBuilderService.class);
        updateTitle("Importing semantic batch of size: " + refsetRecords.size());
        updateMessage(message);
        addToTotalWork(refsetRecords.size());
        Get.activeTasks().add(this);
    }

    private void index(Chronology chronicle) {
        for (IndexBuilderService indexer: indexers) {
            indexer.indexNow(chronicle);
        }
    }

    int nidFromSctid(String sctid) {
        try {
            return identifierService.getNidForUuids(UuidT3Generator.fromSNOMED(sctid));
        } catch (NoSuchElementException e) {
            LOG.error("The SCTID {} was mapped to UUID {} but that UUID has not been loaded into the system", sctid, UuidT3Generator.fromSNOMED(sctid), e);
            throw e;
        }
    }

    @Override
    protected Void call() throws Exception {

        try {

            List<String[]> noSuchElementList = new ArrayList<>();
            boolean skippedAny = false;

            for (String[] refsetRecord : refsetRecords) {
                try {

                    UUID referencedComponentUuid = UUID.fromString(refsetRecord[SRF_REFERENCED_COMPONENT_ID_INDEX]);
                    final Status state = Status.fromZeroOneToken(refsetRecord[SRF_STATUS_INDEX]);
                    if (importType == ImportType.SNAPSHOT_ACTIVE_ONLY) {
                        if (state == Status.INACTIVE) {
                            continue;
                        }
                        // if the referenced component not previously imported, may
                        // have been inactive, so don't import.
                        if (!identifierService.hasUuid(referencedComponentUuid)) {
                            if (!skippedAny) {
                                skippedAny = true;
                                StringBuilder builder = new StringBuilder();
                                int assemblageNid = identifierService.getNidForUuids(UUID.fromString(refsetRecord[SRF_ASSEMBLAGE_ID_INDEX]));
                                builder.append("Skipping at least one record in: ");
                                builder.append(Get.conceptDescriptionText(assemblageNid));
                                builder.append("\n");
                                builder.append(Arrays.toString(refsetRecord));
                                LOG.warn(builder.toString());
                            }
                            continue;
                        }
                    }

                    int authorNid = identifierService.getNidForUuids(UUID.fromString(refsetRecord[SRF_AUTHOR_INDEX]));
                    int pathNid = identifierService.getNidForUuids(UUID.fromString(refsetRecord[SRF_PATH_INDEX]));
                    UUID elementUuid = UUID.fromString(refsetRecord[SRF_ID_INDEX]);
                    int moduleNid = identifierService.getNidForUuids(UUID.fromString(refsetRecord[SRF_MODULE_INDEX]));
                    int assemblageNid = identifierService.getNidForUuids(UUID.fromString(refsetRecord[SRF_ASSEMBLAGE_ID_INDEX]));
                    int referencedComponentNid = identifierService.getNidForUuids(UUID.fromString(refsetRecord[SRF_REFERENCED_COMPONENT_ID_INDEX]));
                    TemporalAccessor accessor = DateTimeFormatter.ISO_INSTANT.parse(DirectImporter.getIsoInstant(refsetRecord[SRF_TIME_INDEX]));
                    long time = accessor.getLong(INSTANT_SECONDS) * 1000;
                    int versionStamp = stampService.getStampSequence(state, time, authorNid, moduleNid, pathNid);

                    SemanticChronologyImpl assemblageMemberToWrite = new SemanticChronologyImpl(
                            this.streamType.getSemanticVersionType(),
                            elementUuid,
                            assemblageNid,
                            referencedComponentNid);

                    switch (this.streamType) {
                        case SRF_NID1_NID2_INT3_ASSEMBLAGE:
                            addVersionNID1_NID2_INT3_REFSET(assemblageMemberToWrite, versionStamp, refsetRecord);
                            break;

                        case SRF_NID1_INT2_ASSEMBLAGE:
                            addVersionNID1_INT2_REFSET(assemblageMemberToWrite, versionStamp, refsetRecord);
                            break;

                        case SRF_NID1_INT2_STR3_STR4_NID5_NID6_ASSEMBLAGE:
                            addVersionNID1_INT2_STR3_STR4_NID5_NID6_REFSET(assemblageMemberToWrite, versionStamp, refsetRecord);
                            break;

                        case SRF_NID1_ASSEMBLAGE:
                            addVersionNID1_REFSET(assemblageMemberToWrite, versionStamp, refsetRecord);
                            break;

                        case SRF_STR1_STR2_NID3_NID4_ASSEMBLAGE:
                            addVersionSTR1_STR2_NID3_NID4_REFSET(assemblageMemberToWrite, versionStamp, refsetRecord);
                            break;

                        case SRF_STR1_STR2_ASSEMBLAGE:
                            addVersionSTR1_STR2_REFSET(assemblageMemberToWrite, versionStamp, refsetRecord);
                            break;

                        case SRF_STR1_STR2_STR3_STR4_STR5_STR6_STR7_ASSEMBLAGE:
                            addVersionSTR1_STR2_STR3_STR4_STR5_STR6_STR7_REFSET(assemblageMemberToWrite, versionStamp, refsetRecord);
                            break;

                        case SRF_MEMBER_ASSEMBLAGE:
                            addVersionMEMBER_REFSET(assemblageMemberToWrite, versionStamp, refsetRecord);
                            break;

                        case SRF_INT1_INT2_STR3_STR4_STR5_NID6_NID7_ASSEMBLAGE:
                            addVersionINT1_INT2_STR3_STR4_STR5_NID6_NID7_REFSET(assemblageMemberToWrite, versionStamp, refsetRecord);
                            break;

                        case SRF_STR1_ASSEMBLAGE:
                            addVersionSTR1_REFSET(assemblageMemberToWrite, versionStamp, refsetRecord);
                            break;

                        case SRF_NID1_NID2_ASSEMBLAGE:
                            addVersionNID1_NID2_REFSET(assemblageMemberToWrite, versionStamp, refsetRecord);
                            break;

                        case SRF_NID1_NID2_STR3_ASSEMBLAGE:
                            addVersionNID1_NID2_STR3_REFSET(assemblageMemberToWrite, versionStamp, refsetRecord);
                            break;

                        case SRF_NID1_STR2_ASSEMBLAGE:
                            addVersionNID1_STR2_REFSET(assemblageMemberToWrite, versionStamp, refsetRecord);
                            break;

                        case SRF_INT1_ASSEMBLAGE:
                            addVersionINT1_REFSET(assemblageMemberToWrite, versionStamp, refsetRecord);
                            break;

                        case SRF_STR1_NID2_NID3_NID4_ASSEMBLAGE:
                            addVersionSTR1_NID2_NID3_NID4_REFSET(assemblageMemberToWrite, versionStamp, refsetRecord);
                            break;

                        case SRF_STR1_STR2_NID3_NID4_NID5_ASSEMBLAGE:
                            addVersionSTR1_STR2_NID3_NID4_NID5_REFSET(assemblageMemberToWrite, versionStamp, refsetRecord);
                            break;

                        default:
                            throw new UnsupportedOperationException("Can't handle: " + this.streamType);

                    }

                    index(assemblageMemberToWrite);
                    assemblageService.writeSemanticChronology(assemblageMemberToWrite);

                } catch (NoSuchElementException ex) {
                    ex.printStackTrace();
                }

                completedUnitOfWork();
            }

        } catch (DateTimeParseException dtepE){
            dtepE.printStackTrace();
        } finally {
            this.writeSemaphore.release();
            Get.activeTasks().remove(this);
        }

        return null;

    }

    private void addVersionNID1_NID2_INT3_REFSET(SemanticChronologyImpl refsetMemberToWrite, int versionStamp, String[] refsetRecord) {
        Nid1_Nid2_Int3_VersionImpl brittleVersion = refsetMemberToWrite.createMutableVersion(versionStamp);
        brittleVersion.setNid1(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 1].trim()));
        brittleVersion.setNid2(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 2].trim()));
        brittleVersion.setInt3(Integer.parseInt(refsetRecord[VARIABLE_FIELD_START + 3].trim()));
    }
    private void addVersionNID1_INT2_REFSET(SemanticChronologyImpl refsetMemberToWrite, int versionStamp, String[] refsetRecord) {
        Nid1_Int2_VersionImpl brittleVersion = refsetMemberToWrite.createMutableVersion(versionStamp);
        brittleVersion.setNid1(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 1].trim()));
        brittleVersion.setInt2(Integer.parseInt(refsetRecord[VARIABLE_FIELD_START + 2].trim()));
    }

    private void addVersionNID1_INT2_STR3_STR4_NID5_NID6_REFSET(SemanticChronologyImpl refsetMemberToWrite, int versionStamp, String[] refsetRecord) {
        Nid1_Int2_Str3_Str4_Nid5_Nid6_VersionImpl brittleVersion = refsetMemberToWrite.createMutableVersion(versionStamp);
        brittleVersion.setNid1(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 1].trim()));
        brittleVersion.setInt2(Integer.parseInt(refsetRecord[VARIABLE_FIELD_START + 2].trim()));
        brittleVersion.setStr3(refsetRecord[VARIABLE_FIELD_START + 3]);
        brittleVersion.setStr4(refsetRecord[VARIABLE_FIELD_START + 4]);
        brittleVersion.setNid5(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 5].trim()));
        brittleVersion.setNid6(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 6].trim()));
    }

    private void addVersionNID1_REFSET(SemanticChronologyImpl refsetMemberToWrite, int versionStamp, String[] refsetRecord) {
        ComponentNidVersionImpl brittleVersion = refsetMemberToWrite.createMutableVersion(versionStamp);
        brittleVersion.setComponentNid(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 1].trim()));
    }

    private void addVersionSTR1_STR2_NID3_NID4_REFSET(SemanticChronologyImpl refsetMemberToWrite, int versionStamp, String[] refsetRecord) {
        Str1_Str2_Nid3_Nid4_VersionImpl brittleVersion = refsetMemberToWrite.createMutableVersion(versionStamp);
        brittleVersion.setStr1(refsetRecord[VARIABLE_FIELD_START + 1]);
        brittleVersion.setStr2(refsetRecord[VARIABLE_FIELD_START + 2]);
        brittleVersion.setNid3(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 3].trim()));
        brittleVersion.setNid4(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 4].trim()));
    }

    private void addVersionSTR1_STR2_REFSET(SemanticChronologyImpl refsetMemberToWrite, int versionStamp, String[] refsetRecord) {
        Str1_Str2_VersionImpl brittleVersion = refsetMemberToWrite.createMutableVersion(versionStamp);
        brittleVersion.setStr1(refsetRecord[VARIABLE_FIELD_START + 1]);
        brittleVersion.setStr2(refsetRecord[VARIABLE_FIELD_START + 2]);
    }

    private void addVersionSTR1_STR2_STR3_STR4_STR5_STR6_STR7_REFSET(SemanticChronologyImpl refsetMemberToWrite, int versionStamp, String[] refsetRecord) {
        Str1_Str2_Str3_Str4_Str5_Str6_Str7_VersionImpl brittleVersion = refsetMemberToWrite.createMutableVersion(versionStamp);
        brittleVersion.setStr1(refsetRecord[VARIABLE_FIELD_START + 1]);
        brittleVersion.setStr2(refsetRecord[VARIABLE_FIELD_START + 2]);
        brittleVersion.setStr3(refsetRecord[VARIABLE_FIELD_START + 3]);
        brittleVersion.setStr4(refsetRecord[VARIABLE_FIELD_START + 4]);
        brittleVersion.setStr5(refsetRecord[VARIABLE_FIELD_START + 5]);
        brittleVersion.setStr6(refsetRecord[VARIABLE_FIELD_START + 6]);
        brittleVersion.setStr7(refsetRecord[VARIABLE_FIELD_START + 7]);
    }

    private void addVersionMEMBER_REFSET(SemanticChronologyImpl refsetMemberToWrite, int versionStamp, String[] refsetRecord) {
        refsetMemberToWrite.createMutableVersion(versionStamp);
    }

    private void addVersionINT1_INT2_STR3_STR4_STR5_NID6_NID7_REFSET(SemanticChronologyImpl refsetMemberToWrite, int versionStamp, String[] refsetRecord) {
        Int1_Int2_Str3_Str4_Str5_Nid6_Nid7_VersionImpl brittleVersion = refsetMemberToWrite.createMutableVersion(versionStamp);
        brittleVersion.setInt1(Integer.parseInt(refsetRecord[VARIABLE_FIELD_START + 1].trim()));
        brittleVersion.setInt2(Integer.parseInt(refsetRecord[VARIABLE_FIELD_START + 2].trim()));
        brittleVersion.setStr3(refsetRecord[VARIABLE_FIELD_START + 3]);
        brittleVersion.setStr4(refsetRecord[VARIABLE_FIELD_START + 4]);
        brittleVersion.setStr5(refsetRecord[VARIABLE_FIELD_START + 5]);
        brittleVersion.setNid6(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 6].trim()));
        brittleVersion.setNid7(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 7].trim()));
    }

    private void addVersionSTR1_REFSET(SemanticChronologyImpl refsetMemberToWrite, int versionStamp, String[] refsetRecord) {
        StringVersionImpl brittleVersion = refsetMemberToWrite.createMutableVersion(versionStamp);
        brittleVersion.setString(refsetRecord[VARIABLE_FIELD_START + 1]);
    }

    private void addVersionNID1_NID2_REFSET(SemanticChronologyImpl refsetMemberToWrite, int versionStamp, String[] refsetRecord) {
        Nid1_Nid2_VersionImpl brittleVersion = refsetMemberToWrite.createMutableVersion(versionStamp);
        brittleVersion.setNid1(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 1].trim()));
        brittleVersion.setNid2(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 2].trim()));
    }

    private void addVersionNID1_NID2_STR3_REFSET(SemanticChronologyImpl refsetMemberToWrite, int versionStamp, String[] refsetRecord) {
        Nid1_Nid2_Str3_VersionImpl brittleVersion = refsetMemberToWrite.createMutableVersion(versionStamp);
        brittleVersion.setNid1(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 1].trim()));
        brittleVersion.setNid2(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 2].trim()));
        brittleVersion.setStr3(refsetRecord[VARIABLE_FIELD_START + 3]);
    }

    private void addVersionNID1_STR2_REFSET(SemanticChronologyImpl refsetMemberToWrite, int versionStamp, String[] refsetRecord) {
        Nid1_Str2_VersionImpl brittleVersion = refsetMemberToWrite.createMutableVersion(versionStamp);
        brittleVersion.setNid1(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 1].trim()));
        brittleVersion.setStr2(refsetRecord[VARIABLE_FIELD_START + 2]);
    }

    private void addVersionINT1_REFSET(SemanticChronologyImpl refsetMemberToWrite, int versionStamp, String[] refsetRecord) {
        LongVersionImpl brittleVersion = refsetMemberToWrite.createMutableVersion(versionStamp);
        brittleVersion.setLongValue(Long.parseLong(refsetRecord[VARIABLE_FIELD_START + 1].trim()));
    }

    private void addVersionSTR1_NID2_NID3_NID4_REFSET(SemanticChronologyImpl refsetMemberToWrite, int versionStamp, String[] refsetRecord) {
        Str1_Nid2_Nid3_Nid4_VersionImpl brittleVersion = refsetMemberToWrite.createMutableVersion(versionStamp);
        brittleVersion.setStr1(refsetRecord[VARIABLE_FIELD_START + 1]);
        brittleVersion.setNid2(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 2].trim()));
        brittleVersion.setNid3(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 3].trim()));
        brittleVersion.setNid4(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 4].trim()));
    }

    private void addVersionSTR1_STR2_NID3_NID4_NID5_REFSET(SemanticChronologyImpl refsetMemberToWrite, int versionStamp, String[] refsetRecord) {
        Str1_Str2_Nid3_Nid4_Nid5_VersionImpl brittleVersion = refsetMemberToWrite.createMutableVersion(versionStamp);
        brittleVersion.setStr1(refsetRecord[VARIABLE_FIELD_START + 1]);
        brittleVersion.setStr2(refsetRecord[VARIABLE_FIELD_START + 2]);
        brittleVersion.setNid3(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 3].trim()));
        brittleVersion.setNid4(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 4].trim()));
        brittleVersion.setNid5(nidFromSctid(refsetRecord[VARIABLE_FIELD_START + 5].trim()));
    }
}
