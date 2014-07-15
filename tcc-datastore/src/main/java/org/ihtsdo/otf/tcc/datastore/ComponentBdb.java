package org.ihtsdo.otf.tcc.datastore;

import java.io.IOException;
import java.util.logging.Level;


import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.PreloadConfig;
import org.ihtsdo.otf.tcc.datastore.temp.AceLog;

/**
 * 
 * @author kec
 * 
 */
public abstract class ComponentBdb {

    protected Database mutable = null;

    public ComponentBdb(Bdb mutableBdbEnv)
            throws IOException {
        init();
    }

    protected abstract void init() throws IOException;

    protected abstract String getDbName();


    protected void preload() {
        PreloadConfig plConfig = new PreloadConfig();
        plConfig.setLoadLNs(false);
        mutable.preload(plConfig);
    }


    public void close() {
        try {
            sync();
            mutable.close();
        } catch (IllegalStateException ex) {
            if (AceLog.getAppLog().isLoggable(Level.INFO)) {
                AceLog.getAppLog().warning(ex.toString());
            }
        } catch (IOException e) {
            if (AceLog.getAppLog().isLoggable(Level.INFO)) {
                AceLog.getAppLog().severe(e.toString());
            }
        }
    }

    public void sync() throws IOException {
        mutable.sync();
    }

    public Database getReadWrite() {
        return mutable;
    }
}
