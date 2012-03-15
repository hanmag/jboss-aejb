package org.nju.artemis.aejb.component;
/**
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public interface Listener<T> {

	void transition(T transitionContext);
}
