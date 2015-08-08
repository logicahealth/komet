package gov.vha.isaac.ochre.api;

import gov.vha.isaac.ochre.api.component.concept.ConceptSpecification;
import java.util.ArrayList;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by kec on 2/16/15.
 */
public class ConceptProxy implements ConceptSpecification {

    protected static int getConceptSequence(int nid) {
        return Get.identifierService().getConceptSequence(nid);
    }
    
    /** Universal identifiers for the concept proxied by the is object */
    protected UUID[] uuids;
    /** A description of the concept proxied by this object */
    protected String description;

    public ConceptProxy() {
    }

    public ConceptProxy(String description, UUID... uuids) {
        this.uuids       = uuids;
        this.description = description;
    }
    public ConceptProxy(String externalString) {
        String[] parts = externalString.split(FIELD_SEPARATOR);
        this.description = parts[0];
        List<UUID> uuidList = new ArrayList<>(parts.length - 1);
        for (int i = 1; i < parts.length; i++) {
            uuidList.add(UUID.fromString(parts[i]));
        }
        if (uuidList.size() < 1) {
            throw new IllegalStateException("No uuids specified in: " 
                    + externalString);
        }
        this.uuids = uuidList.toArray(new UUID[uuidList.size()]);
    }


    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Arrays.deepHashCode(this.uuids);
        hash = 79 * hash + Objects.hashCode(this.description);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ConceptProxy other = (ConceptProxy) obj;
        return Arrays.stream(uuids).anyMatch((UUID objUuid) -> {
            return Arrays.stream(other.uuids).anyMatch((otherUuid) ->{
                return objUuid.equals(otherUuid);
            });
        });
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
           return "ConceptProxy{" + description + "; " + Arrays.asList(uuids) + "}";
        }
        return "ConceptProxy{" + description + "; null UUIDs}";
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public String getConceptDescriptionText() {
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

    @Override
    public List<UUID> getUuidList() {
        return Arrays.asList(uuids);
    }
    
    /**
     * added as an alternative way to get the primary UUID - since most users of a concept spec
     * only have one UUID, and only care about one UUID.
     *
     * @return the first UUID in the UUID list, or null, if not present
     */
    @XmlTransient
    public UUID getPrimodialUuid() {
       if (uuids == null || uuids.length < 1)
       {
           return null;
       }
       else
       {
           return uuids[0];
       }
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
    
    public int getNid() {
        return Get.identifierService().getNidForUuids(uuids);
    }
}
