package sh.isaac.api.marshal;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *  Used to indicate which instance method shall be used as the Marshaler.
 *  Annotation must be placed on the implementing class. Annotations don't inherit on
 *  methods that I know of...
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Marshaler {
}
