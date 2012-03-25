package org.nju.artemis.aejb.evolution.protocols;

import org.nju.artemis.aejb.component.AcContainer;
import org.nju.artemis.aejb.deployment.processors.TransactionManager;

/**
 * Evolution protocol
 * 
 * @author <a href="mailto:wangjue1199@gmail.com">Jason</a>
 */
public interface Protocol {

	/**
	 * Instance level
	 * 
	 * @param targetName target updating AEjb name
	 * @param objectId neighbor AEjb's instance id
	 * @param transactionManager neighbor AEjb's corresponding transaction manager
	 * @return <b>true:</b> safe<br><b>false</b>: unsafe
	 */
	boolean checkTransactionSecurity(String targetName,	String objectId, TransactionManager transactionManager);
	
	String getName();
	
	/**
	 * Component level
	 * 
	 * @param container
	 * @return <b>true:</b> success<br><b>false</b>: unsuccess
	 */
	boolean setToSafePoint(AcContainer container);
}
