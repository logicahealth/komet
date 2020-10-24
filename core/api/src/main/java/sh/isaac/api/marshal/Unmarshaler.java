package sh.isaac.api.marshal;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *  Used to indicate which static method on a class shall be used as the
 *  Unmarshaler.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Unmarshaler {
}
