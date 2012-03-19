package org.nju.artemis.aejb.component.interceptors;

import java.util.Map;

import org.jboss.invocation.Interceptor;
import org.jboss.invocation.InterceptorContext;
import org.jboss.logging.Logger;
import org.nju.artemis.aejb.component.AEjbStatusListener;
import org.nju.artemis.aejb.component.AcContainer;
import org.nju.artemis.aejb.management.client.AEjbClientImpl.AEjbStatus;

/**
 * This interceptor can cache and filter the invocation.
 * 
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public class InvocationFilterInterceptor implements Interceptor {
	Logger log = Logger.getLogger(InvocationFilterInterceptor.class);
	private final AcContainer container;
	private final InvocationManager manager;
	
	public InvocationFilterInterceptor(AcContainer container) {
		this.container = container;
		manager = new InvocationManager();
		this.container.getEvolutionStatistics().addStatusListener(new AEjbStatusListener(manager));
	}
	
	@Override
	public Object processInvocation(InterceptorContext context)	throws Exception {
		log.info("InvocationFilterInterceptor: start processing invocation");
		final String targetAEjbName = (String) context.getContextData().get("aejbName");
		final Map<String, AEjbStatus> status = container.getEvolutionStatistics().getAEjbStatus();
		if(status != null && AEjbStatus.BLOCKING == status.get(targetAEjbName)) {
			manager.blockInvocation(context, targetAEjbName);
		}
		log.info("InvocationFilterInterceptor: stop processing invocation");
		return context.proceed();
	}
	
	public class InvocationManager {
	    private InterceptorContext context;
	    private String targetName;
	    
	    public void resumeInvocation() {
	    	if(context != null) {
	    		synchronized (context) {
					context.notify();
				}
				this.context = null;
	    	}
	    }
	    
	    public void blockInvocation(InterceptorContext context, String targetName) {
			this.context = context;
			this.targetName = targetName;
			synchronized (this.context) {
				try {
					this.context.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	    
	    public String getTargetName() {
	    	return targetName;
	    }
	}
}
