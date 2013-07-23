package org.ihtsdo.otf.tcc.chronicle.cc.concept;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;


import org.ihtsdo.otf.tcc.chronicle.cc.attributes.ConceptAttributes;
import org.ihtsdo.otf.tcc.chronicle.cc.description.Description;
import org.ihtsdo.otf.tcc.chronicle.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.chronicle.cc.relationship.Relationship;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.nid.NidListBI;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.ihtsdo.otf.tcc.chronicle.cc.concept.ConceptDataManager.AddDescriptionSet;
import org.ihtsdo.otf.tcc.chronicle.cc.concept.ConceptDataManager.AddMediaSet;
import org.ihtsdo.otf.tcc.chronicle.cc.concept.ConceptDataManager.AddMemberSet;
import org.ihtsdo.otf.tcc.chronicle.cc.concept.ConceptDataManager.AddSrcRelSet;
import org.ihtsdo.otf.tcc.chronicle.cc.media.Media;

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

   void resetNidData();

   //~--- get methods ---------------------------------------------------------

   Collection<Integer> getAllNids() throws IOException;

   ComponentChronicleBI<?> getComponent(int nid) throws IOException;

   ConceptAttributes getConceptAttributes() throws IOException;

   ConceptAttributes getConceptAttributesIfChanged() throws IOException;

   public Collection<Integer> getConceptNidsAffectedByCommit() throws IOException;

   Set<Integer> getDescNids() throws IOException;

   Set<Integer> getDescNidsReadOnly() throws IOException;

   AddDescriptionSet getDescriptions() throws IOException;

   Collection<Description> getDescriptionsIfChanged() throws IOException;

   List<Relationship> getDestRels() throws IOException;

   List<Relationship> getDestRels(NidSetBI allowedTypes) throws IOException;

   Set<Integer> getImageNids() throws IOException;

   Set<Integer> getImageNidsReadOnly() throws IOException;

   AddMediaSet getImages() throws IOException;

   Collection<Media> getImagesIfChanged() throws IOException;

   long getLastChange();

   long getLastWrite();

   Set<Integer> getMemberNids() throws IOException;

   Set<Integer> getMemberNidsReadOnly() throws IOException;

   int getNid();

   byte[] getReadOnlyBytes() throws IOException;

   byte[] getReadWriteBytes() throws IOException;

   int getReadWriteDataVersion() throws InterruptedException, ExecutionException, IOException;

   TupleInput getReadWriteTupleInput() throws IOException;

   RefexMember<?, ?> getRefsetMember(int memberNid) throws IOException;

   RefexMember<?, ?> getRefsetMemberForComponent(int componentNid) throws IOException;

   AddMemberSet getRefsetMembers() throws IOException;

   Collection<RefexMember<?, ?>> getRefsetMembersIfChanged() throws IOException;

   AddSrcRelSet getSourceRels() throws IOException;

   Collection<Relationship> getSourceRelsIfChanged() throws IOException;

   Set<Integer> getSrcRelNids() throws IOException;

   Set<Integer> getSrcRelNidsReadOnly() throws IOException;

   NidListBI getUncommittedNids();

   boolean isAnnotationIndex() throws IOException;

   boolean isAnnotationStyleRefex() throws IOException;

   /**
    *
    * @return
    * @deprecated use isAnnotationStyleRefex
    */
   @Deprecated
   boolean isAnnotationStyleSet() throws IOException;

   boolean isPrimordial() throws IOException;

   boolean isUncommitted();

   boolean isUnwritten();

   //~--- set methods ---------------------------------------------------------

   void set(ConceptAttributes attr) throws IOException;

   public void setAnnotationIndex(boolean annotationIndex) throws IOException;;

   void setAnnotationStyleRefset(boolean annotationStyleRefset);

   /**
    * For single-concept commit.
    * @param time
    */
   NidSetBI setCommitTime(long time);

   void setLastWrite(long version);
}
