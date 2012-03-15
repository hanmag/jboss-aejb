package org.nju.artemis.aejb.client;


/**
 * @author <a href="wangjue1199@gmail.com">Jason</a>
 */
public interface AEjbClient {

	Object getAEjbClient();
	
	boolean blockAEjb(String aejbName);
	
	boolean resumeAEjb(String aejbName);
}
