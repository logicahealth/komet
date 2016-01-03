/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api.externalizable;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.IdentifierService;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.commit.CommitService;
import gov.vha.isaac.ochre.api.commit.StampService;
import gov.vha.isaac.ochre.api.externalizable.ByteArrayDataBuffer;
import java.util.UUID;
import javax.xml.bind.annotation.XmlAttribute;

/**
 *
 * @author kec
 */
public class StampUniversal {
    
    public static StampUniversal get(int stampSequence) {
        return new StampUniversal(stampSequence);
    }
    public static StampUniversal get(ByteArrayDataBuffer in) {
        return new StampUniversal(in);
    }

    @XmlAttribute
    public State status;

    @XmlAttribute
    public long time;
    @XmlAttribute
    public UUID authorUuid;
    @XmlAttribute
    public UUID moduleUuid;
    @XmlAttribute
    public UUID pathUuid;

    public StampUniversal(ByteArrayDataBuffer in) {
        this.status = State.getFromBoolean(in.getBoolean());
        this.time = in.getLong();
        this.authorUuid = new UUID(in.getLong(), in.getLong());
        this.moduleUuid = new UUID(in.getLong(), in.getLong());
        this.pathUuid = new UUID(in.getLong(), in.getLong());
    }

    public StampUniversal(int stamp) {
        StampService stampService = Get.stampService();
        IdentifierService idService = Get.identifierService();
        this.status = stampService.getStatusForStamp(stamp);
        this.time = stampService.getTimeForStamp(stamp);
        this.authorUuid = idService.getUuidPrimordialFromConceptSequence(stampService.getAuthorSequenceForStamp(stamp)).get();
        this.moduleUuid = idService.getUuidPrimordialFromConceptSequence(stampService.getModuleSequenceForStamp(stamp)).get();
        this.pathUuid = idService.getUuidPrimordialFromConceptSequence(stampService.getPathSequenceForStamp(stamp)).get();
    }

    public void writeExternal(ByteArrayDataBuffer out) {
        out.putBoolean(this.status.getBoolean());
        out.putLong(time);
        out.putLong(this.authorUuid.getMostSignificantBits());
        out.putLong(this.authorUuid.getLeastSignificantBits());
        out.putLong(this.moduleUuid.getMostSignificantBits());
        out.putLong(this.moduleUuid.getLeastSignificantBits());
        out.putLong(this.pathUuid.getMostSignificantBits());
        out.putLong(this.pathUuid.getLeastSignificantBits());
    }
    
    public State getStatus() {
        return status;
    }

    public long getTime() {
        return time;
    }

    public UUID getAuthorUuid() {
        return authorUuid;
    }

    public UUID getModuleUuid() {
        return moduleUuid;
    }

    public UUID getPathUuid() {
        return pathUuid;
    }
    
    public int getStampSequence() {
        IdentifierService idService = Get.identifierService();
        return Get.stampService().getStampSequence(status, time,
                idService.getConceptSequenceForUuids(this.authorUuid), 
                idService.getConceptSequenceForUuids(this.moduleUuid), 
                idService.getConceptSequenceForUuids(this.pathUuid));
    }
}
