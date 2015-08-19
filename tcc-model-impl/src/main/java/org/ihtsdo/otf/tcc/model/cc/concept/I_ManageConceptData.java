package org.ihtsdo.otf.tcc.model.cc.concept;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.nid.NidListBI;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.model.cc.attributes.ConceptAttributes;
import org.ihtsdo.otf.tcc.model.cc.description.Description;
import org.ihtsdo.otf.tcc.model.cc.media.Media;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.model.cc.relationship.Relationship;

public interface I_ManageConceptData extends ModificationTracker {
    
    ConceptChronicle getConceptChronicle();
    
    
    void add(Description desc) throws IOException;

    void add(Media img) throws IOException;

    void add(RefexMember<?, ?> refsetMember) throws IOException;

    void add(Relationship rel) throws IOException;

    void forgetConcept();
    boolean isConceptForgotten();

    /**
     * For single-concept cancel.
     */
    void cancel() throws IOException;

    boolean readyToWrite();

    //~--- get methods ---------------------------------------------------------
    Collection<Integer> getAllNids() throws IOException;

    ComponentChronicleBI<?> getComponent(int nid) throws IOException;

    ConceptAttributes getConceptAttributes() throws IOException;

    void setConceptAttributes(ConceptAttributes attr) throws IOException;

    public Collection<Integer> getConceptNidsAffectedByCommit() throws IOException;

    Set<Integer> getDescNids() throws IOException;

    Collection<Description> getDescriptions() throws IOException;

    void setDescriptions(Set<Description> descriptions) throws IOException;

    List<Relationship> getDestRels() throws IOException;

    List<Relationship> getDestRels(NidSetBI allowedTypes) throws IOException;

    Set<Integer> getImageNids() throws IOException;

    Collection<Media> getMedia() throws IOException;

    Set<Integer> getMemberNids() throws IOException;
    
    int getNid();

    RefexMember<?, ?> getRefsetMember(int memberNid) throws IOException;


    RefexMember<?, ?> getRefsetMemberForComponent(int componentNid) throws IOException;

    Collection<RefexMember<?,?>> getRefsetMembers() throws IOException;

    Collection<Relationship> getSourceRels() throws IOException;

    void setSourceRels(Set<Relationship> relationships) throws IOException;

    Set<Integer> getSrcRelNids() throws IOException;

    NidListBI getUncommittedNids();

    boolean isAnnotationStyleRefex() throws IOException;

    //~--- set methods ---------------------------------------------------------

    boolean isPrimordial() throws IOException;

    void setPrimordial(boolean isPrimordial);

    boolean isUncommitted();

    boolean isUnwritten();

    void setIsAnnotationStyleRefex(boolean annotationStyleRefex);

    /**
     * For single-concept commit.
     *
     * @param time
     */
    NidSetBI setCommitTime(long time);

}
