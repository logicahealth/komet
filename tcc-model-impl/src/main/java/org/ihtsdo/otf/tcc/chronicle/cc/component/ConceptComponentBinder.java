package org.ihtsdo.otf.tcc.chronicle.cc.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.ihtsdo.otf.tcc.chronicle.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.chronicle.cc.concept.I_BindConceptComponents;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.DatabaseEntry;
import java.util.HashSet;
import org.ihtsdo.otf.tcc.chronicle.cc.P;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;

public class ConceptComponentBinder<V extends Revision<V, C>, C extends ConceptComponent<V, C>>
        extends TupleBinding<Collection<C>>
        implements I_BindConceptComponents {

    private static final int maxReadOnlyStatusAtPositionId =
            P.s.getMaxReadOnlyStamp();
    private ConceptChronicle enclosingConcept;
    private ArrayList<C> readOnlyConceptComponentList;
    private ComponentFactory<V, C> factory;
    private AtomicInteger componentsEncountered;
    private AtomicInteger componentsWritten;

    public ConceptComponentBinder(ComponentFactory<V, C> factory,
            AtomicInteger componentsEncountered,
            AtomicInteger componentsWritten) {
        super();
        this.factory = factory;
        this.componentsEncountered = componentsEncountered;
        this.componentsWritten = componentsWritten;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ArrayList<C> entryToObject(TupleInput input) {
        assert enclosingConcept != null : "enclosing concept cannot be null.";
        int listSize = input.readInt();
        assert listSize >= 0 : "Processing nid: " + enclosingConcept.getNid()
                + " listSize: " + listSize
                + "\ndata: " + new DatabaseEntry(input.getBufferBytes()).toString();
        assert listSize < 1000000 : "Processing nid: " + enclosingConcept.getNid()
                + " listSize: " + listSize
                + "\ndata: " + new DatabaseEntry(input.getBufferBytes()).toString();
        if (readOnlyConceptComponentList != null) {
            readOnlyConceptComponentList.ensureCapacity(listSize
                    + readOnlyConceptComponentList.size());
        }
        ArrayList<C> newConceptComponentList;
        HashMap<Integer, C> nidToConceptComponentMap = null;
        if (readOnlyConceptComponentList != null) {
            newConceptComponentList = readOnlyConceptComponentList;
            nidToConceptComponentMap = new HashMap<>(listSize);
            for (C component : readOnlyConceptComponentList) {
                nidToConceptComponentMap.put(component.nid, component);
            }
        } else {
            newConceptComponentList = new ArrayList<>(listSize);
        }
        for (int index = 0; index < listSize; index++) {
            // All components must write the nid first...
            input.mark(16);
            int nid = input.readInt();
            // we have to put it back so the component can read it again...
            input.reset();
            C conceptComponent = (C) ConceptChronicle.componentsCRHM.get(nid);
            if (conceptComponent != null && conceptComponent.getTime() == Long.MIN_VALUE) {
                conceptComponent = null;
                ConceptChronicle.componentsCRHM.remove(nid);
            }
            if (nidToConceptComponentMap != null
                    && nidToConceptComponentMap.containsKey(nid)) {
                if (conceptComponent == null) {
                    conceptComponent = nidToConceptComponentMap.get(nid);
                    C oldComponent = (C) ConceptChronicle.componentsCRHM.putIfAbsent(conceptComponent.nid, conceptComponent);
                    if (oldComponent != null) {
                        conceptComponent = oldComponent;
                        if (nidToConceptComponentMap != null) {
                            nidToConceptComponentMap.put(nid, oldComponent);
                        }
                    }
                }
                try {
                    conceptComponent.merge(factory.create(enclosingConcept, input), new HashSet<ConceptChronicleBI>());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                try {
                    if (conceptComponent == null) {
                        conceptComponent = factory.create(enclosingConcept, input);
                        if (conceptComponent.getTime() != Long.MIN_VALUE) {
                            C oldComponent = (C) ConceptChronicle.componentsCRHM.putIfAbsent(conceptComponent.nid, conceptComponent);
                            if (oldComponent != null) {
                                conceptComponent = oldComponent;
                                if (nidToConceptComponentMap != null) {
                                    nidToConceptComponentMap.put(nid, oldComponent);
                                }
                            }
                        }
                    } else {
                        conceptComponent.merge(factory.create(enclosingConcept, input), new HashSet<ConceptChronicleBI>());
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (conceptComponent.getTime() != Long.MIN_VALUE) {
                    newConceptComponentList.add(conceptComponent);
                }
            }
        }
        newConceptComponentList.trimToSize();
        return newConceptComponentList;
    }

    @Override
    public void objectToEntry(Collection<C> conceptComponentList, TupleOutput output) {
        List<C> componentListToWrite = new ArrayList<>(conceptComponentList.size());
        for (C conceptComponent : conceptComponentList) {
            componentsEncountered.incrementAndGet();
            if (conceptComponent.stampIsInRange(maxReadOnlyStatusAtPositionId + 1, Integer.MAX_VALUE) 
                    && conceptComponent.getTime() != Long.MIN_VALUE) {
                componentListToWrite.add(conceptComponent);
            } else {
                if (conceptComponent.revisions != null) {
                    for (V part : conceptComponent.revisions) {
                        assert part.getStamp() != Integer.MAX_VALUE;
                        if (part.getStamp() > maxReadOnlyStatusAtPositionId
                                && part.getTime() != Long.MIN_VALUE) {
                            componentListToWrite.add(conceptComponent);
                            break;
                        }
                    }
                }
            }
        }
        output.writeInt(componentListToWrite.size()); // List size
        for (C conceptComponent : componentListToWrite) {
            componentsWritten.incrementAndGet();
            conceptComponent.writeComponentToBdb(output, maxReadOnlyStatusAtPositionId);
        }
    }

    @Override
    public ConceptChronicle getEnclosingConcept() {
        return enclosingConcept;
    }

    @Override
    public void setupBinder(ConceptChronicle enclosingConcept) {
        this.enclosingConcept = enclosingConcept;
        this.readOnlyConceptComponentList = null;
    }

    public void setTermComponentList(ArrayList<C> componentList) {
        this.readOnlyConceptComponentList = componentList;
    }
}
