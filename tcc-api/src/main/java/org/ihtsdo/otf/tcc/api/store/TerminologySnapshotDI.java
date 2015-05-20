package org.ihtsdo.otf.tcc.api.store;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.blueprint.TerminologyBuilderBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentContainerBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptContainerBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;

public interface TerminologySnapshotDI extends TerminologyDI {

   TerminologyBuilderBI getBuilder(EditCoordinate ec);

   Optional<? extends ComponentVersionBI> getComponentVersion(Collection<UUID> uuids) throws IOException, ContradictionException;

   Optional<? extends ComponentVersionBI> getComponentVersion(ComponentContainerBI cc) throws IOException, ContradictionException;

   Optional<? extends ComponentVersionBI> getComponentVersion(int nid) throws IOException, ContradictionException;

   Optional<? extends ComponentVersionBI> getComponentVersion(UUID... uuids) throws IOException, ContradictionException;

   ConceptVersionBI getConceptForNid(int nid) throws IOException;

   ConceptVersionBI getConceptVersion(Collection<UUID> uuids) throws IOException;

   ConceptVersionBI getConceptVersion(ConceptContainerBI cc) throws IOException;

   ConceptVersionBI getConceptVersion(int cNid) throws IOException;

   ConceptVersionBI getConceptVersion(UUID... uuids) throws IOException;

   Map<Integer, ConceptVersionBI> getConceptVersions(NativeIdSetBI cNids) throws IOException;

   int[] getPossibleChildren(int cNid) throws IOException;

   ViewCoordinate getViewCoordinate();

   int getConceptNidForNid(Integer nid);
   
   boolean isKindOf(int childNid, int parentNid) throws IOException, ContradictionException;
}
