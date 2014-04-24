package org.ihtsdo.otf.tcc.model.cc.component;

//~--- non-JDK imports --------------------------------------------------------

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;
import org.ihtsdo.otf.tcc.model.cc.P;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.model.cc.concept.I_BindConceptComponents;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexRevision;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.RefexDynamicMember;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.RefexDynamicMemberFactory;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.RefexDynamicRevision;
import org.ihtsdo.otf.tcc.model.index.service.IndexerBI;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
//~--- JDK imports ------------------------------------------------------------import java.io.IOException;

public class RefexDynamicMemberBinder extends TupleBinding<Collection<RefexDynamicMember>> implements I_BindConceptComponents {
    public static AtomicInteger      encountered                   = new AtomicInteger();
    public static AtomicInteger      written                       = new AtomicInteger();
    private static int               maxReadOnlyStatusAtPositionId = P.s.getMaxReadOnlyStamp();
    protected static List<IndexerBI> indexers;

    static {
        indexers = Hk2Looker.get().getAllServices(IndexerBI.class);
    }

    private ConceptChronicle              enclosingConcept;
    private Collection<RefexDynamicMember> refsetMemberList;

    public RefexDynamicMemberBinder(ConceptChronicle concept) {
        this.enclosingConcept = concept;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<RefexDynamicMember> entryToObject(TupleInput input) {
        assert enclosingConcept != null;

        int                                 listSize = input.readInt();
        Collection<RefexDynamicMember>       newRefsetMemberList;
        HashMap<Integer, RefexDynamicMember> nidToRefsetMemberMap = null;

        if (refsetMemberList != null) {
            newRefsetMemberList  = refsetMemberList;
            nidToRefsetMemberMap = new HashMap<>(listSize);

            for (RefexDynamicMember component : refsetMemberList) {
                nidToRefsetMemberMap.put(component.nid, component);
            }
        } else {
            newRefsetMemberList = new ArrayList<>(listSize);
        }

        for (int index = 0; index < listSize; index++) {

            input.mark(0);

            int nid = input.readInt();

            input.reset();

            Object component = ConceptChronicle.componentsCRHM.get(nid);

            if ((component == null) || (component instanceof RefexDynamicMember)) {
                RefexDynamicMember refsetMember = (RefexDynamicMember) component;

                if ((refsetMember != null) && (refsetMember.getTime() == Long.MIN_VALUE)) {
                    refsetMember = null;
                    ConceptChronicle.componentsCRHM.remove(nid);
                }

                if ((nidToRefsetMemberMap != null) && nidToRefsetMemberMap.containsKey(nid)) {
                    if (refsetMember == null) {
                        refsetMember = nidToRefsetMemberMap.get(nid);

                        RefexDynamicMember oldMember = (RefexDynamicMember) ConceptChronicle.componentsCRHM.putIfAbsent(nid,
                                                              refsetMember);

                        if (oldMember != null) {
                            refsetMember = oldMember;
                            nidToRefsetMemberMap.put(nid, refsetMember);
                        }
                    }

                    refsetMember.readComponentFromBdb(input);
                } else {
                    try {
                        if (refsetMember == null) {
                            refsetMember = RefexDynamicMemberFactory.create(nid, enclosingConcept.getNid(), input);

                            if (refsetMember.getTime() != Long.MIN_VALUE) {
                                RefexDynamicMember oldMember = (RefexDynamicMember) ConceptChronicle.componentsCRHM.putIfAbsent(nid,
                                                                      refsetMember);

                                if (oldMember != null) {
                                    refsetMember = oldMember;

                                    if (nidToRefsetMemberMap != null) {
                                        nidToRefsetMemberMap.put(nid, refsetMember);
                                    }
                                }
                            }
                        } else {
                            refsetMember.merge(RefexDynamicMemberFactory.create(nid, enclosingConcept.getNid(),
                                    input));
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    if (refsetMember.getTime() != Long.MIN_VALUE) {
                        newRefsetMemberList.add(refsetMember);
                    }
                }
            } else {
                StringBuilder sb = new StringBuilder();

                sb.append("Refset member has nid: ").append(nid);
                sb.append(" But another component has same nid:\n").append(component);

                try {
                    sb.append("Refset member: \n").append(RefexDynamicMemberFactory.create(nid, enclosingConcept.getNid(), input));
                } catch (IOException ex) {
                    ConceptComponent.logger.log(Level.WARNING, ex.getMessage(), ex);
                }

                ConceptComponent.logger.log(Level.SEVERE, "Nid overlap discovered. See log for more info.",
                                            new Exception(sb.toString()));
            }
        }

        return newRefsetMemberList;
    }

    @Override
    public void objectToEntry(Collection<RefexDynamicMember> list, TupleOutput output) {
        List<RefexDynamicMember> refsetMembersToWrite = new ArrayList<>(list.size());

        for (RefexDynamicMember refsetMember : list) {
            encountered.incrementAndGet();
            assert refsetMember.primordialStamp != Integer.MAX_VALUE;

            if (!refsetMember.isIndexed()) {
                for (IndexerBI i : indexers) {
                    i.index(refsetMember);
                }

                refsetMember.setIndexed();
            }

            if ((refsetMember.primordialStamp > maxReadOnlyStatusAtPositionId)
                    && (refsetMember.getTime() != Long.MIN_VALUE)) {
                refsetMembersToWrite.add(refsetMember);
            } else {
                if (refsetMember.revisions != null) {
                    for (RefexDynamicRevision r : refsetMember.revisions) {
                        if ((r.getStamp() > maxReadOnlyStatusAtPositionId) && (r.getTime() != Long.MIN_VALUE)) {
                            refsetMembersToWrite.add(refsetMember);

                            break;
                        }
                    }
                }
            }
        }

        output.writeInt(refsetMembersToWrite.size());    // List size

        for (RefexDynamicMember refsetMember : refsetMembersToWrite) {
            written.incrementAndGet();
            refsetMember.writeComponentToBdb(output, maxReadOnlyStatusAtPositionId);
        }
    }

    @Override
    public void setupBinder(ConceptChronicle enclosingConcept) {
        this.enclosingConcept = enclosingConcept;
    }

    @Override
    public ConceptChronicle getEnclosingConcept() {
        return enclosingConcept;
    }

    public void setEnclosingConcept(ConceptChronicle enclosingConcept) {
        this.enclosingConcept = enclosingConcept;
    }

    public void setTermComponentList(Collection<RefexDynamicMember> componentList) {
        this.refsetMemberList = componentList;
    }
}
