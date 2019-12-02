package sh.isaac.provider.stamp;

import sh.isaac.api.datastore.ExtendedStoreData;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class FileStoreMapData<K, V> implements ExtendedStoreData<K, V> {
    protected ConcurrentHashMap<K, V> backingMap = new ConcurrentHashMap<>();
    final File dataFile;

    public FileStoreMapData(File dataFile) throws IOException {
        this.dataFile = dataFile;
        if (!this.dataFile.exists()) {
            this.dataFile.getParentFile().mkdirs();
            this.dataFile.createNewFile();
        }
        if (this.dataFile.length() > 1) {
            try (DataInputStream dis = new DataInputStream(new FileInputStream(dataFile))) {
                int size = dis.readInt();
                for (int i = 0; i < size; i++) {
                    Map.Entry<K, V> entry = readEntry(dis);
                    backingMap.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    public void save() throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(dataFile))) {
            HashMap<K, V> mapToWrite = new HashMap<>(backingMap);
            dos.writeInt(mapToWrite.size());
            for (Map.Entry<K, V> entry: mapToWrite.entrySet()) {
                writeEntry(entry, dos);
            }
        }
    }

    protected abstract Map.Entry<K, V> readEntry(DataInputStream dis) throws IOException;
    protected abstract void writeEntry( Map.Entry<K, V> entry, DataOutputStream dos) throws IOException;

    @Override
    public V remove(K key) {
        return backingMap.remove(key);
    }

    @Override
    public V get(K key) {
        return backingMap.get(key);
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return backingMap.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public V put(K key, V value) {
        return backingMap.put(key, value);
    }

    @Override
    public V accumulateAndGet(K key, V newData, BinaryOperator<V> accumulatorFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Map.Entry<K, V>> getEntrySet() {
        return backingMap.entrySet();
    }

    @Override
    public void clearStore() {
        backingMap.clear();
    }

    @Override
    public int size() {
        return backingMap.size();
    }

    @Override
    public long sizeAsLong() {
        return backingMap.size();
    }

    @Override
    public Stream<V> getValueStream() {
        return backingMap.values().stream();
    }

    @Override
    public Stream<Map.Entry<K, V>> getStream() {
        return backingMap.entrySet().stream();
    }
}
