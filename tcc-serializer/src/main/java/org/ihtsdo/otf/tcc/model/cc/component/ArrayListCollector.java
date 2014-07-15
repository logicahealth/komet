package org.ihtsdo.otf.tcc.model.cc.component;

import java.util.ArrayList;

/**
 * Created by kec on 7/14/14.
 */
public class ArrayListCollector<E> implements CollectionCollector<E> {
    ArrayList<E> collection;

    public ArrayList<E> getCollection() {
        return collection;
    }

    @Override
    public void init(int size) {
        collection = new ArrayList<E>(size);
    }

    @Override
    public void add(E e) {
        collection.add(e);
    }


}
