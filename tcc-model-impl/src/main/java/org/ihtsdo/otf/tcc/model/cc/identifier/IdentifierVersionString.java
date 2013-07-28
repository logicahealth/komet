package org.ihtsdo.otf.tcc.chronicle.cc.identifier;

//~--- non-JDK imports --------------------------------------------------------
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import java.io.IOException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;


import org.ihtsdo.otf.tcc.chronicle.cc.component.ConceptComponent.IDENTIFIER_PART_TYPES;
import org.ihtsdo.otf.tcc.api.id.StringIdBI;
import org.ihtsdo.otf.tcc.dto.component.identifier.TtkIdentifierString;

public class IdentifierVersionString extends IdentifierVersion implements StringIdBI {

    private String stringDenotation;

    //~--- constructors --------------------------------------------------------
    public IdentifierVersionString() {
        super();
    }

    public IdentifierVersionString(TtkIdentifierString idv) throws IOException {
        super(idv);
        stringDenotation = idv.getDenotation();
    }

    public IdentifierVersionString(TupleInput input) {
        super(input);
        stringDenotation = input.readString();
    }

    public IdentifierVersionString(IdentifierVersionString another, Status status, long time,int authorNid,
           int moduleNid, int pathNid) {
        super(status, time, authorNid, moduleNid, pathNid, another.authorityNid);
        stringDenotation = (String) another.getDenotation();
    }

    public IdentifierVersionString(Status status, long time,int authorNid,
           int moduleNid, int pathNid, String denotation,
            int authorityNid) {
        super(status, time, authorNid, moduleNid, pathNid, authorityNid);
        stringDenotation = denotation;
    }

    //~--- methods -------------------------------------------------------------
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (IdentifierVersionString.class.isAssignableFrom(obj.getClass())) {
            IdentifierVersionString another = (IdentifierVersionString) obj;

            return this.getStamp() == another.getStamp();
        }

        return false;
    }

    @Override
    public final boolean readyToWriteIdentifier() {
        assert stringDenotation != null : toString();

        return true;
    }

    /*
     * (non-Javadoc) @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append(this.getClass().getSimpleName()).append(": ");
        buf.append("denotation:" + "'").append(this.stringDenotation).append("'");
        buf.append(" ");
        buf.append(super.toString());

        return buf.toString();
    }

    @Override
    protected void writeSourceIdToBdb(TupleOutput output) {
        output.writeString(stringDenotation);
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public String getDenotation() {
        return stringDenotation;
    }

    @Override
    public IDENTIFIER_PART_TYPES getType() {
        return IDENTIFIER_PART_TYPES.STRING;
    }
    //~--- set methods ---------------------------------------------------------
}
