package org.ihtsdo.otf.tcc.model.cc.component;

import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;

/**
 * Created by kec on 7/14/14.
 */
public interface CollectionCollector<E> {
    void init(int size);

    void add(E e);
}
