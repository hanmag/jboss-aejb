package org.nju.artemis.aejb.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.as.ejb3.component.session.SessionBeanComponentDescription.SessionBeanType;
import org.jboss.invocation.Interceptor;
import org.jboss.logging.Logger;
import org.nju.artemis.aejb.component.interceptors.DispatcherInterceptor;
import org.nju.artemis.aejb.component.interceptors.InvocationFilterInterceptor;
import org.nju.artemis.aejb.deployment.processors.TransactionManager;
import org.nju.artemis.aejb.management.client.AEjbClientImpl.AEjbStatus;

/**
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public class AcContainer {
	Logger log = Logger.getLogger(AcContainer.class);
	
	private final String appName;
	private final String aejbName;
	private final String moduleName;
	private final String beanName;
	private final SessionBeanType beanType;
	//Invocation interceptors
	private List<Interceptor> interceptors;
	private Map<String, AEjbStatus> aejbStatus;
	private List<Listener<Map<String, AEjbStatus>>> aejbStatusListeners;
	private List<String> depndencies;

	public AcContainer(String appName, String moduleName, String beanName, SessionBeanType beanType) {
		this.appName = appName;
		this.moduleName = moduleName;
		this.beanName = beanName;
		this.aejbName = moduleName + "/" + beanName;
		this.beanType = beanType;
	}

	public String getAEJBName() {
		return aejbName;
	}
	
	public String getApplicationName() {
		return appName;
	}

	public String getModuleName() {
		return moduleName;
	}
	
	public void start() {
		aejbStatusListeners = new ArrayList<Listener<Map<String, AEjbStatus>>>();
		// Initialize interceptors
        interceptors = new ArrayList<Interceptor>();
        interceptors.add(new InvocationFilterInterceptor(this));
        interceptors.add(new DispatcherInterceptor());
        
		log.info("Start AcContainer for session bean:\n\t ApplicationName = " + appName + ";\n\t ModuleName = " + moduleName + ";\n\t EJBName = " + beanName + ";\n\t EJBType = " + beanType + ";\n\t AEJBName = " + aejbName);
	}
	
	public void stop() {
		interceptors = null;
		
		log.info("Stop AcContainer, AEJBName = " + aejbName);
	}
	
	public List<Interceptor> getInterceptors() {
		return interceptors;
	}
	
	public void setAEjbStatus(Map<String, AEjbStatus> aejbStatus) {
		this.aejbStatus = aejbStatus;
		for(Listener<Map<String, AEjbStatus>> aejbStatusListener:aejbStatusListeners) {
			aejbStatusListener.transition(aejbStatus);
		}
	}
	
	public void setAEjbStatus(String name, AEjbStatus aejbStatus) {
		this.aejbStatus.clear();
		this.aejbStatus.put(name, aejbStatus);
		for(Listener<Map<String, AEjbStatus>> aejbStatusListener:aejbStatusListeners) {
			aejbStatusListener.transition(this.aejbStatus);
		}
	}

	public Map<String, AEjbStatus> getAEjbStatus() {
		return aejbStatus;
	}

	public void addStatusListener(Listener<Map<String, AEjbStatus>> aejbStatusListener) {
		aejbStatusListeners.add(aejbStatusListener);
	}
	
	public void addDepndencies(String aejbName) {
		if(depndencies == null)
			depndencies = new ArrayList<String>();
		depndencies.add(aejbName);
	}
	
	public List<String> getDependencies() {
		return depndencies;
	}
	
	public void setTransactionManager(TransactionManager tm) {
		
	}
}
