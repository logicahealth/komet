package sh.isaac.solor.direct.umls.model;

import sh.isaac.api.Status;
import sh.isaac.solor.direct.clinvar.model.fields.ComponentFields;

import java.util.UUID;

/**
 * 4/10/2019
 *
 * @author kmaulden
 */
public class EquivalencyArtifact implements ComponentFields {

    private final Status status;
    private final long time;
    private final int authorNid;
    private final int moduleNid;
    private final int pathNid;

    private final int sourceNid;
    private final int targetNid;

    private final UUID equivalencyAssemblage;

    private final String cui;

    public EquivalencyArtifact(
            Status status,
            long time,
            int authorNid,
            int moduleNid,
            int pathNid,
            int sourceNid,
            int targetNid,
            UUID equivalencyAssemblage, String cui) {
        this.status = status;
        this.time = time;
        this.authorNid = authorNid;
        this.moduleNid = moduleNid;
        this.pathNid = pathNid;
        this.sourceNid = sourceNid;
        this.targetNid = targetNid;
        this.equivalencyAssemblage = equivalencyAssemblage;
        this.cui = cui;
    }

    //TODO: what is the componentUUID here?
    @Override
    public UUID getComponentUUID() {
        return null;
    }

    @Override
    public Status getStatus() {
        return this.status;
    }

    @Override
    public long getTime() {
        return this.time;
    }

    @Override
    public int getAuthorNid() {
        return this.authorNid;
    }

    @Override
    public int getModuleNid() {
        return this.moduleNid;
    }

    @Override
    public int getPathNid() {
        return this.pathNid;
    }

    public int getSourceNid() { return this.sourceNid; }

    public int getTargetNid() { return this.targetNid; }

    public UUID getEquivalencyAssemblageUUID() { return this.equivalencyAssemblage; }

    public String getCui() { return cui; }
}
