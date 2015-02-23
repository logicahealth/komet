package org.ihtsdo.otf.tcc.api.coordinate;

//~--- JDK imports ------------------------------------------------------------

import gov.vha.isaac.ochre.api.SequenceProvider;
import gov.vha.isaac.ochre.api.coordinate.StampPath;
import gov.vha.isaac.ochre.api.coordinate.StampPosition;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;

@XmlRootElement(name = "path")
@XmlAccessorType(XmlAccessType.PROPERTY)

public class Path implements StampPath, Externalizable {

    private static final int dataVersion = 2;
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();

        if (objDataVersion == 1) {
            UUID conceptUuid = (UUID) in.readObject();
            conceptNid = Integer.MAX_VALUE;
            origins = (Set<Position>) in.readObject();
            conceptSpec = (ConceptSpec) in.readObject();
        } else if (objDataVersion == 2) {
            conceptNid = Integer.MAX_VALUE;
            origins = (Set<Position>) in.readObject();
            conceptSpec = (ConceptSpec) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(origins);
        out.writeObject(getConceptSpec());
    }
    /**
     *
     */
    int conceptNid = Integer.MAX_VALUE;
    Set<Position> origins = new HashSet<>();
    ConceptSpec conceptSpec;

    /**
     * No arg constructor for JAXB
     */
    
    public Path() {
    }
    
    public Path(ConceptSpec conceptSpec, List<? extends Position> origins) {
        super();
        this.conceptSpec = new ConceptSpec(conceptSpec);
        if (origins != null) {
            this.origins = new CopyOnWriteArraySet<>(origins);
        } else {
            this.origins = new CopyOnWriteArraySet<>();
        }
    }

    public Path(SimplePath another) {
        this.conceptSpec = new ConceptSpec(another.getPathConceptSpecification());
        for (SimplePosition origin: another.getOrigins()) {
            origins.add(new Position(origin));
        }
    }
    public Path(Path another) {
        this.conceptSpec = another.getConceptSpec();
        for (Position origin: another.getOrigins()) {
            origins.add(new Position(origin));
        }
    }

    public Path(int conceptId, List<? extends Position> origins) {
        super();
        this.conceptNid = conceptId;
        if (origins != null) {
            this.origins = new CopyOnWriteArraySet<>(origins);
        } else {
            this.origins = new CopyOnWriteArraySet<>();
        }
    }

    public boolean equals(Path another) {
        return (getConceptNid() == another.getConceptNid());
        
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (Path.class.isAssignableFrom(obj.getClass())) {
            return equals((Path) obj);
        }
        return false;
    }

