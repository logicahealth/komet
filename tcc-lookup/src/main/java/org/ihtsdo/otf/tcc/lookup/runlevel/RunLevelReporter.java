/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.otf.tcc.lookup.runlevel;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.runlevel.ChangeableRunLevelFuture;
import org.glassfish.hk2.runlevel.ErrorInformation;
import org.glassfish.hk2.runlevel.RunLevelFuture;
import org.glassfish.hk2.runlevel.RunLevelListener;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author kec
 */
@Service
public class RunLevelReporter implements RunLevelListener {

    private static final Logger log = LogManager.getLogger();

    @Override
    public void onProgress(ChangeableRunLevelFuture currentJob,
                           int levelAchieved) {
        log.info("Achieved run level: " + levelAchieved);
    }

    @Override
    public void onCancelled(RunLevelFuture currentJob,
                            int levelAchieved) {
        log.info("Cancelled at run level: " + levelAchieved);
    }

    @Override
    public void onError(RunLevelFuture currentJob, ErrorInformation info) {
        log.error("Error while progressing to level: " + currentJob.getProposedLevel(),
                info.getError());
    }
    
}