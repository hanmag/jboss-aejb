package org.nju.artemis.aejb.management.client;

import java.io.Serializable;
import java.util.Map;

import org.nju.artemis.aejb.management.client.AEjbClientImpl.AEjbStatus;

/**
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public interface AEjbClient extends Serializable{

	Map<String,String[]> listAEjbs();
	
	Map<String, AEjbStatus> getAEjbStatus();
	
	AEjbStatus getAEjbStatus(String aejbName);
	
	boolean blockAEjb(String aejbName);
	
	boolean resumeAEjb(String aejbName);
	
	void clearAEjbStatus();
	
	void clearSwitchMap();
	
	boolean switchAEjb(String fromName, String toName, String protocol);
	
	Map<String, String> getSwitchMap();
	
	String getProtocol(String name);
	
	boolean replaceAEjb(String fromName, String toName, String protocol);
}
