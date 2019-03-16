package sh.isaac.solor.direct.clinvar.model.fields;

import sh.isaac.api.Status;

import java.util.UUID;

/**
 * 2019-03-07
 * aks8m - https://github.com/aks8m
 */
public interface CoreFields {

    String getID();
    void setID(String id);

    UUID getUUID();
    void setUUID(UUID uuid);

    Status getStatus();
    void setStatus(Status status);

    long getTime();
    void setTime(long time);

    int getAuthor();
    void setAuthor(int author);

    int getModule();
    void setModule(int module);

    int getPath();
    void setPath(int path);

}
