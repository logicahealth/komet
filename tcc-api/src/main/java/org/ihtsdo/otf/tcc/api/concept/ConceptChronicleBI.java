package org.ihtsdo.otf.tcc.api.concept;

//~--- non-JDK imports --------------------------------------------------------

import java.io.DataOutput;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.otf.tcc.api.changeset.ChangeSetGenerationThreadingPolicy;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeChronicleBI;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.media.MediaChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.group.RelGroupVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;
import org.ihtsdo.otf.tcc.api.chronicle.ProcessComponentChronicleBI;

public interface ConceptChronicleBI extends ComponentChronicleBI<ConceptVersionBI> {
    
   void writeExternal(DataOutput out) throws IOException;
   void cancel() throws IOException;

   boolean commit(ChangeSetGenerationPolicy changeSetPolicy,
                  ChangeSetGenerationThreadingPolicy changeSetWriterThreading)
           throws IOException;

   /**
    * Returns a longer - more complete - string representation of the chronicle.
    * Useful for diagnostic purposes.
    *
    * @return
    */
   String toLongString();

   //~--- get methods ---------------------------------------------------------

   ConceptAttributeChronicleBI getConceptAttributes() throws IOException;

   RefexVersionBI<?> getCurrentRefsetMemberForComponent(ViewCoordinate vc, int componentNid)
           throws IOException;
   
   ComponentChronicleBI<?> getComponent(int nid) throws IOException;
   


   Collection<? extends RefexVersionBI<?>> getCurrentRefsetMembers(ViewCoordinate vc) throws IOException;

   /**
     * Retrieves tuples matching the specified view coordinate
     * 
     * @param cuttoffTime
     *          cutoff time to match tuples, tuples with a time greater than
     *          cutoff will no be returned
     * @return List of matching tuples
     * @throws TerminologyException
     */
   public Collection<? extends RefexVersionBI<?>> getCurrentRefsetMembers(ViewCoordinate vc, Long cutoffTime)
           throws IOException;

   Collection<? extends DescriptionChronicleBI> getDescriptions() throws IOException;

   long getLastModificationSequence();

   Collection<? extends MediaChronicleBI> getMedia() throws IOException;

   RefexChronicleBI<?> getRefsetMemberForComponent(int componentNid) throws IOException;

   Collection<? extends RefexChronicleBI<?>> getRefsetMembers() throws IOException;

   Collection<? extends RelGroupVersionBI> getRelationshipGroupsActive(ViewCoordinate vc)
           throws IOException, ContradictionException;

   Collection<? extends RelationshipChronicleBI> getRelationshipsIncoming() throws IOException;

   Collection<? extends RelationshipChronicleBI> getRelationshipsOutgoing() throws IOException;

   boolean hasCurrentRefsetMemberForComponent(ViewCoordinate vc, int componentNid) throws IOException;

   boolean isAnnotationStyleRefex() throws IOException;

   //~--- set methods ---------------------------------------------------------

   void setAnnotationStyleRefex(boolean annotationSyleRefex);
   
   void processComponentChronicles(ProcessComponentChronicleBI processor) throws Exception;

   boolean isAnnotationIndex() throws IOException;
}
