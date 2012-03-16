package javax.aejb;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
@Retention(RUNTIME)
@Target(ElementType.METHOD)
public @interface Transaction {
	String name() default "";

	String[] states() default { "" };

	String[] next() default { "" };
}
