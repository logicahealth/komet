/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.otf.tcc.chronicle.cc;

//~--- non-JDK imports --------------------------------------------------------
//~--- JDK imports ------------------------------------------------------------
import org.ihtsdo.otf.tcc.chronicle.cc.Path;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.io.Serializable;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.otf.tcc.api.coordinate.PathBI;
import org.ihtsdo.otf.tcc.api.coordinate.PositionBI;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;

public class Position implements PositionBI, Serializable {

    private static final int dataVersion = 1;
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();

        if (objDataVersion == 1) {
            time = in.readLong();
            path = (PathBI) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeLong(time);
        out.writeObject(path);
    }
    static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
    //~--- fields --------------------------------------------------------------
    private PathBI path;
    private long time;

    //~--- constructors --------------------------------------------------------
    public Position(long time, PathBI path) {
        super();

        if (path == null) {
            throw new IllegalArgumentException("path cannot be null");
        }

        this.time = time;
        this.path = path;
    }

    //~--- methods -------------------------------------------------------------

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.vodb.types.I_Position#checkAntecedentOrEqualToOrigins(java.util
     * .List)
     */
    @Override
    public boolean checkAntecedentOrEqualToOrigins(Collection<? extends PositionBI> origins) {
        for (PositionBI origin : origins) {
            if (path.getConceptNid() == origin.getPath().getConceptNid()) {
                return time <= origin.getTime();
            } else if (checkAntecedentOrEqualToOrigins(origin.getPath().getOrigins())) {
                return true;
            }
        }

        return false;
    }
    
    private boolean checkAntecedentOrEqualToOrigins(Collection<? extends PositionBI> origins, long testTime,
            int testPathId) {
        for (PositionBI origin : origins) {
            if (testPathId == origin.getPath().getConceptNid()) {
                return origin.getTime() <= testTime;
            } else if (checkAntecedentOrEqualToOrigins(origin.getPath().getOrigins(), testTime, testPathId)) {
                return true;
            }
        }

        return false;
    }

    private boolean checkSubsequentOrEqualToOrigins(Collection<? extends PositionBI> origins, long testTime,
            int testPathId) {
        for (PositionBI origin : origins) {
            if (testPathId == origin.getPath().getConceptNid()) {
                return origin.getTime() >= testTime;
            } else if (checkSubsequentOrEqualToOrigins(origin.getPath().getOrigins(), testTime, testPathId)) {
                return true;
            }
        }

        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_Position#equals(org.dwfa.vodb.types.Position)
     */
    public boolean equals(PositionBI another) {
        return ((time == another.getTime()) && (path.getConceptNid() == another.getPath().getConceptNid()));
    }

    @Override
    public boolean equals(Object obj) {
        if (PositionBI.class.isAssignableFrom(obj.getClass())) {
            return equals((PositionBI) obj);
        }

        return false;
    }

    @Override
    public boolean equals(long time, int pathId) {
        return ((this.time == time) && (path.getConceptNid() == pathId));
    }

    @Override
    public int hashCode() {
        return Hashcode.computeLong(time, path.getConceptNid());
    }

    @SuppressWarnings("unchecked")
    public static PositionBI readPosition(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int version = in.readInt();
        int pathConceptId;

        List<UUID> pathIdList = (List<UUID>) in.readObject();

        if (Ts.get().hasUuid(pathIdList)) {
            pathConceptId = Ts.get().getNidForUuids(pathIdList);
        } else {
            pathConceptId = TermAux.WB_AUX_PATH.getLenient().getNid();
        }

        int size = in.readInt();
        List<PositionBI> origins = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            origins.add(readPosition(in));
        }

        Path p = new Path(pathConceptId, origins);

        return new Position(version, p);
    }

    public static Set<PositionBI> readPositionSet(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        int size = in.readInt();
        Set<PositionBI> positions = Collections.synchronizedSet(new HashSet<PositionBI>(size));

        for (int i = 0; i < size; i++) {
            try {
                PositionBI position = readPosition(in);
                ConceptChronicleBI pathConcept = Ts.get().getConcept(position.getPath().getConceptNid());
                PathBI path = Ts.get().getPath(pathConcept.getNid());

                positions.add(Ts.get().newPosition(path, position.getTime()));
            } catch (NullPointerException npe) {
                Logger.getLogger(Position.class.getName()).log(Level.SEVERE, "readPositionSet position not found");
            } 
        }

        return positions;
    }

    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        try {
            ConceptChronicleBI cb = Ts.get().getConcept(path.getConceptNid());

            buff.append(cb.toUserString());
        } catch (IOException e) {
            buff.append(e.getMessage());
             Logger.getLogger(Position.class.getName()).log(Level.SEVERE, null, e);
        }
        
        buff.append(": ");

        if (time == Long.MAX_VALUE) {
            buff.append("Latest");
        } else if (time == Long.MIN_VALUE) {
            buff.append("BOT");
        } else {
            Date positionDate = new Date(time);

            buff.append(dateFormatter.format(positionDate));
        }

        return buff.toString();
    }

    public static void writePosition(ObjectOutputStream out, PositionBI p) throws IOException {
        out.writeLong(p.getTime());

        if (Ts.get().getUuidsForNid(p.getPath().getConceptNid()) != null) {
            out.writeObject(Ts.get().getUuidsForNid(p.getPath().getConceptNid()));
        } else {
            out.writeObject(TermAux.WB_AUX_PATH.getLenient().getUUIDs());
        }

        out.writeInt(p.getPath().getOrigins().size());

        for (PositionBI origin : p.getPath().getOrigins()) {
            writePosition(out, origin);
        }
    }

    public static void writePositionSet(ObjectOutputStream out, Set<PositionBI> viewPositions)
            throws IOException {
        out.writeInt(viewPositions.size());

        for (PositionBI p : viewPositions) {
            writePosition(out, p);
        }
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public Collection<? extends PositionBI> getAllOrigins() {
        return path.getOrigins();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_Position#getDepth(int)
     */
    public int getDepth(int pathId) {
        int depth = 0;

        if (pathId == path.getConceptNid()) {
            return depth;
        }

        List<PositionBI> depthOrigins = new ArrayList<>(path.getOrigins());

        while (depthOrigins.size() > 0) {
            depth++;

            for (PositionBI o : depthOrigins) {
                if (o.getPath().getConceptNid() == pathId) {
                    return depth;
                }
            }

            List<PositionBI> newOrigins = new ArrayList<>();

            for (PositionBI p : depthOrigins) {
                newOrigins.addAll(p.getPath().getOrigins());
            }

            depthOrigins = newOrigins;
        }

        return Integer.MAX_VALUE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_Position#getPath()
     */
    @Override
    public PathBI getPath() {
        return path;
    }

    public int getPositionId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getTime() {
        return time;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.vodb.types.I_Position#isAntecedentOrEqualTo(org.dwfa.vodb.types
     * .Position)
     */
    @Override
    public boolean isAntecedentOrEqualTo(PositionBI another) {
        if (equals(another)) {
            return true;
        }

        if (path.getConceptNid() == another.getPath().getConceptNid()) {
            return time <= another.getTime();
        }

        return checkAntecedentOrEqualToOrigins(another.getPath().getOrigins());
    }

    @Override
    public boolean isAntecedentOrEqualTo(long time, int pathId) {
        if (equals(time, pathId)) {
            return true;
        }

        if (path.getConceptNid() == pathId) {
            return this.time <= time;
        }

        return checkAntecedentOrEqualToOrigins(path.getOrigins(), time, pathId);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.vodb.types.I_Position#isSubsequentOrEqualTo(org.dwfa.vodb.types
     * .I_Position)
     */
    @Override
    public boolean isSubsequentOrEqualTo(PositionBI another) {
        return another.isAntecedentOrEqualTo(this);
    }

    @Override
    public boolean isSubsequentOrEqualTo(long time, int pathId) {
        if (equals(time, pathId)) {
            return true;
        }

        if (path.getConceptNid() == pathId) {
            return this.time >= time;
        }

        return checkSubsequentOrEqualToOrigins(path.getOrigins(), time, pathId);
    }

    @Override
    public Map<Long, ? extends PositionBI> getIntersections() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<? extends PositionBI> getBarriers() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
