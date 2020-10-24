package sh.isaac.model;

import org.jvnet.hk2.annotations.Contract;
import sh.isaac.api.AssemblageService;
import sh.isaac.api.IdentifiedObjectService;
import sh.isaac.api.component.concept.ConceptService;

@Contract
public interface ChronologyService extends ConceptService, AssemblageService, IdentifiedObjectService {
    long getWriteSequence();
}
