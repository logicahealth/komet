package sh.isaac.api.marshal;

import sh.isaac.api.externalizable.ByteArrayDataBuffer;

/**
 *
 * Template for marshalable class implementations classes
 *

 private static final int marshalVersion = 1;

 // Using a static method rather than a constructor eliminates the need for
 // a readResolve method, but allows the implementation to decide how
 // to handle special cases.

 @Unmarshaler
 public static Object make(ByteArrayDataBuffer in) {
    int objectMarshalVersion = in.getInt();
    switch (objectMarshalVersion) {
        case marshalVersion:
            throw new UnsupportedOperationException();
            break;
        default:
            throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);
    }
 }

 @Override
 @Marshaler
 public void marshal(ByteArrayDataBuffer out) {
    out.putInt(marshalVersion);
    throw new UnsupportedOperationException();
 }



 *
 *
 */
public interface Marshalable {
    @Marshaler
    void marshal(ByteArrayDataBuffer out);

}
