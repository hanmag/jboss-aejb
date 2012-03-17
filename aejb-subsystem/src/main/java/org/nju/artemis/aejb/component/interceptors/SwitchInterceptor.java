package org.nju.artemis.aejb.component.interceptors;

import java.util.Map;

import org.jboss.as.ejb3.component.session.SessionBeanComponentDescription.SessionBeanType;
import org.jboss.invocation.Interceptor;
import org.jboss.invocation.InterceptorContext;
import org.jboss.logging.Logger;
import org.nju.artemis.aejb.component.AcContainer;

/**
 * @author <a href="mailto:wangjue1199@gmail.com">Jason</a>
 */
public class SwitchInterceptor implements Interceptor {
	Logger log = Logger.getLogger(SwitchInterceptor.class);
	private final AcContainer container;
	
	public SwitchInterceptor(AcContainer container) {
		this.container = container;
	}

	@Override
	public Object processInvocation(InterceptorContext context) throws Exception {
		log.info("SwitchInterceptor: processInvocation");
		Map<String,Object> contextData = context.getContextData();
		String aejbName = (String) contextData.get("aejbName");
		AcContainer con = container.getSwitchMap().get(aejbName);
		if(con != null && !con.getAEJBName().equals(aejbName)) {
			String protocol = container.getProtocols().get(aejbName);
			if(protocol != null && isValidProtocolName(protocol)) {
				String objectId = context.getPrivateData(Object.class).toString();
				log.info("objectId = " + objectId);
				
				contextData.put("appName", con.getApplicationName());
				contextData.put("moduleName", con.getModuleName());
				contextData.put("beanName", con.getBeanName());
				contextData.put("distinctName", con.getDistinctName());
				contextData.put("aejbName", con.getAEJBName());
				contextData.put("viewClass", con.getRemoteView() == null ? con.getRemoteView() : con.getLocalView());
				contextData.put("stateful",	con.getBeanType() == SessionBeanType.STATEFUL);
			}
		}
		return context.proceed();
	}

	private boolean isValidProtocolName(String protocol) {
		return protocol.equals("tranquility") || protocol.equals("quiescence");
	}
}
