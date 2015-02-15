package org.ihtsdo.otf.tcc.api.relationship.group;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

public interface RelGroupVersionBI extends RelGroupChronicleBI, ComponentVersionBI {
   Collection<? extends RelationshipVersionBI> getAllCurrentRelVersions();

   Collection<? extends RelationshipVersionBI> getAllRels() throws ContradictionException;

   Collection<? extends RelationshipVersionBI> getCurrentRels() throws ContradictionException;
}
