package org.ihtsdo.otf.tcc.dto.component;

import org.ihtsdo.otf.tcc.api.coordinate.Status;

import javax.xml.bind.annotation.XmlAttribute;
import java.util.UUID;

/**
 * Created by kec on 7/26/14.
 */
public class TtkStamp {
    @XmlAttribute
    public long               time                  = Long.MIN_VALUE;
    @XmlAttribute
    public UUID authorUuid;
    @XmlAttribute
    public UUID               pathUuid;
    @XmlAttribute
    public Status status;
    @XmlAttribute
    public UUID               moduleUuid;

    public TtkStamp(Status status, long time, UUID authorUuid, UUID moduleUuid, UUID pathUuid) {
        this.status = status;
        this.time = time;
        this.authorUuid = authorUuid;
        this.moduleUuid = moduleUuid;
        this.pathUuid = pathUuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TtkStamp ttkStamp = (TtkStamp) o;

        if (time != ttkStamp.time) return false;
        if (!authorUuid.equals(ttkStamp.authorUuid)) return false;
        if (!moduleUuid.equals(ttkStamp.moduleUuid)) return false;
        if (!pathUuid.equals(ttkStamp.pathUuid)) return false;
        if (status != ttkStamp.status) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (time ^ (time >>> 32));
        result = 31 * result + authorUuid.hashCode();
        result = 31 * result + pathUuid.hashCode();
        result = 31 * result + status.hashCode();
        result = 31 * result + moduleUuid.hashCode();
        return result;
    }
}
