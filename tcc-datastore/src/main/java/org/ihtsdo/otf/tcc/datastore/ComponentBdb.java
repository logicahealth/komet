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

    protected Database readOnly = null;
    protected Database mutable = null;

    public ComponentBdb(Bdb readOnlyBdbEnv, Bdb mutableBdbEnv)
            throws IOException {
        try {
            readOnly = Bdb.setupDatabase(readOnlyBdbEnv.bdbEnv.getConfig().getReadOnly(),
                    getDbName(), readOnlyBdbEnv);
        } catch (DatabaseException e) {
            AceLog.getAppLog().warning(e.getLocalizedMessage());
        }
        mutable = Bdb.setupDatabase(false, getDbName(), mutableBdbEnv);
        init();
    }

    protected abstract void init() throws IOException;

    protected abstract String getDbName();

    protected void closeReadOnly() {
        if (readOnly != null) {
            readOnly.close();
            readOnly = null;
        }
    }

    protected void preloadBoth() {
        preload(readOnly);
        preload(mutable);
    }
    
    protected void preloadReadOnly() {
        preload(readOnly);
    }

    protected void preloadMutable() {
        preload(mutable);
    }

    private void preload(Database db) {
        PreloadConfig plConfig = new PreloadConfig();
        plConfig.setLoadLNs(true);
        db.preload(plConfig);
    }

    public void close() {
        try {
            sync();
            if (readOnly != null) {
                readOnly.close();
            }
            mutable.close();
        } catch (IllegalStateException ex) {
            if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                AceLog.getAppLog().warning(ex.toString());
            }
        } catch (IOException e) {
            if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                AceLog.getAppLog().severe(e.toString());
            }
        }
    }

    public void sync() throws IOException {
        if (readOnly != null && readOnly.getConfig().getReadOnly() == false) {
            readOnly.sync();
        }
        mutable.sync();
    }

    public Database getReadOnly() {
        return readOnly;
    }

    public Database getReadWrite() {
        return mutable;
    }
}
