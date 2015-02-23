package org.ihtsdo.otf.tcc.api.coordinate;

import gov.vha.isaac.ochre.api.coordinate.StampPath;
import gov.vha.isaac.ochre.api.coordinate.StampPosition;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.time.Instant;
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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.store.Ts;

@XmlRootElement(name = "position")
@XmlAccessorType(XmlAccessType.PROPERTY)

public class Position implements Comparable<Position>, StampPosition, Externalizable {


    private static final int dataVersion = 1;
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public Position(Position another) {
        this.time = another.getTime();
        this.path = new Path(another.getPath());
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(dataVersion);
        out.writeLong(time);
        out.writeObject(path);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();

        if (objDataVersion == 1) {
            time = in.readLong();
            path = (Path) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
    //~--- fields --------------------------------------------------------------
    private Path path;
    private long time;

    /**
     * No arg constructor for JAXB
     */
    public Position() {
    }

    public Position(SimplePosition another) {
        this.time = another.getTimePoint();
        this.path = new Path(another.getPath());
    }

    //~--- constructors --------------------------------------------------------
    public Position(long time, Path path) {
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
    public boolean checkAntecedentOrEqualToOrigins(Collection<? extends Position> origins) {
        for (Position origin : origins) {
            if (path.getConceptNid() == origin.getPath().getConceptNid()) {
                return time <= origin.getTime();
            } else if (checkAntecedentOrEqualToOrigins(origin.getPath().getOrigins())) {
                return true;
            }
        }

        return false;
    }
    
    private boolean checkAntecedentOrEqualToOrigins(Collection<? extends Position> origins, long testTime,
            int testPathId) {
        for (Position origin : origins) {
            if (testPathId == origin.getPath().getConceptNid()) {
                return origin.getTime() <= testTime;
            } else if (checkAntecedentOrEqualToOrigins(origin.getPath().getOrigins(), testTime, testPathId)) {
                return true;
            }
        }

        return false;
    }

    private boolean checkSubsequentOrEqualToOrigins(Collection<? extends Position> origins, long testTime,
            int testPathId) {
        for (Position origin : origins) {
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
     * @see org.dwfa.vodb.types.I_Position#equals(org.dwfa.vodb.types.OldPosition)
     */
    public boolean equals(Position another) {
        return (time == another.getTime() && path.equals(another.getPath()));
    }

    @Override
    public boolean equals(Object obj) {
        if (Position.class.isAssignableFrom(obj.getClass())) {
            return equals((Position) obj);
        }

        return false;
    }

    public boolean equals(long time, int pathId) {
        return ((this.time == time) && (path.getConceptNid() == pathId));
    }

    @Override
    public int hashCode() {
        return Hashcode.computeLong(time, path.hashCode());
    }

    @SuppressWarnings("unchecked")
    public static Position readPosition(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int version = in.readInt();
        int pathConceptId;

        List<UUID> pathIdList = (List<UUID>) in.readObject();

        if (Ts.get().hasUuid(pathIdList)) {
            pathConceptId = Ts.get().getNidForUuids(pathIdList);
        } else {
            pathConceptId = TermAux.WB_AUX_PATH.getLenient().getNid();
        }

        int size = in.readInt();
        List<Position> origins = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            origins.add(readPosition(in));
        }

        Path p = new Path(pathConceptId, origins);

        return new Position(version, p);
    }

    public static Set<Position> readPositionSet(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        int size = in.readInt();
        Set<Position> positions = Collections.synchronizedSet(new HashSet<Position>(size));

        for (int i = 0; i < size; i++) {
            try {
                Position position = readPosition(in);
                ConceptChronicleBI pathConcept = Ts.get().getConcept(position.getPath().getConceptNid());
                Path path = Ts.get().getPath(pathConcept.getNid());

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
            if (path != null) {
                ConceptChronicleBI cb = Ts.get().getConcept(path.getConceptNid());
                buff.append(cb.toUserString());
            } else {
                buff.append("null path");
            }
            
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

    public static void writePosition(ObjectOutputStream out, Position p) throws IOException {
        out.writeLong(p.getTime());

        if (Ts.get().getUuidsForNid(p.getPath().getConceptNid()) != null) {
            out.writeObject(Ts.get().getUuidsForNid(p.getPath().getConceptNid()));
        } else {
            out.writeObject(TermAux.WB_AUX_PATH.getLenient().getUUIDs());
        }

        out.writeInt(p.getPath().getOrigins().size());

        for (Position origin : p.getPath().getOrigins()) {
            writePosition(out, origin);
        }
    }

    public static void writePositionSet(ObjectOutputStream out, Set<Position> viewPositions)
            throws IOException {
        out.writeInt(viewPositions.size());

        for (Position p : viewPositions) {
            writePosition(out, p);
        }
    }

    //~--- get methods ---------------------------------------------------------
    public Collection<? extends Position> getOrigins() {
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

        List<Position> depthOrigins = new ArrayList<>(path.getOrigins());

        while (depthOrigins.size() > 0) {
            depth++;

            for (Position o : depthOrigins) {
                if (o.getPath().getConceptNid() == pathId) {
                    return depth;
                }
            }

            List<Position> newOrigins = new ArrayList<>();

            for (Position p : depthOrigins) {
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
    public Path getPath() {
        return path;
    }

    /**
     * To support jaxb unmarshalling.
     * @param path 
     */
    public void setPath(Path path) {
        this.path = path;
    }

    public long getTime() {
        return time;
    }
    
    /**
     * To support jaxb unmarshalling.
     * @param time 
     */
    public void setTime(long time) {
        this.time = time;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.vodb.types.I_Position#isAntecedentOrEqualTo(org.dwfa.vodb.types
     * .OldPosition)
     */
    public boolean isAntecedentOrEqualTo(Position another) {
        if (equals(another)) {
            return true;
        }

        if (path.getConceptNid() == another.getPath().getConceptNid()) {
            return time <= another.getTime();
        }

        return checkAntecedentOrEqualToOrigins(another.getPath().getOrigins());
    }

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
    public boolean isSubsequentOrEqualTo(Position another) {
        return another.isAntecedentOrEqualTo(this);
    }

    public boolean isSubsequentOrEqualTo(long time, int pathId) {
        if (equals(time, pathId)) {
            return true;
        }

        if (path.getConceptNid() == pathId) {
            return this.time >= time;
        }

        return checkSubsequentOrEqualToOrigins(path.getOrigins(), time, pathId);
    }

    public Map<Long, ? extends Position> getIntersections() {
        // TODO support intersections
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<? extends Position> getBarriers() {
        // TODO support barriers
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int compareTo(Position o) {
        if (this.time != o.time) {
            if (this.time - o.time > 0) {
                return 1;
            } else {
                return -1;
            }
        }
        return this.path.conceptNid - o.path.conceptNid;
    }

    @Override
    public Instant getInstant() {
        return Instant.ofEpochMilli(time);
    }

    @Override
    public StampPath getStampPath() {
        return path;
    }

}
