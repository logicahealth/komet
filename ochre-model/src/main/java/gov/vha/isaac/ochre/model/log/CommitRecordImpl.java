/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.model.log;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.commit.CommitRecord;
import gov.vha.isaac.ochre.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.collections.SememeSequenceSet;
import gov.vha.isaac.ochre.collections.StampSequenceSet;
import gov.vha.isaac.ochre.model.ByteArrayDataBuffer;
import gov.vha.isaac.ochre.model.OchreExternalizable;
import gov.vha.isaac.ochre.model.OchreExternalizableObjectType;
import java.time.Instant;
import java.util.UUID;
import org.apache.mahout.math.map.OpenIntIntHashMap;

/**
 *
 * @author kec
 */
public class CommitRecordImpl extends CommitRecord implements OchreExternalizable {
    
    public CommitRecordImpl(Instant commitTime,
            StampSequenceSet stampsInCommit,
            OpenIntIntHashMap stampAliases,
            ConceptSequenceSet conceptsInCommit,
            SememeSequenceSet sememesInCommit,
            String commitComment) {
        super(commitTime, stampsInCommit, stampAliases, conceptsInCommit, sememesInCommit, commitComment);
    }
    
    public CommitRecordImpl(short dataFormatVersion, ByteArrayDataBuffer in) {
        switch (in.getObjectDataFormatVersion()) {
            case 1:
                break;
            default:
                throw new RuntimeException("Can't handle formt version: " + in.getObjectDataFormatVersion());
        }
        this.commitTime = Instant.ofEpochSecond(in.getLong(), in.getLong());
        
        int stampsInCommitCount = in.getInt();
        this.stampsInCommit = new StampSequenceSet();
        for (int i = 0; i < stampsInCommitCount; i++) {
            StampUniversal stamp = new StampUniversal(in);
            stampsInCommit.add(stamp.getStampSequence());
        }
        
        int stampAliasesCount = in.getInt();
        this.stampAliases = new OpenIntIntHashMap(stampAliasesCount);
        for (int i = 0; i < stampAliasesCount; i++) {
            StampUniversal stampAlias = new StampUniversal(in);
            StampUniversal primordialStamp = new StampUniversal(in);
            stampAliases.put(stampAlias.getStampSequence(), primordialStamp.getStampSequence());
        }
        
        int conceptsInCommitCount = in.getInt();
        this.conceptsInCommit = new ConceptSequenceSet();
        for (int i = 0; i < conceptsInCommitCount; i++) {
            this.conceptsInCommit.add(new UUID(in.getLong(), in.getLong()));
        }
        
        int sememesInCommitCount = in.getInt();
        this.sememesInCommit = new SememeSequenceSet();
        for (int i = 0; i < sememesInCommitCount; i++) {
            this.sememesInCommit.add(new UUID(in.getLong(), in.getLong()));
        }
        
        this.commitComment = in.readUTF();
    }
    
    @Override
    public void putExternal(ByteArrayDataBuffer out) {
        out.putLong(this.commitTime.getEpochSecond());
        out.putLong(this.commitTime.getNano());
        
        out.putInt(stampsInCommit.size());
        stampsInCommit.stream().forEach((stampSequence) -> StampUniversal.get(stampSequence).writeExternal(out));
        
        out.putInt(stampAliases.size());
        stampAliases.forEachPair((aliasStampSequence, primordialStampSequence) -> {
            StampUniversal.get(aliasStampSequence).writeExternal(out);
            StampUniversal.get(primordialStampSequence).writeExternal(out);
            return true;
        });
        
        out.putInt(conceptsInCommit.size());
        conceptsInCommit.stream().forEach((conceptSequence) -> {
            UUID conceptUuid = Get.identifierService().getUuidPrimordialFromConceptSequence(conceptSequence).get();
            out.putLong(conceptUuid.getMostSignificantBits());
            out.putLong(conceptUuid.getLeastSignificantBits());
        });
        
        out.putInt(sememesInCommit.size());
        sememesInCommit.stream().forEach((sememeSequence) -> {
            UUID conceptUuid = Get.identifierService().getUuidPrimordialFromSememeSequence(sememeSequence).get();
            out.putLong(conceptUuid.getMostSignificantBits());
            out.putLong(conceptUuid.getLeastSignificantBits());
        });
        
        out.putUTF(commitComment);
    }
    
    @Override
    public byte getDataFormatVersion() {
        return 1;
    }
    
    @Override
    public OchreExternalizableObjectType getOchreObjectType() {
        return OchreExternalizableObjectType.COMMIT_RECORD;
    }
    
}
