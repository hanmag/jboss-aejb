package javax.aejb;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;

/**
 * An adaptive session bean must be annotated with the Adaptive annotation
 * 
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Adaptive {

}
