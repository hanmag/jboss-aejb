package org.nju.artemis.aejb.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.nju.artemis.aejb.management.client.AEjbClientImpl.AEjbStatus;

/**
 * This service manages all AEJBs in server.
 * 
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public class AEjbUtilities implements Service<AEjbUtilities>{
	public static final ServiceName SERVICE_NAME = ServiceName.JBOSS.append("aejb", "utilities");
	static Map<String,Map<String,AcContainer>> DeploymentUnits = new HashMap<String,Map<String,AcContainer>>();
	
	public static boolean registerAEJB(String unitName, AcContainer container) {
		if(DeploymentUnits.containsKey(unitName)) {
			AcContainer accon = DeploymentUnits.get(unitName).put(container.getAEJBName(), container);
			if (accon != null)
				return false;
		} else {
			Map<String,AcContainer> aejbs = new HashMap<String,AcContainer>();
			aejbs.put(container.getAEJBName(), container);
			DeploymentUnits.put(unitName, aejbs);
		}
		container.start();
		return true;
	}
	
	public static void removeDeploymentUnit(String unitName){
		if(DeploymentUnits.containsKey(unitName)) {
			Map<String,AcContainer> aejbs = DeploymentUnits.get(unitName);
			Iterator<Entry<String, AcContainer>> iterator = aejbs.entrySet().iterator();
			while(iterator.hasNext()) {
				iterator.next().getValue().stop();
			}
			aejbs = null;
			DeploymentUnits.remove(unitName);
		}
	}
	
	public static Map<String,Map<String,AcContainer>> getAllAEjbsInfo() {
		return DeploymentUnits;
	}

	/**
	 * This menthod used to block or resume components, not use @ComponentLocker because the @param is a Map.
	 * 
	 * @param aejbStatus
	 */
	public void setAEjbStatus(Map<String, AEjbStatus> aejbStatus) {
		for(Entry<String, Map<String, AcContainer>> entry:DeploymentUnits.entrySet()) {
			Map<String, AcContainer> mapValue = entry.getValue();
			for(Entry<String, AcContainer> en:mapValue.entrySet()) {
				Map<String, AEjbStatus> status = new HashMap<String, AEjbStatus>(aejbStatus);
				en.getValue().getEvolutionStatistics().setAEjbStatus(status);
			}
		}
	}
	
	public static AcContainer getContainer(String aejbName) {
		for(Entry<String, Map<String, AcContainer>> entry:DeploymentUnits.entrySet()) {
			Map<String, AcContainer> mapValue = entry.getValue();
			for(Entry<String, AcContainer> en:mapValue.entrySet()) {
				if(aejbName.equals(en.getValue().getAEJBName()))
					return en.getValue();
			}
		}
		return null;
	}
	
	public List<AcContainer> getAllContainers() {
		List<AcContainer> containers = new ArrayList<AcContainer>();
		for(Entry<String, Map<String, AcContainer>> entry:DeploymentUnits.entrySet()) {
			Map<String, AcContainer> mapValue = entry.getValue();
			for(Entry<String, AcContainer> en:mapValue.entrySet()) {
				containers.add(en.getValue());
			}
		}
		return containers;
	}
	
	@Override
	public AEjbUtilities getValue() throws IllegalStateException, IllegalArgumentException {
		return this;
	}

	@Override
	public void start(StartContext context) throws StartException {
	}

	@Override
	public void stop(StopContext context) {
	}
}