    @Override
    public int hashCode() {
        try {
            if (conceptSpec != null) {
                return conceptSpec.getUuids()[0].hashCode();
            }
            return Ts.get().getUuidPrimordialForNid(conceptNid).hashCode();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.Path#getConceptNid()
     */
    public int getConceptNid() {
        if (conceptNid == Integer.MAX_VALUE) {
            try {
                if (conceptSpec != null) {
                    conceptNid = conceptSpec.getNid();
                }
            } catch (ValidationException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return conceptNid;
    }
   public ConceptSpec getConceptSpec() {
        try {
            if (this.conceptSpec != null) {
                return conceptSpec;
            }
            return new ConceptSpec(this.conceptNid);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
   }
   public void setConceptSpec(ConceptSpec conceptSpec) throws IOException {
       this.conceptSpec = conceptSpec;
       this.conceptNid = Integer.MAX_VALUE;
   }
 
   
   /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.Path#getOrigins()
     */
    public Collection<Position> getOrigins() {
        return origins;
    }
    
    /**
     * Added to support jaxb unmarshalling. 
     * @param origins 
     */
    public void setOrigins(Collection<Position> origins) {
        this.origins = new HashSet(origins);
    }

    public Set<Position> getInheritedOrigins() {
        HashSet<Position> inheritedOrigins = new HashSet<>();
        for (Position origin : this.origins) {
            inheritedOrigins.addAll(origin.getPath().getInheritedOrigins());
            inheritedOrigins.add(origin);
        }
        return inheritedOrigins;
    }

    public Set<Position> getNormalisedOrigins() {
        return getNormalisedOrigins(null);
    }

    public Set<Position> getNormalisedOrigins(Collection<Path> paths) {
        final Set<Position> inheritedOrigins = getInheritedOrigins();
        if (paths != null) {
            for (Path path : paths) {
                if (path != this) {
                    inheritedOrigins.addAll(path.getInheritedOrigins());
                }
            }
        }
        Set<Position> normalisedOrigins = new HashSet<>(inheritedOrigins);
        for (Position a : inheritedOrigins) {
            for (Position b : inheritedOrigins) {
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
     * @see org.dwfa.vodb.types.Path#getMatchingPath(int)
     */
    public Path getMatchingPath(int pathId) {
        if (conceptNid == pathId) {
            return this;
        }
        for (Position origin : origins) {
            if (origin.getPath().getMatchingPath(pathId) != null) {
                return origin.getPath();
            }
        }
        return null;
    }

    public static String toHtmlString(Path path) throws IOException {
        StringBuilder buff = new StringBuilder();
        buff.append("<html><font color='blue' size='+1'><u>");
        ConceptChronicleBI cb = Ts.get().getConcept(path.getConceptNid());
        buff.append(cb.toUserString());
        buff.append("</u></font>");
        for (Position origin : path.getOrigins()) {
            buff.append("<br>&nbsp;&nbsp;&nbsp;Origin: ");
            buff.append(origin);
        }
        return buff.toString();
    }

    public static void writePath(ObjectOutputStream out, Path p) throws IOException {
        List<UUID> uuids = Ts.get().getUuidsForNid(p.getConceptNid());
        if (uuids.size() > 0) {
            out.writeObject(Ts.get().getUuidsForNid(p.getConceptNid()));
        } else {
            throw new IOException("no uuids for component: " + p);
        }
        out.writeInt(p.getOrigins().size());
        for (Position origin : p.getOrigins()) {
            Position.writePosition(out, origin);
        }
    }

    @SuppressWarnings("unchecked")
    public static Path readPath(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int pathId;
        List<UUID> pathIdList = (List<UUID>) in.readObject();
        if (Ts.get().hasUuid(pathIdList)) {
            pathId = Ts.get().getNidForUuids(pathIdList);
        } else {
            pathId = TermAux.WB_AUX_PATH.getLenient().getNid();
            Logger.getLogger(Path.class.getName()).log(Level.SEVERE, "ReadPath error. {0} missing. Substuting WB Aux ", pathIdList);
        }
        int size = in.readInt();
        List<Position> origins = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            origins.add(Position.readPosition(in));
        }
        return new Path(pathId, origins);
    }

    public static Set<Path> readPathSet(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int size = in.readInt();
        Set<Path> positions = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            positions.add(readPath(in));
        }
        return positions;
    }

    public static void writePathSet(ObjectOutputStream out, Set<Path> viewPositions) throws IOException {
        out.writeInt(viewPositions.size());
        for (Path p : viewPositions) {
            writePath(out, p);
        }
    }

    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        try {
            int cNid = getConceptNid();
            if (cNid != Integer.MAX_VALUE) {
                ConceptChronicleBI cb = Ts.get().getConcept(cNid);
                buff.append(cb.toUserString());
            } else {
                buff.append("not yet defined concept");
            }
            
        } catch (IOException e) {
            buff.append(e.getMessage());
            Logger.getLogger(Path.class.getName()).log(Level.SEVERE, null, e);
        }
        return buff.toString();
    }

    public String toHtmlString() throws IOException {
        return Path.toHtmlString(this);
    }

    public List<UUID> getUUIDs() {
        try {
            return new ArrayList<>(Ts.get().getUuidsForNid(conceptNid));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static SequenceProvider sequenceProvider;
    
    private static SequenceProvider getSequenceProvider() {
        if (sequenceProvider == null) {
            sequenceProvider = Hk2Looker.getService(SequenceProvider.class);
        }
        return sequenceProvider;
    }

    @Override
    public int getPathConceptSequence() {
        return getSequenceProvider().getConceptSequence(getConceptNid());
    }

    @Override
    public Collection<? extends StampPosition> getPathOrigins() {
        return this.origins;
    }
}
