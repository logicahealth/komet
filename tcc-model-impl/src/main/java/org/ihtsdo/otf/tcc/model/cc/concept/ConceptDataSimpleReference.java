package org.ihtsdo.otf.tcc.model.cc.concept;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.model.cc.component.MediaBinder;
import com.sleepycat.bind.tuple.TupleInput;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponentBinder;
import org.ihtsdo.otf.tcc.model.cc.component.RefexDynamicMemberBinder;
import org.ihtsdo.otf.tcc.model.cc.component.Revision;
import org.ihtsdo.otf.tcc.model.cc.attributes.ConceptAttributes;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptAttributesBinder;
import org.ihtsdo.otf.tcc.model.cc.description.Description;
import org.ihtsdo.otf.tcc.model.cc.component.DescriptionBinder;
import org.ihtsdo.otf.tcc.model.cc.identifier.IdentifierVersion;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMember;
import org.ihtsdo.otf.tcc.model.cc.component.RefexMemberBinder;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexRevision;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.RefexDynamicMember;
import org.ihtsdo.otf.tcc.model.cc.relationship.Relationship;
import org.ihtsdo.otf.tcc.model.cc.component.RelationshipBinder;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.nid.NidList;
import org.ihtsdo.otf.tcc.api.nid.NidListBI;
import org.ihtsdo.otf.tcc.api.nid.NidSet;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.group.RelGroupChronicleBI;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import org.ihtsdo.otf.tcc.model.cc.P;
import org.ihtsdo.otf.tcc.model.cc.media.Media;

public class ConceptDataSimpleReference extends ConceptDataManager {
    private AtomicReference<ConceptAttributes> attributes = new AtomicReference<>();
    private AtomicReference<AddSrcRelSet> srcRels = new AtomicReference<>();
    private AtomicReference<ConcurrentSkipListSet<Integer>> srcRelNids = new AtomicReference<>();
    ReentrantLock sourceRelLock = new ReentrantLock();
    ReentrantLock refsetMembersLock = new ReentrantLock();
    ReentrantLock refsetMembersDynamicLock = new ReentrantLock();
    private AtomicReference<ConcurrentHashMap<Integer, RefexMember<?, ?>>> refsetMembersMap = new AtomicReference<>();
    private AtomicReference<ConcurrentHashMap<Integer, RefexDynamicMember>> refsetDynamicMembersMap = new AtomicReference<>();
    private AtomicReference<AddMemberSet> refsetMembers = new AtomicReference<>();
    private AtomicReference<AddMemberDynamicSet> refsetDynamicMembers = new AtomicReference<>();
    private AtomicReference<ConcurrentHashMap<Integer, RefexMember<?, ?>>> refsetComponentMap = new AtomicReference<>();
    private AtomicReference<ConcurrentHashMap<Integer, RefexDynamicMember>> refsetDynamicComponentMap = new AtomicReference<>();
    private AtomicReference<ConcurrentSkipListSet<Integer>> memberNids = new AtomicReference<>();
    private ReentrantLock memberMapLock = new ReentrantLock();
    private ReentrantLock memberDynamicMapLock = new ReentrantLock();
    private AtomicReference<AddMediaSet> images = new AtomicReference<>();
    ReentrantLock imageLock = new ReentrantLock();
    private AtomicReference<ConcurrentSkipListSet<Integer>> imageNids = new AtomicReference<>();
    private AtomicReference<AddDescriptionSet> descriptions = new AtomicReference<>();
    ReentrantLock descLock = new ReentrantLock();
    private AtomicReference<ConcurrentSkipListSet<Integer>> descNids = new AtomicReference<>();
    ReentrantLock attrLock = new ReentrantLock();
    private Boolean annotationStyleRefset;

   //~--- constructors --------------------------------------------------------

   public ConceptDataSimpleReference(ConceptChronicle enclosingConcept) throws IOException {
      super(P.s.getConceptDataFetcher(enclosingConcept.getNid()));
      this.enclosingConcept = enclosingConcept;
   }

   public ConceptDataSimpleReference(ConceptChronicle enclosingConcept, byte[] roBytes, byte[] mutableBytes)
           throws IOException {
      super(new NidDataInMemory(roBytes, mutableBytes));
      assert enclosingConcept != null : "enclosing concept cannot be null.";
      this.enclosingConcept = enclosingConcept;
   }

   public ConceptDataSimpleReference(ConceptChronicle enclosingConcept, NidDataInMemory data)
           throws IOException {
      super(data);
      assert enclosingConcept != null : "enclosing concept cannot be null.";
      this.enclosingConcept = enclosingConcept;
   }

   //~--- methods -------------------------------------------------------------

   /*
    * (non-Javadoc)
    *
    * @see
    * org.ihtsdo.db.bdb.concept.I_ManageConceptData#add(org.ihtsdo.db.bdb.concept
    * .component.refset.RefsetMember)
    */
   @Override
   public void add(RefexMember<?, ?> refsetMember) throws IOException {
      getRefsetMembers().addDirect(refsetMember);
      getMemberNids().add(refsetMember.nid);
      addToMemberMap(refsetMember);
      modified();
   }
   
   @Override
   public void add(RefexDynamicMember refsetMember) throws IOException {
      getRefsetDynamicMembers().addDirect(refsetMember);
      getMemberNids().add(refsetMember.nid);
      addToMemberMap(refsetMember);
      modified();
   }

