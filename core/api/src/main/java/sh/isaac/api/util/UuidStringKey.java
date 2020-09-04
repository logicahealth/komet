package sh.isaac.api.util;

import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.Marshalable;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;

import java.util.UUID;

public class UuidStringKey implements Comparable<UuidStringKey>, Marshalable {
    public static final int marshalVersion = 1;

    final UUID uuid;
    String string;

     public UuidStringKey(UUID uuid, String string) {
        this.uuid = uuid;
        this.string = string;
    }
    public UuidStringKey(String[] data) {
        this.uuid = UUID.fromString(data[0]);
        this.string = data[1];
    }

    public UuidStringKey(String externalString) {
        this(externalString.split("@", 2));
    }

    @Override
    @Marshaler
    public void marshal(ByteArrayDataBuffer out) {
         out.putInt(marshalVersion);
         out.putUTF(uuid.toString());
         out.putUTF(string);
    }

    @Unmarshaler
    public static UuidStringKey make(ByteArrayDataBuffer in) {
        int objectMarshalVersion = in.getInt();
        switch (objectMarshalVersion) {
            case marshalVersion:
                return new UuidStringKey(UUID.fromString(in.getUTF()), in.getUTF());
            default:
                throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);

        }
    }

    public String[] toStringArray() {
        return new String[] {uuid.toString(), string};
    }
    @Override
    public int compareTo(UuidStringKey o) {
        int comparison = NaturalOrder.compareStrings(this.string, o.string);
        if (comparison != 0) {
            return comparison;
        }
        return uuid.compareTo(o.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UuidStringKey that = (UuidStringKey) o;
        return uuid.equals(that.uuid);
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getString() {
        return string;
    }

    @Override
    public String toString() {
        return string;
    }

    public String toExternalString() {
         return this.uuid.toString() + "@" + this.string;
    }

    public void updateString(String string) {
        this.string = string;
    }
}
