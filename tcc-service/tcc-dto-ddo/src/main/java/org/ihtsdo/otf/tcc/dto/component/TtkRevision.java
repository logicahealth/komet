package org.ihtsdo.otf.tcc.dto.component;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.coordinate.ExternalStampBI;
import org.ihtsdo.otf.tcc.api.id.IdBI;
import org.ihtsdo.otf.tcc.dto.component.transformer.ComponentFields;
import org.ihtsdo.otf.tcc.dto.component.transformer.ComponentTransformerBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import org.ihtsdo.otf.tcc.api.coordinate.Status;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class TtkRevision implements ExternalStampBI {
    
    private static final Set<UUID> activeSet = new HashSet<>();
    static {
        activeSet.add(UUID.fromString("32dc7b19-95cc-365e-99c9-5095124ebe72"));
        activeSet.add(UUID.fromString("3f3c499c-a9e3-3f93-95c4-1c7bf4cf2b83"));
        activeSet.add(UUID.fromString("573a2008-9893-3989-b635-0c1800129e94"));
        activeSet.add(UUID.fromString("181e45e8-b05a-33da-8b52-7027cbee6856"));
        activeSet.add(UUID.fromString("2faa9261-8fb2-11db-b606-0800200c9a66"));
        activeSet.add(UUID.fromString("ec2bc932-7676-3b26-9d3a-1a8151e116a1"));
        activeSet.add(UUID.fromString("61a93c05-b81d-3732-adad-84dcdc36bed7"));
        activeSet.add(UUID.fromString("01da0c12-4175-33c0-aea1-7639abc4ff63"));
        activeSet.add(UUID.fromString("5ca212f4-d460-3e0c-b923-d13bc6201c9a"));
        activeSet.add(UUID.fromString("7b941165-37a6-3ad2-b416-051b95e9b7d7"));
        activeSet.add(UUID.fromString("948c05d0-fc5a-3ba6-92fe-8b748d6ec05e"));
        activeSet.add(UUID.fromString("609871ea-d80a-3296-8cbd-953a550688de"));
        activeSet.add(UUID.fromString("94c83a47-e9a5-31ce-a12b-2acdb8d53fd6"));
        activeSet.add(UUID.fromString("b104ccaa-7967-3a23-b62a-62111338b23f"));
        activeSet.add(UUID.fromString("9644e663-3087-3a45-8c18-27b9d6dda543"));
        activeSet.add(UUID.fromString("bc85c0c5-8161-3103-b1c6-3709f1cc267e"));
        activeSet.add(UUID.fromString("06f90e15-ad80-339b-a526-d279b9b5ca6c"));
        activeSet.add(UUID.fromString("b7cfafe5-7d80-37bb-af05-399b723ed98a"));
        activeSet.add(UUID.fromString("64028987-2b04-3a7c-8785-e6998be140f5"));
        activeSet.add(UUID.fromString("40abbf43-8ef2-39dc-9a3d-c4d4b7c5e186"));
        activeSet.add(UUID.fromString("c9c4ca80-879e-3e8b-a8be-14aef64ff4e5"));
        activeSet.add(UUID.fromString("854552b2-74b7-3f68-81fc-3211950d2ba9"));
        activeSet.add(UUID.fromString("c026841e-d942-302d-83aa-f0e862c3a3e1"));
        activeSet.add(UUID.fromString("476b117b-6d1a-34ac-b3d7-2792d0999838"));
        activeSet.add(UUID.fromString("21616248-170b-3fc1-8d2a-b70a054854dd"));
        activeSet.add(UUID.fromString("a1811d12-bbd9-3ae8-bc87-1ce76e7c26c7"));
        activeSet.add(UUID.fromString("7b8fb2a3-7ccb-38f7-b13e-e56fbbac93f1"));
        activeSet.add(UUID.fromString("0fdca2a1-3c80-366a-acb0-f05266c56fa7"));
        activeSet.add(UUID.fromString("d0f2ad7c-b7a6-3f6f-89e1-f0407f365a26"));
        activeSet.add(UUID.fromString("4bc081d8-9f64-3a89-a668-d11ca031979b"));
        activeSet.add(UUID.fromString("6480f3d5-01a6-3b23-9295-d9fe85a14b9b"));
        activeSet.add(UUID.fromString("7c6cc951-eb97-3db0-b701-8e1550c6672c"));
        activeSet.add(UUID.fromString("aa581d75-8d6f-3e35-99d9-72d0f5293ee6"));
        activeSet.add(UUID.fromString("11c24184-4d8a-3cd3-bc30-bb0aa4c76e93"));
        activeSet.add(UUID.fromString("ce4f67e2-dc4c-3f1d-ae1a-c5596fb21d7d"));
        activeSet.add(UUID.fromString("b4135a8d-1cee-315c-b170-2ee68e94a83d"));
        activeSet.add(UUID.fromString("9382a905-7309-3a4b-9168-b341469f30f7"));
        activeSet.add(UUID.fromString("b7999177-019f-3605-a411-44cb160f2aca"));
        activeSet.add(UUID.fromString("96ce7a06-61ae-3c83-aaab-57bef0f56333"));
        activeSet.add(UUID.fromString("9801e17e-480b-3794-b002-e7de1d2cbb68"));
        activeSet.add(UUID.fromString("fad5d8eb-97c3-3b65-95b6-3eec5b4bdd89"));
        activeSet.add(UUID.fromString("cde62339-a53f-3044-b4b6-60a2b44473cb"));
        activeSet.add(UUID.fromString("8ebf8ab5-b93d-309c-83bd-742922358ebd"));
        activeSet.add(UUID.fromString("05ecc914-73d0-3a05-9f47-21bfbb03561a"));
        activeSet.add(UUID.fromString("d2c480c7-f79b-3663-aa8e-0f1f9d2c285c"));
        activeSet.add(UUID.fromString("d1f8f7cb-ce6c-33cc-ae84-d4547fe97238"));
        activeSet.add(UUID.fromString("d12702ee-c37f-385f-a070-61d56d4d0f1f"));
        activeSet.add(UUID.fromString("6cc3df26-661e-33cd-a93d-1c9e797c90e3"));
        activeSet.add(UUID.fromString("9906317a-f50f-30f6-8b59-a751ae1cdeb9"));
    }

    @SuppressWarnings("unused")
	private static final long serialVersionUID      = 1;
    public static UUID        unspecifiedUserUuid   = UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c");
    public static UUID        unspecifiedModuleUuid = UUID.fromString("40d1c869-b509-32f8-b735-836eac577a67");
    @XmlAttribute
    public long               time                  = Long.MIN_VALUE;
    @XmlAttribute
    public UUID               authorUuid;
    @XmlAttribute
    public UUID               pathUuid;
    @XmlAttribute
    public Status               status;
    @XmlAttribute
    public UUID               moduleUuid;

    public TtkRevision() {
        super();
    }

    public TtkRevision(ComponentVersionBI another) throws IOException {
        super();
        this.status     = another.getStatus();
        this.authorUuid = Ts.get().getComponent(another.getAuthorNid()).getPrimordialUuid();
        this.pathUuid   = Ts.get().getComponent(another.getPathNid()).getPrimordialUuid();
        this.moduleUuid = Ts.get().getComponent(another.getModuleNid()).getPrimordialUuid();
        assert pathUuid != null : another;
        assert authorUuid != null : another;
        assert status != null : another;
        assert moduleUuid != null : another;
        this.time = another.getTime();
    }

    public TtkRevision(IdBI id) throws IOException {
        super();
        this.authorUuid = Ts.get().getComponent(id.getAuthorNid()).getPrimordialUuid();
        this.pathUuid   = Ts.get().getComponent(id.getPathNid()).getPrimordialUuid();
        this.status = id.getStatus();
        this.moduleUuid = Ts.get().getComponent(id.getModuleNid()).getPrimordialUuid();
        this.time       = id.getTime();
        assert pathUuid != null : id;
        assert authorUuid != null : id;
        assert status != null : id;
        assert moduleUuid != null : id;
    }

    public TtkRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
        assert pathUuid != null : this;
        assert authorUuid != null : this;
        assert status != null : this;
        assert moduleUuid != null : this;
    }

    public TtkRevision(TtkRevision another, ComponentTransformerBI transformer) {
        super();
        this.status = another.status;
        this.authorUuid = transformer.transform(another.authorUuid, another, ComponentFields.AUTHOR_UUID);
        this.pathUuid   = transformer.transform(another.pathUuid, another, ComponentFields.PATH_UUID);
        this.moduleUuid = transformer.transform(another.moduleUuid, another, ComponentFields.MODULE_UUID);
        assert pathUuid != null : another;
        assert authorUuid != null : another;
        assert status != null : another;
        assert moduleUuid != null : another;
        this.time = transformer.transform(another.time, another, ComponentFields.TIME);
    }

    /**
     * Compares this object to the specified object. The result is <tt>true</tt> if and only if the argument
     * is not <tt>null</tt>, is a <tt>EVersion</tt> object, and contains the same values, field by field, as
     * this <tt>EVersion</tt>.
     *
     * @param obj the object to compare with.
     * @return
     * <code>true</code> if the objects are the same;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (TtkRevision.class.isAssignableFrom(obj.getClass())) {
            TtkRevision another = (TtkRevision) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            if (this.status != another.status) {
                return false;
            }

            if ((this.authorUuid != null) && (another.authorUuid != null)) {
                if (!this.authorUuid.equals(another.authorUuid)) {
                    return false;
                }
            } else if (!((this.authorUuid == null) && (another.authorUuid == null))) {
                return false;
            }

            if (!this.pathUuid.equals(another.pathUuid)) {
                return false;
            }

            if ((this.moduleUuid != null) && (another.moduleUuid != null)) {
                if (!this.moduleUuid.equals(another.moduleUuid)) {
                    return false;
                }
            } else if (!((this.moduleUuid == null) && (another.moduleUuid == null))) {
                return false;
            }

            if (this.time != another.time) {
                return false;
            }

            // Objects are equal! (Don't climb any higher in the hierarchy)
            return true;
        }

        return false;
    }

    /**
     * Returns a hash code for this
     * <code>EVersion</code>.
     *
     * @return a hash code value for this <tt>EVersion</tt>.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(new int[] { status.hashCode(), pathUuid.hashCode(), (int) time,
                                           (int) (time >>> 32) });
    }

    public static CharSequence informAboutUuid(UUID uuid) {
        if (Ts.get() == null) {
            return uuid.toString();
        }

        StringBuilder sb = new StringBuilder();

        if (Ts.get().hasUuid(uuid)) {
            try {
                int nid  = Ts.get().getNidForUuids(uuid);
                int cNid = Ts.get().getConceptNidForNid(nid);

                if (cNid == nid) {
                    ConceptChronicleBI cc = Ts.get().getConcept(cNid);

                    sb.append("'");
                    sb.append(cc.toUserString());
                    sb.append("' ");
                    sb.append(cNid);
                    sb.append(" ");
                } else {
                    ComponentBI component = Ts.get().getComponent(nid);

                    

                    if (component != null) {
                        sb.append("' ");
                        sb.append(component.toUserString());
                    } else {
                        sb.append("'null");
                    }

                    sb.append("' ");
                    sb.append(nid);
                    sb.append(" ");
                }
            } catch (IOException ex) {
                Logger.getLogger(TtkRevision.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        sb.append(uuid.toString());

        return sb;
    }

    public abstract TtkRevision makeTransform(ComponentTransformerBI transformer);
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        pathUuid   = new UUID(in.readLong(), in.readLong());
        if (dataVersion > 9) {
            boolean active = in.readBoolean();
            if (active) {
                status = Status.ACTIVE;
            } else {
                status = Status.INACTIVE;
            }
            
        } else {
            UUID statusUuid = new UUID(in.readLong(), in.readLong());
            if (activeSet.contains(statusUuid)) {
                status = Status.ACTIVE;
            } else {
                status = Status.INACTIVE;
            }            
            
        }

        if (dataVersion >= 3) {
            authorUuid = new UUID(in.readLong(), in.readLong());
        } else {
            authorUuid = unspecifiedUserUuid;
        }

        if (dataVersion >= 8) {
            moduleUuid = new UUID(in.readLong(), in.readLong());
        } else {
            moduleUuid = unspecifiedModuleUuid;
        }

        time = in.readLong();

        if (time == Long.MAX_VALUE) {
            time = Long.MIN_VALUE;
        }
    }

    /**
     * Returns a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(" s:");
        buff.append(this.status);
        buff.append(" t: ");
        buff.append(new Date(this.time)).append(" ").append(this.time);
        buff.append(" a:");
        buff.append(informAboutUuid(this.authorUuid));
        buff.append(" m:");
        buff.append(informAboutUuid(this.moduleUuid));
        buff.append(" p:");
        buff.append(informAboutUuid(this.pathUuid));

        return buff.toString();
    }

    public void writeExternal(DataOutput out) throws IOException {
        if (time == Long.MAX_VALUE) {
            time = Long.MIN_VALUE;
        }

        assert pathUuid != null : this;
        assert authorUuid != null : this;
        assert status != null : this;
        assert moduleUuid != null : this;
        out.writeLong(pathUuid.getMostSignificantBits());
        out.writeLong(pathUuid.getLeastSignificantBits());
        out.writeBoolean(status == Status.ACTIVE);

        if (authorUuid == null) {
            authorUuid = unspecifiedUserUuid;
        }

        out.writeLong(authorUuid.getMostSignificantBits());
        out.writeLong(authorUuid.getLeastSignificantBits());

        if (moduleUuid == null) {
            moduleUuid = unspecifiedModuleUuid;
        }

        out.writeLong(moduleUuid.getMostSignificantBits());
        out.writeLong(moduleUuid.getLeastSignificantBits());
        out.writeLong(time);
    }

    @Override
    public UUID getAuthorUuid() {
        return authorUuid;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ihtsdo.etypes.I_VersionExternal#getPathUuid()
     */
    @Override
    public UUID getPathUuid() {
        return pathUuid;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ihtsdo.etypes.I_VersionExternal#getStatusUuid()
     */
    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public UUID getModuleUuid() {
        return moduleUuid;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ihtsdo.etypes.I_VersionExternal#getTime()
     */
    @Override
    public long getTime() {
        return time;
    }

    public void setAuthorUuid(UUID authorUuid) {
        this.authorUuid = authorUuid;
    }

    public void setPathUuid(UUID pathUuid) {
        this.pathUuid = pathUuid;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setModuleUuid(UUID moduleUuid) {
        this.moduleUuid = moduleUuid;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
