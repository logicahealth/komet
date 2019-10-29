package sh.komet.gui.util;

import sh.isaac.api.util.NaturalOrder;

import java.util.UUID;

public class UuidStringKey implements Comparable<UuidStringKey> {
    final UUID uuid;
    String string;

    public UuidStringKey(UUID uuid, String string) {
        this.uuid = uuid;
        this.string = string;
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

    @Override
    public String toString() {
        return string;
    }

    public void updateString(String string) {
        this.string = string;
    }
}
