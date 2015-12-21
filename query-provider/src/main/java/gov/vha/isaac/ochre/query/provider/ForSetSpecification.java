package gov.vha.isaac.ochre.query.provider;


import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.collections.NidSet;
import gov.vha.isaac.ochre.collections.SememeSequenceSet;
import javax.xml.bind.annotation.*;
import java.io.IOException;
import java.util.*;

/**
 * Created by kec on 11/2/14.
 */
@XmlRootElement(name = "for-set")
@XmlAccessorType(value = XmlAccessType.NONE)
public class ForSetSpecification {
    @XmlElementWrapper(name = "for")
    @XmlElement(name = "component")
    private List<ComponentCollectionTypes> forCollectionTypes = new ArrayList<>();

    @XmlElementWrapper(name = "custom-for")
    @XmlElement(name = "uuid")
    private Set<UUID> customCollection = new HashSet<>();

    public ForSetSpecification() {
    }

    public ForSetSpecification(ComponentCollectionTypes... forCollectionTypes) {
        this.forCollectionTypes.addAll(Arrays.asList(forCollectionTypes));
    }

    public List<ComponentCollectionTypes> getForCollectionTypes() {
        return forCollectionTypes;
    }

    public void setForCollectionTypes(List<ComponentCollectionTypes> forCollectionTypes) {
        this.forCollectionTypes = forCollectionTypes;
    }

    public Set<UUID> getCustomCollection() {
        return customCollection;
    }

    public void setCustomCollection(Set<UUID> customCollection) {
        this.customCollection = customCollection;
    }

    public NidSet getCollection() throws IOException {
        NidSet forSet = NidSet.of();
        for (ComponentCollectionTypes collection : forCollectionTypes) {
            switch (collection) {
                case ALL_COMPONENTS:
                    forSet.or(NidSet.of(Get.identifierService().getComponentNidStream()));
                    break;
                case ALL_CONCEPTS:
                    forSet.or(NidSet.of(ConceptSequenceSet.of(Get.identifierService().getConceptSequenceStream())));
                    break;
                case ALL_SEMEMES:
                    forSet.or(NidSet.of(SememeSequenceSet.of(Get.identifierService().getSememeSequenceStream())));
                    break;
                case CUSTOM_SET:
                    for (UUID uuid : customCollection) {
                        forSet.add(Get.identifierService().getNidForUuids(uuid));
                    }
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }
        return forSet;

    }
}
