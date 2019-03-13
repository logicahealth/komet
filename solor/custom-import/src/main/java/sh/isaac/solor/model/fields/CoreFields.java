package sh.isaac.solor.model.fields;

import sh.isaac.api.Status;

/**
 * 2019-03-07
 * aks8m - https://github.com/aks8m
 */
public interface CoreFields {

    String getID();
    void setID(String id);

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
