package org.nju.artemis.aejb.component;

import java.util.Map;

import org.nju.artemis.aejb.component.interceptors.InvocationFilterInterceptor.InvocationManager;
import org.nju.artemis.aejb.management.client.AEjbClientImpl.AEjbStatus;

/**
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public class AEjbStatusListener implements Listener<Map<String, AEjbStatus>> {

	private final InvocationManager invocationManager;
	
	public AEjbStatusListener(InvocationManager invocationManager) {
		this.invocationManager = invocationManager;
	}
	
	@Override
	public void transition(Map<String, AEjbStatus> context) {
		if(invocationManager != null && context != null && !context.isEmpty()) {
			final String targetName = invocationManager.getTargetName();
			if(targetName != null && AEjbStatus.RESUMING == context.get(targetName)) {
				invocationManager.resumeInvocation();
			}
		}
	}

}
