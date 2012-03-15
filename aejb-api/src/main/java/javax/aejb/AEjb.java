package javax.aejb;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
@Retention(RUNTIME)
@Target(FIELD) 
public @interface AEjb {
	String name() default "";

	Class beanInterface() default java.lang.Object.class;

	String beanName() default "";

	/**
	 * A portable lookup string containing the JNDI name for the target EJB
	 * component.
	 */
	String lookup() default "";

	String mappedName() default "";

	String description() default "";
}
