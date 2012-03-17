package org.nju.artemis.aejb.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.as.ejb3.component.session.SessionBeanComponentDescription.SessionBeanType;
import org.jboss.invocation.Interceptor;
import org.jboss.logging.Logger;
import org.nju.artemis.aejb.component.interceptors.DispatcherInterceptor;
import org.nju.artemis.aejb.component.interceptors.InvocationFilterInterceptor;
import org.nju.artemis.aejb.component.interceptors.SwitchInterceptor;
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
	private final String distinctName;
	private final SessionBeanType beanType;
	//Invocation interceptors
	private List<Interceptor> interceptors;
	private Map<String, AEjbStatus> aejbStatus = new HashMap<String, AEjbStatus>();
	private List<Listener<Map<String, AEjbStatus>>> aejbStatusListeners;
	private List<String> depndencies;
	//transaction manager
	private TransactionManager transactionManager;
	//view
	private Class<?> localView, remoteView;
	//switch map
	private Map<String, AcContainer> switchMap = new HashMap<String, AcContainer>();
	//protocols
	private Map<String, String> protocols = new HashMap<String, String>();

	public AcContainer(String appName, String moduleName, String beanName, String distinctName, SessionBeanType beanType) {
		this.appName = appName;
		this.moduleName = moduleName;
		this.beanName = beanName;
		this.aejbName = moduleName + "/" + beanName;
		this.distinctName = distinctName;
		this.beanType = beanType;
	}

	public String getAEJBName() {
		return aejbName;
	}
	
	public String getBeanName() {
		return beanName;
	}

	public String getDistinctName() {
		return distinctName;
	}

	public SessionBeanType getBeanType() {
		return beanType;
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
        interceptors.add(new SwitchInterceptor(this));
        interceptors.add(new InvocationFilterInterceptor(this));
        interceptors.add(new DispatcherInterceptor());
        
		log.info("Start AcContainer for session bean:\n\t ApplicationName = " + appName + ";\n\t ModuleName = " + moduleName + ";\n\t EJBName = " + beanName + ";\n\t EJBType = " + beanType + ";\n\t AEJBName = " + aejbName);
	}
	
	public void stop() {
		interceptors = null;
		aejbStatus.clear();
		aejbStatus = null;
		switchMap.clear();
		switchMap = null;
		depndencies.clear();
		depndencies = null;
		
		log.info("Stop AcContainer, AEJBName = " + aejbName);
	}
	
	public List<Interceptor> getInterceptors() {
		return interceptors;
	}
	
	public void setAEjbStatus(Map<String, AEjbStatus> aejbStatus) {
		this.aejbStatus.putAll(aejbStatus);
		for(Listener<Map<String, AEjbStatus>> aejbStatusListener:aejbStatusListeners) {
			aejbStatusListener.transition(aejbStatus);
		}
	}
	
	public void setAEjbStatus(String name, AEjbStatus aejbStatus) {
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
		if(depndencies.contains(aejbName))
			depndencies.remove(aejbName);
		depndencies.add(aejbName);
	}
	
	public void changeDependencies(String oldD, String newD) {
		addDepndencies(newD);
		depndencies.remove(oldD);
	}
	
	public List<String> getDependencies() {
		return depndencies;
	}
	
	public void setTransactionManager(TransactionManager tm) {
		transactionManager = tm;
	}

	public TransactionManager getTransactionManager() {
		return transactionManager;
	}

	public Class<?> getLocalView() {
		return localView;
	}

	public void setLocalView(Class<?> localView) {
		this.localView = localView;
	}

	public Class<?> getRemoteView() {
		return remoteView;
	}

	public void setRemoteView(Class<?> remoteView) {
		this.remoteView = remoteView;
	}

	public Map<String, AcContainer> getSwitchMap() {
		return switchMap;
	}

	public void setSwitchMap(Map<String, AcContainer> switchMap) {
		this.switchMap.putAll(switchMap);
	}
	
	public void addSwitchMap(String from, AcContainer toContainer) {
		this.switchMap.put(from, toContainer);
	}
	
	public Map<String, String> getProtocols() {
		return protocols;
	}

	public void addProtocol(String name, String protocol) {
		this.protocols.put(name, protocol);
	}
}
