package sh.isaac.solor.direct.generic.artifact;

import sh.isaac.api.Status;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.configuration.LanguageCoordinates;
import sh.isaac.utility.LanguageMap;
import java.util.UUID;

/**
 * 2019-03-07
 * aks8m - https://github.com/aks8m
 */
public final class DescriptionArtifact implements GenericArtifact {

    //Concept Fields
    private final Status status;
    private final long time;
    private final int authorNid;
    private final int moduleNid;
    private final int pathNid;

    //Description Fields
    private final UUID referencedComponentUUID;
    private final String iso639LanguageCode;
    private final int typeNid;
    private final String term;
    private final int caseSignificanceNid;

    //Identifier Fields
    private final String identifierValue;
    private final UUID identifierAssemblageUUID;

    public DescriptionArtifact(
            Status status,
            long time,
            int authorNid,
            int moduleNid,
            int pathNid,
            UUID referencedComponentUUID,
            String iso639LanguageCode,
            int typeNid,
            String term,
            int caseSignificanceNid,
            String identifierValue,
            UUID identifierAssemblageUUID) {

        this.status = status;
        this.time = time;
        this.authorNid = authorNid;
        this.moduleNid = moduleNid;
        this.pathNid = pathNid;
        this.referencedComponentUUID = referencedComponentUUID;
        this.iso639LanguageCode = iso639LanguageCode;
        this.typeNid = typeNid;
        this.term = term;
        this.caseSignificanceNid = caseSignificanceNid;
        this.identifierValue = identifierValue;
        this.identifierAssemblageUUID = identifierAssemblageUUID;
    }

    @Override
    public UUID getComponentUUID() {
        return UuidT5Generator.get(this.term);
    }

    public Status getStatus() {
        return this.status;
    }

    public long getTime() {
        return this.time;
    }

    public int getAuthorNid() {
        return this.authorNid;
    }

    public int getModuleNid() {
        return this.moduleNid;
    }

    public int getPathNid() {
        return this.pathNid;
    }

    public UUID getReferencedComponentUUID() {
        return this.referencedComponentUUID;
    }

    public int getLanguageConceptNid() {
        return LanguageMap.iso639toConceptNid(this.iso639LanguageCode);
    }

    public int getDescriptionAssemblageNid() {
        return LanguageMap.iso639toDescriptionAssemblageNid(this.iso639LanguageCode);
    }

    public int getTypeNid() {
        return this.typeNid;
    }

    public String getTerm() {
        return this.term;
    }

    public int getCaseSignificanceNid() {
        return this.caseSignificanceNid;
    }

    public UUID getIdentifierComponentUUID() {
        return UuidT5Generator.get(this.identifierAssemblageUUID, this.identifierValue);
    }

    public String getIdentifierValue() {
        return this.identifierValue;
    }

    public UUID getIdentifierAssemblageUUID() {
        return this.identifierAssemblageUUID;
    }

    @Override
    public int hashCode() {
        return this.getComponentUUID().hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if(obj instanceof DescriptionArtifact){
            return this.getComponentUUID().equals(((DescriptionArtifact) obj).getComponentUUID());
        }else {
            return false;
        }
    }
}