   private void addConceptNidsAffectedByCommit(Collection<? extends ConceptComponent<?, ?>> componentList,
           Collection<Integer> affectedConceptNids)
           throws IOException {
      if (componentList != null) {
         for (ConceptComponent<?, ?> cc : componentList) {
            addConceptNidsAffectedByCommit(cc, affectedConceptNids);
         }
      }
   }

   private void addConceptNidsAffectedByCommit(ConceptComponent<?, ?> cc,
           Collection<Integer> affectedConceptNids)
           throws IOException {
      if (cc != null) {
         if (cc.isUncommitted()) {
            if (cc instanceof RelationshipChronicleBI) {
               RelationshipChronicleBI r = (RelationshipChronicleBI) cc;

               affectedConceptNids.add(r.getOriginNid());
               affectedConceptNids.add(r.getDestinationNid());
            } else if (cc instanceof RefexChronicleBI) {
               RefexChronicleBI r = (RefexChronicleBI) cc;

               affectedConceptNids.add(P.s.getConceptNidForNid(r.getReferencedComponentNid()));
               affectedConceptNids.add(r.getAssemblageNid());
            } else {
               affectedConceptNids.add(getNid());
            }
         }
      }
   }

   @Override
   protected void addToMemberMap(RefexMember<?, ?> refsetMember) {
      memberMapLock.lock();

      try {
         if (refsetMembersMap.get() != null) {
            refsetMembersMap.get().put(refsetMember.nid, refsetMember);
         }

         if (refsetComponentMap.get() != null) {
            refsetComponentMap.get().put(refsetMember.getReferencedComponentNid(), refsetMember);
         }
      } finally {
         memberMapLock.unlock();
      }
   }
   
   @Override
   protected void addToMemberMap(RefexDynamicMember refsetDynamicMember) {
      memberDynamicMapLock.lock();

      try {
         if (refsetDynamicMembersMap.get() != null) {
             refsetDynamicMembersMap.get().put(refsetDynamicMember.nid, refsetDynamicMember);
         }

         if (refsetDynamicComponentMap.get() != null) {
             refsetDynamicComponentMap.get().put(refsetDynamicMember.getReferencedComponentNid(), refsetDynamicMember);
         }
      } finally {
          memberDynamicMapLock.unlock();
      }
   }

   private void addUncommittedNids(Collection<? extends ConceptComponent<?, ?>> componentList,
                                   NidListBI uncommittedNids) {
      if (componentList != null) {
         for (ConceptComponent<?, ?> cc : componentList) {
            addUncommittedNids(cc, uncommittedNids);
         }
      }
   }

