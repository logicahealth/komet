/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.api.snapshot.calculator;

import gov.vha.isaac.ochre.collections.StampSequenceSet;

/**
 *
 * @author kec
 */
public class LatestStampResult {

    private final StampSequenceSet latestStamps = new StampSequenceSet();

    public void addAll(StampSequenceSet stamps) {
        latestStamps.or(stamps);
    }
    
    public StampSequenceSet getLatestStamps() {
        return StampSequenceSet.of(latestStamps);
    }
    
    public void add(int stamp) {
        latestStamps.add(stamp);
    }

    public void reset() {
        latestStamps.clear();
    }

}
