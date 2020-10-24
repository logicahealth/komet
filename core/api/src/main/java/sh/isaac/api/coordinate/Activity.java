package sh.isaac.api.coordinate;

import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.Marshalable;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;

public enum Activity implements Marshalable {


       VIEWING("Viewing"),
       DEVELOPING("Developing"),
       PROMOTING("Promoting"),
       MODULARIZING("Modularizing");

       private static final int marshalVersion = 1;

       private String userString;

       Activity(String userString) {
              this.userString = userString;
       }

       public String toUserString() {
          return this.userString;
       }


       // Using a static method rather than a constructor eliminates the need for
       // a readResolve method, but allows the implementation to decide how
       // to handle special cases.

       @Unmarshaler
       public static Object make(ByteArrayDataBuffer in) {
              int objectMarshalVersion = in.getInt();
              switch (objectMarshalVersion) {
                     case marshalVersion:
                            return Activity.valueOf(in.getUTF());
                     default:
                            throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);
              }
       }

       @Override
       @Marshaler
       public void marshal(ByteArrayDataBuffer out) {
              out.putInt(marshalVersion);
              out.putUTF(name());
       }

}
