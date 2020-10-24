package sh.isaac.api.marshal;
/**
 * Marshaling: arrange or assemble (a group of people, especially soldiers) in order: the general marshaled his troops.
 * </br>
 * To "marshal" an object means to record its state and codebase(s)
 * in such a way that when the marshalled object is "unmarshalled," a
 * copy of the original object is obtained, possibly by automatically
 * loading the class definitions of the object.
 * </br>
 * In the future, we may consider replacing the sh.isaac.api.externalizable
 * package with concepts from this package. The approach and annotations here
 * are loosely based on solutions described in: https://cr.openjdk.java.net/~briangoetz/amber/serialization.html
 * although we don't have implementations described there to use at this time,
 * so we approximate them here.
 *
 * </br>
 *  Annotations in this package indicate which static method on a class shall be used as the
 *  Unmarshaler and which instance method shall be used as the Marshaler.
 *
 *  Both the Marshaler and Unmarshaler methods shall take a single paramater of type: ByteArrayDataBuffer, and read or write
 *  version information and state information from that buffer.
 * </br>
 *
 */