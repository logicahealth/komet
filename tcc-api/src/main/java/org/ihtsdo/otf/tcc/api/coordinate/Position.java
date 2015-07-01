package org.ihtsdo.otf.tcc.api.coordinate;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.IdentifierService;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.PathService;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptService;
import gov.vha.isaac.ochre.api.coordinate.StampPath;
import gov.vha.isaac.ochre.api.coordinate.StampPosition;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;

@XmlRootElement(name = "position")
@XmlAccessorType(XmlAccessType.PROPERTY)

public class Position implements StampPosition, Externalizable {


    private static final int dataVersion = 1;
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    

    private static PathService pathService;
    private static PathService getPathService() {
        if (pathService == null) {
            pathService = LookupService.getService(PathService.class);
        }
        return pathService;
    }
    

    public Position(Position another) {
        this.time = another.getTime();
        this.path = new Path(another.getPath());
    }
    
    public Position(StampPosition another) {
        this.time = another.getTime();
        this.path =  getPathService().getStampPath(another.getStampPathSequence());
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
            path = (StampPath) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
    //~--- fields --------------------------------------------------------------
    private StampPath path;
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
    public Position(long time, StampPath path) {
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
    public boolean checkAntecedentOrEqualToOrigins(Collection<? extends StampPosition> origins) {
        for (StampPosition origin : origins) {
            if (path.getPathConceptSequence() == origin.getStampPathSequence()) {
                return time <= origin.getTime();
            } else if (checkAntecedentOrEqualToOrigins(origin.getStampPath().getPathOrigins())) {
                return true;
            }
        }

        return false;
    }
    
    private boolean checkAntecedentOrEqualToOrigins(Collection<? extends StampPosition> origins, long testTime,
            int testPathId) {
        if (testPathId < 0) {
            testPathId = Get.identifierService().getConceptSequence(testPathId);
        }
        for (StampPosition origin : origins) {
            if (testPathId == origin.getStampPathSequence()) {
                return origin.getTime() <= testTime;
            } else if (checkAntecedentOrEqualToOrigins(origin.getStampPath().getPathOrigins(), testTime, testPathId)) {
                return true;
            }
        }

        return false;
    }

    private boolean checkSubsequentOrEqualToOrigins(Collection<? extends StampPosition> origins, long testTime,
            int testPathId) {
        if (testPathId < 0) {
            testPathId = Get.identifierService().getConceptSequence(testPathId);
        }
         for (StampPosition origin : origins) {
            if (testPathId == origin.getStampPathSequence()) {
                return origin.getTime() >= testTime;
            } else if (checkSubsequentOrEqualToOrigins(origin.getStampPath().getPathOrigins(), testTime, testPathId)) {
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
        if (pathId < 0) {
            pathId = Get.identifierService().getConceptSequence(pathId);
        }
         return ((this.time == time) && (path.getPathConceptSequence() == pathId));
    }

    @Override
    public int hashCode() {
        return Hashcode.computeLong(time, path.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        if (path != null) {
            ConceptChronology cb = Get.conceptService().getConcept(path.getPathConceptSequence());
            buff.append(cb.toUserString());
        } else {
            buff.append("null path");
        }
        
        buff.append(": ");

        if (time == Long.MAX_VALUE) {
            buff.append("Latest");
        } else if (time == Long.MIN_VALUE) {
            buff.append("canceled");
        } else {
            Date positionDate = new Date(time);

            buff.append(dateFormatter.format(positionDate));
        }

        return buff.toString();
    }

    //~--- get methods ---------------------------------------------------------
    public Collection<? extends StampPosition> getOrigins() {
        return path.getPathOrigins();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_Position#getDepth(int)
     */
    public int getDepth(int pathId) {
        int depth = 0;
        if (pathId < 0) {
            pathId = Get.identifierService().getConceptSequence(pathId);
        }

        if (pathId == path.getPathConceptSequence()) {
            return depth;
        }

        List<StampPosition> depthOrigins = new ArrayList<>(path.getPathOrigins());

        while (depthOrigins.size() > 0) {
            depth++;

            for (StampPosition o : depthOrigins) {
                if (o.getStampPathSequence() == pathId) {
                    return depth;
                }
            }

            List<StampPosition> newOrigins = new ArrayList<>();

            for (StampPosition p : depthOrigins) {
                newOrigins.addAll(p.getStampPath().getPathOrigins());
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
    public StampPath getPath() {
        return path;
    }

    /**
     * To support jaxb unmarshalling.
     * @param path 
     */
    public void setPath(Path path) {
        this.path = path;
    }

    @Override
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

        if (path.getPathConceptSequence() == another.getPath().getPathConceptSequence()) {
            return time <= another.getTime();
        }

        return checkAntecedentOrEqualToOrigins(another.getPath().getPathOrigins());
    }

    public boolean isAntecedentOrEqualTo(long time, int pathId) {
        if (pathId < 0) {
            pathId = Get.identifierService().getConceptSequence(pathId);
        }
        if (equals(time, pathId)) {
            return true;
        }

        if (path.getPathConceptSequence() == pathId) {
            return this.time <= time;
        }

        return checkAntecedentOrEqualToOrigins(path.getPathOrigins(), time, pathId);
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
        if (pathId < 0) {
            pathId = Get.identifierService().getConceptSequence(pathId);
        }
        if (equals(time, pathId)) {
            return true;
        }

        if (path.getPathConceptSequence() == pathId) {
            return this.time >= time;
        }

        return checkSubsequentOrEqualToOrigins(path.getPathOrigins(), time, pathId);
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
    public int compareTo(StampPosition o) {
        int comparison = Long.compare(time, o.getTime());
        if (comparison != 0) {
            return comparison;
        }
        return Integer.compare(this.getStampPathSequence(), 
                o.getStampPathSequence());
    }
   

    public Instant getInstant() {
        return Instant.ofEpochMilli(time);
    }

    @Override
    public StampPath getStampPath() {
        return path;
    }

    @Override
    public int getStampPathSequence() {
        return path.getPathConceptSequence();
    }

}
