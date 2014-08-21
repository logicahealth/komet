package org.ihtsdo.otf.tcc.model.cc.concept;

/**
 * Created by kec on 7/29/14.
 */
public interface ModificationTracker {
    void modified();

    void modified(long sequence);
}
