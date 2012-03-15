package org.nju.artemis.aejb.evolution;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public class OperationContext {

	private Map<Object, Object> contextData = new HashMap<Object, Object>();
	
	public Map<Object, Object> getContextData() {
		return contextData;
	}
}
