package sh.isaac.api.marshal;

import sh.isaac.api.externalizable.ByteArrayDataBuffer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MarshalUtil {
    /*
    @TODO potential optimizations include caching classes and methods that implement Marshaler & Unmarshaler
     */

    private enum MarshalToken {
        UNSUPPORTED(0),
        MARSHALABLE(1),
        LIST(2),
        NULL(3);

        final byte token;

        MarshalToken(int token) {
            this.token = (byte) token;
        }

        static MarshalToken fromClass(Object obj) {
            if (obj == null) {
                return NULL;
            }
            if (obj instanceof List) {
                return LIST;
            }
            for (Method method: obj.getClass().getMethods()) {
                if (method.isAnnotationPresent(Marshaler.class)) {
                    return MARSHALABLE;
                }
            }
            return UNSUPPORTED;
        }

        static MarshalToken fromByte(byte token) {
            for (MarshalToken marshalToken: MarshalToken.values()) {
                if (marshalToken.token == token) {
                    return marshalToken;
                }
            }
            return UNSUPPORTED;
        }
        static MarshalToken fromBuffer(ByteArrayDataBuffer buffer) {
             return fromByte(buffer.getByte());
        }
    }

    public static void toFile(Object object, File file) throws IOException, ReflectiveOperationException {
        toFile(object, file.toPath());
    }

    public static void toFile(Object object, Path filePath) throws ReflectiveOperationException, IOException {
        try (OutputStream outputStream = Files.newOutputStream(filePath)) {
            outputStream.write(toBytes(object));
        }
    }
    public static <T extends Object> T fromFile(File file) throws ReflectiveOperationException, IOException {
        return fromFile(file.toPath());
    }

    public static <T extends Object> T fromFile(Path filePath) throws ReflectiveOperationException, IOException {
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            return fromBytes(inputStream.readAllBytes());
        }
    }

    public static byte[] toBytes(Object object) throws ReflectiveOperationException {
        ByteArrayDataBuffer buffer = new ByteArrayDataBuffer();
        buffer.setExternalData(true);
        marshal(object, buffer);
        buffer.trimToSize();
        return buffer.getData();
    }

    public static <T extends Object> T fromBytes(byte[] bytes) throws ReflectiveOperationException {
        ByteArrayDataBuffer buff = new ByteArrayDataBuffer(bytes);
        buff.setExternalData(true);
        return unmarshal(buff);
    }

    public static void marshal(Object object, ByteArrayDataBuffer out) throws ReflectiveOperationException {
        switch (MarshalToken.fromClass(object)) {
            case LIST:
                marshalList((List) object, out);
                break;
            case MARSHALABLE:
                marshalDefault(object, out);
                break;
            case NULL:
                out.putByte(MarshalToken.NULL.token);
                break;
            default:
                throw new ReflectiveOperationException("Can't marshal " + object.getClass().getName());
        }

    }

    private static void marshalList(List list, ByteArrayDataBuffer out) throws ReflectiveOperationException {
        out.putByte(MarshalToken.LIST.token);
        out.putInt(list.size());
        for (Object listElement: list) {
            marshal(listElement, out);
        }
    }

    private static <T extends Object> T listUnmarshal(ByteArrayDataBuffer in) throws ReflectiveOperationException {
        int listSize = in.getInt();
        ArrayList list = new ArrayList(listSize);
        for (int i = 0; i < listSize; i++) {
            list.add(unmarshal(in));
        }
        return (T) list;
    }


    private static void marshalDefault(Object object, ByteArrayDataBuffer out) throws ReflectiveOperationException {
        out.putByte(MarshalToken.MARSHALABLE.token);
        Class objectClass = object.getClass();
        String objectClassName = objectClass.getName();
        ArrayList<Method> marshalMethodList = new ArrayList<>();
        for (Method method: objectClass.getDeclaredMethods()) {
            for (Annotation annotation: method.getAnnotations()) {
                if (annotation instanceof Marshaler) {
                    if (Modifier.isStatic(method.getModifiers())) {
                        throw new ReflectiveOperationException("Marshaler method for class: " + objectClassName
                                + " is static: " + method);
                    } else {
                        marshalMethodList.add(method);
                    }
                }
            }
        }
        if (marshalMethodList.isEmpty()) {
            throw new IllegalStateException("No marshal method for class: " + objectClassName);
        } else if (marshalMethodList.size() == 1) {
            out.putUTF(objectClassName);
            Method unmarshalMethod = marshalMethodList.get(0);
            unmarshalMethod.invoke(object, out);
        } else {
            throw new ReflectiveOperationException("More than one unmarshal method for class: " + objectClassName
                    + " methods: " + marshalMethodList);
        }
    }

    public static <T extends Object> T unmarshal(ByteArrayDataBuffer in) throws ReflectiveOperationException {
        MarshalToken marshalToken = MarshalToken.fromBuffer(in);
        switch (marshalToken) {
            case LIST:
                return listUnmarshal(in);
            case MARSHALABLE:
                return defaultUnmarshal(in);
            case NULL:
                return null;
            default:
                throw new ReflectiveOperationException("Can't unmarshal: " + marshalToken);
        }


    }
    private static <T extends Object> T defaultUnmarshal(ByteArrayDataBuffer in) throws ReflectiveOperationException {
        String className = in.getUTF();
        Class objectClass = Class.forName(className);
        ArrayList<Method> unmarshalMethodList = new ArrayList<>();
        for (Method method: objectClass.getDeclaredMethods()) {
            for (Annotation annotation: method.getAnnotations()) {
                if (annotation instanceof Unmarshaler) {
                    if (Modifier.isStatic(method.getModifiers())) {
                        unmarshalMethodList.add(method);
                    } else {
                        throw new ReflectiveOperationException("Marshaler method for class: " + className
                                + " is not static: " + method);
                    }
                }
            }
        }
        if (unmarshalMethodList.isEmpty()) {
            throw new IllegalStateException("No unmarshal method for class: " + className);
        } else if (unmarshalMethodList.size() == 1) {
            Method unmarshalMethod = unmarshalMethodList.get(0);
                 return (T) unmarshalMethod.invoke(null, in);
         }
        throw new ReflectiveOperationException("More than one unmarshal method for class: " + className
                + " methods: " + unmarshalMethodList);
    }
}
