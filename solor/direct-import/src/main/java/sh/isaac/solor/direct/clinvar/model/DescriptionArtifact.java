package sh.isaac.solor.direct.clinvar.model;

import sh.isaac.api.Status;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.configuration.LanguageCoordinates;
import sh.isaac.solor.direct.clinvar.model.fields.ComponentFields;
import sh.isaac.solor.direct.clinvar.model.fields.DescriptionFields;
import sh.isaac.solor.direct.clinvar.model.fields.IdentifierFields;

import java.util.UUID;

/**
 * 2019-03-07
 * aks8m - https://github.com/aks8m
 */
public final class DescriptionArtifact implements ComponentFields, DescriptionFields, IdentifierFields {

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

    @Override
    public UUID getReferencedComponentUUID() {
        return this.referencedComponentUUID;
    }

    @Override
    public int getLanguageConceptNid() {
        return LanguageCoordinates.iso639toConceptNid(this.iso639LanguageCode);
    }

    @Override
    public int getDescriptionAssemblageNid() {
        return LanguageCoordinates.iso639toDescriptionAssemblageNid(this.iso639LanguageCode);
    }

    @Override
    public int getTypeNid() {
        return this.typeNid;
    }

    @Override
    public String getTerm() {
        return this.term;
    }

    @Override
    public int getCaseSignificanceNid() {
        return this.caseSignificanceNid;
    }

    @Override
    public UUID getIdentifierComponentUUID() {
        return UuidT5Generator.get(this.identifierAssemblageUUID, this.identifierValue);
    }

    @Override
    public String getIdentifierValue() {
        return this.identifierValue;
    }

    @Override
    public UUID getIdentifierAssemblageUUID() {
        return this.identifierAssemblageUUID;
    }

    @Override
    public int hashCode() {
        return this.getComponentUUID().hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if(obj instanceof DescriptionArtifact)
            return this.hashCode() == obj.hashCode();
        else
            return false;
    }
}
