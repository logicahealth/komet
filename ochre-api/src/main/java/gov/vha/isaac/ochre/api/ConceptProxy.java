package gov.vha.isaac.ochre.api;

import javax.xml.bind.annotation.XmlTransient;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by kec on 2/16/15.
 */
public class ConceptProxy {
    /** Universal identifiers for the concept proxied by the is object */
    protected UUID[] uuids;
    /** Description of the concept proxied by this object */
    protected String description;

    public ConceptProxy() {
    }

    public ConceptProxy(String description, UUID... uuids) {
        this.uuids       = uuids;
        this.description = description;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Arrays.deepHashCode(this.uuids);
        hash = 79 * hash + Objects.hashCode(this.description);
        return hash;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public String toString() {
        if (uuids != null) {
           return "ConceptSpec{" + description + "; " + Arrays.asList(uuids) + "}";
        }
        return "ConceptSpec{" + description + "; null UUIDs}";
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getDescription() {
       return description;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @XmlTransient
    public UUID[] getUuids() {
       return uuids;
    }

    /**
     * added as an alternative way to get the uuids as strings rather than UUID
     * objects
     * this was done to help with Maven making use of this class
     *
     * @return
     */
    public String[] getUuidsAsString() {
       String[] returnVal = new String[uuids.length];
       int      i         = 0;

       for (UUID uuid : uuids) {
          returnVal[i++] = uuid.toString();
       }

       return returnVal;
    }

    /**
     * Method description
     *
     *
     * @param description
     */
    public void setDescription(String description) {
       this.description = description;
    }

    /**
     * Method description
     *
     *
     * @param uuids
     */
    public void setUuids(UUID[] uuids) {
       this.uuids = uuids;
    }

    /**
     * Added primarily for Maven so that using a String type configuration in
     * a POM file the UUIDs array could be set.
     * This allows the ConceptSpec class to be embedded into a object to be configured
     * by Maven POM configuration.
     *
     * @param uuids
     */
    public void setUuidsAsString(String[] uuids) {
       this.uuids = new UUID[uuids.length];

       int i = 0;

       for (String uuid : uuids) {
          this.uuids[i++] = UUID.fromString(uuid);
       }
    }
}
