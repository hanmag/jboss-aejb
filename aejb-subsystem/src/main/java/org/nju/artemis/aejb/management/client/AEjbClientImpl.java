package org.nju.artemis.aejb.management.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jboss.logging.Logger;
import org.nju.artemis.aejb.component.AEjbUtilities;
import org.nju.artemis.aejb.component.AcContainer;

/**
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public class AEjbClientImpl implements AEjbClient {
	private static final long serialVersionUID = 1L;
	Logger log = Logger.getLogger(AEjbClientImpl.class);
	private Map<String, String[]> aejbInfos = new HashMap<String,String[]>();
	private Set<String> aejbNames = new HashSet<String>();
	private Map<String, AEjbStatus> aejbStatus = new HashMap<String,AEjbStatus>();
	private Map<String, String> switchMap = new HashMap<String, String>();
	private Map<String, String> protocols = new HashMap<String, String>();
	public static final AEjbClientImpl INSTANCE = new AEjbClientImpl();
	
	public enum AEjbStatus {
        BLOCKING,
        RESUMING,
    }
	
	private AEjbClientImpl(){
	}
	
	public Map<String,String[]> listAEjbs(){
		return aejbInfos;
	}
	
	void refreshAEjbsInfo() {
		Map<String, Map<String,AcContainer>> deploymentUnits = AEjbUtilities.getAllAEjbsInfo();
		Iterator<Entry<String, Map<String, AcContainer>>> iterator = deploymentUnits.entrySet().iterator();
		reset();
		
		while(iterator.hasNext()) {
			Entry<String, Map<String, AcContainer>> entry = iterator.next();
			Map<String, AcContainer> aejbs = entry.getValue();
			int size = aejbs.size();
			Collection<String> aejbNames = new HashSet<String>(aejbs.keySet());
			aejbInfos.put(entry.getKey(), aejbs.keySet().toArray(new String[size]));
			this.aejbNames.addAll(aejbNames);
		}
	}
	
	private void reset() {
		aejbInfos.clear();
		aejbNames.clear();
	}
	
	public boolean blockAEjb(String aejbName) {
		if(aejbStatus.containsKey(aejbName) && aejbStatus.get(aejbName) == AEjbStatus.BLOCKING)
			return true;
		else if(aejbStatus.containsKey(aejbName) || aejbNames.contains(aejbName)) {
			aejbStatus.put(aejbName, AEjbStatus.BLOCKING);
			return true;
		}
		return false;
	}
	
	public Map<String, AEjbStatus> getAEjbStatus() {
		return aejbStatus;
	}
	
	public AEjbStatus getAEjbStatus(String aejbName) {
		return aejbStatus.get(aejbName);
	}
	
	public void clearAEjbStatus() {
		aejbStatus.clear();
	}
	
	public void clearSwitchMap() {
		switchMap.clear();
	}

	@Override
	public boolean resumeAEjb(String aejbName) {
		if(aejbStatus.containsKey(aejbName) && aejbStatus.get(aejbName) == AEjbStatus.RESUMING)
			return true;
		else if(aejbStatus.containsKey(aejbName) || aejbNames.contains(aejbName)) {
			aejbStatus.put(aejbName, AEjbStatus.RESUMING);
			return true;
		}
		return false;
	}

	@Override
	public boolean switchAEjb(String fromName, String toName, String protocol) {
		if(switchMap.containsKey(fromName) && toName.equals(switchMap.get(fromName))) {
			protocols.put(fromName, protocol);
			return true;
		}
		if(aejbNames.contains(fromName) && aejbNames.contains(toName)) {
			switchMap.put(fromName, toName);
			protocols.put(fromName, protocol);
			return true;
		}
		return false;
	}

	@Override
	public Map<String, String> getSwitchMap() {
		return switchMap;
	}

	@Override
	public String getProtocol(String name) {
		return protocols.get(name);
	}

	@Override
	public boolean replaceAEjb(String fromName, String toName, String protocol) {
		// TODO Auto-generated method stub
		return false;
	}
}
