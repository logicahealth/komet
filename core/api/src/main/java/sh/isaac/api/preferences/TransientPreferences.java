package sh.isaac.api.preferences;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.BackingStoreException;
import java.util.Base64;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeListener;

public class TransientPreferences implements IsaacPreferences {
    ConcurrentHashMap<String, String> preferenceMap = new ConcurrentHashMap<>();
    private final String name;

    public TransientPreferences(UUID nodeUuid) {
        this.name = nodeUuid.toString();
    }

    @Override
    public void put(String key, String value) {
        preferenceMap.put(key, value);
    }

    @Override
    public String get(String key, String defaultValue) {
        return preferenceMap.getOrDefault(key, defaultValue);
    }

    @Override
    public void remove(String key) {
        preferenceMap.remove(key);
    }

    @Override
    public void clear() {
        preferenceMap.clear();
    }

    @Override
    public void putInt(String key, int value) {
        put(key, Integer.toString(value));
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return Integer.valueOf(get(key, Integer.toString(defaultValue)));
    }

    @Override
    public void putLong(String key, long value) {
        put(key, Long.toString(value));
    }

    @Override
    public long getLong(String key, long defaultValue) {
        return Long.valueOf(get(key, Long.toString(defaultValue)));
    }

    @Override
    public void putBoolean(String key, boolean value) {
        put(key, Boolean.toString(value));
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.valueOf(get(key, Boolean.toString(defaultValue)));
    }

    @Override
    public void putDouble(String key, double value) {
        put(key, Double.toString(value));
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        return Double.valueOf(get(key, Double.toString(defaultValue)));
    }

    @Override
    public void putByteArray(String key, byte[] value) {
        put(key, Base64.getEncoder().encodeToString(value));
    }

    @Override
    public byte[] getByteArray(String key, byte[] defaultValue) {
        return Base64.getDecoder().decode(get(key, Base64.getEncoder().encodeToString(defaultValue)));
    }

    @Override
    public String[] keys() throws BackingStoreException {
        return preferenceMap.keySet().toArray(new String[preferenceMap.size()]);
    }

    @Override
    public String[] childrenNames() throws BackingStoreException {
        throw new UnsupportedOperationException();
    }

    @Override
    public IsaacPreferences parent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IsaacPreferences node(String pathName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean nodeExists(String pathName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeNode()  {
        throw new UnsupportedOperationException();
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String absolutePath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PreferenceNodeType getNodeType() {
        return PreferenceNodeType.TRANSIENT;
    }

    @Override
    public void flush() throws BackingStoreException {
        // transient, nothing to do.
    }

    @Override
    public void sync() throws BackingStoreException {
        // transient, nothing to do.
    }

    @Override
    public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removePreferenceChangeListener(PreferenceChangeListener pcl) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addNodeChangeListener(NodeChangeListener ncl) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeNodeChangeListener(NodeChangeListener ncl) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void exportNode(OutputStream os) throws IOException, BackingStoreException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void exportSubtree(OutputStream os) throws IOException, BackingStoreException {
        throw new UnsupportedOperationException();
    }
}
