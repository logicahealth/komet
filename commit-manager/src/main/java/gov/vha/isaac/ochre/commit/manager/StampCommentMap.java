/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.commit.manager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.mahout.math.map.OpenIntObjectHashMap;

/**
 *
 * @author kec
 */
public class StampCommentMap {

    ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    OpenIntObjectHashMap<String> stampCommentMap = new OpenIntObjectHashMap();

    public int getSize() {
        return stampCommentMap.size();
    }

    public void addComment(int stamp, String comment) {
        rwl.writeLock().lock();
        try {
            if (comment != null) {
                stampCommentMap.put(stamp, comment);
            } else {
                stampCommentMap.removeKey(stamp);
            }
            
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * 
     * @param stamp
     * @return Comment associated with the stamp. 
     */
    public Optional<String> getComment(int stamp) {
        rwl.readLock().lock();
        try {
            return Optional.ofNullable(stampCommentMap.get(stamp));
        } finally {
            rwl.readLock().unlock();
        }
    }

    public void write(File mapFile) throws IOException {
        try (DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(mapFile)))) {
            output.writeInt(stampCommentMap.size());
            stampCommentMap.forEachPair((int nid, String comment) -> {
                try {
                    output.writeInt(nid);
                    output.writeUTF(comment);
                    return true;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
    }

    public void read(File mapFile) throws IOException {
        try (DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(mapFile)))) {
            int size = input.readInt();
            stampCommentMap.ensureCapacity(size);
            for (int i = 0; i < size; i++) {
                int stamp = input.readInt();
                String comment = input.readUTF();
                stampCommentMap.put(stamp, comment);
            }
        }
    }
    
}