   private void addUncommittedNids(ConceptComponent<?, ?> cc, NidListBI uncommittedNids) {
      if (cc != null) {
         if (cc.getTime() == Long.MAX_VALUE) {
            uncommittedNids.add(cc.nid);
         } else {
            if (cc.revisions != null) {
               for (Revision<?, ?> r : cc.revisions) {
                  if (r.getTime() == Long.MAX_VALUE) {
                     uncommittedNids.add(cc.nid);

                     break;
                  }
               }
            }
         }

         if (cc.annotations != null) {
            for (ConceptComponent<?, ?> annotation : cc.annotations) {
               if (annotation.annotations != null) {
                  for (ConceptComponent<?, ?> aa : annotation.annotations) {
                     addUncommittedNids(aa, uncommittedNids);
                  }
               }

               if (annotation.getTime() == Long.MAX_VALUE) {
                  uncommittedNids.add(annotation.nid);

                  break;
               } else if (annotation.revisions != null) {
                  for (Revision<?, ?> r : annotation.revisions) {
                     if (r.getTime() == Long.MAX_VALUE) {
                        uncommittedNids.add(annotation.nid);

                        break;
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public void cancel() throws IOException {
      cancel(attributes.get());
      cancel(srcRels.get());
      cancel(descriptions.get());
      cancel(images.get());
      cancel(refsetMembers.get());
      cancel(refsetDynamicMembers.get());
   }

   private void cancel(Collection<? extends ConceptComponent<?, ?>> componentList) throws IOException {
      ArrayList<ConceptComponent<?, ?>> toRemove = new ArrayList<>();

      if (componentList != null) {
         for (ConceptComponent<?, ?> cc : componentList) {
            if (cancel(cc)) {
               toRemove.add(cc);
               removeRefsetReferences(cc);
            }
         }

         componentList.removeAll(toRemove);
      }
   }

   private boolean cancel(ConceptComponent<?, ?> cc) throws IOException {
      if (cc == null) {
         return true;
      }

      // component
      if (cc.getTime() == Long.MAX_VALUE) {
         cc.cancel();
         removeRefsetReferences(cc);

         return true;
      }

      cc.cancel();

      return false;
   }

   @Override
   public void diet() {
      if (!isUnwritten()) {
         refsetMembersMap.set(null);
         refsetDynamicMembersMap.set(null);
         refsetComponentMap.set(null);
         refsetDynamicComponentMap.set(null);
         refsetMembers.set(null);
         refsetDynamicMembers.set(null);
      }
   }

   @SuppressWarnings("unchecked")
   private void handleCanceledComponents() {
      if (lastExtinctRemoval < P.s.getLastCancel()) {
         if ((refsetMembers != null) && (refsetMembers.get() != null) && (refsetMembers.get().size() > 0)) {
            List<RefexMember<?, ?>> removed = (List<RefexMember<?,
                                                  ?>>) removeCanceledFromList(refsetMembers.get());

            if ((refsetMembersMap.get() != null) || (refsetComponentMap.get() != null)) {
               Map<Integer, ?> memberMap    = refsetMembersMap.get();
               Map<Integer, ?> componentMap = refsetComponentMap.get();

               for (RefexMember<?, ?> cc : removed) {
                  if (memberMap != null) {
                     memberMap.remove(cc.getNid());
                  }

                  if (componentMap != null) {
                     componentMap.remove(cc.getReferencedComponentNid());
                  }
               }
            }
         }
         
         if ((refsetDynamicMembers != null) && (refsetDynamicMembers.get() != null) && (refsetDynamicMembers.get().size() > 0)) {
             List<RefexDynamicMember> removed = (List<RefexDynamicMember>) removeCanceledFromList(refsetDynamicMembers.get());
             if ((refsetDynamicMembersMap.get() != null) || (refsetDynamicComponentMap.get() != null)) {
                 Map<Integer, ?> memberMap    = refsetDynamicMembersMap.get();
                 Map<Integer, ?> componentMap = refsetDynamicComponentMap.get();
    
                 for (RefexDynamicMember cc : removed) {
                    if (memberMap != null) {
                       memberMap.remove(cc.getNid());
                    }
    
                    if (componentMap != null) {
                       componentMap.remove(cc.getReferencedComponentNid());
                    }
                 }
              }
         }

         if ((descriptions != null) && (descriptions.get() != null) && (descriptions.get().size() > 0)) {
            AddDescriptionSet descList = descriptions.get();

            removeCanceledFromList(descList);
         }

         if ((images != null) && (images.get() != null) && (images.get().size() > 0)) {
            removeCanceledFromList(images.get());
         }

         if ((srcRels != null) && (srcRels.get() != null) && (srcRels.get().size() > 0)) {
            removeCanceledFromList(srcRels.get());
         }

         lastExtinctRemoval = P.s.getSequence();
      }
   }

   @Override
   public boolean readyToWrite() {
      if (attributes.get() != null) {
         attributes.get().readyToWriteComponent();
      }

      if (srcRels.get() != null) {
         for (Relationship r : srcRels.get()) {
            assert r.readyToWriteComponent();
         }
      }

      if (descriptions.get() != null) {
         for (Description component : descriptions.get()) {
            assert component.readyToWriteComponent();
         }
      }

      if (images.get() != null) {
         for (Media component : images.get()) {
            assert component.readyToWriteComponent();
         }
      }

      if (refsetMembers.get() != null) {
         for (RefexMember component : refsetMembers.get()) {
            assert component.readyToWriteComponent();
         }
      }
      
      if (refsetDynamicMembers.get() != null) {
          for (RefexDynamicMember component : refsetDynamicMembers.get()) {
             assert component.readyToWriteComponent();
          }
       }

      if (descNids.get() != null) {
         for (Integer component : descNids.get()) {
            assert component != null;
            assert component != Integer.MAX_VALUE;
         }
      }

      if (srcRelNids.get() != null) {
         for (Integer component : srcRelNids.get()) {
            assert component != null;
            assert component != Integer.MAX_VALUE;
         }
      }

      if (imageNids.get() != null) {
         for (Integer component : imageNids.get()) {
            assert component != null;
            assert component != Integer.MAX_VALUE;
         }
      }

      if (memberNids.get() != null) {
         for (Integer component : memberNids.get()) {
            assert component != null;
            assert component != Integer.MAX_VALUE;
         }
      }

      return true;
   }

   private List<? extends ConceptComponent<?,
           ?>> removeCanceledFromList(Collection<? extends ConceptComponent<?, ?>> ccList) {
      List<ConceptComponent<?, ?>> toRemove = new ArrayList<>();

      if (ccList != null) {
         synchronized (ccList) {
            for (ConceptComponent<?, ?> cc : ccList) {
               if (cc.getTime() == Long.MIN_VALUE) {
                  toRemove.add(cc);
                  cc.clearVersions();
                  ConceptChronicle.componentsCRHM.remove(cc.getNid());
               } else {
                  if (cc.revisions != null) {
                     List<Revision<?, ?>> revisionToRemove = new ArrayList<>();

                     for (Revision<?, ?> r : cc.revisions) {
                        if (r.getTime() == Long.MIN_VALUE) {
                           cc.clearVersions();
                           revisionToRemove.add(r);
                        }
                     }

                     for (Revision<?, ?> r : revisionToRemove) {
                        cc.revisions.remove(r);
                     }
                  }
               }
            }

            ccList.removeAll(toRemove);
         }
      }

      return toRemove;
   }

   private void removeRefsetReferences(ConceptComponent<?, ?> cc) throws IOException {
      for (RefexChronicleBI<?> rc : cc.getRefsetMembers()) {
         ConceptChronicle      refsetCon = ConceptChronicle.get(rc.getAssemblageNid());
         RefexMember rm        = (RefexMember) rc;

         rm.primordialStamp = -1;
         P.s.addUncommittedNoChecks(refsetCon);
      }
   }

   private void setupMemberMap(Collection<RefexMember<?, ?>> refsetMemberList) {
      memberMapLock.lock();

      try {
         if ((refsetMembersMap.get() == null) || (refsetComponentMap.get() == null)) {
            ConcurrentHashMap<Integer, RefexMember<?, ?>> memberMap = new ConcurrentHashMap<>(refsetMemberList.size(),
                                                                                0.75f, 2);
            ConcurrentHashMap<Integer, RefexMember<?, ?>> componentMap = new ConcurrentHashMap<>(refsetMemberList.size(),
                                                                                   0.75f, 2);

            for (RefexMember<?, ?> m : refsetMemberList) {
               memberMap.put(m.nid, m);
               componentMap.put(m.getReferencedComponentNid(), m);
            }

            refsetMembersMap.set(memberMap);
            refsetComponentMap.set(componentMap);
         }
      } finally {
         memberMapLock.unlock();
      }
   }
   
   private void setupMemberDynamicMap(Collection<RefexDynamicMember> refsetMemberDynamicList) {
      memberDynamicMapLock.lock();

      try {
         if ((refsetDynamicMembers.get() == null) || (refsetDynamicComponentMap.get() == null)) {
            ConcurrentHashMap<Integer, RefexDynamicMember> memberMap = new ConcurrentHashMap<>(refsetMemberDynamicList.size(),
                                                                                0.75f, 2);
            ConcurrentHashMap<Integer, RefexDynamicMember> componentMap = new ConcurrentHashMap<>(refsetMemberDynamicList.size(),
                                                                                   0.75f, 2);

            for (RefexDynamicMember m : refsetMemberDynamicList) {
               memberMap.put(m.nid, m);
               componentMap.put(m.getReferencedComponentNid(), m);
            }

            refsetDynamicMembersMap.set(memberMap);
            refsetDynamicComponentMap.set(componentMap);
         }
      } finally {
         memberDynamicMapLock.unlock();
      }
   }

   //~--- get methods ---------------------------------------------------------

   private RefexChronicleBI<?> getAnnotation(int nid) throws IOException {
      RefexChronicleBI<?> cc;

      // recursive search through all annotations...
      if (getConceptAttributes() != null) {
         cc = getAnnotation(getConceptAttributes().annotations, nid);

         if (cc != null) {
            return cc;
         }
      }

      if (getDescriptions() != null) {
         for (Description d : getDescriptions()) {
            cc = getAnnotation(d.annotations, nid);

            if (cc != null) {
               return cc;
            }
         }
      }

      if (getSourceRels() != null) {
         for (Relationship r : getSourceRels()) {
            cc = getAnnotation(r.annotations, nid);

            if (cc != null) {
               return cc;
            }
         }
      }

      if (getImages() != null) {
         for (Media i : getImages()) {
            cc = getAnnotation(i.annotations, nid);

            if (cc != null) {
               return cc;
            }
         }
      }

      if (getRefsetMembers() != null) {
         for (RefexMember r : getRefsetMembers()) {
            cc = getAnnotation(r.annotations, nid);

            if (cc != null) {
               return cc;
            }
         }
      }
      
      if (getRefsetDynamicMembers() != null) {
          for (RefexDynamicMember r : getRefsetDynamicMembers()) {
             cc = getAnnotation(r.annotations, nid);

             if (cc != null) {
                return cc;
             }
          }
       }

      return null;
   }
   
   private RefexDynamicChronicleBI<?> getAnnotationDynamic(int nid) throws IOException {
      RefexDynamicChronicleBI<?> cc;

      // recursive search through all annotations...
      if (getConceptAttributes() != null) {
         cc = getAnnotationDynamic(getConceptAttributes().annotationsDynamic, nid);

         if (cc != null) {
            return cc;
         }
      }

      if (getDescriptions() != null) {
         for (Description d : getDescriptions()) {
            cc = getAnnotationDynamic(d.annotationsDynamic, nid);

            if (cc != null) {
               return cc;
            }
         }
      }

      if (getSourceRels() != null) {
         for (Relationship r : getSourceRels()) {
            cc = getAnnotationDynamic(r.annotationsDynamic, nid);

            if (cc != null) {
               return cc;
            }
         }
      }

      if (getImages() != null) {
         for (Media i : getImages()) {
            cc = getAnnotationDynamic(i.annotationsDynamic, nid);

            if (cc != null) {
               return cc;
            }
         }
      }

      if (getRefsetMembers() != null) {
         for (RefexMember<?, ?> r : getRefsetMembers()) {
            cc = getAnnotationDynamic(r.annotationsDynamic, nid);

            if (cc != null) {
               return cc;
            }
         }
      }
      
      if (getRefsetDynamicMembers() != null) {
          for (RefexDynamicMember r : getRefsetDynamicMembers()) {
             cc = getAnnotationDynamic(r.annotationsDynamic, nid);

             if (cc != null) {
                return cc;
             }
          }
       }

      return null;
   }

   private RefexChronicleBI<?> getAnnotation(Collection<? extends RefexChronicleBI<?>> annotations, int nid)
           throws IOException {
      if (annotations == null) {
         return null;
      }

      for (RefexChronicleBI<?> annotation : annotations) {
         if (annotation.getNid() == nid) {
            return annotation;
         }

         RefexChronicleBI<?> cc = getAnnotation(annotation.getAnnotations(), nid);

         if (cc != null) {
            return cc;
         }
      }

      return null;
   }
   
   private RefexDynamicChronicleBI<?> getAnnotationDynamic(Collection<? extends RefexDynamicChronicleBI<?>> annotations, int nid)
           throws IOException {
      if (annotations == null) {
         return null;
      }

      for (RefexDynamicChronicleBI<?> annotation : annotations) {
         if (annotation.getNid() == nid) {
            return annotation;
         }

         RefexDynamicChronicleBI<?> cc = getAnnotationDynamic(annotation.getRefexDynamicAnnotations(), nid);

         if (cc != null) {
            return cc;
         }
      }

      return null;
   }

   @Override
   public ComponentChronicleBI<?> getComponent(int nid) throws IOException {
      if ((getConceptAttributes() != null) && (getConceptAttributes().nid == nid)) {
         return getConceptAttributes();
      }

      if (getDescNids().contains(nid)) {
         for (Description d : getDescriptions()) {
            if (d.getNid() == nid) {
               return d;
            }
         }
      }

      if (getSrcRelNids().contains(nid)) {
         for (Relationship r : getSourceRels()) {
            if (r.getNid() == nid) {
               return r;
            }
         }
      }

      if (getImageNids().contains(nid)) {
         for (Media i : getImages()) {
            if (i.getNid() == nid) {
               return i;
            }
         }
      }

      if (getMemberNids().contains(nid)) {
          //This is a bit odd now - getMemberNids() contains both oldstyle refex, and dynamic style refex.
         //But, I have two different calls to get them - so try both.
          ComponentChronicleBI<?> temp = getRefsetMember(nid);
          if (temp == null)
          {
              temp = getRefsetDynamicMember(nid);
          }
          return temp;
      }
      
      ComponentChronicleBI<?> component = getAnnotation(nid);
      
      if (component != null) {
          return component;
      }

      for (RelGroupChronicleBI group : enclosingConcept.getAllRelGroups()) {
         if (group.getNid() == nid) {
            return group;
         }
      }

      return null;
   }

   @Override
   public ConceptAttributes getConceptAttributes() throws IOException {
      if (attributes.get() == null) {
         attrLock.lock();

         try {
            if (attributes.get() == null) {
               ArrayList<ConceptAttributes> components = getList(new ConceptAttributesBinder(),
                                                            OFFSETS.ATTRIBUTES, enclosingConcept);

               if ((components != null) && (components.size() > 0)) {
                  attributes.compareAndSet(null, components.get(0));
               }
            }
         } finally {
            attrLock.unlock();
         }
      }

      return attributes.get();
   }

   @Override
   public ConceptAttributes getConceptAttributesIfChanged() throws IOException {
      return attributes.get();
   }

   @Override
   public Collection<Integer> getConceptNidsAffectedByCommit() throws IOException {
      Collection<Integer> uncommittedNids = new HashSet<>();

      addConceptNidsAffectedByCommit(attributes.get(), uncommittedNids);
      addConceptNidsAffectedByCommit(srcRels.get(), uncommittedNids);
      addConceptNidsAffectedByCommit(descriptions.get(), uncommittedNids);
      addConceptNidsAffectedByCommit(images.get(), uncommittedNids);
      addConceptNidsAffectedByCommit(refsetMembers.get(), uncommittedNids);
      addConceptNidsAffectedByCommit(refsetDynamicMembers.get(), uncommittedNids);

      return uncommittedNids;
   }

   @Override
   public Set<Integer> getDescNids() throws IOException {
      if (descNids.get() == null) {
         ConcurrentSkipListSet<Integer> temp = new ConcurrentSkipListSet<>(getDescNidsReadOnly());

         temp.addAll(getMutableIntSet(OFFSETS.DESC_NIDS));
         descNids.compareAndSet(null, temp);
      }

      return descNids.get();
   }

   @Override
   public Set<Integer> getDescNidsReadOnly() throws IOException {
      return getReadOnlyIntSet(OFFSETS.DESC_NIDS);
   }

   @Override
   public AddDescriptionSet getDescriptions() throws IOException {
      if (descriptions.get() == null) {
         descLock.lock();

         try {
            if (descriptions.get() == null) {
               descriptions.compareAndSet(null,
                                          new AddDescriptionSet(getList(new DescriptionBinder(),
                                             OFFSETS.DESCRIPTIONS, enclosingConcept)));
            }
         } finally {
            descLock.unlock();
         }
      }

      handleCanceledComponents();

      return descriptions.get();
   }

   @Override
   public Collection<Description> getDescriptionsIfChanged() throws IOException {
      return descriptions.get();
   }

   @Override
   public Set<Integer> getImageNids() throws IOException {
      if (imageNids.get() == null) {
         ConcurrentSkipListSet<Integer> temp = new ConcurrentSkipListSet<>(getImageNidsReadOnly());

         temp.addAll(getMutableIntSet(OFFSETS.IMAGE_NIDS));
         imageNids.compareAndSet(null, temp);
      }

      return imageNids.get();
   }

   @Override
   public Set<Integer> getImageNidsReadOnly() throws IOException {
      return getReadOnlyIntSet(OFFSETS.IMAGE_NIDS);
   }

   @Override
   public AddMediaSet getImages() throws IOException {
      if (images.get() == null) {
         imageLock.lock();

         try {
            if (images.get() == null) {
               images.compareAndSet(null,
                                    new AddMediaSet(getList(new MediaBinder(), OFFSETS.IMAGES,
                                       enclosingConcept)));
            }
         } finally {
            imageLock.unlock();
         }
      }

      handleCanceledComponents();

      return images.get();
   }

   @Override
   public Collection<Media> getImagesIfChanged() throws IOException {
      return images.get();
   }

   private <C extends ConceptComponent<V, C>,
            V extends Revision<V, C>> ArrayList<C> getList(ConceptComponentBinder<V, C> binder,
               OFFSETS offset, ConceptChronicle enclosingConcept)
           throws IOException {
      binder.setupBinder(enclosingConcept);

      ArrayList<C> componentList;
      TupleInput   readOnlyInput = nidData.getReadOnlyTupleInput();

      if (readOnlyInput.available() > 0) {
         checkFormatAndVersion(readOnlyInput);
         readOnlyInput.mark(128);
         readOnlyInput.skipFast(offset.offset);

         int listStart = readOnlyInput.readInt();

         readOnlyInput.reset();
         readOnlyInput.skipFast(listStart);
         componentList = binder.entryToObject(readOnlyInput);
      } else {
         componentList = new ArrayList<>();
      }

      assert componentList != null;
      binder.setTermComponentList(componentList);

      TupleInput readWriteInput = nidData.getMutableTupleInput();

      if (readWriteInput.available() > 0) {
         checkFormatAndVersion(readWriteInput);
         readWriteInput.mark(128);
         readWriteInput.skipFast(offset.offset);

         int listStart = readWriteInput.readInt();

         readWriteInput.reset();
         readWriteInput.skipFast(listStart);
         componentList = binder.entryToObject(readWriteInput);
      }

      return componentList;
   }

   private Collection<RefexMember<?, ?>> getList(RefexMemberBinder binder, OFFSETS offset,
           ConceptChronicle enclosingConcept)
           throws IOException {
      binder.setupBinder(enclosingConcept);

      Collection<RefexMember<?, ?>> componentList;
      TupleInput                     readOnlyInput = nidData.getReadOnlyTupleInput();

      if (readOnlyInput.available() > 0) {
         checkFormatAndVersion(readOnlyInput);
         readOnlyInput.mark(128);
         readOnlyInput.skipFast(offset.offset);

         int listStart = readOnlyInput.readInt();

         readOnlyInput.reset();
         readOnlyInput.skipFast(listStart);
         componentList = binder.entryToObject(readOnlyInput);
      } else {
         componentList = new ArrayList<>();
      }

      assert componentList != null;
      binder.setTermComponentList(componentList);

      TupleInput readWriteInput = nidData.getMutableTupleInput();

      if (readWriteInput.available() > 0) {
         readWriteInput.mark(128);
         checkFormatAndVersion(readWriteInput);
         readWriteInput.reset();
         readWriteInput.skipFast(offset.offset);

         int listStart = readWriteInput.readInt();

         readWriteInput.reset();
         readWriteInput.skipFast(listStart);
         componentList = binder.entryToObject(readWriteInput);
      }

      return componentList;
   }
   
   private Collection<RefexDynamicMember> getList(RefexDynamicMemberBinder binder, OFFSETS offset,
           ConceptChronicle enclosingConcept)
           throws IOException {
      binder.setupBinder(enclosingConcept);

      Collection<RefexDynamicMember> componentList;
      TupleInput                     readOnlyInput = nidData.getReadOnlyTupleInput();

      if (readOnlyInput.available() > 0) {
         checkFormatAndVersion(readOnlyInput);
         readOnlyInput.mark(128);
         readOnlyInput.skipFast(offset.offset);

         int listStart = readOnlyInput.readInt();

         readOnlyInput.reset();
         readOnlyInput.skipFast(listStart);
         componentList = binder.entryToObject(readOnlyInput);
      } else {
         componentList = new ArrayList<>();
      }

      assert componentList != null;
      binder.setTermComponentList(componentList);

      TupleInput readWriteInput = nidData.getMutableTupleInput();

      if (readWriteInput.available() > 0) {
         readWriteInput.mark(128);
         checkFormatAndVersion(readWriteInput);
         readWriteInput.reset();
         readWriteInput.skipFast(offset.offset);

         int listStart = readWriteInput.readInt();

         readWriteInput.reset();
         readWriteInput.skipFast(listStart);
         componentList = binder.entryToObject(readWriteInput);
      }

      return componentList;
   }

   @Override
   public Set<Integer> getMemberNids() throws IOException {
      if (memberNids.get() == null) {
         ConcurrentSkipListSet<Integer> temp = new ConcurrentSkipListSet<>(getMemberNidsReadOnly());

         temp.addAll(getMutableIntSet(OFFSETS.MEMBER_NIDS));
         memberNids.compareAndSet(null, temp);
      }

      return memberNids.get();
   }

   @Override
   public Set<Integer> getMemberNidsReadOnly() throws IOException {
      return getReadOnlyIntSet(OFFSETS.MEMBER_NIDS);
   }

   protected ConcurrentSkipListSet<Integer> getMutableIntSet(OFFSETS offset) throws IOException {
      TupleInput mutableInput = nidData.getMutableTupleInput();

      if (mutableInput.available() < OFFSETS.getHeaderSize()) {
         return new ConcurrentSkipListSet<>();
      }

      mutableInput.mark(OFFSETS.getHeaderSize());
      mutableInput.skipFast(offset.offset);

      int dataOffset = mutableInput.readInt();

      mutableInput.reset();
      mutableInput.skipFast(dataOffset);

      IntSetBinder binder = new IntSetBinder();

      return binder.entryToObject(mutableInput);
   }

   public ConceptDataFetcherI getNidData() {
      return nidData;
   }

   protected ConcurrentSkipListSet<Integer> getReadOnlyIntSet(OFFSETS offset) throws IOException {
      TupleInput readOnlyInput = nidData.getReadOnlyTupleInput();

      if (readOnlyInput.available() < OFFSETS.getHeaderSize()) {
         return new ConcurrentSkipListSet<>();
      }

      readOnlyInput.mark(OFFSETS.getHeaderSize());
      readOnlyInput.skipFast(offset.offset);

      int dataOffset = readOnlyInput.readInt();

      readOnlyInput.reset();
      readOnlyInput.skipFast(dataOffset);

      IntSetBinder binder = new IntSetBinder();

      return binder.entryToObject(readOnlyInput);
   }

   @Override
   public RefexMember<?, ?> getRefsetMember(int memberNid) throws IOException {
      if (isAnnotationStyleRefex()) {
         if (getMemberNids().contains(memberNid)) {
            if (P.s.getConceptNidForNid(memberNid) == getNid()) {
               return (RefexMember<?, ?>) getAnnotation(memberNid);
            } else {
               return (RefexMember<?, ?>) P.s.getComponent(memberNid);
            }
         }

         return null;
      }

      Collection<RefexMember<?, ?>> refsetMemberList = getRefsetMembers();

      if (refsetMembersMap.get() != null) {
         return refsetMembersMap.get().get(memberNid);
      }

      if (refsetMemberList.size() < useMemberMapThreshold) {
         for (RefexMember<?, ?> member : refsetMemberList) {
            if (member.nid == memberNid) {
               return member;
            }
         }

         return null;
      }

      if (refsetMembersMap.get() == null) {
         setupMemberMap(refsetMemberList);
      }

      return refsetMembersMap.get().get(memberNid);
   }
   
   @Override
   public RefexDynamicMember getRefsetDynamicMember(int memberNid) throws IOException {
      if (isAnnotationStyleRefex()) {
         if (getMemberNids().contains(memberNid)) {
            if (P.s.getConceptNidForNid(memberNid) == getNid()) {
               return (RefexDynamicMember) getAnnotationDynamic(memberNid);
            } else {
               return (RefexDynamicMember) P.s.getComponent(memberNid);
            }
         }

         return null;
      }

      Collection<RefexDynamicMember> refsetMemberList = getRefsetDynamicMembers();

      if (refsetDynamicMembersMap.get() != null) {
         return refsetDynamicMembersMap.get().get(memberNid);
      }

      if (refsetMemberList.size() < useMemberMapThreshold) {
         for (RefexDynamicMember member : refsetMemberList) {
            if (member.nid == memberNid) {
               return member;
            }
         }

         return null;
      }

      if (refsetDynamicMembersMap.get() == null) {
         setupMemberDynamicMap(refsetMemberList);
      }

      return refsetDynamicMembersMap.get().get(memberNid);
   }

   @Override
   public RefexMember<?, ?> getRefsetMemberForComponent(int componentNid) throws IOException {
      Collection<RefexMember<?, ?>> refsetMemberList = getRefsetMembers();

      if (refsetMemberList.size() < useMemberMapThreshold) {
         for (RefexMember<?, ?> member : refsetMemberList) {
            if (member.getReferencedComponentNid() == componentNid) {
               return member;
            }
         }

         return null;
      }

      if (refsetComponentMap.get() == null) {
         setupMemberMap(refsetMemberList);
      }

      return refsetComponentMap.get().get(componentNid);
   }

   @Override
   public AddMemberSet getRefsetMembers() throws IOException {

      if (refsetMembers.get() == null) {
         refsetMembersLock.lock();

         try {
            if (refsetMembers.get() == null) {
               refsetMembers.compareAndSet(null,
                                           new AddMemberSet(getList(new RefexMemberBinder(enclosingConcept),
                                              OFFSETS.REFSET_MEMBERS, enclosingConcept)));
            }
         } finally {
            refsetMembersLock.unlock();
         }
      }

      handleCanceledComponents();

      return refsetMembers.get();
   }
   
   @Override
   public AddMemberDynamicSet getRefsetDynamicMembers() throws IOException {

      if (refsetDynamicMembers.get() == null) {
         refsetMembersDynamicLock.lock();

         try {
            if (refsetDynamicMembers.get() == null) {
                refsetDynamicMembers.compareAndSet(null,
                                           new AddMemberDynamicSet(getList(new RefexDynamicMemberBinder(enclosingConcept),
                                              OFFSETS.REFSET_MEMBERS, enclosingConcept)));
            }
         } finally {
             refsetMembersDynamicLock.unlock();
         }
      }

      handleCanceledComponents();

      return refsetDynamicMembers.get();
   }

   @Override
   public Collection<RefexMember<?, ?>> getRefsetMembersIfChanged() throws IOException {
      return refsetMembers.get();
   }
   
   @Override
   public Collection<RefexDynamicMember> getRefsetDynamicMembersIfChanged() throws IOException {
      return refsetDynamicMembers.get();
   }

   @Override
   public AddSrcRelSet getSourceRels() throws IOException {
      if (srcRels.get() == null) {
         sourceRelLock.lock();

         try {
            if (srcRels.get() == null) {
               srcRels.compareAndSet(null,
                                     new AddSrcRelSet(getList(new RelationshipBinder(), OFFSETS.SOURCE_RELS,
                                        enclosingConcept), true));
            }
         } finally {
            sourceRelLock.unlock();
         }
      }

      handleCanceledComponents();

      return srcRels.get();
   }

   @Override
   public Collection<Relationship> getSourceRelsIfChanged() throws IOException {
      return srcRels.get();
   }

   @Override
   public Set<Integer> getSrcRelNids() throws IOException {
      if (srcRelNids.get() == null) {
         ConcurrentSkipListSet<Integer> temp = new ConcurrentSkipListSet<>(getSrcRelNidsReadOnly());

         temp.addAll(getMutableIntSet(OFFSETS.SRC_REL_NIDS));
         srcRelNids.compareAndSet(null, temp);
      }

      return srcRelNids.get();
   }

   @Override
   public Set<Integer> getSrcRelNidsReadOnly() throws IOException {
      return getReadOnlyIntSet(OFFSETS.SRC_REL_NIDS);
   }

   @Override
   public NidListBI getUncommittedNids() {
      NidListBI uncommittedNids = new NidList();

      addUncommittedNids(attributes.get(), uncommittedNids);
      addUncommittedNids(srcRels.get(), uncommittedNids);
      addUncommittedNids(descriptions.get(), uncommittedNids);
      addUncommittedNids(images.get(), uncommittedNids);
      addUncommittedNids(refsetMembers.get(), uncommittedNids);
      addUncommittedNids(refsetDynamicMembers.get(), uncommittedNids);

      return uncommittedNids;
   }

   @Override
   public boolean hasComponent(int nid) throws IOException {
      if (getNid() == nid) {
         return true;
      }

      if (getDescNids().contains(nid)) {
         return true;
      }

      if (getSrcRelNids().contains(nid)) {
         return true;
      }

      if (getImageNids().contains(nid)) {
         return true;
      }

      if (getMemberNids().contains(nid)) {
         return true;
      }

      return false;
   }

   private boolean hasUncommittedAnnotation(ConceptComponent<?, ?> cc) {
      if ((cc != null) && (cc.annotations != null)) {
         for (RefexChronicleBI<?> rmc : cc.annotations) {
            if (rmc.isUncommitted()) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public boolean hasUncommittedComponents() {
      if (hasUncommittedVersion(attributes.get())) {
         return true;
      }

      if (hasUncommittedVersion(srcRels.get())) {
         return true;
      }

      if (hasUncommittedVersion(descriptions.get())) {
         return true;
      }

      if (hasUncommittedVersion(images.get())) {
         return true;
      }

      if (hasUncommittedVersion(refsetMembers.get())) {
         return true;
      }
      
      if (hasUncommittedVersion(refsetDynamicMembers.get())) {
          return true;
       }

      return false;
   }

   private boolean hasUncommittedId(ConceptComponent<?, ?> cc) {
      if ((cc != null) && (cc.getAdditionalIdentifierParts() != null)) {
         for (IdentifierVersion idv : cc.getAdditionalIdentifierParts()) {
            if (idv.getTime() == Long.MAX_VALUE) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean hasUncommittedVersion(Collection<? extends ConceptComponent<?, ?>> componentList) {
      if (componentList != null) {
         for (ConceptComponent<?, ?> cc : componentList) {
            if (hasUncommittedVersion(cc)) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean hasUncommittedVersion(ConceptComponent<?, ?> cc) {
      if (cc != null) {
         if (cc.getTime() == Long.MAX_VALUE) {
            return true;
         }

         if (cc.revisions != null) {
            for (Revision<?, ?> r : cc.revisions) {
               if (r.getTime() == Long.MAX_VALUE) {
                  return true;
               }
            }
         }

         if (hasUncommittedId(cc)) {
            return true;
         }

         if (hasUncommittedAnnotation(cc)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean isAnnotationStyleRefex() throws IOException {
      if (annotationStyleRefset == null) {
         annotationStyleRefset = getIsAnnotationStyleRefset();
      }

      return annotationStyleRefset;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void set(ConceptAttributes attr) throws IOException {
      if (attributes.get() != null) {
         throw new IOException("Attributes is already set. Please modify the exisiting attributes object.");
      }

      if (!attributes.compareAndSet(null, attr)) {
         throw new IOException("Attributes is already set. Please modify the exisiting attributes object.");
      }

      enclosingConcept.modified();
   }


   @Override
   public void setAnnotationStyleRefset(boolean annotationStyleRefset) {
      modified();
      this.annotationStyleRefset = annotationStyleRefset;
   }

   @Override
   public NidSetBI setCommitTime(long time) {
      NidSet sapNids = new NidSet();

      setCommitTime(attributes.get(), time, sapNids);
      setCommitTime(srcRels.get(), time, sapNids);
      setCommitTime(descriptions.get(), time, sapNids);
      setCommitTime(images.get(), time, sapNids);
      setCommitTime(refsetMembers.get(), time, sapNids);
      setCommitTime(refsetDynamicMembers.get(), time, sapNids);

      return sapNids;
   }

   private void setCommitTime(Collection<? extends ConceptComponent<?, ?>> componentList, long time,
                              NidSetBI sapNids) {
      if (componentList != null) {
         for (ConceptComponent<?, ?> cc : componentList) {
            setCommitTime(cc, time, sapNids);
         }
      }
   }

   private void setCommitTime(ConceptComponent<?, ?> cc, long time, NidSetBI sapNids) {

      // component
      if (cc.getTime() == Long.MAX_VALUE) {
         cc.setTime(time);
         sapNids.add(cc.primordialStamp);
      }

      if (cc.revisions != null) {
         for (Revision<?, ?> r : cc.revisions) {
            if (r.getTime() == Long.MAX_VALUE) {
               r.setTime(time);
               sapNids.add(r.stamp);
            }
         }
      }

      // id
      if (cc.getAdditionalIdentifierParts() != null) {
         for (IdentifierVersion idv : cc.getAdditionalIdentifierParts()) {
            if (idv.getTime() == Long.MAX_VALUE) {
               idv.setTime(time);
               sapNids.add(idv.getStamp());
            }
         }
      }

      // annotation
      if (cc.annotations != null) {
         for (RefexChronicleBI<?> rc : cc.annotations) {
            RefexMember<?, ?> rm = (RefexMember<?, ?>) rc;

            if (rm.getTime() == Long.MAX_VALUE) {
               rm.setTime(time);
               sapNids.add(rm.getStamp());
            }

            if (rm.revisions != null) {
               for (RefexRevision<?, ?> rr : rm.revisions) {
                  if (rr.getTime() == Long.MAX_VALUE) {
                     rr.setTime(time);
                     sapNids.add(rr.getStamp());
                  }
               }
            }
         }
      }
   }
}
