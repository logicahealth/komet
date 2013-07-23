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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.otf.tcc.api.coordinate.PathBI;
import org.ihtsdo.otf.tcc.api.coordinate.PositionBI;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;

public class Path implements PathBI, Serializable {

    private static final int dataVersion = 1;
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();

        if (objDataVersion == 1) {
            UUID conceptUuid = (UUID) in.readObject();
            conceptNid = Ts.get().getNidForUuids(conceptUuid);
            origins = (Set<PositionBI>) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(Ts.get().getUuidPrimordialForNid(conceptNid));
        out.writeObject(origins);
    }
    /**
     *
     */
    int conceptNid;
    Set<PositionBI> origins;

    public Path(int conceptId, List<? extends PositionBI> origins) {
        super();
        this.conceptNid = conceptId;
        if (origins != null) {
            this.origins = new CopyOnWriteArraySet<>(origins);
        } else {
            this.origins = new CopyOnWriteArraySet<>();
        }
    }

    public boolean equals(PathBI another) {
        return (conceptNid == another.getConceptNid());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (PathBI.class.isAssignableFrom(obj.getClass())) {
            return equals((PathBI) obj);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return conceptNid;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.PathBI#getConceptNid()
     */
    @Override
    public int getConceptNid() {
        return conceptNid;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.PathBI#getOrigins()
     */
    @Override
    public Collection<? extends PositionBI> getOrigins() {
        return Collections.unmodifiableSet(origins);
    }

    @Override
    public Set<PositionBI> getInheritedOrigins() {
        HashSet<PositionBI> inheritedOrigins = new HashSet<>();
        for (PositionBI origin : this.origins) {
            inheritedOrigins.addAll(origin.getPath().getInheritedOrigins());
            inheritedOrigins.add(origin);
        }
        return inheritedOrigins;
    }

    @Override
    public Set<PositionBI> getNormalisedOrigins() {
        return getNormalisedOrigins(null);
    }

    public Set<PositionBI> getNormalisedOrigins(Collection<PathBI> paths) {
        final Set<PositionBI> inheritedOrigins = getInheritedOrigins();
        if (paths != null) {
            for (PathBI path : paths) {
                if (path != this) {
                    inheritedOrigins.addAll(path.getInheritedOrigins());
                }
            }
        }
        Set<PositionBI> normalisedOrigins = new HashSet<>(inheritedOrigins);
        for (PositionBI a : inheritedOrigins) {
            for (PositionBI b : inheritedOrigins) {
                if ((a.getPath().getConceptNid()) == b.getPath().getConceptNid() && (a.getTime() < b.getTime())) {
                    normalisedOrigins.remove(a);
                }
            }
        }
        return normalisedOrigins;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.PathBI#getMatchingPath(int)
     */
    @Override
    public PathBI getMatchingPath(int pathId) {
        if (conceptNid == pathId) {
            return this;
        }
        for (PositionBI origin : origins) {
            if (origin.getPath().getMatchingPath(pathId) != null) {
                return origin.getPath();
            }
        }
        return null;
    }

    public static String toHtmlString(PathBI path) throws IOException {
        StringBuilder buff = new StringBuilder();
        buff.append("<html><font color='blue' size='+1'><u>");
        ConceptChronicleBI cb = Ts.get().getConcept(path.getConceptNid());
        buff.append(cb.toUserString());
        buff.append("</u></font>");
        for (PositionBI origin : path.getOrigins()) {
            buff.append("<br>&nbsp;&nbsp;&nbsp;Origin: ");
            buff.append(origin);
        }
        return buff.toString();
    }

    public static void writePath(ObjectOutputStream out, PathBI p) throws IOException {
        List<UUID> uuids = Ts.get().getUuidsForNid(p.getConceptNid());
        if (uuids.size() > 0) {
            out.writeObject(Ts.get().getUuidsForNid(p.getConceptNid()));
        } else {
            throw new IOException("no uuids for component: " + p);
        }
        out.writeInt(p.getOrigins().size());
        for (PositionBI origin : p.getOrigins()) {
            Position.writePosition(out, origin);
        }
    }

    @SuppressWarnings("unchecked")
    public static PathBI readPath(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int pathId;
        List<UUID> pathIdList = (List<UUID>) in.readObject();
        if (Ts.get().hasUuid(pathIdList)) {
            pathId = Ts.get().getNidForUuids(pathIdList);
        } else {
            pathId = TermAux.WB_AUX_PATH.getLenient().getNid();
            Logger.getLogger(Path.class.getName()).log(Level.SEVERE, "ReadPath error. {0} missing. Substuting WB Aux ", pathIdList);
        }
        int size = in.readInt();
        List<PositionBI> origins = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            origins.add(Position.readPosition(in));
        }
        return new Path(pathId, origins);
    }

    public static Set<PathBI> readPathSet(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int size = in.readInt();
        Set<PathBI> positions = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            positions.add(readPath(in));
        }
        return positions;
    }

    public static void writePathSet(ObjectOutputStream out, Set<PathBI> viewPositions) throws IOException {
        out.writeInt(viewPositions.size());
        for (PathBI p : viewPositions) {
            writePath(out, p);
        }
    }

    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        try {
            ConceptChronicleBI cb = Ts.get().getConcept(getConceptNid());
            buff.append(cb.toUserString());
        } catch (IOException e) {
            buff.append(e.getMessage());
            Logger.getLogger(Path.class.getName()).log(Level.SEVERE, null, e);
        }
        return buff.toString();
    }

    @Override
    public String toHtmlString() throws IOException {
        return Path.toHtmlString(this);
    }

    @Override
    public List<UUID> getUUIDs() {
        try {
            return new ArrayList<>(Ts.get().getUuidsForNid(conceptNid));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
