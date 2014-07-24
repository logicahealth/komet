package org.ihtsdo.otf.tcc.model.cc.concept;

//~--- non-JDK imports --------------------------------------------------------

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.nid.NidListBI;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.model.cc.attributes.ConceptAttributes;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptDataManager.AddMediaSet;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptDataManager.AddMemberSet;
import org.ihtsdo.otf.tcc.model.cc.description.Description;
import org.ihtsdo.otf.tcc.model.cc.media.Media;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.model.cc.relationship.Relationship;
public interface I_ManageConceptData {
   void add(Description desc) throws IOException;

   void add(Media img) throws IOException;

   void add(RefexMember<?, ?> refsetMember) throws IOException;

   void add(Relationship rel) throws IOException;

   /**
    * For single-concept cancel.
    */
   void cancel() throws IOException;

   void diet();

   void modified();

   void modified(long sequence);

   boolean readyToWrite();

   //~--- get methods ---------------------------------------------------------
   Collection<Integer> getAllNids() throws IOException;
   
   ComponentChronicleBI<?> getComponent(int nid) throws IOException;

   ConceptAttributes getConceptAttributes() throws IOException;
   
   ConceptAttributes getConceptAttributesIfChanged() throws IOException;
   
   public Collection<Integer> getConceptNidsAffectedByCommit() throws IOException;

   Set<Integer> getDescNids() throws IOException;

   Set<Integer> getDescNidsReadOnly() throws IOException;

   Set<Description> getDescriptions() throws IOException;

   Collection<Description> getDescriptionsIfChanged() throws IOException;
   
   List<Relationship> getDestRels() throws IOException;

   List<Relationship> getDestRels(NidSetBI allowedTypes) throws IOException;

   Set<Integer> getImageNids() throws IOException;

   Set<Integer> getImageNidsReadOnly() throws IOException;

   AddMediaSet getImages() throws IOException;

   Collection<Media> getImagesIfChanged() throws IOException;

   Set<Integer> getMemberNids() throws IOException;

   Set<Integer> getMemberNidsReadOnly() throws IOException;

   int getNid();

   RefexMember<?, ?> getRefsetMember(int memberNid) throws IOException;

   RefexMember<?, ?> getRefsetMemberForComponent(int componentNid) throws IOException;

   AddMemberSet getRefsetMembers() throws IOException;

   Collection<RefexMember<?, ?>> getRefsetMembersIfChanged() throws IOException;
   
   Set<Relationship> getSourceRels() throws IOException;

   Collection<Relationship> getSourceRelsIfChanged() throws IOException;

   Set<Integer> getSrcRelNids() throws IOException;

   Set<Integer> getSrcRelNidsReadOnly() throws IOException;

   NidListBI getUncommittedNids();

   boolean isAnnotationStyleRefex() throws IOException;

   boolean isPrimordial() throws IOException;

   boolean isUncommitted();

   boolean isUnwritten();

   //~--- set methods ---------------------------------------------------------

   void setConceptAttributes(ConceptAttributes attr) throws IOException;
   
   void setDescriptions(Set<Description> descriptions) throws IOException;
   
   void setSourceRels(Set<Relationship> relationships) throws IOException;

   void setIsAnnotationStyleRefex(boolean annotationStyleRefex);
   
   void setPrimordial(boolean isPrimordial);
   
   void loadEagerly() throws IOException;

   /**
    * For single-concept commit.
    * @param time
    */
   NidSetBI setCommitTime(long time);
   
}
