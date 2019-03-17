package sh.isaac.solor.direct.clinvar.model.fields;

import sh.isaac.api.Status;

import java.util.UUID;

/**
 * 2019-03-07
 * aks8m - https://github.com/aks8m
 */
public interface ComponentFields {

    UUID getComponentUUID();

    Status getStatus();

    long getTime();

    int getAuthorNid();

    int getModuleNid();

    int getPathNid();

}
