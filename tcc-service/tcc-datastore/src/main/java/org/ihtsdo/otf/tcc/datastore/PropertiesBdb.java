package org.ihtsdo.otf.tcc.datastore;

/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import org.ihtsdo.otf.tcc.datastore.temp.AceLog;

public class PropertiesBdb extends ComponentBdb {

    private TupleBinding<String> stringBinder = TupleBinding.getPrimitiveBinding(String.class);

    public PropertiesBdb(Bdb readOnlyBdbEnv, Bdb mutableBdbEnv)
            throws IOException {
        super(readOnlyBdbEnv, mutableBdbEnv);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.impl.I_StoreMetaData#getProperty(java.lang.String)
     */
    public String getProperty(String key) throws IOException {
        DatabaseEntry propKey = new DatabaseEntry();
        DatabaseEntry propValue = new DatabaseEntry();

        stringBinder.objectToEntry(key, propKey);
        try {
            if (mutable.get(null, propKey, propValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                return (String) stringBinder.entryToObject(propValue);
            }
            if (readOnly.get(null, propKey, propValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                return (String) stringBinder.entryToObject(propValue);
            }
        } catch (DatabaseException e) {
            throw new IOException(e);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.impl.I_StoreMetaData#getProperties()
     */
    public Map<String, String> getProperties() throws IOException {
        try {
            Cursor propCursor = mutable.openCursor(null, null);
            DatabaseEntry foundKey = new DatabaseEntry();
            DatabaseEntry foundData = new DatabaseEntry();
            HashMap<String, String> propMap = new HashMap<>();
            try {
                while (propCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                    String key = (String) stringBinder.entryToObject(foundKey);
                    String value = (String) stringBinder.entryToObject(foundData);
                    propMap.put(key, value);
                }
            } finally {
                propCursor.close();
            }
            propCursor = readOnly.openCursor(null, null);
            try {
                while (propCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                    String key = (String) stringBinder.entryToObject(foundKey);
                    String value = (String) stringBinder.entryToObject(foundData);
                    if (propMap.containsKey(key) == false) {
                        propMap.put(key, value);
                    }
                }
            } finally {
                propCursor.close();
            }
            return Collections.unmodifiableMap(propMap);
        } catch (DatabaseException | IllegalArgumentException e) {
            throw new IOException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.impl.I_StoreMetaData#setProperty(java.lang.String,
     * java.lang.String)
     */
    public void setProperty(String key, String value) throws IOException {
        DatabaseEntry propKey = new DatabaseEntry();
        DatabaseEntry propValue = new DatabaseEntry();
        stringBinder.objectToEntry(key, propKey);
        stringBinder.objectToEntry(value, propValue);
        try {
            mutable.put(null, propKey, propValue);
        } catch (DatabaseException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void close() throws DatabaseException {
        try {
            sync();
        } catch (IOException e) {
            AceLog.getAppLog().severe(e.getLocalizedMessage(), e);
        }
        super.close();
    }

    @Override
    public void sync() throws IOException {
        super.sync();
    }

    @Override
    protected String getDbName() {
        return "properties";
    }

    @Override
    protected void init() throws IOException {
        // Nothing to do...
    }
}
