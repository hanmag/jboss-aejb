package org.nju.artemis.aejb.evolution;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public class OperationContext {

	private String operationName;
	private String targetName;
	private Map<Object, Object> contextData = new HashMap<Object, Object>();
	
	public OperationContext(String operationName, String targetName) {
		this.operationName = operationName;
		this.targetName = targetName;
	}
	
	public Map<Object, Object> getContextData() {
		return contextData;
	}

	public String getOperationName() {
		return operationName;
	}
	
	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public String getTargetName() {
		return targetName;
	}
	
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}
}
