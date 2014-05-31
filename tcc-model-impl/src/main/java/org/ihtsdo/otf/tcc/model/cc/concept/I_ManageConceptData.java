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
  //@Embeddable @Access(AccessType.PROPERTY)  //TODO-AKF: start here, add transients
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
//   @Transient
   Collection<Integer> getAllNids() throws IOException;
   
//   @Transient
   ComponentChronicleBI<?> getComponent(int nid) throws IOException;
   
//   @OneToOne
   ConceptAttributes getConceptAttributes() throws IOException;
   
//   @Transient
   ConceptAttributes getConceptAttributesIfChanged() throws IOException;
   
//   @Transient
   public Collection<Integer> getConceptNidsAffectedByCommit() throws IOException;

//   @Transient
   Set<Integer> getDescNids() throws IOException;

//   @Transient
   Set<Integer> getDescNidsReadOnly() throws IOException;

//   @OneToMany
   Set<Description> getDescriptions() throws IOException;

//   @Transient
   Collection<Description> getDescriptionsIfChanged() throws IOException;
   
//   @Transient//TODO-AKF: should dest rels be included?
   List<Relationship> getDestRels() throws IOException;

//   @Transient
   List<Relationship> getDestRels(NidSetBI allowedTypes) throws IOException;

//   @Transient
   Set<Integer> getImageNids() throws IOException;

//   @Transient
   Set<Integer> getImageNidsReadOnly() throws IOException;

//   @Transient //TODO-AKF: for now, need to implement media
   AddMediaSet getImages() throws IOException;

//   @Transient
   Collection<Media> getImagesIfChanged() throws IOException;

//   @Transient
   Set<Integer> getMemberNids() throws IOException;

//   @Transient
   Set<Integer> getMemberNidsReadOnly() throws IOException;

//   @Transient
   int getNid();

//   @Transient //TODO-AKF: for now, need to implement refsets
   RefexMember<?, ?> getRefsetMember(int memberNid) throws IOException;

//   @Transient //TODO-AKF: for now, need to implement refsets
   RefexMember<?, ?> getRefsetMemberForComponent(int componentNid) throws IOException;

//   @Transient //TODO-AKF: for now, need to implement refsets
   AddMemberSet getRefsetMembers() throws IOException;

//   @Transient //TODO-AKF: for now, need to implement refsets
   Collection<RefexMember<?, ?>> getRefsetMembersIfChanged() throws IOException;
   
//   @OneToMany
   Set<Relationship> getSourceRels() throws IOException;

//   @Transient
   Collection<Relationship> getSourceRelsIfChanged() throws IOException;

//   @Transient
   Set<Integer> getSrcRelNids() throws IOException;

//   @Transient
   Set<Integer> getSrcRelNidsReadOnly() throws IOException;

//   @Transient
   NidListBI getUncommittedNids();
   
//   @Transient //TODO-AKF: testing
   boolean isAnnotationStyleRefex() throws IOException;
//TODO-AKF: should this be included?
   boolean isPrimordial() throws IOException;

//   @Transient
   boolean isUncommitted();

//   @Transient
   boolean isUnwritten();

   //~--- set methods ---------------------------------------------------------

   void setConceptAttributes(ConceptAttributes attr) throws IOException;
   
   void setDescriptions(Set<Description> descriptions) throws IOException;
   
   void setSourceRels(Set<Relationship> relationships) throws IOException;
//            TODO-AKF: testing
   void setIsAnnotationStyleRefex(boolean annotationStyleRefex);
   
   void setPrimordial(boolean isPrimordial);

   /**
    * For single-concept commit.
    * @param time
    */
   NidSetBI setCommitTime(long time);
   
}
