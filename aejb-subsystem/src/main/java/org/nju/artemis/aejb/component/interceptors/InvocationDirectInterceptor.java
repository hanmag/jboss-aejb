package org.nju.artemis.aejb.component.interceptors;

import java.util.Map;

import org.jboss.as.ejb3.component.session.SessionBeanComponentDescription.SessionBeanType;
import org.jboss.invocation.Interceptor;
import org.jboss.invocation.InterceptorContext;
import org.jboss.logging.Logger;
import org.nju.artemis.aejb.component.AcContainer;

/**
 * This interceptor decides which AEjb will get this invocation.
 * 
 * @author <a href="mailto:wangjue1199@gmail.com">Jason</a>
 */
public class InvocationDirectInterceptor implements Interceptor {
	Logger log = Logger.getLogger(InvocationDirectInterceptor.class);
	private final AcContainer container;
	private InvocationContext lastInvocation;
	
	public InvocationDirectInterceptor(AcContainer container) {
		this.container = container;
	}

	@Override
	public Object processInvocation(InterceptorContext context) throws Exception {
		log.info("InvocationDirectInterceptor: start processing invocation");
		Map<String,Object> contextData = context.getContextData();
		final String aejbName = (String) contextData.get("aejbName");
		AcContainer con = container.getEvolutionStatistics().getDirectionalMap().get(aejbName);
		if(lastInvocation != null) {
			contextData.put("appName", lastInvocation.appName());
			contextData.put("moduleName", lastInvocation.moduleName());
			contextData.put("beanName", lastInvocation.beanName());
			contextData.put("distinctName", lastInvocation.distinctName());
			contextData.put("viewClass", lastInvocation.viewClass());
			contextData.put("stateful",	lastInvocation.stateful());
		}
		if(con != null) {
			// whether it is safe
			if(context.getPrivateData(Boolean.class) == null || context.getPrivateData(Boolean.class) == false)
				return context.proceed();
			contextData.put("appName", con.getApplicationName());
			contextData.put("moduleName", con.getModuleName());
			contextData.put("beanName", con.getBeanName());
			contextData.put("distinctName", con.getDistinctName());
//			contextData.put("viewClass", con.getRemoteView() != null ? con.getRemoteView().iterator().next() : con.getLocalView().iterator().next());
			contextData.put("viewClass", con.getViewByClass((Class<?>) contextData.get("viewClass")));
			contextData.put("stateful",	con.getBeanType() == SessionBeanType.STATEFUL);
			lastInvocation = new InvocationContext(con.getApplicationName(), con.getModuleName(), con.getBeanName(), con.getDistinctName(), con.getViewByClass((Class<?>) contextData.get("viewClass")), con.getBeanType() == SessionBeanType.STATEFUL);
		}
		log.info("InvocationDirectInterceptor: stop processing invocation");
		return context.proceed();
	}
	
	private class InvocationContext {
		private String $appName;
		private String $moduleName;
		private String $beanName;
		private String $distinctName;
		private Class<?> $viewClass;
		private boolean $stateful;
		
		public InvocationContext(String $appName, String $moduleName, String $beanName, String $distinctName, Class<?> $viewClass, boolean $stateful) {
			this.$appName = $appName;
			this.$moduleName = $moduleName;
			this.$beanName = $beanName;
			this.$distinctName = $distinctName;
			this.$viewClass = $viewClass;
			this.$stateful = $stateful;
		}
		String appName() {
			return $appName;
		}
		String moduleName() {
			return $moduleName;
		}
		String beanName() {
			return $beanName;
		}
		String distinctName() {
			return $distinctName;
		}
		Class<?> viewClass() {
			return $viewClass;
		}
		boolean stateful() {
			return $stateful;
		}
	}
}
