package sh.isaac.api;

import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.identity.IdentifiedObject;
import sh.isaac.api.util.StringUtils;
import sh.isaac.api.util.UUIDUtil;

import java.util.*;

import static sh.isaac.api.component.concept.ConceptSpecification.FIELD_SEPARATOR;

public class ComponentProxy implements IdentifiedObject {
    /**
     * Universal identifiers for the concept proxied by the is object.
     */
    private UUID[] uuids;

    /**
     * The fully qualified name for this object.
     */
    private final String componentString;

    private int cachedNid = 0;

    public ComponentProxy(int componentNid, String componentString) {
        this.cachedNid = componentNid;
        this.componentString = componentString;
    }

    public ComponentProxy(ConceptSpecification conceptSpecification) {
        this(conceptSpecification.toExternalString());
    }

    public ComponentProxy(String componentString, UUID[] uuids) {
        this.uuids = uuids;
        this.componentString = componentString;
    }

    public ComponentProxy(String externalString) {

        final String[] parts = StringUtils.split(externalString, FIELD_SEPARATOR);

        int partIndex = 0;

        this.componentString = parts[partIndex++];

        final List<UUID> uuidList = new ArrayList<>(parts.length - partIndex);

        for (int i = partIndex; i < parts.length; i++) {
            if (UUIDUtil.isUUID(parts[i])) {
                uuidList.add(UUID.fromString(parts[i]));
            }
        }

        if (uuidList.size() < 1) {
            throw new IllegalStateException("No uuids specified in: " + externalString);
        }

        this.uuids = uuidList.toArray(new UUID[uuidList.size()]);
    }

    /**
     * Gets the nid.
     *
     * @return the nid
     */
    @Override
    public int getNid() throws NoSuchElementException {
        if (cachedNid == 0) {
            try {
                cachedNid = Get.identifierService().getNidForUuids(getPrimordialUuid());
            }
            catch (NoSuchElementException e) {
                //This it to help me bootstrap the system... normally, all metadata will be pre-assigned by the IdentifierProvider upon startup.
                cachedNid = Get.identifierService().assignNid(getUuids());
            }
        }
        return cachedNid;
    }

    @Override
    public UUID getPrimordialUuid() {
        if ((this.getUuids() == null) || (this.uuids.length < 1)) {
            return null;
        } else {
            return this.uuids[0];
        }
    }

    /**
     * Gets the uuid list.
     *
     * @return the uuid list
     */
    @Override
    public List<UUID> getUuidList() {
        return Arrays.asList(this.getUuids());
    }

    /**
     * Gets the universal identifiers for the concept proxied by the is object.
     *
     * @return the universal identifiers for the concept proxied by the is object
     */
    @Override

    public UUID[] getUuids() {
        if (this.uuids == null) {
            this.uuids = Get.identifierService().getUuidArrayForNid(cachedNid);
        }
        return this.uuids;
    }

    public String getComponentString() {
        return componentString;
    }

    public String toExternalString() {
        final StringBuilder sb = new StringBuilder();

        sb.append(componentString);
        getUuidList().stream().forEach((uuid) -> {
            sb.append(FIELD_SEPARATOR)
                    .append(uuid.toString());
        });
        return sb.toString();

    }

    @Override
    public String toString() {
        return "ComponentProxy{" + componentString + ", " + Arrays.toString(getUuids()) +
                '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + this.getNid();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof IdentifiedObject)) {
            return false;
        }
        final IdentifiedObject other = (IdentifiedObject) obj;
        return this.getNid() == other.getNid();
     }

}
