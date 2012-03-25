package org.nju.artemis.aejb.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.as.ejb3.component.session.SessionBeanComponentDescription.SessionBeanType;
import org.jboss.invocation.Interceptor;
import org.jboss.logging.Logger;
import org.nju.artemis.aejb.component.interceptors.DispatcherInterceptor;
import org.nju.artemis.aejb.component.interceptors.InvocationFilterInterceptor;
import org.nju.artemis.aejb.component.interceptors.InvocationDirectInterceptor;
import org.nju.artemis.aejb.component.interceptors.TransactionSecurityInterceptor;
import org.nju.artemis.aejb.deployment.processors.TransactionManager;
import org.nju.artemis.aejb.evolution.handlers.InterfaceCompareHandler;
import org.nju.artemis.aejb.management.client.AEjbClientImpl.AEjbStatus;

/**
 * This class contains all AEjb informations and {@link EvolutionStatistics evolution statistics}.
 * 
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public class AcContainer {
	Logger log = Logger.getLogger(AcContainer.class);
	//Basic information
	final private String appName;
	final private String aejbName;
	final private String moduleName;
	final private String beanName;
	final private String distinctName;
	final private SessionBeanType beanType;
	//Invocation interceptors
	private List<Interceptor> interceptors;
	//Number of running invocation
	private int running;
	//Origin and Runtime depndencies
	private Set<String> originDepndencies;
	private Set<String> runtimeDepndencies;
	//Transaction managers
	private Map<String, TransactionManager> transactionManagers;
	//View classes
	private Set<Class<?>> localViews = new HashSet<Class<?>>();
	private Set<Class<?>> remoteViews = new HashSet<Class<?>>();
	//Evolution statistics
	final private EvolutionStatistics evolutionStatistics = new EvolutionStatistics();

	public AcContainer(String appName, String moduleName, String beanName, String distinctName, SessionBeanType beanType) {
		this.appName = appName == null ? "" : appName;
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
	
	public EvolutionStatistics getEvolutionStatistics() {
		return evolutionStatistics;
	}

	public void start() {
		// Initialize interceptors
        interceptors = new ArrayList<Interceptor>();
        interceptors.add(new TransactionSecurityInterceptor(this));
        interceptors.add(new InvocationFilterInterceptor(this));
        interceptors.add(new InvocationDirectInterceptor(this));
        interceptors.add(new DispatcherInterceptor());
        
		log.info("Start AcContainer for session bean:\n\t ApplicationName = " + appName + ";\n\t ModuleName = "
		+ moduleName + ";\n\t DistinctName = " + distinctName + ";\n\t EJBName = " + beanName + ";\n\t EJBType = " + beanType + ";\n\t AEJBName = " + aejbName);
	}
	
	public void stop() {
		evolutionStatistics.clear();
		interceptors.clear();
		originDepndencies = null;
		runtimeDepndencies = null;
		localViews.clear();
		remoteViews.clear();
		transactionManagers = null;
		
		log.info("Stop AcContainer, AEJBName = " + aejbName);
	}
	
	public List<Interceptor> getInterceptors() {
		return interceptors;
	}
	
	public void addOriginDepndencies(String aejbName) {
		if(originDepndencies == null)
			originDepndencies = new HashSet<String>();
		originDepndencies.add(aejbName);
	}
	
	public void changeRuntimeDependencies(String oldD, String newD) {
		// originDepndencies != null
		if(runtimeDepndencies == null)
			runtimeDepndencies = new HashSet<String>(originDepndencies);
		if (oldD.equals(newD) == false) {
			runtimeDepndencies.add(newD);
			runtimeDepndencies.remove(oldD);
		}
	}
	
	public void addRunning() {
		synchronized (this) {
			running++;
		}
	}
	
	public void removeRunning() {
		synchronized (this) {
			running--;
		}
	}
	
	public boolean isActive() {
		synchronized (this) {
			if (running < 0) {
				log.warn("AcContainer: " + getAEJBName() + " 's running == " + running);
				running = 0;
			}
			return running != 0;
		}
	}
	
	/**
	 * Runtime Dependencies, maybe have been changed
	 * 
	 * @return Null, if no dependency
	 */
	public Set<String> getRuntimeDependencies() {
		if(runtimeDepndencies == null && originDepndencies == null)
			
			return null;
		if(runtimeDepndencies == null)
			runtimeDepndencies = new HashSet<String>(originDepndencies);
		return runtimeDepndencies;
	}
	
	public void setTransactionManager(TransactionManager tm) {
		if(transactionManagers == null)
			transactionManagers = new HashMap<String, TransactionManager>();
		transactionManagers.put(tm.getName(), tm);
	}

	/**
	 * @param transactionName transactionManager.getName()
	 * 
	 * @return Null, if no transaction manager added
	 */
	public TransactionManager getTransactionManager(String transactionName) {
		if(transactionManagers == null)
			return null;
		return transactionManagers.get(transactionName);
	}

	public Set<Class<?>> getLocalView() {
		return localViews;
	}

	public void setLocalView(Class<?> localView) {
		this.localViews.add(localView);
	}

	public Set<Class<?>> getRemoteView() {
		return remoteViews;
	}
	
	public void setRemoteView(Class<?> remoteView) {
		this.remoteViews.add(remoteView);
	}
	
	/**
	 * 
	 * @param viewClass must be an interface
	 * @return return the same class in {@link localViews} and {@link remoteViews} with @viewClass; 
	 * return null if no same class.
	 */
	public Class<?> getViewByClass(Class<?> viewClass) {
		if(viewClass.isInterface() == false)
			return null;
		for(Class<?> remoteView:remoteViews) {
			if(InterfaceCompareHandler.equals(remoteView, viewClass))
				return remoteView;
		}
		for(Class<?> localView:localViews) {
			if(InterfaceCompareHandler.equals(localView, viewClass))
				return localView;
		}
		return null;
	}

	/**
	 * Contains runtime evolution statistics.<br>
	 * <dd>aejbStatus: block or recover state.<br>
	 * <dd>directionalMap: switch or replace to another AEjb.<br>
	 * <dd>protocols: tranquility or quiescence or ...
	 * 
	 * @author Jason
	 */
	public class EvolutionStatistics {
		//Status: block or recover
		private Map<String, AEjbStatus> aejbStatus = new HashMap<String, AEjbStatus>();
		private Set<Listener<Map<String, AEjbStatus>>> aejbStatusListeners = new HashSet<Listener<Map<String, AEjbStatus>>>();;
		//Directional Map
		private Map<String, AcContainer> directionalMap = new HashMap<String, AcContainer>();
		private Map<String, String> tempMap = new HashMap<String, String>();
		//Protocols
		private Map<String, String> protocols = new HashMap<String, String>();
		
		public void clear() {
			aejbStatus.clear();
			aejbStatusListeners.clear();
			directionalMap.clear();
			protocols.clear();
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

		public Map<String, AcContainer> getDirectionalMap() {
			return directionalMap;
		}

		public void addToDirectionalMap(String from, AcContainer toContainer) {
			if((originDepndencies == null || originDepndencies.contains(from) == false) && tempMap.containsKey(from) == false)
				return;
			if(originDepndencies != null && originDepndencies.contains(from)) {
				if(tempMap.containsValue(from) && tempMap.containsKey(from) == false)
					return;
				directionalMap.put(from, toContainer);
				tempMap.put(toContainer.getAEJBName(), from);
				if(tempMap.containsKey(from))
					tempMap.remove(from);
			} else if(tempMap.containsKey(from)) {
				String dependency = tempMap.remove(from);
				directionalMap.put(dependency, toContainer);
				tempMap.put(toContainer.getAEJBName(), dependency);
			}
			if(directionalMap.size() != tempMap.size())
				log.info("directional map have some errors");
		}
		
		public Map<String, String> getProtocols() {
			return protocols;
		}

		public void addProtocol(String name, String protocol) {
			protocols.put(name, protocol);
		}
		
		public String getProtocolByBeanName(String name) {
			return protocols.get(name);
		}
	}
}
